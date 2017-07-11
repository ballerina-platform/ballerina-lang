/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.trigger;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;

/**
 * Implementation of {@link EventTrigger} which will trigger events based on a cron expression.
 */
public class CronEventTrigger implements EventTrigger, Job {

    protected static final Logger LOG = Logger.getLogger(CronEventTrigger.class);

    private TriggerDefinition triggerDefinition;
    private SiddhiAppContext siddhiAppContext;
    private StreamJunction streamJunction;
    private Scheduler scheduler;
    private String jobName;
    private String jobGroup = "TriggerGroup";

    @Override
    public void init(TriggerDefinition triggerDefinition, SiddhiAppContext siddhiAppContext, StreamJunction
            streamJunction) {

        this.triggerDefinition = triggerDefinition;
        this.siddhiAppContext = siddhiAppContext;
        this.streamJunction = streamJunction;
    }

    @Override
    public TriggerDefinition getTriggerDefinition() {
        return triggerDefinition;
    }

    @Override
    public String getId() {
        return triggerDefinition.getId();
    }

    /**
     * This will be called only once and this can be used to acquire
     * required resources for the processing element.
     * This will be called after initializing the system and before
     * starting to process the events.
     */
    @Override
    public void start() {
        scheduleCronJob(triggerDefinition.getAt(), triggerDefinition.getId());
    }

    /**
     * This will be called only once and this can be used to release
     * the acquired resources for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {
        try {
            if (scheduler != null) {
                scheduler.deleteJob(new JobKey(jobName, jobGroup));
            }
        } catch (SchedulerException e) {
            LOG.error("Error while removing the cron trigger job, " + e.getMessage(), e);
        }
    }

    private void scheduleCronJob(String cronString, String elementId) {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            jobName = "TriggerJob_" + elementId;
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            scheduler.start();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("trigger", this);

            JobDetail job = org.quartz.JobBuilder.newJob(CronEventTrigger.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData(dataMap)
                    .build();

            Trigger trigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("TriggerJob_" + elementId, jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            LOG.error("Error while instantiating quartz scheduler for trigger '" + triggerDefinition.getId() + "'," +
                    e.getMessage(), e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {


        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        CronEventTrigger cronEventTrigger = (CronEventTrigger) dataMap.get("trigger");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running Trigger Job '" + cronEventTrigger.getId() + "'");
        }
        cronEventTrigger.sendEvent();
    }

    private void sendEvent() {
        long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
        streamJunction.sendEvent(new Event(currentTime, new Object[]{currentTime}));
    }
}
