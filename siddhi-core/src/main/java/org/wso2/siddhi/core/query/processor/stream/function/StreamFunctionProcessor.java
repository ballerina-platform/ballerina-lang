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
package org.wso2.siddhi.core.query.processor.stream.function;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.AbstractStreamProcessor;

public abstract class StreamFunctionProcessor extends AbstractStreamProcessor {

    @Override
    protected void processEventChunk(ComplexEventChunk complexEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (complexEventChunk.hasNext()) {
            ComplexEvent complexEvent = complexEventChunk.next();
            Object[] outputData;
            switch (attributeExpressionLength) {
                case 0:
                    outputData = process((Object) null);
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                    break;
                case 1:
                    outputData = process(attributeExpressionExecutors[0].execute(complexEvent));
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                    break;
                default:
                    Object[] inputData = new Object[attributeExpressionLength];
                    for (int i = 0; i < attributeExpressionLength; i++) {
                        inputData[i] = attributeExpressionExecutors[i].execute(complexEvent);
                    }
                    outputData = process(inputData);
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
            }
        }
        nextProcessor.process(complexEventChunk);

    }

    /**
     * The process method of the StreamFunction, used when more then one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    protected abstract Object[] process(Object[] data);


    /**
     * The process method of the StreamFunction, used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    protected abstract Object[] process(Object data);


}
