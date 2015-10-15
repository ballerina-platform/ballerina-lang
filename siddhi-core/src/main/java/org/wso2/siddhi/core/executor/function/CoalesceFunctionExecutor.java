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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class CoalesceFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length == 0) {
            throw new ExecutionPlanValidationException("Coalesce must have at least one parameter");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            if (type != expressionExecutor.getReturnType()) {
                throw new ExecutionPlanValidationException("Coalesce cannot have parameters with different type");
            }
        }
        returnType = type;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    protected Object execute(Object[] obj) {
        for (Object aObj : obj) {
            if (aObj != null) {
                return aObj;
            }
        }
        return null;
    }

    @Override
    protected Object execute(Object data) {
        return data;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    @Override
    public Object[] currentState() {
        //No states
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}
