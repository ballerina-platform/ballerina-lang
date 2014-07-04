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
public class DiscreteSeasonalityTestCase
{
    static final Logger log = Logger.getLogger(SimpleRegressionTestCase.class);
    private double betaZero;
    private int count = 0;

    @Before
    public void init() {
    }

    @Test
    public void testRegression() throws InterruptedException {
        log.info("Regression Test - Timeseries with Discrete Seasonality");

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

        List<Class> list = new ArrayList<Class>();
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionForecastTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionOutlierTransformProcessor.class);

        siddhiConfiguration.setSiddhiExtensions(list);
        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( y double, t long )");


        siddhiManager.addQuery("from DataStream " +
                " select y, t, convert( t*1000, string, 'MM' ) as eventMonth " +
                " insert into tempStream1;");

        siddhiManager.addQuery("from tempStream1 " +
                " select y, t, eventMonth" +
                " insert into tempStream2;");

        siddhiManager.addQuery("from tempStream2[eventMonth contains '12'] " +
                " select y, t, 1 as dum1" +
                " insert into tempStream3;");

        siddhiManager.addQuery("from tempStream2[not(eventMonth contains '12')] " +
                " select y, t, 0 as dum1" +
                " insert into tempStream3;");

        String queryReference = siddhiManager.addQuery("from tempStream3#transform.timeseries:regress( 1, 1000, 0.95, y, dum1) \n" +
                "        select *  \n" +
                "        insert into RegressionResult;\n");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                if(inEvents[0].getData1()!=null) {
                    betaZero = (Double) inEvents[0].getData1();
                }
                count++;
            }
        });

        double ci = 0.95;

        inputHandler.send(new Object[]{ 3439, 694224000 });
        inputHandler.send(new Object[]{ 3264, 696902400 });
        inputHandler.send(new Object[]{ 3437, 699408000 });
        inputHandler.send(new Object[]{ 3523, 702086400 });
        inputHandler.send(new Object[]{ 3545, 704678400 });
        inputHandler.send(new Object[]{ 3611, 707356800 });
        inputHandler.send(new Object[]{ 3637, 709948800 });
        inputHandler.send(new Object[]{ 3986, 712627200 });
        inputHandler.send(new Object[]{ 3797, 715305600 });
        inputHandler.send(new Object[]{ 3758, 717897600 });
        inputHandler.send(new Object[]{ 4428, 720576000 });
        inputHandler.send(new Object[]{ 8566, 723168000 });
        inputHandler.send(new Object[]{ 3801, 725846400 });
        inputHandler.send(new Object[]{ 3204, 728524800 });
        inputHandler.send(new Object[]{ 3686, 730944000 });
        inputHandler.send(new Object[]{ 3827, 733622400 });
        inputHandler.send(new Object[]{ 3770, 736214400 });
        inputHandler.send(new Object[]{ 3923, 738892800 });
        inputHandler.send(new Object[]{ 3839, 741484800 });
        inputHandler.send(new Object[]{ 4270, 744163200 });
        inputHandler.send(new Object[]{ 3988, 746841600 });
        inputHandler.send(new Object[]{ 3920, 749433600 });
        inputHandler.send(new Object[]{ 4853, 752112000 });
        inputHandler.send(new Object[]{ 9010, 754704000 });

        Thread.sleep(1000);


        siddhiManager.shutdown();
        Assert.assertEquals("No of events: ", 24, count);
        Assert.assertEquals("Beta0: ", 3795.7272727272725, betaZero);

    }
}
