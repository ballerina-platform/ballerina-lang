/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.stringfunctions;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CharAtFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType;
    boolean hasValidArguments = false;
    String exceptionMessage =
            "charat function should take the form str:charat(<string stream> , <int index>) or string:charat(<string stream> , <int index>, <boolean isVaryingIndex>";

    //state-variables
    int index = 0;
    boolean isFirst = true;
    boolean isVaryingIndex = false;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length < 2 || attributeExpressionExecutors.length > 3) {
            throw new ExecutionPlanCreationException(
                    "charat function can have only two parameters");
        }
        if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                returnType = Attribute.Type.STRING;
                isVaryingIndex = false;
                hasValidArguments = true;
            }
        } else if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT &&
                    attributeExpressionExecutors[2].getReturnType() == Attribute.Type.BOOL) {
                returnType = Attribute.Type.STRING;
                isVaryingIndex = true;
                hasValidArguments = true;
            }
        }
        if (!hasValidArguments) {
            throw new ExecutionPlanCreationException(exceptionMessage);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        try {
            String source = (String) data[0];
            if (isFirst) {
                if (isVaryingIndex) {
                    isVaryingIndex = (Boolean) data[2];
                } else {
                    isVaryingIndex = false;
                }
                isFirst = false;
                index = (Integer) data[1];
            }
            // validate the index value for the first time
            if (isVaryingIndex) {
                index = (Integer) data[1];
            }
            return source.charAt(index);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new ExecutionPlanRuntimeException("Index value must be within the string length");
        }
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the charAt function takes in 2 parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{index, isFirst, isVaryingIndex};
    }

    @Override
    public void restoreState(Object[] state) {
        index = (Integer)state[0];
        isFirst = (Boolean)state[1];
        isVaryingIndex = (Boolean)state[2];
    }
}


