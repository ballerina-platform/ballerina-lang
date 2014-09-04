/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public class CoalesceFunctionExecutor extends FunctionExecutor {

    Attribute.Type returnType;

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    @Override
    public void init(List<ExpressionExecutor> attributeExpressionExecutors, SiddhiContext siddhiContext) {
        Attribute.Type type = attributeExpressionExecutors.get(0).getReturnType();
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            if (type != expressionExecutor.getReturnType()) {
                throw new OperationNotSupportedException("Coalesce cannot have parameters with different type");
            }
        }
        returnType = type;
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

}
