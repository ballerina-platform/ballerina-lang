/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.wso2.siddhi.extension.eventtable.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;


public class DefineTableTestCase {
    // TODO : Use asserts where possible
    static final Logger log = Logger.getLogger(DefineTableTestCase.class);

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("testTableDefinition1 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();

        TableDefinition tableDefinition = TableDefinition.id("cseEventStream").annotation(Annotation.annotation("from").element("eventtable", "hazelcast")).attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT);

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineTable(tableDefinition);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("testTableDefinition2 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery3() throws InterruptedException {
        log.info("testTableDefinition3 - OUT 0");

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
    public void testQuery4() throws InterruptedException {
        log.info("testTableDefinition4 - OUT 0");

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
    public void testQuery5() throws InterruptedException {
        log.info("testTableDefinition5 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery6() throws InterruptedException {
        log.info("testTableDefinition6 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "define stream TestEventTable(symbol string, price int, volume float); " +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(definitions);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = DuplicateDefinitionException.class)
    public void testQuery7() throws InterruptedException {
        log.info("testTableDefinition7 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String definitions = "" +
                "@from(eventtable = 'hazelcast')" +
                "define table TestEventTable(symbol string, price int, volume float); " +
                "define stream TestEventTable(symbol string, price int, volume float); ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(definitions);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = SiddhiParserException.class)
    public void testQuery8() throws InterruptedException {
        log.info("testTableDefinition8 - OUT 0");

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
    public void testQuery9() throws InterruptedException {
        log.info("testTableDefinition9 - OUT 0");

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
    public void testQuery10() throws InterruptedException {
        log.info("testTableDefinition10 - OUT 0");

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
    public void testQuery11() throws InterruptedException {
        log.info("testTableDefinition11 - OUT 0");

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
    public void testQuery12() throws InterruptedException {
        log.info("testTableDefinition12 - OUT 0");

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
    public void testQuery13() throws InterruptedException {
        log.info("testTableDefinition13 - OUT 0");

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
    public void testQuery14() throws InterruptedException {
        log.info("testTableDefinition14 - OUT 0");

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
    public void testQuery15() throws InterruptedException {
        log.info("testTableDefinition15 - OUT 0");

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
    public void testQuery16() throws InterruptedException {
        log.info("testTableDefinition16 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast', cluster.name = 'siddhi_cluster_t16')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery17() throws InterruptedException {
        log.info("testTableDefinition17 - OUT 0");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast', cluster.name = 'siddhi_cluster_t17', cluster.password = 'cluster_pw')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testQuery18() throws InterruptedException {
        log.info("testTableDefinition18 - OUT 0");

        Config cfg_1 = new Config();
        cfg_1.getGroupConfig().setName("siddhi_cluster_t18").setPassword("cluster_pw");
        cfg_1.setProperty("hazelcast.logging.type", "log4j");
        HazelcastInstance instance_1 = Hazelcast.newHazelcastInstance(cfg_1);

        Config cfg_2 = new Config();
        cfg_2.getGroupConfig().setName("siddhi_cluster_t18").setPassword("cluster_pw");
        cfg_2.setProperty("hazelcast.logging.type", "log4j");
        HazelcastInstance instance_2 = Hazelcast.newHazelcastInstance(cfg_2);

        StringBuilder sb = new StringBuilder();
        for (Member member : instance_2.getCluster().getMembers()) {
            sb.append(member.getSocketAddress()).append(',');
        }
        String addresses = StringUtils.replace(sb.toString(), "/", "");
        addresses = StringUtils.removeEnd(addresses, ",");

        SiddhiManager siddhiManager = new SiddhiManager();
        String tables = "" +
                "@from(eventtable = 'hazelcast', cluster.name = 'siddhi_cluster_t18', cluster.password = 'cluster_pw', cluster.addresses = '" + addresses + "')" +
                "define table EventTable(symbol string, price int, volume float) ";
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(tables);
        executionPlanRuntime.shutdown();
    }
}