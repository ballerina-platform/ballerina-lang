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
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.testng.annotations.Test;

/**
 * Sequence query test case.
 */
public class SequenceQueryTestCase {

//    from e1=Stream1[price >= 20] , e2=Stream2[ price >= e1.price]
//    select e1.symbol, avg(e2.price ) as avgPrice
//    insert into OutputStream
//    group by e1.symbol
//    having avgPrice>50;

    @Test
    public void testCreatingFilterPatternQuery() {
        Query query = Query.query();
        query.from(
                InputStream.sequenceStream(
                        State.next(State.stream(InputStream.stream("e1", "Stream1")),
                                State.stream(InputStream.stream("e2", "Stream1")),
                                Expression.Time.day(1)
                        )));
        query.insertInto("OutputStream");
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("e1")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price").ofStream("e2", 0))).
                        groupBy(Expression.variable("symbol").ofStream("e1")).
                        having(Expression.compare(Expression.variable("avgPrice"),
                                Compare.Operator.GREATER_THAN,
                                Expression.value(50)))


        );

    }
}
