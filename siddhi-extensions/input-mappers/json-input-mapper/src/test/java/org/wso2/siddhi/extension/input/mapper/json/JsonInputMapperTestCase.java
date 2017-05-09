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

package org.wso2.siddhi.extension.input.mapper.json;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.source.InMemoryInputTransport;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.core.stream.input.source.InMemorySource;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.Subscription;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

public class JsonSourcemapperTestCase {
    static final Logger log = Logger.getLogger(JsonSourcemapperTestCase.class);

    /**
     * Expected input format:
     * {'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}
     */
    @Test
    public void subscriptionTest1() throws InterruptedException {
        log.info("Subscription Test 1: Test an in memory source with default json mapping");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options (topic 'stock') " +
                        "map json " +
                        "insert into FooStream;");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest2() throws InterruptedException {
        log.info("Subscription Test 2: Test an in memory source with named and positional json mapping - expect " +
                "exception");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options (topic 'stock') " +
                        "map json '$.country', '$.price', '$.volume' as 'volume' " +
                        "insert into FooStream;");
        // First two parameters are based on position and the last one is named parameter

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    /**
     * Expected input format:
     * {'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}
     */
    @Test
    public void subscriptionTest3() throws InterruptedException {
        log.info("Subscription Test 3: Test an in memory sourcesource with custom positional json mapping");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options(topic 'stock') " +
                        "map json '$.symbol', '$.price', '$.volume' " +
                        "insert into FooStream;");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("output_symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    /**
     * Expected input format:
     * {'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}
     */
    @Test
    public void subscriptionTest4() throws InterruptedException {
        log.info("Subscription Test 4: Test an in memory source with custom named json mapping");

        Subscription subscription = SiddhiCompiler.parseSubscription(
                "subscribe inMemory options(topic 'stock') " +
                        "map json '$.volume' as 'volume', '$.symbol' as 'symbol', '$.price' as 'price' " +
                        "insert into FooStream;");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.defineStream(StreamDefinition.id("FooStream")
                .attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT));
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'country': 'Sri Lanka', 'symbol': 'WSO2', 'price': 56.75, 'volume': 5}");

        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest5() throws InterruptedException {
        log.info("Subscription Test 5: Test infer output stream using json mapping - expect exception");

        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic", "stock"));
        subscription.map(Mapping.format("json").map("volume", "$.volume").map("symbol", "$.symbol").map("price", "$" +
                ".price"));
        subscription.insertInto("FooStream");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void subscriptionTest6() throws InterruptedException {
        log.info("Subscription Test 6: Test error in infer output stream using json mapping without mapping name" +
                " - expect exception");

        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic", "stock"));
        subscription.map(Mapping.format("json").map("$.volume").map("$.symbol").map("$.price"));
        subscription.insertInto("FooStream");

        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
        executionPlan.addSubscription(subscription);

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("source:inMemory", InMemorySource.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "{'symbol': 'WSO2', 'price': 56.75, 'volume': 5, 'country': 'Sri Lanka'}");

        executionPlanRuntime.shutdown();
    }
}
