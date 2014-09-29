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

package org.wso2.siddhi.core.stream;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.constructor.EventConstructor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class QueryStreamReceiver implements StreamJunction.Receiver {

    private String streamId;
    private EventConstructor eventConstructor;
    private StreamEvent streamEventBuffer;
    private StreamEvent lastStreamEventInBuffer;
    private Processor processorChain;
    private Processor next;


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
        clonedQueryStreamReceiver.setEventConstructor(eventConstructor);
        return clonedQueryStreamReceiver;
    }

    @Override
    public void receive(StreamEvent streamEvent) {
        StreamEvent convertedStreamEvent = eventConstructor.constructStreamEvent(streamEvent);
        next.process(convertedStreamEvent);
        eventConstructor.returnEvent(convertedStreamEvent);
    }

    @Override
    public void receive(Event event) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(event);
        next.process(streamEvent);
        eventConstructor.returnEvent(streamEvent);
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(event);
        process(endOfBatch, streamEvent);
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent streamEvent = eventConstructor.constructStreamEvent(timeStamp, data);
        next.process(streamEvent);
        eventConstructor.returnEvent(streamEvent);
    }

    private void process(boolean endOfBatch, StreamEvent streamEvent) {
        if (streamEventBuffer == null) {
            if (endOfBatch) {
                next.process(streamEvent);
                eventConstructor.returnEvent(streamEvent);
            } else {
                streamEventBuffer = streamEvent;
                lastStreamEventInBuffer = streamEvent;
            }
        } else {
               lastStreamEventInBuffer.setNext(streamEvent);
               lastStreamEventInBuffer = streamEvent;
            if (endOfBatch) {
                next.process(streamEventBuffer);
                eventConstructor.returnEvent(streamEventBuffer);
                streamEventBuffer = null;
                lastStreamEventInBuffer = null;
            }
        }
    }

    public Processor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(Processor processorChain) {
        this.processorChain = processorChain;
    }

    public void setEventConstructor(EventConstructor eventConstructor) {
        this.eventConstructor = eventConstructor;
    }

    public void setNext(Processor next) {
        this.next = next;
    }
}
