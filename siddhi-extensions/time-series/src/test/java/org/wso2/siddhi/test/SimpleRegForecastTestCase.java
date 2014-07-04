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
public class SimpleRegForecastTestCase
{
    static final Logger log = Logger.getLogger(SimpleRegForecastTestCase.class);

    private int count;
    private double betaZero;

    @Before
    public void init() {
        count = 0;
    }

    @Test
    public void testRegression() throws InterruptedException {
        log.info("Regression Test 5 - Simple Linear Forecast");

        SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

        List<Class> list = new ArrayList<Class>();
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionForecastTransformProcessor.class);
        list.add(org.wso2.siddhi.extension.timeseries.LinearRegressionOutlierTransformProcessor.class);

        siddhiConfiguration.setSiddhiExtensions(list);

        SiddhiManager siddhiManager = new SiddhiManager(siddhiConfiguration);

        InputHandler inputHandler = siddhiManager.defineStream("define stream DataStream ( y double, symbol string, x double )");

        String queryReference = siddhiManager.addQuery("from DataStream#transform.timeseries:forecast( 1, 1000000, 0.95, x+2, y, x) \n" +
                "        select *  \n" +
                "        insert into RegressionResult;\n");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if(inEvents[0].getData1()!=null) {
                    betaZero = (Double) inEvents[0].getData2();
                    System.out.println("beta0: "+betaZero);
                }
                count++;

            }
        });

        System.out.println(System.currentTimeMillis());

        inputHandler.send(new Object[]{3300,"IBM",31});
        inputHandler.send(new Object[]{2600,"GOOG",18});
        inputHandler.send(new Object[]{2500,"GOOG",17});
        inputHandler.send(new Object[]{2475,"APPL",12});
        inputHandler.send(new Object[]{2313,"MSFT",8});
        inputHandler.send(new Object[]{2175,"IBM",26});
        inputHandler.send(new Object[]{600,"APPL",14});
        inputHandler.send(new Object[]{460,"APPL",3});
        inputHandler.send(new Object[]{240,"IBM",1});
        inputHandler.send(new Object[]{200,"GOOG",10});
        inputHandler.send(new Object[]{177,"GOOG",0});
        inputHandler.send(new Object[]{140,"APPL",6});
        inputHandler.send(new Object[]{117,"MSFT",1});
        inputHandler.send(new Object[]{115,"IBM",0});
        inputHandler.send(new Object[]{2600,"APPL",19});
        inputHandler.send(new Object[]{1907,"APPL",13});
        inputHandler.send(new Object[]{1190,"IBM",3});
        inputHandler.send(new Object[]{990,"GOOG",16});
        inputHandler.send(new Object[]{925,"GOOG",6});
        inputHandler.send(new Object[]{365,"APPL",0});
        inputHandler.send(new Object[]{302,"MSFT",10});
        inputHandler.send(new Object[]{300,"IBM",6});
        inputHandler.send(new Object[]{129,"APPL",2});
        inputHandler.send(new Object[]{111,"APPL",1});
        inputHandler.send(new Object[]{6100,"IBM",18});
        inputHandler.send(new Object[]{4125,"GOOG",19});
        inputHandler.send(new Object[]{3213,"GOOG",1});
        inputHandler.send(new Object[]{2319,"APPL",38});
        inputHandler.send(new Object[]{2000,"MSFT",10});
        inputHandler.send(new Object[]{1600,"IBM",0});
        inputHandler.send(new Object[]{1394,"APPL",4});
        inputHandler.send(new Object[]{935,"APPL",4});
        inputHandler.send(new Object[]{850,"IBM",0});
        inputHandler.send(new Object[]{775,"GOOG",5});
        inputHandler.send(new Object[]{760,"GOOG",6});
        inputHandler.send(new Object[]{629,"APPL",1});
        inputHandler.send(new Object[]{275,"MSFT",6});
        inputHandler.send(new Object[]{120,"IBM",0});
        inputHandler.send(new Object[]{2567,"APPL",12});
        inputHandler.send(new Object[]{2500,"APPL",28});
        inputHandler.send(new Object[]{2350,"IBM",21});
        inputHandler.send(new Object[]{2317,"GOOG",3});
        inputHandler.send(new Object[]{2000,"GOOG",12});
        inputHandler.send(new Object[]{715,"APPL",1});
        inputHandler.send(new Object[]{660,"MSFT",9});
        inputHandler.send(new Object[]{650,"IBM",0});
        inputHandler.send(new Object[]{260,"APPL",0});
        inputHandler.send(new Object[]{250,"APPL",1});
        inputHandler.send(new Object[]{200,"IBM",13});
        inputHandler.send(new Object[]{180,"GOOG",6});

        Thread.sleep(1000);
        siddhiManager.shutdown();

        double delta=0;
        Assert.assertEquals("No of events: ", 50, count);
        Assert.assertEquals(573.1418421169493, betaZero, delta);
    }
}


