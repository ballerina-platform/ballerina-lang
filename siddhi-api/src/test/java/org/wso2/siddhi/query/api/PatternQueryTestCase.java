/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.query.api;


import org.junit.Test;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.pattern.Pattern;
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;

public class PatternQueryTestCase {

//    from e1=Stream1[price >= 20] -> e2=Stream2[ price >= e1.price]
//    insert into OutStream e1.symbol, avg(e2.price ) as avgPrice
//    group by e1.symbol
//    having avgPrice>50;

    @Test
    public void testCreatingFilterPatternQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                QueryFactory.inputStream("e1", "Stream1").filter(Condition.compare(Expression.variable("price"),
                                                                                                    Condition.Operator.GREATER_THAN_EQUAL,
                                                                                                    Expression.value(30))),
                                Pattern.followedBy(QueryFactory.inputStream("e2", "Stream1").filter(Condition.compare(Expression.variable("price"),
                                                                                                                       Condition.Operator.GREATER_THAN_EQUAL,
                                                                                                                       Expression.value(20))),
                                                   QueryFactory.inputStream("e3", "Stream2").filter(Condition.compare(Expression.variable("price"),
                                                                                                                       Condition.Operator.GREATER_THAN_EQUAL,
                                                                                                                       Expression.variable("e1", "price")))))
                        , Expression.value(3000))

        );
        query.insertInto("OutStream");
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("e1", "symbol")).
                        select("avgPrice", "avg", Expression.variable("e2", "price")).
                        groupBy("e1", "symbol").
                        having(Condition.compare(Expression.variable("avgPrice"),
                                                 Condition.Operator.GREATER_THAN,
                                                 Expression.value(50)))


        );

    }

//    from every a1 = infoStock[action == "buy"] and
//            a2 = infoStock[action == "buy"] ->
//                 b1 = cseEventStream[price > 70][timer.within(30)] ->
//                      b2 = cseEventStream[price > 75]
//    insert into StockQuote
//    a1.action as action, b1.price as priceA, b2.price as priceB

    @Test
    public void testCreatingPatternQuery() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                   QueryFactory.inputStream("e1", "Stream1"))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }

    @Test
    public void testCreatingPatternQuery1() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                   Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                                      QueryFactory.inputStream("e1", "Stream1")))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }

    @Test
    public void testCreatingPatternQuery2() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                   Pattern.every(
                                           Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                                              QueryFactory.inputStream("e1", "Stream1"))))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }

    @Test
    public void testCreatingPatternQuery3() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(Pattern.every(
                        Pattern.followedBy(QueryFactory.inputStream("e1", "Stream1"),
                                           QueryFactory.inputStream("e1", "Stream1"))),
                                   QueryFactory.inputStream("e1", "Stream1"))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }

    @Test
    public void testCreatingPatternQuery4() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(Pattern.logical(QueryFactory.inputStream("e1", "Stream1"),
                                                   LogicalElement.Type.AND,
                                                   QueryFactory.inputStream("e1", "Stream1")),
                                   Pattern.logical(QueryFactory.inputStream("e1", "Stream1"),
                                                   LogicalElement.Type.AND,
                                                   QueryFactory.inputStream("e1", "Stream1")))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }

    @Test
    public void testCreatingPatternQuery5() {
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.patternStream(
                Pattern.followedBy(Pattern.logical(QueryFactory.inputStream("e1", "Stream1"),
                                                   LogicalElement.Type.AND,
                                                   QueryFactory.inputStream("e1", "Stream1")),
                                   Pattern.count(QueryFactory.inputStream("e1", "Stream1"),0,7))));
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("action", Expression.variable("a1", "action")).
                        select("priceA", Expression.variable("b1", "price")).
                        select("priceB", Expression.variable("b2", "price"))

        );

    }


}
