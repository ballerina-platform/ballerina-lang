/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.aggregation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class AggregationTestCase {

    static final Logger LOG = Logger.getLogger(AggregationTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void incrementalStreamProcessorTest1() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query1') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " aggregate by arrival every sec ... min";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test
    public void incrementalStreamProcessorTest2() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query2') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " aggregate every sec ... min";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test
    public void incrementalStreamProcessorTest3() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); ";

        String query = "" +
                " @info(name = 'query3') " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select sum(price) as sumPrice " +
                " group by price " +
                " aggregate every sec, min, hour, day";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test
    public void externalTimeTest1() throws InterruptedException {
        LOG.info("Incremental Processing: externalTimeTest1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price1 float, " +
                "                              price2 float, volume long , quantity int, timestamp long);";
        String query = "" +
                "define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1, (quantity * volume) as mult  " +
                "aggregate by timestamp every sec...year ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        // Thursday, June 1, 2017 4:05:50 AM
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        // Thursday, June 1, 2017 4:05:52 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        // Thursday, June 1, 2017 4:05:54 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

        // Thursday, June 1, 2017 4:05:56 AM
        inputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
        inputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

        // Thursday, June 1, 2017 4:06:56 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

        // Thursday, June 1, 2017 4:07:56 AM
        inputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

        // Thursday, June 1, 2017 5:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

        // Thursday, June 1, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

        // Friday, June 2, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

        // Saturday, June 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

        // Monday, July 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

        // Thursday, August 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

        // Friday, August 3, 2018 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

        // Saturday, August 3, 2019 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

        // Monday, August 3, 2020 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void externalTimeTest() throws InterruptedException {
        LOG.info("Incremental Processing: externalTimeTest");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price1 float, " +
                "                              price2 float, volume long , quantity int, timestamp long);";
        String query = "" +
                "define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1, (quantity * volume) as mult  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        // TODO: 6/29/17 Check with null later

        // Thursday, June 1, 2017 4:05:50 AM
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        // Thursday, June 1, 2017 4:05:52 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        // Thursday, June 1, 2017 4:05:54 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

        // Thursday, June 1, 2017 4:05:56 AM
        inputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
        inputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

        // Thursday, June 1, 2017 4:06:56 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

        // Thursday, June 1, 2017 4:07:56 AM
        inputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

        // Thursday, June 1, 2017 5:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

        // Thursday, June 1, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496297276000L});

        // Friday, June 2, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 800f, null, 100L, 10, 1496383676000L});

        // Saturday, June 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 900f, null, 100L, 15, 1496470076000L});

        // Monday, July 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1499062076000L});

        // Thursday, August 3, 2017 6:07:56 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1501740476000L});

        // Friday, August 3, 2018 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 6, 1533276476000L});

        // Saturday, August 3, 2019 6:07:56 AM
        inputHandler.send(new Object[]{"WSO2", 260f, 44f, 200L, 16, 1564812476000L});

        // Monday, August 3, 2020 6:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 260f, 44f, 200L, 16, 1596434876000L});

        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void eventTimeTest() throws InterruptedException {
        LOG.info("Incremental Processing: eventTimeTest");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price1 float, " +
                "                              price2 float, volume long , quantity int, timestamp long);";
        String query = " define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1, (quantity * volume) as mult  " +
                "group by symbol " + "aggregate every sec...hour ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});
        Thread.sleep(2000);

        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});
        Thread.sleep(2000);

        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});
        Thread.sleep(2000);

//        inputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
//        inputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});
//        Thread.sleep(60000);
//
//        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});
//        Thread.sleep(60000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeOutOfOrderTest1() throws InterruptedException {
        LOG.info("Incremental Processing: externalTimeOutOfOrderTest1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price1 float, " +
                "                              price2 float, volume long , quantity int, timestamp long);";
        String query = "" +
                "define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1, (quantity * volume) as mult  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        // Thursday, June 1, 2017 4:05:50 AM
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});

        // Thursday, June 1, 2017 4:05:52 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289952000L});
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289952000L});

        // Thursday, June 1, 2017 4:05:50 AM (out of order. must be processed with 1st event)
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        // Thursday, June 1, 2017 4:05:54 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289954000L});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289954000L});

        // Thursday, June 1, 2017 4:05:50 AM (out of order. should be processed for 1st second.
        // However, since 1st second's data has already been sent to next executor, this would
        // be processed with current sec data.
        inputHandler.send(new Object[]{"IBM", 50f, 60f, 90L, 6, 1496289950000L});

        // Thursday, June 1, 2017 4:05:56 AM
        inputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289956000L});
        inputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289956000L});

        // Thursday, June 1, 2017 4:06:56 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496290016000L});

        // Thursday, June 1, 2017 4:07:56 AM
        inputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496290076000L});

        // Thursday, June 1, 2017 5:07:56 AM
        inputHandler.send(new Object[]{"CISCO", 700f, null, 200L, 20, 1496293676000L});

        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void externalTimeOutOfOrderTest2() throws InterruptedException {
        LOG.info("Incremental Processing: externalTimeOutOfOrderTest2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price1 float, " +
                "                              price2 float, volume long , quantity int, timestamp long);";
        String query = "" +
                "define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1, (quantity * volume) as mult  " +
                "group by symbol " +
                "aggregate by timestamp every sec...year ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();

        // Thursday, June 1, 2017 4:05:50 AM
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90L, 6, 1496289950000L});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10, 1496289950000L});

        // Thursday, June 1, 2017 4:05:53 AM
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200L, 56, 1496289953000L});
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200L, 16, 1496289953000L});

        // Thursday, June 1, 2017 4:05:52 AM
        inputHandler.send(new Object[]{"IBM", 900f, null, 200L, 60, 1496289952000L});
        inputHandler.send(new Object[]{"IBM", 500f, null, 200L, 7, 1496289952000L});

        // Thursday, June 1, 2017 4:05:51 AM
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 26, 1496289951000L});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200L, 96, 1496289951000L});

        // Thursday, June 1, 2017 4:05:53 AM
        inputHandler.send(new Object[]{"IBM", 400f, null, 200L, 9, 1496289953000L});

        // Thursday, June 1, 2017 4:05:54 AM
        inputHandler.send(new Object[]{"IBM", 600f, null, 200L, 6, 1496289954000L});

        // Thursday, June 1, 2017 4:06:56 AM
        inputHandler.send(new Object[]{"IBM", 1000f, null, 200L, 9, 1496290016000L});

        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void incrementalAggregationTest1() {
        SiddhiManager siddhiManager = new SiddhiManager();

        String app = "" +
                " define stream cseEventStream (arrival long, symbol string, price float, volume int); " +
                " " +
                " define aggregation cseEventAggregation " +
                " from cseEventStream " +
                " select symbol, sum(price) as total, avg(price) as avgPrice " +
                " aggregate by arrival every sec ... min; " +
                "" +
                "define stream barStream (symbol string, value int); " +
                "" +
                "from barStream as b join cseEventAggregation as a " +
                "on a.symbol == b.symbol " +
                "within \"2014-02-15T00:00:00Z\", \"2014-03-16T00:00:00Z\" " +
                "per \"day\" " +
                "select a.symbol, a.total, a.avgPrice " +
                "insert into fooBar;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(app);
    }
}
