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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.jobmacro.JobMacroParameters;
import org.xwiki.contrib.jobmacro.internal.context.Copier;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.JobGroupPath;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.AbstractSignableMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.security.authorization.AccessDeniedException;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

/**
 * Job Macro.
 *
 * @version $Id$
 */
@Component
@Named("job")
public class JobMacro extends AbstractSignableMacro<JobMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Execute the macro content asynchronously and display progress.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The code that should be executed asynchronously";

    private static final String PROPERTY_JOB_TYPE = "job.type";

    private static final String JOBID_PARAMETER = "jobid";

    private static final String JOB_PROGRESS_MACRO = "jobprogress";

    /**
     * Used to access the current context and clone it.
     */
    @Inject
    private Execution execution;

    @Inject
    private Copier<ExecutionContext> executionContextCloner;

    /**
     * Used to retrieve the job that is currently being executed.
     */
    @Inject
    private JobExecutor jobExecutor;

    /**
     * The parser used to parse the parameters.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Renderer used to render parameters.
     */
    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextBlockRenderer;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public JobMacro()
    {
        super("Job", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), JobMacroParameters.class);

        setDefaultCategory(DEFAULT_CATEGORY_DEVELOPMENT);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(JobMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        String id = parseToPlainText(parameters.getId(), context);
        List<String> jobId = asList(id);

        Job job = this.jobExecutor.getJob(jobId);

        if (job == null
            && Boolean.parseBoolean(parseToPlainText(parameters.getStart(), context))) {
            try {
                startJob(parseToPlainText(parameters.getJobType(), context), jobId,
                    asList(parseToPlainText(parameters.getGroupPath(), context)), content, context);
            } catch (Exception e) {
                throw new MacroExecutionException("Failed starting job", e);
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put(JOBID_PARAMETER, id);

        return Collections.<Block>singletonList(new MacroBlock(JOB_PROGRESS_MACRO, params, false));
    }

    private void startJob(String jobType, List<String> jobId, List<String> groupPath, String content,
        MacroTransformationContext context) throws MacroExecutionException, AccessDeniedException, JobException
    {
        contextualAuthorizationManager.checkAccess(Right.PROGRAM);

        this.jobExecutor.execute(JobMacroJob.JOBTYPE,
            getJobMacroRequest(jobType, jobId, groupPath, content, context));
    }

    private JobMacroRequest getJobMacroRequest(String jobType, List<String> jobId, List<String> groupPath,
        String content, MacroTransformationContext context)
    {
        ExecutionContext executionContext = this.execution.getContext();
        ExecutionContext clonedExecutionContext = this.executionContextCloner.copy(executionContext);

        JobMacroRequest request = new JobMacroRequest();
        request.setId(jobId);
        if (groupPath != null && groupPath.size() > 0) {
            request.setGroupPath(new JobGroupPath(groupPath));
        }
        request.setContent(content);
        request.setRemote(false);
        request.setExecutionContext(clonedExecutionContext);
        request.setTransformationContext(context.clone());
        request.setProperty(PROPERTY_JOB_TYPE, jobType);
        return request;
    }

    private List<String> asList(String id)
    {
        return Arrays.asList(id.split("/"));
    }

    private String parseToPlainText(String param, MacroTransformationContext context) throws MacroExecutionException
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        this.plainTextBlockRenderer.render(this.contentParser.parse(param, context, true, false), printer);
        return printer.toString();
    }
}
