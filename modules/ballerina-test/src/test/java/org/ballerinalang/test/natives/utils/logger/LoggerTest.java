/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.natives.utils.logger;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * Test cases for the log API
 *
 * @since 0.94
 */
public class LoggerTest {

    private final String timestampFormat = "yyyy-MM-dd hh:mm:ss,SSS";
    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();

    private PrintStream original;
    private CompileResult result;

    @BeforeTest
    public void setup() {
        original = System.err;
        System.setErr(new PrintStream(consoleOutput));
        System.setProperty("java.util.logging.config.file",
                           ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
        result = BTestUtils.compile("test-src/natives/utils/logger/log-api.bal");
    }

    @AfterTest
    public void cleanup() {
        System.setErr(original);
    }

    @Test
    public void testLogError() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test error log";
        final String expectedLog = "ERROR [] - " + logMsg + " \n";

        BTestUtils.invoke(result, "testError", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test
    public void testLogWarn() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test warn log";
        final String expectedLog = "WARN [] - " + logMsg + " \n";

        BTestUtils.invoke(result, "testWarn", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test
    public void testLogInfo() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test info log";
        final String expectedLog = "INFO [] - " + logMsg + " \n";

        BTestUtils.invoke(result, "testInfo", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test
    public void testLogDebug() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test debug log";
        final String expectedLog = "DEBUG [] - " + logMsg + " \n";

        BTestUtils.invoke(result, "testDebug", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test
    public void testLogTrace() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test trace log";
        final String expectedLog = "TRACE [] - " + logMsg + " \n";

        BTestUtils.invoke(result, "testTrace", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }
}
