/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.siddhi.query.api.execution.query.Query;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.State;
import org.ballerinalang.siddhi.query.api.execution.query.input.stream.InputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.api.expression.constant.TimeConstant;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Testcase to find absent patterns.
 */
public class AbsentPatternTestCase {

    @Test(expectedExceptions = SiddhiParserException.class)
    public void test1() throws SiddhiParserException {

        SiddhiCompiler.parseQuery("from e1=Stream1[price>20] -> not Stream2[price>e1.price] " +
                "select e1.symbol as symbol, e1.price as price " +
                "insert into OutputStream ;");
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void test2() throws SiddhiParserException {

        SiddhiCompiler.parseQuery("from e1=Stream1[price>20] -> not e2=Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol, e1.price as price " +
                "insert into OutputStream ;");
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void test3() throws SiddhiParserException {

        SiddhiCompiler.parseQuery("from not Stream1[price>20] for 1 sec -> not Stream2[price>e1.price] for 1 sec " +
                "select e1.symbol as symbol, e1.price as price " +
                "insert into OutputStream ;");
    }

    @Test
    public void test4() throws SiddhiParserException {
        Query query = SiddhiCompiler.parseQuery("from e1=Stream1[price>20] -> not Stream2[price>e1.price] for 2 sec " +
                "select e1.symbol as symbol1 " +
                "insert into OutputStream ;");
        AssertJUnit.assertNotNull(query);

        Query api = Query.query();
        api.from(
                InputStream.patternStream(State.next(
                        State.stream(InputStream.stream("e1", "Stream1")
                                .filter(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.value(20)))),
                        State.logicalNot(State.stream(InputStream.stream("Stream2")
                                .filter(Expression.compare(Expression.variable("price"),
                                        Compare.Operator.GREATER_THAN,
                                        Expression.variable("price").ofStream("e1")))))
                                .waitingTime(new TimeConstant(2000)))
                ))
                .select(Selector.selector().select("symbol1", Expression.variable("symbol").ofStream("e1")))
                .insertInto("OutputStream");

        AssertJUnit.assertEquals(api, query);
    }

}
