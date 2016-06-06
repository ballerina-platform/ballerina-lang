/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.eventtable.rdbms;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DeleteFromRDBMSTestCase {
    private static final Logger log = Logger.getLogger(DeleteFromRDBMSTestCase.class);
    private DataSource dataSource = new BasicDataSource();

    @Test
    public void deleteFromRDBMSTableTest1() throws InterruptedException {

        log.info("deleteFromTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {
                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);

                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol == symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 0, totalRowsInTable);

                executionPlanRuntime.shutdown();

            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }


    @Test
    public void deleteFromRDBMSTableTest2() throws InterruptedException {

        log.info("deleteFromTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "' , bloom.filters = 'enable')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on symbol == StockTable.symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"WSO2", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 0, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }


    @Test
    public void deleteFromRDBMSTableTest3() throws InterruptedException {

        log.info("deleteFromTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol == 'IBM'  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromRDBMSTableTest4() throws InterruptedException {

        log.info("deleteFromTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on 'IBM' == StockTable.symbol  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromRDBMSTableTest5() throws InterruptedException {

        log.info("deleteFromTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on 'IBM' == symbol  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromRDBMSTableTest6() throws InterruptedException {

        log.info("deleteFromTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on symbol == 'IBM'  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }


    @Test
    public void deleteFromTableTest7() throws InterruptedException {
        log.info("deleteFromTableTest7");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "') " +
                        "define table StockTable (symbol string, price float, volume long); ";
                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol==symbol and StockTable.price > price and  StockTable.volume == volume  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"IBM", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromTableTest8() throws InterruptedException {
        log.info("deleteFromTableTest8");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);

        try {
            if (dataSource.getConnection() != null) {

                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "') " +
                        "define table StockTable (symbol string, price float, volume long); ";
                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol=='IBM' and StockTable.price > 50 and  StockTable.volume == volume  ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"IBM", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 1, totalRowsInTable);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }


    }


    @Test
    public void deleteFromRDBMSTableTest9() throws InterruptedException {

        log.info("deleteFromTableTest9");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {
                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);

                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "', cache='lfu', cache.size='1000')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol == symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);

                executionPlanRuntime.shutdown();

            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromRDBMSTableTest10() throws InterruptedException {

        log.info("deleteFromTableTest10");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {
                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);

                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream DeleteStockStream (symbol string, price float, volume long); " +
                        "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "', bloom.filters = 'enable')  " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from DeleteStockStream " +
                        "delete StockTable " +
                        "   on StockTable.symbol == symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);

                executionPlanRuntime.shutdown();

            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

    @Test
    public void deleteFromRDBMSTableTest11() throws InterruptedException {

        log.info("deleteFromTableTest11");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);
        try {
            if (dataSource.getConnection() != null) {
                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource,RDBMSTestConstants.TABLE_NAME);

                String streams = "" +
                                 "define stream StockStream (symbol string, price float, volume long); " +
                                 "define stream DeleteStockStream (symbol string, price float, volume long); " +
                                 "@from(eventtable = 'rdbms' ,datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "' , table.name = '" + RDBMSTestConstants.TABLE_NAME + "', bloom.filters = 'enable')  " +
                                 "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                               "@info(name = 'query1') " +
                               "from StockStream " +
                               "insert into StockTable ;" +
                               "" +
                               "@info(name = 'query2') " +
                               "from DeleteStockStream " +
                               "delete StockTable " +
                               "   on StockTable.symbol == symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                deleteStockStream.send(new Object[]{"IBM", 57.6f, 100l});
                Thread.sleep(1000);
                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Deletion failed", 2, totalRowsInTable);
                Thread.sleep(1000);
                stockStream.send(new Object[]{null, 45.5f, 100l});
                executionPlanRuntime.shutdown();
                Thread.sleep(1000);
                try{
                    siddhiManager.createExecutionPlanRuntime(streams + query);
                } catch (NullPointerException ex){
                    Assert.fail("Cannot Process null values in bloom filter");
                }
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }

}
