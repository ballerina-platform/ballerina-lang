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

package org.wso2.siddhi.core.query.function;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

public class FunctionTestCase {
    private static final Logger log = Logger.getLogger(FunctionTestCase.class);
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

        StreamDefinition cseEventStream = StreamDefinition.id("cseEventStream").attribute("symbol", Attribute.Type
                .STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT);

        Query query = new Query();
        query.from(InputStream.stream("cseEventStream"));
        query.annotation(Annotation.annotation("info").element("name", "query1"));
        query.select(
                Selector.selector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.function("coalesce", Expression.variable("price1"), Expression
                                .variable("price2")))
        );
        query.insertInto("StockQuote");

        SiddhiApp siddhiApp = new SiddhiApp("ep1");
        siddhiApp.defineStream(cseEventStream);
        siddhiApp.addQuery(query);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
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


        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 55.6f, 70.6f});
        inputHandler.send(new Object[]{"WSO2", 65.7f, 12.8f});
        inputHandler.send(new Object[]{"WSO2", 23.6f, null});
        inputHandler.send(new Object[]{"WSO2", null, 34.6f});
        inputHandler.send(new Object[]{"WSO2", null, null});
        Thread.sleep(100);
        Assert.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();

    }

    @Test(expected = SiddhiAppValidationException.class)
    public void functionTest2() throws InterruptedException {
        log.info("function test 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price1 double, price2 float, volume " +
                "long , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream select symbol, coalesce(price1,price2) as price," +
                "quantity insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
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
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200L, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200L, 56});
        Thread.sleep(100);
        Assert.assertEquals(4, count);
        siddhiAppRuntime.shutdown();

    }

    @Test
    public void functionTest3() throws InterruptedException {
        log.info("function test 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price1 float, price2 float, volume long" +
                " , quantity int);";
        String query = "@info(name = 'query1') from cseEventStream[coalesce(price1,price2) > 0f] select symbol, " +
                "coalesce(price1,price2) as price,quantity insert into outputStream ;";


        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
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

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60L, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40L, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200L, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200L, 56});
        Thread.sleep(100);
        org.junit.Assert.assertEquals(3, count);
        siddhiAppRuntime.shutdown();

    }

}
