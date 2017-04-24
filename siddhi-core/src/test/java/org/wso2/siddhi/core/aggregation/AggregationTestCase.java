package org.wso2.siddhi.core.aggregation;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.parser.AggregationParser;
import org.wso2.siddhi.core.util.parser.AggregationRuntime;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

public class AggregationTestCase {

    @Test
    public void basicTest() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation").from(
                InputStream.stream("StockStream")).
                select(Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol").ofStream("StockStream"))).
                aggregateBy(Expression.variable("timeStamp")).every(
                TimePeriod.interval(TimePeriod.Duration.SECONDS, TimePeriod.Duration.MINUTES));


        //SiddhiContext siddhicontext = new SiddhiContext();
        //ExecutionPlanContext context = new ExecutionPlanContext();
        //context.setSiddhiContext(siddhicontext);
        //AggregationRuntime aggregationRuntime =  AggregationParser.parse(aggregationDefinition, context);

    }

    @Test
    public void functionTest3() throws InterruptedException {
//        log.info("function test 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream cseEventStream (symbol string, price1 float, price2 float, volume long , quantity int);";
        String query = " define aggregation test " +
                "from cseEventStream " +
                "select symbol, avg(price1) as avgPrice " +
                "group by symbol " +
                "aggregate every sec, min ;";
        //String query = " @info(name = 'query1') from cseEventStream select symbol, sum(volume) as vol group by symbol insert into outputStream; ";


        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

//        executionPlanRuntime.addCallback("query1", new QueryCallback() {
//            int count = 0;
//            @Override
//            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                for (Event inEvent : inEvents) {
//                    count++;
//                    if (count == 1) {
//                        //Assert.assertEquals(50.0f, inEvent.getData()[1]);
//                    } else if (count == 2) {
//                        //Assert.assertEquals(70.0f, inEvent.getData()[1]);
//                    } else if (count == 3) {
//                        //Assert.assertEquals(44.0f, inEvent.getData()[1]);
//                    }
//                }
//            }
//        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, null, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", null, 44f, 200l, 56});
        inputHandler.send(new Object[]{"WSO2", null, null, 200l, 56});
        Thread.sleep(100);
//        junit.framework.Assert.assertEquals(3, count);
        executionPlanRuntime.shutdown();

    }

}
