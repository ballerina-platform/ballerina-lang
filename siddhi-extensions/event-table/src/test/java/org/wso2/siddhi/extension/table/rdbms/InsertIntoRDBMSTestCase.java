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
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class InsertIntoRDBMSTestCase {
    private static final Logger log = Logger.getLogger(InsertIntoRDBMSTestCase.class);
    private DataSource dataSource = new BasicDataSource();

    @Test
    public void insertIntoRDBMSTableTest1() throws InterruptedException {
        log.info("InsertIntoTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setDataSource(RDBMSTestConstants.DATA_SOURCE_NAME, dataSource);

        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                DBConnectionHelper.getDBConnectionHelperInstance().clearDatabaseTable(dataSource, RDBMSTestConstants
                        .TABLE_NAME);
                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "@store(type = 'rdbms' , datasource.name = '" + RDBMSTestConstants.DATA_SOURCE_NAME + "'" +
                        " , table.name = '" + RDBMSTestConstants.TABLE_NAME + "') " +
                        "define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream   " +
                        "insert into StockTable ;";

                SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

                InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

                siddhiAppRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                Thread.sleep(1000);

                long totalRowsInTable = DBConnectionHelper.getDBConnectionHelperInstance().getRowsInTable(dataSource);
                Assert.assertEquals("Insertion failed", 3, totalRowsInTable);

                siddhiAppRuntime.shutdown();
            }
        } catch (SQLException e) {
            log.info("Test case ignored due to DB connection unavailability");
        }

    }
}
