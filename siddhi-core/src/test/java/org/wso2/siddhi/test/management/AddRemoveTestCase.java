/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.management;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class AddRemoveTestCase {
    static final Logger log = Logger.getLogger(AddRemoveTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testQuery1() throws InterruptedException, SiddhiParserException {
        log.info("Remove Query test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        siddhiManager.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count++;
            }
        });

        String queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                       "select symbol, price, volume " +
                                                       " having price*12 >100 " +
                                                       "insert into outStream ;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});


        siddhiManager.removeQuery(queryReference);
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        siddhiManager.shutdown();

        Assert.assertEquals(2, count);

    }

    @Test
    public void testQuery2() throws InterruptedException, SiddhiParserException {

        log.info("Remove then Add Query test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        siddhiManager.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count++;
            }
        });

        String queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                       "select symbol, price, volume " +
                                                       "insert into outStream;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
//

        siddhiManager.removeQuery(queryReference);

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                "select symbol, price, volume " +
                                                "insert into outStream;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        siddhiManager.shutdown();

        Assert.assertEquals(4, count);

    }

    @Test
    public void testQuery3() throws InterruptedException, SiddhiParserException {

        log.info("Remove then Add different Query test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseStream ( symbol string, price float, volume int )");

        siddhiManager.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue((events[0].getData().length == 2) || (events[0].getData().length == 3));
                if (events[0].getData().length == 2) {
                    count++;
                } else if (events[0].getData().length == 3) {
                    count--;
                }
                eventArrived = true;
            }
        });

        String queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                       "select symbol, price, volume " +
                                                       "insert into outStream;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
//

        siddhiManager.removeQuery(queryReference);

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});

        queryReference = siddhiManager.addQuery("from cseStream[price>10] " +
                                                "select symbol, volume " +
                                                "insert into outStream ;");

        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        siddhiManager.shutdown();

        Assert.assertEquals(0, count);
        Assert.assertEquals(true, eventArrived);

    }

    @Test
    public void testQuery4() throws InterruptedException, SiddhiParserException {

        log.info("Remove Multiple Queries test1");


        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler allStockQuotesHandler = siddhiManager.defineStream("define stream allStockQuotes ( symbol string, price double )");
        InputHandler twitterFeedHandler = siddhiManager.defineStream("define stream twitterFeed ( company string, wordCount int )");

        siddhiManager.addCallback("outStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertTrue("IBM".equals(events[0].getData(0)) || "WSO2".equals(events[0].getData(0)));
                count++;
            }
        });

        String queryReference1 = siddhiManager.addQuery("                    from allStockQuotes#window.time(600000) \n" +
                                                        "                    select symbol as symbol, price, avg(price) as averagePrice \n" +
                                                        "                    group by symbol \n" +
                                                        "                    having ( price >  averagePrice*1.02 ) or ( averagePrice*0.98 > price ) " +
                                                        "                    insert into fastMovingStockQuotes \n" +
                                                        "                    ");


//        String queryReference2 = siddhiManager.addQuery("from twitterFeed#window.time(600000)\n" +
//                                                        "                    insert into highFrequentTweets\n" +
//                                                        "                    company as company, sum(wordCount) as words\n" +
//                                                        "                    group by company\n" +
//                                                        "                    having (words > 10);");
//
//        String queryReference3 = siddhiManager.addQuery(" from fastMovingStockQuotes#window.time(60000) as fastMovingStockQuotes join\n" +
//                                                        "                    highFrequentTweets#window.time(60000) as highFrequentTweets\n" +
//                                                        "\t\t    on fastMovingStockQuotes.symbol == highFrequentTweets.company\n" +
//                                                        "                    insert into predictedStockQuotes\n" +
//                                                        "                    fastMovingStockQuotes.symbol as company, fastMovingStockQuotes.averagePrice as amount, highFrequentTweets.words as words");
//

        siddhiManager.removeQuery(queryReference1);
//        siddhiManager.removeQuery(queryReference2);
//        siddhiManager.removeQuery(queryReference3);

        siddhiManager.shutdown();

    }

}
