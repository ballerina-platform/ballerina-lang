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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.table;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class InsertIntoTableTestCase {
    private static final Logger log = Logger.getLogger(InsertIntoTableTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void insertIntoTableTest1() throws InterruptedException {
        log.info("InsertIntoTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 75.6f, 100l});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void insertIntoTableTest2() throws InterruptedException {
        log.info("InsertIntoTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); " +
                "define table StockTable2 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockStream " +
                "insert into StockTable2 ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 75.6f, 100l});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);

        executionPlanRuntime.shutdown();

    }

    @Test
    public void insertIntoTableTest3() throws InterruptedException {
        log.info("InsertIntoTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream StockStream2 (symbol string, price float, volume long); " +
                "define table StockTable (symbol string, price float, volume long); " +
                "define table StockTable2 (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from StockStream2 " +
                "insert into StockTable2 ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
        stockStream.send(new Object[]{"IBM", 75.6f, 100l});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);

        executionPlanRuntime.shutdown();

    }


}
