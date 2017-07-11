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

package org.wso2.siddhi.core.query.output.callback;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.execution.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Query Callback is used to get resulting output events from a Siddhi query. Users can create and register a callback
 * to a specific query and onEvent() of callback will be called upon query emitting results.
 */
public abstract class QueryCallback {

    private static final Logger log = Logger.getLogger(QueryCallback.class);

    private SiddhiAppContext siddhiAppContext;
    private Query query;

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setContext(SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
    }

    public void receiveStreamEvent(ComplexEventChunk complexEventChunk) {

        Event[] currentEvents = null;
        Event[] expiredEvents = null;
        long timestamp = -1;
        List<Event> currentEventBuffer = new ArrayList<Event>();
        List<Event> expiredEventBuffer = new ArrayList<Event>();

        complexEventChunk.reset();
        while (complexEventChunk.hasNext()) {
            ComplexEvent streamEvent = complexEventChunk.next();
            if (streamEvent.getType() == StreamEvent.Type.EXPIRED) {
                bufferEvent(streamEvent, expiredEventBuffer);
            } else if (streamEvent.getType() == StreamEvent.Type.CURRENT) {
                bufferEvent(streamEvent, currentEventBuffer);
            }
            timestamp = streamEvent.getTimestamp();
        }

        if (!currentEventBuffer.isEmpty()) {
            currentEvents = currentEventBuffer.toArray(new Event[currentEventBuffer.size()]);
            currentEventBuffer.clear();
        }

        if (!expiredEventBuffer.isEmpty()) {
            expiredEvents = expiredEventBuffer.toArray(new Event[expiredEventBuffer.size()]);
            expiredEventBuffer.clear();
        }

        send(timestamp, currentEvents, expiredEvents);
    }

    private void send(long timestamp, Event[] currentEvents, Event[] expiredEvents) {
        try {
            receive(timestamp, currentEvents, expiredEvents);
        } catch (RuntimeException e) {
            log.error("Error on sending events" + Arrays.deepToString(currentEvents) + ", " + Arrays.deepToString
                    (expiredEvents), e);
        }
    }

    private void bufferEvent(ComplexEvent complexEvent, List<Event> eventBuffer) {
        eventBuffer.add(new Event(complexEvent.getOutputData().length).copyFrom(complexEvent));
    }

    public synchronized void startProcessing() {

    }

    public synchronized void stopProcessing() {

    }

    public abstract void receive(long timestamp, Event[] inEvents, Event[] removeEvents);

}
