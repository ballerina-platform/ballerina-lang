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
import org.ballerinalang.siddhi.query.api.execution.query.input.state.State;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.api.expression.constant.TimeConstant;
import org.testng.annotations.Test;

public class PatternQueryTestCase {


//    from e1=Stream1[price >= 30] -> e2=Stream1[ price >= 20] -> e3=Stream2[ price >= e1.price]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    group by e1.symbol
//    having avgPrice>50;
//    insert into OutputStream

    @Test
    public void testPatternQuery1() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare(Expression
                                                .variable("price"),
                                        Compare.Operator.GREATER_THAN_EQUAL,
                                        Expression.value(30)))),
                                State.next(
                                        State.stream(InputStream.stream("e2", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(20)))),
                                        State.stream(InputStream.stream("e3", "Stream2").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.variable("price").ofStream("e1"))))
                                )
                        )
                )

        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2"))).
                        groupBy(Expression.variable("symbol").ofStream("e1")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN,
                                Expression.value(50)))


        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ price >= 20] -> e3=Stream2[ price >= e1.price]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    group by e1.symbol
//    having avgPrice>50;
//    insert into OutputStream

    @Test
    public void testPatternQuery2() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.stream(InputStream.stream("e2", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(20)))),
                                        State.stream(InputStream.stream("e3", "Stream2").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.variable("price").ofStream("e1"))))
                                )
                        )
                )

        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2"))).
                        groupBy(Expression.variable("symbol").ofStream("e1")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN,
                                Expression.value(50)))


        );
        query.insertInto("OutputStream");
    }

//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ price >= 20] -> every (e3=Stream2[ price >= e1.price]) ->
// e4=Stream3[price>74] -> e5= Stream4[symbol=='IBM']
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery3() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.stream(InputStream.stream("e2", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(20)))),
                                        State.next(
                                                State.every(
                                                        State.stream(InputStream.stream("e3", "Stream2").filter
                                                                (Expression.compare(Expression.variable("price"),
                                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                                        Expression.variable("price").ofStream("e1"))))),
                                                State.next(
                                                        State.stream(InputStream.stream("e4", "Stream3").filter
                                                                (Expression.compare(Expression.variable("price"),
                                                                        Compare.Operator.GREATER_THAN,
                                                                        Expression.value(74)))),
                                                        State.stream(InputStream.stream("e5", "Stream4").filter
                                                                (Expression.compare(Expression.variable("symbol"),
                                                                        Compare.Operator.EQUAL,
                                                                        Expression.value("IBM"))))

                                                )
                                        )
                                )
                        )
                )

        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30] -> e2=Stream1[ price >= 20]) -> e3=Stream2[ price >= e1.price] ->
// e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery4() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.next(
                                                State.stream(InputStream.stream("e1", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(30)))),
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))))),
                                State.next(
                                        State.stream(InputStream.stream("e3", "Stream2").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.variable("price").ofStream("e1")))),

                                        State.stream(InputStream.stream("e4", "Stream3").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.value(74)))))

                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");


    }


//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ price >= 20] and e3=Stream2[ price >= e1.price] ->
// e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery5() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.logicalAnd(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))))
                                        ,
                                        State.stream(InputStream.stream("e4", "Stream3").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.value(74)))))
                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ price >= 20] or e3=Stream2[ price >= e1.price] within 3 min
// -> e4=Stream3[price>74] within 2 min
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery6() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.logicalOr(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))),
                                                Expression.Time.minute(3))
                                        ,
                                        State.stream(InputStream.stream("e4", "Stream3").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.value(74)))))

                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ prev.price >= 20]<3:5> -> e3=Stream2[ price >= e1.price] ->
// e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery7() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.count(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price").ofStream("e2", Variable
                                                                        .LAST),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                3, 5),
                                        State.next(
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))),
                                                State.stream(InputStream.stream("e4", "Stream3").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN,
                                                                Expression.value(74)))))
                                )
                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }

//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ prev.price >= 20]<:5> -> e3=Stream2[ price >= e1.price] ->
// e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery8() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.countLessThanEqual(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price").ofStream("e2", Variable
                                                                        .LAST),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                5),
                                        State.next(
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))),
                                                State.stream(InputStream.stream("e4", "Stream3").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN,
                                                                Expression.value(74)))))
                                )
                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30]) -> e2=Stream1[ prev.price >= 20]<5:> within 3 min -> e3=Stream2[ price >=
// e1.price] -> e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery9() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.countMoreThanEqual(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price").ofStream("e2", Variable
                                                                        .LAST),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                5, Expression.Time.minute(3)),
                                        State.next(
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))),
                                                State.stream(InputStream.stream("e4", "Stream3").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN,
                                                                Expression.value(74)))))
                                )
                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }


//    from every (e1=Stream1[price >= 30]) -> (e2=Stream1[ prev.price >= 20] -> e3=Stream2[ price >= e1.price])
// within 4 min -> e4=Stream3[price>74]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream

    @Test
    public void testPatternQuery10() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.every(
                                        State.stream(InputStream.stream("e1", "Stream1").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(30))))),
                                State.next(
                                        State.next(
                                                State.stream(InputStream.stream("e2", "Stream1").filter(Expression
                                                        .compare(Expression.variable("price").ofStream("e2", Variable
                                                                        .LAST),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.value(20)))),
                                                State.stream(InputStream.stream("e3", "Stream2").filter(Expression
                                                        .compare(Expression.variable("price"),
                                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                                Expression.variable("price").ofStream("e1")))),
                                                Expression.Time.minute(4)),
                                        State.stream(InputStream.stream("e4", "Stream3").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN,
                                                        Expression.value(74))))
                                )
                        )
                )
        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2")))

        );
        query.insertInto("OutputStream");

    }

    //    from e1=Stream1[price >= 30] -> not Stream1[ price >= 20] for 1 sec -> e3=Stream2[ price >= e1.price]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    group by e1.symbol
//    having avgPrice>50;
//    insert into OutputStream
    @Test
    public void testPatternQuery11() {
        Query query = Query.query();
        query.from(
                InputStream.patternStream(
                        State.next(
                                State.stream(InputStream.stream("e1", "Stream1")
                                        .filter(Expression.compare(Expression.variable("price"),
                                                Compare.Operator.GREATER_THAN_EQUAL,
                                                Expression.value(30)))),
                                State.next(State.logicalNot(State.stream(InputStream.stream("Stream1")
                                                .filter(Expression.compare(Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.value(20)))), new TimeConstant(1000)),
                                        State.stream(InputStream.stream("e3", "Stream2").filter(Expression.compare
                                                (Expression.variable("price"),
                                                        Compare.Operator.GREATER_THAN_EQUAL,
                                                        Expression.variable("price").ofStream("e1")))))
                        )
                )

        );
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2"))).
                        groupBy(Expression.variable("symbol").ofStream("e1")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN,
                                Expression.value(50)))


        );
        query.insertInto("OutputStream");

    }

}
