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
package org.ballerinalang.siddhi.query.api;


import org.ballerinalang.siddhi.query.api.aggregation.Within;
import org.ballerinalang.siddhi.query.api.execution.query.StoreQuery;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.InputStore;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.testng.annotations.Test;

public class StoreQueryTestCase {

    @Test
    public void test1() {
        StoreQuery.query().
                from(
                        InputStore.store("cseEventTable")).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol")).
                                select(Expression.variable("price")).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );
    }

    @Test
    public void test2() {
        StoreQuery.query().
                from(
                        InputStore.store("cseEventTable").
                                on(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN, Expression.value(40)))).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol")).
                                select(Expression.variable("price")).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );
    }


    @Test
    public void test3() {
        StoreQuery.query().
                from(
                        InputStore.store("cseEventTable").
                                on(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN, Expression.value(40)),
                                        Within.within(Expression.value("2017/01/*")), Expression.value("day"))).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol")).
                                select(Expression.variable("price")).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );
    }
}
