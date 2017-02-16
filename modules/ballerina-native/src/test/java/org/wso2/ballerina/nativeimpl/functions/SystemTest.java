/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.nativeimpl.functions;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.nativeimpl.util.BTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Native functions in ballerina.lang.system.
 */
public class SystemTest {

    private BLangProgram bLangProgram;
    private final String printFuncName = "testPrintAndPrintln";

    private PrintStream original;
    Logger rootLogger;
    TestLogAppender testLogAppender;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        rootLogger = Logger.getRootLogger();
        original = System.out;
        bLangProgram = BTestUtils.parseBalFile("samples/systemTest.bal");
        rootLogger.getLoggerRepository().getLogger("org.wso2.ballerina.nativeimpl.lang.system")
                .setLevel(Level.ALL);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws IOException {
        System.setOut(original);
    }

    @Test
    public void testStringPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final String s2 = "A Greeting from Ballerina...!!!";
            final String expected = s1 + "\n" + s2;

            BValueType[] args = {new BString(s1), new BString(s2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "String", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testLongPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final long v1 = 1000;
            final long v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BLong(v1), new BLong(v2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "Long", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testIntPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final int v1 = 1000;
            final int v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BInteger(v1), new BInteger(v2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "Int", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testFloatPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final float v1 = 1000;
            final float v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BFloat(v1), new BFloat(v2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "Float", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testDoublePrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final double v1 = 1000.122;
            final double v2 = 1.12;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BDouble(v1), new BDouble(v2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "Double", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testBooleanPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final boolean v1 = false;
            final boolean v2 = true;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BBoolean(v1), new BBoolean(v2)};
            BLangFunctions.invoke(bLangProgram, printFuncName + "Boolean", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testLog() {
        testLogAppender = new TestLogAppender();
        rootLogger.addAppender(testLogAppender);
        try {
            BValueType[] args = {new BLong(100), new BDouble(10.1)};
            BLangFunctions.invoke(bLangProgram, "testLog", args);
            // We are not expecting boolean log in event list.
            Assert.assertEquals(testLogAppender.getEvents().size(), 5, "Logging events didn't match.");
            Assert.assertEquals(testLogAppender.events.get(0).getLevel(), Level.TRACE);
            Assert.assertEquals(testLogAppender.events.get(1).getLevel(), Level.DEBUG);
            Assert.assertEquals(testLogAppender.events.get(2).getLevel(), Level.INFO);
            Assert.assertEquals(testLogAppender.events.get(3).getLevel(), Level.WARN);
            Assert.assertEquals(testLogAppender.events.get(4).getLevel(), Level.ERROR);
        } finally {
            rootLogger.removeAppender(testLogAppender);
        }
    }

    @Test
    public void testFunctionTimes() {
        testLogAppender = new TestLogAppender();
        rootLogger.addAppender(testLogAppender);
        try {
            BLangFunctions.invoke(bLangProgram, "testTimeFunctions");
            // We are not expecting boolean log in event list.
            Assert.assertEquals(testLogAppender.getEvents().size(), 3, "Time Logging events didn't match.");
            Assert.assertTrue(!((String) testLogAppender.events.get(0).getMessage()).endsWith("[INFO] 0"));
            Assert.assertTrue(!((String) testLogAppender.events.get(1).getMessage()).endsWith("[INFO] 0"));
            Assert.assertTrue(!((String) testLogAppender.events.get(2).getMessage()).endsWith("[INFO] 0"));
        } finally {
            rootLogger.removeAppender(testLogAppender);
        }

    }

    @Test(description = "Test new line character in string")
    public void testNewlineCharacter() {
        java.io.ByteArrayOutputStream out = null;
        java.io.PrintStream mainStream = System.out;
        try {
            out = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));
            BLangFunctions.invoke(bLangProgram, "printNewline");
            String outPut = out.toString();
            Assert.assertNotNull(outPut, "string is not printed");
            //getting the last new line character
            Assert.assertEquals(outPut.charAt(outPut.length() - 1), '\n'
                    , "New line character not found in output string");
        } finally {
            System.setOut(mainStream);
            try {
                out.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }

    @Test
    public void testGetEnv() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            final String pathValue = System.getenv("PATH");
            BValueType[] args = {new BString("PATH")};
            Functions.invoke(bFile, "getEnvVar", args);
            Assert.assertEquals(outContent.toString(), pathValue);
        } finally {
            System.setOut(original);
        }
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testGetEnvNonExisting() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            BValueType[] args = {new BString("PATH2")};
            Functions.invoke(bFile, "getEnvVar", args);
            outContent.toString();
        } finally {
            System.setOut(original);
        }
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testGetEnvEmptyKey() throws IOException {
        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outContent));
            BValueType[] args = {new BString("")};
            Functions.invoke(bFile, "getEnvVar", args);
            outContent.toString();
        } finally {
            System.setOut(original);
        }
    }

    static class TestLogAppender extends AppenderSkeleton {

        List<LoggingEvent> events;

        public TestLogAppender() {
            this.events = new ArrayList<>();
        }

        List<LoggingEvent> getEvents() {
            return events;
        }

        @Override
        protected void append(LoggingEvent loggingEvent) {
            if (loggingEvent.getLoggerName().contains("ballerina")) {
                events.add(loggingEvent);
            }
        }

        @Override
        public void close() {
            // Do nothing.
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }

}
