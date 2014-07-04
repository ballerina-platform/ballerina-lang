package org.wso2.siddhi.test;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seshika on 4/7/14.
 */
public class SimpleRegressionTestCase
{
    static final Logger log = Logger.getLogger(SimpleRegressionTestCase.class);

    private int count;
    private double betaZero;

    @Before
    public void init() {
        count = 0;
    }

    @Test
    public void testRegression() throws InterruptedException {
        log.info("Regression Test 1 - Simple Linear");

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

        List<Class> list = new ArrayList<Class>();
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionForecastTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionOutlierTransformProcessor.class);

        siddhiConfiguration.setSiddhiExtensions(list);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( y double, x double )");

        String queryReference = siddhiManager.addQuery("from DataStream#transform.timeseries:regress( 1, 1000000, 0.95,  y, x) \n" +
                "        select *  \n" +
                "        insert into RegressionResult;\n");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents[0].getData1() != null) {
                    betaZero = (Double) inEvents[0].getData1();
                }
                count++;
            }
        });

        System.out.println(System.currentTimeMillis());

        inputHandler.send(new Object[]{3300.00,31.00});
        inputHandler.send(new Object[]{2600.00,18.00});
        inputHandler.send(new Object[]{2500.00,17.00});
        inputHandler.send(new Object[]{2475.00,12.00});
        inputHandler.send(new Object[]{2313.00,8.00});
        inputHandler.send(new Object[]{2175.00,26.00});
        inputHandler.send(new Object[]{600.00,14.00});
        inputHandler.send(new Object[]{460.00,3.00});
        inputHandler.send(new Object[]{240.00,1.00});
        inputHandler.send(new Object[]{200.00,10.00});
        inputHandler.send(new Object[]{177.00,0.00});
        inputHandler.send(new Object[]{140.00,6.00});
        inputHandler.send(new Object[]{117.00,1.00});
        inputHandler.send(new Object[]{115.00,0.00});
        inputHandler.send(new Object[]{2600.00,19.00});
        inputHandler.send(new Object[]{1907.00,13.00});
        inputHandler.send(new Object[]{1190.00,3.00});
        inputHandler.send(new Object[]{990.00,16.00});
        inputHandler.send(new Object[]{925.00,6.00});
        inputHandler.send(new Object[]{365.00,0.00});
        inputHandler.send(new Object[]{302.00,10.00});
        inputHandler.send(new Object[]{300.00,6.00});
        inputHandler.send(new Object[]{129.00,2.00});
        inputHandler.send(new Object[]{111.00,1.00});
        inputHandler.send(new Object[]{6100.00,18.00});
        inputHandler.send(new Object[]{4125.00,19.00});
        inputHandler.send(new Object[]{3213.00,1.00});
        inputHandler.send(new Object[]{2319.00,38.00});
        inputHandler.send(new Object[]{2000.00,10.00});
        inputHandler.send(new Object[]{1600.00,0.00});
        inputHandler.send(new Object[]{1394.00,4.00});
        inputHandler.send(new Object[]{935.00,4.00});
        inputHandler.send(new Object[]{850.00,0.00});
        inputHandler.send(new Object[]{775.00,5.00});
        inputHandler.send(new Object[]{760.00,6.00});
        inputHandler.send(new Object[]{629.00,1.00});
        inputHandler.send(new Object[]{275.00,6.00});
        inputHandler.send(new Object[]{120.00,0.00});
        inputHandler.send(new Object[]{2567.00,12.00});
        inputHandler.send(new Object[]{2500.00,28.00});
        inputHandler.send(new Object[]{2350.00,21.00});
        inputHandler.send(new Object[]{2317.00,3.00});
        inputHandler.send(new Object[]{2000.00,12.00});
        inputHandler.send(new Object[]{715.00,1.00});
        inputHandler.send(new Object[]{660.00,9.00});
        inputHandler.send(new Object[]{650.00,0.00});
        inputHandler.send(new Object[]{260.00,0.00});
        inputHandler.send(new Object[]{250.00,1.00});
        inputHandler.send(new Object[]{200.00,13.00});
        inputHandler.send(new Object[]{180.00,6.00});

        Thread.sleep(1000);
        siddhiManager.shutdown();

//        Assert.assertEquals("No of events: ", 50, count);
//        Assert.assertEquals("Beta0: ", 573.1418421169498, betaZero);

    }
}


