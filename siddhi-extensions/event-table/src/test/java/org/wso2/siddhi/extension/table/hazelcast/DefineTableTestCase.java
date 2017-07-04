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

package org.wso2.siddhi.extension.table.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.table.test.util.SiddhiTestHelper;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefineTableTestCase {
    private static final Logger log = Logger.getLogger(DefineTableTestCase.class);
    private static final long RESULT_WAIT = 500;
    private static List<String> instances;
    private AtomicInteger inEventCount = new AtomicInteger(0);
    private boolean eventArrived;
    private int removeEventCount;
    private List<Object[]> inEventsList;

    @Before
    public void init() throws InterruptedException {
        inEventCount.set(0);
        eventArrived = false;
        removeEventCount = 0;
        inEventsList = new ArrayList<Object[]>();
        instances = new ArrayList<String>();
        for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
            instances.add(instance.getName());
        }
    }

    @After
    public void cleanup() throws InterruptedException {
        for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
            if (!instances.contains(instance.getName())) {
                log.info("shutting down : " + instance.getName());
                instance.getLifecycleService().terminate();
            }
        }
        Thread.sleep(1000);
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTableDefinition1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        TableDefinition tableDefinition = TableDefinition
                .id("cseEventStream")
                .annotation(Annotation.annotation("store")
                        .element("type", "hazelcast"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testTableDefinition2 - OUT 0");
        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition
                .id("StockTable")
                .annotation(Annotation.annotation("store").element("type", "hazelcast"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.addQuery(query);
        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});

            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));
            HazelcastInstance instance = instanceMap.get(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName());
            List<StreamEvent> streamEvents = instance.getList(
                    HazelcastTableConstants.HAZELCAST_COLLECTION_PREFIX +
                            siddhiAppRuntime.getName() + '.' + tableDefinition.getId());

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents) {
                actual.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual, expected));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testTableDefinition3 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast')" +
                "define table Table(symbol string, price int, volume float) ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery4() throws InterruptedException {
        log.info("testTableDefinition4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, price int, volume float); " +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbols string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery5() throws InterruptedException {
        log.info("testTableDefinition5 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, volume float); " +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbols string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testTableDefinition6 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, price int, volume float); " +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery7() throws InterruptedException {
        log.info("testTableDefinition7 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "define stream TestTable(symbol string, price int, volume float); " +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(definitions);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery8() throws InterruptedException {
        log.info("testTableDefinition8 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "@store(type = 'hazelcast')" +
                "define table TestTable(symbol string, price int, volume float); " +
                "define stream TestTable(symbol string, price int, volume float); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(definitions);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testQuery9() throws InterruptedException {
        log.info("testTableDefinition9 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;" +
                "" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long); ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }


    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery10() throws InterruptedException {
        log.info("testTableDefinition10 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery11() throws InterruptedException {
        log.info("testTableDefinition11 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float); " +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long);" +
                "" +
                "from StockStream " +
                "select symbol, price " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery12() throws InterruptedException {
        log.info("testTableDefinition12 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery13() throws InterruptedException {
        log.info("testTableDefinition13 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery14() throws InterruptedException {
        log.info("testTableDefinition14 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float, time long); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery15() throws InterruptedException {
        log.info("testTableDefinition15 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume int); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test(expected = SiddhiAppValidationException.class)
    public void testQuery16() throws InterruptedException {
        log.info("testTableDefinition16 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@store(type = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from OutputStream " +
                "select symbol, price, volume " +
                "insert into StockStream;";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testQuery17() throws InterruptedException {
        log.info("testTableDefinition17 - OUT 0");

        String clusterName = "siddhi_cluster_t17";
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast', cluster.name = '" + clusterName + "')" +
                "define table Table(symbol string, price int, volume float) ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        try {
            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));

            HazelcastInstance instance = instanceMap.get(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName());
            Assert.assertEquals(clusterName, instance.getConfig().getGroupConfig().getName());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testQuery18() throws InterruptedException {
        log.info("testTableDefinition18 - OUT 0");

        String clusterName = "siddhi_cluster_t18";
        String clusterPassword = "cluster_pw";
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@store(type = 'hazelcast', cluster.name = '" + clusterName +
                "', cluster.password = '" + clusterPassword + "')" +
                "define table Table(symbol string, price int, volume float) ";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(tables);
        try {
            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName()));

            HazelcastInstance instance = instanceMap.get(HazelcastTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    siddhiAppRuntime.getName());
            Assert.assertEquals(clusterName, instance.getConfig().getGroupConfig().getName());
            Assert.assertEquals(clusterPassword, instance.getConfig().getGroupConfig().getPassword());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Ignore("Ignoring due to conflict with other test cases.")
    @Test
    public void testQuery19() throws InterruptedException {
        log.info("testTableDefinition19 - OUT 0");

        SiddhiManager siddhiManager1 = new SiddhiManager();
        String ep1 = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "@store(type = 'hazelcast', well.known.addresses = 'localhost', collection.name = 'stock')" +
                "@IndexBy('symbol') " +
                "define table StockTable (symbol string, price float, volume long); " +
                "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;";
        SiddhiAppRuntime siddhiAppRuntime1 = siddhiManager1.createSiddhiAppRuntime(ep1);

        SiddhiManager siddhiManager2 = new SiddhiManager();
        String ep2 = "" +
                "define stream StockCheckStream (symbol string); " +
                "@store(type = 'hazelcast', well.known.addresses = 'localhost', collection.name = 'stock')" +
                "@IndexBy('symbol') " +
                "define table StockTable (symbol string, price float, volume long); " +
                "" +
                "@info(name = 'query1') " +
                "from StockCheckStream[StockTable.symbol==StockCheckStream.symbol in StockTable] " +
                "insert into OutStream ;";
        SiddhiAppRuntime siddhiAppRuntime2 = siddhiManager2.createSiddhiAppRuntime(ep2);

        try {
            siddhiAppRuntime2.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timestamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = siddhiAppRuntime1.getInputHandler("StockStream");
            InputHandler stockCheckStream = siddhiAppRuntime2.getInputHandler("StockCheckStream");
            siddhiAppRuntime1.start();
            siddhiAppRuntime2.start();

            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 100l});

            Thread.sleep(RESULT_WAIT);
            stockCheckStream.send(new Object[]{"IBM"});
            stockCheckStream.send(new Object[]{"WSO2"});

            List<Object[]> expected = Arrays.asList(new Object[]{"IBM"}, new Object[]{"WSO2"});
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            siddhiAppRuntime1.shutdown();
            siddhiAppRuntime2.shutdown();
        }
    }

    @Ignore("Ignoring due to conflict with other test cases.")
    @Test
    public void testQuery20() throws InterruptedException {
        log.info("testTableDefinition20 - OUT 0");

        String clusterName = "siddhi_cluster_t20";
        String clusterPassword = "cluster_pw";
        String collectionName = "hzList";

        Config config_1 = new Config("instance_01");
        config_1.setProperty("hazelcast.logging.type", "log4j");
        config_1.setProperty("hazelcast.socket.keep.alive", "true");
        config_1.getGroupConfig().setName(clusterName);
        config_1.getGroupConfig().setPassword(clusterPassword);
        config_1.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config_1.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config_1.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost");
        HazelcastInstance instance_1 = Hazelcast.newHazelcastInstance(config_1);

        Config config_2 = new Config("instance_02");
        config_2.setProperty("hazelcast.logging.type", "log4j");
        config_2.setProperty("hazelcast.socket.keep.alive", "true");
        config_2.getGroupConfig().setName(clusterName);
        config_2.getGroupConfig().setPassword(clusterPassword);
        config_2.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config_2.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config_2.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost");
        HazelcastInstance instance_2 = Hazelcast.newHazelcastInstance(config_2);

        Thread.sleep(RESULT_WAIT);

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition.id("StockTable")
                .annotation(Annotation.annotation("store")
                        .element("type", "hazelcast")
                        .element("cluster.name", clusterName)
                        .element("cluster.password", clusterPassword)
                        .element("collection.name", collectionName)
                        .element("well.known.addresses", "localhost"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.addQuery(query);
        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});

            List<StreamEvent> streamEvents_1 = instance_1.getList(collectionName);
            List<StreamEvent> streamEvents_2 = instance_2.getList(collectionName);

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents_1, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual_1 = new ArrayList<Object[]>();
            List<Object[]> actual_2 = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents_1) {
                actual_1.add(event.getOutputData());
            }
            for (StreamEvent event : streamEvents_2) {
                actual_2.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents_1.size());
            Assert.assertEquals(2, streamEvents_2.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual_1, expected));
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual_2, expected));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testQuery21() throws InterruptedException {
        log.info("testTableDefinition21 - OUT 0");

        String instanceName = "siddhi_instance_t21";
        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition.id("StockTable")
                .annotation(Annotation.annotation("store")
                        .element("type", "hazelcast"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.addQuery(query);
        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});

            Config cfg = new Config(instanceName);
            cfg.setProperty("hazelcast.logging.type", "log4j");
            HazelcastInstance hci = Hazelcast.getOrCreateHazelcastInstance(cfg);
            List<StreamEvent> streamEvents = hci.getList(
                    HazelcastTableConstants.HAZELCAST_COLLECTION_PREFIX +
                            siddhiAppRuntime.getName() + '.' + tableDefinition.getId());

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents) {
                actual.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual, expected));
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Ignore("Ignoring due to conflict with other test cases.")
    @Test
    public void testQuery22() throws InterruptedException {
        log.info("testTableDefinition22 - OUT 0");

        String clusterName = "siddhi_cluster_t22";
        String clusterPassword = "cluster_pw";
        String collectionName = "siddhi_collection";

        Config cfg_1 = new Config("instance_1");
        cfg_1.getGroupConfig().setName(clusterName).setPassword(clusterPassword);
        cfg_1.setProperty("hazelcast.logging.type", "log4j");
        cfg_1.setProperty("hazelcast.socket.keep.alive", "false");
        HazelcastInstance instance_1 = Hazelcast.newHazelcastInstance(cfg_1);
        String address = instance_1.getCluster().getLocalMember().getSocketAddress().toString().replace("/", "");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition.id("StockTable")
                .annotation(Annotation.annotation("store")
                        .element("type", "hazelcast")
                        .element("cluster.name", clusterName)
                        .element("cluster.password", clusterPassword)
                        .element("collection.name", collectionName)
                        .element("cluster.addresses", address))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.addQuery(query);
        siddhiApp.defineStream(streamDefinition);
        siddhiApp.defineTable(tableDefinition);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        try {
            InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");
            siddhiAppRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});
            Thread.sleep(RESULT_WAIT * 4);

            List<StreamEvent> streamEvents = instance_1.getList(collectionName);
            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            Assert.assertEquals(2, streamEvents.size());
            instance_1.getLifecycleService().terminate();
            Thread.sleep(10000);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}