/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.extension.util;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute.Type;


public class StringConcatAggregatorString extends AttributeAggregator {
    private static final long serialVersionUID = 1358667438272544590L;
    private String aggregatedStringValue = "";

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         SiddhiContext
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

    }

    @Override
    public Type getReturnType() {
        return Type.STRING;
    }


    @Override
    public Object processAdd(Object data) {
        aggregatedStringValue = aggregatedStringValue + data;
        return aggregatedStringValue;
    }

    @Override
    public Object processAdd(Object[] data) {
        for (Object aData : data) {
            aggregatedStringValue = aggregatedStringValue + aData;
        }
        return aggregatedStringValue;
    }


    @Override
    public Object processRemove(Object data) {
        aggregatedStringValue = aggregatedStringValue.replaceFirst(data.toString(), "");
        return aggregatedStringValue;
    }

    @Override
    public Object processRemove(Object[] data) {
        for (Object aData : data) {
            aggregatedStringValue = aggregatedStringValue.replaceFirst(aData.toString(), "");
        }
        return aggregatedStringValue;
    }

    @Override
    public Object reset() {
        aggregatedStringValue = "";
        return aggregatedStringValue;
    }


    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object[] currentState() {
        return new Object[]{aggregatedStringValue};
    }

    @Override
    public void restoreState(Object[] state) {
        aggregatedStringValue = (String) state[0];
    }
}