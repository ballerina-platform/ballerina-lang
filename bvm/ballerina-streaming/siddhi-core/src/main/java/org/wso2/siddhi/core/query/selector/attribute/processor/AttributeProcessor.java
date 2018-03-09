/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.selector.attribute.processor;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Processor for event attributes.
 */
public class AttributeProcessor {

    private ExpressionExecutor expressionExecutor;
    private int outputPosition;

    public AttributeProcessor(ExpressionExecutor expressionExecutor) {
        this.expressionExecutor = expressionExecutor;
    }

    public Attribute.Type getOutputType() {
        return expressionExecutor.getReturnType();
    }

    public void process(ComplexEvent event) {
        event.setOutputData(expressionExecutor.execute(event), outputPosition);
    }

    public AttributeProcessor cloneProcessor(String key) {
        AttributeProcessor attributeProcessor = new AttributeProcessor(expressionExecutor.cloneExecutor(key));
        attributeProcessor.setOutputPosition(this.outputPosition);
        return attributeProcessor;

    }

    public int getOutputPosition() {
        return outputPosition;
    }

    public void setOutputPosition(int position) {
        this.outputPosition = position;
    }

    public ExpressionExecutor getExpressionExecutor() {
        return expressionExecutor;
    }
}
