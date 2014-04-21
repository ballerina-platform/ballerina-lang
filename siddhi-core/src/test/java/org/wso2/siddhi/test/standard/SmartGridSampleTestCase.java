/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.test.standard;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.input.pattern.Pattern;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;

import java.util.ArrayList;
import java.util.List;

public class SmartGridSampleTestCase {
    static final Logger log = Logger.getLogger(SmartGridSampleTestCase.class);
    private int eventCount;
    private boolean eventArrived;
    protected static List<Object[]> STSDataSet = new ArrayList<Object[]>();
    protected static List<Object[]> STMDataSet = new ArrayList<Object[]>();


    @BeforeClass
    public static void populateDataSet() {
        STSDataSet.add(new Object[]{"A1", 60f, 0L});
        STSDataSet.add(new Object[]{"A2", 70f, 0L});
        STSDataSet.add(new Object[]{"A3", 60f, 0L});
        STSDataSet.add(new Object[]{"A4", 70f, 0L});
        STSDataSet.add(new Object[]{"A5", 60f, 0L});
        STSDataSet.add(new Object[]{"A6", 70f, 0L});

        STMDataSet.add(new Object[]{"B1", 70f, 0L});
        STMDataSet.add(new Object[]{"B2", 60f, 0L});
        STMDataSet.add(new Object[]{"B3", 70f, 0L});
        STMDataSet.add(new Object[]{"B4", 60f, 0L});
        STMDataSet.add(new Object[]{"B5", 70f, 0L});
        STMDataSet.add(new Object[]{"B6", 60f, 0L});

    }

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    private void publishEvents(InputHandler STSStreamHandler, InputHandler STMStreamHandler) {
        try {
            for (int i = 0; i < STMDataSet.size(); i++) {
                STSStreamHandler.send(STSDataSet.get(i));
                Thread.sleep(500);
                STMStreamHandler.send(STMDataSet.get(i));
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("Matching every occurrence of STS event with a STM following event");


        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STS").attribute("eventID", Attribute.Type.STRING).attribute("setpoint", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STM").attribute("eventID", Attribute.Type.STRING).attribute("reading", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.every(
                                        QueryFactory.inputStream("e1", "STS").filter(
                                                Condition.compare(Expression.variable("setpoint"),
                                                                  Condition.Operator.LESS_THAN,
                                                                  Expression.value(70)))),
                                QueryFactory.inputStream("e2", "STM").filter(
                                        Condition.compare(Expression.variable("reading"),
                                                          Condition.Operator.LESS_THAN,
                                                          Expression.value(70))))));

        query.select(
                QueryFactory.outputSelector().
                        select("STSEventID", Expression.variable("e1", "eventID")).
                        select("STMEventID", Expression.variable("e2", "eventID"))
        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
//                    junit.framework.Assert.assertTrue("IBM".equals(getData(newEventData, 0, 0)) || "WSO2".equals(getData(newEventData, 0, 0)));
                    eventCount++;
                }
                eventArrived = true;
            }
        });
        InputHandler STSStreamHandler = siddhiManager.getInputHandler("STS");
        InputHandler STMStreamHandler = siddhiManager.getInputHandler("STM");

        publishEvents(STSStreamHandler, STMStreamHandler);

        Assert.assertEquals("Number of success events", 3, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Thread.sleep(2000);

    }

    @Test
    public void testQuery2() throws InterruptedException {
        log.info("Matching every occurrence of STS event with every of their STM following events");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STS").attribute("eventID", Attribute.Type.STRING).attribute("setpoint", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STM").attribute("eventID", Attribute.Type.STRING).attribute("reading", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.patternStream(
                        Pattern.followedBy(
                                Pattern.every(
                                        QueryFactory.inputStream("e1", "STS").filter(
                                                Condition.compare(Expression.variable("setpoint"),
                                                                  Condition.Operator.LESS_THAN,
                                                                  Expression.value(70)))),
                                Pattern.every(QueryFactory.inputStream("e2", "STM").filter(
                                        Condition.compare(Expression.variable("reading"),
                                                          Condition.Operator.LESS_THAN,
                                                          Expression.value(70)))))));

        query.select(
                QueryFactory.outputSelector().
                        select("STSEventID", Expression.variable("e1", "eventID")).
                        select("STMEventID", Expression.variable("e2", "eventID"))
        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
//                    junit.framework.Assert.assertTrue("IBM".equals(getData(newEventData, 0, 0)) || "WSO2".equals(getData(newEventData, 0, 0)));
                    eventCount++;
                }
                eventArrived = true;
            }

        });
        InputHandler STSStreamHandler = siddhiManager.getInputHandler("STS");
        InputHandler STMStreamHandler = siddhiManager.getInputHandler("STM");

        publishEvents(STSStreamHandler, STMStreamHandler);

        Assert.assertEquals("Number of success events", 6, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Thread.sleep(2000);

    }

    @Test
    public void testQuery3() throws InterruptedException {
        log.info("Long window join query covering all events");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STS").attribute("eventID", Attribute.Type.STRING).attribute("setpoint", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STM").attribute("eventID", Attribute.Type.STRING).attribute("reading", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("e1", "STS").
                                filter(Condition.compare(Expression.variable("setpoint"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("time", Expression.value(50000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("e2", "STM").
                                filter(Condition.compare(Expression.variable("reading"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("time", Expression.value(50000))
                ));

        query.select(
                QueryFactory.outputSelector().
                        select("STSEventID", Expression.variable("e1", "eventID")).
                        select("STMEventID", Expression.variable("e2", "eventID"))
        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
//                    junit.framework.Assert.assertTrue("IBM".equals(getData(newEventData, 0, 0)) || "WSO2".equals(getData(newEventData, 0, 0)));
                    eventCount+=inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler STSStreamHandler = siddhiManager.getInputHandler("STS");
        InputHandler STMStreamHandler = siddhiManager.getInputHandler("STM");

        publishEvents(STSStreamHandler, STMStreamHandler);

        Assert.assertEquals("Number of success events", 9, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Thread.sleep(3000);

    }


    @Test
    //The last event in the testQuery3 wont occur as A5 expires before the arrival of B6
    public void testQuery4() throws InterruptedException {
        log.info("Limited window join query ");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STS").attribute("eventID", Attribute.Type.STRING).attribute("setpoint", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STM").attribute("eventID", Attribute.Type.STRING).attribute("reading", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("e1", "STS").
                                filter(Condition.compare(Expression.variable("setpoint"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("time", Expression.value(5000)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("e2", "STM").
                                filter(Condition.compare(Expression.variable("reading"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("time", Expression.value(5000))
                ));
        query.select(
                QueryFactory.outputSelector().
                        select("STSEventID", Expression.variable("e1", "eventID")).
                        select("STMEventID", Expression.variable("e2", "eventID"))
        );
        query.insertInto("OutStream", OutStream.OutputEventsFor.ALL_EVENTS);


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
//                    junit.framework.Assert.assertTrue("IBM".equals(getData(newEventData, 0, 0)) || "WSO2".equals(getData(newEventData, 0, 0)));
                    eventCount+=inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler STSStreamHandler = siddhiManager.getInputHandler("STS");
        InputHandler STMStreamHandler = siddhiManager.getInputHandler("STM");

        publishEvents(STSStreamHandler, STMStreamHandler);

        Thread.sleep(4000);
        Assert.assertEquals("Number of success events", 8, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);

    }


    @Test
    //This is only a partial solution here we are not taking time into consideration !!
    public void testQuery5() throws InterruptedException {
        log.info("Window join query with event consumption with only match once");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STS").attribute("eventID", Attribute.Type.STRING).attribute("setpoint", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));
        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("STM").attribute("eventID", Attribute.Type.STRING).attribute("reading", Attribute.Type.FLOAT).attribute("time", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.joinStream(
                        QueryFactory.inputStream("e1", "STS").
                                filter(Condition.compare(Expression.variable("setpoint"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("length", Expression.value(1)),
                        JoinStream.Type.INNER_JOIN,
                        QueryFactory.inputStream("e2", "STM").
                                filter(Condition.compare(Expression.variable("reading"),
                                                         Condition.Operator.LESS_THAN,
                                                         Expression.value(70))).
                                window("length", Expression.value(1)),
                        null, null,
                        JoinStream.EventTrigger.RIGHT

                ));

        query.select(
                QueryFactory.outputSelector().
                        select("STSEventID", Expression.variable("e1", "eventID")).
                        select("STMEventID", Expression.variable("e2", "eventID"))
        );
        query.insertInto("OutStream");


        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
//                    junit.framework.Assert.assertTrue("IBM".equals(getData(newEventData, 0, 0)) || "WSO2".equals(getData(newEventData, 0, 0)));
                    eventCount++;
                }
                eventArrived = true;
            }

        });
        InputHandler STSStreamHandler = siddhiManager.getInputHandler("STS");
        InputHandler STMStreamHandler = siddhiManager.getInputHandler("STM");

        publishEvents(STSStreamHandler, STMStreamHandler);

        Assert.assertEquals("Number of success events", 3, eventCount);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Thread.sleep(3000);

    }

}
