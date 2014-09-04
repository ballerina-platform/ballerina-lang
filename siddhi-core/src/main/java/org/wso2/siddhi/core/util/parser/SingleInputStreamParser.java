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

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.stream.QueryStreamReceiver;
import org.wso2.siddhi.core.stream.runtime.SingleStreamRuntime;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

public class SingleInputStreamParser {


    public static SingleStreamRuntime parseInputStream(SingleInputStream inputStream, SiddhiContext context, MetaStreamEvent metaStreamEvent, List<VariableExpressionExecutor> executors) {
        Processor processor = null;
        int i = 0;
        if (!inputStream.getStreamHandlers().isEmpty()) {
            for (StreamHandler handler : inputStream.getStreamHandlers()) {
                if (i == 0) {
                    processor = generateProcessor(handler, context, metaStreamEvent, executors);
                    i++;
                } else {
                    processor.setToLast(generateProcessor(handler, context, metaStreamEvent, executors));
                }
            }
        }
        QueryStreamReceiver queryStreamReceiver = new QueryStreamReceiver((StreamDefinition) metaStreamEvent.getDefinition());
        SingleStreamRuntime singleStreamRuntime = new SingleStreamRuntime(queryStreamReceiver, processor);
        return singleStreamRuntime;
    }

    private static Processor generateProcessor(StreamHandler handler, SiddhiContext context, MetaStreamEvent metaStreamEvent, List<VariableExpressionExecutor> executors) {
        if (handler instanceof Filter) {
            Expression condition = ((Filter) handler).getFilterExpression();
            try {
                return new FilterProcessor(ExpressionParser.parseExpression(condition, null, context, null, metaStreamEvent, executors));  //metaStreamEvent has stream definition info
            } catch (ValidatorException e) {
                //This will never occur
                return null;
            }
        } else {
            //TODO else if (window function etc)
            throw new OperationNotSupportedException("Only filter operation is supported at the moment");
        }
    }
}
