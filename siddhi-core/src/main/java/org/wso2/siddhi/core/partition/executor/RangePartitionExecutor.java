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

package org.wso2.siddhi.core.partition.executor;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;

/**
 * Range partition executor will compute whether a given {@link ComplexEvent} belongs to a pre-defined range and
 * returns respective range key.
 */
public class RangePartitionExecutor implements PartitionExecutor {

    private ConditionExpressionExecutor conditionExecutor;
    private String key;

    public RangePartitionExecutor(ConditionExpressionExecutor conditionExecutor, String key) {
        this.conditionExecutor = conditionExecutor;
        this.key = key;
    }

    public String execute(ComplexEvent event) {
        if (conditionExecutor.execute(event)) {
            return key;
        }
        return null;
    }

}
