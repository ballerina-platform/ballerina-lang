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
import org.wso2.siddhi.query.api.query.input.sequence.Sequence;

public class SequenceQueryTestCase {

//    from e1=Stream1[price >= 20] , e2=Stream2[ price >= e1.price]
//    insert into OutStream e1.symbol, avg(e2.price ) as avgPrice
//    group by e1.symbol
//    having avgPrice>50;

    @Test
    public void testCreatingFilterPatternQuery() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.sequenceStream(
                        Sequence.next(QueryFactory.inputStream("e1", "Stream1"),
                                      QueryFactory.inputStream("e2", "Stream1")),
                        Expression.value(2000)));
        query.insertInto("OutStream");
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("e1", "symbol")).
                        select("avgPrice", "avg", Expression.variable("e2", 0, "price")).
                        groupBy("e1", "symbol").
                        having(Condition.compare(Expression.variable("avgPrice"),
                                                 Condition.Operator.GREATER_THAN,
                                                 Expression.value(50)))


        );

    }
}