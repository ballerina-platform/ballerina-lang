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
package org.ballerinalang.siddhi.query.api;

import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.ratelimit.OutputRate;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.testng.annotations.Test;

/**
 * Test case to validate output rate limiting.
 */
public class OutputRateTestCase {

    @Test
    public void testCreatingQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(Expression.and(Expression.compare(Expression.add(Expression.value(7), Expression.value
                                        (9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.compare(Expression.value(100),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.variable("volume")
                                )
                                )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("symbol"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );

        query.output(OutputRate.perEvents(Expression.value(5)).output(OutputRate.Type.ALL));
        query.insertInto("StockQuote");

    }

    @Test
    public void testCreatingQuery1() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(Expression.and(Expression.compare(Expression.add(Expression.value(7), Expression.value
                                        (9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.compare(Expression.value(100),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.variable("volume")
                                )
                                )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("symbol"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );

        query.output(OutputRate.perTimePeriod(Expression.value(1000L)).output(OutputRate.Type.LAST));
        query.insertInto("StockQuote");

    }

    @Test
    public void testCreatingQuery2() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(Expression.and(Expression.compare(Expression.add(Expression.value(7), Expression.value
                                        (9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.compare(Expression.value(100),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.variable("volume")
                                )
                                )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("symbol"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );

        query.output(OutputRate.perTimePeriod(Expression.Time.minute(1)).output(OutputRate.Type.LAST));
        query.insertInto("StockQuote");

    }

    @Test
    public void testCreatingQuery3() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(Expression.and(Expression.compare(Expression.add(Expression.value(7), Expression.value
                                        (9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.compare(Expression.value(100),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.variable("volume")
                                )
                                )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("symbol"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );

        query.output(OutputRate.perSnapshot(Expression.Time.minute(1)));
        query.insertInto("StockQuote");

    }
}
