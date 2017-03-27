/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.input.transport.tcp;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.tcp.transport.TcpNettyClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TcpInputTransportTestCase {
    static final Logger log = Logger.getLogger(TcpInputTransportTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void testTcpInputTransport1() throws InterruptedException {
        log.info("tcpInputTransport TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "@source(type='tcp', @map(type='passThrough'))" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals("test", event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals("test1", event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals("test2", event.getData(0));
                            break;
                        default:
                            org.junit.Assert.fail();
                    }
                }
            }
        });

        executionPlanRuntime.start();

        TcpNettyClient tcpNettyClient = new TcpNettyClient();
        tcpNettyClient.connect("localhost", 8080);
        ArrayList<Event> arrayList = new ArrayList<Event>(3);

        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        tcpNettyClient.send("inputStream", arrayList.toArray(new Event[3]));

        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testTcpInputTransportPauseAndResume() throws InterruptedException {
        init();
        log.info("tcpInputTransport TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "@source(type='tcp', @map(type='passThrough'))" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        Collection<List<InputTransport>> inputTransports = executionPlanRuntime.getInputTransports();

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventArrived = true;
                for (Event event : inEvents) {
                    count++;
                    switch (count) {
                        case 1:
                            Assert.assertEquals("test", event.getData(0));
                            break;
                        case 2:
                            Assert.assertEquals("test1", event.getData(0));
                            break;
                        case 3:
                            Assert.assertEquals("test2", event.getData(0));
                            break;
                        default:
                    }
                }
            }
        });

        executionPlanRuntime.start();

        TcpNettyClient tcpNettyClient = new TcpNettyClient();
        tcpNettyClient.connect("localhost", 8080);
        ArrayList<Event> arrayList = new ArrayList<Event>(3);

        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        tcpNettyClient.send("inputStream", arrayList.toArray(new Event[3]));


        Thread.sleep(1000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(3, count);
        count = 0;
        eventArrived = false;

        // pause
        inputTransports.forEach(e -> e.forEach(InputTransport::pause));
        Thread.sleep(1000);
        // send few events
        arrayList.clear();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        tcpNettyClient.send("inputStream", arrayList.toArray(new Event[2]));

        Thread.sleep(1000);
        Assert.assertFalse(eventArrived);

        // resume
        inputTransports.forEach(e -> e.forEach(InputTransport::resume));
        // send few more events
        arrayList.clear();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test3", 361, 31.0f, 3801l, 231.0, false}));
        tcpNettyClient.send("inputStream", arrayList.toArray(new Event[2]));
        Thread.sleep(1000);
        // once resumed, we should be able to access the data sent while the transport is paused
        Assert.assertEquals(4, count);
        Assert.assertTrue(eventArrived);

        count = 0;

        // send few more events
        arrayList.clear();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        tcpNettyClient.send("inputStream", arrayList.toArray(new Event[2]));

        Thread.sleep(1000);
        Assert.assertEquals(2, count);

        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
        Thread.sleep(300);
        executionPlanRuntime.shutdown();
    }

}
