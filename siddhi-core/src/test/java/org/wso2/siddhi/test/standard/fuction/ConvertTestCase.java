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
package org.wso2.siddhi.test.standard.fuction;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class ConvertTestCase {
    static final Logger log = Logger.getLogger(ConvertTestCase.class);
    private int eventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        eventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testConvertFunctionQuery1() throws InterruptedException {
        log.info("ConvertFunction test1");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");
        ;

        String queryReference = siddhiManager.addQuery("from typeStream" +
                                                       " select convert(typeS,string) as valueS, convert(typeF,float) as valueF, convert(typeD,double) as valueD , convert(typeI,int) as valueI , convert(typeL,long) as valueL , convert(typeB,bool) as valueB ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                junit.framework.Assert.assertTrue(inEvents[0].getData0() instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData1() instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData2() instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData3() instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData4() instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData5() instanceof Boolean);

            }
        });

        inputHandler.send(new Object[]{"WSO2", 2f, 3d, 4, 5l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testConvertFunctionQuery2() throws InterruptedException {
        log.info("ConvertFunction test2");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");

        String queryReference = siddhiManager.addQuery("from typeStream" +
                                                       " select convert(typeS,string) as valueS1, convert(typeS,float) as valueF1, convert(typeS,double) as valueD1, convert(typeS,int) as valueI1 , convert(typeS,long) as valueL1 , convert(typeS,bool) as valueB1, " +
                                                       "        convert(typeF,string) as valueS2, convert(typeF,float) as valueF2, convert(typeF,double) as valueD2, convert(typeF,int) as valueI2 , convert(typeF,long) as valueL2 , convert(typeF,bool) as valueB2, " +
                                                       "        convert(typeD,string) as valueS3, convert(typeD,float) as valueF3, convert(typeD,double) as valueD3, convert(typeD,int) as valueI3 , convert(typeD,long) as valueL3 , convert(typeD,bool) as valueB3, " +
                                                       "        convert(typeI,string) as valueS4, convert(typeI,float) as valueF4, convert(typeI,double) as valueD4, convert(typeI,int) as valueI4 , convert(typeI,long) as valueL4 , convert(typeI,bool) as valueB4, " +
                                                       "        convert(typeL,string) as valueS5, convert(typeL,float) as valueF5, convert(typeL,double) as valueD5, convert(typeL,int) as valueI5 , convert(typeL,long) as valueL5 , convert(typeL,bool) as valueB5, " +
                                                       "        convert(typeB,string) as valueS6, convert(typeB,float) as valueF6, convert(typeB,double) as valueD6, convert(typeB,int) as valueI6 , convert(typeB,long) as valueL6 , convert(typeB,bool) as valueB6 ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                junit.framework.Assert.assertTrue(inEvents[0].getData0() instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData1() == null);
                junit.framework.Assert.assertTrue(inEvents[0].getData2() == null);
                junit.framework.Assert.assertTrue(inEvents[0].getData3() == null);
                junit.framework.Assert.assertTrue(inEvents[0].getData4() == null);
                junit.framework.Assert.assertTrue(inEvents[0].getData5() instanceof Boolean && !((Boolean) inEvents[0].getData5()));

                junit.framework.Assert.assertTrue(inEvents[0].getData(6) instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData(7) instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData(8) instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData(9) instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData(10) instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData(11) instanceof Boolean && !((Boolean) inEvents[0].getData(11)));

                junit.framework.Assert.assertTrue(inEvents[0].getData(12) instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData(13) instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData(14) instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData(15) instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData(16) instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData(17) instanceof Boolean && !((Boolean) inEvents[0].getData(17)));

                junit.framework.Assert.assertTrue(inEvents[0].getData(18) instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData(19) instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData(20) instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData(21) instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData(22) instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData(23) instanceof Boolean && !((Boolean) inEvents[0].getData(23)));

                junit.framework.Assert.assertTrue(inEvents[0].getData(24) instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData(25) instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData(26) instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData(27) instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData(28) instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData(29) instanceof Boolean && !((Boolean) inEvents[0].getData(29)));

                junit.framework.Assert.assertTrue(inEvents[0].getData(30) instanceof String);
                junit.framework.Assert.assertTrue(inEvents[0].getData(31) instanceof Float);
                junit.framework.Assert.assertTrue(inEvents[0].getData(32) instanceof Double);
                junit.framework.Assert.assertTrue(inEvents[0].getData(33) instanceof Integer);
                junit.framework.Assert.assertTrue(inEvents[0].getData(34) instanceof Long);
                junit.framework.Assert.assertTrue(inEvents[0].getData(35) instanceof Boolean && ((Boolean) inEvents[0].getData(35)));


            }
        });

        inputHandler.send(new Object[]{"WSO2", 2f, 3d, 4, 5l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testConvertFunctionQuery3() throws InterruptedException {
        log.info("ConvertFunction test3");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");
        ;

        String queryReference = siddhiManager.addQuery("from typeStream" +
                                                       " select convert(typeS,bool) as valueB1, convert(typeF,bool) as valueB2, convert(typeD,bool) as valueB3 , convert(typeI,bool) as valueB4 , convert(typeL,bool) as valueB5 , convert(typeB,bool) as valueB6 ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                junit.framework.Assert.assertTrue(inEvents[0].getData0() instanceof Boolean && (Boolean) inEvents[0].getData0());
                junit.framework.Assert.assertTrue(inEvents[0].getData1() instanceof Boolean && (Boolean) inEvents[0].getData1());
                junit.framework.Assert.assertTrue(inEvents[0].getData2() instanceof Boolean && (Boolean) inEvents[0].getData2());
                junit.framework.Assert.assertTrue(inEvents[0].getData3() instanceof Boolean && (Boolean) inEvents[0].getData3());
                junit.framework.Assert.assertTrue(inEvents[0].getData4() instanceof Boolean && (Boolean) inEvents[0].getData4());
                junit.framework.Assert.assertTrue(inEvents[0].getData5() instanceof Boolean && (Boolean) inEvents[0].getData5());

            }
        });

        inputHandler.send(new Object[]{"true", 1f, 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();

    }

    @Test(expected = QueryCreationException.class)
    public void testConvertFunctionQuery4() throws InterruptedException {
        log.info("ConvertFunction test4");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(string) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"true", 1f, 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(0, eventCount);
        siddhiManager.shutdown();

    }

    @Test(expected = QueryCreationException.class)
    public void testConvertFunctionQuery5() throws InterruptedException {
        log.info("ConvertFunction test5");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS,typeS,typeS,typeS) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"true", 1f, 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(0, eventCount);
        siddhiManager.shutdown();

    }

    @Test
    public void testConvertFunctionQuery6() throws InterruptedException {
        log.info("ConvertFunction test6");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS,long,'DD:MM:yy') as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals(1358899200000l, inEvents[0].getData0());

            }
        });

        inputHandler.send(new Object[]{"23:01:13", 1f, 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test(expected = QueryCreationException.class)
    public void testConvertFunctionQuery7() throws InterruptedException {
        log.info("ConvertFunction test7");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS,int,'dd:MM:yy') as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
            }
        });

        inputHandler.send(new Object[]{"23:01:13", 1f, 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery8() throws InterruptedException {
        log.info("ConvertFunction test8");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeL,string,'dd:MM:yy') as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("05:07:13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", 1f, 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery9() throws InterruptedException {
        log.info("ConvertFunction test9");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS, string,'dd:MM:yy','MM-dd-yy') as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("01-23-13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", 1f, 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery10() throws InterruptedException {
        log.info("ConvertFunction test10");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeSF string, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS,long,typeSF) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals(1358899200000l, inEvents[0].getData0());

            }
        });

        inputHandler.send(new Object[]{"23:01:13", "DD:MM:yy", 1d, 1, 1l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }


    @Test
    public void testConvertFunctionQuery11() throws InterruptedException {
        log.info("ConvertFunction test11");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeSF string, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeL,string,typeSF) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("05:07:13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", "dd:MM:yy", 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery12() throws InterruptedException {
        log.info("ConvertFunction test12");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeSF string, typeSF2 string, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS, string,typeSF,typeSF2) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("01-23-13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", "dd:MM:yy", "MM-dd-yy", 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery13() throws InterruptedException {
        log.info("ConvertFunction test13");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeSF string, typeSF2 string, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS, string,typeSF,'MM-dd-yy') as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("01-23-13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", "dd:MM:yy", "", 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }

    @Test
    public void testConvertFunctionQuery14() throws InterruptedException {
        log.info("ConvertFunction test14");

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler inputHandler = siddhiManager.defineStream("define stream typeStream (typeS string, typeSF string, typeSF2 string, typeD double, typeI int, typeL long, typeB bool) ");


        String queryReference = siddhiManager.addQuery("from typeStream " +
                                                       " select convert(typeS, string,\"dd:MM:yy\",typeSF2) as type1;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                eventCount++;
                Assert.assertEquals("01-23-13", inEvents[0].getData0());
            }
        });

        inputHandler.send(new Object[]{"23:01:13", "", "MM-dd-yy", 1d, 1, 1373043027082l, true});
        junit.framework.Assert.assertEquals(1, eventCount);
        siddhiManager.shutdown();
    }
}
