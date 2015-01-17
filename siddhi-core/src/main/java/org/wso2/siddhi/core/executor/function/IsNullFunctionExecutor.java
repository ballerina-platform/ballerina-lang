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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public class IsNullFunctionExecutor extends FunctionExecutor {

    Attribute.Type returnType = Attribute.Type.BOOL;

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public ExpressionExecutor cloneExecutor() {
        return this;
    }


    @Override
    public void init(List<ExpressionExecutor> attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
    }

    protected Object execute(Object[] obj) {
        for (Object aObj : obj) {
            if (aObj == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Object execute(Object data) {
        return data == null;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }
}
