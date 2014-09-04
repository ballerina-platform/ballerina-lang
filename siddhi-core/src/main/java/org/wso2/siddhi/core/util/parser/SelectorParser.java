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
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.PassThroughAttributeProcessor;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.function.AttributeFunction;

import java.util.ArrayList;
import java.util.List;

public class SelectorParser {
    public static QuerySelector parse(Selector selector, OutputStream outStream, SiddhiContext context, MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {
        boolean currentOn = false;
        boolean expiredOn = false;
        String id = null;

        if (outStream != null) {
            if (outStream.getOutputEventType() == OutputStream.OutputEventType.CURRENT_EVENTS || outStream.getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
                currentOn = true;
            }
            if (outStream.getOutputEventType() == OutputStream.OutputEventType.EXPIRED_EVENTS || outStream.getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
                expiredOn = true;
            }

            id = outStream.getId();
        } else {
            currentOn = true;
            expiredOn = true;
        }
        QuerySelector querySelector = new QuerySelector(id, selector, currentOn, expiredOn, context);
        querySelector.setAttributeProcessorList(getAttributeProcessors(selector, outStream,context, metaStateEvent, executors));
        return querySelector;
    }

    private static List<AttributeProcessor> getAttributeProcessors(Selector selector, OutputStream outStream, SiddhiContext siddhiContext, MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {
        List<AttributeProcessor> attributeProcessorList = new ArrayList<AttributeProcessor>();
        StreamDefinition temp = new StreamDefinition(outStream.getId());
        for (OutputAttribute outputAttribute : selector.getSelectionList()) {
            try {
                if (outputAttribute.getExpression() instanceof Constant) {
                    PassThroughAttributeProcessor attributeGenerator = new PassThroughAttributeProcessor(ExpressionParser.parseExpression(outputAttribute.getExpression(),
                            null, siddhiContext, null, metaStateEvent, executors));
                    attributeProcessorList.add(attributeGenerator);
                    temp.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
                } else if (outputAttribute.getExpression() instanceof AttributeFunction) {
                    //TODO implement
                    throw new OperationNotSupportedException("Attribute Functions are not supported");
                } else if (outputAttribute.getExpression() instanceof Variable) {
                    PassThroughAttributeProcessor attributeGenerator;
                    if (metaStateEvent.getEventCount() == 1) {  //meta stream event
                        attributeGenerator = new PassThroughAttributeProcessor(ExpressionParser.parseExpression(outputAttribute.getExpression(),
                                null, siddhiContext, null, metaStateEvent.getMetaEvent(0), executors));
                        temp.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
                    } else {
                        attributeGenerator = new PassThroughAttributeProcessor(ExpressionParser.parseExpression(outputAttribute.getExpression(),
                                null, siddhiContext, null, metaStateEvent, executors));
                        temp.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
                    }
                    attributeProcessorList.add(attributeGenerator);
                }
                metaStateEvent.setOutputDefinition(temp);
            } catch (ValidatorException e) {
                //this will never happen as this is already validated
            }
        }
        return attributeProcessorList;
    }
}
