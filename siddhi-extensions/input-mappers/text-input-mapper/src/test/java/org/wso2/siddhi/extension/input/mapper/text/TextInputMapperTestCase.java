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

package org.wso2.siddhi.extension.input.mapper.text;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;

import java.util.concurrent.atomic.AtomicInteger;

public class TextInputMapperTestCase {
    static final Logger log = Logger.getLogger(TextInputMapperTestCase.class);

    private AtomicInteger count = new AtomicInteger();

    @Before
    public void init() {
        count.set(0);
    }

    /**
     * Expected input format:
     * WSO2,56.75,5
     */
    @Test
    public void testTextInputMapper1() throws InterruptedException {
        log.info("test TextInputMapper1");

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "@source(type='inMemory', topic='stock', @map(type='text')) " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);

        executionPlanRuntime.addCallback("BarStream", new StreamCallback() {

            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    switch (count.incrementAndGet()) {
                        case 1:
                            junit.framework.Assert.assertEquals(55.6f, event.getData(1));
                            break;
                        case 2:
                            junit.framework.Assert.assertEquals(75.6f, event.getData(1));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        InMemoryBroker.publish("stock", "WSO2,55.6,100");
        InMemoryBroker.publish("stock", "IBM,75.6,10");
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Number of events", 2, count.get());
        executionPlanRuntime.shutdown();
    }
// TODO: 2/11/17 Fix these
//    /**
//     * Expected input format:
//     * WSO2,56.75,5,Sri Lanka
//     */
//    @Test
//    public void subscriptionTest8() throws InterruptedException {
//        log.info("Subscription Test 8: Test an in memory transport with custom text mapping");
//
//        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic", "stock"));
//        subscription.map(Mapping.format("text").map("regex1[1]").map("regex1[2]").map("regex1[3]").option("regex1",
// "" +
//                "([^,;]+),([^,;]+),([^,;]+),([^,;]+)"));
//        subscription.insertInto("FooStream");
//
//        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
//        executionPlan.defineStream(StreamDefinition.id("FooStream")
//                .attribute("symbol", Attribute.Type.STRING)
//                .attribute("price", Attribute.Type.FLOAT)
//                .attribute("volume", Attribute.Type.INT));
//        executionPlan.addSubscription(subscription);
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
//        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
//            @Override
//            public void receive(Event[] events) {
//                EventPrinter.print(events);
//            }
//        });
//
//        executionPlanRuntime.start();
//
//        InMemoryBroker.publish("stock", "WSO2,56.75,5,Sri Lanka");
//        InMemoryBroker.publish("stock", "IBM,75.6,10,USA");
//
//        executionPlanRuntime.shutdown();
//    }
//
//    /**
//     * Expected input format:
//     * symbol=WSO2, price=56.75, volume=5, country=Sri Lanka
//     */
//    @Test
//    public void subscriptionTest9() throws InterruptedException {
//        log.info("Subscription Test 9: Test an in memory transport with custom text mapping");
//
//        Subscription subscription = Subscription.Subscribe(Transport.transport("inMemory").option("topic", "stock"));
//        subscription.map(Mapping.format("text").map("regex1[2]").map("regex2[2]").map("regex3[2]")
//                .option("regex1", "(symbol=)([^,;]+)")
//                .option("regex2", "(price=)([^,;]+)")
//                .option("regex3", "(volume=)([^,;]+)"));
//        subscription.insertInto("FooStream");
//
//        ExecutionPlan executionPlan = ExecutionPlan.executionPlan();
//        executionPlan.defineStream(StreamDefinition.id("FooStream")
//                .attribute("symbol", Attribute.Type.STRING)
//                .attribute("price", Attribute.Type.FLOAT)
//                .attribute("volume", Attribute.Type.INT));
//        executionPlan.addSubscription(subscription);
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        siddhiManager.setExtension("inputtransport:inMemory", InMemoryInputTransport.class);
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
//        executionPlanRuntime.addCallback("FooStream", new StreamCallback() {
//            @Override
//            public void receive(Event[] events) {
//                EventPrinter.print(events);
//            }
//        });
//
//        executionPlanRuntime.start();
//
//        InMemoryBroker.publish("stock", "price=56.75, volume=5, symbol=WSO2, country=Sri Lanka");
//
//        executionPlanRuntime.shutdown();
//    }
}
