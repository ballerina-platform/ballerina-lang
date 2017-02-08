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
package org.wso2.siddhi.extension.output.mapper.wso2event;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.core.util.transport.InMemoryOutputTransport;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WSO2EventOutputMapperTestCase {
    static final Logger log = Logger.getLogger(WSO2EventOutputMapperTestCase.class);
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
    //    map wso2event
    @Test
    public void testMapOutputMapperWithQueryAPI() throws InterruptedException {
        log.info("Test default wso2 mapping with Siddhi Query API");
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
                .attribute("meta_symbol", Attribute.Type.STRING)
                .attribute("correlation_price", Attribute.Type.FLOAT)
                .attribute("volume", Attribute.Type.INT);

        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.select(
                Selector.selector().select(new Variable("meta_symbol")).select(new Variable("correlation_price")).select(new Variable("volume"))
        );
        query.publish(
                Transport.transport("inMemory").option("topic", "{{meta_symbol}}"), OutputStream.OutputEventType.CURRENT_EVENTS,
                Mapping.format("wso2event").option("streamID", "mappedStream:1.0.0")
        );

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemoryOutputTransport.class);
        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(streamDefinition);
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
    //    select symbol,price,volume
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map wso2event with options
    @Test
    public void testMapOutputMapperWithSiddhiQL() throws InterruptedException {
        List<Event> onMessageList = new ArrayList<Event>();
        log.info("Test default wso2 mapping with SiddhiQL using options");

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add((Event) msg);
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
                onMessageList.add((Event) msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "define stream FooStream (meta_symbol string, correlation_price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select meta_symbol,correlation_price,volume " +
                "publish inMemory options (topic '{{meta_symbol}}') " +
                "map wso2event options (streamID 'mappedStream:1.0.0'); ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemoryOutputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("mappedStream:1.0.0");
        event.setMetaData(new Object[]{"WSO2"});
        event.setCorrelationData(new Object[]{55.6f});
        event.setPayloadData(new Object[]{100L});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("mappedStream:1.0.0");
        event2.setMetaData(new Object[]{"IBM"});
        event2.setCorrelationData(new Object[]{75.6f});
        event2.setPayloadData(new Object[]{100L});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("mappedStream:1.0.0");
        event3.setMetaData(new Object[]{"WSO2"});
        event3.setCorrelationData(new Object[]{57.6f});
        event3.setPayloadData(new Object[]{100L});
        eventList.add(event3);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert event mapping
        for (Event aEvent : onMessageList) {
            aEvent.setTimeStamp(0);
        }
        Assert.assertEquals("Mapping is incorrect!", onMessageList, eventList);
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    //    from FooStream
    //    select symbol,price,volume
    //    publish inMemory options ("topic", "{{symbol}}")
    //    map wso2event with dynamic options
    @Test
    public void testMapOutputMapperWithSiddhiQLWithDynamicOptions() throws InterruptedException {
        List<Event> onMessageList = new ArrayList<Event>();
        log.info("Test default wso2 mapping with SiddhiQL using dynamic options");

        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                wso2Count.incrementAndGet();
                onMessageList.add((Event) msg);
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
                onMessageList.add((Event) msg);
            }

            @Override
            public String getTopic() {
                return "IBM";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);
        InMemoryBroker.subscribe(subscriberIBM);

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "define stream FooStream (meta_symbol string, correlation_price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select meta_symbol,correlation_price,volume " +
                "publish inMemory options (topic '{{meta_symbol}}') " +
                "map wso2event options (streamID '{{meta_symbol}}Stream:1.0.0'); ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemoryOutputTransport.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setStreamId("WSO2Stream:1.0.0");
        event.setMetaData(new Object[]{"WSO2"});
        event.setCorrelationData(new Object[]{55.6f});
        event.setPayloadData(new Object[]{100L});
        eventList.add(event);
        Event event2 = new Event();
        event2.setStreamId("IBMStream:1.0.0");
        event2.setMetaData(new Object[]{"IBM"});
        event2.setCorrelationData(new Object[]{75.6f});
        event2.setPayloadData(new Object[]{100L});
        eventList.add(event2);
        Event event3 = new Event();
        event3.setStreamId("WSO2Stream:1.0.0");
        event3.setMetaData(new Object[]{"WSO2"});
        event3.setCorrelationData(new Object[]{57.6f});
        event3.setPayloadData(new Object[]{100L});
        eventList.add(event3);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert event mapping
        for (Event aEvent : onMessageList) {
            aEvent.setTimeStamp(0);
        }
        Assert.assertEquals("Mapping is incorrect!", onMessageList, eventList);
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }
}