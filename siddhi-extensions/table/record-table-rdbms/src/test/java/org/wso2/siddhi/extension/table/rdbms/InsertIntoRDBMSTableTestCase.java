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
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.sql.SQLException;

import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.RDBMSTableTestUtils.url;

public class InsertIntoRDBMSTableTestCase {
    private static final Logger log = Logger.getLogger(InsertIntoRDBMSTableTestCase.class);

    @BeforeClass
    public static void startTest() {
        log.info("== RDBMS Table INSERT tests started ==");
    }

    @AfterClass
    public static void shutdown() {
        log.info("== RDBMS Table INSERT tests completed ==");
    }

    @Test
    public void insertIntoRDBMSTableTest1() throws InterruptedException, SQLException {
        log.info("insertIntoRDBMSTableTest1");
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
                    "@info(name = 'query1') " +
                    "from StockStream   " +
                    "insert into StockTable ;";

            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
            stockStream.send(new Object[]{"IBM", 75.6f, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
            Thread.sleep(1000);

            long totalRowsInTable = RDBMSTableTestUtils.getRowsInTable(TABLE_NAME);
            Assert.assertEquals("Insertion failed", 3, totalRowsInTable);
            siddhiAppRuntime.shutdown();
        } catch (SQLException e) {
            log.info("Test case 'insertIntoRDBMSTableTest1' ignored due to " + e.getMessage());
            throw e;
        }
    }
}
