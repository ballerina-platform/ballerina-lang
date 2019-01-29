/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.query.test;

import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.ballerinalang.siddhi.query.api.aggregation.Within;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase to validate defined aggregations.
 */
public class DefineAggregationTestCase {

    @Test
    public void test1() throws SiddhiParserException {

        AggregationDefinition aggregationDefinitionQuery = SiddhiCompiler
                .parseAggregationDefinition("define aggregation StockAggregation " + "from StockStream "
                        + "select StockStream.timestamp as timestamp, StockStream.symbol as symbol, "
                        + "       StockStream.price as price " + "   group by StockStream.symbol "
                        + "aggregate by timestamp " + "every seconds ... days ;");

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation")
                .from(InputStream.stream("StockStream"))
                .select(Selector.basicSelector()
                        .select("timestamp", Expression.variable("timestamp").ofStream("StockStream"))
                        .select("symbol", Expression.variable("symbol").ofStream("StockStream"))
                        .select("price", Expression.variable("price").ofStream("StockStream"))
                        .groupBy(Expression.variable("symbol").ofStream("StockStream")))
                .aggregateBy(Expression.variable("timestamp"))
                .every(TimePeriod.range(TimePeriod.Duration.SECONDS, TimePeriod.Duration.DAYS));

        AssertJUnit.assertEquals(aggregationDefinition, aggregationDefinitionQuery);
    }

    @Test
    public void test2() throws SiddhiParserException {

        AggregationDefinition aggregationDefinitionQuery = SiddhiCompiler
                .parseAggregationDefinition("define aggregation StockAggregationDefinition " + "from StockStream "
                        + "select StockStream.timestamp, StockStream.symbol as symbol, "
                        + "       StockStream.price as price " + "   group by StockStream.symbol "
                        + "aggregate by timestamp " + "every seconds, minutes, hours ;");

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregationDefinition")
                .from(InputStream.stream("StockStream"))
                .select(Selector.basicSelector().select(Expression.variable("timestamp").ofStream("StockStream"))
                        .select("symbol", Expression.variable("symbol").ofStream("StockStream"))
                        .select("price", Expression.variable("price").ofStream("StockStream"))
                        .groupBy(Expression.variable("symbol").ofStream("StockStream")))
                .aggregateBy(Expression.variable("timestamp")).every(TimePeriod.interval(TimePeriod.Duration.SECONDS,
                        TimePeriod.Duration.MINUTES, TimePeriod.Duration.HOURS));

        AssertJUnit.assertEquals(aggregationDefinition, aggregationDefinitionQuery);

    }

    @Test
    public void test3() throws SiddhiParserException {

        Query query = SiddhiCompiler
                .parseQuery("from barStream as b join cseEventAggregation as a " +
                        "on a.symbol == b.symbol " +
                        "within \"2014-02-15T00:00:00Z\", \"2014-03-16T00:00:00Z\" " +
                        "per \"day\" " +
                        "select a.symbol, a.total, a.avgPrice " +
                        "insert into fooBar;");

        Query queryApi = Query.query().
                from(InputStream.joinStream(
                        InputStream.stream("b", "barStream"),
                        JoinInputStream.Type.JOIN,
                        InputStream.stream("a", "cseEventAggregation"),
                        Expression.compare(
                                Expression.variable("symbol").ofStream("a"),
                                Compare.Operator.EQUAL,
                                Expression.variable("symbol").ofStream("b")),
                        Within.within(Expression.value("2014-02-15T00:00:00Z"),
                                Expression.value("2014-03-16T00:00:00Z")),
                        Expression.value("day")
                        )
                ).
                select(
                        Selector.selector().
                                select(Expression.variable("symbol").ofStream("a")).
                                select(Expression.variable("total").ofStream("a")).
                                select(Expression.variable("avgPrice").ofStream("a"))
                ).
                insertInto("fooBar");

        AssertJUnit.assertEquals(queryApi, query);

    }


}
