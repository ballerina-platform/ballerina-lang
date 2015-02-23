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
 * replace_all(string, target, replacement)
 * Replaces each substring of this string that matches the given expression with the given replacement.
 * Accept Type(s): (STRING,STRING,STRING)
 * Return Type(s): STRING
 */
public class ReplaceAllFunctionExtension extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.STRING;

    //state-variables
    boolean isTargetConstant = false;
    boolean isReplacementConstant = false;
    String target;
    String replacement;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 3) {
            throw new ExecutionPlanCreationException(
                    "str:replace_all function can have only three parameters");
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING
                || attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING
                || attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanCreationException(
                    "str:replace_all function should take the form string:replace_all(<string attribute>, <string target>, <string replacement>)");
        }
        if (attributeExpressionExecutors[1].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
            isTargetConstant = true;
            target = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
        }
        if (attributeExpressionExecutors[2].getClass().toString().equals("class org.wso2.siddhi.core.executor.ConstantExpressionExecutor")) {
            isReplacementConstant = true;
            replacement = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue();
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null || data[1] == null || data[2] == null) {
            throw new OperationNotSupportedException("Input to str:replace_all() function cannot be null");
        }
        String source = (String) data[0];
        if (!isTargetConstant) {
            target = (String) data[1];
        }
        if (!isReplacementConstant) {
            replacement = (String) data[2];
        }
        return source.replaceAll(target, replacement);
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the replace_all function take 3 parameters, this method does not get called. Hence, not implemented.
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
        return new Object[]{isTargetConstant, isReplacementConstant, target, replacement};
    }

    @Override
    public void restoreState(Object[] state) {
        isTargetConstant = (Boolean) state[0];
        isReplacementConstant = (Boolean) state[1];
        target = (String) state[2];
        replacement = (String) state[3];
    }
}
