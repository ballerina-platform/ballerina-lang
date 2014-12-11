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

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

public class LengthWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private StreamEventCloner streamEventCloner;
    private MetaStreamEvent metaStreamEvent;

    @Override
    public void init(MetaStreamEvent metaStreamEvent, StreamEventCloner streamEventCloner) {
        this.metaStreamEvent = metaStreamEvent;
        this.streamEventCloner = streamEventCloner;
        expiredEventChunk = new ComplexEventChunk<StreamEvent>();

        if (parameters != null) {
            length = ((IntConstant) parameters[0]).getValue();
        }
    }

    @Override
    public void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor) {
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
            clonedEvent.setType(StreamEvent.Type.EXPIRED);
            if (count < length) {
                count++;
                this.expiredEventChunk.add(clonedEvent);
            } else {
                StreamEvent firstEvent = this.expiredEventChunk.poll();
                streamEventChunk.insertBeforeCurrent(firstEvent);
                this.expiredEventChunk.add(clonedEvent);
            }
        }
        nextProcessor.process(streamEventChunk);
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
