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
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.NoSuchAttributeException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.sink.InMemorySink;
import org.wso2.siddhi.core.util.transport.InMemoryBroker;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonSinkMapperTestCase {
    static final Logger log = Logger.getLogger(JSONOutputMapperWithSiddhiQueryAPITestCase.class);
    private AtomicInteger wso2Count = new AtomicInteger(0);
    private AtomicInteger ibmCount = new AtomicInteger(0);

    @Before
    public void init() {
        wso2Count.set(0);
        ibmCount.set(0);
    }

    /*
    * Default json output mapping
    */
    @Test
    public void jsonSinkMapperTestCase1() throws InterruptedException {
        log.info("jsonSinkMapperTestCase1 :");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                String jsonString;
                switch (wso2Count.incrementAndGet()) {
                    case 1:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 2:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":57.678,\"volume\":100}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 3:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":50.0,\"volume\":100}}";
                        Assert.assertEquals(jsonString,msg);
                        break;
                    case 4:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2#$%\",\"price\":50.0,\"volume\":100}}";
                        Assert.assertEquals(jsonString,msg);
                        break;
                    case 5:
                        jsonString = "[{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}," +
                                "{\"event\":{\"symbol\":\"IBM\",\"price\":32.6,\"volume\":160}}]";
                        Assert.assertEquals(jsonString,msg);
                        break;
                    default:
                        Assert.fail();
                }
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='WSO2', @map(type='json')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        Event wso2Event = new Event();
        Event ibmEvent = new Event();
        Object[] wso2Data = {"WSO2", 55.6f, 100L};
        Object[] ibmData = {"IBM", 32.6f, 160L};

        wso2Event.setData(wso2Data);
        ibmEvent.setData(ibmData);
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.678f, 100L});
        stockStream.send(new Object[]{"WSO2", 50f, 100L});
        stockStream.send(new Object[]{"WSO2#$%", 50f, 100L});
        stockStream.send(new Event[]{wso2Event, ibmEvent});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals(5, wso2Count.get());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
    }

    @Test
    public void jsonSinkMapperTestCase2() throws InterruptedException {
        log.info("jsonSinkMapperTestCase2 :");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                String jsonString;
                switch (wso2Count.incrementAndGet()) {
                    case 1:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":\"undefined\"}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 2:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":\"undefined\",\"volume\":100}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 3:
                        jsonString = "{\"event\":{\"symbol\":\"undefined\",\"price\":55.6,\"volume\":100}}";
                        Assert.assertEquals(jsonString,msg);
                        break;
                    default:
                        Assert.fail();
                }
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='WSO2', @map(type='json')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, null});
        stockStream.send(new Object[]{"WSO2", null, 100L});
        stockStream.send(new Object[]{null, 55.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals(3, wso2Count.get());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
    }

    @Test
    public void jsonSinkMapperTestCase3() throws InterruptedException {
        log.info("jsonSinkMapperTestCase3 :");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                String jsonString;
                switch (wso2Count.incrementAndGet()) {
                    case 1:
                        jsonString = "{\"portfolio\":" +
                                "{\"company\":{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 2:
                        jsonString = "{\"portfolio\":" +
                                "{\"company\":{\"event\":{\"symbol\":\"WSO2\",\"price\":56.6,\"volume\":200}}}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 3:
                        jsonString = "{\"portfolio\":" +
                                "{\"company\":{\"event\":{\"symbol\":\"WSO2\",\"price\":57.6,\"volume\":300}}}}";
                        Assert.assertEquals(jsonString,msg);
                        break;
                    default:
                        Assert.fail();
                }
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        //subscribe to "inMemory" broker per topic
        InMemoryBroker.subscribe(subscriberWSO2);

        String streams = "" +
                "@Plan:name('TestExecutionPlan')" +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='WSO2', @map(type='json', " +
                "enclosing.element=\"$.portfolio.company\")) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 56.6f, 200L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 300L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals(3, wso2Count.get());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
    }

    @Test
    public void jsonSinkMapperTestCase4() throws InterruptedException {
        log.info("jsonSinkMapperTestCase3 :");
        InMemoryBroker.Subscriber subscriberWSO2 = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                System.out.println(msg);
                String jsonString;
                switch (wso2Count.incrementAndGet()) {
                    case 1:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":55.6,\"volume\":100}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    case 2:
                        jsonString = "{\"event\":{\"symbol\":\"WSO2\",\"price\":56.6,\"volume\":101}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    default:
                        Assert.fail();
                }
            }

            @Override
            public String getTopic() {
                return "WSO2";
            }
        };

        InMemoryBroker.Subscriber subscriberIBM = new InMemoryBroker.Subscriber() {
            @Override
            public void onMessage(Object msg) {
                System.out.println(msg);
                String jsonString;
                switch (ibmCount.incrementAndGet()) {
                    case 1:
                        jsonString = "{\"event\":{\"symbol\":\"IBM\",\"price\":75.6,\"volume\":200}}";
                        Assert.assertEquals(jsonString, msg);
                        break;
                    default:
                        Assert.fail();
                }
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
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', validate.json='true')) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 56.6f, 101L});
        stockStream.send(new Object[]{"IBM", 75.6f, 200L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals(2, wso2Count.get());
        Assert.assertEquals(1, ibmCount.get());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }



    /*
    * Custom json output mapping
    */
    @Test
    public void jsonSinkMapperTestCase5() throws InterruptedException {
        log.info("jsonSinkMapperTestCase5 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', validate.json='true', " +
                "enclosing.element=\"$.portfolio.company\", " +
                "@payload(\"\"\"{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"{{symbol}}\",\n" +
                "      \"Price\":{{price}}\n" +
                "   }\n" +
                "}\"\"\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
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
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":55.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(0).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"IBM\",\n" +
                "      \"Price\":75.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(1).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":57.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(2).toString());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    @Test
    public void jsonSinkMapperTestCase6() throws InterruptedException {
        log.info("jsonSinkMapperTestCase6 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', " +
                "validate.json='true', " +
                "@payload(\"{'StockData':{'Symbol':{{symbol}}},'Price':{{{price}}\")))" +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 0, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 0, ibmCount.get());

        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    @Test
    public void jsonSinkMapperTestCase7() throws InterruptedException {
        log.info("jsonSinkMapperTestCase7 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', " +
                "validate.json='false', " +
                "@payload(\"{'StockData':{'Symbol':{{symbol}}},'Price':{{{price}}\")))" +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
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
        Assert.assertEquals("Mapping incorrect!", "{'StockData:{'Symbol:WSO2},Price:{55.6",
                onMessageList.get(0).toString());
        Assert.assertEquals("Mapping incorrect!", "{'StockData:{'Symbol:IBM},Price:{75.6",
                onMessageList.get(1).toString());
        Assert.assertEquals("Mapping incorrect!", "{'StockData:{'Symbol:WSO2},Price:{57.6",
                onMessageList.get(2).toString());

        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    @Test
    public void jsonSinkMapperTestCase8() throws InterruptedException {
        log.info("jsonSinkMapperTestCase8 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', validate.json='true', " +
                "enclosing.element=\"$.portfolio.company\", " +
                "@payload(\"\"\"{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"{{symbol}}\",\n" +
                "      \"Price\":{{non-exist}}\n" +
                "   }\n" +
                "}\"\"\"))) " +
                "define stream BarStream (symbol string, price float, volume long); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        try {
            siddhiManager.createExecutionPlanRuntime(streams + query);
        }catch (Exception e){
            Assert.assertEquals(NoSuchAttributeException.class,e.getClass());
        }
        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    @Test
    public void jsonSinkMapperTestCase9() throws InterruptedException {
        log.info("jsonSinkMapperTestCase9 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long, country string); " +
                "@sink(type='inMemory', topic='{{symbol}}', @map(type='json', validate.json='true', " +
                "enclosing.element=\"$.portfolio.company\", " +
                "@payload(\"\"\"{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"{{symbol}}\",\n" +
                "      \"Price\":{{price}}\n" +
                "   }\n" +
                "}\"\"\"))) " +
                "define stream BarStream (symbol string, price float, volume long, country string); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L, "SL"});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L, "USA"});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L, "SL"});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom json
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":55.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(0).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"IBM\",\n" +
                "      \"Price\":75.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(1).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":57.6\n" +
                "   }\n" +
                "}}}", onMessageList.get(2).toString());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }

    @Test
    public void jsonSinkMapperTestCase10() throws InterruptedException {
        log.info("jsonSinkMapperTestCase10 :");
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

        String streams = "" +
                "@Plan:name('TestExecutionPlan') " +
                "define stream FooStream (symbol string, price float, volume long, company string); " +
                "@sink(type='inMemory', topic='{{company}}', @map(type='json', validate.json='true', " +
                "enclosing.element=\"$.portfolio.company\", " +
                "@payload(\"\"\"{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"{{symbol}}\",\n" +
                "      \"Price\":{{price}},\n" +
                "      \"Volume\":{{volume}}\n" +
                "   }\n" +
                "}\"\"\"))) " +
                "define stream BarStream (symbol string, price float, volume long, company string); ";

        String query = "" +
                "from FooStream " +
                "select * " +
                "insert into BarStream; ";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputtransport:inMemory", InMemorySink.class);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, null, "WSO2"});
        stockStream.send(new Object[]{"IBM", null, 500L, "IBM"});
        stockStream.send(new Object[]{null, 57.6f, 200L, "WSO2"});
        Thread.sleep(100);

        //assert event count
        Assert.assertEquals("Incorrect number of events consumed!", 2, wso2Count.get());
        Assert.assertEquals("Incorrect number of events consumed!", 1, ibmCount.get());
        //assert custom json
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"WSO2\",\n" +
                "      \"Price\":55.6,\n" +
                "      \"Volume\":undefined\n" +
                "   }\n" +
                "}}}", onMessageList.get(0).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"IBM\",\n" +
                "      \"Price\":undefined,\n" +
                "      \"Volume\":500\n" +
                "   }\n" +
                "}}}", onMessageList.get(1).toString());
        Assert.assertEquals("Mapping incorrect!", "{\"portfolio\":{\"company\":{\n" +
                "   \"Stock Data\":{\n" +
                "      \"Symbol\":\"undefined\",\n" +
                "      \"Price\":57.6,\n" +
                "      \"Volume\":200\n" +
                "   }\n" +
                "}}}", onMessageList.get(2).toString());
        executionPlanRuntime.shutdown();

        //unsubscribe from "inMemory" broker per topic
        InMemoryBroker.unsubscribe(subscriberWSO2);
        InMemoryBroker.unsubscribe(subscriberIBM);
    }
}