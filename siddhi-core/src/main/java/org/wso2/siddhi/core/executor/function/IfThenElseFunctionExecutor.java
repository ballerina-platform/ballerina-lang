/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class IfThenElseFunctionExecutor extends FunctionExecutor {

    Attribute.Type returnType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 3) {
            // check whether all the arguments passed
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to ifThenElse() function, " +
                                                       "required only 3, but found " + attributeExpressionExecutors.length);
        } else if (!attributeExpressionExecutors[0].getReturnType().equals(Attribute.Type.BOOL)) {
            // check whether first argument Boolean or not
            throw new ExecutionPlanValidationException("Input type of if in ifThenElse function should be of type BOOL, " +
                                                       "but found " + attributeExpressionExecutors[0].getReturnType());
        } else if (!attributeExpressionExecutors[1].getReturnType().equals(attributeExpressionExecutors[2].getReturnType())) {
            // check whether second and thirds argument's return type are equivalent.
            throw new ExecutionPlanValidationException("Input type of then in ifThenElse function and else in ifThenElse " +
                                                       "function should be of equivalent type. but found then type: " +
                                                       attributeExpressionExecutors[1].getReturnType() + " and else type: " +
                                                       attributeExpressionExecutors[2].getReturnType());
        } else {
            returnType = attributeExpressionExecutors[1].getReturnType();
        }

    }

    @Override
    protected Object execute(Object[] data) {
        //check whether first argument true or null
        if (data[0] != null && data[0].equals(Boolean.TRUE)) {
            return data[1];
        } else {
            return data[2];
        }
    }

    @Override
    protected Object execute(Object data) {
        return null;//Since the e function takes in multiple parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[0]; //No need to maintain a state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
