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

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventFactory;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

public class LengthBatchWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private StreamEvent removeEventHead = null;
    private StreamEvent removeEventTail = null;
    StreamEventFactory removeEventFactory;
    private MetaStreamEvent metaStreamEvent;

    @Override
    public void init(MetaStreamEvent metaStreamEvent) {
        this.metaStreamEvent = metaStreamEvent;
        if (parameters != null) {
            this.setLength(((IntConstant) parameters[0]).getValue());
        }
        removeEventFactory = new StreamEventFactory(0, metaStreamEvent.getAfterWindowData().size(),
                metaStreamEvent.getOutputData().size());

    }

    @Override
    public void process(StreamEvent event) {
        StreamEvent currentEvent;
        StreamEvent head = event;
        while (event != null) {
            processEvent(event);
            currentEvent = event;
            event = event.getNext();
            if (count == length) {
                currentEvent.setNext(removeEventHead);
                removeEventHead = null;
                removeEventTail.setNext(event);
                count = 0;
            }
        }
        nextProcessor.process(head);
    }

    /**
     * Create a copy of in event and store as remove event to emit at window expiration as expired events.
     *
     * @param event
     */
    private void processEvent(StreamEvent event) {      //can do in event or borrow from pool??
        StreamEvent removeEvent = removeEventFactory.newInstance();
        if(removeEvent.getOnAfterWindowData()!=null) {
            System.arraycopy(event.getOnAfterWindowData(), 0, removeEvent.getOnAfterWindowData(), 0,
                    event.getOnAfterWindowData().length);
        }
        System.arraycopy(event.getOutputData(), 0, removeEvent.getOutputData(), 0, event.getOutputData().length);
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
