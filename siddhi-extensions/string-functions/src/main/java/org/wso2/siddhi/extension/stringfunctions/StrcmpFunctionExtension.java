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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * strcmp(string, compareTo)
 * Compares two strings lexicographically.
 * Accept Type(s): (STRING,STRING)
 * Return Type(s): INT
 */
public class StrcmpFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.INT;

    //state-variables
    boolean isCompareToConstant = false;
    String compareTo = null;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanCreationException(
                    "strcmp function can have only two parameters");
        }
        if (!(attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING
                && attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING)) {
            throw new ExecutionPlanCreationException(
                    "strcmp function should take the form string:strcmp(<string attribute> , <string compareWith>)");
        }
        if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
            isCompareToConstant = true;
            compareTo = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null || data[1] == null) {
            throw new OperationNotSupportedException("Input to str:strcmp() function cannot be null");
        }
        String source = (String) data[0];
        if (!isCompareToConstant) {
            compareTo = (String) data[1];
        }
        return source.compareTo(compareTo);
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the strcmp function takes in 2 parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {
        //Nothing to start.
    }

    @Override
    public void stop() {
        //Nothing to stop.
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{isCompareToConstant, compareTo};
    }

    @Override
    public void restoreState(Object[] state) {
        isCompareToConstant = (Boolean) state[0];
        compareTo = (String) state[1];
    }
}
