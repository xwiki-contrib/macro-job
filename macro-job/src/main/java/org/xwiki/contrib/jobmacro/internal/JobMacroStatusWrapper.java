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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.xwiki.contrib.jobmacro.internal.JobMacro.SERIALIZE;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.event.status.JobProgress;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.observation.ObservationManager;

/**
 * Contains information about the running job.
 * 
 * @since 1.0
 * @version $Id$
 */
public class JobMacroStatusWrapper implements JobStatus, Serializable
{
    private static final long serialVersionUID = 1L;

    private final transient DefaultJobStatus<JobMacroRequest> jobStatus;

    private transient Throwable error;

    /**
     * Constructor.
     *
     * @param request the request
     * @param observationManager the observation manager
     * @param loggerManager the logger manager
     * @param parentJobStatus the parent job status, if any
     */
    public JobMacroStatusWrapper(JobMacroRequest request, ObservationManager observationManager,
        LoggerManager loggerManager, JobStatus parentJobStatus)
    {
        this.jobStatus = new DefaultJobStatus<>(request, parentJobStatus, observationManager, loggerManager);
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        JobMacroStatus status = new JobMacroStatus();

        List<SERIALIZE> serialize = getRequest().getSerialize();

        if (serialize.contains(SERIALIZE.PROGRESS)) {
            status.progress = getProgress();
        }

        if (serialize.contains(SERIALIZE.LOGS)) {
            status.logs = getLog();
        }

        status.error = getError();
        status.state = getState();

        if (serialize.contains(SERIALIZE.REQUEST)) {
            status.request = getRequest();
        }

        status.startDate = getStartDate();
        status.endDate = getEndDate();
        status.isolated = jobStatus.isIsolated();

        out.writeObject(status);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public State getState()
    {
        return jobStatus.getState();
    }

    @Override
    public JobMacroRequest getRequest()
    {
        return jobStatus.getRequest();
    }

    @Override
    public LogQueue getLog()
    {
        return jobStatus.getLog();
    }

    @Override
    public JobProgress getProgress()
    {
        return jobStatus.getProgress();
    }

    @Override
    public void ask(Object question) throws InterruptedException
    {
        jobStatus.ask(question);
    }

    @Override
    public Object getQuestion()
    {
        return jobStatus.getQuestion();
    }

    @Override
    public void answered()
    {
        jobStatus.answered();
    }

    @Override
    public Date getStartDate()
    {
        return jobStatus.getStartDate();
    }

    @Override
    public Date getEndDate()
    {
        return jobStatus.getEndDate();
    }

    @Override
    public List<LogEvent> getLog(LogLevel logLevel)
    {
        return jobStatus.getLog(logLevel);
    }

    /**
     * @param date the start date
     * @see DefaultJobStatus#setStartDate(Date)
     */
    public void setStartDate(Date date)
    {
        jobStatus.setStartDate(date);
    }

    /**
     * @param state the state
     * @see DefaultJobStatus#setState(State)
     */
    public void setState(State state)
    {
        jobStatus.setState(state);
    }

    /**
     * @see DefaultJobStatus#startListening()
     */
    public void startListening()
    {
        jobStatus.startListening();
    }

    /**
     * @param date the end date
     * @see DefaultJobStatus#setEndDate(Date) 
     */
    public void setEndDate(Date date)
    {
        jobStatus.setEndDate(date);
    }

    /**
     * @see DefaultJobStatus#stopListening()
     */
    public void stopListening()
    {
        jobStatus.stopListening();
    }

    /**
     * @return the last error
     */
    public Throwable getError()
    {
        return this.error;
    }

    /**
     * @param error the last error of the job
     */
    public void setError(Throwable error)
    {
        this.error = error;
    }
}
