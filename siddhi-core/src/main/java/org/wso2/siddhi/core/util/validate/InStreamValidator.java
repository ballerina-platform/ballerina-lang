/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.util.validate;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Map;

public class InStreamValidator {
    /**
     * Validate a given input stream. Complex streams should be handled at upper layer
     *
     * @param inputStream
     * @param streamDefinitionMap @throws ValidatorException
     * @param tempDefinitionMap
     */
    public static void validate(InputStream inputStream, Map<String, StreamDefinition> streamDefinitionMap, Map<String, StreamDefinition> tempDefinitionMap) throws ValidatorException {
        if (inputStream instanceof BasicSingleInputStream || inputStream instanceof SingleInputStream) {
            SingleInputStream stream = (SingleInputStream) inputStream;
            tempDefinitionMap.put(stream.getStreamId(), streamDefinitionMap.get(stream.getStreamId()));
            String defaultDefinition = stream.getStreamId();
            SiddhiContext mockSiddhiContext = new SiddhiContext();
            for (StreamHandler handler : stream.getStreamHandlers()) {
                if (handler instanceof Filter) {
                    Expression condition = ((Filter) handler).getFilterExpression();
                    ExpressionParser.parseExpression(condition, defaultDefinition, mockSiddhiContext, tempDefinitionMap, null, null);
                } else if (handler instanceof Window) {
                    for (Expression expression : ((Window) handler).getParameters()) {
                        ExpressionParser.parseExpression(expression, defaultDefinition, mockSiddhiContext, tempDefinitionMap, null, null);
                    }
                } else if (handler instanceof StreamFunction) {
                    //TODO: handle. get output attr names and types and set them in temp map
                }
            }

            if ((stream.getStreamReferenceId() != null) && !(stream.getStreamId()).equals(stream.getStreamReferenceId())) { //if ref id is provided and different
                StreamDefinition temp = tempDefinitionMap.remove(((SingleInputStream) inputStream).getStreamId());          //remove original definition from temp map
                tempDefinitionMap.put(((SingleInputStream) inputStream).getStreamReferenceId(), temp);                      //add ref id to original map
            }
        }
    }
}
