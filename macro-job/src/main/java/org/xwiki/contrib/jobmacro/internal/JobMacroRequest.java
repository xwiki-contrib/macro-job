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

import java.util.ArrayList;
import java.util.List;

import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.jobmacro.internal.JobMacro.SERIALIZE;
import org.xwiki.job.AbstractRequest;
import org.xwiki.job.JobGroupPath;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Job Request for the {@link JobMacroJob}.
 *
 * @version $Id$
 */
public class JobMacroRequest extends AbstractRequest
{
    private static final long serialVersionUID = 1L;

    private String content;

    private JobGroupPath groupPath;

    private List<SERIALIZE> serialize;

    private transient ExecutionContext executionContext;

    private transient MacroTransformationContext transformationContext;

    /**
     * @return the content to parse and execute.
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Set the content to parse and execute.
     * 
     * @param content the content
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * @return the group path or null if for a single job.
     */
    public JobGroupPath getGroupPath()
    {
        return groupPath;
    }

    /**
     * Set the group path. Set to null for a single job.
     * 
     * @param groupPath the group path.
     */
    public void setGroupPath(JobGroupPath groupPath)
    {
        this.groupPath = groupPath;
    }

    /**
     * @return the execution context to initialize for this job.
     */
    public ExecutionContext getExecutionContext()
    {
        return executionContext;
    }

    /**
     * Set the execution context used for executing the job.
     * 
     * @param executionContext the execution context
     */
    public void setExecutionContext(ExecutionContext executionContext)
    {
        this.executionContext = executionContext;
    }

    /**
     * @return the macro transformation context for rendering the macro content.
     */
    public MacroTransformationContext getTransformationContext()
    {
        return transformationContext;
    }

    /**
     * Set the macro transformation context for rendering the macro content.
     * 
     * @param transformationContext the macro transformation context
     */
    public void setTransformationContext(MacroTransformationContext transformationContext)
    {
        this.transformationContext = transformationContext;
    }

    /**
     * @return the logging level.
     * @deprecated since 2.2.0, it's not taken into account anymore
     */
    @Deprecated
    public List<SERIALIZE> getSerialize()
    {
        return serialize;
    }

    /**
     * Set the logging level.
     * 
     * @param serialize a collection of object enabled for serialization.
     * @deprecated since 2.2.0, it's not taken into account anymore
     */
    @Deprecated
    public void setSerialize(List<SERIALIZE> serialize)
    {
        this.serialize = serialize;
    }

    /**
     * Enable logging of the given information.
     * 
     * @param serialize the object to serialize
     * @return true if the level has been enabled, false if it was already enabled
     * @deprecated since 2.2.0, it's not taken into account anymore
     */
    @Deprecated
    public boolean enableSerialize(SERIALIZE serialize)
    {
        if (this.serialize == null) {
            this.serialize = new ArrayList<>();
        }

        if (!this.serialize.contains(serialize)) {
            return this.serialize.add(serialize);
        }
        return false;
    }

    /**
     * Disable logging of the given information.
     * 
     * @param serialize the object to remove from serialization
     * @return true if the level has been activated, false if it was already active
     * @deprecated since 2.2.0, it's not taken into account anymore
     */
    @Deprecated
    public boolean disableSerialize(SERIALIZE serialize)
    {
        if (serialize != null && this.serialize != null && this.serialize.contains(serialize)) {
            return this.serialize.remove(serialize);
        }
        return false;
    }
}
