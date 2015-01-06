/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.join.Finder;
import org.wso2.siddhi.core.query.input.stream.join.JoinProcessor;
import org.wso2.siddhi.core.query.input.stream.join.JoinStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.window.FindableProcessor;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;

import java.util.List;

public class JoinInputStreamParser {


    public static JoinStreamRuntime parseInputStream(SingleStreamRuntime leftStreamRuntime, SingleStreamRuntime rightStreamRuntime,
                                                     JoinInputStream joinInputStream, ExecutionPlanContext executionPlanContext,
                                                     MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {

        JoinProcessor leftPreJoinProcessor = new JoinProcessor(true, true);
        JoinProcessor leftPostJoinProcessor = new JoinProcessor(true, false);

        WindowProcessor leftWindowProcessor = insertJoinProcessorsAndGetWindow(leftPreJoinProcessor,
                leftPostJoinProcessor, leftStreamRuntime);

        JoinProcessor rightPreJoinProcessor = new JoinProcessor(false, true);
        JoinProcessor rightPostJoinProcessor = new JoinProcessor(false, false);

        WindowProcessor rightWindowProcessor = insertJoinProcessorsAndGetWindow(rightPreJoinProcessor,
                rightPostJoinProcessor, rightStreamRuntime);

        leftPreJoinProcessor.setFindableProcessor((FindableProcessor) rightWindowProcessor);
        leftPostJoinProcessor.setFindableProcessor((FindableProcessor) rightWindowProcessor);

        rightPreJoinProcessor.setFindableProcessor((FindableProcessor) leftWindowProcessor);
        rightPostJoinProcessor.setFindableProcessor((FindableProcessor) leftWindowProcessor);

        //todo fix
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(joinInputStream.getOnCompare(),
                metaStateEvent, -1, executors, executionPlanContext, false, 0);
        Finder leftFinder = new Finder(expressionExecutor, 1, 0);
        Finder rightFinder = new Finder(expressionExecutor, 0, 1);

        if (joinInputStream.getTrigger() != JoinInputStream.EventTrigger.LEFT) {
            rightPreJoinProcessor.setTrigger(true);
            rightPreJoinProcessor.setFinder(rightFinder);
            rightPostJoinProcessor.setTrigger(true);
            rightPostJoinProcessor.setFinder(rightFinder);
        }
        if (joinInputStream.getTrigger() != JoinInputStream.EventTrigger.RIGHT) {
            leftPreJoinProcessor.setTrigger(true);
            leftPreJoinProcessor.setFinder(leftFinder);
            leftPostJoinProcessor.setTrigger(true);
            leftPostJoinProcessor.setFinder(leftFinder);
        }

        JoinStreamRuntime joinStreamRuntime = new JoinStreamRuntime(executionPlanContext,metaStateEvent);
        joinStreamRuntime.addRuntime(leftStreamRuntime);
        joinStreamRuntime.addRuntime(rightStreamRuntime);
        return joinStreamRuntime;
    }

    private static WindowProcessor insertJoinProcessorsAndGetWindow(JoinProcessor preJoinProcessor,
                                                                    JoinProcessor postJoinProcessor,
                                                                    SingleStreamRuntime streamRuntime) {
        Processor lastProcessor = streamRuntime.getProcessorChain();
        Processor prevLastProcessor = null;
        if (lastProcessor != null) {
            while (lastProcessor.getNextProcessor() != null) {
                prevLastProcessor = lastProcessor;
                lastProcessor = lastProcessor.getNextProcessor();
            }
        }

        if (lastProcessor instanceof WindowProcessor) {
            if (prevLastProcessor != null) {
                prevLastProcessor.setNextProcessor(preJoinProcessor);
            } else {
                streamRuntime.setProcessorChain(preJoinProcessor);
            }
            preJoinProcessor.setNextProcessor(lastProcessor);
            lastProcessor.setNextProcessor(postJoinProcessor);
            return (WindowProcessor) lastProcessor;
        } else {
            throw new OperationNotSupportedException("Only streams with window can be joined");
        }

    }
}
