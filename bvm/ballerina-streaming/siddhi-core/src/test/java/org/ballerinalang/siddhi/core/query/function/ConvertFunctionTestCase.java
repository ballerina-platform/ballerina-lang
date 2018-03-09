/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.query.function;

import org.ballerinalang.siddhi.core.SiddhiAppRuntime;
import org.ballerinalang.siddhi.core.SiddhiManager;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.query.output.callback.QueryCallback;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.siddhi.core.util.EventPrinter;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConvertFunctionTestCase {
    private static final Logger log = LoggerFactory.getLogger(ConvertFunctionTestCase.class);
    private int count;

    @BeforeMethod
    public void init() {
        count = 0;
    }

    @Test
    public void convertFunctionTest1() throws InterruptedException {
        log.info("convert function test 1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool, typeN double) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'string') as valueS, convert(typeF,'float') as valueF, convert(typeD,'double') " +
                "as valueD , convert(typeI,'int') as valueI , convert(typeL,'long') as valueL , convert(typeB,'bool')" +
                " as valueB, convert(typeN,'string') as valueN " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count++;
                    AssertJUnit.assertTrue(inEvent.getData(0) instanceof String);
                    AssertJUnit.assertTrue(inEvent.getData(1) instanceof Float);
                    AssertJUnit.assertTrue(inEvent.getData(2) instanceof Double);
                    AssertJUnit.assertTrue(inEvent.getData(3) instanceof Integer);
                    AssertJUnit.assertTrue(inEvent.getData(4) instanceof Long);
                    AssertJUnit.assertTrue(inEvent.getData(5) instanceof Boolean);
                    AssertJUnit.assertTrue(inEvent.getData(6) == null);
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("typeStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 2f, 3d, 4, 5L, true, null});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void convertFunctionTest2() throws InterruptedException {
        log.info("convert function test 2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'string') as valueS1, convert(typeS,'float') as valueF1, convert(typeS," +
                "'double') as valueD1, convert(typeS,'int') as valueI1 , convert(typeS,'long') as valueL1 , convert" +
                "(typeS,'bool') as valueB1, " +
                "        convert(typeF,'string') as valueS2, convert(typeF,'float') as valueF2, convert(typeF," +
                "'double') as valueD2, convert(typeF,'int') as valueI2 , convert(typeF,'long') as valueL2 , convert" +
                "(typeF,'bool') as valueB2, " +
                "        convert(typeD,'string') as valueS3, convert(typeD,'float') as valueF3, convert(typeD," +
                "'double') as valueD3, convert(typeD,'int') as valueI3 , convert(typeD,'long') as valueL3 , convert" +
                "(typeD,'bool') as valueB3, " +
                "        convert(typeI,'string') as valueS4, convert(typeI,'float') as valueF4, convert(typeI," +
                "'double') as valueD4, convert(typeI,'int') as valueI4 , convert(typeI,'long') as valueL4 , convert" +
                "(typeI,'bool') as valueB4, " +
                "        convert(typeL,'string') as valueS5, convert(typeL,'float') as valueF5, convert(typeL," +
                "'double') as valueD5, convert(typeL,'int') as valueI5 , convert(typeL,'long') as valueL5 , convert" +
                "(typeL,'bool') as valueB5, " +
                "        convert(typeB,'string') as valueS6, convert(typeB,'float') as valueF6, convert(typeB," +
                "'double') as valueD6, convert(typeB,'int') as valueI6 , convert(typeB,'long') as valueL6 , convert" +
                "(typeB,'bool') as valueB6  " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                count++;
                AssertJUnit.assertTrue(inEvents[0].getData(0) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(1) == null);
                AssertJUnit.assertTrue(inEvents[0].getData(2) == null);
                AssertJUnit.assertTrue(inEvents[0].getData(3) == null);
                AssertJUnit.assertTrue(inEvents[0].getData(4) == null);
                AssertJUnit.assertTrue(inEvents[0].getData(5) instanceof Boolean &&
                        !((Boolean) inEvents[0].getData(5)));

                AssertJUnit.assertTrue(inEvents[0].getData(6) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(7) instanceof Float);
                AssertJUnit.assertTrue(inEvents[0].getData(8) instanceof Double);
                AssertJUnit.assertTrue(inEvents[0].getData(9) instanceof Integer);
                AssertJUnit.assertTrue(inEvents[0].getData(10) instanceof Long);
                AssertJUnit.assertTrue(inEvents[0].getData(11) instanceof Boolean &&
                        !((Boolean) inEvents[0].getData(11)));

                AssertJUnit.assertTrue(inEvents[0].getData(12) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(13) instanceof Float);
                AssertJUnit.assertTrue(inEvents[0].getData(14) instanceof Double);
                AssertJUnit.assertTrue(inEvents[0].getData(15) instanceof Integer);
                AssertJUnit.assertTrue(inEvents[0].getData(16) instanceof Long);
                AssertJUnit.assertTrue(inEvents[0].getData(17) instanceof Boolean &&
                        !((Boolean) inEvents[0].getData(17)));

                AssertJUnit.assertTrue(inEvents[0].getData(18) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(19) instanceof Float);
                AssertJUnit.assertTrue(inEvents[0].getData(20) instanceof Double);
                AssertJUnit.assertTrue(inEvents[0].getData(21) instanceof Integer);
                AssertJUnit.assertTrue(inEvents[0].getData(22) instanceof Long);
                AssertJUnit.assertTrue(inEvents[0].getData(23) instanceof Boolean &&
                        !((Boolean) inEvents[0].getData(23)));

                AssertJUnit.assertTrue(inEvents[0].getData(24) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(25) instanceof Float);
                AssertJUnit.assertTrue(inEvents[0].getData(26) instanceof Double);
                AssertJUnit.assertTrue(inEvents[0].getData(27) instanceof Integer);
                AssertJUnit.assertTrue(inEvents[0].getData(28) instanceof Long);
                AssertJUnit.assertTrue(inEvents[0].getData(29) instanceof Boolean &&
                        !((Boolean) inEvents[0].getData(29)));

                AssertJUnit.assertTrue(inEvents[0].getData(30) instanceof String);
                AssertJUnit.assertTrue(inEvents[0].getData(31) instanceof Float);
                AssertJUnit.assertTrue(inEvents[0].getData(32) instanceof Double);
                AssertJUnit.assertTrue(inEvents[0].getData(33) instanceof Integer);
                AssertJUnit.assertTrue(inEvents[0].getData(34) instanceof Long);
                AssertJUnit.assertTrue(inEvents[0].getData(35) instanceof Boolean &&
                        ((Boolean) inEvents[0].getData(35)));
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("typeStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 2f, 3d, 4, 5L, true});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void convertFunctionTest3() throws InterruptedException {
        log.info("convert function test 3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'bool') as valueB1, convert(typeF,'bool') as valueB2, convert(typeD,'bool') as " +
                "valueB3 , convert(typeI,'bool') as valueB4 , convert(typeL,'bool') as valueB5 , convert(typeB," +
                "'bool') as valueB6 " +
                "insert into outputStream ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(cseEventStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timestamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timestamp, inEvents, removeEvents);
                count++;
                AssertJUnit.assertTrue(inEvents[0].getData(0) instanceof Boolean && (Boolean) inEvents[0].getData(0));
                AssertJUnit.assertTrue(inEvents[0].getData(1) instanceof Boolean && (Boolean) inEvents[0].getData(1));
                AssertJUnit.assertTrue(inEvents[0].getData(2) instanceof Boolean && (Boolean) inEvents[0].getData(2));
                AssertJUnit.assertTrue(inEvents[0].getData(3) instanceof Boolean && (Boolean) inEvents[0].getData(3));
                AssertJUnit.assertTrue(inEvents[0].getData(4) instanceof Boolean && (Boolean) inEvents[0].getData(4));
                AssertJUnit.assertTrue(inEvents[0].getData(5) instanceof Boolean && (Boolean) inEvents[0].getData(5));
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("typeStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"true", 1f, 1d, 1, 1L, true});
        Thread.sleep(100);
        AssertJUnit.assertEquals(1, count);
        siddhiAppRuntime.shutdown();
    }


    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionTest4() throws InterruptedException {
        log.info("convert function test 4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS) as valueB1 " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionTest5() throws InterruptedException {
        log.info("convert function test 5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'string','int') as valueB1 " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void convertFunctionTest6() throws InterruptedException {
        log.info("convert function test 6");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "" +
                "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL long, typeB " +
                "bool) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,string) as valueB1 " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionExceptionTest7() throws InterruptedException {
        log.info("convert function test 7");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL " +
                "long, typeB bool, typeN double) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'234') as valueS, convert(typeF,'float') as valueF, convert(typeD," +
                "'double') as valueD , convert(typeI,'int') as valueI , convert(typeL,'long') as valueL , " +
                "convert(typeB,'bool') as valueB, convert(typeN,'string') as valueN " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionExceptionTest8() throws InterruptedException {
        log.info("convert function test 8");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL " +
                "long, typeB bool, typeN double) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,123) as valueS, convert(typeF,'float') as valueF, convert(typeD," +
                "'double') as valueD , convert(typeI,'int') as valueI , convert(typeL,'long') as valueL , " +
                "convert(typeB,'bool') as valueB, convert(typeN,'string') as valueN " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionExceptionTest9() throws InterruptedException {
        log.info("convert function test 9");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream typeStream (typeS object, typeF float, typeD double, typeI int, typeL " +
                "long, typeB bool, typeN double) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,typeS) as valueS, convert(typeF,'float') as valueF, convert(typeD," +
                "'double') as valueD , convert(typeI,'int') as valueI , convert(typeL,'long') as valueL , " +
                "convert(typeB,'bool') as valueB, convert(typeN,'string') as valueN " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void convertFunctionExceptionTest10() throws InterruptedException {
        log.info("convert function test 10");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "define stream typeStream (typeS string, typeF float, typeD double, typeI int, typeL " +
                "long, typeB bool, typeN double) ;";
        String query = "" +
                "@info(name = 'query1') " +
                "from typeStream " +
                "select convert(typeS,'invalidType') as valueS, convert(typeF,'float') as valueF, convert" +
                "(typeD," +
                "'double') as valueD , convert(typeI,'int') as valueI , convert(typeL,'long') as valueL , " +
                "convert(typeB,'bool') as valueB, convert(typeN,'string') as valueN " +
                "insert into outputStream ;";

        siddhiManager.createSiddhiAppRuntime(cseEventStream + query);
    }
}
