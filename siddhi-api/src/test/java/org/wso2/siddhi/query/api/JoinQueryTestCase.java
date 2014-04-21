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
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class JoinQueryTestCase {

//    from cseEventStream[win.lengthBatch(50)][price >= 20]
//    insert into StockQuote symbol, avg(price) as avgPrice
//    group by symbol
//    having avgPrice>50

    @Test
    public void testCreatingJoinQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("cseEventStream").
                                window( "lengthBatch",Expression.value( 50)),
                        JoinStream.Type.JOIN,
                        QueryFactory.inputStream("cseEventStream").
                                filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                                        Condition.Operator.GREATER_THAN,
                                                                        Expression.variable("cseEventStream", "price")),
                                                      Condition.compare(Expression.value(100),
                                                                        Condition.Operator.GREATER_THAN_EQUAL,
                                                                        Expression.variable("cseEventStream", "volume")
                                                      )
                                        )
                                ).window("lengthBatch", Expression.value(50)) ,
                        Condition.compare(Expression.variable("cseEventStream", "price"),
                                          Condition.Operator.EQUAL,
                                          Expression.variable("cseEventStream", "price"))


                )
        );
        query.insertInto("StockQuote", OutStream.OutputEventsFor.EXPIRED_EVENTS);
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("cseEventStream", "symbol")).
                        select("", null, Expression.variable("cseEventStream", "symbol")).
                        groupBy("cseEventStream", "symbol").
                        having(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                 Condition.Operator.GREATER_THAN,
                                                 Expression.variable(null, "price"))
                        )
        );

    }

//    from TickEvent as t unidirectional join NewsEvent[std.unique(symbol)]as n
//    on t.symbol == n.symbol
//    insert into JoinStream *

    @Test
    public void testCreatingUnidirectionalJoinQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("t", "TickEvent"),
                        JoinStream.Type.JOIN,
                        QueryFactory.inputStream("n", "NewsEvent").
                                window("unique", Expression.variable("symbol")),
                        Condition.compare(Expression.variable("t", "symbol"),
                                          Condition.Operator.EQUAL,
                                          Expression.variable("n", "symbol")),
                        null,
                        JoinStream.EventTrigger.LEFT

                )
        );
        query.insertInto("JoinStream");
    }
}
