package org.wso2.siddhi.core.aggregation;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.ElementIdGenerator;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.parser.AggregationParser;
import org.wso2.siddhi.core.util.parser.AggregationRuntime;
import org.wso2.siddhi.core.util.snapshot.SnapshotService;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationTestCase {

    static final Logger log = Logger.getLogger(AggregationTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void basicTest() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation").from(
                InputStream.stream("StockStream")).
                select(Selector.basicSelector().
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol").ofStream("StockStream"))).
                aggregateBy(Expression.variable("timestamp")).every(
                TimePeriod.interval(TimePeriod.Duration.SECONDS, TimePeriod.Duration.MINUTES));


        //SiddhiContext siddhicontext = new SiddhiContext();
        //ExecutionPlanContext context = new ExecutionPlanContext();
        //context.setSiddhiContext(siddhicontext);
        //AggregationRuntime aggregationRuntime =  AggregationParser.parse(aggregationDefinition, context);

    }

    @Test
    public void functionTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int, timestamp long);";
        String query = " define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice, sum(price1) as totprice1 " +
                "group by symbol "+
                "aggregate every sec...min ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 90l, 6, 1496289950l});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10, 1496289990l});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"WSO2", 60f, 44f, 200l, 56, 1496291990l}); // TODO: 5/18/17 check with null later
        inputHandler.send(new Object[]{"WSO2", 100f, null, 200l, 56, 1496291990l});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"IBM", 100f, null, 200l, 56, 1496292992l});
        inputHandler.send(new Object[]{"IBM", 100f, null, 200l, 56, 1496292993l});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void existentAggregate() throws InterruptedException {
        log.info("existentAggregate");

        SiddhiManager siddhiManager = new SiddhiManager();

        /*String executionPlan = "" +
                "@Plan:name('EventOutputRateLimitTest16') " +
                "" +
                "define stream LoginEvents (timestamp long, ip string);" +
                "" +
                "@info(name = 'query1') " +
                "from LoginEvents#window.lengthBatch(4) " +
                "select  ip , count() as total " +
                "group by ip " +
                "output all every 2 events " +
                "insert expired events into uniqueIps ;";*/

        String executionPlan = "" +
                "@Plan:name('xxx') " +
                        "" +
                        "define stream TempStream (temp int, roomNo string, deviceID string);" +
                        "" +
                        "@info(name = 'query1') " +
                "from TempStream#window.timeBatch(1 sec) " +
                "select avg(temp) as avgTemp, roomNo, deviceID " +
                "group by roomNo " +
                "insert into AvgTempStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        log.info("Running : " + executionPlanRuntime.getName());

        executionPlanRuntime.addCallback("AvgTempStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count++;
            }
        });

        /*executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                if (removeEvents != null) {
                    count += removeEvents.length;
                } else {
                    Assert.fail("InEvents emitted");
                }
                eventArrived = true;
            }

        });*/

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("TempStream");

        executionPlanRuntime.start();

        inputHandler.send(new Object[]{30, "A", "192.10.1.5"});
        inputHandler.send(new Object[]{55, "B", "192.10.1.3"});
        inputHandler.send(new Object[]{66, "A", "192.10.1.3"});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{54, "A", "192.10.1.9"});
        inputHandler.send(new Object[]{60, "B", "192.10.1.3"});
        /*inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.4"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.30"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.31"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.32"});
        inputHandler.send(new Object[]{System.currentTimeMillis(), "192.10.1.33"});*/
        Thread.sleep(1000);

//        Assert.assertEquals("Event arrived", true, eventArrived);
//        Assert.assertEquals("Number of output event value", 4, count);

        executionPlanRuntime.shutdown();
    }

    @Test
    public void test1() {
        StreamDefinition streamDefinition = StreamDefinition.id("cseEventStream").
                attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).
                attribute("volume", Attribute.Type.INT).attribute("timestamp", Attribute.Type.LONG);
        Map<String, AbstractDefinition> streamDefinitionMap = new HashMap<String, AbstractDefinition>();
        streamDefinitionMap.put("cseEventStream", streamDefinition);

        Map<String, AbstractDefinition> tableDefinitionMap = new HashMap<String, AbstractDefinition>();
        Map<String, AbstractDefinition> windowDefinitionMap = new HashMap<String, AbstractDefinition>();
        Map<String, Table> tableMap = new HashMap<String, Table>();
        Map<String, Window> eventWindowMap = new HashMap<String, Window>();
        Map<String, List<Source>> eventSourceMap = new HashMap<String, List<Source>>();
        Map<String, List<Sink>> eventSinkMap = new HashMap<String, List<Sink>>();
        LockSynchronizer lockSynchronizer = new LockSynchronizer();

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation").from(
                InputStream.stream("cseEventStream")).
                select(Selector.basicSelector().
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol").ofStream("StockStream"))).
                aggregateBy(Expression.variable("timestamp")).every(
                TimePeriod.interval(TimePeriod.Duration.SECONDS, TimePeriod.Duration.MINUTES));

//        aggregationDefinition.annotation(Annotation.annotation("info").element("name", "aggregator1")); // TODO: 5/11/17 why this doesn't work?
        SiddhiContext siddhicontext = new SiddhiContext();
        ExecutionPlanContext context = new ExecutionPlanContext();
        context.setSiddhiContext(siddhicontext);
        context.setElementIdGenerator(new ElementIdGenerator(context.getName()));
        context.setSnapshotService(new SnapshotService(context));
        AggregationRuntime aggregationRuntime =  AggregationParser.parse(aggregationDefinition, context, streamDefinitionMap, tableDefinitionMap,
                windowDefinitionMap, tableMap, eventWindowMap, eventSourceMap, eventSinkMap, lockSynchronizer);

        Assert.assertNotNull(aggregationRuntime);

        /*int count = 0;
        for (int i = 0; i < 3; i++) {
            StreamEvent event = new StreamEvent(0, 0, 3);
            event.setOutputData(new Object[]{"WSO2", i * 11f, 5});
            aggregationRuntime.getIncrementalExecutor().execute(event);
        }
*/
        
//        aggregationRuntime.getIncrementalExecutor().execute(); // TODO: 5/9/17 send events to "cseEventStream 
    }
        



}
