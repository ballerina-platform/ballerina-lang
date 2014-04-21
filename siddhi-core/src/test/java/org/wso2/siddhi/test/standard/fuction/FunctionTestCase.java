/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.standard.fuction;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;

public class FunctionTestCase {
    static final Logger log = Logger.getLogger(FunctionTestCase.class);
    private int eventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    //*****************************************************************************************************************
    //Coalesce

    @Test
    public void testFunctionQuery1() throws InterruptedException {
        log.info("Function test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    junit.framework.Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    junit.framework.Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    junit.framework.Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    junit.framework.Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }

    @Test(expected = QueryCreationException.class)
    public void testFunctionQuery2() throws InterruptedException {
        log.info("Function test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.DOUBLE).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    junit.framework.Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    junit.framework.Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    junit.framework.Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    junit.framework.Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery3() throws InterruptedException {
        log.info("Function test3");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[coalesce(price1,price2) > 0f]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    junit.framework.Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    junit.framework.Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    junit.framework.Assert.assertEquals(44.0f, inEvents[0].getData1());
//                } else if (eventCount == 4) {
//                    Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(3, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery4() throws InterruptedException {
        log.info("Function test4");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[isMatch('[^//s]+',symbol)]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery5() throws InterruptedException {
        log.info("Function test5");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[not isMatch('[//s]+',symbol)]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }


    @Test
    public void testFunctionQuery6() throws InterruptedException {
        log.info("Function test6");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[not (isMatch('[^//s]+',symbol) and false)]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery7() throws InterruptedException {
        log.info("Function test7");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[not isMatch('[//s]+',symbol) and false]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                if (eventCount == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData1());
                } else if (eventCount == 2) {
                    Assert.assertEquals(70.0f, inEvents[0].getData1());
                } else if (eventCount == 3) {
                    Assert.assertEquals(44.0f, inEvents[0].getData1());
                } else if (eventCount == 4) {
                    Assert.assertEquals(null, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery8() throws InterruptedException {
        log.info("Function test8");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[(not isMatch('[//s]+',symbol)) and false]" +
                                                       " select symbol, coalesce(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(0, eventCount);
        siddhiManager.shutdown();

    }

    //**************************************************
    //Concat

    @Test
    public void testFunctionQuery9() throws InterruptedException {
        log.info("Function test9");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream" +
                " select symbol, concat(price1,price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals("50.060.0", inEvents[0].getData1());
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testFunctionQuery10() throws InterruptedException {
        log.info("Function test10");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream" +
                " select symbol, concat(symbol,' ',price2) as price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals("WSO2 60.0", inEvents[0].getData1());
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();

    }


}
