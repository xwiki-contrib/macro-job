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
import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.internal.AbstractJob;
import org.xwiki.job.internal.DefaultJobStatus;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.script.ScriptContextManager;
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
        ScriptContext scontext = scriptContextManager.getScriptContext();
        scontext.setAttribute(JOB_ID_VARIABLE, this.request.getId(), ScriptContext.ENGINE_SCOPE);
        scontext.setAttribute(GROUP_PATH_VARIABLE, this.request.getGroupPath(), ScriptContext.ENGINE_SCOPE);
        scontext.setAttribute(PROGRESS_VARIABLE, this.progressManager, ScriptContext.ENGINE_SCOPE);

        VelocityContext vcontext = velocityManager.getVelocityContext();
        vcontext.put(JOB_ID_VARIABLE, this.request.getId());
        vcontext.put(GROUP_PATH_VARIABLE, this.request.getGroupPath());
        vcontext.put(PROGRESS_VARIABLE, this.progressManager);

        this.contentParser.parse(request.getContent(), request.getTransformationContext(), true, false);
    }
}
