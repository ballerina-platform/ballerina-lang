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
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.sql.SQLException;

import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.url;

public class JoinRDBMSTableTestCase {
    private static final Logger log = Logger.getLogger(JoinRDBMSTableTestCase.class);
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
        log.info("== RDBMS Table JOIN tests started ==");
    }

    @AfterClass
    public static void shutdown() {
        log.info("== RDBMS Table JOIN tests completed ==");
    }

    @Test
    public void testTableJoinQuery1() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery1 - OUT 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable " +
                    "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, " +
                    "StockTable.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "IBM", 10L}, event.getData());
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery1' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testTableJoinQuery2() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery2 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable " +
                    " on CheckStockStream.symbol==StockTable.symbol " +
                    "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, " +
                    "StockTable.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery2' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void testTableJoinQuery3() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery3 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol1 string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable " +
                    " on symbol1== symbol " +
                    "select symbol1 as checkSymbol, symbol as symbol, volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery3' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void testTableJoinQuery4() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery4 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) as c join StockTable as s " +
                    " on c.symbol==s.symbol " +
                    "select c.symbol as checkSymbol, s.symbol as symbol, s.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery4' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testTableJoinQuery5() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery5 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable as s " +
                    " on CheckStockStream.symbol==s.symbol " +
                    "select CheckStockStream.symbol as checkSymbol, s.symbol as symbol, s.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery5' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void testTableJoinQuery6() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery6 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);

            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable as s " +
                    " on CheckStockStream.symbol==s.symbol " +
                    "select CheckStockStream.symbol as checkSymbol, s.symbol as symbol, s.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery6' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testTableJoinQuery7() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery7 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
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
                    "from CheckStockStream#window.length(1) join StockTable " +
                    " on StockTable.symbol==CheckStockStream.symbol " +
                    "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, " +
                    "StockTable.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery7' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test
    public void testTableJoinQuery8() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery8 - OUT 1");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float); " +
                    "define stream CheckStockStream (symbol string); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "define table StockTable (symbol string, price float); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from CheckStockStream#window.length(1) join StockTable " +
                    " on StockTable.symbol==CheckStockStream.symbol " +
                    "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2"}, event.getData());
                                    break;
                                default:
                                    Assert.assertSame(1, inEventCount);
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

            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 1, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery8' ignored due to " + e.getMessage());
            throw e;
        }

    }

    @Test
    public void testTableJoinQuery9() throws InterruptedException, SQLException {
        log.info("testTableJoinQuery9 - OUT 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            RDBMSTableTestUtils.clearDatabaseTable(TABLE_NAME);
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream CheckStockStream (symbol string); " +
                    "@Store(type=\"rdbms\", jdbc.url=\"" + url + "\", " +
                    "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                    //"@PrimaryKey(\"symbol\")" +
                    //"@Index(\"volume\")" +
                    "@connection(maxWait = '4000')" +
                    "define table StockTable (symbol string, price float, volume long); ";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into StockTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from CheckStockStream#window.length(1) join StockTable " +
                    "select CheckStockStream.symbol as checkSymbol, StockTable.symbol as symbol, " +
                    "StockTable.volume as volume  " +
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
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "WSO2", 100L}, event.getData());
                                    break;
                                case 2:
                                    Assert.assertArrayEquals(new Object[]{"WSO2", "IBM", 10L}, event.getData());
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

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 10L});
            checkStockStream.send(new Object[]{"WSO2"});
            Thread.sleep(1000);

            Assert.assertEquals("Number of success events", 2, inEventCount);
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'testTableJoinQuery9' ignored due to " + e.getMessage());
            throw e;
        }
    }

}
