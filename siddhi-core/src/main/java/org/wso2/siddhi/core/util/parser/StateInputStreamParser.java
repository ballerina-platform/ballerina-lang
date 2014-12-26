/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.StateStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.state.StreamPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.runtime.EveryInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.InnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.NextInnerStateRuntime;
import org.wso2.siddhi.core.query.input.stream.state.runtime.StreamInnerStateRuntime;
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

        StateStreamRuntime stateStreamRuntime = new StateStreamRuntime(executionPlanContext);

        for (String streamId : stateInputStream.getAllStreamIds()) {
            int streamCount = stateInputStream.getStreamCount(streamId);
            if (streamCount == 1) {
                processStreamReceiverMap.put(streamId, new ProcessStreamReceiver(streamId));
            } else {
                processStreamReceiverMap.put(streamId, new MultiProcessStreamReceiver(streamId, streamCount));
            }
        }

        StateElement stateElement = stateInputStream.getStateElement();

        InnerStateRuntime innerStateRuntime = parse(stateElement, definitionMap, metaStateEvent,
                executionPlanContext, executors, processStreamReceiverMap);

        stateStreamRuntime.setInnerStateRuntime(innerStateRuntime);

        return stateStreamRuntime;
    }

    private static InnerStateRuntime parse(StateElement stateElement, Map<String, AbstractDefinition> definitionMap,
                                           MetaStateEvent metaStateEvent, ExecutionPlanContext executionPlanContext,
                                           List<VariableExpressionExecutor> executors,
                                           Map<String, ProcessStreamReceiver> processStreamReceiverMap) {


        if (stateElement instanceof StreamStateElement) {

            BasicSingleInputStream basicSingleInputStream = ((StreamStateElement) stateElement).getBasicSingleInputStream();
            SingleStreamRuntime singleStreamRuntime = SingleInputStreamParser.parseInputStream(basicSingleInputStream,
                    executionPlanContext, executors, definitionMap, metaStateEvent, true,
                    processStreamReceiverMap.get(basicSingleInputStream.getStreamId()));

            int stateIndex = metaStateEvent.getStreamEventCount() - 1;
            StreamPreStateProcessor streamPreStateProcessor = new StreamPreStateProcessor();
            streamPreStateProcessor.setStateId(stateIndex);
            streamPreStateProcessor.setNextProcessor(singleStreamRuntime.getProcessorChain());
            singleStreamRuntime.setProcessorChain(streamPreStateProcessor);

            StreamPostStateProcessor streamPostStateProcessor = new StreamPostStateProcessor();
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
                    executionPlanContext, executors, processStreamReceiverMap);

            StateElement nextElement = ((NextStateElement) stateElement).getNextStateElement();
            InnerStateRuntime nextInnerStateRuntime = parse(nextElement, definitionMap, metaStateEvent,
                    executionPlanContext, executors, processStreamReceiverMap);

            currentInnerStateRuntime.getLastProcessor().setNextStateProcessor(nextInnerStateRuntime.getFirstProcessor());

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
                    executionPlanContext, executors, processStreamReceiverMap);

            EveryInnerStateRuntime everyInnerStateRuntime = new EveryInnerStateRuntime(innerStateRuntime);

            everyInnerStateRuntime.setFirstProcessor(innerStateRuntime.getFirstProcessor());
            everyInnerStateRuntime.setLastProcessor(innerStateRuntime.getLastProcessor());

            for (SingleStreamRuntime singleStreamRuntime : innerStateRuntime.getSingleStreamRuntimeList()) {
                everyInnerStateRuntime.addStreamRuntime(singleStreamRuntime);
            }

            everyInnerStateRuntime.getLastProcessor().setNextEveryStatePerProcessor(everyInnerStateRuntime.getFirstProcessor());

            return everyInnerStateRuntime;

        } else if (stateElement instanceof LogicalStateElement) {

//            StateElement stateElement1 = ((LogicalStateElement) stateElement).getStreamStateElement1();
//            parse(stateElement1, definitionMap, metaStateEvent, stateStreamRuntime, executionPlanContext, executors, processStreamReceiverMap);
//            SingleStreamRuntime singleStreamRuntime1 = stateStreamRuntime.getSingleStreamRuntimes().getLast();
//
//            PreStateProcessor preStateProcessor1 = (PreStateProcessor) singleStreamRuntime1.getProcessorChain();
//            PostStateProcessor postStateProcessor1 = (PostStateProcessor) getLastProcessor(singleStreamRuntime1
//                    .getProcessorChain());
//
//            StateElement stateElement2 = ((LogicalStateElement) stateElement).getStreamStateElement2();
//            parse(stateElement2, definitionMap, metaStateEvent, stateStreamRuntime, executionPlanContext, executors,
//                    processStreamReceiverMap);
//            SingleStreamRuntime singleStreamRuntime2 = stateStreamRuntime.getSingleStreamRuntimes().getLast();
//
//            PreStateProcessor preStateProcessor2 = (PreStateProcessor) singleStreamRuntime2.getProcessorChain();
//            PostStateProcessor postStateProcessor2 = (PostStateProcessor) getLastProcessor(singleStreamRuntime2
//                    .getProcessorChain());

            throw new OperationNotSupportedException();
        } else if (stateElement instanceof CountStateElement) {
            throw new OperationNotSupportedException();

        } else {
            throw new OperationNotSupportedException();
            //todo   not
        }

    }

}
