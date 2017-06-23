/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Map;

/**
 * Executor class for ifThenElse function. Function execution logic is implemented in execute here.
 */
@Extension(
        name = "ifThenElse",
        namespace = "",
        description = "Evaluates the 'condition' parameter and returns value of the 'if.expression' parameter " +
                "if the condition is true, or returns value of the 'else.expression' parameter if the condition is "
                + "false. Here both 'if.expression' and 'else.expression' should be of the same type.",
        parameters = {
                @Parameter(name = "condition",
                        description = "This specifies the if then else condition value.",
                        type = {DataType.BOOL}),
                @Parameter(name = "if.expression",
                        description = "This specifies the value to be returned if " +
                                "the value of the condition parameter is true.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                DataType.STRING, DataType.BOOL, DataType.OBJECT}),
                @Parameter(name = "else.expression",
                        description = "This specifies the value to be returned if " +
                                "the value of the condition parameter is false.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                DataType.STRING, DataType.BOOL, DataType.OBJECT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returned type will be same as the 'if.expression' and 'else.expression' type.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                        DataType.STRING, DataType.BOOL, DataType.OBJECT}),
        examples = {
                @Example(
                        syntax = "@info(name = 'query1')\n" +
                                "from sensorEventStream\n" +
                                "select sensorValue, ifThenElse(sensorValue>35,'High','Low') as status\n" +
                                "insert into outputStream;",
                        description = "This will returns High if sensorValue = 50."
                ),
                @Example(
                        syntax = "@info(name = 'query1')\n" +
                                "from sensorEventStream\n" +
                                "select sensorValue, ifThenElse(voltage < 5, 0, 1) as status\n" +
                                "insert into outputStream;",
                        description = "This will returns 1 if voltage= 12."
                ),
                @Example(
                        syntax = "@info(name = 'query1')\n" +
                                "from userEventStream\n" +
                                "select userName, ifThenElse(password == 'admin', true, false) as passwordState\n" +
                                "insert into outputStream;",
                        description = "This will returns  passwordState as true if password = admin."
                )
        }
)
public class IfThenElseFunctionExecutor extends FunctionExecutor {
    private static final Logger log = Logger.getLogger(IfThenElseFunctionExecutor.class);
    Attribute.Type returnType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 3) {
            // check whether all the arguments passed
            throw new SiddhiAppValidationException("Invalid no of arguments passed to ifThenElse() function, " +
                    "required only 3, but found " + attributeExpressionExecutors.length);
        } else if (!attributeExpressionExecutors[0].getReturnType().equals(Attribute.Type.BOOL)) {
            // check whether first argument Boolean or not
            throw new SiddhiAppValidationException("Input type of if in ifThenElse function should be of " +
                    "type BOOL, but found " + attributeExpressionExecutors[0].getReturnType());
        } else if (!attributeExpressionExecutors[1].getReturnType().equals(
                attributeExpressionExecutors[2].getReturnType())) {
            // check whether second and thirds argument's return type are equivalent.
            throw new SiddhiAppValidationException("Input type of then in ifThenElse function and else in " +
                    "ifThenElse function should be of equivalent type. but found then type: " +
                    attributeExpressionExecutors[1].getReturnType() + " and else type: " +
                    attributeExpressionExecutors[2].getReturnType());
        } else {
            returnType = attributeExpressionExecutors[1].getReturnType();
        }
    }

    @Override
    protected Object execute(Object[] data) {
        // check whether first argument true or null
        if (Boolean.TRUE.equals(data[0])) {
            return data[1];
        } else {
            return data[2];
        }
    }

    @Override
    protected Object execute(Object data) {
        // Since the e function takes in multiple parameters, this method does not get called. Hence, not implemented.
        return null;
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
        return null; // No need to maintain a state.
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        // Since there's no need to maintain a state, nothing needs to be done here.
    }

    @Override
    public Object execute(ComplexEvent event) {
        try {
            Boolean condition = Boolean.TRUE.equals(attributeExpressionExecutors[0].execute(event));
            return execute(
                    new Object[]{
                            condition,
                            (condition) ? attributeExpressionExecutors[1].execute(event) : null,
                            (!condition) ? attributeExpressionExecutors[2].execute(event) : null
                    }
            );
        } catch (Exception e) {
            log.error("Exception on siddhi app '" + siddhiAppContext.getName() +
                    "' on class '" + this.getClass().getName() + "', " + e.getMessage(), e);
            return null;
        }
    }
}
