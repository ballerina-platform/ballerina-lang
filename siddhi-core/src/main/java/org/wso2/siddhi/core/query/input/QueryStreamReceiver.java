/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.input;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.EventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.converter.EventManager;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class QueryStreamReceiver implements StreamJunction.Receiver {

    private String streamId;
    private Processor processorChain;
    private Processor next;
    private EventChunk eventChunk = new EventChunk();


    public QueryStreamReceiver(StreamDefinition streamDefinition) {
        this.streamId = streamDefinition.getId();
    }

    private QueryStreamReceiver(String id) {
        streamId = id;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public QueryStreamReceiver clone(String key) {
        QueryStreamReceiver clonedQueryStreamReceiver = new QueryStreamReceiver(streamId + key);
        clonedQueryStreamReceiver.setEventManager(eventChunk.getEventManager());
        return clonedQueryStreamReceiver;
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        eventChunk.assignEvent(streamEvent);
        processAndClear();
    }

    @Override
    public void receive(Event event) {
        eventChunk.assignEvent(event);
        processAndClear();
    }

    @Override
    public void receive(Event[] events) {
        eventChunk.assignEvent(events);
        processAndClear();
    }


    @Override
    public void receive(Event event, boolean endOfBatch) {
        eventChunk.insert(event);
        if (endOfBatch) {
            processAndClear();
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        eventChunk.assignEvent(timeStamp,data);
        processAndClear();
    }

    private void processAndClear(){
        next.process(eventChunk);
        eventChunk.returnAllAndClear();
    }

    public Processor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(Processor processorChain) {
        this.processorChain = processorChain;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventChunk.setEventManager(eventManager);
    }

    public void setNext(Processor next) {
        this.next = next;
    }
}
