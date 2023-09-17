/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.test.profiler;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerLogReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tests for Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class ProfilerTest extends BaseTest {
    private static final String testFileLocation = Paths.get("src/test/resources/profiler")
            .toAbsolutePath().toString();
    private final String outputFile = "ProfilerOutput.html";
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testProfilerExecutionWithBalPackage() throws BallerinaTestException {
        String sourceRoot = testFileLocation + "/";
        String packageName = "projectForProfile/package_a";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        LogLeecher[] leechers = getProfilerLogLeechers(packageName + "/target/bin/" + outputFile);
        bMainInstance.runMain("profile", new String[]{packageName}, envProperties,
                null, leechers, sourceRoot);
        for (LogLeecher leecher : leechers) {
            leecher.waitForText(5000);
        }
    }

    private LogLeecher[] getProfilerLogLeechers(String htmlFilePath) {
        return new LogLeecher[]{
                new LogLeecher("[1/6] Initializing..."),
                new LogLeecher("[2/6] Copying executable..."),
                new LogLeecher("[3/6] Performing analysis..."),
                new LogLeecher("[4/6] Instrumenting functions..."),
                new LogLeecher("○ Instrumented module count: "),
                new LogLeecher("○ Instrumented function count: "),
                new LogLeecher("[5/6] Running executable..."),
                new LogLeecher("[6/6] Generating output..."),
                new LogLeecher("○ Execution time:"),
                new LogLeecher("○ Output: "),
                new LogLeecher(htmlFilePath)};
    }

    @Test
    public void testProfilerExecutionWithSingleBalFile() throws BallerinaTestException {
        String sourceRoot = testFileLocation + "/";
        String fileName = "profiler_single_file.bal";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        LogLeecher[] leechers = getProfilerLogLeechers(sourceRoot + outputFile);
        bMainInstance.runMain("profile", new String[]{fileName}, envProperties,
                null, leechers, sourceRoot);
        for (LogLeecher leecher : leechers) {
            leecher.waitForText(5000);
        }
    }

    @Test
    public void testProfilerExecutionWithKillSignal() throws BallerinaTestException {
        String sourceRoot = testFileLocation + "/";
        String packageName = "serviceProjectForProfile/package_a";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        LogLeecher[] beforeExecleechers = new LogLeecher[]{new LogLeecher("[1/6] Initializing..."),
                new LogLeecher("[2/6] Copying executable..."),
                new LogLeecher("[3/6] Performing analysis..."),
                new LogLeecher("[4/6] Instrumenting functions..."),
                new LogLeecher("○ Instrumented module count: "),
                new LogLeecher("○ Instrumented function count: "),
                new LogLeecher("[5/6] Running executable...")};
        Process process = bMainInstance.runCommandAndGetProcess("profile", new String[]{packageName},
                envProperties, sourceRoot);
        LogLeecher[] afterExecleechers = new LogLeecher[]{
                new LogLeecher("[6/6] Generating output..."),
                new LogLeecher("○ Execution time:"),
                new LogLeecher("○ Output: ")};
        ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
        addLogLeechers(beforeExecleechers, serverInfoLogReader);
        addLogLeechers(afterExecleechers, serverInfoLogReader);
        try {
            serverInfoLogReader.start();
            bMainInstance.waitForLeechers(List.of(beforeExecleechers), 20000);
            Thread.sleep(1000);
            ProcessHandle profilerHandle = process.children().findFirst().get().children().findFirst().get();
            long profileId = profilerHandle.pid();
            long balProcessID = profilerHandle.children().findFirst().get().pid();
            Runtime.getRuntime().exec("kill -SIGINT " + balProcessID);
            Runtime.getRuntime().exec("kill -SIGINT " + profileId);
            bMainInstance.waitForLeechers(List.of(afterExecleechers), 20000);
            process.waitFor();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error testing Ballerina profiler", e);
        }
        serverInfoLogReader.stop();
        serverInfoLogReader.removeAllLeechers();
    }

    private static void addLogLeechers(LogLeecher[] leechers, ServerLogReader serverInfoLogReader) {
        for (LogLeecher leecher : leechers) {
            serverInfoLogReader.addLeecher(leecher);
        }
    }
}
