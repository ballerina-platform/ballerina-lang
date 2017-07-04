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
package org.wso2.siddhi.core.stream.output;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StreamCallback is used to receive events from {@link StreamJunction}. This class should be extended if one intends
 * to get events from a Siddhi Stream.
 */
public abstract class StreamCallback implements StreamJunction.Receiver {

    private static final Logger log = Logger.getLogger(StreamCallback.class);

    private String streamId;
    private AbstractDefinition streamDefinition;
    private SiddhiAppContext siddhiAppContext;

    private List<Event> batchingEventBuffer = new ArrayList<Event>();

    @Override
    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public AbstractDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(AbstractDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public void setContext(SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
    }

    @Override
    public void receive(ComplexEvent complexEvent) {
        List<Event> eventBuffer = new ArrayList<Event>();
        while (complexEvent != null) {
            eventBuffer.add(new Event(complexEvent.getOutputData().length).copyFrom(complexEvent));
            complexEvent = complexEvent.getNext();
        }
        if (eventBuffer.size() == 1) {
            receive(eventBuffer.get(0));
        } else {
            receiveEvents(eventBuffer.toArray(new Event[eventBuffer.size()]));
        }
    }

    @Override
    public void receive(Event event) {
        receiveEvents(new Event[]{event});
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        Event[] bufferedEvents = null;
        synchronized (this) {
            batchingEventBuffer.add(event);
            if (endOfBatch) {
                bufferedEvents = batchingEventBuffer.toArray(new Event[batchingEventBuffer.size()]);
                batchingEventBuffer.clear();
            }
        }
        if (bufferedEvents != null) {
            receiveEvents(bufferedEvents);
        }
    }

    public void receive(long timestamp, Object[] data) {
        receiveEvents(new Event[]{new Event(timestamp, data)});
    }

    public void receiveEvents(Event[] events) {
        try {
            receive(events);
        } catch (RuntimeException e) {
            log.error("Error on sending events" + Arrays.deepToString(events), e);
        }
    }

    public abstract void receive(Event[] events);

    public synchronized void startProcessing() {

    }

    public synchronized void stopProcessing() {

    }

}
