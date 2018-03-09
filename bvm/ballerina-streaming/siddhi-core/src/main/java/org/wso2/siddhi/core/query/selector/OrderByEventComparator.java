/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.query.selector;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.MetaComplexEvent;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.parser.ExpressionParser;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OrderByAttribute;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Comparator class to arrange complex events based on hashcode.
 */
public class OrderByEventComparator implements Comparator<ComplexEvent> {

    private VariableExpressionExecutor[] groupByExecutors = null;
    private boolean[] isAscendingArray = null;

    public OrderByEventComparator(List<OrderByAttribute> groupByList,
                                  MetaComplexEvent metaComplexEvent,
                                  int currentState, Map<String, Table> tableMap,
                                  List<VariableExpressionExecutor> executors,
                                  SiddhiAppContext siddhiContext, String queryName) {
        if (!groupByList.isEmpty()) {
            groupByExecutors = new VariableExpressionExecutor[groupByList.size()];
            isAscendingArray = new boolean[groupByList.size()];
            for (int i = 0, expressionsSize = groupByList.size(); i < expressionsSize; i++) {
                OrderByAttribute orderByAttribute = groupByList.get(i);
                groupByExecutors[i] = (VariableExpressionExecutor) ExpressionParser.parseExpression(
                        orderByAttribute.getVariable(), metaComplexEvent, currentState, tableMap, executors,
                        siddhiContext, false, 0, queryName);
                isAscendingArray[i] = OrderByAttribute.Order.DESC != orderByAttribute.getOrder();
            }
        }
    }

    @Override
    public int compare(ComplexEvent complexEvent1, ComplexEvent complexEvent2) {
        if (groupByExecutors != null) {
            for (int i = 0, groupByExecutorsLength = groupByExecutors.length; i < groupByExecutorsLength; i++) {
                ExpressionExecutor executor = groupByExecutors[i];
                boolean isAscending = isAscendingArray[i];
                Object value1 = executor.execute(complexEvent1);
                Object value2 = executor.execute(complexEvent2);
                if (value1 != null && value2 != null) {
                    int results = 0;
                    Attribute.Type type = executor.getReturnType();
                    switch (type) {
                        case STRING:
                            results = ((String) value1).compareTo(((String) value2));
                            break;
                        case INT:
                            results = ((Integer) value1).compareTo(((Integer) value2));
                            break;
                        case LONG:
                            results = ((Long) value1).compareTo(((Long) value2));
                            break;
                        case FLOAT:
                            results = ((Float) value1).compareTo(((Float) value2));
                            break;
                        case DOUBLE:
                            results = ((Double) value1).compareTo(((Double) value2));
                            break;
                        case BOOL:
                            results = ((Boolean) value1).compareTo(((Boolean) value2));
                            break;
                        case OBJECT:
                            int hashDiff = value1.hashCode() - value2.hashCode();
                            if (hashDiff < 0) {
                                results = -1;
                            } else if (hashDiff > 0) {
                                results = 1;
                            }
                    }
                    if (!isAscending) {
                        results = results * -1;
                    }
                    if (results != 0) {
                        return results;
                    }
                } else if (value1 != null) {
                    return -1;
                } else if (value2 != null) {
                    return 1;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }
}
