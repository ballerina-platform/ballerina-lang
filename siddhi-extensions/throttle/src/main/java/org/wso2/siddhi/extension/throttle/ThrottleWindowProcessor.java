/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.extension.throttle;

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
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;

public class ThrottleWindowProcessor extends WindowProcessor implements Job{

    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private StreamEvent firstEventOfBatch = new StreamEvent(0,0,0);
    private ExecutionPlanContext executionPlanContext;
    private Scheduler scheduler;
    private String jobName;
    private final String jobGroup = "CronWindowGroup";
    private String cronString;


    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        if (attributeExpressionExecutors != null) {
            cronString = (String) (((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue());
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        if (firstEventOfBatch.getNext() == null) {
            if (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                firstEventOfBatch = streamEventCloner.copyStreamEvent(streamEvent);
            }
        }
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
            currentEventChunk.add(clonedStreamEvent);
            streamEventChunk.remove();
        }
        dispatchCurrentEvents();
    }

    @Override
    public void start() {
        scheduleCronJob(cronString, elementId);
    }

    @Override
    public void stop() {
        try {
            scheduler.deleteJob(new JobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            log.error("Error while removing the cron job : " + e.getMessage(), e);
        }
    }

    @Override
    public Object[] currentState() {
        return new Object[]{currentEventChunk, firstEventOfBatch};
    }

    @Override
    public void restoreState(Object[] state) {
        currentEventChunk = (ComplexEventChunk<StreamEvent>) state[0];
        firstEventOfBatch = (StreamEvent) state[1];
    }

    private void scheduleCronJob(String cronString, String elementId) {
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            scheduler.start();

            JobDataMap dataMap = new JobDataMap();
            dataMap.put("windowProcessor", this);

            jobName = "EventRemoverJob_" + elementId;
            JobDetail job = org.quartz.JobBuilder.newJob(ThrottleWindowProcessor.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData(dataMap)
                    .build();

            Trigger trigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("EventRemoverTrigger_" + elementId, jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            log.error("Error while instantiating quartz scheduler", e);
        }
    }

    public void dispatchCurrentEvents() {
        ComplexEventChunk<StreamEvent> streamEventChunk = new ComplexEventChunk<StreamEvent>();

        if (currentEventChunk.getFirst() != null) {
            streamEventChunk.add(currentEventChunk.getFirst());
            currentEventChunk.clear();
            nextProcessor.process(streamEventChunk);
        }
    }

    public void dispatchResetEvent() {
        ComplexEventChunk<StreamEvent> streamEventChunk = new ComplexEventChunk<StreamEvent>();

        firstEventOfBatch.setType(StreamEvent.Type.RESET);
        streamEventChunk.add(firstEventOfBatch);
        nextProcessor.process(streamEventChunk);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Running Window Reset Job");
        }

        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ThrottleWindowProcessor windowProcessor = (ThrottleWindowProcessor) dataMap.get("windowProcessor");
        windowProcessor.dispatchResetEvent();

    }
}
