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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.QueryStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleThreadEntryValveProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream_function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

public class SingleInputStreamParser {

    /**
     * Parse single InputStream and return SingleStreamRuntime
     *
     * @param inputStream          single input stream to be parsed
     * @param executionPlanContext query to be parsed
     * @param metaStreamEvent      Meta event used to collect execution info of stream associated with query
     * @param executors            List to hold VariableExpressionExecutors to update after query parsing
     * @return
     */
    public static SingleStreamRuntime parseInputStream(SingleInputStream inputStream, ExecutionPlanContext executionPlanContext,
                                                       MetaStreamEvent metaStreamEvent, List<VariableExpressionExecutor> executors) {
        Processor processor = null;
        Processor singleThreadValve = null;
        boolean first = true;
        if (!inputStream.getStreamHandlers().isEmpty()) {
            for (StreamHandler handler : inputStream.getStreamHandlers()) {
                Processor currentProcessor = generateProcessor(handler, executionPlanContext, metaStreamEvent,
                        executors);
                if (currentProcessor instanceof SchedulingProcessor) {
                    if (singleThreadValve == null) {

                        singleThreadValve = new SingleThreadEntryValveProcessor(executionPlanContext);
                        if (first) {
                            processor = singleThreadValve;
                            first = false;
                        } else {
                            processor.setToLast(singleThreadValve);
                        }
                    }
                    Scheduler scheduler = new Scheduler(executionPlanContext.getScheduledExecutorService(), singleThreadValve);
                    ((SchedulingProcessor) currentProcessor).setScheduler(scheduler);
                }
                if (first) {
                    processor = currentProcessor;
                    first = false;
                } else {
                    processor.setToLast(currentProcessor);
                }
            }
        }
        metaStreamEvent.initializeAfterWindowData();
        QueryStreamReceiver queryStreamReceiver = new QueryStreamReceiver(metaStreamEvent.getInputDefinition().getId());
        return new SingleStreamRuntime(queryStreamReceiver, processor);
    }

    private static Processor generateProcessor(StreamHandler handler, ExecutionPlanContext context, MetaStreamEvent metaStreamEvent,
                                               List<VariableExpressionExecutor> executors) {
        ExpressionExecutor[] inputExpressions = new ExpressionExecutor[handler.getParameters().length];
        Expression[] parameters = handler.getParameters();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            inputExpressions[i] = ExpressionParser.parseExpression(parameters[i], context, metaStreamEvent, executors,
                    false);
        }
        if (handler instanceof Filter) {
            return new FilterProcessor(inputExpressions[0]);

        } else if (handler instanceof Window) {
            WindowProcessor windowProcessor = (WindowProcessor) SiddhiClassLoader.loadSiddhiImplementation(((Window) handler).getFunction(),
                    WindowProcessor.class);
            windowProcessor.initProcessor(metaStreamEvent.getInputDefinition(), inputExpressions);
            return windowProcessor;

        } else if (handler instanceof StreamFunction) {
            StreamProcessor streamProcessor = (StreamFunctionProcessor) SiddhiClassLoader.loadSiddhiImplementation(
                    ((StreamFunction) handler).getFunction(), StreamFunctionProcessor.class);
            metaStreamEvent.setInputDefinition(streamProcessor.initProcessor(metaStreamEvent.getInputDefinition(),
                    inputExpressions));
            return streamProcessor;

        } else {
            throw new IllegalStateException(handler.getClass().getName() + " is not supported");
        }
    }
}
