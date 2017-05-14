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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;

import java.sql.SQLException;

import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.TABLE_NAME;

public class DefineRDBMSTableTest {
    private static final Logger log = Logger.getLogger(DefineRDBMSTableTest.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        log.info("== RDBMS Table DEFINITION tests started ==");
    }

    @After
    public void shutdown() {
        log.info("== RDBMS Table DEFINITION tests completed ==");
    }

    @Test
    public void RDBMSTableDefinitionTest1() throws InterruptedException, SQLException {
        //Testing table creation
        log.info("RDBMSTableDefinitionTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"MSFT", 57.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 3, totalRowsInTable);
            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'insertIntoRDBMSTableTest1' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void RDBMSTableDefinitionTest2() throws InterruptedException, SQLException {
        //Testing table creation with a primary key (normal insertion)
        log.info("RDBMSTableDefinitionTest2");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    "@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"MSFT", 57.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 3, totalRowsInTable);
            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest2' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void RDBMSTableDefinitionTest3() throws InterruptedException, SQLException {
        //Testing table creation with a primary key (purposeful duplicate insertion)
        log.info("RDBMSTableDefinitionTest3");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    "@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.1F, 100L});
            Thread.sleep(1000);

            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest3' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void RDBMSTableDefinitionTest4() throws InterruptedException, SQLException {
        //Testing table creation with index
        log.info("RDBMSTableDefinitionTest4");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    "@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Definition/Insertion failed", 2, totalRowsInTable);
            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest4' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void RDBMSTableDefinitionTest5() throws InterruptedException, SQLException {
        //Testing table creation with no username
        log.info("RDBMSTableDefinitionTest5");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest5' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void RDBMSTableDefinitionTest6() throws InterruptedException, SQLException {
        //Testing table creation with no password
        log.info("RDBMSTableDefinitionTest6");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                    "username=\"root\", field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";

            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest6' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expected = RDBMSTableException.class)
    public void RDBMSTableDefinitionTest7() throws InterruptedException, SQLException {
        //Testing table creation with no connection URL
        log.info("RDBMSTableDefinitionTest7");
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

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            executionPlanRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'RDBMSTableDefinitionTest7' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void deleteRDBMSTableTest1() throws InterruptedException {
        log.info("deleteTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream DeleteStockStream (deleteSymbol string); " +
                "@store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                "define table StockTable (symbol string, price float, volume long); ";

        String query = "" +
                "@info(name = 'query1') " +
                "from DeleteStockStream " +
                "delete StockTable " +
                "   on StockTable.symbol == deleteSymbol ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");
        executionPlanRuntime.start();

        deleteStockStream.send(new Object[]{"IBM"});
        deleteStockStream.send(new Object[]{"WSO2"});
        Thread.sleep(1000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void updateRDBMSTableTest1() throws InterruptedException {
        log.info("updateTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +

                "@store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler updateStockStream = executionPlanRuntime.getInputHandler("UpdateStockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
        stockStream.send(new Object[]{"IBM", 75.6F, 100L});
        stockStream.send(new Object[]{"WSO2_2", 57.6F, 100L});
        updateStockStream.send(new Object[]{"IBM", 56.6F, 100L});

        Thread.sleep(1000);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void updateOrInsertRDBMSTableTest1() throws InterruptedException {
        log.info("updateOrInsertTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long); " +

                "@store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
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

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler updateStockStream = executionPlanRuntime.getInputHandler("UpdateStockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
        stockStream.send(new Object[]{"IBM", 75.6F, 100L});
        stockStream.send(new Object[]{"WSO2_2", 57.6F, 100L});
        updateStockStream.send(new Object[]{"IBM", 56.6F, 100L});
        updateStockStream.send(new Object[]{"MSFT", 47.1F, 100L});

        Thread.sleep(1000);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTableJoinQuery1() throws InterruptedException {
        log.info("testTableJoinQuery1 - OUT 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string); " +

                "@store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream#window.length(1) join StockTable " +
                "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, StockTable.volume as volume " +
                "insert into OutputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        System.out.println("outside");
        executionPlanRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                System.out.println("inside");
                if (inEvents != null) {
                    System.out.println(inEvents.length);
                    for (Event event : inEvents) {
                        System.out.println(event);
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

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
        InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO3", 55.6F, 100L});
        stockStream.send(new Object[]{"MSFT", 75.6F, 10L});
        checkStockStream.send(new Object[]{"WSO3"});

        Thread.sleep(1000);

        Assert.assertEquals("Number of success events", 2, inEventCount);
        Assert.assertEquals("Number of remove events", 0, removeEventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

        executionPlanRuntime.shutdown();
    }
}
