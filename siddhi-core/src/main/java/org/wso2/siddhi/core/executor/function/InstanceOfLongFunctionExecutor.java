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

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Map;

/**
 * Executor class for instanceOf Long function. Function execution logic is implemented in execute here.
 */
@Extension(
        name = "instanceOfLong",
        namespace = "",
        description = "Checks whether the parameter is an instance of Long or not.",
        parameters = {
                @Parameter(name = "arg",
                        description = "The parameter to be checked.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                DataType.STRING, DataType.BOOL, DataType.OBJECT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returned type will be boolean and true if and only if the input is a instance of Long.",
                type = {DataType.BOOL}),
        examples = {
                @Example(
                        syntax = "from fooStream\n" +
                                "select instanceOfLong(value) as state\n" +
                                "insert into barStream;",
                        description = "This will return true if the value field format is long ex : 56456l."
                ),
                @Example(
                        syntax = "from fooStream\n" +
                                "select instanceOfLong(switchState) as state\n" +
                                "insert into barStream;",
                        description = "if the switchState = true then this will returns false as the value is an" +
                                " instance of the boolean not a long."
                )
        }
)
public class InstanceOfLongFunctionExecutor extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.BOOL;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to instanceOfLong() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null; //Since the instanceOfLong function takes in 1 parameter, this method does not get called. Hence,
        // not implemented.
    }

    @Override
    protected Object execute(Object data) {
        return data instanceof Long;
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
