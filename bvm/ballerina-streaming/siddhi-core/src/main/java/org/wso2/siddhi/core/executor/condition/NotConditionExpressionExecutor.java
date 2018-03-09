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

package org.wso2.siddhi.core.executor.condition;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Executor class for Not condition. Condition evaluation logic is implemented within executor.
 */
public class NotConditionExpressionExecutor extends ConditionExpressionExecutor {

    private ExpressionExecutor conditionExecutor;

    public NotConditionExpressionExecutor(ExpressionExecutor conditionExecutor) {
        if (conditionExecutor.getReturnType().equals(Attribute.Type.BOOL)) {
            this.conditionExecutor = conditionExecutor;
        } else {
            throw new OperationNotSupportedException("Return type of condition executor " + conditionExecutor
                    .toString() + " should be of type BOOL. Actual Type: " + conditionExecutor.getReturnType()
                    .toString());
        }
    }

    public Boolean execute(ComplexEvent event) {
        Boolean result = (Boolean) conditionExecutor.execute(event);
        if (result == Boolean.TRUE) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        return new NotConditionExpressionExecutor(conditionExecutor.cloneExecutor(key));
    }


}
