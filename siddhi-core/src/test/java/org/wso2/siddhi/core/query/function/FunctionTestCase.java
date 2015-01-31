/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.query.function;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

public class FunctionTestCase {
    static final Logger log = Logger.getLogger(FunctionTestCase.class);
    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    //Coalesce

    @Test
    public void functionTest1() throws InterruptedException {
        log.info("function test 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).
                annotation(Annotation.annotation("config").element("async", "true"));

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("coalesce", new Expression[]{Expression.variable("price1"), Expression.variable("price2")}))
        );
        query.insertInto("StockQuote");

        ExecutionPlan executionPlan = new ExecutionPlan("ep1");
        executionPlan.defineStream(cseEventStream);
        executionPlan.addQuery(query);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(55.6f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(65.7f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(23.6f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(34.6f, inEvent.getData()[1]);
                    } else if (count == 5) {
                        Assert.assertNull(inEvent.getData()[1]);
                    }
                }
            }

        });


        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 55.6f, 70.6f});
        inputHandler.send(new Object[]{"WSO2", 65.7f, 12.8f});
        inputHandler.send(new Object[]{"WSO2", 23.6f, null});
        inputHandler.send(new Object[]{"WSO2", null, 34.6f});
        inputHandler.send(new Object[]{"WSO2", null, null});
        Thread.sleep(100);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void functionTest2() throws InterruptedException {
        log.info("function test 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 double, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, coalesce(price1,price2) as price,quantity insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }

        });
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest3() throws InterruptedException {
        log.info("function test 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[coalesce(price1,price2) > 0f] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream ;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(3, count);
        executionPlanRuntime.shutdown();

    }

    //isMatch

    @Test
    public void functionTest4() throws InterruptedException {
        log.info("function test 4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[isMatch('[^//s]+',symbol)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest5() throws InterruptedException {
        log.info("function test 5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[not isMatch('[//s]+',symbol)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void functionTest6() throws InterruptedException {
        log.info("function test 6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[not (isMatch('[^//s]+',symbol) and false)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest7() throws InterruptedException {
        log.info("function test 7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[not (isMatch('[//s]+',symbol) and false)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest8() throws InterruptedException {
        log.info("function test 8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[(not isMatch('[//s]+',symbol)) and false] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(0, count);
        executionPlanRuntime.shutdown();

    }

    //Concat

    @Test
    public void functionTest9() throws InterruptedException {
        log.info("function test 9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, concat(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals("50.060.0", inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest10() throws InterruptedException {
        log.info("function test 10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, concat(symbol,' ',price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals("WSO2 60.0", inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();
    }

    //sin

    @Test
    public void functionTest11() throws InterruptedException {
        log.info("function test 11");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, sin(price) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(1.0, inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.570796325, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest12() throws InterruptedException {
        log.info("function test 12");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, sin(price) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(0.9999417202299663, inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56f, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void functionTest13() throws InterruptedException {
        log.info("function test 13");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price double, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, sin(price) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(0.9999417202299663, inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56d, 60l, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void functionTest14() throws InterruptedException {
        log.info("function test 14");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price double, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, sin(volume) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(0.5311861979208834, inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56f, 0.56, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void functionTest15() throws InterruptedException {
        log.info("function test 15");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price double, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, sin(price,volume) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(0.5311861979208834, inEvents[0].getData()[1]);
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56f, 0.56, 6});
        Thread.sleep(100);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();

    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void functionTest16() throws InterruptedException {
        log.info("function test 16");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[isMatch(symbol)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void functionTest17() throws InterruptedException {
        log.info("function test 17");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[isMatch(12,symbol)] select symbol, coalesce(price1,price2) as price,quantity insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(50.0f, inEvent.getData()[1]);
                    } else if (count == 2) {
                        Assert.assertEquals(70.0f, inEvent.getData()[1]);
                    } else if (count == 3) {
                        Assert.assertEquals(44.0f, inEvent.getData()[1]);
                    } else if (count == 4) {
                        Assert.assertEquals(null, inEvent.getData()[1]);
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        executionPlanRuntime.shutdown();

    }

}
