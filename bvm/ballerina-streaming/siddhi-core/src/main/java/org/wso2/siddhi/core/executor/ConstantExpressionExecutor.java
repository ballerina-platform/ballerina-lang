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

package org.wso2.siddhi.core.executor;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Executor class for Constant Siddhi expressions. Function execution logic is implemented in execute method.
 */
public class ConstantExpressionExecutor implements ExpressionExecutor {
    private Object value;
    private Attribute.Type type;

    public ConstantExpressionExecutor(Object value, Attribute.Type type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public Object execute(ComplexEvent event) {
        return value;
    }

    public Attribute.Type getReturnType() {
        return type;
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        return this;
    }

    public Object getValue() {
        return value;
    }
}
