package org.wso2.siddhi.core.query.function;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

/**
 * Created by sajithd on 11/26/15.
 */
public class IfThenElseFunctionTestCase {
    static final Logger log = Logger.getLogger(IfThenElseFunctionTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void ifFunctionExtensionTestCase1() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High','Low') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof String);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,status : "
                            + inEvent.getData(1));
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase2() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High',5) as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof String);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,status : "
                            + inEvent.getData(1));
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase3() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(35,'High','Low') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof String);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,status : "
                            + inEvent.getData(1));
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void ifFunctionExtensionTestCase4() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue>35,'High') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof String);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,status : "
                            + inEvent.getData(1));
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void ifFunctionExtensionTestCase5() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue int,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(sensorValue<35,sensorValue*5,sensorValue*10) as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Integer);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof Integer);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,gainValue : "
                            + inEvent.getData(1) + " °C");
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50});
        inputHandler.send(new Object[]{20});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void ifFunctionExtensionTestCase6() throws InterruptedException {
        log.info("IfThenElseFunctionExtension TestCase6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String sensorEventStream = "define stream sensorEventStream (sensorValue double,status string);";

        String query = ("@info(name = 'query1') " +
                "from sensorEventStream " +
                "select sensorValue, ifThenElse(ifThenElse(sensorValue>35,true,false),'High','Low') as status " +
                "insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(sensorEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    junit.framework.Assert.assertTrue(inEvent.getData(0) instanceof Double);
                    junit.framework.Assert.assertTrue(inEvent.getData(1) instanceof String);
                    System.out.println("Event : " + count + ",sensorValue : " + inEvent.getData(0) + " °C ,status : "
                            + inEvent.getData(1));
                }

            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("sensorEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{50.4});
        inputHandler.send(new Object[]{20.4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        executionPlanRuntime.shutdown();
    }


}