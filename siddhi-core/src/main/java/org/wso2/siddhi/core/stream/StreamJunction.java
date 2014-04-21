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
package org.wso2.siddhi.core.stream;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.tracer.EventMonitorService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamJunction {
    private List<StreamReceiver> streamReceivers = new CopyOnWriteArrayList<StreamReceiver>();
    private String streamId;
    private EventMonitorService eventMonitorService;

    public StreamJunction(String streamId, EventMonitorService eventMonitorService) {
        this.streamId = streamId;
        this.eventMonitorService = eventMonitorService;
    }

    public void send(StreamEvent allEvents) {
        if (eventMonitorService.isEnableTrace()) {
            eventMonitorService.trace(allEvents, " on Event Stream");
        }
        if (eventMonitorService.isEnableStats()) {
            eventMonitorService.calculateStats(allEvents);
        }
        for (StreamReceiver handlerProcessor : streamReceivers) {
            handlerProcessor.receive(allEvents);
        }
    }

    public synchronized void addEventFlow(StreamReceiver streamReceiver) {
        //in reverse order to execute the later states first to overcome to dependencies of count states
        streamReceivers.add(0, streamReceiver);
    }

    public synchronized void removeEventFlow(HandlerProcessor queryStreamProcessor) {
        streamReceivers.remove(queryStreamProcessor);
    }

    public String getStreamId() {
        return streamId;
    }
}
