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
package org.wso2.siddhi.query.api;


import org.junit.Test;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class TableQueryTestCase {

    @Test
    public void testCreatingDeleteQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(Expression.value(100),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.deleteBy("StockQuote", Expression.compare(
                Expression.variable("symbol"),
                Compare.Operator.EQUAL,
                Expression.variable("symbol").ofStream("StockQuote")));

    }


    @Test
    public void testCreatingDeleteByTypeQuery() {
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
        query.deleteBy("StockQuote", OutputStream.OutputEventType.ALL_EVENTS,
                Expression.compare(
                        Expression.variable("symbol"),
                        Compare.Operator.EQUAL,
                        Expression.variable("symbol").ofStream("StockQuote"))
        );

    }

    @Test
    public void testCreatingUpdateByQuery() {
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
                        select("price", Expression.variable("price"))
        );
        query.updateBy("StockQuote", Expression.compare(
                Expression.variable("symbol"),
                Compare.Operator.EQUAL,
                Expression.variable("symbol").ofStream("StockQuote")));
    }

    @Test
    public void testCreatingUpdateByTypeQuery() {
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
                        select("price", Expression.variable("price"))
        );
        query.updateBy("StockQuote", OutputStream.OutputEventType.ALL_EVENTS, Expression.compare(
                Expression.variable("symbol"),
                Compare.Operator.EQUAL,
                Expression.variable("symbol").ofStream("StockQuote")));

    }


    @Test
    public void testCreatingInQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("cseEventStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.in(
                                                Expression.compare(Expression.value(9.5),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.variable("price")),
                                                "table"
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
        query.insertInto("StockQuote");

    }

}
