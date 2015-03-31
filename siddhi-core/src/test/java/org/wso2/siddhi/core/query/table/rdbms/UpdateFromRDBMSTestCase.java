/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.query.table.rdbms;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import javax.sql.DataSource;
import java.sql.SQLException;

public class UpdateFromRDBMSTestCase {
    private static final Logger log = Logger.getLogger(UpdateFromRDBMSTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private static String dataSourceName = "cepDataSource";
    private DataSource dataSource = new BasicDataSource();

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void updateFromRDBMSTableTest1() throws InterruptedException {

        log.info("updateFromTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addSiddhiDataSource(dataSourceName, dataSource);

        try {
            if (dataSource.getConnection() != null) {


                String streams = "" +
                        "define stream StockStream (symbol string, price float, volume long); " +
                        "define stream UpdateStockStream (symbol string, price float, volume long); " +
                        "@from(datasource.id = 'cepDataSource' , table.name = 'table1')  define table StockTable (symbol string, price float, volume long); ";

                String query = "" +
                        "@info(name = 'query1') " +
                        "from StockStream " +
                        "insert into StockTable ;" +
                        "" +
                        "@info(name = 'query2') " +
                        "from UpdateStockStream " +
                        "update StockTable " +
                        "   on StockTable.symbol == symbol ;";

                ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

                InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
                InputHandler updateStockStream = executionPlanRuntime.getInputHandler("UpdateStockStream");

                executionPlanRuntime.start();

                stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
                stockStream.send(new Object[]{"IBM", 75.6f, 100l});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
                updateStockStream.send(new Object[]{"IBM", 57.6f, 100l});

                Thread.sleep(500);
                executionPlanRuntime.shutdown();
            }
        } catch (SQLException e) {
            //Ignore the tests
        }

    }


}
