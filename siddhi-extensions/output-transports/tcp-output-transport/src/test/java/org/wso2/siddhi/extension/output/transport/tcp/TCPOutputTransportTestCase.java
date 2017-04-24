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

package org.wso2.siddhi.extension.output.transport.tcp;

import org.junit.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.tcp.transport.TCPNettyServer;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;

import java.util.ArrayList;

public class TCPOutputTransportTestCase {
    static final Logger log = Logger.getLogger(TCPOutputTransportTestCase.class);
    private volatile int count;
    private volatile int count1;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        count1 = 0;
        eventArrived = false;
    }

    @Test
    public void testTcpOutputTransport1() throws InterruptedException {
        log.info("tcpInputTransport TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);


    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testTcpOutputTransport2() throws InterruptedException {
        log.info("tcpInputTransport TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        Thread.sleep(300);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testTcpOutputTransport3() throws InterruptedException {
        log.info("tcpInputTransport TestCase 3");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', host='127.0.0.1', port='9766', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9766);
        serverConfig.setHost("127.0.0.1");
        tcpNettyServer.bootServer(serverConfig);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport4() throws InterruptedException {
        log.info("tcpInputTransport TestCase 4");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', port='9766', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9766);
        serverConfig.setHost("127.0.0.1");
        tcpNettyServer.bootServer(serverConfig);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport5() throws InterruptedException {
        log.info("tcpInputTransport TestCase 5");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', port='9766', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9766);
        serverConfig.setHost("127.0.0.1");
        tcpNettyServer.bootServer(serverConfig);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test(expected = ExecutionPlanCreationException.class)
    public void testTcpOutputTransport6() throws InterruptedException {
        log.info("tcpInputTransport TestCase 6");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', host='127.0.0.1', port='9766', @map(type='text')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = null;
        try {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
            executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }

        }
    }

    @Test
    public void testTcpOutputTransport7() throws InterruptedException {
        log.info("tcpInputTransport TestCase 7");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo') " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport8() throws InterruptedException {
        log.info("tcpInputTransport TestCase 8");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='{{a}}') " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
                count++;
                switch (count) {
                    case 1:
                        Assert.assertEquals("foo", event.getData(0));
                        break;
                    case 2:
                        Assert.assertEquals("foo", event.getData(0));
                        break;
                    default:
                        org.junit.Assert.fail();
                }
            }

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();

        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"bar", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"bar", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"foo", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"foo", 362, 32.0f, 3802l, 232.0, true}));

        inputHandler.send(arrayList.toArray(new Event[4]));

        Thread.sleep(300);

        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport9() throws InterruptedException {
        log.info("tcpInputTransport TestCase 9");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='bar', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
            }

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Ignore
    @Test
    //todo validate log
    public void testTcpOutputTransport10() throws InterruptedException {
        log.info("tcpInputTransport TestCase 10");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='bar', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertFalse(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test(expected = ExecutionPlanCreationException.class)
    public void testTcpOutputTransport11() throws InterruptedException {
        log.info("tcpInputTransport TestCase 11");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', host='127.0.0.1', port='{{d}}') " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = null;
        try {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
            executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }

        }
    }

    @Test(expected = ExecutionPlanCreationException.class)
    public void testTcpOutputTransport12() throws InterruptedException {
        log.info("tcpInputTransport TestCase 12");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', host='{{a}}') " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = null;
        try {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
            executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }

        }
    }

    @Test
    public void testTcpOutputTransport13() throws InterruptedException {
        log.info("tcpInputTransport TestCase 13");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo') " +
                "define stream outputStream1 (a string, b int, c float, d long, e double, f bool);" +
                "@sink(type='tcp', context='foo') " +
                "define stream outputStream2 (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "" +
                "from inputStream " +
                "select *  " +
                "insert into outputStream1; " +
                "" +
                "from inputStream " +
                "select *  " +
                "insert into outputStream2; " +
                "");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
                count++;
                /*
                commenting this out since we cannot guarantee an event order here
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
                    case 4:
                        Assert.assertEquals("test", event.getData(0));
                        break;
                    case 5:
                        Assert.assertEquals("test1", event.getData(0));
                        break;
                    case 6:
                        Assert.assertEquals("test2", event.getData(0));
                        break;
                    default:
                        org.junit.Assert.fail();

                }*/
            }

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(3000);

        Assert.assertEquals(6, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport14() throws InterruptedException {
        log.info("tcpInputTransport TestCase 14");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo1', port='9854') " +
                "@sink(type='tcp', context='foo2') " +
                "define stream outputStream(a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "" +
                "from inputStream " +
                "select *  " +
                "insert into outputStream; " +
                "");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition1 = StreamDefinition.id("foo1").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        StreamDefinition streamDefinition2 = StreamDefinition.id("foo2").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer1 = new TCPNettyServer();
        TCPNettyServer tcpNettyServer2 = new TCPNettyServer();
        tcpNettyServer1.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition1;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer2.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition2;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
                count1++;
                switch (count1) {
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9854);
        tcpNettyServer1.bootServer(serverConfig);
        tcpNettyServer2.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(3000);

        Assert.assertEquals(3, count);
        Assert.assertEquals(3, count1);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer1.shutdownGracefully();
        tcpNettyServer2.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport15() throws InterruptedException {
        log.info("tcpInputTransport TestCase 15");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "@plan:name('foo') " +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo/inputStream1') " +
                "define stream outputStream(a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "" +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;" +
                " " +
                "");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition1 = StreamDefinition.id("foo/inputStream1").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer1 = new TCPNettyServer();
        tcpNettyServer1.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition1;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer1.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(3000);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer1.shutdownGracefully();

    }

    @Test
    public void testTcpOutputTransport16() throws InterruptedException {
        log.info("tcpInputTransport TestCase 16");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +
                "@sink(type='tcp', context='foo', @map(type='passThrough')) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("foo").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event);
                eventArrived = true;
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

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });


        executionPlanRuntime.start();
        Thread.sleep(2000);
        tcpNettyServer.bootServer(new ServerConfig());

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
        tcpNettyServer.shutdownGracefully();

    }

}

