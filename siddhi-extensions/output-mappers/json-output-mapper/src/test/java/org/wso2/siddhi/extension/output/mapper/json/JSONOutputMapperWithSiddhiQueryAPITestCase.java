/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.output.mapper.json;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JSONOutputMapperWithSiddhiQueryAPITestCase {
    private static final Logger log = Logger.getLogger(JSONOutputMapperWithSiddhiQueryAPITestCase.class);
    private AtomicInteger wso2Count = new AtomicInteger(0);
    private AtomicInteger ibmCount = new AtomicInteger(0);

    @Before
    public void init() {
        wso2Count.set(0);
        ibmCount.set(0);
    }

    //    from FooStream
    //    select symbol,price,volume
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map json
    @Test
    public void testJSONOutputMapperWithDefaultMapping() throws InterruptedException {
        log.info("Test default json mapping with Siddhi Query API");

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        StreamDefinition streamDefinition = StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT);

        StreamDefinition outputDefinition = StreamDefinition.id("BarStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT)
                .annotation(Annotation.annotation("sink")
                        .element("type", "inMemory")
                        .element("topic", "{{symbol}}")
                        .annotation(Annotation.annotation("map")
                                .element("type", "json")));

        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.select(
                Selector.selector().select(new Variable("symbol")).select(new Variable("price")).select(new Variable
                        ("volume"))
        );
        query.insertInto("BarStream");


        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(outputDefinition);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    //    from FooStream
    //    select symbol,price
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map json custom
    @Test
    public void testJSONOutputMapperWithCustomMapping() throws InterruptedException {
        log.info("Test custom json mapping with Siddhi Query API");
        List<Object> onMessageList = new ArrayList<Object>();

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                ibmCount.incrementAndGet();
                onMessageList.add(msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        StreamDefinition streamDefinition = StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT);

        StreamDefinition outputDefinition = StreamDefinition.id("BarStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT)
                .annotation(Annotation.annotation("sink")
                        .element("type", "inMemory")
                        .element("topic", "{{symbol}}")
                        .annotation(Annotation.annotation("map")
                                .element("type", "json")
                                .annotation(Annotation.annotation("payload").element("{\n" +
                                        "   \"Stock Data\":{\n" +
                                        "      \"Symbol\":\"{{symbol}}\",\n" +
                                        "      \"Price\":{{price}}\n" +
                                        "   }\n" +
                                        "}"))));

        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.select(
                Selector.selector().select(new Variable("symbol")).select(new Variable("price")).select(new Variable
                        ("volume"))
        );
        query.insertInto("BarStream");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(streamDefinition);
        executionPlan.defineStream(outputDefinition);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom json
        Assert.assertEquals("Mapping incorrect!", "{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":55.6\n" +
                "   }\n" +
                "}", onMessageList.get(0).toString());
        Assert.assertEquals("Mapping incorrect!", "{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"IBM\",\n" +
                "      \"Price\":75.6\n" +
                "   }\n" +
                "}", onMessageList.get(1).toString());
        Assert.assertEquals("Mapping incorrect!", "{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":57.6\n" +
                "   }\n" +
                "}", onMessageList.get(2).toString());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }
}
