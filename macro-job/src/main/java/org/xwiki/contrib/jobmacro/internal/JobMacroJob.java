/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.contrib.jobmacro.internal;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextInitializer;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.event.JobFinishedEvent;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.marker.BeginTranslationMarker;
import org.xwiki.logging.marker.EndTranslationMarker;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWikiContext;

/**
 * Job executed by the JobMacro.
 *
 * @version $Id$
 */
@Component
@Named(JobMacroJob.JOBTYPE)
public class JobMacroJob extends AbstractJob<JobMacroRequest, DefaultJobStatus<JobMacroRequest>>
    implements GroupedJob
{
    /**
     * Job Type.
     */
    public static final String JOBTYPE = "macrojob";

    private static final BeginTranslationMarker LOG_BEGIN = new BeginTranslationMarker("job.log.begin");

    private static final BeginTranslationMarker LOG_BEGIN_ID = new BeginTranslationMarker("job.log.beginWithId");

    private static final EndTranslationMarker LOG_END = new EndTranslationMarker("job.log.end");

    private static final EndTranslationMarker LOG_END_ID = new EndTranslationMarker("job.log.endWithId");

    private static final TranslationMarker LOG_STATUS_STORE_FAILED =
        new TranslationMarker("job.log.status.store.failed");

    private static final String JOB_ID_VARIABLE = "jobId";

    private static final String GROUP_PATH_VARIABLE = "groupPath";

    private static final String PROGRESS_VARIABLE = "progress";

    @Inject
    private Execution execution;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private VelocityManager velocityManager;

    @Inject
    private MacroContentParser contentParser;

    @Inject
    private Container container;

    @Inject
    @Named("threadclassloader")
    private ExecutionContextInitializer classLoaderInitializer;

    @Override
    public JobGroupPath getGroupPath()
    {
        return this.request.getGroupPath();
    }

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    public void run()
    {
        try {
            this.execution.setContext(request.getExecutionContext());
            runInContext();
        } finally {
            execution.removeContext();
        }
    }

    @Override
    protected void runInternal() throws Exception
    {
        // Restore the request before executing the content
        ExecutionContext executionContext = request.getExecutionContext();
        // the class loader is thread sensitive, thus it needs to be initialized here (in the thread of the job)
        classLoaderInitializer.initialize(executionContext);
        XWikiContext context = (XWikiContext) executionContext.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        container.setRequest(new ServletRequest(context.getRequest()));

        ScriptContext scontext = scriptContextManager.getScriptContext();

        // Clean the script context from known thread unsafe objects or object referencing the wrong XWikiContext
        scontext.removeAttribute("util", ScriptContext.ENGINE_SCOPE);
        scontext.removeAttribute("xwiki", ScriptContext.ENGINE_SCOPE);
        scontext.removeAttribute("request", ScriptContext.ENGINE_SCOPE);
        scontext.removeAttribute("response", ScriptContext.ENGINE_SCOPE);
        scontext.removeAttribute("xcontext", ScriptContext.ENGINE_SCOPE);

        scontext.setAttribute(JOB_ID_VARIABLE, this.request.getId(), ScriptContext.ENGINE_SCOPE);
        scontext.setAttribute(GROUP_PATH_VARIABLE, this.request.getGroupPath(), ScriptContext.ENGINE_SCOPE);
        scontext.setAttribute(PROGRESS_VARIABLE, this.progressManager, ScriptContext.ENGINE_SCOPE);

        VelocityContext vcontext = velocityManager.getVelocityContext();
        vcontext.put(JOB_ID_VARIABLE, this.request.getId());
        vcontext.put(GROUP_PATH_VARIABLE, this.request.getGroupPath());
        vcontext.put(PROGRESS_VARIABLE, this.progressManager);

        this.contentParser.parse(request.getContent(), request.getTransformationContext(), true, false);
    }

    /**
     * Called when the job is starting.
     */
    @Override
    protected void jobStarting()
    {
        this.jobContext.pushCurrentJob(this);

        this.observationManager.notify(new JobStartedEvent(getRequest().getId(), getType(), this.request), this);

        this.status.setStartDate(new Date());
        this.status.setState(JobStatus.State.RUNNING);

        this.status.startListening();

        if (getRequest().isVerbose()) {
            if (getStatus().getRequest().getId() != null) {
                this.logger.info(LOG_BEGIN_ID, "Starting job of type [{}] with identifier [{}]", getType(),
                    getStatus().getRequest().getId());
            } else {
                this.logger.info(LOG_BEGIN, "Starting job of type [{}]", getType());
            }
        }
    }

    /**
     * Called when the job is done.
     *
     * @param error the exception throw during execution of the job
     */
    @Override
    protected void jobFinished(Throwable error)
    {
        this.lock.lock();

        try {
            this.status.setError(error);

            // Give a chance to any listener to do custom action associated to the job
            //this.observationManager.notify(new JobFinishingEvent(getRequest().getId(), getType(), this.request), this,
            //    error);

            if (getRequest().isVerbose()) {
                if (getStatus().getRequest().getId() != null) {
                    this.logger.info(LOG_END_ID, "Finished job of type [{}] with identifier [{}]", getType(),
                        getStatus().getRequest().getId());
                } else {
                    this.logger.info(LOG_END, "Finished job of type [{}]", getType());
                }
            }

            // Indicate when the job ended
            this.status.setEndDate(new Date());

            // Stop updating job status (progress, log, etc.)
            this.status.stopListening();

            // Update job state
            this.status.setState(JobStatus.State.FINISHED);

            // Release threads waiting for job being done
            this.finishedCondition.signalAll();

            // Remove the job from the current jobs context
            this.jobContext.popCurrentJob();

            // Store the job status
            try {
                if (this.request.getId() != null) {
                    // Get rid of things which should not be stored and could take a lot of memory in the status cache
                    this.status.getRequest().setExecutionContext(null);
                    this.status.getRequest().setTransformationContext(null);

                    this.store.storeAsync(this.status);
                }
            } catch (Throwable t) {
                this.logger.warn(LOG_STATUS_STORE_FAILED, "Failed to store job status [{}]", this.status, t);
            }
        } finally {
            this.lock.unlock();

            // Notify listener that job is fully finished
            this.observationManager.notify(new JobFinishedEvent(getRequest().getId(), getType(), this.request), this,
                error);
        }
    }
}
