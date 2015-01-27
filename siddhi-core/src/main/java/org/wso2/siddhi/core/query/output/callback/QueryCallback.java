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

package org.wso2.siddhi.core.query.output.callback;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.execution.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class QueryCallback {

    private static final Logger log = Logger.getLogger(QueryCallback.class);

    private ExecutionPlanContext executionPlanContext;
    private Query query;
    private List<Event> currentEventBuffer = new ArrayList<Event>();
    private List<Event> expiredEventBuffer = new ArrayList<Event>();

    private Disruptor<EventHolder> disruptor;
    private RingBuffer<EventHolder> ringBuffer;
    private AsyncEventHandler asyncEventHandler;

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setContext(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    public void receiveStreamEvent(ComplexEventChunk complexEventChunk) {

        Event[] currentEvents = null;
        Event[] expiredEvents = null;
        long timeStamp = -1;

        while (complexEventChunk.hasNext()) {
            ComplexEvent streamEvent = complexEventChunk.next();
            if (streamEvent.getType() == StreamEvent.Type.EXPIRED) {
                bufferEvent(streamEvent, expiredEventBuffer);
            } else {
                bufferEvent(streamEvent, currentEventBuffer);
            }
            timeStamp = streamEvent.getTimestamp();
        }

        if (!currentEventBuffer.isEmpty()) {
            currentEvents = currentEventBuffer.toArray(new Event[currentEventBuffer.size()]);
            currentEventBuffer.clear();
        }

        if (!expiredEventBuffer.isEmpty()) {
            expiredEvents = expiredEventBuffer.toArray(new Event[expiredEventBuffer.size()]);
            expiredEventBuffer.clear();
        }

        if (disruptor == null) {
            send(timeStamp, currentEvents, expiredEvents);
        } else {
            sendAsync(timeStamp, currentEvents, expiredEvents);
        }
    }

    private void sendAsync(long timeStamp, Event[] currentEvents, Event[] expiredEvents) {
        long sequenceNo = ringBuffer.next();
        try {
            EventHolder holder = ringBuffer.get(sequenceNo);
            holder.timeStamp = timeStamp;
            holder.currentEvents = currentEvents;
            holder.expiredEvents = expiredEvents;
        } finally {
            ringBuffer.publish(sequenceNo);
        }
    }

//    private void send(long timeStamp, Event[] currentEvents, Event[] expiredEvents, boolean endOfBatch) {
//
//        if (endOfBatch) {
//            send(timeStamp, currentEvents, currentEvents);
//        } else {
//            StreamEvent processedEvent = currentStreamEvent;
//            bufferEvent(processedEvent, currentEventBuffer);
//
//            processedEvent = expiredStreamEvent;
//            bufferEvent(processedEvent, expiredEventBuffer);
//        }
//    }

    private void send(long timeStamp, Event[] currentEvents, Event[] expiredEvents) {
        try {
            receive(timeStamp, currentEvents, expiredEvents);
        } catch (RuntimeException e) {
            log.error("Error on sending events" + Arrays.deepToString(currentEvents) + ", " + Arrays.deepToString(expiredEvents), e);
        }
    }

    private void bufferEvent(ComplexEvent complexEvent, List<Event> eventBuffer) {
        eventBuffer.add(new Event(complexEvent.getOutputData().length).copyFrom(complexEvent));


//        StreamEvent processedEvent = streamEventList;
//        while (processedEvent != null) {
//            eventBuffer.add(new Event(processedEvent.getOutputDataAttributes().length).copyFrom(processedEvent));
//            processedEvent = processedEvent.getNext();
//        }
    }

    public synchronized void startProcessing() {

        Boolean asyncEnabled = null;
//        try {
//            Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_CONFIG,
//                    SiddhiConstants.ANNOTATION_ELEMENT_CALLBACK_ASYNC,
//                    query.getAnnotations());
//
//            if (element != null) {
//                asyncEnabled = SiddhiConstants.TRUE.equalsIgnoreCase(element.getValue());
//            }
//
//        } catch (DuplicateAnnotationException e) {
//            throw new QueryCreationException(e.getMessage() + " for the same Query " +
//                    query.toString());
//        }

        if (asyncEnabled != null && asyncEnabled || asyncEnabled == null) {

            disruptor = new Disruptor<EventHolder>(new EventHolderFactory(),
                    executionPlanContext.getSiddhiContext().getEventBufferSize(),
                    executionPlanContext.getExecutorService(), ProducerType.SINGLE, new SleepingWaitStrategy());

            asyncEventHandler = new AsyncEventHandler(this);
            disruptor.handleEventsWith(asyncEventHandler);
            ringBuffer = disruptor.start();
        }
    }

    public synchronized void stopProcessing() {
        if (disruptor != null) {
            asyncEventHandler.queryCallback = null;
            disruptor.shutdown();
        }
    }

    public abstract void receive(long timeStamp, Event[] inEvents, Event[] removeEvents);


    public class AsyncEventHandler implements EventHandler<EventHolder> {

        private QueryCallback queryCallback;

        public AsyncEventHandler(QueryCallback queryCallback) {
            this.queryCallback = queryCallback;
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
            if (queryCallback != null) {
                queryCallback.send(eventHolder.timeStamp, eventHolder.currentEvents, eventHolder.expiredEvents);
            }
        }
    }

    public class EventHolder {
        private long timeStamp;
        private Event[] currentEvents;
        private Event[] expiredEvents;
    }

    public class EventHolderFactory implements com.lmax.disruptor.EventFactory<EventHolder> {

        public EventHolder newInstance() {
            return new EventHolder();
        }
    }
}
