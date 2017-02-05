/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.stream;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JunctionTestCase {
    static final Logger log = Logger.getLogger(JunctionTestCase.class);
    private int count;
    private boolean eventArrived;
    private ExecutorService executorService;
    private ExecutionPlanContext executionPlanContext;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
        executorService = Executors.newCachedThreadPool();
        SiddhiContext siddhiContext = new SiddhiContext();
        executionPlanContext = new ExecutionPlanContext();
        executionPlanContext.setSiddhiContext(siddhiContext);
    }


    @Test
    public void JunctionToReceiverTest() throws InterruptedException {
        log.info("junction to receiver");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("parallel"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, executionPlanContext);
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(2, count);
        streamJunctionA.stopProcessing();

    }

    @Test
    public void OneToOneTest() throws InterruptedException {
        log.info("one to one");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("parallel"));

        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, executionPlanContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("parallel"));

        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, executionPlanContext);
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
                    Assert.assertTrue(streamEvent.getData()[0].equals("IBM") || (streamEvent.getData()[0].equals("WSO2")));
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(2, count);
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();


    }

    @Test
    public void MultiThreadedTest1() throws InterruptedException {
        log.info("multi threaded 1");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, executionPlanContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, executionPlanContext);
        final StreamJunction.Publisher streamPublisherB_1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_3 = streamJunctionB.constructPublisher();


        StreamCallback streamCallbackA_1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB_1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB_2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    innerStreamEvent.setOutputData(streamEvent.getData());
                    streamPublisherB_3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    count++;
                    eventArrived = true;
                    Assert.assertTrue(streamEvent.getData()[0].equals("IBM") || (streamEvent.getData()[0].equals("WSO2")));
                }
            }
        };

        streamJunctionA.subscribe(streamCallbackA_1);
        streamJunctionA.subscribe(streamCallbackA_2);
        streamJunctionA.subscribe(streamCallbackA_3);
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(6, count);
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();


    }

    @Test
    public void MultiThreadedTest2() throws InterruptedException {
        log.info("multi threaded 2");

        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, executionPlanContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, executionPlanContext);
        final StreamJunction.Publisher streamPublisherB_1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_3 = streamJunctionB.constructPublisher();

        StreamDefinition streamC = StreamDefinition.id("streamC").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionC = new StreamJunction(streamC, executorService, 1024, executionPlanContext);
        final StreamJunction.Publisher streamPublisherC_1 = streamJunctionC.constructPublisher();
        final StreamJunction.Publisher streamPublisherC_2 = streamJunctionC.constructPublisher();

        StreamCallback streamCallbackA_1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A3");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB_1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC_1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackB_2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = new StreamEvent(2, 2, 2);
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC_2.send(innerStreamEvent);
                }
            }
        };

        final boolean[] eventsArrived = {false, false, false, false, false, false, false, false, false, false, false, false};

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

        streamJunctionA.subscribe(streamCallbackA_1);
        streamJunctionA.subscribe(streamCallbackA_2);
        streamJunctionA.subscribe(streamCallbackA_3);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB_1);
        streamJunctionB.subscribe(streamCallbackB_2);
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(12, count);
        for (boolean arrived : eventsArrived) {
            Assert.assertTrue(arrived);
        }
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();
        streamJunctionC.stopProcessing();
    }


    @Test
    public void MultiThreadedWithEventPoolTest() throws InterruptedException {
        log.info("multi threaded test using event pool");

        final StreamEventPool streamEventPool_A_1 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPool_A_2 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPool_A_3 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPool_B_1 = new StreamEventPool(2, 2, 2, 4);
        final StreamEventPool streamEventPool_B_2 = new StreamEventPool(2, 2, 2, 4);


        StreamDefinition streamA = StreamDefinition.id("streamA").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionA = new StreamJunction(streamA, executorService, 1024, executionPlanContext);
        StreamJunction.Publisher streamPublisherA = streamJunctionA.constructPublisher();

        StreamDefinition streamB = StreamDefinition.id("streamB").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionB = new StreamJunction(streamB, executorService, 1024, executionPlanContext);
        final StreamJunction.Publisher streamPublisherB_1 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_2 = streamJunctionB.constructPublisher();
        final StreamJunction.Publisher streamPublisherB_3 = streamJunctionB.constructPublisher();

        StreamDefinition streamC = StreamDefinition.id("streamC").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).
                annotation(Annotation.annotation("async"));
        StreamJunction streamJunctionC = new StreamJunction(streamC, executorService, 1024, executionPlanContext);
        final StreamJunction.Publisher streamPublisherC_1 = streamJunctionC.constructPublisher();
        final StreamJunction.Publisher streamPublisherC_2 = streamJunctionC.constructPublisher();

        StreamCallback streamCallbackA_1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPool_A_1.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPool_A_2.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_2.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackA_3 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPool_A_3.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("A3");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherB_3.send(innerStreamEvent);
                }
            }
        };


        StreamCallback streamCallbackB_1 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPool_B_1.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B1");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC_1.send(innerStreamEvent);
                }
            }
        };

        StreamCallback streamCallbackB_2 = new StreamCallback() {
            @Override
            public void receive(Event[] streamEvents) {
                for (Event streamEvent : streamEvents) {
                    StreamEvent innerStreamEvent = streamEventPool_B_2.borrowEvent();
                    innerStreamEvent.setTimestamp(streamEvent.getTimestamp());
                    Object[] data = new Object[]{streamEvent.getData()[0], streamEvent.getData()[1]};
                    data[0] = ((String) data[0]).concat("B2");
                    innerStreamEvent.setOutputData(data);
                    streamPublisherC_2.send(innerStreamEvent);
                }
            }
        };


        final boolean[] eventsArrived = {false, false, false, false, false, false, false, false, false, false, false, false};


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

        streamJunctionA.subscribe(streamCallbackA_1);
        streamJunctionA.subscribe(streamCallbackA_2);
        streamJunctionA.subscribe(streamCallbackA_3);
        streamJunctionA.startProcessing();

        streamJunctionB.subscribe(streamCallbackB_1);
        streamJunctionB.subscribe(streamCallbackB_2);
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
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(12, count);
        for (boolean arrived : eventsArrived) {
            Assert.assertTrue(arrived);
        }
        streamJunctionA.stopProcessing();
        streamJunctionB.stopProcessing();
        streamJunctionC.stopProcessing();
    }
}
