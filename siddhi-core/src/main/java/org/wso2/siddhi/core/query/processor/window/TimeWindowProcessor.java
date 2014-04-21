/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.query.processor.window;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.event.remove.RemoveListEvent;
import org.wso2.siddhi.core.event.remove.RemoveStream;
import org.wso2.siddhi.core.snapshot.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.EventConverter;
import org.wso2.siddhi.core.util.collection.queue.TimeStampSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.ISchedulerTimestampSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.SchedulerTimestampSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.SchedulerTimestampSiddhiQueueGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeWindowProcessor extends WindowProcessor implements RunnableWindowProcessor {

    static final Logger log = Logger.getLogger(TimeWindowProcessor.class);
    private ScheduledExecutorService eventRemoverScheduler;
    private long timeToKeep;
    private long constantSchedulingInterval = -1;
    private boolean isConstantSchedulingMode = false;
    private ThreadBarrier threadBarrier;
    private ISchedulerTimestampSiddhiQueue<StreamEvent> window;

    @Override
    public void processEvent(InEvent event) {
        acquireLock();
        try {
            window.put(new RemoveEvent(event, System.currentTimeMillis() + timeToKeep));
            nextProcessor.process(event);
        } finally {
            releaseLock();
        }
    }


    @Override
    public void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            if (!async && siddhiContext.isDistributedProcessingEnabled()) {
                long expireTime = System.currentTimeMillis() + timeToKeep;
                for (int i = 0, activeEvents = listEvent.getActiveEvents(); i < activeEvents; i++) {
                    window.put(new RemoveEvent(listEvent.getEvent(i), expireTime));
                }
            } else {
                window.put(new RemoveListEvent(EventConverter.toRemoveEventArray(listEvent.getEvents(), listEvent.getActiveEvents(), System.currentTimeMillis() + timeToKeep)));
            }
            nextProcessor.process(listEvent);
        } finally {
            releaseLock();
        }
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return window.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        if (siddhiContext.isDistributedProcessingEnabled()) {
            return ((SchedulerTimestampSiddhiQueueGrid<StreamEvent>) window).iterator(predicate);
        } else {
            return window.iterator();
        }
    }

    @Override
    public void run() {
        acquireLock();
        try {
            while (true) {
                threadBarrier.pass();
                StreamEvent streamEvent = window.peek();
                try {
                    if (streamEvent == null) {
                        break;
                    }
                    long timeDiff = ((RemoveStream) streamEvent).getExpiryTime() - System.currentTimeMillis();
                    try {
                            if (timeDiff > 0) {
                                if (!isConstantSchedulingMode){
                                    if (siddhiContext.isDistributedProcessingEnabled()) {
                                        //should not use sleep as it will not release the lock, hence it will fail in distributed case
                                        eventRemoverScheduler.schedule(this, timeDiff, TimeUnit.MILLISECONDS);
                                        break;
                                    } else {
                                        //this cannot be used for distributed case as it will course concurrency issues
                                        releaseLock();
                                        Thread.sleep(timeDiff);
                                        acquireLock();
                                    }
                                }else {
                                    break;
                                }
                            }

                        Collection<StreamEvent> resultList = window.poll(System.currentTimeMillis());
                        if (resultList != null){
                            for (StreamEvent event : resultList) {
                                if (streamEvent instanceof AtomicEvent) {
                                    nextProcessor.process((AtomicEvent) event);
                                } else {
                                    nextProcessor.process((ListEvent) event);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        log.warn("Time window sleep interrupted at elementId " + elementId);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        } finally {
            // If this is constant scheduling, reschedule after every execution since
            // arrival of events won't do any scheduling
            if (isConstantSchedulingMode){
                this.scheduleConstantTime();
            }
            releaseLock();
        }
    }

    @Override
    protected Object[] currentState() {
        return window.currentState();
    }

    @Override
    protected void restoreState(Object[] data) {
        window.restoreState(data);

        if (!isConstantSchedulingMode){
            window.reSchedule();
        }
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        if (parameters[0] instanceof IntConstant) {
            timeToKeep = ((IntConstant) parameters[0]).getValue();
        } else {
            timeToKeep = ((LongConstant) parameters[0]).getValue();
        }

        if (parameters.length  == 2){
            constantSchedulingInterval = ((IntConstant) parameters[1]).getValue();
            if (constantSchedulingInterval > 0){
                isConstantSchedulingMode = true;
            }
        }

        if (!isConstantSchedulingMode){
            if (this.siddhiContext.isDistributedProcessingEnabled()) {
                window = new SchedulerTimestampSiddhiQueueGrid<StreamEvent>(elementId, this, this.siddhiContext, this.async);
            } else {
                window = new SchedulerTimestampSiddhiQueue<StreamEvent>(this);
            }
        }else{
            if (this.siddhiContext.isDistributedProcessingEnabled()) {
                throw new UnsupportedOperationException("Constant time sliding not supported for distributed processing.");
                //TODO : Implement constant time sliding window grid for distributed case
            }else{
                window = new TimeStampSiddhiQueue<StreamEvent>(constantSchedulingInterval);
                this.schedule();
            }
        }

    }

    public void scheduleNow() {
        eventRemoverScheduler.execute(this);
    }

    public void schedule() {
        eventRemoverScheduler.schedule(this, timeToKeep, TimeUnit.MILLISECONDS);
    }

    public void scheduleConstantTime(){
        eventRemoverScheduler.schedule(this, constantSchedulingInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        eventRemoverScheduler = scheduledExecutorService;
    }

    public void setThreadBarrier(ThreadBarrier threadBarrier) {
        this.threadBarrier = threadBarrier;
    }

    @Override
    public void destroy(){

    }
}

