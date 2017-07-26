package org.wso2.siddhi.core.store;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class StoreQueryWindowTestCase {

    private static final Logger log = Logger.getLogger(StoreQueryWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void test1() throws InterruptedException {
        log.info("Test1 window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define window StockWindow (symbol string, price float, volume long) length(2); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockWindow ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockWindow ");
        EventPrinter.print(events);
        Assert.assertEquals(2, events.length);

        events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > 75 ");
        EventPrinter.print(events);
        Assert.assertEquals(1, events.length);

        events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > volume*3/4  ");
        EventPrinter.print(events);
        Assert.assertEquals(1, events.length);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void test2() throws InterruptedException {
        log.info("Test2 window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define window StockWindow (symbol string, price float, volume long) length(3); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockWindow ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > 75 " +
                "select symbol, volume ");
        EventPrinter.print(events);
        Assert.assertEquals(1, events.length);
        Assert.assertEquals(2, events[0].getData().length);

        events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > 5 " +
                "select symbol, volume " +
                "having symbol == 'WSO2' ");
        EventPrinter.print(events);
        Assert.assertEquals(2, events.length);

        siddhiAppRuntime.shutdown();

    }

    @Test
    public void test3() throws InterruptedException {
        log.info("Test3 window");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define window StockWindow (symbol string, price float, volume long) length(3); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockWindow ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(streams + query);

        InputHandler stockStream = siddhiAppRuntime.getInputHandler("StockStream");

        siddhiAppRuntime.start();

        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(500);

        Event[] events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > 5 " +
                "select symbol, sum(volume) as totalVolume " +
                "group by symbol " +
                "having totalVolume >150 ");
        EventPrinter.print(events);
        Assert.assertEquals(1, events.length);
        Assert.assertEquals(200L, events[0].getData(1));

        events = siddhiAppRuntime.query("" +
                "from StockWindow " +
                "on price > 5 " +
                "select symbol, sum(volume) as totalVolume " +
                "group by symbol  ");
        EventPrinter.print(events);
        Assert.assertEquals(2, events.length);

        siddhiAppRuntime.shutdown();

    }
}
