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
 * Created by seshika on 4/25/14.
 */
public class ContinuousSeasonalityTestCase
{
    static final Logger log = Logger.getLogger(SimpleRegressionTestCase.class);
    private int count=0;
    private double betaZero;
    @Before
    public void init() {
    }

    @Test
    public void testRegression() throws InterruptedException {
        log.info("Regression Test - Timeseries with Continuous Seasonality");

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

        List<Class> list = new ArrayList<Class>();
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionForecastTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionOutlierTransformProcessor.class);

        siddhiConfiguration.setSiddhiExtensions(list);
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( y double, x1 double )");

        siddhiManager.addQuery("from DataStream " +
                " select y, x1, sin(x1) as sinx1 " +
                " insert into tempStream1;");

        String queryReference = siddhiManager.addQuery("from tempStream1#transform.timeseries:regress( 1, 1000, 0.95, y, x1, sinx1) \n" +
                "        select *  \n" +
                "        insert into RegressionResult;\n");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                count++;
                if(inEvents[0].getData1()!=null){
                    betaZero= (Double) inEvents[0].getData1();
                }
            }
        });

        double ci = 0.95;

        inputHandler.send(new Object[]{ 5.49, 0.00 });
        inputHandler.send(new Object[]{ 6.79, 1.00 });
        inputHandler.send(new Object[]{ 6.46, 2.00 });
        inputHandler.send(new Object[]{ 6.24, 3.00 });
        inputHandler.send(new Object[]{ 5.05, 4.00 });
        inputHandler.send(new Object[]{ 4.92, 5.00 });
        inputHandler.send(new Object[]{ 5.64, 6.00 });
        inputHandler.send(new Object[]{ 7.33, 7.00 });
        inputHandler.send(new Object[]{ 7.55, 8.00 });
        inputHandler.send(new Object[]{ 6.87, 9.00 });
        inputHandler.send(new Object[]{ 6.20, 10.00 });
        inputHandler.send(new Object[]{ 5.79, 11.00 });
        inputHandler.send(new Object[]{ 6.56, 12.00 });
        inputHandler.send(new Object[]{ 6.71, 13.00 });
        inputHandler.send(new Object[]{ 7.41, 14.00 });
        inputHandler.send(new Object[]{ 7.97, 15.00 });
        inputHandler.send(new Object[]{ 6.51, 16.00 });
        inputHandler.send(new Object[]{ 5.95, 17.00 });
        inputHandler.send(new Object[]{ 6.40, 18.00 });
        inputHandler.send(new Object[]{ 7.88, 19.00 });
        inputHandler.send(new Object[]{ 7.92, 20.00 });

        Thread.sleep(1000);

        siddhiManager.shutdown();
        Assert.assertEquals("No of events: ", 21, count);
        Assert.assertEquals("Beta0: ",  5.661764084669217, betaZero);
    }
}
