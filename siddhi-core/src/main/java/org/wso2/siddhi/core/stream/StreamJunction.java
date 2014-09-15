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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.EventFactory;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class StreamJunction {

    private List<Receiver> receivers = new CopyOnWriteArrayList<Receiver>();
    private List<Publisher> publishers = new CopyOnWriteArrayList<Publisher>();

    private StreamDefinition streamDefinition;
    private ExecutorService executorService;
    private int bufferSize;
    private String id;

    private Disruptor<Event> disruptor;
    private RingBuffer<Event> ringBuffer;
    static final Logger log = Logger.getLogger(StreamJunction.class);

    public StreamJunction(String id, StreamDefinition streamDefinition, ExecutorService executorService, int defaultBufferSize) {
        this.id = id;
        this.streamDefinition = streamDefinition;
        bufferSize = defaultBufferSize;
        this.executorService = executorService;
    }

    public void sendEvent(StreamEvent streamEvent) {
        /*if(log.isTraceEnabled()){
            log.trace("event is received by streamJunction "+ id +this);
        }*/
        StreamEvent streamEventList = streamEvent;
        if (disruptor != null) {

            while (streamEventList != null) {
                long sequenceNo = ringBuffer.next();
                try {
                    Event existingEvent = ringBuffer.get(sequenceNo);
                    existingEvent.copyFrom(streamEventList);
                } finally {
                    ringBuffer.publish(sequenceNo);
                }
                streamEventList = streamEventList.getNext();
            }

        } else {
            for (Receiver receiver : receivers) {
                receiver.receive(streamEvent);
            }
        }

    }

    public void sendEvent(Event event) {
        /*if(log.isTraceEnabled()){
            log.trace("event is received by streamJunction "+ id+ this);
        }*/
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

    public synchronized void startProcessing() {
        if (!receivers.isEmpty()) {

            Boolean asyncEnabled = null;
            try {
                Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_CONFIG,
                        SiddhiConstants.ANNOTATION_ELEMENT_ASYNC,
                        streamDefinition.getAnnotations());

                if (element != null) {
                    asyncEnabled = SiddhiConstants.TRUE.equalsIgnoreCase(element.getValue());
                }

            } catch (DuplicateAnnotationException e) {
                throw new QueryCreationException(e.getMessage() + " for the same Stream Definition " +
                        streamDefinition.toString());
            }

            if (asyncEnabled != null && asyncEnabled || asyncEnabled == null && publishers.size() > 1) {

                ProducerType producerType = ProducerType.SINGLE;
                if (publishers.size() > 1) {
                    producerType = ProducerType.MULTI;
                }

                disruptor = new Disruptor<Event>(new EventFactory(streamDefinition.getAttributeList().size()),
                        bufferSize, executorService, producerType, new SleepingWaitStrategy());

                for (Receiver receiver : receivers) {
                    disruptor.handleEventsWith(new StreamHandler(receiver));
                }

                ringBuffer = disruptor.start();

            }
        }
    }

    public synchronized void stopProcessing() {
        for (Publisher publisher : publishers) {
            publisher.setStreamJunction(null);
        }
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    public synchronized Publisher constructPublisher() {
        Publisher publisher = new Publisher();
        publisher.setStreamJunction(this);
        publishers.add(publisher);
        return publisher;
    }

    public synchronized void subscribe(Receiver receiver) {
        //to have reverse order at the sequence/pattern processors
        receivers.add(0, receiver);
    }

    public String getStreamId() {
        return streamDefinition.getId();
    }

    public interface Receiver {

        public String getStreamId();

        public void receive(StreamEvent streamEvent);

        public void receive(Event event);

        public void receive(Event event, boolean endOfBatch);
    }

    public class StreamHandler implements EventHandler<Event> {

        private Receiver receiver;

        public StreamHandler(Receiver receiver) {
            this.receiver = receiver;
        }

        public void onEvent(Event event, long sequence, boolean endOfBatch) {
            receiver.receive(event, endOfBatch);
        }

    }

    public class Publisher {

        private StreamJunction streamJunction;

        public void setStreamJunction(StreamJunction streamJunction) {
            this.streamJunction = streamJunction;
        }

        public void send(StreamEvent streamEvent) {
            if (streamJunction != null) {
                streamJunction.sendEvent(streamEvent);
            }
        }

        public void send(Event event) {
            if (streamJunction != null) {
                streamJunction.sendEvent(event);
            }
        }

        public String getStreamId() {
            return streamJunction.getStreamId();
        }
    }
}
