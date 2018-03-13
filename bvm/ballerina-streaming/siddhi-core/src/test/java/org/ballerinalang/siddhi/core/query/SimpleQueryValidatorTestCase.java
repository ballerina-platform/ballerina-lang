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
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.config.SiddhiContext;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.util.parser.SiddhiAppParser;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleQueryValidatorTestCase {
    private SiddhiContext siddhiContext;

    @BeforeMethod
    public void init() {
        siddhiContext = new SiddhiContext();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testQueryWithNotExistingAttributes() throws InterruptedException {

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50] select symbol1,price,volume insert " +
                "into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query),
                cseEventStream + query, siddhiContext);
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void testQueryWithDuplicateDefinition() throws InterruptedException {
        String cseEventStream = "define stream \n cseEventStream (symbol string, price float, volume long);";
        String duplicateStream = "define stream outputStream (symbol string, price float);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50] select symbol,price,volume insert " +
                "into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + duplicateStream + query),
                cseEventStream + duplicateStream + query, siddhiContext);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInvalidFilterCondition1() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[volume >= 50 and volume] select symbol,price," +
                "volume insert into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query),
                cseEventStream + query, siddhiContext);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testInvalidFilterCondition2() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long);";
        String query = "@info(name = 'query1') from cseEventStream[not(price)] select symbol,price,volume insert into" +
                " outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query),
                cseEventStream + query, siddhiContext);
    }

    @Test
    public void testComplexFilterQuery1() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long, available " +
                "bool);";
        String query = "@info(name = 'query1') from cseEventStream[available] select symbol,price,volume insert into " +
                "outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query),
                cseEventStream + query, siddhiContext);
    }

    @Test
    public void testComplexFilterQuery2() throws InterruptedException {
        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume long, available " +
                "bool);";
        String query = "@info(name = 'query1') from cseEventStream[available and price>50] select symbol,price,volume" +
                " insert into outputStream ;";

        SiddhiAppParser.parse(SiddhiCompiler.parse(cseEventStream + query),
                cseEventStream + query, siddhiContext);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testQueryWithTable() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table TestTable(symbol string, volume float); " +
                "" +
                "from TestTable " +
                "select * " +
                "insert into OutputStream; ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testQueryWithAggregation() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define stream TradeStream (symbol string, price double, volume long, timestamp long);\n" +
                "define aggregation TradeAggregation\n" +
                "  from TradeStream\n" +
                "  select symbol, avg(price) as avgPrice, sum(price) as total\n" +
                "    group by symbol\n" +
                "    aggregate by symbol every sec ... year; " +
                "" +
                "from every TradeAggregation \n" +
                "select * \n" +
                "insert into OutputStream; ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testQueryWithEveryTable() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table TestTable(symbol string, volume float);\n" +
                "" +
                "from every TestTable " +
                "select * " +
                "insert into OutputStream; ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testQueryWithEveryAggregation() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define stream TradeStream (symbol string, price double, volume long, timestamp long);\n" +
                "define aggregation TradeAggregation\n" +
                "  from TradeStream\n" +
                "  select symbol, avg(price) as avgPrice, sum(price) as total\n" +
                "    group by symbol\n" +
                "    aggregate by symbol every sec ... year; " +
                "" +
                "from every TradeAggregation " +
                "select * " +
                "insert into OutputStream; ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }
}
