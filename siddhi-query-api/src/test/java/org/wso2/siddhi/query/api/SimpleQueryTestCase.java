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
import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

public class SimpleQueryTestCase {

//    from StockStream[ 7+9.5>price AND 100>=volume]
//    insert into OutStockStream symbol, avg(price) as avgPrice
//    group by symbol
//    having avgPrice>50

    @Test
    public void testCreatingFilterQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(
                                                Expression.value(100),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        )
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
        query.insertInto("OutStockStream");

        SiddhiApp.siddhiApp("test").addQuery(query);

    }

    @Test
    public void testQuery1() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(
                                                Expression.value(100),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        ).window("length", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        query.insertInto("OutStockStream");

    }

    @Test
    public void testQuery2() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(Expression.and(
                                Expression.compare(
                                        Expression.add(Expression.value(7), Expression.value(9.5)),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price")),
                                Expression.compare(
                                        Expression.value(100),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.variable("volume")
                                )
                        )).
                        window("length", Expression.value(50)).
                        filter(
                                Expression.compare(
                                        Expression.variable("symbol"),
                                        Compare.Operator.EQUAL,
                                        Expression.value("WSO2")
                                )
                        )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        query.insertInto("OutStockStream");

    }

    @Test
    public void testQuery3() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(
                                                Expression.value(100),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        ).
                        function("bar", Expression.value("price")).
                        window("length", Expression.value(50)).
                        function("foo", Expression.value(67), Expression.value(89)).
                        filter(
                                Expression.compare(
                                        Expression.value(10),
                                        Compare.Operator.LESS_THAN_EQUAL,
                                        Expression.variable("price")
                                )
                        )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        query.insertInto("OutStockStream");

    }

    @Test(expected = DuplicateAttributeException.class)
    public void testCreatingFilterQueryWithDuplicateOutputAttribute() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
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
                        select("price", Expression.function("avg", Expression.variable("price"))).
                        select("price", Expression.variable("price")).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        query.insertInto("OutStockStream");

    }

//    from (from StockStream[win.length(50)][ price >= 20]
//            return symbol, avg(price) as avgPrice
//    group by symbol) [symbol=="IBM"]
//    insert into IBMOutStockStream symbol, avgPrice

    @Test
    public void testCreatingNestedFilterQuery() {
        Query query = Query.query();
        query.from(InputStream.stream(
                Query.query().
                        from(InputStream.stream("StockStream").
                                filter(
                                        Expression.compare(
                                                Expression.variable("price").ofStream("StockStream"),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.value(20))
                                ).filter(Expression.isNull(Expression.variable("price").ofStream("StockStream")))).
                        select(
                                Selector.selector().
                                        select("symbol", Expression.variable("symbol")).
                                        select("avgPrice", Expression.function("avg", Expression.variable("price")))
                        ).
                        returns())
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.variable("avgPrice"))
        );
        query.insertInto("IBMOutStockStream");
    }

//    from StockStream[win.lengthBatch(50)][price >= 20]
//    return symbol, avg(price) as avgPrice

    @Test
    public void testCreatingReturnFilterQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
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
                        select("avgPrice", Expression.function("avg", Expression.variable("price")))
        );
        query.returns();

    }


    @Test
    public void testCreatingReturnFilterQueryWithExtension() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(Expression.and(Expression.compare(Expression.function("ext", "FooBarCond", Expression
                                        .value(7), Expression.value(9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.function("ext", "BarCond", Expression.value(100),
                                        Expression.variable("volume")
                                )
                                )
                        ).function("ext", "Foo", Expression.value(67), Expression.value(89)).window("ext",
                        "lengthFirst10", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("ext", "avg", Expression.variable("price")))
        );

    }

    @Test
    public void testCreatingReturnFilterQueryWithFunction() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(Expression.and(Expression.compare(Expression.function("FooBarCond", Expression.value
                                        (7), Expression.value(9.5)),
                                Compare.Operator.GREATER_THAN,
                                Expression.variable("price")),
                                Expression.function("BarCond", Expression.value(100),
                                        Expression.variable("volume")
                                )
                                )
                        ).function("ext", "Foo", Expression.value(67), Expression.value(89)).window("ext",
                        "lengthFirst10", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("ext", "avg", Expression.variable("symbol")))
        );

    }


}
