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
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;

import java.sql.SQLException;

import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.url;

public class UpdateOrInsertRDBMSTableTestCase {
    private static final Logger log = Logger.getLogger(UpdateOrInsertRDBMSTableTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @BeforeClass
    public static void startTest() {
        log.info("== RDBMS Table UPDATE/INSERT tests started ==");
    }

    @AfterClass
    public static void shutdown() {
        log.info("== RDBMS Table UPDATE/INSERT tests completed ==");
    }

    @Test
    public void updateOrInsertTableTest1() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream UpdateStockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol=='IBM' ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6F, 100L});
            updateStockStream.send(new Object[]{"GOOG", 10.6F, 100L});
            Thread.sleep(500);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest1' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest2() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest2");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query2') " +
                    "from StockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 10F, 100L});
            Thread.sleep(500);

            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest2' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest3() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest3");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long); " +
                    "define stream UpdateStockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and  volume==StockTable.volume) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                case 3:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(3, inEventCount);
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 55.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            updateStockStream.send(new Object[]{"IBM", 77.6F, 200L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            Thread.sleep(500);

            Assert.assertEquals("Number of success events", 3, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest3' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest4() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest4");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query2') " +
                    "from StockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and  volume==StockTable.volume) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                case 3:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(3, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 55.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            stockStream.send(new Object[]{"IBM", 77.6F, 200L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            Thread.sleep(500);

            Assert.assertEquals("Number of success events", 3, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest4' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Ignore
    @Test(expected = DuplicateDefinitionException.class)
    public void updateOrInsertTableTest5() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);

            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long); " +
                    "define stream UpdateStockStream (comp string, vol long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "select comp as symbol, vol as volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    eventArrived = true;
                }
            });

            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = siddhiAppRuntime.getInputHandler("CheckStockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 55.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            updateStockStream.send(new Object[]{"FB", 300L});
            checkStockStream.send(new Object[]{"FB", 300L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            Thread.sleep(500);

            Assert.assertEquals("Number of success events", 0, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", false, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest5' ignored due to " + e.getMessage());
            throw e;
        }

    }

    @Test
    public void updateOrInsertTableTest6() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest6");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long); " +
                    "define stream UpdateStockStream (comp string, vol long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "select comp as symbol, 0f as price, vol as volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and  volume==StockTable.volume) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                case 3:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(3, inEventCount);
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 55.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            updateStockStream.send(new Object[]{"IBM", 200L});
            updateStockStream.send(new Object[]{"FB", 300L});
            checkStockStream.send(new Object[]{"IBM", 100L});
            checkStockStream.send(new Object[]{"WSO2", 100L});
            Thread.sleep(500);

            Assert.assertEquals("Number of success events", 3, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest5' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void updateOrInsertTableTest7() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest7");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long, price float); " +
                    "define stream UpdateStockStream (comp string, vol long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "select comp as symbol,  5f as price, vol as volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and volume==StockTable.volume and price " +
                    "< StockTable.price) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L, 56.6f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200L, 0f}, event.getData());
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 155.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L, 56.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"IBM", 200L, 0f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            Thread.sleep(2000);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest7' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest8() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest8");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long, price float); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query2') " +
                    "from StockStream " +
                    "select symbol, price, volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and volume==StockTable.volume and price " +
                    "< StockTable.price) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L, 55.6f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200L, 55.6f}, event.getData());
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

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 155.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L, 55.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            stockStream.send(new Object[]{"IBM", 155.6F, 200L});
            checkStockStream.send(new Object[]{"IBM", 200L, 55.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            Thread.sleep(500);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest8' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest9() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest9");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long, price float); " +
                    "define stream UpdateStockStream (comp string, vol long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream left outer join StockTable " +
                    "   on UpdateStockStream.comp == StockTable.symbol " +
                    "select  symbol, ifThenElse(price is nulL,0F,price) as price, vol as volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(CheckStockStream.symbol==StockTable.symbol and " +
                    "CheckStockStream.volume==StockTable.volume and " +
                    "CheckStockStream.price < StockTable.price) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 100L, 55.6f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200L, 55.6f}, event.getData());
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 155.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L, 55.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200L});
            checkStockStream.send(new Object[]{"IBM", 200L, 55.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();

        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest9' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void updateOrInsertTableTest10() throws InterruptedException, SQLException {
        log.info("updateOrInsertTableTest10");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string, volume long, price float); " +
                    "define stream UpdateStockStream (comp string, vol long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream left outer join StockTable " +
                    "   on UpdateStockStream.comp == StockTable.symbol " +
                    "select comp as symbol, ifThenElse(price is nulL,5F,price) as price, vol as volume " +
                    "update or insert into StockTable " +
                    "   on StockTable.symbol==symbol;" +
                    "" +
                    "@info(name = 'query3') " +
                    "from CheckStockStream[(symbol==StockTable.symbol and volume == StockTable.volume and " +
                    "price < StockTable.price) in StockTable] " +
                    "insert into OutStream;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            siddhiAppRuntime.addCallback("query3", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventCount++;
                            switch (inEventCount) {
                                case 1:
                                    Assert.assertArrayEquals(new Object[]{"IBM", 200L, 0f}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", 300L, 4.6f}, event.getData());
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
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            checkStockStream.send(new Object[]{"IBM", 100L, 155.6f});
            checkStockStream.send(new Object[]{"WSO2", 100L, 155.6f});
            updateStockStream.send(new Object[]{"IBM", 200L});
            updateStockStream.send(new Object[]{"WSO2", 300L});
            checkStockStream.send(new Object[]{"IBM", 200L, 0f});
            checkStockStream.send(new Object[]{"WSO2", 300L, 4.6f});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'updateOrInsertTableTest10' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void insertOverwriteTableTest11() throws InterruptedException, SQLException {
        log.info("insertOverwriteTableTest11");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream UpdateStockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.volume==volume ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6F, 100L});
            updateStockStream.send(new Object[]{"GOOG", 10.6F, 100L});
            Thread.sleep(500);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Update failed", 3, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'insertOverwriteTableTest11' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void insertOverwriteTableTest12() throws InterruptedException, SQLException {
        log.info("insertOverwriteTableTest12");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream UpdateStockStream (symbol string, price float, volume long); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from UpdateStockStream " +
                    "update or insert into StockTable " +
                    "   on StockTable.volume == volume ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            InputHandler updateStockStream = siddhiAppRuntime.getInputHandler("UpdateStockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6F, 100L});
            updateStockStream.send(new Object[]{"GOOG", 10.6F, 200L});
            Thread.sleep(500);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Update failed", 4, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'insertOverwriteTableTest12' ignored due to " + e.getMessage());
            throw e;
        }
    }
}
