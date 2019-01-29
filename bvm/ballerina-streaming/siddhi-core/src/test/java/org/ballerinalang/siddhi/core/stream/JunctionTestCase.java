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
package org.ballerinalang.siddhi.core.stream;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.config.SiddhiContext;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.stream.output.StreamCallback;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Testcase for stream junction validation.
 */
public class JunctionTestCase {
    private static final Logger log = LoggerFactory.getLogger(JunctionTestCase.class);
    private int count;
    private boolean eventArrived;
    private ExecutorService executorService;
    private SiddhiAppContext siddhiAppContext;

    @BeforeMethod
    public void init() {
        count = 0;
        eventArrived = false;
        executorService = Executors.newCachedThreadPool();
        SiddhiContext siddhiContext = new SiddhiContext();
        siddhiAppContext = new SiddhiAppContext();
        siddhiAppContext.setSiddhiContext(siddhiContext);
    }


    @Test
    public void junctionToReceiverTest() throws InterruptedException {
        log.info("junction to receiver");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("parallel"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, siddhiAppContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamCallback streamCallback = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                count += streamEvents.length;
                eventArrived = true;

            }
        };

        streamJunctionA.subscribe(streamCallback);
        streamJunctionA.startProcessing();
        streamPublisherA.send(new StreamEvent(2, 2, 2));
        streamPublisherA.send(new StreamEvent(2, 2, 2));
        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(2, count);
        streamJunctionA.stopProcessing();

    }

    @Test
    public void oneToOneTest() throws InterruptedException {
        log.info("one to one");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("parallel"));

        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, siddhiAppContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("parallel"));

        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherB = streamJunctionB.constructPublisher();


        StreamCallback streamCallbackA = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackB = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                count += streamEvents.length;
                eventArrived = true;
                for (Event streamEvent : streamEvents) {
                    AssertJUnit.assertTrue(streamEvent.getData()[0].equals("IBM") || (streamEvent.getData()[0].equals
                            ("WSO2")));
                }
            }
        };

        streamJunctionA.subscribe(streamCallbackA);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB);
        streamJunctionB.startProcessing();

//        Thread.sleep(100);

        StreamEvent streamEvent1 = new StreamEvent(2, 2, 2);
        streamEvent1.setTimestamp(System.currentTimeMillis());
        streamEvent1.setOutputData(new Object[]{"IBM", 12});

        StreamEvent streamEvent2 = new StreamEvent(2, 2, 2);
        streamEvent2.setTimestamp(System.currentTimeMillis());
        streamEvent2.setOutputData(new Object[]{"WSO2", 112});

        streamPublisherA.send(streamEvent1);
        streamPublisherA.send(streamEvent2);
        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(2, count);
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();


    }

    @Test
    public void multiThreadedTest1() throws InterruptedException {
        log.info("multi threaded 1");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, siddhiAppContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherB1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB3 = streamJunctionB.constructPublisher();


        StreamCallback streamCallbackA1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    count++;
                    eventArrived = true;
                    AssertJUnit.assertTrue(streamEvent.getData()[0].equals("IBM") || (streamEvent.getData()[0].equals
                            ("WSO2")));
                }
            }
        };

        streamJunctionA.subscribe(streamCallbackA1);
        streamJunctionA.subscribe(streamCallbackA2);
        streamJunctionA.subscribe(streamCallbackA3);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB);
        streamJunctionB.startProcessing();

        StreamEvent streamEvent1 = new StreamEvent(2, 2, 2);
        streamEvent1.setTimestamp(System.currentTimeMillis());
        streamEvent1.setOutputData(new Object[]{"IBM", 12});

        StreamEvent streamEvent2 = new StreamEvent(2, 2, 2);
        streamEvent2.setTimestamp(System.currentTimeMillis());
        streamEvent2.setOutputData(new Object[]{"WSO2", 112});

        streamPublisherA.send(streamEvent1);
        streamPublisherA.send(streamEvent2);
        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(6, count);
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();


    }

    @Test
    public void multiThreadedTest2() throws InterruptedException {
        log.info("multi threaded 2");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, siddhiAppContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherB1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB3 = streamJunctionB.constructPublisher();

        StreamDefinition streamC = StreamDefinition.id("streamC").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionC = new StreamJunction(streamC, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherC1 = streamJunctionC.constructPublisher();
        final StreamJunction.Publisher streamPublisherC2 = streamJunctionC.constructPublisher();

        StreamCallback streamCallbackA1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A3");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackB2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC2.send(innerStreamEvent);
                }
            }
        };

        final boolean[] eventsArrived = {false, false, false, false, false, false, false, false, false, false, false,
                false};

        StreamCallback streamCallbackC = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {

                    count++;
                    eventArrived = true;
                    Object symbol = streamEvent.getData()[0];
                    if (symbol.equals("IBMA1B1")) {
                        eventsArrived[0] = true;
                    } else if (symbol.equals("IBMA1B2")) {
                        eventsArrived[1] = true;
                    } else if (symbol.equals("IBMA2B1")) {
                        eventsArrived[2] = true;
                    } else if (symbol.equals("IBMA2B2")) {
                        eventsArrived[3] = true;
                    } else if (symbol.equals("IBMA3B1")) {
                        eventsArrived[4] = true;
                    } else if (symbol.equals("IBMA3B2")) {
                        eventsArrived[5] = true;
                    }
                    if (symbol.equals("WSO2A1B1")) {
                        eventsArrived[6] = true;
                    } else if (symbol.equals("WSO2A1B2")) {
                        eventsArrived[7] = true;
                    } else if (symbol.equals("WSO2A2B1")) {
                        eventsArrived[8] = true;
                    } else if (symbol.equals("WSO2A2B2")) {
                        eventsArrived[9] = true;
                    } else if (symbol.equals("WSO2A3B1")) {
                        eventsArrived[10] = true;
                    } else if (symbol.equals("WSO2A3B2")) {
                        eventsArrived[11] = true;
                    }
                }
            }
        };

        streamJunctionA.subscribe(streamCallbackA1);
        streamJunctionA.subscribe(streamCallbackA2);
        streamJunctionA.subscribe(streamCallbackA3);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB1);
        streamJunctionB.subscribe(streamCallbackB2);
        streamJunctionB.startProcessing();

        streamJunctionC.subscribe(streamCallbackC);
        streamJunctionC.startProcessing();

        StreamEvent streamEvent1 = new StreamEvent(2, 2, 2);
        streamEvent1.setTimestamp(System.currentTimeMillis());
        streamEvent1.setOutputData(new Object[]{"IBM", 12});

        StreamEvent streamEvent2 = new StreamEvent(2, 2, 2);
        streamEvent2.setTimestamp(System.currentTimeMillis());
        streamEvent2.setOutputData(new Object[]{"WSO2", 112});

        streamPublisherA.send(streamEvent1);
        streamPublisherA.send(streamEvent2);
        Thread.sleep(100);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(12, count);
        for (boolean arrived : eventsArrived) {
            AssertJUnit.assertTrue(arrived);
        }
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();
        streamJunctionC.stopProcessing();
    }


    @Test
    public void multiThreadedWithEventPoolTest() throws InterruptedException {
        log.info("multi threaded test using event pool");

        final StreamEventPool streamEventPoolA1 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPoolA2 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPoolA3 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPoolB1 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPoolB2 = new StreamEventPool(2, 2, 2, 4);


        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, siddhiAppContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherB1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB3 = streamJunctionB.constructPublisher();

        StreamDefinition streamC = StreamDefinition.id("streamC").attribute("symbol", Attribute.Type.STRING)
                .attribute("price", Attribute.Type.INT).
                        annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionC = new StreamJunction(streamC, executorService, 1024, siddhiAppContext);
        final StreamJunction.Publisher streamPublisherC1 = streamJunctionC.constructPublisher();
        final StreamJunction.Publisher streamPublisherC2 = streamJunctionC.constructPublisher();

        StreamCallback streamCallbackA1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPoolA1.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPoolA2.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPoolA3.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A3");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPoolB1.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackB2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPoolB2.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC2.send(innerStreamEvent);
                }
            }
        };


        final boolean[] eventsArrived = {false, false, false, false, false, false, false, false, false, false, false,
                false};


        StreamCallback streamCallbackC = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    count++;
                    eventArrived = true;
                    Object symbol = streamEvent.getData()[0];
                    if (symbol.equals("IBMA1B1")) {
                        eventsArrived[0] = true;
                    } else if (symbol.equals("IBMA1B2")) {
                        eventsArrived[1] = true;
                    } else if (symbol.equals("IBMA2B1")) {
                        eventsArrived[2] = true;
                    } else if (symbol.equals("IBMA2B2")) {
                        eventsArrived[3] = true;
                    } else if (symbol.equals("IBMA3B1")) {
                        eventsArrived[4] = true;
                    } else if (symbol.equals("IBMA3B2")) {
                        eventsArrived[5] = true;
                    }
                    if (symbol.equals("WSO2A1B1")) {
                        eventsArrived[6] = true;
                    } else if (symbol.equals("WSO2A1B2")) {
                        eventsArrived[7] = true;
                    } else if (symbol.equals("WSO2A2B1")) {
                        eventsArrived[8] = true;
                    } else if (symbol.equals("WSO2A2B2")) {
                        eventsArrived[9] = true;
                    } else if (symbol.equals("WSO2A3B1")) {
                        eventsArrived[10] = true;
                    } else if (symbol.equals("WSO2A3B2")) {
                        eventsArrived[11] = true;
                    }
                }
            }
        };

        streamJunctionA.subscribe(streamCallbackA1);
        streamJunctionA.subscribe(streamCallbackA2);
        streamJunctionA.subscribe(streamCallbackA3);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB1);
        streamJunctionB.subscribe(streamCallbackB2);
        streamJunctionB.startProcessing();

        streamJunctionC.subscribe(streamCallbackC);
        streamJunctionC.startProcessing();

        StreamEvent streamEvent1 = new StreamEvent(2, 2, 2);
        streamEvent1.setTimestamp(System.currentTimeMillis());
        streamEvent1.setOutputData(new Object[]{"IBM", 12});

        StreamEvent streamEvent2 = new StreamEvent(2, 2, 2);
        streamEvent2.setTimestamp(System.currentTimeMillis());
        streamEvent2.setOutputData(new Object[]{"WSO2", 112});

        streamPublisherA.send(streamEvent1);
        streamPublisherA.send(streamEvent2);
        Thread.sleep(1000);
        AssertJUnit.assertTrue(eventArrived);
        AssertJUnit.assertEquals(12, count);
        for (boolean arrived : eventsArrived) {
            AssertJUnit.assertTrue(arrived);
        }
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();
        streamJunctionC.stopProcessing();
    }
}
