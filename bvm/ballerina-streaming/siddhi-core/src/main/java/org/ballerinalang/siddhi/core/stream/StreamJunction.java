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

package org.ballerinalang.siddhi.core.stream;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.event.SiddhiEventFactory;
import org.ballerinalang.siddhi.core.stream.input.InputProcessor;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.statistics.EventBufferHolder;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.core.util.timestamp.EventTimeBasedMillisTimestampGenerator;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateAnnotationException;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Stream Junction is the place where streams are collected and distributed. There will be an Stream Junction per
 * evey event stream. {@link StreamJunction.Publisher} can be used to publish events to the junction and
 * {@link StreamJunction.Receiver} can be used to receive events from Stream Junction. Stream Junction will hold the
 * events till they are consumed by registered Receivers.
 */
public class StreamJunction implements EventBufferHolder {
    private static final Logger log = LoggerFactory.getLogger(StreamJunction.class);
    private final SiddhiAppContext siddhiAppContext;
    private final StreamDefinition streamDefinition;
    private int bufferSize;
    private List<Receiver> receivers = new CopyOnWriteArrayList<Receiver>();
    private List<Publisher> publishers = Collections.synchronizedList(new LinkedList<>());
    private ExecutorService executorService;
    private boolean async = false;
    private Disruptor<Event> disruptor;
    private RingBuffer<Event> ringBuffer;
    private ThroughputTracker throughputTracker = null;
    private boolean isTraceEnabled;

    public StreamJunction(StreamDefinition streamDefinition, ExecutorService executorService, int bufferSize,
                          SiddhiAppContext siddhiAppContext) {
        this.streamDefinition = streamDefinition;
        this.bufferSize = bufferSize;
        this.executorService = executorService;
        this.siddhiAppContext = siddhiAppContext;
        if (siddhiAppContext.getStatisticsManager() != null) {
            this.throughputTracker = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    streamDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_STREAMS, null);
        }
        try {
            Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_ASYNC,
                    streamDefinition.getAnnotations());
            if (annotation != null) {
                async = true;
                String bufferSizeString = annotation.getElement(SiddhiConstants.ANNOTATION_ELEMENT_BUFFER_SIZE);
                if (bufferSizeString != null) {
                    this.bufferSize = Integer.parseInt(bufferSizeString);
                }
            }

        } catch (DuplicateAnnotationException e) {
            throw new DuplicateAnnotationException(e.getMessageWithOutContext() + " for the same Stream " +
                    streamDefinition.getId(), e, e.getQueryContextStartIndex(), e.getQueryContextEndIndex(),
                    siddhiAppContext.getName(), siddhiAppContext.getSiddhiAppString());
        }
        isTraceEnabled = log.isTraceEnabled();
    }

    public void sendEvent(ComplexEvent complexEvent) {
        if (isTraceEnabled) {
            log.trace("Event is received by streamJunction " + this);
        }
        ComplexEvent complexEventList = complexEvent;
        if (disruptor != null) {
            while (complexEventList != null) {
                if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTracker.eventIn();
                }
                long sequenceNo = ringBuffer.next();
                try {
                    Event existingEvent = ringBuffer.get(sequenceNo);
                    existingEvent.copyFrom(complexEventList);
                } finally {
                    ringBuffer.publish(sequenceNo);
                }
                complexEventList = complexEventList.getNext();
            }
        } else {
            if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
                int messageCount = 0;
                while (complexEventList != null) {
                    messageCount++;
                    complexEventList = complexEventList.getNext();
                }
                throughputTracker.eventsIn(messageCount);
            }
            for (Receiver receiver : receivers) {
                receiver.receive(complexEvent);
            }
        }
    }

    public void sendEvent(Event event) {
        if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
            throughputTracker.eventIn();
        }
        if (isTraceEnabled) {
            log.trace(event + " event is received by streamJunction " + this);
        }
        if (disruptor != null) {
            long sequenceNo = ringBuffer.next();
            try {
                Event existingEvent = ringBuffer.get(sequenceNo);
                existingEvent.copyFrom(event);
            } finally {
                ringBuffer.publish(sequenceNo);
            }
        } else {
            for (Receiver receiver : receivers) {
                receiver.receive(event);
            }
        }
    }

    private void sendEvent(Event[] events) {
        if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
            throughputTracker.eventsIn(events.length);
        }
        if (isTraceEnabled) {
            log.trace("Event is received by streamJunction " + this);
        }
        if (disruptor != null) {
            for (Event event : events) {   // Todo : optimize for arrays
                long sequenceNo = ringBuffer.next();
                try {
                    Event existingEvent = ringBuffer.get(sequenceNo);
                    existingEvent.copyFrom(event);
                } finally {
                    ringBuffer.publish(sequenceNo);
                }
            }
        } else {
            for (Receiver receiver : receivers) {
                receiver.receive(events);
            }
        }
    }

    private void sendEvent(List<Event> events) {
        if (isTraceEnabled) {
            log.trace("Event is received by streamJunction " + this);
        }
        if (disruptor != null) {
            for (Event event : events) {   // Todo : optimize for arrays
                long sequenceNo = ringBuffer.next();
                try {
                    Event existingEvent = ringBuffer.get(sequenceNo);
                    existingEvent.copyFrom(event);
                } finally {
                    ringBuffer.publish(sequenceNo);
                }
            }
        } else {
            for (Receiver receiver : receivers) {
                receiver.receive(events.toArray(new Event[events.size()]));
            }
        }
    }

    private void sendData(long timeStamp, Object[] data) {
        // Set timestamp to system if Siddhi is in playback mode
        if (siddhiAppContext.isPlayback()) {
            ((EventTimeBasedMillisTimestampGenerator) this.siddhiAppContext.getTimestampGenerator())
                    .setCurrentTimestamp(timeStamp);
        }
        if (throughputTracker != null && siddhiAppContext.isStatsEnabled()) {
            throughputTracker.eventIn();
        }
        if (disruptor != null) {
            long sequenceNo = ringBuffer.next();
            try {
                Event existingEvent = ringBuffer.get(sequenceNo);
                existingEvent.setTimestamp(timeStamp);
                existingEvent.setIsExpired(false);
                System.arraycopy(data, 0, existingEvent.getData(), 0, data.length);
            } finally {
                ringBuffer.publish(sequenceNo);
            }
        } else {
            for (Receiver receiver : receivers) {
                receiver.receive(timeStamp, data);
            }
        }
    }

    /**
     * Create and start disruptor based on annotations given in the streamDefinition.
     */
    public synchronized void startProcessing() {
        if (!receivers.isEmpty() && async) {
            for (Constructor constructor : Disruptor.class.getConstructors()) {
                if (constructor.getParameterTypes().length == 5) {      // If new disruptor classes available
                    ProducerType producerType = ProducerType.MULTI;
                    disruptor = new Disruptor<Event>(new SiddhiEventFactory(streamDefinition.getAttributeList().size()),
                            bufferSize, executorService, producerType,
                            new BlockingWaitStrategy());
                    disruptor.handleExceptionsWith(siddhiAppContext.getDisruptorExceptionHandler());
                    break;
                }
            }
            if (disruptor == null) {
                disruptor = new Disruptor<Event>(new SiddhiEventFactory(streamDefinition.getAttributeList().size()),
                        bufferSize, executorService);
                disruptor.handleExceptionsWith(siddhiAppContext.getDisruptorExceptionHandler());
            }
            for (Receiver receiver : receivers) {
                disruptor.handleEventsWith(new StreamHandler(receiver));
            }
            ringBuffer = disruptor.start();
        } else {
            for (Receiver receiver : receivers) {
                if (receiver instanceof StreamCallback) {
                    ((StreamCallback) receiver).startProcessing();
                }
            }
        }
    }

    public synchronized void stopProcessing() {
        if (disruptor != null) {
            disruptor.shutdown();
        } else {
            for (Receiver receiver : receivers) {
                if (receiver instanceof StreamCallback) {
                    ((StreamCallback) receiver).stopProcessing();
                }
            }
        }
    }

    public synchronized Publisher constructPublisher() {
        Publisher publisher = new Publisher();
        publisher.setStreamJunction(this);
        publishers.add(publisher);
        return publisher;
    }

    public synchronized void subscribe(Receiver receiver) {
        // To have reverse order at the sequence/pattern processors.
        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    public String getStreamId() {
        return streamDefinition.getId();
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    @Override
    public long getBufferedEvents() {
        if (disruptor != null) {
            return disruptor.getBufferSize() - disruptor.getRingBuffer().remainingCapacity();
        }
        return 0L;
    }

    @Override
    public boolean containsBufferedEvents() {
        return (!receivers.isEmpty() && async);
    }

    /**
     * Interface to be implemented by all receivers who need to subscribe to Stream Junction and receive events.
     */
    public interface Receiver {

        String getStreamId();

        void receive(ComplexEvent complexEvent);

        void receive(Event event);

        void receive(Event event, boolean endOfBatch);

        void receive(long timeStamp, Object[] data);

        void receive(Event[] events);
    }

    /**
     * Interface to be implemented to receive events via handlers.
     */
    public class StreamHandler implements EventHandler<Event> {

        private Receiver receiver;

        public StreamHandler(Receiver receiver) {
            this.receiver = receiver;
        }

        public void onEvent(Event event, long sequence, boolean endOfBatch) {
            receiver.receive(event, endOfBatch);
        }
    }

    /**
     * Interface to be implemented to send events into the Stream Junction.
     */
    public class Publisher implements InputProcessor {

        private StreamJunction streamJunction;

        public void setStreamJunction(StreamJunction streamJunction) {
            this.streamJunction = streamJunction;
        }

        public void send(ComplexEvent complexEvent) {
            streamJunction.sendEvent(complexEvent);
        }

        @Override
        public void send(Event event, int streamIndex) {
            streamJunction.sendEvent(event);
        }

        @Override
        public void send(Event[] events, int streamIndex) {
            streamJunction.sendEvent(events);
        }

        @Override
        public void send(List<Event> events, int streamIndex) {
            streamJunction.sendEvent(events);
        }

        @Override
        public void send(long timeStamp, Object[] data, int streamIndex) {
            streamJunction.sendData(timeStamp, data);
        }

        public String getStreamId() {
            return streamJunction.getStreamId();
        }
    }
}
