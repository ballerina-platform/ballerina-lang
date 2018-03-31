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

package org.ballerinalang.siddhi.query.test;

import org.ballerinalang.siddhi.query.api.exception.DuplicateAttributeException;
import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OrderByAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SimpleQueryTestCase {

    @Test
    public void test1() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[price>3]#window.length(50) " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having (price >= 20) " +
                "insert all events into StockQuote; "
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query().from(InputStream.stream("StockStream").
                filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN, Expression
                        .value(3))).
                window("length", Expression.value(50))).
                select(Selector.selector().select(Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(
                                Expression.variable("price"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(20)))).
                insertInto("StockQuote", OutputStream.OutputEventType.ALL_EVENTS);
        AssertJUnit.assertEquals(api, query);

    }

    @Test
    public void test2() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  StockStream [price >= 20]#window.lengthBatch(50) " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having avgPrice>50 " +
                "insert into StockQuote; "
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query().from(InputStream.stream("StockStream").
                filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN_EQUAL,
                        Expression.value(20))).
                window("lengthBatch", Expression.value(50))).
                select(Selector.selector().
                        select(Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(
                                Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN,
                                Expression.value(50)))).
                insertInto("StockQuote");
        AssertJUnit.assertEquals(api, query);
    }

    @Test
    public void test3() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from AllStockQuotes#window.time(10 min)\n" +
                "select symbol as symbol, price, avg(price) as averagePrice \n" +
                "group by symbol \n" +
                "having ( price > ( averagePrice*1.02) ) or ( averagePrice > price ) " +
                "insert into FastMovingStockQuotes \n;");

        Query api = Query.query().from(InputStream.stream("AllStockQuotes").
                window("time", Expression.Time.minute(10))).
                select(Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select(Expression.variable("price")).
                        select("averagePrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.or(
                                Expression.compare(
                                        Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.multiply(Expression.variable("averagePrice"), Expression.value
                                                (1.02))),
                                Expression.compare(
                                        Expression.variable("averagePrice"),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price"))))).
                insertInto("FastMovingStockQuotes");
        AssertJUnit.assertEquals(api, query);
    }

    @Test
    public void test4() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  AllStockQuotes#window.lenghtBatch(50)  " +
                "select symbol, avg(price) as avgPrice " +
                "return ;"
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query().from(InputStream.stream("AllStockQuotes").
                window("lenghtBatch", Expression.value(50))).
                select(Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price")))).
                returns();
        AssertJUnit.assertEquals(api, query);
    }


    @Test
    public void test5() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  AllStockQuotes[price==Foo.price and Foo.try<5]  " +
                "select symbol, avg(price) as avgPrice " +
                "return ;"
        );
        AssertJUnit.assertNotNull(query);
        Query api = Query.query().from(InputStream.stream("AllStockQuotes").
                filter(Expression.and(Expression.compare(Expression.variable("price"), Compare.Operator.EQUAL,
                        Expression.variable("price").ofStream("Foo")), Expression.compare(Expression.variable("try")
                        .ofStream("Foo"), Compare.Operator.LESS_THAN, Expression.value(5))))).
                select(Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))));
        AssertJUnit.assertEquals(api, query);
    }

//    from StockStream[ 7+9.5>price AND 100>=volume]
//    insert into OutStockStream symbol, avg(price) as avgPrice
//    group by symbol
//    having avgPrice>50

    @Test
    public void test6() {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[7+9.5 > price and 100 >= volume]  " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having avgPrice>= 50 " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query();
        api.from(
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
        api.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        api.insertInto("OutStockStream");
        AssertJUnit.assertEquals(api, query);

    }

    @Test
    public void test7() {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[7+9.5 < price or 100 <= volume]#window.length(50) " +
                " " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having avgPrice!= 50 " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query();
        api.from(
                InputStream.stream("StockStream").
                        filter(
                                Expression.or(
                                        Expression.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.LESS_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(
                                                Expression.value(100),
                                                Compare.Operator.LESS_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        ).window("length", Expression.value(50))
        );
        api.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.NOT_EQUAL,
                                Expression.value(50)
                        ))
        );
        api.insertInto("OutStockStream");
        AssertJUnit.assertEquals(api, query);

    }

    @Test
    public void test8() {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[7-9.5 > price and 100 >= volume]#window.length(50)" +
                "#[symbol=='WSO2']  " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having avgPrice  >= 50 " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query();
        api.from(
                InputStream.stream("StockStream").
                        filter(Expression.and(
                                Expression.compare(
                                        Expression.subtract(Expression.value(7), Expression.value(9.5)),
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
        api.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        api.insertInto("OutStockStream");

        AssertJUnit.assertEquals(api, query);
    }

    @Test
    public void test9() {

        Query query = SiddhiCompiler.parseQuery("from  StockStream[7-9.5 > price and 100 >= volume]#bar(price)#window" +
                ".length(50)#foo(67,89)#[10<=price]  " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having avgPrice  >= 50 " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query();
        api.from(
                InputStream.stream("StockStream").
                        filter(
                                Expression.and(
                                        Expression.compare(
                                                Expression.subtract(Expression.value(7), Expression.value(9.5)),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Expression.compare(
                                                Expression.value(100),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.variable("volume")
                                        )
                                )
                        ).
                        function("bar", Expression.variable("price")).
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
        api.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(50)
                        ))
        );
        api.insertInto("OutStockStream");

        AssertJUnit.assertEquals(api, query);
    }

    @Test(expectedExceptions = DuplicateAttributeException.class)
    public void testCreatingFilterQueryWithDuplicateOutputAttribute() {
        SiddhiCompiler.parseQuery("from  StockStream[7*9.5 > price and 100 >= volume]#window.length(50) " +
                "select symbol, avg(price) as price, price as price " +
                "group by symbol " +
                "having avgPrice  >= 50 " +
                "insert into OutStockStream ;"
        );

    }

//    from (from StockStream[win.length(50)][ price >= 20]
//            return symbol, avg(price) as avgPrice
//    group by symbol) [symbol=="IBM"]
//    insert into IBMOutStockStream symbol, avgPrice

    @Test
    public void testCreatingNestedFilterQuery() {

        Query query1 = SiddhiCompiler.parseQuery("from  from StockStream[StockStream.price >= 20][ StockStream.price " +
                "is null]  " +
                "select symbol, avg(price) as avgPrice " +
                "return " +
                "select symbol, avgPrice " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query1);

        Query query2 = SiddhiCompiler.parseQuery("from  ( from StockStream[StockStream.price >= 20][ StockStream" +
                ".price is null]  " +
                "select symbol, avg(price) as avgPrice " +
                "return ) " +
                "select symbol, avgPrice " +
                "insert into OutStockStream ;"
        );
        AssertJUnit.assertNotNull(query2);

        Query api = Query.query();
        api.from(InputStream.stream(
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
        api.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", Expression.variable("avgPrice"))
        );
        api.insertInto("OutStockStream");
        AssertJUnit.assertEquals(api, query1);
        AssertJUnit.assertEquals(api, query2);
    }

//    from StockStream[win.lengthBatch(50)][price >= 20]
//    return symbol, avg(price) as avgPrice

    @Test
    public void testCreatingReturnFilterQuery() {
        Query query = Query.query();
        query.from(
                InputStream.stream("StockStream").
                        filter(Expression.and(Expression.compare(Expression.divide(Expression.value(7), Expression
                                        .value(9.5)),
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

    @Test
    public void test10() {
        Query queryString = SiddhiCompiler.parseQuery("" +
                "from  StockStream[7+9.5 > price and 100 >= volume]#window.length(50) " +
                " " +
                "select symbol as symbol, price as price, volume as volume  " +
                "update StockQuote \n " +
                "   set StockQuote.price = price, \n " +
                "    StockQuote.volume = volume " +
                " on symbol==StockQuote.symbol ;"
        );
        AssertJUnit.assertNotNull(queryString);

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
                        ).window("length", Expression.value(50))
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.updateBy("StockQuote", OutputStream.OutputEventType.CURRENT_EVENTS,
                UpdateStream.updateSet().
                        set(
                                Expression.variable("price").ofStream("StockQuote"),
                                Expression.variable("price")).
                        set(
                                Expression.variable("volume").ofStream("StockQuote"),
                                Expression.variable("volume")),
                Expression.compare(
                        Expression.variable("symbol"),
                        Compare.Operator.EQUAL,
                        Expression.variable("symbol").ofStream("StockQuote")));

        AssertJUnit.assertEquals(query, queryString);

    }

    @Test
    public void test11() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[price>3]#window.length(50) " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having (price >= 20)" +
                "order by avgPrice " +
                "limit 5 " +
                "insert all events into StockQuote; "
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query().from(InputStream.stream("StockStream").
                filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN, Expression
                        .value(3))).
                window("length", Expression.value(50))).
                select(Selector.selector().select(Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(
                                Expression.variable("price"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(20))).
                        orderBy(Expression.variable("avgPrice")).
                        limit(Expression.value(5))).
                insertInto("StockQuote", OutputStream.OutputEventType.ALL_EVENTS);
        AssertJUnit.assertEquals(api, query);

    }

    @Test
    public void test12() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from  StockStream[price>3]#window.length(50) " +
                "select symbol, avg(price) as avgPrice " +
                "group by symbol " +
                "having (price >= 20)" +
                "order by avgPrice desc " +
                "limit 5 " +
                "insert all events into StockQuote; "
        );
        AssertJUnit.assertNotNull(query);

        Query api = Query.query().from(InputStream.stream("StockStream").
                filter(Expression.compare(Expression.variable("price"), Compare.Operator.GREATER_THAN, Expression
                        .value(3))).
                window("length", Expression.value(50))).
                select(Selector.selector().select(Expression.variable("symbol")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol")).
                        having(Expression.compare(
                                Expression.variable("price"),
                                Compare.Operator.GREATER_THAN_EQUAL,
                                Expression.value(20))).
                        orderBy(Expression.variable("avgPrice"), OrderByAttribute.Order.DESC).
                        limit(Expression.value(5))).
                insertInto("StockQuote", OutputStream.OutputEventType.ALL_EVENTS);
        AssertJUnit.assertEquals(api, query);

    }


}


