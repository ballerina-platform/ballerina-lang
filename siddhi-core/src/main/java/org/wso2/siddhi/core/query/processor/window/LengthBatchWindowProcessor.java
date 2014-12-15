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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;

public class LengthBatchWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>();


    @Override
    protected void init(ExpressionExecutor[] inputExecutors) {
        if (inputExecutors != null) {
            length = (Integer) (((ConstantExpressionExecutor) inputExecutors[0]).getValue());
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
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
                if (expiredEventChunk.getFirst() != null) {
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
                count = 0;

            }
            streamEventChunk.remove();

        }
        if (streamEventChunk.getFirst() != null) {
            nextProcessor.process(streamEventChunk);
        }

    }

    @Override
    protected WindowProcessor cloneWindowProcessor() {
        LengthBatchWindowProcessor lengthBatchWindowProcessor = new LengthBatchWindowProcessor();
        lengthBatchWindowProcessor.length = length;
        return lengthBatchWindowProcessor;
    }
}
