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
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

public class TableQueryTestCase {

    @Test
    public void testCreatingDeleteQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                               Condition.Operator.GREATER_THAN,
                                                               Expression.variable("price")),
                                             Condition.compare(Expression.value(100),
                                                               Condition.Operator.GREATER_THAN_EQUAL,
                                                               Expression.variable("volume")
                                             )
                        )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.deleteBy("StockQuote", Condition.compare(Expression.variable("symbol"),
                                                       Condition.Operator.EQUAL,
                                                       Expression.variable("StockQuote", "symbol")));


    }


    @Test
    public void testCreatingDeleteByTypeQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                               Condition.Operator.GREATER_THAN,
                                                               Expression.variable("price")),
                                             Condition.compare(Expression.value(100),
                                                               Condition.Operator.GREATER_THAN_EQUAL,
                                                               Expression.variable("volume")
                                             )
                        )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.deleteBy("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS, Condition.compare(Expression.variable("symbol"),
                                                                                             Condition.Operator.EQUAL,
                                                                                             Expression.variable("StockQuote", "symbol")));

    }

    @Test
    public void testCreatingUpdateByQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                               Condition.Operator.GREATER_THAN,
                                                               Expression.variable("price")),
                                             Condition.compare(Expression.value(100),
                                                               Condition.Operator.GREATER_THAN_EQUAL,
                                                               Expression.variable("volume")
                                             )
                        )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.updateBy("StockQuote", Condition.compare(Expression.variable("symbol"),
                                                       Condition.Operator.EQUAL,
                                                       Expression.variable("StockQuote", "symbol")));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );


    }

    @Test
    public void testCreatingUpdateByTypeQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                               Condition.Operator.GREATER_THAN,
                                                               Expression.variable("price")),
                                             Condition.compare(Expression.value(100),
                                                               Condition.Operator.GREATER_THAN_EQUAL,
                                                               Expression.variable("volume")
                                             )
                        )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.updateBy("StockQuote", OutStream.OutputEventsFor.ALL_EVENTS, Condition.compare(Expression.variable("symbol"),
                                                                                             Condition.Operator.EQUAL,
                                                                                             Expression.variable("StockQuote", "symbol")));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );


    }


    @Test
    public void testCreatingInQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(
                                Condition.and(
                                        Condition.compare(
                                                Expression.add(Expression.value(7), Expression.value(9.5)),
                                                Condition.Operator.GREATER_THAN,
                                                Expression.variable("price")),
                                        Condition.in(
                                                Condition.compare(Expression.value(9.5),
                                                                  Condition.Operator.GREATER_THAN,
                                                                  Expression.variable("price")),
                                                "eventTable"
                                        )

                                )).window("lengthBatch", Expression.value(50))
        );
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", "avg", Expression.variable("symbol")).
                        groupBy("symbol").
                        having(Condition.compare(Expression.variable("avgPrice"),
                                                 Condition.Operator.GREATER_THAN_EQUAL,
                                                 Expression.value(50)
                        ))
        );

    }


}
