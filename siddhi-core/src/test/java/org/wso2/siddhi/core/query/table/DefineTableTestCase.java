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

package org.wso2.siddhi.core.query.table;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.query.table.util.TestStore;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 1/17/15.
 */
public class DefineTableTestCase {
    private static final Logger log = Logger.getLogger(DefineTableTestCase.class);

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTableDefinition1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TableDefinition tableDefinition = TableDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price", Attribute.Type.INT);

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testTableDefinition2 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table Table(symbol string, price int, volume float) ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery3() throws InterruptedException {
        log.info("testTableDefinition3 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table TestTable(symbol string, price int, volume float); " +
                "define table TestTable(symbols string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery4() throws InterruptedException {
        log.info("testTableDefinition4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table TestTable(symbol string, volume float); " +
                "define table TestTable(symbols string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery5() throws InterruptedException {
        log.info("testTableDefinition5 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "define table TestTable(symbol string, price int, volume float); " +
                "define table TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery6() throws InterruptedException {
        log.info("testTableDefinition6 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "define stream TestTable(symbol string, price int, volume float); " +
                "define table TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(definitions);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery7() throws InterruptedException {
        log.info("testTableDefinition7 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "define table TestTable(symbol string, price int, volume float); " +
                "define stream TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(definitions);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testQuery8() throws InterruptedException {
        log.info("testTableDefinition8 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;" +
                "" +
                "define table OutputStream (symbol string, price float, volume long); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }


    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery9() throws InterruptedException {
        log.info("testTableDefinition9 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price float, volume long); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery10() throws InterruptedException {
        log.info("testTableDefinition10 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float); " +
                "define table OutputStream (symbol string, price float, volume long);" +
                "" +
                "from StockStream " +
                "select symbol, price " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery11() throws InterruptedException {
        log.info("testTableDefinition11 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery12() throws InterruptedException {
        log.info("testTableDefinition12 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery13() throws InterruptedException {
        log.info("testTableDefinition13 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price int, volume float, time long); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery14() throws InterruptedException {
        log.info("testTableDefinition14 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price int, volume int); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testQuery15() throws InterruptedException {
        log.info("testTableDefinition15 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from OutputStream " +
                "select symbol, price, volume " +
                "insert into StockStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery16() {
        log.info("testTableDefinition16 - Table w/ ref");

        Map<String, String> storeConfigs = new HashMap<>();
        storeConfigs.put("test1.type", "test");
        storeConfigs.put("test1.uri", "http://localhost");
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(null, storeConfigs);
        inMemoryConfigManager.extractStoreConfigs("test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setConfigManager(inMemoryConfigManager);
        siddhiManager.setExtension("store:test", TestStore.class);
        String siddhiApp = "" +
                "@store(ref='test1')" +
                "define table testTable (symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();

        Map<String, String> expectedStoreConfigs = new HashMap<>();
        expectedStoreConfigs.put("type", "test");
        expectedStoreConfigs.put("uri", "http://localhost");
        Assert.assertEquals("Test store initialization failure", expectedStoreConfigs, TestStore.storeConfigs);
    }

    @Test
    public void testQuery17() throws InterruptedException {
        log.info("testTableDefinition17 - Table w/o ref");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type='test', uri='http://localhost:8080')" +
                "define table testStore (symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();

        Map<String, String> expectedStoreConfigs = new HashMap<>();
        expectedStoreConfigs.put("type", "test");
        expectedStoreConfigs.put("uri", "http://localhost:8080");
        Assert.assertEquals("Test store initialization failure", expectedStoreConfigs, TestStore.storeConfigs);
    }
}
