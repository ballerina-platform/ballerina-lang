/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.test.standard.ratelimit;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class EvnetOutputRateLimitTestCase {
    static final Logger log = Logger.getLogger(EvnetOutputRateLimitTestCase.class);

    private int count;
    private long value;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        value = 0;
        eventArrived = false;
    }


    @Test
    public void testEventOutputRateLimitQuery1() throws InterruptedException {
        log.info("EventOutputRateLimit test1");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output all every 2 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testEventOutputRateLimitQuery2() throws InterruptedException {
        log.info("EventOutputRateLimit test2");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output every 2 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testEventOutputRateLimitQuery3() throws InterruptedException {
        log.info("EventOutputRateLimit test3");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output last every 2 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.4".equals(inEvents[0].getData0()));
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testEventOutputRateLimitQuery4() throws InterruptedException {
        log.info("EventOutputRateLimit test4");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output first every 2 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.9".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()));
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});

        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 3, count);
        siddhiManager.shutdown();
    }


    //group by

    @Test
    public void testEventOutputRateLimitQuery5() throws InterruptedException {
        log.info("EventOutputRateLimit test5");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "group by ip " +
                                                       "output last every 5 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testEventOutputRateLimitQuery6() throws InterruptedException {
        log.info("EventOutputRateLimit test6");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "group by ip " +
                                                       "output first every 5 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 6, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testEventOutputRateLimitQuery7() throws InterruptedException {
        log.info("EventOutputRateLimit test7");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "group by ip " +
                                                       "output every 5 events " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                } else {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 5, count);
        siddhiManager.shutdown();
    }

}
