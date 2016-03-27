/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.input;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 11/28/14.
 */
public class SingleStreamEntryValve implements InputProcessor {

    private final SingleEntryValveHandler singleEntryValveHandler;
    private Disruptor<IndexedEventFactory.IndexedEvent> singleEntryDisruptor;
    private RingBuffer<IndexedEventFactory.IndexedEvent> ringBuffer;
    private ExecutionPlanContext executionPlanContext;
    private InputProcessor inputProcessor;
    private AtomicLong eventSizeInDisruptor = new AtomicLong(0l);
    private long count;

    static final Logger log = Logger.getLogger(SingleStreamEntryValve.class);

    public SingleStreamEntryValve(ExecutionPlanContext executionPlanContext, InputProcessor inputProcessor) {
        this.executionPlanContext = executionPlanContext;
        this.inputProcessor = inputProcessor;
        this.singleEntryValveHandler = new SingleEntryValveHandler();
        for (Constructor constructor : Disruptor.class.getConstructors()) {
            if (constructor.getParameterTypes().length == 5) {      //if new disruptor classes available
                singleEntryDisruptor = new Disruptor<IndexedEventFactory.IndexedEvent>(new IndexedEventFactory(),
                        executionPlanContext.getSiddhiContext().getEventBufferSize(),
                        executionPlanContext.getExecutorService(),
                        ProducerType.MULTI,
                        PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS));
                break;
            }
        }
        if (singleEntryDisruptor == null) {
            singleEntryDisruptor = new Disruptor<IndexedEventFactory.IndexedEvent>(new IndexedEventFactory(),
                    executionPlanContext.getSiddhiContext().getEventBufferSize(),
                    executionPlanContext.getExecutorService());
        }
    }

    @Override
    public void send(Event event, int streamIndex) {
        inputProcessor.send(event, streamIndex);
//        try {
//            long sequenceNo = ringBuffer.next();
//            try {
//                IndexedEventFactory.IndexedEvent existingEvent = ringBuffer.get(sequenceNo);
//                existingEvent.setEvent(event);
//                existingEvent.setStreamIndex(streamIndex);
//            } finally {
//                eventSizeInDisruptor.incrementAndGet();
//                ringBuffer.publish(sequenceNo);    //Todo fix this for array of events
//            }
//        } catch (NullPointerException e) {
//            throw new ExecutionPlanRuntimeException("Execution Plan:" + executionPlanContext.getName() + " not " +
//                    "initialised yet! Run executionPlanRuntime.start();", e);
//        }

    }

    @Override
    public void send(Event[] events, int streamIndex) {
        for (Event event : events) {
            send(event, streamIndex);
        }
    }

    @Override
    public void send(List<Event> events, int streamIndex) {
        for (Event event : events) {
            send(event, streamIndex);
        }
    }

    @Override
    public void send(long timeStamp, Object[] data, int streamIndex) {
        send(new Event(timeStamp, data), streamIndex);
    }

    public synchronized void startProcessing() {
        singleEntryDisruptor.handleExceptionsWith(executionPlanContext.getSiddhiContext().getExceptionHandler());
        singleEntryDisruptor.handleEventsWith(singleEntryValveHandler);
//        ringBuffer = singleEntryDisruptor.start();
    }

    public synchronized void stopProcessing() {
//        singleEntryDisruptor.shutdown();
    }

    public class SingleEntryValveHandler implements EventHandler<IndexedEventFactory.IndexedEvent> {

        private List<Event> eventBuffer = new ArrayList<Event>();
        private int currentIndex = -1;

        /**
         * Called when a publisher has published an event to the {@link com.lmax.disruptor.RingBuffer}
         *
         * @param indexedEvent published to the {@link com.lmax.disruptor.RingBuffer}
         * @param sequence     of the event being processed
         * @param endOfBatch   flag to indicate if this is the last event in a batch from the {@link com.lmax.disruptor.RingBuffer}
         * @throws Exception if the EventHandler would like the exception handled further up the chain.
         */
        @Override
        public void onEvent(IndexedEventFactory.IndexedEvent indexedEvent, long sequence, boolean endOfBatch) throws Exception {
            eventSizeInDisruptor.decrementAndGet();
            int streamIndex = indexedEvent.getStreamIndex();
            if (currentIndex != streamIndex) {
                sendEvents();
                currentIndex = streamIndex;
            }
            eventBuffer.add(indexedEvent.getEvent());

            if (endOfBatch) {
                sendEvents();
                currentIndex = -1;
            }
        }

        private void sendEvents() {
            int size = eventBuffer.size();
            try {
                switch (size) {
                    case 0: {
                        return;
                    }
                    case 1: {
                        inputProcessor.send(eventBuffer.get(0), currentIndex);
                        eventBuffer.clear();
                        return;
                    }
                    default: {
                        inputProcessor.send(eventBuffer.toArray(new Event[size]), currentIndex);
                    }
                }
            } finally {
                if(size>0) {
                    eventBuffer.clear();
                }
            }
        }
    }

    public static class IndexedEventFactory implements com.lmax.disruptor.EventFactory<IndexedEventFactory.IndexedEvent> {


        public IndexedEvent newInstance() {
            return new IndexedEvent();
        }

        public class IndexedEvent {

            private int streamIndex;
            private Event event;

            public Event getEvent() {
                return event;
            }

            public void setEvent(Event event) {
                this.event = event;
            }

            public int getStreamIndex() {
                return streamIndex;
            }

            public void setStreamIndex(int streamIndex) {
                this.streamIndex = streamIndex;
            }

            @Override
            public String toString() {
                return "IndexedEvent{" +
                        "streamIndex=" + streamIndex +
                        ", event=" + event +
                        '}';
            }
        }
    }

}
