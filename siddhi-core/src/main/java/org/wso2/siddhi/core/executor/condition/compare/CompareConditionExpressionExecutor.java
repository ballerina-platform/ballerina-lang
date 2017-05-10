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
package org.wso2.siddhi.core.executor.condition.compare;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;

/**
 * Parent Executor class for Compare conditions. common evaluation logic is implemented within executor.
 */
public abstract class CompareConditionExpressionExecutor extends ConditionExpressionExecutor {

    protected ExpressionExecutor leftExpressionExecutor;
    protected ExpressionExecutor rightExpressionExecutor;

    public CompareConditionExpressionExecutor(ExpressionExecutor leftExpressionExecutor,
                                              ExpressionExecutor rightExpressionExecutor) {
        this.leftExpressionExecutor = leftExpressionExecutor;
        this.rightExpressionExecutor = rightExpressionExecutor;
    }


    public Boolean execute(ComplexEvent event) {
        Object left = leftExpressionExecutor.execute(event);
        Object right = rightExpressionExecutor.execute(event);
        return !(left == null || right == null) && execute(left, right);
    }

    protected abstract Boolean execute(Object left, Object right);

}
