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

import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogQueue;

/**
 * The status of a job triggered run in the job macro.
 *
 * @since 1.0
 * @version $Id$
 */
public class JobMacroStatus
{
    /**
     * Job progress.
     */
    public JobProgress progress;

    /**
     * Job logs.
     */
    public LogQueue logs;

    /**
     * Job type.
     */
    public String jobType = JobMacroJob.JOBTYPE;

    /**
     * Job state.
     */
    public JobStatus.State state;

    /**
     * Job thrown exception.
     */
    public Throwable error;

    /**
     * Job request.
     */
    public JobMacroRequest request;

    /**
     * Job start date.
     */
    public Date startDate;

    /**
     * Job end date.
     */
    public Date endDate;

    /**
     * Job log isolation.
     */
    public boolean isolated;
}
