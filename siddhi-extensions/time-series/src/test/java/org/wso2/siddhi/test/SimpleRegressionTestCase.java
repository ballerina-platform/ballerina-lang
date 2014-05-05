package org.wso2.siddhi.test;

import junit.framework.Assert;
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

        siddhiConfiguration.setSiddhiExtensions(list);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( ci double, y double, x double )");

        String queryReference = siddhiManager.addQuery("from DataStream#transform.timeseries:regress(ci, y, x) \n" +
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

        double ci = 0.95;
        inputHandler.send(new Object[]{ ci,  3300, 31 });
        inputHandler.send(new Object[]{ ci,  2600, 18 });
        inputHandler.send(new Object[]{ ci,  2500, 17 });
        inputHandler.send(new Object[]{ ci,  2475, 12 });
        inputHandler.send(new Object[]{ ci,  2313, 8 });
        inputHandler.send(new Object[]{ ci,  2175, 26 });
        inputHandler.send(new Object[]{ ci,  600, 14 });
        inputHandler.send(new Object[]{ ci,  460, 3 });
        inputHandler.send(new Object[]{ ci,  240, 1 });
        inputHandler.send(new Object[]{ ci,  200, 10 });
        inputHandler.send(new Object[]{ ci,  177, 0 });
        inputHandler.send(new Object[]{ ci,  140, 6 });
        inputHandler.send(new Object[]{ ci,  117, 1 });
        inputHandler.send(new Object[]{ ci,  115, 0 });
        inputHandler.send(new Object[]{ ci,  2600, 19 });
        inputHandler.send(new Object[]{ ci,  1907, 13 });
        inputHandler.send(new Object[]{ ci,  1190, 3 });
        inputHandler.send(new Object[]{ ci,  990, 16 });
        inputHandler.send(new Object[]{ ci,  925, 6 });
        inputHandler.send(new Object[]{ ci,  365, 0 });
        inputHandler.send(new Object[]{ ci,  302, 10 });
        inputHandler.send(new Object[]{ ci,  300, 6 });
        inputHandler.send(new Object[]{ ci,  129, 2 });
        inputHandler.send(new Object[]{ ci,  111, 1 });
        inputHandler.send(new Object[]{ ci,  6100, 18 });
        inputHandler.send(new Object[]{ ci,  4125, 19 });
        inputHandler.send(new Object[]{ ci,  3213, 1 });
        inputHandler.send(new Object[]{ ci,  2319, 38 });
        inputHandler.send(new Object[]{ ci,  2000, 10 });
        inputHandler.send(new Object[]{ ci,  1600, 0 });
        inputHandler.send(new Object[]{ ci,  1394, 4 });
        inputHandler.send(new Object[]{ ci,  935, 4 });
        inputHandler.send(new Object[]{ ci,  850, 0 });
        inputHandler.send(new Object[]{ ci,  775, 5 });
        inputHandler.send(new Object[]{ ci,  760, 6 });
        inputHandler.send(new Object[]{ ci,  629, 1 });
        inputHandler.send(new Object[]{ ci,  275, 6 });
        inputHandler.send(new Object[]{ ci,  120, 0 });
        inputHandler.send(new Object[]{ ci,  2567, 12 });
        inputHandler.send(new Object[]{ ci,  2500, 28 });
        inputHandler.send(new Object[]{ ci,  2350, 21 });
        inputHandler.send(new Object[]{ ci,  2317, 3 });
        inputHandler.send(new Object[]{ ci,  2000, 12 });
        inputHandler.send(new Object[]{ ci,  715, 1 });
        inputHandler.send(new Object[]{ ci,  660, 9 });
        inputHandler.send(new Object[]{ ci,  650, 0 });
        inputHandler.send(new Object[]{ ci,  260, 0 });
        inputHandler.send(new Object[]{ ci,  250, 1 });
        inputHandler.send(new Object[]{ ci,  200, 13 });
        inputHandler.send(new Object[]{ ci,  180, 6 });


        Thread.sleep(1000);
        siddhiManager.shutdown();

        Assert.assertEquals("No of events: ", 50, count);
        Assert.assertEquals("Beta0: ", 573.1418421169498, betaZero);

    }
}
