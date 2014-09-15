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

package org.wso2.siddhi.core.query.selector.attribute.processor.executor;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.handler.AttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public abstract class AbstractAggregationAttributeExecutor implements ExpressionExecutor {
    protected AttributeAggregator attributeAggregator;
    protected List<ExpressionExecutor> expressionExecutors;
    protected SiddhiContext siddhiContext;
    protected int size;

    protected Object process(StreamEvent event, AttributeAggregator attributeAggregator) {
        if (size > 1) {
            Object[] data = new Object[expressionExecutors.size()];
            for (int i = 0, size = data.length; i < size; i++) {
                data[i] = expressionExecutors.get(i).execute(event);
            }
            return attributeAggregator.processAdd(data);
        } else {
            return attributeAggregator.processAdd(expressionExecutors.get(0).execute(event));
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return attributeAggregator.getReturnType();
    }





}

