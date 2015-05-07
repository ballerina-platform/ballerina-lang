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

package org.wso2.siddhi.extension.string;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/*
* coalesce(arg1,arg2,...,argN)
* returns the value of the first of its input parameters that is not NULL
* Accept Type(s): Arguments can be of any type, given that the argument count is more than zero and all the arguments are of the same type.
* Return Type(s): Same type as the input
* */
public class CoalesceFunctionExtension extends FunctionExecutor{

    private Attribute.Type returnType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        int attributeCount = 0;
        if (attributeExpressionExecutors.length == 0) {
            throw new ExecutionPlanValidationException("str:coalesce() function requires at least one argument, but found only " +
                    attributeExpressionExecutors.length);
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            attributeCount++;
            if (type != expressionExecutor.getReturnType()) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the "+attributeCount+
                        "'th argument of str:coalesce() function, required "+type+", but found "
                        +attributeExpressionExecutors[attributeCount-1].getReturnType().toString());
            }
        }
        returnType = type;
    }

    @Override
    protected Object execute(Object[] data) {
        for (Object aData : data) {
            if (aData != null) {
                return aData;
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
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null; //No state
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}
