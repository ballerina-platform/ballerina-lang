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
package org.wso2.siddhi.core.stream.output;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.tracer.EventMonitorService;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.stream.StreamReceiver;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class StreamCallback implements Runnable, StreamReceiver, SchedulerElement {

    private SchedulerSiddhiQueue<StreamEvent> inputQueue;
    private ThreadPoolExecutor threadPoolExecutor;
    private String streamId;
    private SiddhiContext siddhiContext;
    static final Logger log = Logger.getLogger(StreamCallback.class);
    private EventMonitorService eventMonitorService;


    public void setSiddhiContext(SiddhiContext context) {
        this.siddhiContext = context;
        this.threadPoolExecutor = context.getThreadPoolExecutor();
        this.eventMonitorService = context.getEventMonitorService();
        this.inputQueue = new SchedulerSiddhiQueue<StreamEvent>(this);

    }

    public void receive(StreamEvent streamEvent) {
        if (siddhiContext.isAsyncProcessing()) {
            if (log.isDebugEnabled()) {
                log.debug("Adding to streamId callback " + streamEvent);
            }
            inputQueue.put(streamEvent);
        } else {
            send(streamEvent);
        }
    }

    @Override
    public void run() {
        try {
            int eventCounter = 0;
            while (true) {
                StreamEvent event = inputQueue.poll();
                if (event == null) {
                    break;
                } else if (siddhiContext.getEventBatchSize() > 0 && eventCounter > siddhiContext.getEventBatchSize()) {
                    threadPoolExecutor.execute(this);
                    break;
                }
                send(event);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    private void send(StreamEvent event) {
        if (eventMonitorService.isEnableTrace()) {
            eventMonitorService.trace(event, " on Stream Callback");
        }
        receive(event.toArray());
    }

    public abstract void receive(Event[] events);

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
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
