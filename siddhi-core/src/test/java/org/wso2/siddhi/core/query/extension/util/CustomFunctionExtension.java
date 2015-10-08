/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.extension.util;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CustomFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType;

    @Override
    public void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            Attribute.Type attributeType = expressionExecutor.getReturnType();
            if (attributeType == Attribute.Type.DOUBLE) {
                returnType = attributeType;

            } else if ((attributeType == Attribute.Type.STRING) || (attributeType == Attribute.Type.BOOL)) {
                throw new ExecutionPlanCreationException("Plus cannot have parameters with types String or Bool");
            } else {
                returnType = Attribute.Type.LONG;
            }
        }

    }

    /**
     * Return type of the custom function mentioned
     *
     * @return return type
     */
    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    @Override
    protected Object execute(Object[] obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
            for (Object aObj : obj) {
                total += Double.parseDouble(String.valueOf(aObj));
            }

            return total;
        } else {
            long total = 0;
            for (Object aObj : obj) {
                total += Long.parseLong(String.valueOf(aObj));
            }
            return total;
        }

    }

    @Override
    protected Object execute(Object obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Double.parseDouble(String.valueOf(aObj));
                }
            }
            return total;
        } else {
            long total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Long.parseLong(String.valueOf(aObj));
                }
            }
            return total;
        }
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
    public Object[] currentState() {
        //No state
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
        //Nothing to be done
    }
}