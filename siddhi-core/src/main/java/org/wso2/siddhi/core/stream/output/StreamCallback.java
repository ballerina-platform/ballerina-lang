/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.stream.output;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class StreamCallback implements StreamJunction.Receiver {

    private static final Logger log = Logger.getLogger(StreamCallback.class);

    private String streamId;
    private AbstractDefinition streamDefinition;
    private List<Event> eventBuffer = new ArrayList<Event>();
    private ExecutionPlanContext executionPlanContext;
    private AsyncEventHandler asyncEventHandler;

    private Disruptor<EventHolder> disruptor;
    private RingBuffer<EventHolder> ringBuffer;


    @Override
    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public void setStreamDefinition(AbstractDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public void setContext(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    @Override
    public void receive(ComplexEvent complexEvent) {

        while (complexEvent != null) {
            eventBuffer.add(new Event(complexEvent.getOutputData().length).copyFrom(complexEvent));
            complexEvent = complexEvent.getNext();
        }
        if (disruptor == null) {
            receiveSync(eventBuffer.toArray(new Event[eventBuffer.size()]));
        } else {
            receiveAsync(eventBuffer.toArray(new Event[eventBuffer.size()]));
        }
        eventBuffer.clear();
    }

    @Override
    public void receive(Event event) {
        if (disruptor == null) {
            receiveSync(new Event[]{event});
        } else {
            receiveAsync(new Event[]{event});
        }
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        eventBuffer.add(event);
        if (endOfBatch) {
            receiveSync(eventBuffer.toArray(new Event[eventBuffer.size()]));
            eventBuffer.clear();
        }
    }

    public void receive(long timeStamp, Object[] data) {
        if (disruptor == null) {
            receiveSync(new Event[]{new Event(timeStamp, data)});
        } else {
            receiveAsync(new Event[]{new Event(timeStamp, data)});
        }
    }

    public void receiveSync(Event[] events) {
        try {
            receive(events);
        } catch (RuntimeException e) {
            log.error("Error on sending events" + Arrays.deepToString(events), e);
        }
    }

    public abstract void receive(Event[] events);

    private void receiveAsync(Event[] events) {
        long sequenceNo = ringBuffer.next();
        try {
            EventHolder eventHolder = ringBuffer.get(sequenceNo);
            eventHolder.events = events;
        } finally {
            ringBuffer.publish(sequenceNo);
        }
    }

    public synchronized void startProcessing() {
        Boolean asyncEnabled = null;
        if (asyncEnabled != null && asyncEnabled || asyncEnabled == null) {

            disruptor = new Disruptor<EventHolder>(new com.lmax.disruptor.EventFactory<EventHolder>() {
                @Override
                public EventHolder newInstance() {
                    return new EventHolder();
                }
            },
                    executionPlanContext.getSiddhiContext().getEventBufferSize(),
                    executionPlanContext.getExecutorService(), ProducerType.SINGLE, new SleepingWaitStrategy());

            asyncEventHandler = new AsyncEventHandler(this);
            disruptor.handleEventsWith(asyncEventHandler);
            ringBuffer = disruptor.start();
        }
    }

    public synchronized void stopProcessing() {
        if (disruptor != null) {
            asyncEventHandler.streamCallback = null;
            disruptor.shutdown();
        }
    }

    public class AsyncEventHandler implements EventHandler<EventHolder> {

        private StreamCallback streamCallback;

        public AsyncEventHandler(StreamCallback streamCallback) {
            this.streamCallback = streamCallback;
        }

        /**
         * Called when a publisher has published an event to the {@link com.lmax.disruptor.RingBuffer}
         *
         * @param eventHolder published to the {@link com.lmax.disruptor.RingBuffer}
         * @param sequence    of the event being processed
         * @param endOfBatch  flag to indicate if this is the last event in a batch from the {@link com.lmax.disruptor.RingBuffer}
         * @throws Exception if the EventHandler would like the exception handled further up the chain.
         */
        @Override
        public void onEvent(EventHolder eventHolder, long sequence, boolean endOfBatch) throws Exception {
            if (streamCallback != null) {
                streamCallback.receive(eventHolder.events);
            }
        }
    }


    private class EventHolder {
        public Event[] events;
    }
}
