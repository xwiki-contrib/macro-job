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

package org.xwiki.contrib.jobmacro;

import org.xwiki.contrib.jobmacro.internal.JobMacro;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyMandatory;

/**
 * Parameters for the {@link JobMacro} Macro.
 *
 * @version $Id$
 */
public class JobMacroParameters
{
    /**
     * @see #getJobType()
     */
    private String jobType = "JobMacro";

    /**
     * @see #getId()
     */
    private String id;

    /**
     * @see #getGroupPath()
     */
    private String groupPath;

    /**
     * @see #getSerialize()
     */
    private String serialize;

    /**
     * @see #getStart()
     */
    private String start = "false";

    /**
     * @return the job type (used for translations)
     */
    public String getJobType()
    {
        return jobType;
    }

    /**
     * @return the id parameter
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the groupPath parameter
     */
    public String getGroupPath()
    {
        return this.groupPath;
    }

    /**
     * @return the serialize parameter
     */
    public String getSerialize()
    {
        return this.serialize;
    }

    /**
     * @return true if the job should be start if it is not running.
     */
    public String getStart()
    {
        return start;
    }

    /**
     * @param jobType the job type
     */
    @PropertyDescription("Job type used to name this kind of job.")
    public void setJobType(String jobType)
    {
        this.jobType = jobType;
    }

    /**
     * @param jobId the job identifier
     */
    @PropertyMandatory
    @PropertyDescription("Unique identifier for this job. (Wiki syntax supported)")
    public void setId(String jobId)
    {
        this.id = jobId;
    }

    /**
     * @param groupPath the group path
     */
    @PropertyDescription("Group path of this job")
    public void setGroupPath(String groupPath)
    {
        this.groupPath = groupPath;
    }

    /**
     * @param serialize the list of object to serialize in the status as a lowercase comma separated list
     */
    @PropertyDescription("Elements to serialize in the logs as a comma separated list (progress, logs, request)")
    public void setSerialize(String serialize)
    {
        this.serialize = serialize;
    }

    /**
     * @param start if true, restart the job even if existing logs exists.
     */
    @PropertyDescription("Start the job if it is not running. Default to false. (Wiki syntax supported)")
    public void setStart(String start)
    {
        this.start = start;
    }
}
