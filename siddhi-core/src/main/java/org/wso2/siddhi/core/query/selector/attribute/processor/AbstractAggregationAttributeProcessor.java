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
package org.wso2.siddhi.core.query.selector.attribute.processor;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.ComplexMetaEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.ValidatorException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAggregationAttributeProcessor extends FunctionExecutor implements AttributeProcessor{

    protected String elementId;
    protected OutputAttributeAggregator sampleOutputAttributeAggregator;
    protected List<ExpressionExecutor> expressionExecutors;
    protected final OutputAttributeAggregatorFactory outputAttributeAggregatorFactory;
    protected SiddhiContext siddhiContext;
    private final int size;

    public AbstractAggregationAttributeProcessor(Expression[] expressions,
                                                 Map<String, StreamDefinition> tempStreamDefinitionMap,
                                                 OutputAttributeAggregatorFactory outputAttributeAggregatorFactory,
                                                 ComplexMetaEvent metaEvent, List<VariableExpressionExecutor> variableExpressionExecutors,
                                                 SiddhiContext siddhiContext) {
        this.outputAttributeAggregatorFactory = outputAttributeAggregatorFactory;
        this.siddhiContext = siddhiContext;
        this.expressionExecutors = new ArrayList<ExpressionExecutor>();
        for (Expression expression : expressions) {
            try {
                this.expressionExecutors.add(ExpressionParser.parseExpression(expression, null, siddhiContext, tempStreamDefinitionMap, metaEvent, variableExpressionExecutors));
            } catch (ValidatorException e) {
                //TODO
            }
        }


        this.elementId = elementId;
        Attribute.Type[] attributeTypes = new Attribute.Type[expressionExecutors.size()];
        for (int i = 0; i < expressionExecutors.size(); i++) {
            attributeTypes[i] = expressionExecutors.get(i).getReturnType();
        }
        this.sampleOutputAttributeAggregator = outputAttributeAggregatorFactory.createAttributeAggregator(attributeTypes);
        siddhiContext.addEternalReferencedHolder(sampleOutputAttributeAggregator);
        size = expressionExecutors.size();
    }

    public AbstractAggregationAttributeProcessor(List<ExpressionExecutor> expressionExecutors,OutputAttributeAggregator outputAttributeAggregator,SiddhiContext siddhiContext) {
        this.expressionExecutors = expressionExecutors;
        this.sampleOutputAttributeAggregator = outputAttributeAggregator.newInstance();
        siddhiContext.addEternalReferencedHolder(sampleOutputAttributeAggregator);
        this.outputAttributeAggregatorFactory = null;
        size = expressionExecutors.size();
    }


    protected Object process(StreamEvent event, OutputAttributeAggregator outputAttributeAggregator) {
        if (size > 1) {
            Object[] data = new Object[expressionExecutors.size()];
            for (int i = 0, size = data.length; i < size; i++) {
                data[i] = expressionExecutors.get(i).execute(event);
            }
//            if (event instanceof RemoveStream) {
//                return outputAttributeAggregator.processRemove(data);
//            } else {
                return outputAttributeAggregator.processAdd(data);
//            }
        } else {
//            if (event instanceof RemoveStream) {
//                return outputAttributeAggregator.processRemove(expressionExecutors.get(0).execute(event));
//            } else {
                return outputAttributeAggregator.processAdd(expressionExecutors.get(0).execute(event));
//            }
        }
    }


    @Override
    public Attribute.Type getOutputType() {
        return sampleOutputAttributeAggregator.getReturnType();
    }





}
