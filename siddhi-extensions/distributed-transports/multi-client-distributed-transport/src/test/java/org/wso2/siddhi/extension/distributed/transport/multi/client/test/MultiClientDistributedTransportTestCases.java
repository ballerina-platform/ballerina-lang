package org.wso2.siddhi.extension.distributed.transport.multi.client.test;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.TcpNettyServer;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;

import java.util.ArrayList;


public class MultiClientDistributedTransportTestCases {
    static final Logger log = Logger.getLogger(MultiClientDistributedTransportTestCases.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testTcpOutputTransport1() throws InterruptedException {
        log.info("tcpInputTransport TestCase 1");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "" +
                "define stream inputStream (a string, b int, c float, d long, e double, f bool); " +

                "@sink(type='tcp', hostname='localhost', streamId='foo', @map(type='passThrough'), " +
                "   @distribution(publishingStrategy='roundRobin', partitionKey='a', " +
                "       @destination(port = '8080'), " +
                "       @destination(port = '8081'))) " +
                "define stream outputStream (a string, b int, c float, d long, e double, f bool);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select *  " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        StreamDefinition streamDefinition = StreamDefinition.id("outputStream").attribute("a", Attribute.Type.STRING)
                .attribute("b", Attribute.Type.INT).attribute("c", Attribute.Type.FLOAT).attribute("d", Attribute.Type.LONG)
                .attribute("e", Attribute.Type.DOUBLE).attribute("f", Attribute.Type.BOOL);

        TcpNettyServer tcpNettyServer = new TcpNettyServer();
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
            }

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        tcpNettyServer.bootServer(new ServerConfig());

        //=========
        TcpNettyServer tcpNettyServer1 = new TcpNettyServer();
        tcpNettyServer1.addStreamListener(new StreamListener() {
            @Override
            public StreamDefinition getStreamDefinition() {
                return streamDefinition;
            }

            @Override
            public void onEvent(Event event) {
                System.out.println(event  + " @ server2");
                eventArrived = true;
                count++;
            }

            @Override
            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }
        });

        ServerConfig conf = new ServerConfig();
        conf.setPort(8081);
        tcpNettyServer1.bootServer(conf);

        //=========
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();

        ArrayList<Event> arrayList = new ArrayList<Event>();
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test", 36, 3.0f, 380l, 23.0, true}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test1", 361, 31.0f, 3801l, 231.0, false}));
        arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"test2", 362, 32.0f, 3802l, 232.0, true}));
        inputHandler.send(arrayList.toArray(new Event[3]));

        Thread.sleep(300);

        //Assert.assertEquals(3, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

        tcpNettyServer.shutdownGracefully();
        tcpNettyServer1.shutdownGracefully();

    }
}
