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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.internal.AbstractJob;
import org.xwiki.job.internal.DefaultJobStatus;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.velocity.VelocityManager;

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

    private static final TranslationMarker LOG_EXCEPTION = new TranslationMarker("job.log.exception");

    @Inject
    private Execution execution;

    @Inject
    private VelocityManager velocityManager;

    @Inject
    private MacroContentParser contentParser;

    /**
     * Default constructor.
     */
    public JobMacroJob()
    {
        super();
        this.initExecutionContext = false;
    }

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
    protected void runInContext() {
        Throwable error = null;
        this.execution.setContext(request.getExecutionContext());

        try {
            this.jobStarting();
            this.runInternal();
        } catch (Throwable e) {
            this.logger.error(LOG_EXCEPTION, "Exception thrown during job execution", e);
            error = e;
        } finally {
            this.jobFinished(error);
        }
    }

    @Override
    protected void runInternal() throws Exception
    {
        VelocityContext vcontext = velocityManager.getVelocityContext();
        vcontext.put("jobId", this.request.getId());
        vcontext.put("groupPath", this.request.getGroupPath());
        vcontext.put("progress", this.progressManager);

        this.contentParser.parse(request.getContent(), request.getTransformationContext(), true, false);
    }
}
