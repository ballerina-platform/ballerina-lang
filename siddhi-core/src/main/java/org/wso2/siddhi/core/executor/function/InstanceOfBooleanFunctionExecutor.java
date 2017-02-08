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

package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Map;

/**
 * instanceOfBoolean(input)
 * This method returns true if and only if the input is a instance of Boolean
 * input - the value to check for Boolean instance eg: true
 * Accept Type(s) for instanceOfBoolean(input);
 *         input : BOOLEAN, STRING, INT, FLOAT, DOUBLE, LONG
 * Return Type(s): BOOLEAN
 */
//@Description("Checks whether the parameter is an instance of Boolean or not.")
//@Parameters({
//        @Parameter(name = "arg", type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
//                DataType.STRING, DataType.BOOL, DataType.OBJECT})
//})
//@Return(type = {DataType.BOOL})
@Extension(
        name = "instanceOfBoolean",
        namespace = "",
        description = "",
        parameters = {},
        returnAttributes = {}
)
public class InstanceOfBooleanFunctionExecutor extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.BOOL;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to instanceOfBoolean() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;//Since the instanceOfBoolean function takes in 1 parameter, this method does not get called. Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        return data instanceof Boolean;
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
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {

    }
}
