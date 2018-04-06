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

package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test cases for the log API.
 *
 * @since 0.94
 */
public class LoggerTest {

    private final String timestampFormat = "yyyy-MM-dd hh:mm:ss,SSS";
    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();

    private PrintStream original;
    private CompileResult result;

    //    @BeforeClass
    public void setup() {
        original = System.err;
        System.setErr(new PrintStream(consoleOutput));
        System.setProperty("java.util.logging.config.file",
                           ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
        result = BCompileUtil.compile("test-src/natives/utils/logger/log-api.bal");
    }

//    @AfterTest
    public void cleanup() {
        System.setErr(original);
    }

    @Test(description = "Test case for error level logging through the log API", enabled = false)
    public void testLogError() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test error log";
        final String expectedLog = "ERROR [] - " + logMsg + " \n";

        BRunUtil.invoke(result, "testError", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test(description = "Test case for warn level logging through the log API", enabled = false)
    public void testLogWarn() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test warn log";
        final String expectedLog = "WARN [] - " + logMsg + " \n";

        BRunUtil.invoke(result, "testWarn", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test(description = "Test case for info level logging through the log API", enabled = false)
    public void testLogInfo() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test info log";
        final String expectedLog = "INFO [] - " + logMsg + " \n";

        BRunUtil.invoke(result, "testInfo", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test(description = "Test case for debug level logging through the log API", enabled = false)
    public void testLogDebug() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test debug log";
        final String expectedLog = "DEBUG [] - " + logMsg + " \n";

        BRunUtil.invoke(result, "testDebug", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }

    @Test(description = "Test case for trace level logging through the log API", enabled = false)
    public void testLogTrace() throws IOException {
        consoleOutput.reset();
        final String logMsg = "Test trace log";
        final String expectedLog = "TRACE [] - " + logMsg + " \n";

        BRunUtil.invoke(result, "testTrace", new BValue[]{new BString(logMsg)});

        Assert.assertEquals(consoleOutput.toString().substring(timestampFormat.length() + 1), expectedLog);
    }
}
