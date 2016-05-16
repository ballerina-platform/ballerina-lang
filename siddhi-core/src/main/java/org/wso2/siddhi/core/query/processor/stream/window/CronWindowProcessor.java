/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.processor.stream.window;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;

public class CronWindowProcessor extends WindowProcessor implements Job {

    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
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
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
                currentEventChunk.add(clonedStreamEvent);
                streamEventChunk.remove();
            }
        }
    }

    @Override
    public void start() {
        scheduleCronJob(cronString, elementId);
    }

    @Override
    public void stop() {
        try {
            if (scheduler != null) {
                scheduler.deleteJob(new JobKey(jobName, jobGroup));
            }
        } catch (SchedulerException e) {
            log.error("Error while removing the cron job : " + e.getMessage(), e);
        }
    }

    @Override
    public Object[] currentState() {
        return new Object[]{currentEventChunk.getFirst(), expiredEventChunk.getFirst()};
    }

    @Override
    public void restoreState(Object[] state) {
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state[0]);
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state[1]);
    }

    private void scheduleCronJob(String cronString, String elementId) {
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            jobName = "EventRemoverJob_" + elementId;
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            scheduler.start();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("windowProcessor", this);

            JobDetail job = org.quartz.JobBuilder.newJob(CronWindowProcessor.class)
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

    public void dispatchEvents() {

        ComplexEventChunk<StreamEvent> streamEventChunk = new ComplexEventChunk<StreamEvent>(false);
        synchronized (this) {
            if (currentEventChunk.getFirst() != null) {
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    expiredEvent.setTimestamp(currentTime);
                }
                if (expiredEventChunk.getFirst() != null) {
                    streamEventChunk.add(expiredEventChunk.getFirst());
                }
                expiredEventChunk.clear();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = currentEventChunk.next();
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEventChunk.add(toExpireEvent);
                }

                streamEventChunk.add(currentEventChunk.getFirst());
                currentEventChunk.clear();

            }
        }
        if (streamEventChunk.getFirst() != null) {
            nextProcessor.process(streamEventChunk);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Running Event Remover Job");
        }

        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        CronWindowProcessor windowProcessor = (CronWindowProcessor) dataMap.get("windowProcessor");
        windowProcessor.dispatchEvents();

    }
}
