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
package org.wso2.siddhi.core.query.processor.stream_function;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulater;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;

public abstract class StreamFunctionProcessor extends StreamProcessor {

    @Override
    protected void process(ComplexEventChunk complexEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, StreamEventPopulater streamEventPopulater) {
        while (complexEventChunk.hasNext()) {
            ComplexEvent complexEvent = complexEventChunk.next();

            if (attributeExpressionLength > 1) {
                Object[] inputData = new Object[attributeExpressionLength];
                for (int i = 0; i < attributeExpressionLength; i++) {
                    inputData[i] = attributeExpressionExecutors[i].execute(complexEvent);
                }
                Object[] outputData = process(inputData);
                streamEventPopulater.populateStreamEvent(complexEvent, outputData);
            } else {
                Object outputData = process(attributeExpressionExecutors[0].execute(complexEvent));
                streamEventPopulater.populateStreamEvent(complexEvent, outputData);
            }


        }
        nextProcessor.process(complexEventChunk);

    }

    /**
     * The process method of the StreamFunction, used when multiple function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the date for additional output attributes introduced by the function
     */
    protected abstract Object[] process(Object[] data);


    /**
     * The process method of the StreamFunction, used when single function parameter is provided
     *
     * @param data the data value for the function parameter
     * @return the date for additional output attribute introduced by the function
     */
    protected abstract Object process(Object data);


}
