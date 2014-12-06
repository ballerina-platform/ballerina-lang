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
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.event.stream.*;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

public class LengthBatchWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private StreamEventChunk currentEventChunk = new StreamEventChunk();
    private StreamEventChunk expiredEventChunk = new StreamEventChunk();
    private MetaStreamEvent metaStreamEvent;
    private StreamEventCloner streamEventCloner;


    /**
     * Initialization method for window processors. Should set parameters accordingly and configure processor
     * to an executable status.
     *
     * @param metaStreamEvent
     * @param streamEventCloner
     */
    @Override
    public void init(MetaStreamEvent metaStreamEvent, StreamEventCloner streamEventCloner) {
        this.metaStreamEvent = metaStreamEvent;
        this.streamEventCloner = streamEventCloner;
        if (parameters != null) {
            this.setLength(((IntConstant) parameters[0]).getValue());
        }
    }

    @Override
    public void process(StreamEventChunk streamEventChunk, Processor nextProcessor) {
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
//            clonedStreamEvent.setExpired(true);
            currentEventChunk.add(clonedStreamEvent);
            count++;
            if (count == length) {
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    expiredEvent.setTimestamp(System.currentTimeMillis());  //todo fix
                }
                if(expiredEventChunk.getFirst()!=null) {
                    streamEventChunk.insertBeforeCurrent(expiredEventChunk.getFirst());
                }
                expiredEventChunk.clear();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = currentEventChunk.next();
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEventChunk.add(toExpireEvent);
                }
                streamEventChunk.insertBeforeCurrent(currentEventChunk.getFirst());
                currentEventChunk.clear();
                count=0;

            }
            streamEventChunk.remove();

        }
        if(streamEventChunk.getFirst()!=null) {
            nextProcessor.process(streamEventChunk);
        }

//        StreamEvent event = streamEventChunk.getFirst();
//        StreamEvent currentEvent;
//        StreamEvent head = event;
//        while (event != null) {
//            processEvent(event);
//            currentEvent = event;
//            event = event.getNext();
//            if (count == length) {
//                currentEvent.setNext(removeEventHead);
//                removeEventHead = null;
//                removeEventTail.setNext(event);
//                count = 0;
//            }
//        }
//        ConvertingStreamEventChunk headStreamEventChunk = new ConvertingStreamEventChunk(null,null);
//        headEventChunk.setEventConverter(eventChunk.getEventConverter());
//        headEventChunk.assignConvertedEvents(head);
//        nextProcessor.process(headStreamEventChunk);
    }

//    /**
//     * Create a copy of in event and store as remove event to emit at window expiration as expired events.
//     *
//     * @param event
//     */
//    private void processEvent(StreamEvent event) {      //can do in event or borrow from pool??
//        StreamEvent removeEvent = removeEventFactory.newInstance();
//        if (removeEvent.getOnAfterWindowData() != null) {
//            System.arraycopy(event.getOnAfterWindowData(), 0, removeEvent.getOnAfterWindowData(), 0,
//                    event.getOnAfterWindowData().length);
//        }
//        System.arraycopy(event.getOutputData(), 0, removeEvent.getOutputData(), 0, event.getOutputData().length);
//        removeEvent.setExpired(true);
//        if (removeEventHead == null) {      //better if we can do it in init()
//            removeEventHead = removeEvent;
//            removeEventTail = removeEvent;
//        } else {
//            removeEventTail.setNext(removeEvent);
//            removeEventTail = removeEvent;
//        }
//        count++;
//    }

    @Override
    public Processor cloneProcessor() {
        LengthBatchWindowProcessor lengthBatchWindowProcessor = new LengthBatchWindowProcessor();
        lengthBatchWindowProcessor.setLength(this.length);
        return lengthBatchWindowProcessor;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
