/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.logging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test case for the ballerina/log module.
 *
 * @since 1.0.0
 */
public class LogAPITestCase extends BaseTest {

    private static final String projectDirPath = Paths.get("src", "test", "resources", "logging", "log-project")
            .toAbsolutePath().toString();
    private static final String testFileLocation = Paths.get("src", "test", "resources", "logging")
            .toAbsolutePath().toString();
    private static final String testFileName = "log_level_test.bal";
    private static final String logLevelProperty = "b7a.log.level";

    private static final String errLog = "ERROR level log";
    private static final String errLogWithErr = "ERROR level log with error : error B7aError foo=bar";
    private static final String warnLog = "WARN level log";
    private static final String infoLog = "INFO level log";
    private static final String debugLog = "DEBUG level log";
    private static final String traceLog = "TRACE level log";

    private static final PrintStream console = System.out;

    @Test
    public void testBasicLogFunctionality() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{"mainmod"}, new HashMap<>(),
                                                           projectDirPath, true);
        String[] logLines = output.split("\n");
        assertEquals(logLines.length, 4);

        validateLogLevel(logLines[0], "INFO", "[logorg/foo]", "Logging from inside `foo` module");
        validateLogLevel(logLines[1], "INFO", "[logorg/bar]", "Logging from inside `bar` module");
        validateLogLevel(logLines[2], "ERROR", "[logorg/baz]", "Logging at ERROR level inside `baz`");
        validateLogLevel(logLines[3], "INFO", "[logorg/mainmod]", "Logging from inside `mainmod` module");
    }

    @Test
    public void testLogsOff() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=OFF", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        assertTrue(output.isEmpty());
    }

    @Test
    public void testErrorLevel() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=ERROR", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 2);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);
    }

    @Test
    public void testWarnLevel() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=WARN", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 3);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "WARN", "[]", warnLog);
    }

    @Test
    public void testInfoLevel() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=INFO", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 4);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "WARN", "[]", warnLog);

        console.println(logLines[3]);
        validateLogLevel(logLines[3], "INFO", "[]", infoLog);
    }

    @Test
    public void testDebugLevel() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=DEBUG", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 5);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "WARN", "[]", warnLog);

        console.println(logLines[3]);
        validateLogLevel(logLines[3], "INFO", "[]", infoLog);

        console.println(logLines[4]);
        validateLogLevel(logLines[4], "DEBUG", "[]", debugLog);
    }

    @Test
    public void testTraceLevel() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=TRACE", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 6);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "WARN", "[]", warnLog);

        console.println(logLines[3]);
        validateLogLevel(logLines[3], "INFO", "[]", infoLog);

        console.println(logLines[4]);
        validateLogLevel(logLines[4], "DEBUG", "[]", debugLog);

        console.println(logLines[5]);
        validateLogLevel(logLines[5], "TRACE", "[]", traceLog);
    }

    @Test
    public void testAllOn() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", logLevelProperty + "=ALL", testFileName};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), testFileLocation, true);
        String[] logLines = output.split("\n");

        assertEquals(logLines.length, 6);

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "ERROR", "[]", errLog);

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "ERROR", "[]", errLogWithErr);

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "WARN", "[]", warnLog);

        console.println(logLines[3]);
        validateLogLevel(logLines[3], "INFO", "[]", infoLog);

        console.println(logLines[4]);
        validateLogLevel(logLines[4], "DEBUG", "[]", debugLog);

        console.println(logLines[5]);
        validateLogLevel(logLines[5], "TRACE", "[]", traceLog);
    }

    @Test
    public void testSettingLogLevelToPackage() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[]{"-e", "logorg/foo.loglevel=DEBUG", "-e", "logorg/baz.loglevel=ERROR", "-e",
                "logorg/mainmod.loglevel=OFF", "mainmod"};
        String output = bMainInstance.runMainAndReadStdOut("run", args, new HashMap<>(), projectDirPath, true);
        String[] logLines = output.split("\n");
        assertEquals(logLines.length, 4, printLogLines(logLines));

        console.println(logLines[0]);
        validateLogLevel(logLines[0], "INFO", "[logorg/foo]", "Logging from inside `foo` module");

        console.println(logLines[1]);
        validateLogLevel(logLines[1], "DEBUG", "[logorg/foo]", "Logging at DEBUG level inside `foo`");

        console.println(logLines[2]);
        validateLogLevel(logLines[2], "INFO", "[logorg/bar]", "Logging from inside `bar` module");

        console.println(logLines[3]);
        validateLogLevel(logLines[3], "ERROR", "[logorg/baz]", "Logging at ERROR level inside `baz`");
    }

    private void validateLogLevel(String log, String logLevel, String logLocation, String logMsg) {
        assertTrue(log.contains(logLevel));
        assertTrue(log.contains(logLocation));
        assertTrue(log.contains(logMsg));
    }

    private String printLogLines(String[] logs) {
        StringBuilder sBuilder = new StringBuilder("\n");
        for (String log : logs) {
            sBuilder.append(log).append('\n');
        }
        return sBuilder.toString();
    }
}
