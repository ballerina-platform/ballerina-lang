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
package org.wso2.siddhi.core.query.processor.stream.window;

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
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link WindowProcessor} which represent a Window operating based on a cron expression.
 */
@Extension(
        name = "cron",
        namespace = "",
        description = "This window returns events processed periodically as the output in time-repeating patterns, " +
                "triggered based on time passing.",
        parameters = {
                @Parameter(name = "cron.expression",
                        description = "The cron expression that represents a time schedule.",
                        type = {DataType.STRING})
        },
        examples = @Example(
                syntax = "define window cseEventWindow (symbol string, price float, volume int)" +
                        "cron('*/5 * * * * ?');\n" +
                        "@info(name = 'query0')\n" +
                        "from cseEventStream\n" +
                        "insert into cseEventWindow;\n" +
                        "@info(name = 'query1')\n" +
                        "from cseEventWindow \n" +
                        "select symbol,price,volume\n" +
                        "insert into outputStream ;",
                description = "This will processed events as the output every 5 seconds.")
)
public class CronWindowProcessor extends WindowProcessor implements Job {
    private static final Logger log = Logger.getLogger(CronWindowProcessor.class);
    private final String jobGroup = "CronWindowGroup";
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private SiddhiAppContext siddhiAppContext;
    private Scheduler scheduler;
    private String jobName;
    private String cronString;


    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader, boolean
            outputExpectsExpiredEvents, SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        if (attributeExpressionExecutors != null) {
            cronString = (String) (((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue());
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {
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
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("CurrentEventChunk", currentEventChunk.getFirst());
        state.put("ExpiredEventChunk", expiredEventChunk.getFirst());
        return state;
    }


    @Override
    public void restoreState(Map<String, Object> state) {
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state.get("CurrentEventChunk"));
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state.get("ExpiredEventChunk"));
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
                long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
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
