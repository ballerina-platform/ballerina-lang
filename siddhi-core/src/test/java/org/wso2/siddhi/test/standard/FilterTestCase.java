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
package org.wso2.siddhi.test.standard;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.MalformedAttributeException;
import org.wso2.siddhi.query.api.exception.SourceNotExistException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class FilterTestCase {
    static final Logger log = Logger.getLogger(FilterTestCase.class);

    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFilterQuery1() throws InterruptedException {

        log.info("Filter test1");
        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume"))
        );
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }
        });
//        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery2() throws InterruptedException {
        log.info("Filter test2");
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery3() throws InterruptedException {
        log.info("Filter test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol"))
        );
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery4() throws InterruptedException {
        log.info("Filter test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").
                filter(Condition.compare(Expression.value(70),
                                         Condition.Operator.GREATER_THAN,
                                         Expression.variable("price"))
                )
        );
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price"))
        );
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("WSO2".equals(inEvents[0].getData(0)));
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery5() throws InterruptedException {


        log.info("Filter test5");
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("volume", Expression.variable("volume")).groupBy("symbol")
        );
        query.insertInto("StockQuote");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery6() throws InterruptedException {

        log.info("Filter test6");
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select symbol, price, sum(volume) as sumVolume " +
                                                       "group by symbol " +
                                                       "insert into StockQuote;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery7() throws InterruptedException {

        log.info("Filter test7");
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select symbol, volume/1000 as vol, sum(volume) as sumVolume " +
                                                       "group by symbol " +
                                                       "insert into StockQuote;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"IBM", 75.6f, 100});
        inputHandler.send(new Object[]{"WSO2", 75.6f, 100});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery8() throws InterruptedException {
        log.info("Filter test8");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("price", Expression.variable("price")).
                        select("increasedVolume", Expression.add(Expression.value(100), Expression.variable("volume"))).
                        select("decreasedVolume", Expression.minus(Expression.variable("volume"), Expression.value(50))).
                        select("multipliedVolume", Expression.multiply(Expression.value(4), Expression.variable("volume"))).
                        select("dividedVolume", Expression.divide(Expression.variable("volume"), Expression.value(8)))
        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("200".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("50".equals(inEvents[0].getData()[3].toString()));
                Assert.assertTrue("400".equals(inEvents[0].getData()[4].toString()));
                Assert.assertTrue("12.5".equals(inEvents[0].getData()[5].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);

        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery9() throws InterruptedException {
        log.info("Filter test9");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select symbol, price, volume+100 as increasedVolume,(volume -50) as decreasedVolume,(4*volume) as multipliedVolume,(volume/8) as dividedVolume,(4*volume+34) as calculatedVolume " +
                                                       "group by symbol " +
                                                       "insert into OutputStream ;");


        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("200".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("50".equals(inEvents[0].getData()[3].toString()));
                Assert.assertTrue("400".equals(inEvents[0].getData()[4].toString()));
                Assert.assertTrue("12.5".equals(inEvents[0].getData()[5].toString()));
                Assert.assertTrue("434".equals(inEvents[0].getData()[6].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);

        siddhiManager.shutdown();

    }


    @Test(expected = MalformedAttributeException.class)
    public void testFilterQuery10() throws InterruptedException {
        log.info("Filter test10");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                                                       "select  ssymbol, price, volume+100 as increasedVolume,(volume -50) as decreasedVolume,(4*volume) as multipliedVolume,(volume/8) as dividedVolume,(4*volume+34) as calculatedVolume " +
                                                       "group by symbol " +
                                                       "insert into OutputStream;");
        siddhiManager.shutdown();

    }

    @Test(expected = MalformedAttributeException.class)
    public void testFilterQuery11() throws InterruptedException {
        log.info("Filter test11");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[pric>200] " +
                                                       "select symbol, price, volume+100 as increasedVolume,(volume -50) as decreasedVolume,(4*volume) as multipliedVolume,(volume/8) as dividedVolume,(4*volume+34) as calculatedVolume " +
                                                       "group by symbol " +
                                                       "insert into OutputStream;");
        siddhiManager.shutdown();

    }

    @Test(expected = SiddhiParserException.class)
    public void testFilterQuery12() throws InterruptedException {
        log.info("Filter test12");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[pric>200] " +
                                                       "insert intutputStream symbol, price, volume+100 as increasedVolume,(volume -50) as decreasedVolume,(4*volume) as multipliedVolume,(volume/8) as dividedVolume,(4*volume+34) as calculatedVolume " +
                                                       "group by symbol;");

        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery13() throws InterruptedException {
        log.info("Filter test13");

        SiddhiManager siddhiManager = new SiddhiManager();


        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[symbol contains 'WS'] " +
                                                       "select  symbol, price, volume " +
                                                       "insert into OutputStream " +
                                                       ";");


        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test(expected = OperationNotSupportedException.class)
    public void testFilterQuery14() throws InterruptedException {
        log.info("Filter test14");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price contains 'WS'] " +
                                                       "select  symbol, price, volume " +
                                                       "insert into OutputStream" +
                                                       ";");
    }

    @Test
    public void testFilterQuery15() throws InterruptedException {
        log.info("Filter test15");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[symbol instanceof string] " +
                                                       "select  symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{12, 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery16() throws InterruptedException {
        log.info("Filter test16");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[price instanceof float] " +
                                                       "select  symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        siddhiManager.shutdown();

    }

    @Test(expected = MalformedAttributeException.class)
    public void testFilterQuery17() throws InterruptedException {
        log.info("Filter test17");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price instanceof str] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");
    }

    @Test
    public void testFilterQuery18() throws InterruptedException {
        log.info("Filter test18");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[symbol instanceof float] " +
                                                       "select  symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 75.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(0, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery19() throws InterruptedException {
        log.info("Filter test19");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume > 100] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(103l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 103l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery20() throws InterruptedException {
        log.info("Filter test20");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume < 100] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 103l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery21() throws InterruptedException {
        log.info("Filter test21");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 100] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"WSO2", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery23() throws InterruptedException {
        log.info("Filter test23");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[symbol != 'WSO2'] " +
                                                       "select symbol,price,volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
        inputHandler.send(new Object[]{"IBM", 35f, 50l});

        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery24() throws InterruptedException {
        log.info("Filter test24");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50f))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
        inputHandler.send(new Object[]{"IBM", 35f, 50l});

        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery25() throws InterruptedException {
        log.info("Filter test25");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 45f, 100l});
        inputHandler.send(new Object[]{"IBM", 35f, 50l});

        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery22() throws InterruptedException {
        log.info("Filter test22");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 100l ] " +
                                                       "select symbol, price, volume " +
                                                       "insert into OutputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery26() throws InterruptedException {
        log.info("Filter test26");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 55.6f] " +
                                                       "select symbol, price, volume " +
                                                       "insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                //Assert.assertEquals(10l, ((Long) inEvents[0].getData(2)).longValue());
                count++;
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.6f, 100l});
        inputHandler.send(new Object[]{"IBM", 57.6f, 10l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery27() throws InterruptedException {
        log.info("Filter test27");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string,price float,volume long)");

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 50d ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 55.5f, 40l});
        inputHandler.send(new Object[]{"WSO2", 53.5f, 50l});
        inputHandler.send(new Object[]{"WSO2", 50.5f, 400l});

        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery28() throws InterruptedException {
        log.info("Filter test28");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(100L))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 40f, 70});
        inputHandler.send(new Object[]{"WSO2", 60f, 50});
        inputHandler.send(new Object[]{"WSO2", 100f, 100});

        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();


    }

    @Test
    public void testFilterQuery29() throws InterruptedException {
        log.info("Filter test29");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryRefernce = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryRefernce, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 40f, 50});
        inputHandler.send(new Object[]{"WSO2", 20f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();


    }

    @Test
    public void testFilterQuery30() throws InterruptedException {
        log.info("Filter test30");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.NOT_EQUAL, Expression.value(50f))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryRefernce = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryRefernce, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 40f, 50});
        inputHandler.send(new Object[]{"WSO2", 20f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery31() throws InterruptedException {
        log.info("Filter test31");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[volume != 50d] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 50});
        inputHandler.send(new Object[]{"IBM", 50f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery32() throws InterruptedException {
        log.info("Filter test31");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        //String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50l] insert into outputStream symbol,price,volume;");

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.NOT_EQUAL, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 50});
        inputHandler.send(new Object[]{"IBM", 500f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery33() throws InterruptedException {
        log.info("Filter test33");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50d ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 50});
        inputHandler.send(new Object[]{"IBM", 55f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery34() throws InterruptedException {
        log.info("Filter test34");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50 ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery35() throws InterruptedException {
        log.info("Filter test35");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50f ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery36() throws InterruptedException {
        log.info("Filter test36");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50d ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery37() throws InterruptedException {
        log.info("Filter test37");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        //String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50d ] insert into outputStream symbol,price,volume;");

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.NOT_EQUAL, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery38() throws InterruptedException {
        log.info("Filter test38");

        SiddhiManager siddhiManager = new SiddhiManager();

        // InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).attribute("validity", Attribute.Type.BOOL));
        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int, validity bool)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[validity != true ] select symbol,price,volume insert into outputStream ;");


        //String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50, true});
        inputHandler.send(new Object[]{"IBM", 55d, 100, false});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery38_2() throws InterruptedException {
        log.info("Filter test38_2");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT).attribute("validity", Attribute.Type.BOOL));
        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("validity"), Condition.Operator.EQUAL, Expression.value(true))));
        query.insertInto("outputStream");
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50, true});
        inputHandler.send(new Object[]{"IBM", 55d, 100, false});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }


    @Test
    public void testFilterQuery39() throws InterruptedException {
        log.info("Filter test39");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price != 50 ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 50});
        inputHandler.send(new Object[]{"IBM", 55f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }


    //***************************************************************************************************************************

    @Test
    public void testFilterQuery40() throws InterruptedException {
        log.info("Filter test40");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price > 50d ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 50});
        inputHandler.send(new Object[]{"IBM", 55f, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery41() throws InterruptedException {
        log.info("Filter test41");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price > 50 ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery42() throws InterruptedException {
        log.info("Filter test42");

        SiddhiManager siddhiManager = new SiddhiManager();

        InputHandler inputHandler = siddhiManager.defineStream("define stream cseEventStream (symbol string, price double, volume int)");
        String queryReference = siddhiManager.addQuery("from cseEventStream[price > 50f ] select symbol,price,volume insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 50});
        inputHandler.send(new Object[]{"IBM", 55d, 100});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();
    }

    @Test
    public void testFilterQuery43() throws InterruptedException {
        log.info("Filter test43");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN, Expression.value(50f))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery44() throws InterruptedException {
        log.info("Filter test44");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery45() throws InterruptedException {
        log.info("Filter test45");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60});
        inputHandler.send(new Object[]{"WSO2", 70f, 40});
        inputHandler.send(new Object[]{"WSO2", 44f, 200});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery46() throws InterruptedException {
        log.info("Filter test46");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("volume", Expression.variable("volume")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery47() throws InterruptedException {
        log.info("Filter test47");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.FLOAT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN, Expression.value(50l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f});
        inputHandler.send(new Object[]{"WSO2", 70f, 40f});
        inputHandler.send(new Object[]{"WSO2", 44f, 200f});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery48() throws InterruptedException {
        log.info("Filter test48");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume > 50d] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery49() throws InterruptedException {
        log.info("Filter test49");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.INT));


        String queryReference = siddhiManager.addQuery("from cseEventStream[volume > 50d] select symbol,price insert into outputStream ;");
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60});
        inputHandler.send(new Object[]{"WSO2", 70f, 40});
        inputHandler.send(new Object[]{"WSO2", 44f, 200});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery50() throws InterruptedException {
        log.info("Filter test50");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 60d ] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery51() throws InterruptedException {
        log.info("Filter test51");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 60f ] select symbol,price insert into outputStream ; ");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery52() throws InterruptedException {
        log.info("Filter test52");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 60 ] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery53() throws InterruptedException {
        log.info("Filter test53");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.EQUAL, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery54() throws InterruptedException {
        log.info("Filter test54");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price == 50d ] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery55() throws InterruptedException {
        log.info("Filter test55");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price == 50f ] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery56() throws InterruptedException {
        log.info("Filter test56");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price == 70 ] select symbol,price insert into outputStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery57() throws InterruptedException {
        log.info("Filter test57");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.EQUAL, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")));
        query.insertInto("outputStream");

        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery58() throws InterruptedException {
        log.info("Filter test58");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.EQUAL, Expression.value(5d))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery59() throws InterruptedException {
        log.info("Filter test59");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.EQUAL, Expression.value(5f))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery60() throws InterruptedException {
        log.info("Filter test60");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.EQUAL, Expression.value(2))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery61() throws InterruptedException {
        log.info("Filter test61");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.EQUAL, Expression.value(4l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery62() throws InterruptedException {
        log.info("Filter test62");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.EQUAL, Expression.value(200l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60l, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 200l, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery63() throws InterruptedException {
        log.info("Filter test63");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 40d ] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery64() throws InterruptedException {
        log.info("Filter test64");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 40f ] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery65() throws InterruptedException {
        log.info("Filter test65");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume == 40 ] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    //**************************************************************************************************************************

    @Test
    public void testFilterQuery66() throws InterruptedException {
        log.info("Filter test66 : NOT Operator");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[not(volume == 40)] select  symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    //**************************************************************************************************************************
    //Test cases for less than or equal
    @Test
    public void testFilterQuery67() throws InterruptedException {
        log.info("Filter test67");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price <= 60d] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery68() throws InterruptedException {
        log.info("Filter test68");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price <= 100f] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery69() throws InterruptedException {
        log.info("Filter test69");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price <= 50] select symbol,price insert into outputStream;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 60l});
        inputHandler.send(new Object[]{"WSO2", 70d, 40l});
        inputHandler.send(new Object[]{"WSO2", 44d, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery70() throws InterruptedException {
        log.info("Filter test70");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(200l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery71() throws InterruptedException {
        log.info("Filter test71");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price <= 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery72() throws InterruptedException {
        log.info("Filter test72");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(200l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 5});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery73() throws InterruptedException {
        log.info("Filter test73");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(5d))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery74() throws InterruptedException {
        log.info("Filter test74");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(5f))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery75() throws InterruptedException {
        log.info("Filter test75");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(3l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery76() throws InterruptedException {
        log.info("Filter test76");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume <= 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery77() throws InterruptedException {
        log.info("Filter test77");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume <= 50f] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery78() throws InterruptedException {
        log.info("Filter test78");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume <= 50] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery79() throws InterruptedException {
        log.info("Filter test79");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.LESS_THAN_EQUAL, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60l, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }


    //*************************************************************************************************************************
    //Test cases for less-than operator

    @Test
    public void testFilterQuery80() throws InterruptedException {
        log.info("Filter test80");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume < 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery81() throws InterruptedException {
        log.info("Filter test81");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume < 70f] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery82() throws InterruptedException {
        log.info("Filter test82");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price < 50] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 60d});
        inputHandler.send(new Object[]{"WSO2", 70d, 40d});
        inputHandler.send(new Object[]{"WSO2", 44d, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery83() throws InterruptedException {
        log.info("Filter test83");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.LESS_THAN, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery84() throws InterruptedException {
        log.info("Filter test84");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.LESS_THAN, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery85() throws InterruptedException {
        log.info("Filter test85");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.LESS_THAN, Expression.value(4l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery86() throws InterruptedException {
        log.info("Filter test86");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.LESS_THAN, Expression.value(40l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20l, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery87() throws InterruptedException {
        log.info("Filter test87");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price < 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery88() throws InterruptedException {
        log.info("Filter test88");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price < 55f] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery89() throws InterruptedException {
        log.info("Filter test89");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity < 50d] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery90() throws InterruptedException {
        log.info("Filter test90");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity < 10f] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery91() throws InterruptedException {
        log.info("Filter test91");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity < 15] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery92() throws InterruptedException {
        log.info("Filter test92");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume < 100d] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery93() throws InterruptedException {
        log.info("Filter test93");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume < 100f] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }


    //*********************************************************************************************************************
    // Test cases for Greater_than_equal operator

    @Test
    public void testFilterQuery94() throws InterruptedException {
        log.info("Filter test94");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume >= 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery95() throws InterruptedException {
        log.info("Filter test95");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume >= 70f] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery96() throws InterruptedException {
        log.info("Filter test96");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.DOUBLE).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price >= 50] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50d, 60d});
        inputHandler.send(new Object[]{"WSO2", 70d, 40d});
        inputHandler.send(new Object[]{"WSO2", 44d, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }


    @Test
    public void testFilterQuery97() throws InterruptedException {
        log.info("Filter test97");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 60f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery98() throws InterruptedException {
        log.info("Filter test98");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("price"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(60l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery99() throws InterruptedException {
        log.info("Filter test99");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("quantity"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(4l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 60d, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300d, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery100() throws InterruptedException {
        log.info("Filter test100");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream").filter(Condition.compare(Expression.variable("volume"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(40l))));
        query.select(QueryFactory.outputSelector().select("symbol", Expression.variable("symbol")).select("price", Expression.variable("price")).select("quantity", Expression.variable("quantity")));
        query.insertInto("outputStream");
        String queryReference = siddhiManager.addQuery(query);
        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 500f, 50l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 20l, 2});
        inputHandler.send(new Object[]{"WSO2", 50f, 300l, 4});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery101() throws InterruptedException {
        log.info("Filter test101");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price >= 50d] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery102() throws InterruptedException {
        log.info("Filter test102");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        String queryReference = siddhiManager.addQuery("from cseEventStream[price >= 55f] select symbol,price;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery103() throws InterruptedException {
        log.info("Filter test103");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity >= 50d] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery104() throws InterruptedException {
        log.info("Filter test104");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity >= 10f] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(2, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery105() throws InterruptedException {
        log.info("Filter test105");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[quantity >= 15] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60d, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40d, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200d, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery106() throws InterruptedException {
        log.info("Filter test106");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume >= 100d] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery107() throws InterruptedException {
        log.info("Filter test107");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume >= 100f] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }


    //***********************************************************************************************************************
    //Expression-Add
    @Test
    public void testFilterQuery108() throws InterruptedException {
        log.info("Filter test108");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("increasedPrice", Expression.add(Expression.value(100), Expression.variable("price"))).
                        select("increasedVolume", Expression.add(Expression.value(50), Expression.variable("volume"))).
                        select("increasedQuantity", Expression.add(Expression.value(4), Expression.variable("quantity")))

        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("155.5".equals(inEvents[0].getData()[1].toString()));
                Assert.assertTrue("150.0".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("9".equals(inEvents[0].getData()[3].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5});

        Thread.sleep(100);

        siddhiManager.shutdown();

    }

    //*******************************************************************************************************************
    //Expression-Minus
    @Test
    public void testFilterQuery109() throws InterruptedException {
        log.info("Filter test109");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("decreasedPrice", Expression.minus(Expression.variable("price"), Expression.value(20))).
                        select("decreasedVolume", Expression.minus(Expression.variable("volume"), Expression.value(50))).
                        select("decreasedQuantity", Expression.minus(Expression.variable("quantity"), Expression.value(4)))

        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("35.5".equals(inEvents[0].getData()[1].toString()));
                Assert.assertTrue("50.0".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("1".equals(inEvents[0].getData()[3].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5});

        Thread.sleep(100);

        siddhiManager.shutdown();

    }


    //************************************************************************************************************************
    //Expression Divide
    @Test
    public void testFilterQuery110() throws InterruptedException {
        log.info("Filter test110");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("dividedPrice", Expression.divide(Expression.variable("price"), Expression.value(2))).
                        select("dividedVolume", Expression.divide(Expression.variable("volume"), Expression.value(2)))

        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("30.0".equals(inEvents[0].getData()[1].toString()));
                Assert.assertTrue("50.0".equals(inEvents[0].getData()[2].toString()));

            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 60f, 100d});

        Thread.sleep(100);

        siddhiManager.shutdown();
    }

    //*********************************************************************************************************************
    //Expression Multiply
    @Test
    public void testFilterQuery111() throws InterruptedException {
        log.info("Filter test111");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("multipliedQuantity", Expression.multiply(Expression.variable("quantity"), Expression.value(4)))

        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("20".equals(inEvents[0].getData()[1].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.5f, 100d, 5});

        Thread.sleep(100);

        siddhiManager.shutdown();

    }

    //******************************************************************************************************************
    //Expression Mod

    @Test
    public void testFilterQuery112() throws InterruptedException {
        log.info("Filter test112");

        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.DOUBLE).attribute("quantity", Attribute.Type.INT).attribute("awards", Attribute.Type.LONG));

        Query query = QueryFactory.createQuery();
        query.from(QueryFactory.inputStream("cseEventStream"));
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("modPrice", Expression.mod(Expression.variable("price"), Expression.value(2))).
                        select("modVolume", Expression.mod(Expression.variable("volume"), Expression.value(2))).
                        select("modQuantity", Expression.mod(Expression.variable("quantity"), Expression.value(2))).
                        select("modAwards", Expression.mod(Expression.variable("awards"), Expression.value(2)))

        );
        query.insertInto("OutputStream");


        String queryReference = siddhiManager.addQuery(query);

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override

            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertTrue("1.5".equals(inEvents[0].getData()[1].toString()));
                Assert.assertTrue("1.0".equals(inEvents[0].getData()[2].toString()));
                Assert.assertTrue("1".equals(inEvents[0].getData()[3].toString()));
                Assert.assertTrue("1".equals(inEvents[0].getData()[4].toString()));
            }

        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 55.5f, 101d, 5, 7l});

        Thread.sleep(100);

        siddhiManager.shutdown();

    }

    //*****************************************************************************************************************
    //Exception

    @Test(expected = SourceNotExistException.class)
    public void testFilterQuery113() throws InterruptedException {
        log.info("Filter test113");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream1").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[volume >= 100f] select symbol,price,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60l, 6});
        inputHandler.send(new Object[]{"WSO2", 70f, 40l, 10});
        inputHandler.send(new Object[]{"WSO2", 44f, 200l, 56});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }



    //true check

    @Test
    public void testFilterQuery117() throws InterruptedException {
        log.info("Filter test117");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[true]" +
                                                       " select symbol, price1,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                if (count == 1) {
                    Assert.assertEquals(50.0f, inEvents[0].getData1());
                }
            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        Assert.assertEquals(1, count);
        siddhiManager.shutdown();

    }

    @Test
    public void testFilterQuery118() throws InterruptedException {
        log.info("Filter test118");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream(QueryFactory.createStreamDefinition().name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price1", Attribute.Type.FLOAT).attribute("price2", Attribute.Type.FLOAT).attribute("volume", Attribute.Type.LONG).attribute("quantity", Attribute.Type.INT));

        String queryReference = siddhiManager.addQuery("from cseEventStream[false]" +
                                                       " select symbol, price1,quantity;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                count++;
                Assert.fail("No events should occur");

            }
        });

        inputHandler.send(new Object[]{"WSO2", 50f, 60f, 60l, 6});
        Thread.sleep(100);
        Assert.assertEquals(0, count);
        siddhiManager.shutdown();

    }

    //****************************************
    //group by test

    @Test
    public void testFilterQuery119() throws InterruptedException {

        log.info("Filter test119");
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.defineStream("define stream cseEventStream (symbol1 string, symbol2 string, price float, volume long) ");

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select symbol1, symbol2, price, sum(volume) as sumVolume " +
                "group by symbol1, symbol2 " +
                "insert into StockQuote;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
//                Assert.assertTrue("IBM".equals(inEvents[0].getData(0)) || "WSO2".equals(inEvents[0].getData(0)));
                count++;
                eventArrived = true;
            }
        });
        InputHandler inputHandler = siddhiManager.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"I","BM", 75.6f, 100l});
        inputHandler.send(new Object[]{"IBM", "", 75.6f, 25l});
        inputHandler.send(new Object[]{"IB","M", 75.6f, 100l});
        Thread.sleep(100);
        Assert.assertEquals(3, count);
        Assert.assertEquals("Event arrived", true, eventArrived);
        siddhiManager.shutdown();

    }




}
