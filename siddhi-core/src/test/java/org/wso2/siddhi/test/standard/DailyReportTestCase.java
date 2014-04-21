package org.wso2.siddhi.test.standard;

//import com.wso2.analytics.utils.*;
//import com.wso2.analytics.utils.MonthsPassedLoader;
//import com.wso2.analytics.utils.OTArticle2TagLoader;
//import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class DailyReportTestCase {


    static final Logger log = Logger.getLogger(WindowTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }


    @Test
    public void processDailyReport() throws Exception {
//        System.out.println("processDailyReport Test");

        SiddhiManager siddhiManager;
        InputHandler inputHandler;

        int time = 3000;

        siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream OTEventStream (userID string, timestamp long, monthOfTheYear string," +
                                   " date int,monthsPassed int, userOrg string, document string , country string, " +
                                   "name string , referralurl string,tag string , searchTerms string ) ");
        inputHandler = siddhiManager.getInputHandler("OTEventStream");


        //total count
        String totQueryReference = siddhiManager.addQuery("from OTEventStream#window.timeBatch(" + time + ") " +
                                                          "select count(timestamp)as totHits " +
                                                          "insert into totalHits;");

        siddhiManager.addCallback(totQueryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, org.wso2.siddhi.core.event.Event[] inEvents,
                                org.wso2.siddhi.core.event.Event[] removeEvents) {
//                System.out.println("totalHits " + inEvents.length);
                EventPrinter.print(timeStamp, inEvents, removeEvents);

            }
        });

        //tag count
        String tagQueryReference = siddhiManager.addQuery("from OTEventStream#window.timeBatch(" + time + ") " +
                                                          "select  tag, count(tag) as hits " +
                                                          " group by tag " +
                                                          "insert into DailyTagHits;");

        siddhiManager.addCallback(tagQueryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, org.wso2.siddhi.core.event.Event[] inEvents,
                                org.wso2.siddhi.core.event.Event[] removeEvents) {
//                System.out.println("DailyTagHits " + inEvents.length);
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });

        String tagPqueryReference = siddhiManager.addQuery("from totalHits" +
                                                           "#window.time(" + time + ")" +
                                                           " join DailyTagHits" +
                                                           "#window.time(" + time + ") " +
                                                           "insert into joinOut " + ";");
//                                                           "group by DailyTagHits.tag;");

        siddhiManager.addCallback(tagPqueryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, org.wso2.siddhi.core.event.Event[] inEvents,
                                org.wso2.siddhi.core.event.Event[] removeEvents) {
//                System.out.println("joinOut " + (inEvents != null ? inEvents.length : removeEvents.length));
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    count += inEvents.length;
                    eventArrived = true;
                } else {
                    Assert.fail();
                }
            }
        });


        inputHandler.send(new Object[]{"userID", 120l, "monthOfTheYear", 12, 4012, "userOrg", "document", "country", "name", "referralurl", "tag1", "searchTerms"});
        inputHandler.send(new Object[]{"userID", 120l, "monthOfTheYear", 12, 4012, "userOrg", "document", "country", "name", "referralurl", "tag2", "searchTerms"});

        Thread.sleep(5000);

        siddhiManager.shutdown();
        Assert.assertEquals(2, count);
        Assert.assertEquals(true, eventArrived);

    }


}
