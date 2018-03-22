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
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OrderByLimitTestCase {
    private int inEventCount;
    private int removeEventCount;
    private int count;
    private boolean eventArrived;

    @BeforeMethod
    public void init() {
        count = 0;
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void limitTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, price, volume " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(2, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(0) || inEvents[0].getData(2).equals(4));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(4, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, price, volume " +
                "order by symbol " +
                "limit 3 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(3, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(2) || inEvents[0].getData(2).equals(7));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"AAA", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"IBM", 601.5f, 6});
        inputHandler.send(new Object[]{"BBB", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(6, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, sum(price) as totalPrice, volume " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(1, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(3) || inEvents[0].getData(2).equals(7));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(2, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest4() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, sum(price) as totalPrice, volume " +
                "order by symbol " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(1, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(3) || inEvents[0].getData(2).equals(7));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(2, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest5() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, sum(volume) as totalVolume, volume, price " +
                "group by symbol " +
                "order by price, totalVolume " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(2, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(0) || inEvents[0].getData(2).equals(4));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 60.5f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 60.5f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(4, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest6() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, sum(price) as totalPrice, volume " +
                "group by symbol " +
                "order by totalPrice " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(2, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(3) || inEvents[0].getData(2).equals(7));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(4, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest7() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, price, volume " +
                "group by symbol " +
                "order by price " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(2, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(1) || inEvents[0].getData(2).equals(7));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"IBM", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(4, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void limitTest9() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(4) " +
                "select symbol, price, volume " +
                "group by symbol " +
                "order by price " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(1, inEvents.length);
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"IBM", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(8, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest10() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.lengthBatch(4) " +
                "select symbol, sum(price) as totalPrice, volume " +
                "group by symbol " +
                "order by totalPrice desc " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(2, inEvents.length);
                Assert.assertTrue(inEvents[0].getData(2).equals(2) || inEvents[0].getData(2).equals(4));
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"IBM", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 7060.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(4, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void limitTest11() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(4) " +
                "select symbol, price, volume " +
                "order by price " +
                "limit 2 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                Assert.assertEquals(1, inEvents.length);
                inEventCount = inEventCount + inEvents.length;
                eventArrived = true;
            }

        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"IBM", 60.5f, 1});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"XYZ", 60.5f, 3});
        inputHandler.send(new Object[]{"IBM", 700f, 4});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 7});
        Thread.sleep(500);
        AssertJUnit.assertEquals(8, inEventCount);
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
