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
package org.wso2.siddhi.extension.table.rdbms;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;

import java.sql.SQLException;

import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.url;

public class DefineRDBMSTableTestCase {
    private static final Logger log = Logger.getLogger(DefineRDBMSTableTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @BeforeClass
    public static void startTest() {
        log.info("== RDBMS Table DEFINITION tests started ==");
    }

    @AfterClass
    public static void shutdown() {
        log.info("== RDBMS Table DEFINITION tests completed ==");
    }

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void rdbmstabledefinitiontest1() throws InterruptedException, SQLException {
        //Testing table creation
        log.info("rdbmstabledefinitiontest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\"," +
                    "pool.properties=\"driverClassName:org.h2.Driver\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"MSFT", 57.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 3, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'insertIntoRDBMSTableTest1' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void rdbmstabledefinitiontest2() throws InterruptedException, SQLException {
        //Testing table creation with a primary key (normal insertion)
        log.info("rdbmstabledefinitiontest2");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    "@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"MSFT", 57.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 3, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest2' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void rdbmstabledefinitiontest3() throws InterruptedException, SQLException {
        //Testing table creation with a primary key (purposeful duplicate insertion)
        log.info("rdbmstabledefinitiontest3");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    "@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.1F, 100L});
            Thread.sleep(1000);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest3' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void rdbmstabledefinitiontest4() throws InterruptedException, SQLException {
        //Testing table creation with a compound primary key (normal insertion)
        log.info("rdbmstabledefinitiontest4");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    "@PrimaryKey(\"symbol, price\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"MSFT", 57.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 58.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 4, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest4' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void rdbmstabledefinitiontest5() throws InterruptedException, SQLException {
        //Testing table creation with index
        log.info("rdbmstabledefinitiontest5");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    "@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 2, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest5' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void rdbmstabledefinitiontest6() throws InterruptedException, SQLException {
        //Testing table creation with no username
        log.info("rdbmstabledefinitiontest6");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest6' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void rdbmstabledefinitiontest7() throws InterruptedException, SQLException {
        //Testing table creation with no password
        log.info("rdbmstabledefinitiontest7");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest7' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void rdbmstabledefinitiontest8() throws InterruptedException, SQLException {
        //Testing table creation with no connection URL
        log.info("rdbmstabledefinitiontest8");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'rdbmstabledefinitiontest8' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Ignore
    @Test
    public void deleteRDBMSTableTest1() throws InterruptedException {
        log.info("deleteTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream DeleteStockStream (deleteSymbol string); " +
                "@store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                "define table StockTable (symbol string, price float, volume long); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol == deleteSymbol ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        InputHandler deleteStockStream = siddhiAppRuntime.getInputHandler("DeleteStockStream");
        siddhiAppRuntime.start();

        deleteStockStream.send(new Object[]{"IBM"});
        deleteStockStream.send(new Object[]{"WSO2"});
        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();
    }

    @Ignore
    @Test
    public void updateRDBMSTableTest1() throws InterruptedException {
        log.info("updateTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +

                "@store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                //"@PrimaryKey(\"symbol\")" +
                "@Index(\"volume\")" +
                "define table StockTable (symbol string, price float, volume long); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "update StockTable " +
                "   on StockTable.symbol == symbol;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
        stockStream.send(new Object[]{"IBM", 75.6F, 100L});
        stockStream.send(new Object[]{"WSO2_2", 57.6F, 100L});
        updateStockStream.send(new Object[]{"IBM", 56.6F, 100L});

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

    }

    @Ignore
    @Test
    public void updateOrInsertRDBMSTableTest1() throws InterruptedException {
        log.info("updateOrInsertTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +

                "@store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                //"@PrimaryKey(\"symbol\")" +
                "@Index(\"volume\")" +
                "define table StockTable (symbol string, price float, volume long); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from UpdateStockStream " +
                "UPDATE OR INSERT INTO StockTable " +
                "   on StockTable.symbol == symbol;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
        stockStream.send(new Object[]{"IBM", 75.6F, 100L});
        stockStream.send(new Object[]{"WSO2_2", 57.6F, 100L});
        updateStockStream.send(new Object[]{"IBM", 56.6F, 100L});
        updateStockStream.send(new Object[]{"MSFT", 47.1F, 100L});

        Thread.sleep(1000);
        siddhiAppRuntime.shutdown();

    }

    @Ignore
    @Test
    public void testTableJoinQuery1() throws InterruptedException {
        log.info("testTableJoinQuery1 - OUT 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +

                "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as " +
                "volume " +
                "insert into OutputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        inEventCount++;
                        switch (inEventCount) {
                            case 1:
                                Assert.assertArrayEquals(new Object[]{"WSO3", "WSO3", 100L}, event.getData());
                                break;
                            case 2:
                                Assert.assertArrayEquals(new Object[]{"WSO3", "MSFT", 10L}, event.getData());
                                break;
                            default:
                                Assert.assertSame(2, inEventCount);
                        }
                    }
                    eventArrived = true;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }
        });

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO3", 55.6F, 100L});
        stockStream.send(new Object[]{"MSFT", 75.6F, 10L});
        checkStockStream.send(new Object[]{"WSO3"});

        Thread.sleep(1000);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        siddhiAppRuntime.shutdown();
    }
}
