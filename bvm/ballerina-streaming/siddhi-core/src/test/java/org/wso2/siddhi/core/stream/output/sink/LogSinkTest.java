/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.output.sink;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.UnitTestAppender;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class LogSinkTest {
    @Test
    public void testWithAllOptions() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "@App:name(\"HelloWorldApp\")\n" +
                "define stream CargoStream (weight int);";
        String outputStream = "@sink(type='log', prefix='My Log',  priority='info')\n" +
                "define stream OutputStream(weight int, totalWeight long);";

        String query = (
                "@info(name='HelloWorldQuery') " +
                        "from CargoStream " +
                        "select weight, sum(weight) as totalWeight " +
                        "insert into OutputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + outputStream + query);

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CargoStream");

        Logger logger = Logger.getLogger(LogSink.class);
        UnitTestAppender appender = new UnitTestAppender();
        logger.addAppender(appender);
        try {
            inputHandler.send(new Object[]{2});
            AssertJUnit.assertTrue(appender.getMessages().contains("My Log"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[2, 2]"));
            inputHandler.send(new Object[]{3});
            AssertJUnit.assertTrue(appender.getMessages().contains("My Log"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[3, 5]"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred when testing with all options", e);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testWithDefaultPrefix() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "@App:name(\"HelloWorldApp\")\n" +
                "define stream CargoStream (weight int);";
        String outputStream = "@sink(type='log',  priority='info')\n" +
                "define stream OutputStream(weight int, totalWeight long);";

        String query = (
                "@info(name='HelloWorldQuery') " +
                        "from CargoStream " +
                        "select weight, sum(weight) as totalWeight " +
                        "insert into OutputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + outputStream + query);

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CargoStream");

        Logger logger = Logger.getLogger(LogSink.class);
        UnitTestAppender appender = new UnitTestAppender();
        logger.addAppender(appender);
        try {
            inputHandler.send(new Object[]{2});
            AssertJUnit.assertTrue(appender.getMessages().contains("HelloWorldApp : OutputStream"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[2, 2]"));
            inputHandler.send(new Object[]{3});
            AssertJUnit.assertTrue(appender.getMessages().contains("HelloWorldApp : OutputStream"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[3, 5]"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred when testing with default prefix", e);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testWithDefaultPriority() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "@App:name(\"HelloWorldApp\")\n" +
                "define stream CargoStream (weight int);";
        String outputStream = "@sink(type='log', prefix='My Log')\n" +
                "define stream OutputStream(weight int, totalWeight long);";

        String query = (
                "@info(name='HelloWorldQuery') " +
                        "from CargoStream " +
                        "select weight, sum(weight) as totalWeight " +
                        "insert into OutputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + outputStream + query);

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CargoStream");

        Logger logger = Logger.getLogger(LogSink.class);
        UnitTestAppender appender = new UnitTestAppender();
        logger.addAppender(appender);
        try {
            inputHandler.send(new Object[]{2});
            AssertJUnit.assertTrue(appender.getMessages().contains("My Log"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[2, 2]"));
            inputHandler.send(new Object[]{3});
            AssertJUnit.assertTrue(appender.getMessages().contains("My Log"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[3, 5]"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred when testing with default priority", e);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testWithDefaultOptions() throws Exception {
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "@App:name(\"HelloWorldApp\")\n" +
                "define stream CargoStream (weight int);";
        String outputStream = "@sink(type='log')\n" +
                "define stream OutputStream(weight int, totalWeight long);";

        String query = (
                "@info(name='HelloWorldQuery') " +
                        "from CargoStream " +
                        "select weight, sum(weight) as totalWeight " +
                        "insert into OutputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + outputStream + query);

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CargoStream");

        Logger logger = Logger.getLogger(LogSink.class);
        UnitTestAppender appender = new UnitTestAppender();
        logger.addAppender(appender);
        try {
            inputHandler.send(new Object[]{2});
            AssertJUnit.assertTrue(appender.getMessages().contains("HelloWorldApp : OutputStream"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[2, 2]"));
            inputHandler.send(new Object[]{3});
            AssertJUnit.assertTrue(appender.getMessages().contains("HelloWorldApp : OutputStream"));
            AssertJUnit.assertTrue(appender.getMessages().contains("data=[3, 5]"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred when testing with default options", e);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
