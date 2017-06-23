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
 * Executor class for coalesce function. Returns the value of the first input parameter that is not null.
 */
@Extension(
        name = "coalesce",
        namespace = "",
        description = "Returns the value of the first input parameter that is not null, " +
                "and all input parameters have to be on the same type.",
        parameters = {
                @Parameter(name = "args",
                        description = "This function accepts one or more parameters. " +
                                "They can belong to any one of the available types." +
                                " All the specified parameters should be of the same type.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                DataType.STRING, DataType.BOOL, DataType.OBJECT})
        },
        returnAttributes = @ReturnAttribute(
                description = "This will be the same as the type of the first input parameter.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                        DataType.STRING, DataType.BOOL, DataType.OBJECT}),
        examples = {
                @Example(
                        syntax = "from fooStream\n" +
                                "select coalesce('123', null, '789') as value\n" +
                                "insert into barStream;",
                        description = "This will returns first null value 123."),
                @Example(
                        syntax = "from fooStream\n" +
                                "select coalesce(null, 76, 567) as value\n" +
                                "insert into barStream;",
                        description = "This will returns first null value 76."),
                @Example(
                        syntax = "from fooStream\n" +
                                "select coalesce(null, null, null) as value\n" +
                                "insert into barStream;",
                        description = "This will returns null as there are no notnull values.")
        }
)
public class CoalesceFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length == 0) {
            throw new SiddhiAppValidationException("Coalesce must have at least one parameter");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            if (type != expressionExecutor.getReturnType()) {
                throw new SiddhiAppValidationException("Coalesce cannot have parameters with different type");
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
    public Map<String, Object> currentState() {
        //No states
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }
}
