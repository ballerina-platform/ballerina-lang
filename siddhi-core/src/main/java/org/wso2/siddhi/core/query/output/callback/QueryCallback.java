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
package org.wso2.siddhi.core.query.output.callback;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.tracer.EventMonitorService;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.util.CallbackEventComposite;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class QueryCallback implements Runnable, SchedulerElement {

    private SchedulerSiddhiQueue<CallbackEventComposite> inputQueue;
    private ThreadPoolExecutor threadPoolExecutor;
    private SiddhiContext siddhiContext;
    private StreamDefinition streamDefinition;
    static final Logger log = Logger.getLogger(QueryCallback.class);
    private EventMonitorService eventMonitorService;


    public void setSiddhiContext(SiddhiContext context) {
        this.siddhiContext = context;
        this.threadPoolExecutor = context.getThreadPoolExecutor();
        this.eventMonitorService = context.getEventMonitorService();
        this.inputQueue = new SchedulerSiddhiQueue<CallbackEventComposite>(this);
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void receiveStreamEvent(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent) {

        if (siddhiContext.isAsyncProcessing()) {
            if (log.isDebugEnabled()) {
                log.debug("Adding to QueryCallback " + currentEvent + " " + expiredEvent);
            }
            inputQueue.put(new CallbackEventComposite(timeStamp, currentEvent, expiredEvent));
        } else {
            send(timeStamp, currentEvent, expiredEvent);
        }
    }


    @Override
    public void run() {
        try {
            int eventCounter = 0;
            while (true) {
                CallbackEventComposite eventComposite = inputQueue.poll();
                if (eventComposite == null) {
                    break;
                } else if (siddhiContext.getEventBatchSize() > 0 && eventCounter > siddhiContext.getEventBatchSize()) {
                    threadPoolExecutor.execute(this);
                    break;
                }
                send(eventComposite.getTimeStamp(), eventComposite.getCurrentEvent(), eventComposite.getExpiredEvent());
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    private void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent) {
        if (currentEvent != null) {
            if (eventMonitorService.isEnableTrace()) {
                eventMonitorService.trace(currentEvent, " current event on Query Callback");
            }
            receive(timeStamp, currentEvent.toArray(), null);
        } else if (expiredEvent != null) {
            if (eventMonitorService.isEnableTrace()) {
                eventMonitorService.trace(expiredEvent, " expired event on Query Callback");
            }
            receive(timeStamp, null, expiredEvent.toArray());
        } else {
            receive(timeStamp, null, null);
        }
    }

    public abstract void receive(long timeStamp, Event[] inEvents,
                                 Event[] removeEvents);

    public void setStreamDefinition(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    @Override
    public void schedule() {
        threadPoolExecutor.execute(this);
    }

    @Override
    public void scheduleNow() {
        threadPoolExecutor.execute(this);
    }
}
