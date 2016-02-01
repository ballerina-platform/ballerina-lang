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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.eventtable.test.util.SiddhiTestHelper;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.*;

public class DefineTableTestCase {
    private static final Logger log = Logger.getLogger(DefineTableTestCase.class);
    private static final long RESULT_WAIT = 500;

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTableDefinition1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        TableDefinition tableDefinition = TableDefinition
                .id("cseEventStream")
                .annotation(Annotation.annotation("from")
                        .element("eventtable", "hazelcast"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineTable(tableDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));
        } finally {
            executionPlanRuntime.shutdown();
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
                .annotation(Annotation.annotation("from").element("eventtable", "hazelcast"))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.addQuery(query);
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineTable(tableDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});

            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));
            HazelcastInstance instance = instanceMap.get(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName());
            List<StreamEvent> streamEvents = instance.getList(
                    HazelcastEventTableConstants.HAZELCAST_LIST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName() + '_' + tableDefinition.getId());

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents) {
                actual.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual, expected));
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("testTableDefinition3 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery4() throws InterruptedException {
        log.info("testTableDefinition4 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbols string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery5() throws InterruptedException {
        log.info("testTableDefinition5 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbols string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery6() throws InterruptedException {
        log.info("testTableDefinition6 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        try {
            List<String> hciNames = new ArrayList<String>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                hciNames.add(hci.getName());
            }
            Assert.assertTrue(hciNames.contains(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery7() throws InterruptedException {
        log.info("testTableDefinition7 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "define stream TestEventTable(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(definitions);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery8() throws InterruptedException {
        log.info("testTableDefinition8 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); " +
                "define stream TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(definitions);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testQuery9() throws InterruptedException {
        log.info("testTableDefinition9 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;" +
                "" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }


    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery10() throws InterruptedException {
        log.info("testTableDefinition10 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery11() throws InterruptedException {
        log.info("testTableDefinition11 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price float, volume long);" +
                "" +
                "from StockStream " +
                "select symbol, price " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery12() throws InterruptedException {
        log.info("testTableDefinition12 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select symbol, price, volume " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery13() throws InterruptedException {
        log.info("testTableDefinition13 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery14() throws InterruptedException {
        log.info("testTableDefinition14 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float, time long); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery15() throws InterruptedException {
        log.info("testTableDefinition15 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume int); " +
                "" +
                "from StockStream " +
                "select * " +
                "insert into OutputStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testQuery16() throws InterruptedException {
        log.info("testTableDefinition16 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = "" +
                "define stream StockStream(symbol string, price int, volume float);" +
                "@from(eventtable = 'hazelcast')" +
                "define table OutputStream (symbol string, price int, volume float); " +
                "" +
                "from OutputStream " +
                "select symbol, price, volume " +
                "insert into StockStream;";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery17() throws InterruptedException {
        log.info("testTableDefinition17 - OUT 0");

        String clusterName = "siddhi_cluster_t17";
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast', cluster.name = '" + clusterName + "')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        try {
            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));

            HazelcastInstance instance = instanceMap.get(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName());
            Assert.assertEquals(clusterName, instance.getConfig().getGroupConfig().getName());
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void testQuery18() throws InterruptedException {
        log.info("testTableDefinition18 - OUT 0");

        String clusterName = "siddhi_cluster_t18";
        String clusterPassword = "cluster_pw";
        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast', cluster.name = '" + clusterName +
                "', cluster.password = '" + clusterPassword + "')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        try {
            Map<String, HazelcastInstance> instanceMap = new HashMap<String, HazelcastInstance>();
            for (HazelcastInstance hci : Hazelcast.getAllHazelcastInstances()) {
                instanceMap.put(hci.getName(), hci);
            }
            Assert.assertTrue(instanceMap.containsKey(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName()));

            HazelcastInstance instance = instanceMap.get(HazelcastEventTableConstants.HAZELCAST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName());
            Assert.assertEquals(clusterName, instance.getConfig().getGroupConfig().getName());
            Assert.assertEquals(clusterPassword, instance.getConfig().getGroupConfig().getPassword());
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void testQuery19() throws InterruptedException {
        log.info("testTableDefinition19 - OUT 0");

        String clusterName = "siddhi_cluster_t19";
        String clusterPassword = "cluster_pw";

        Config cfg_1 = new Config();
        cfg_1.getGroupConfig().setName(clusterName).setPassword(clusterPassword);
        cfg_1.setProperty("hazelcast.logging.type", "log4j");
        HazelcastInstance instance_1 = Hazelcast.newHazelcastInstance(cfg_1);

        Config cfg_2 = new Config();
        cfg_2.getGroupConfig().setName(clusterName).setPassword(clusterPassword);
        cfg_2.setProperty("hazelcast.logging.type", "log4j");
        HazelcastInstance instance_2 = Hazelcast.newHazelcastInstance(cfg_2);

        StringBuilder sb = new StringBuilder();
        for (Member member : instance_2.getCluster().getMembers()) {
            sb.append(member.getSocketAddress()).append(',');
        }
        String addresses = StringUtils.replace(sb.toString(), "/", "");
        addresses = StringUtils.removeEnd(addresses, ",");

        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition.id("StockTable")
                .annotation(Annotation.annotation("from")
                        .element("eventtable", "hazelcast")
                        .element("cluster.name", clusterName)
                        .element("cluster.password", clusterPassword)
                        .element("cluster.addresses", addresses))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.addQuery(query);
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineTable(tableDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});
            List<StreamEvent> streamEvents = instance_2.getList(
                    HazelcastEventTableConstants.HAZELCAST_LIST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName() + '_' + tableDefinition.getId());

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents) {
                actual.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual, expected));
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void testQuery20() throws InterruptedException {
        log.info("testTableDefinition20 - OUT 0");

        String instanceName = "siddhi_instance_t20";
        SiddhiManager siddhiManager = new SiddhiManager();
        StreamDefinition streamDefinition = StreamDefinition
                .id("StockStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        TableDefinition tableDefinition = TableDefinition.id("StockTable")
                .annotation(Annotation.annotation("from")
                        .element("eventtable", "hazelcast")
                        .element("instance.name", instanceName))
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT);
        Query query = Query.query();
        query.from(InputStream.stream("StockStream"));
        query.insertInto("StockTable");
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.addQuery(query);
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineTable(tableDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        try {
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f});
            stockStream.send(new Object[]{"IBM", 75.6f});

            Config cfg = new Config(instanceName);
            cfg.setProperty("hazelcast.logging.type", "log4j");
            HazelcastInstance hci = Hazelcast.getOrCreateHazelcastInstance(cfg);
            List<StreamEvent> streamEvents = hci.getList(
                    HazelcastEventTableConstants.HAZELCAST_LIST_INSTANCE_PREFIX +
                    executionPlanRuntime.getName() + '_' + tableDefinition.getId());

            SiddhiTestHelper.waitForEvents(100, 2, streamEvents, 60000);
            List<Object[]> expected = Arrays.asList(new Object[]{"WSO2", 55.6f}, new Object[]{"IBM", 75.6f});
            List<Object[]> actual = new ArrayList<Object[]>();
            for (StreamEvent event : streamEvents) {
                actual.add(event.getOutputData());
            }
            Assert.assertEquals(2, streamEvents.size());
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(actual, expected));
        } finally {
            executionPlanRuntime.shutdown();
        }
    }
}