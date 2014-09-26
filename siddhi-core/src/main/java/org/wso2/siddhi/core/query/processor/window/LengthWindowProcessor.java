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

import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

public class LengthWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private StreamEvent removeEventHead = null;
    private StreamEvent removeEventTail = null;

    @Override
    public void init() {
        if (parameters != null) {
            length = ((IntConstant) parameters[0]).getValue();
        }
    }

    @Override
    public void process(StreamEvent event) {
        StreamEvent head = event;           //head of in events
        StreamEvent expiredEventTail;
        StreamEvent expiredEventHead;
        while (event != null) {
            processEvent(event);
            event = event.getNext();
        }
        //if window is expired
        if (count > length) {
            int diff = count - length;
            expiredEventTail = removeEventHead;
            for (int i = 1; i < diff; i++) {
                expiredEventTail = expiredEventTail.getNext();
            }
            expiredEventHead = removeEventHead;
            removeEventHead = expiredEventTail.getNext();
            expiredEventTail.setNext(null);
            head.addToLast(expiredEventHead);
            nextProcessor.process(head);                            //emit in events and remove events as expired events
            count = count - diff;
        } else {
            nextProcessor.process(head);                            //emit only in events as window is not expired
        }
    }

    /**
     * Create a copy of in event and store as remove event to emit at window expiration as expired events.
     *
     * @param event
     */
    private void processEvent(StreamEvent event) {
        StreamEvent removeEvent = new StreamEvent(0, event.getOnAfterWindowDataSize(), event.getOutputDataSize());
        System.arraycopy(event.getOnAfterWindowData(), 0, removeEvent.getBeforeWindowData(), 0, event.getOnAfterWindowDataSize());
        System.arraycopy(event.getOutputData(), 0, removeEvent.getOutputData(), 0, event.getOutputDataSize());
        removeEvent.setExpired(true);
        if (removeEventHead == null) {      //better if we can do it in init()
            removeEventHead = removeEvent;
            removeEventTail = removeEvent;
        } else {
            removeEventTail.setNext(removeEvent);
            removeEventTail = removeEvent;
        }
        count++;
    }

    @Override
    public Processor cloneProcessor() {
        LengthWindowProcessor lengthWindowProcessor = new LengthWindowProcessor();
        lengthWindowProcessor.setLength(this.length);
        return lengthWindowProcessor;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
