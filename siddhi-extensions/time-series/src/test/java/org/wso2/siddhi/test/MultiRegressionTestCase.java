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
 * Created by seshika on 4/10/14.
 */
public class MultiRegressionTestCase
{
    static final Logger log = Logger.getLogger(MultiRegressionTestCase.class);
    private int count=0;
    private double betaOne;

    @Before
    public void init() {}

    @Test
    public void testRegression() throws InterruptedException {
        log.info("Regression test2 - Multiple Linear");

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

        List<Class> list = new ArrayList<Class>();
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionTransformProcessor.class);

        siddhiConfiguration.setSiddhiExtensions(list);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( y double, x1 double, x2 double , x3 double, x4 int )");

        String queryReference = siddhiManager.addQuery("from DataStream#transform.timeseries:regress( 3, 100, 0.95, y, x1, x2, x3, x4 ) \n" +
                "        select *  \n" +
                "        insert into RegressionResult;\n");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                if(inEvents[0].getData2()!=null){
                    betaOne = (Double) inEvents[0].getData2();
                }
                count++;
            }
        });

        System.out.println(System.currentTimeMillis());

            inputHandler.send(new Object[]{ 3300 , 104 , 22 , 80 , 3 });
            inputHandler.send(new Object[]{ 2600 , 66 , 39 , 69 , 3 });
            inputHandler.send(new Object[]{ 2500 , 73 , 63 , 116 , 5 });
            inputHandler.send(new Object[]{ 2475 , 50 , 23 , 64 , 21 });
            inputHandler.send(new Object[]{ 2313 , 58 , 70 , 53 , 8 });
            inputHandler.send(new Object[]{ 2175 , 100 , 87 , 89 , 4 });
            inputHandler.send(new Object[]{ 600 , 38 , 15 , 45 , 10 });
            inputHandler.send(new Object[]{ 460 , 21 , 11 , 32 , 3 });
            inputHandler.send(new Object[]{ 240 , 18 , 24 , 26 , 2 });
            inputHandler.send(new Object[]{ 200 , 33 , 14 , 96 , 6 });
            inputHandler.send(new Object[]{ 177 , 10 , 5 , 18 , 7 });
            inputHandler.send(new Object[]{ 140 , 22 , 19 , 56 , 3 });
            inputHandler.send(new Object[]{ 117 , 3 , 2 , 1 , 0 });
            inputHandler.send(new Object[]{ 115 , 2 , 4 , 3 , 0 });
            inputHandler.send(new Object[]{ 2600 , 75 , 53 , 64 , 7 });
            inputHandler.send(new Object[]{ 1907 , 73 , 50 , 100 , 14 });
            inputHandler.send(new Object[]{ 1190 , 26 , 42 , 61 , 8 });
            inputHandler.send(new Object[]{ 990 , 64 , 42 , 102 , 6 });
            inputHandler.send(new Object[]{ 925 , 26 , 22 , 26 , 5 });
            inputHandler.send(new Object[]{ 365 , 15 , 14 , 30 , 6 });
            inputHandler.send(new Object[]{ 302 , 51 , 95 , 151 , 27 });
            inputHandler.send(new Object[]{ 300 , 39 , 34 , 89 , 6 });
            inputHandler.send(new Object[]{ 129 , 18 , 20 , 22 , 5 });
            inputHandler.send(new Object[]{ 111 , 8 , 1 , 18 , 0 });
            inputHandler.send(new Object[]{ 6100 , 100 , 90 , 67 , 15 });
            inputHandler.send(new Object[]{ 4125 , 96 , 55 , 74 , 7 });
            inputHandler.send(new Object[]{ 3213 , 17 , 39 , 47 , 3 });
            inputHandler.send(new Object[]{ 2319 , 117 , 78 , 120 , 31 });
            inputHandler.send(new Object[]{ 2000 , 40 , 36 , 56 , 4 });
            inputHandler.send(new Object[]{ 1600 , 31 , 50 , 69 , 15 });
            inputHandler.send(new Object[]{ 1394 , 51 , 83 , 50 , 5 });
            inputHandler.send(new Object[]{ 935 , 21 , 30 , 42 , 3 });
            inputHandler.send(new Object[]{ 850 , 54 , 75 , 38 , 20 });
            inputHandler.send(new Object[]{ 775 , 35 , 9 , 19 , 3 });
            inputHandler.send(new Object[]{ 760 , 36 , 40 , 53 , 14 });
            inputHandler.send(new Object[]{ 629 , 30 , 24 , 43 , 0 });
            inputHandler.send(new Object[]{ 275 , 34 , 33 , 57 , 8 });
            inputHandler.send(new Object[]{ 120 , 5 , 14 , 19 , 2 });
            inputHandler.send(new Object[]{ 2567 , 42 , 41 , 66 , 8 });
            inputHandler.send(new Object[]{ 2500 , 81 , 48 , 93 , 5 });
            inputHandler.send(new Object[]{ 2350 , 92 , 67 , 100 , 3 });
            inputHandler.send(new Object[]{ 2317 , 12 , 37 , 20 , 4 });
            inputHandler.send(new Object[]{ 2000 , 40 , 12 , 57 , 9 });
            inputHandler.send(new Object[]{ 715 , 11 , 16 , 36 , 3 });
            inputHandler.send(new Object[]{ 660 , 49 , 14 , 49 , 9 });
            inputHandler.send(new Object[]{ 650 , 15 , 30 , 30 , 4 });
            inputHandler.send(new Object[]{ 260 , 12 , 13 , 14 , 0 });
            inputHandler.send(new Object[]{ 250 , 11 , 2 , 26 , 2 });
            inputHandler.send(new Object[]{ 200 , 50 , 31 , 73 , 3 });
            inputHandler.send(new Object[]{ 180 , 21 , 17 , 26 , 8 });

        Thread.sleep(1000);
        siddhiManager.shutdown();

        Assert.assertEquals("No of events: ", 50, count);
        Assert.assertEquals("Beta0: ", 34.42398117538763, betaOne);

    }

}
