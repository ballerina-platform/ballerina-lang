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
package org.wso2.siddhi.core.query.selector;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.List;
import java.util.Map;

/**
 * Class to generate keys for GroupBy groups
 */
public class GroupByKeyGenerator {

    private VariableExpressionExecutor[] groupByExecutors = null;

    public GroupByKeyGenerator(List<Variable> groupByList,
                               MetaComplexEvent metaComplexEvent,
                               Map<String, Table> tableMap,
                               List<VariableExpressionExecutor> executors,
                               SiddhiAppContext siddhiContext, String queryName) {
        if (!groupByList.isEmpty()) {
            groupByExecutors = new VariableExpressionExecutor[groupByList.size()];
            for (int i = 0, expressionsSize = groupByList.size(); i < expressionsSize; i++) {
                groupByExecutors[i] = (VariableExpressionExecutor) ExpressionParser.parseExpression(
                        groupByList.get(i), metaComplexEvent, SiddhiConstants.UNKNOWN_STATE, tableMap, executors,
                        siddhiContext, false, 0, queryName);
            }
        }
    }

    /**
     * generate groupBy key of a streamEvent
     *
     * @param event complexEvent
     * @return GroupByKey
     */
    public String constructEventKey(ComplexEvent event) {
        if (groupByExecutors != null) {
            StringBuilder sb = new StringBuilder();
            for (ExpressionExecutor executor : groupByExecutors) {
                sb.append(executor.execute(event)).append("::");
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
