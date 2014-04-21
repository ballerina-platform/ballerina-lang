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
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

public class SnapshotOutputRateLimitTestCase {
    static final Logger log = Logger.getLogger(SnapshotOutputRateLimitTestCase.class);

    private int count;
    private long value;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        value = 0;
        eventArrived = false;
    }

    // 5,8,11,13,

    @Test
    public void testSnapshotOutputRateLimitQuery1() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.3".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    Assert.fail("Remove events emitted");
                } else {
                    Assert.fail("Null events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertTrue("Number of output event value", 1 <= count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery2() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.3".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    if (count != 0) {
                        Assert.fail("Remove events emitted");
                    }
                } else {
                    if (count != 0) {
                        Assert.fail("Null events emitted");
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery3() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test3");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.3".equals(inEvents[0].getData0()) || "192.10.1.4".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    Assert.fail("Remove events emitted");
                } else {
                    Assert.fail("Null events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.9"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        Thread.sleep(1100);
//        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
//        Thread.sleep(1000);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 3, count);
        siddhiManager.shutdown();
    }


    //With time window

    @Test
    public void testSnapshotOutputRateLimitQuery4() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test4");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    Assert.fail("Remove events emitted");
                } else {
                    Assert.fail("Null events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery5() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test5");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    if (count != 0) {
                        Assert.fail("Remove events emitted");
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery6() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test6");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback("uniqueIps", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
                eventArrived = true;
            }
        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 1, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery7() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test7");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.3".equals(inEvents[0].getData0()) || "192.10.1.5".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    Assert.fail("Remove events emitted");
                } else {
                    Assert.fail("Null events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery8() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    Assert.assertTrue("192.10.1.3".equals(inEvents[0].getData0()) || "192.10.1.5".equals(inEvents[0].getData0()));
                } else if (removeEvents != null) {
                    Assert.fail("Remove events emitted");
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);

        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery9() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test9");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2) {
//                    Assert.assertNull(inEvents);
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5"});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3"});
        Thread.sleep(1200);
//        Assert.assertEquals("Event arrived", true, eventArrived);
//        Assert.assertEquals("Number of output event value", 3, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery10() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test10");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select  sum(calls) as totalCalls " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2) {
                    Assert.assertTrue((Long) inEvents[0].getData0() == 9l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData0() == 12l);
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery11() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test11");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select  sum(calls) as totalCalls " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2) {
                    Assert.assertTrue((Long) inEvents[0].getData0() == 9l);
                } else if (count == 3) {
                    Assert.assertTrue((Long) inEvents[0].getData0() == 9l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData0() == 21l);
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery12() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test12");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select ip, sum(calls) as totalCalls " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    if (count == 1) {
                        Assert.assertTrue((Long) inEvents[0].getData1() == 9l && (Long) inEvents[1].getData1() == 9l);
                    } else if (count == 3) {
                        Assert.assertTrue((Long) inEvents[0].getData1() == 12l && (Long) inEvents[1].getData1() == 12l);
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 2, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery13() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test13");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select ip, sum(calls) as totalCalls " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 9l && (Long) inEvents[1].getData1() == 9l);
                } else if (count == 3) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 9l && (Long) inEvents[1].getData1() == 9l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 21l && (Long) inEvents[1].getData1() == 21l);
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event value", 4, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery14() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test14");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select ip as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()) || "192.10.1.4".equals(inEvents[0].getData0()));
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.4", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles ", 3, count);
        Assert.assertEquals("Number of output events  ", 7, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery15() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test15");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents " +
                                                       "select ip, sum(calls) as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()));
                    value += inEvents.length;
                    if (count == 3) {
                        Assert.assertTrue((Long) inEvents[0].getData1() == 5l && (Long) inEvents[1].getData1() == 16l);
                    }
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 3, count);
        Assert.assertEquals("Number of output events", 6, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery16() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test16");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select ip " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    if (count == 2 || count == 4) {
                        Assert.assertTrue("192.10.1.5".equals(inEvents[0].getData0()) || "192.10.1.3".equals(inEvents[0].getData0()));
                    }
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles with inEvents", 2, count);
        Assert.assertEquals("Number of output event", 4, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery17() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test17");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select ip " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 4, count);
        Assert.assertEquals("Number of output event value", 8, value);
        siddhiManager.shutdown();
    }


    @Test
    public void testSnapshotOutputRateLimitQuery18() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test18");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select ip, sum(calls) as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 3l || (Long) inEvents[1].getData1() == 6l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 2l || (Long) inEvents[1].getData1() == 10l);
                }
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 4, count);
        Assert.assertEquals("Number of output events", 4, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery19() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test19");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select ip, sum(calls) as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 2 || count == 3) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 3l || (Long) inEvents[1].getData1() == 6l);
                } else if (count == 4) {
                    Assert.assertTrue((Long) inEvents[0].getData1() == 5l || (Long) inEvents[1].getData1() == 16l);
                }
                if (inEvents != null) {
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 4, count);
        Assert.assertEquals("Number of output event value", 8, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery20() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test20");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(1 sec) " +
                                                       "select sum(calls) as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;

                    if (count == 1) {
                        Assert.assertTrue((Long) inEvents[0].getData0() == 3l || (Long) inEvents[1].getData0() == 6l);
                    } else if (count == 2) {
                        Assert.assertTrue((Long) inEvents[0].getData0() == 2l || (Long) inEvents[1].getData0() == 10l);
                    }
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 2, count);
        Assert.assertEquals("Number of output events", 4, value);
        siddhiManager.shutdown();
    }

    @Test
    public void testSnapshotOutputRateLimitQuery21() throws InterruptedException {
        log.info("SnapshotOutputRateLimit test21");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream LoginEvents (timeStamp long, ip string, calls int) ");

        String queryReference = siddhiManager.addQuery("from LoginEvents#window.time(5 sec) " +
                                                       "select sum(calls) as totalCalls " +
                                                       "group by ip " +
                                                       "output snapshot every 1 sec " +
                                                       "insert into uniqueIps for all-events ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count++;
                    if (count == 1 || count == 2) {
                        Assert.assertTrue((Long) inEvents[0].getData0() == 3l || (Long) inEvents[1].getData0() == 6l);
                    } else if (count == 3) {
                        Assert.assertTrue((Long) inEvents[0].getData0() == 5l || (Long) inEvents[1].getData0() == 16l);
                    }
                    value += inEvents.length;
                }
                eventArrived = true;
            }

        });
        InputHandler loginSucceedEvents = siddhiManager.getInputHandler("LoginEvents");

        Thread.sleep(1100);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 3});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 6});
        Thread.sleep(2200);
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.5", 2});
        loginSucceedEvents.send(new Object[]{System.currentTimeMillis(), "192.10.1.3", 10});
        Thread.sleep(1200);
        Assert.assertEquals("Event arrived", true, eventArrived);
        Assert.assertEquals("Number of output event bundles", 3, count);
        Assert.assertEquals("Number of output event value", 6, value);
        siddhiManager.shutdown();
    }
}
