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
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;

import java.util.LinkedList;
import java.util.List;

public abstract class StreamFunctionProcessor extends StreamProcessor {

    @Override
    protected void process(ComplexEventChunk complexEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, StreamEventPopulater streamEventPopulater) {
        while (complexEventChunk.hasNext()) {
            ComplexEvent complexEvent = complexEventChunk.next();

            Object[] inputData = new Object[inputExecutors.length];
            for (int i = 0, inputExecutorsLength = inputExecutors.length; i < inputExecutorsLength; i++) {
                ExpressionExecutor executor = inputExecutors[i];
                inputData[i] = executor.execute(complexEvent);
            }
            Object[] outputData = process(inputData);
            streamEventPopulater.populateStreamEvent(complexEvent, outputData);

        }
        nextProcessor.process(complexEventChunk);

    }

    /**
     * The process method of the StreamFunction
     *
     * @param inputData the data values for the function parameters
     * @return the date for additional output attributes introduced by the function
     */
    protected abstract Object[] process(Object[] inputData);

    @Override
    protected StreamProcessor cloneStreamProcessor() {
        //todo fix
        try {
            StreamFunctionProcessor streamFunctionProcessor = this.getClass().newInstance();
            List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
            for (ExpressionExecutor attributeExecutor : this.inputExecutors) {
                innerExpressionExecutors.add(attributeExecutor.cloneExecutor());
            }
//            streamFunctionProcessor.initExecutor(innerExpressionExecutors, executionPlanContext);
//            streamFunctionProcessor.start();
            return streamFunctionProcessor;
        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
//        return cloneStreamFunctionProcessor();
    }

//    protected abstract StreamFunctionProcessor cloneStreamFunctionProcessor();
}
