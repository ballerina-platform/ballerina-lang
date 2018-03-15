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

package org.ballerinalang.siddhi.core.store;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.StoreQueryCreationException;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.query.compiler.exception.SiddhiParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StoreQueryTableTestCase {

    private static final Logger log = LoggerFactory.getLogger(StoreQueryTableTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void test1() throws InterruptedException {
        log.info("Test1 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockTable ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 75 ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > volume*3/4  ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void test2() throws InterruptedException {
        log.info("Test2 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 75 " +
                "select symbol, volume ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        AssertJUnit.assertEquals(2, events[0].getData().length);

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "select symbol, volume ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);
        AssertJUnit.assertEquals(2, events[0].getData().length);

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 5 " +
                "select symbol, volume " +
                "having symbol == 'WSO2' ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(2, events.length);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void test3() throws InterruptedException {
        log.info("Test3 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 5 " +
                "select symbol, sum(volume) as totalVolume " +
                "group by symbol " +
                "having totalVolume >150 ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        AssertJUnit.assertEquals(200L, events[0].getData(1));

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 5 " +
                "select symbol, sum(volume) as totalVolume " +
                "group by symbol  ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(2, events.length);

        events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on price > 5 " +
                "select symbol, sum(volume) as totalVolume " +
                "group by symbol,price  ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(3, events.length);

        siddhiAppRuntime.shutdown();

    }

    @Test(expectedExceptions = StoreQueryCreationException.class)
    public void test4() throws InterruptedException {
        log.info("Test4 table");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
            Thread.sleep(500);

            Event[] events = siddhiAppRuntime.query("" +
                    "from StockTable " +
                    "on price > 5 " +
                    "select symbol1, sum(volume) as totalVolume " +
                    "group by symbol " +
                    "having totalVolume >150 ");
            EventPrinter.print(events);
            AssertJUnit.assertEquals(1, events.length);
            AssertJUnit.assertEquals(200L, events[0].getData(1));

        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expectedExceptions = StoreQueryCreationException.class)
    public void test5() throws InterruptedException {
        log.info("Test5 table");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        try {
            siddhiAppRuntime.start();
            Event[] events = siddhiAppRuntime.query("" +
                    "from StockTable1 " +
                    "on price > 5 " +
                    "select symbol1, sum(volume) as totalVolume " +
                    "group by symbol " +
                    "having totalVolume >150 ");
            EventPrinter.print(events);
            AssertJUnit.assertEquals(1, events.length);
            AssertJUnit.assertEquals(200L, events[0].getData(1));

        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expectedExceptions = SiddhiParserException.class)
    public void test6() throws InterruptedException {
        log.info("Test5 table");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams);
        try {
            siddhiAppRuntime.start();
            Event[] events = siddhiAppRuntime.query("" +
                    "from StockTable1 " +
                    "on price > 5 " +
                    "select symbol1, sum(volume)  totalVolume " +
                    "group by symbol " +
                    "having totalVolume >150 ");
            EventPrinter.print(events);
            AssertJUnit.assertEquals(1, events.length);
            AssertJUnit.assertEquals(200L, events[0].getData(1));

        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void test7() throws InterruptedException {
        log.info("Test7 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on symbol == 'IBM' " +
                "select symbol, volume ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        AssertJUnit.assertEquals("IBM", events[0].getData()[0]);
    }

    @Test
    public void test9() throws InterruptedException {
        log.info("Test9 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockTable " +
                "on volume > 10 " +
                "select symbol, price, volume " +
                "order by price " +
                "limit 2 ");
        EventPrinter.print(events);
        AssertJUnit.assertEquals(2, events.length);
        AssertJUnit.assertEquals(55.6F, events[0].getData()[1]);
        AssertJUnit.assertEquals(75.6f, events[1].getData()[1]);
    }


    @Test
    public void test10() throws InterruptedException {
        log.info("Test10 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        String storeQuery = "" +
                "from StockTable " +
                "on volume > 10 " +
                "select symbol, price, sum(volume) as totalVolume ";
        Event[] events = siddhiAppRuntime.query(storeQuery);
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        AssertJUnit.assertEquals(200L, events[0].getData()[2]);

        events = siddhiAppRuntime.query(storeQuery);
        EventPrinter.print(events);
        AssertJUnit.assertEquals(1, events.length);
        AssertJUnit.assertEquals(200L, events[0].getData()[2]);
    }

    @Test
    public void test11() throws InterruptedException {
        log.info("Test10 table");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        String storeQuery = "" +
                "from StockTable " +
                "on volume > 10 " +
                "select symbol, price, sum(volume) as totalVolume " +
                "group by symbol ";
        Event[] events = siddhiAppRuntime.query(storeQuery);
        EventPrinter.print(events);
        AssertJUnit.assertEquals(2, events.length);
        AssertJUnit.assertEquals(100L, events[0].getData()[2]);
        AssertJUnit.assertEquals(100L, events[1].getData()[2]);

        events = siddhiAppRuntime.query(storeQuery);
        EventPrinter.print(events);
        AssertJUnit.assertEquals(2, events.length);
        AssertJUnit.assertEquals(100L, events[0].getData()[2]);
        AssertJUnit.assertEquals(100L, events[1].getData()[2]);
    }
}
