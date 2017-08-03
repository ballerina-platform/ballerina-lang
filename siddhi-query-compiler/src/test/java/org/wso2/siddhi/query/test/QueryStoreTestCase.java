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

package org.wso2.siddhi.query.test;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.api.execution.query.input.store.InputStore;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class QueryStoreTestCase {

    @Test
    public void test1() throws SiddhiParserException {
        StoreQuery query = SiddhiCompiler.parseStoreQuery("" +
                "from StockTable " +
                "on price>3 " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having (price >= 20) ;"
        );
        Assert.assertNotNull(query);

        StoreQuery api = StoreQuery.query().
                from(
                        InputStore.store("StockTable").
                                on(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.value(3)))).
                select(
                        Selector.selector().
                                select(Expression.variable("symbol")).
                                select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.variable("price"),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.value(20))
                                )
                );

        Assert.assertEquals(api, query);


    }

    @Test
    public void test2() {

        StoreQuery query = SiddhiCompiler.parseStoreQuery("" +
                "from StockTable " +
                "select symbol, price " +
                "group by symbol " +
                "having (7 > price) ;"
        );
        Assert.assertNotNull(query);


        StoreQuery api = StoreQuery.query().
                from(
                        InputStore.store("StockTable")).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol")).
                                select(Expression.variable("price")).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.value(7),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );

        Assert.assertEquals(api, query);
    }

    @Test
    public void test3() {
        StoreQuery query = SiddhiCompiler.parseStoreQuery("" +
                "from StockTable " +
                "on price > 40 " +
                "select symbol, price " +
                "group by symbol " +
                "having (7 > price) ;"
        );
        Assert.assertNotNull(query);

        StoreQuery api = StoreQuery.query().
                from(
                        InputStore.store("StockTable").
                                on(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN, Expression.value(40)))).
                select(
                        Selector.selector().
                                select("symbol", Expression.variable("symbol")).
                                select(Expression.variable("price")).
                                groupBy(Expression.variable("symbol")).
                                having(
                                        Expression.compare(
                                                Expression.value(7),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );
        Assert.assertEquals(api, query);

    }


    @Test
    public void test4() {
        StoreQuery query = SiddhiCompiler.parseStoreQuery("" +
                "from StockTable " +
                "on price > 40 " +
                "within '2017/01/*' " +
                "per 'day' " +
                "select symbol, price " +
                "group by symbol " +
                "having (7 > price) ;"
        );
        Assert.assertNotNull(query);

        StoreQuery api = StoreQuery.query().
                from(
                        InputStore.store("StockTable").
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
                                                Expression.value(7),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price"))
                                )
                );
        Assert.assertEquals(api, query);

    }

}


