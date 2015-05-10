/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.extension.string;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/*
* concat(string1, string2, ..., stringN)
* Returns a string that is the result of concatenating two or more string values.
* Accept Type(s): STRING. There should be at least two arguments.
* Return Type(s): STRING
* */
public class ConcatFunctionExtension extends FunctionExecutor{

    private Attribute.Type returnType = Attribute.Type.STRING;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        int attributeCount = 0;
        if (attributeExpressionExecutors.length < 2) {
            throw new ExecutionPlanValidationException("str:charat() function requires at least two arguments, " +
                    "but found only " + attributeExpressionExecutors.length);
        }
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            attributeCount++;
            if (returnType != expressionExecutor.getReturnType()) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the "+attributeCount+"'th argument of str:concat() function, " +
                        "required "+Attribute.Type.STRING+", but found "+attributeExpressionExecutors[attributeCount-1].getReturnType().toString());
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        StringBuilder sb = new StringBuilder();
        for (Object aData : data) {
            if(aData != null){
                sb.append(aData);
            }
        }
        return sb.toString();
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
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;    //No states
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}
