/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.executor.function;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Map;

@Extension(
        name = "default",
        namespace = "",
        description = "Checks if the 'attribute' parameter is null and if so returns the value of the 'default' "
                + "parameter",
        parameters = {
                @Parameter(name = "attribute",
                           description = "The attribute that could be null.",
                           type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                   DataType.STRING, DataType.BOOL, DataType.OBJECT}),
                @Parameter(name = "default",
                           description = "The default value that will be used when 'attribute' parameter is null",
                           type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                                   DataType.STRING, DataType.BOOL, DataType.OBJECT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returned type will be same as the 'attribute' and 'default' type.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT,
                        DataType.STRING, DataType.BOOL, DataType.OBJECT})
)
public class defaultFunctionExecutor extends FunctionExecutor {
    private static final Logger log = Logger.getLogger(defaultFunctionExecutor.class);
    Attribute.Type returnType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ConfigReader configReader, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            // check whether all the arguments passed
            throw new ExecutionPlanValidationException("Invalid no of parameters passed to default() function, " +
                                                               "it require only 2 (attribute, default value) , "
                                                               + "but found "
                                                               + attributeExpressionExecutors.length);
        } else if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new ExecutionPlanValidationException("Invalid parameter passed to default() function, " +
                                                               "this only consumes constants, but found "
                                                               + attributeExpressionExecutors[1].getClass().getName());

        } else if ((attributeExpressionExecutors[0].getReturnType() != attributeExpressionExecutors[1]
                .getReturnType())) {
            throw new ExecutionPlanValidationException("Both attribute and default value parameters need to be of "
                                                               + "same return type but they are of " +
                                                               attributeExpressionExecutors[0].getReturnType() + "and" +
                                                               attributeExpressionExecutors[1].getReturnType());
        }
        returnType = attributeExpressionExecutors[0].getReturnType();
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null) {
            return data[1];
        } else {
            return data[0];
        }
    }

    @Override
    protected Object execute(Object data) {
        //this will not occur
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

}
