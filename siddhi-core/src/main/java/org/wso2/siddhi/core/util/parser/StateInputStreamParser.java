/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org)
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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.MultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.ProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.SingleProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.*;
import org.wso2.siddhi.core.query.input.stream.state.runtime.*;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateInputStreamParser {


    public static StateStreamRuntime parseInputStream(StateInputStream stateInputStream,
                                                      ExecutionPlanContext executionPlanContext,
                                                      MetaStateEvent metaStateEvent,
                                                      List<VariableExpressionExecutor> executors,
                                                      Map<String, AbstractDefinition> definitionMap) {

        Map<String, ProcessStreamReceiver> processStreamReceiverMap = new HashMap<String, ProcessStreamReceiver>();

        StateStreamRuntime stateStreamRuntime = new StateStreamRuntime(executionPlanContext, metaStateEvent);

        for (String streamId : stateInputStream.getAllStreamIds()) {
            int streamCount = stateInputStream.getStreamCount(streamId);
            if (streamCount == 1) {
                processStreamReceiverMap.put(streamId, new SingleProcessStreamReceiver(streamId));
            } else {
                processStreamReceiverMap.put(streamId, new MultiProcessStreamReceiver(streamId, streamCount));
            }
        }

        StateElement stateElement = stateInputStream.getStateElement();

        InnerStateRuntime innerStateRuntime = parse(stateElement, definitionMap, metaStateEvent,
                executionPlanContext, executors, processStreamReceiverMap, null, null);

        stateStreamRuntime.setInnerStateRuntime(innerStateRuntime);

        return stateStreamRuntime;
    }

    private static InnerStateRuntime parse(StateElement stateElement, Map<String, AbstractDefinition> definitionMap,
                                           MetaStateEvent metaStateEvent, ExecutionPlanContext executionPlanContext,
                                           List<VariableExpressionExecutor> executors,
                                           Map<String, ProcessStreamReceiver> processStreamReceiverMap,
                                           StreamPreStateProcessor streamPreStateProcessor,
                                           StreamPostStateProcessor streamPostStateProcessor) {


        if (stateElement instanceof StreamStateElement) {

            BasicSingleInputStream basicSingleInputStream = ((StreamStateElement) stateElement).getBasicSingleInputStream();
            SingleStreamRuntime singleStreamRuntime = SingleInputStreamParser.parseInputStream(basicSingleInputStream,
                    executionPlanContext, executors, definitionMap, metaStateEvent,
                    processStreamReceiverMap.get(basicSingleInputStream.getStreamId()));

            int stateIndex = metaStateEvent.getStreamEventCount() - 1;
            if (streamPreStateProcessor == null) {
                streamPreStateProcessor = new StreamPreStateProcessor();
            }
            streamPreStateProcessor.setStateId(stateIndex);
            streamPreStateProcessor.setNextProcessor(singleStreamRuntime.getProcessorChain());
            singleStreamRuntime.setProcessorChain(streamPreStateProcessor);
            if (streamPostStateProcessor == null) {
                streamPostStateProcessor = new StreamPostStateProcessor();
            }
            streamPostStateProcessor.setStateId(stateIndex);
            singleStreamRuntime.getProcessorChain().setToLast(streamPostStateProcessor);
            streamPostStateProcessor.setThisStatePreProcessor(streamPreStateProcessor);

            StreamInnerStateRuntime innerStateRuntime = new StreamInnerStateRuntime();

            innerStateRuntime.setFirstProcessor(streamPreStateProcessor);
            innerStateRuntime.setLastProcessor(streamPostStateProcessor);
            innerStateRuntime.addStreamRuntime(singleStreamRuntime);

            return innerStateRuntime;

        } else if (stateElement instanceof NextStateElement) {

            StateElement currentElement = ((NextStateElement) stateElement).getStateElement();
            InnerStateRuntime currentInnerStateRuntime = parse(currentElement, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap, streamPreStateProcessor, streamPostStateProcessor);

            StateElement nextElement = ((NextStateElement) stateElement).getNextStateElement();
            InnerStateRuntime nextInnerStateRuntime = parse(nextElement, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap, streamPreStateProcessor, streamPostStateProcessor);

            currentInnerStateRuntime.getLastProcessor().setNextStatePreProcessor(nextInnerStateRuntime.getFirstProcessor());

            NextInnerStateRuntime nextStateRuntime = new NextInnerStateRuntime(currentInnerStateRuntime, nextInnerStateRuntime);
            nextStateRuntime.setFirstProcessor(currentInnerStateRuntime.getFirstProcessor());
            nextStateRuntime.setLastProcessor(nextInnerStateRuntime.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : currentInnerStateRuntime.getSingleStreamRuntimeList()) {
                nextStateRuntime.addStreamRuntime(singleStreamRuntime);
            }
            for (SingleStreamRuntime singleStreamRuntime : nextInnerStateRuntime.getSingleStreamRuntimeList()) {
                nextStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            return nextStateRuntime;

        } else if (stateElement instanceof EveryStateElement) {

            StateElement currentElement = ((EveryStateElement) stateElement).getStateElement();
            InnerStateRuntime innerStateRuntime = parse(currentElement, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap, streamPreStateProcessor, streamPostStateProcessor);

            EveryInnerStateRuntime everyInnerStateRuntime = new EveryInnerStateRuntime(innerStateRuntime);

            everyInnerStateRuntime.setFirstProcessor(innerStateRuntime.getFirstProcessor());
            everyInnerStateRuntime.setLastProcessor(innerStateRuntime.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime.getSingleStreamRuntimeList()) {
                everyInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            everyInnerStateRuntime.getLastProcessor().setNextEveryStatePerProcessor(everyInnerStateRuntime.getFirstProcessor());

            return everyInnerStateRuntime;

        } else if (stateElement instanceof LogicalStateElement) {

            LogicalStateElement.Type type = ((LogicalStateElement) stateElement).getType();

            LogicalPreStateProcessor logicalPreStateProcessor1 = new LogicalPreStateProcessor(type);
            LogicalPostStateProcessor logicalPostStateProcessor1 = new LogicalPostStateProcessor(type);

            LogicalPreStateProcessor logicalPreStateProcessor2 = new LogicalPreStateProcessor(type);
            LogicalPostStateProcessor logicalPostStateProcessor2 = new LogicalPostStateProcessor(type);

            logicalPostStateProcessor1.setPartnerPreStateProcessor(logicalPreStateProcessor2);
            logicalPostStateProcessor2.setPartnerPreStateProcessor(logicalPreStateProcessor1);

            logicalPostStateProcessor1.setPartnerPostStateProcessor(logicalPostStateProcessor2);
            logicalPostStateProcessor2.setPartnerPostStateProcessor(logicalPostStateProcessor1);

            logicalPreStateProcessor1.setPartnerStatePreProcessor(logicalPreStateProcessor2);
            logicalPreStateProcessor2.setPartnerStatePreProcessor(logicalPreStateProcessor1);

            StateElement stateElement2 = ((LogicalStateElement) stateElement).getStreamStateElement2();
            InnerStateRuntime innerStateRuntime2 = parse(stateElement2, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap,
                    logicalPreStateProcessor2, logicalPostStateProcessor2);

            StateElement stateElement1 = ((LogicalStateElement) stateElement).getStreamStateElement1();
            InnerStateRuntime innerStateRuntime1 = parse(stateElement1, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap,
                    logicalPreStateProcessor1, logicalPostStateProcessor1);


            LogicalInnerStateRuntime logicalInnerStateRuntime = new LogicalInnerStateRuntime(
                    innerStateRuntime1, innerStateRuntime2);

            logicalInnerStateRuntime.setFirstProcessor(innerStateRuntime1.getFirstProcessor());
            logicalInnerStateRuntime.setLastProcessor(innerStateRuntime2.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime2.getSingleStreamRuntimeList()) {
                logicalInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime1.getSingleStreamRuntimeList()) {
                logicalInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            return logicalInnerStateRuntime;

        } else if (stateElement instanceof CountStateElement) {

            int minCount = ((CountStateElement) stateElement).getMinCount();
            int maxCount = ((CountStateElement) stateElement).getMaxCount();
            if (minCount == SiddhiConstants.ANY) {
                minCount = 0;
            }
            if (maxCount == SiddhiConstants.ANY) {
                maxCount = Integer.MAX_VALUE;
            }

            CountPreStateProcessor countPreStateProcessor = new CountPreStateProcessor(minCount, maxCount);
            CountPostStateProcessor countPostStateProcessor = new CountPostStateProcessor(minCount, maxCount);

            countPreStateProcessor.setCountPostStateProcessor(countPostStateProcessor);
            StateElement currentElement = ((CountStateElement) stateElement).getStreamStateElement();
            InnerStateRuntime innerStateRuntime = parse(currentElement, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap, countPreStateProcessor, countPostStateProcessor);

            return new CountInnerStateRuntime((StreamInnerStateRuntime) innerStateRuntime);

        } else {
            throw new OperationNotSupportedException();
            //todo   not
        }

    }

}
