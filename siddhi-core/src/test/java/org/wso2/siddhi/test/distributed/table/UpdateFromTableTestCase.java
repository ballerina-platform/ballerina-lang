/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.distributed.table;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class UpdateFromTableTestCase {
    static final Logger log = Logger.getLogger(UpdateFromTableTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("UpdateFromTableTestCase test1");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"GOOG", 10f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("UpdateFromTableTestCase test2");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable" +
                               "    on cseEventTable.symbol=='IBM';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"GOOG", 10f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("UpdateFromTableTestCase test1");

        SiddhiConfiguration configuration = new SiddhiConfiguration();
        configuration.setDistributedProcessing(true);
        SiddhiManager siddhiManager = new SiddhiManager(configuration);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseUpdateEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "insert into cseEventTable;");

        siddhiManager.addQuery("from cseUpdateEventStream " +
                               "update cseEventTable" +
                               "    on cseEventTable.symbol==symbol;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"IBM", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        InputHandler cseUpdateEventStream = siddhiManager.getInputHandler("cseUpdateEventStream");
        cseUpdateEventStream.send(new Object[]{"WSO2", 10f, 100l});
        Thread.sleep(50000);
        siddhiManager.shutdown();

    }
}
