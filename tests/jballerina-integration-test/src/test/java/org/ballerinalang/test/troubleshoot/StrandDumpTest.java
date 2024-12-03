/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.troubleshoot;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerLogReader;
import org.ballerinalang.test.context.Utils;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class to test functionality of Ballerina strand dump.
 *
 * @since 2201.2.0
 */
public class StrandDumpTest extends BaseTest {

    private static final Path testFileLocation = Path.of("src/test/resources/troubleshoot/strandDump")
            .toAbsolutePath();
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private static final int TIMEOUT = 60000;
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testStrandDumpOfBalPackage() throws BallerinaTestException {
        Path expectedOutputFilePath = testFileLocation.resolve(
                "testOutputs/testPackageWithModulesStrandDumpRegEx.txt");
        Path steadyStateOutputFilePath = testFileLocation.resolve(
                "testOutputs/testPackageWithModulesSteadyState.txt");
        Path sourceRoot = testFileLocation;
        String packageName = "testPackageWithModules";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.runMain("build", new String[]{packageName}, envProperties, null, null, sourceRoot);

        Path jarPath = sourceRoot.resolve(packageName + "/target/bin/" + packageName + ".jar");
        runJarAndVerifyStrandDump(envProperties, jarPath, sourceRoot, expectedOutputFilePath,
                steadyStateOutputFilePath);
    }

    @Test
    public void testStrandDumpDuringBalTest() throws BallerinaTestException {
        if (Utils.isWindowsOS()) {
            throw new SkipException("Currently not working on windows");
        }

        Path expectedOutputFilePath = testFileLocation.resolve("testOutputs/balTestStrandDumpRegEx.txt");
        Path steadyStateOutputFilePath = testFileLocation.resolve("testOutputs/balTestSteadyState.txt");
        String packageName = "testPackageWithModules";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);

        String[] cmdArgs = new String[]{"bash", balServer.getServerHome() + "/bin/bal", "test", packageName};
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(testFileLocation.toFile());
        startProcessAndVerifyStrandDump(processBuilder, envProperties, expectedOutputFilePath,
                steadyStateOutputFilePath, false);
    }

    @Test
    public void testStrandDumpOfSingleBalFile() throws BallerinaTestException {
        Path expectedOutputFilePath = testFileLocation.resolve("testOutputs/balProgram1StrandDumpRegEx.txt");
        Path steadyStateOutputFilePath = testFileLocation.resolve("testOutputs/balProgram1SteadyStateOutput.txt");
        Path commandDir = balServer.getServerHome();
        Path balFile = testFileLocation.resolve("singleBalFiles/balProgram1.bal");
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.runMain("build", new String[]{balFile.toString()}, envProperties, null, null, commandDir);

        String balFileName = balFile.getFileName().toString();
        Path jarPath = commandDir.resolve(balFileName.substring(0, balFileName.length() - 4) + ".jar");
        runJarAndVerifyStrandDump(envProperties, jarPath, commandDir, expectedOutputFilePath,
                steadyStateOutputFilePath);
    }

    private void runJarAndVerifyStrandDump(Map<String, String> envProperties, Path jarPath, Path commandDir,
                                           Path expectedStrandDumpFilePath, Path steadyStateOutputFilePath)
            throws BallerinaTestException {
        if (Utils.isWindowsOS()) {
            throw new SkipException("Currently not working on windows");
        }

        List<String> runCmdSet = new ArrayList<>();
        bMainInstance.addJavaAgents(envProperties);
        runCmdSet.add("java");
        runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
        String tempBalHome = Path.of("src/test/resources/ballerina.home").toAbsolutePath().toString();
        runCmdSet.add("-Dballerina.home=" + tempBalHome);
        runCmdSet.addAll(Arrays.asList("-jar", jarPath.toAbsolutePath().toString()));

        ProcessBuilder processBuilder = new ProcessBuilder(runCmdSet).directory(commandDir.toAbsolutePath().toFile());
        startProcessAndVerifyStrandDump(processBuilder, envProperties, expectedStrandDumpFilePath,
                steadyStateOutputFilePath, true);
    }

    private void startProcessAndVerifyStrandDump(ProcessBuilder processBuilder, Map<String, String> envProperties,
                                                 Path expectedOutputFilePath, Path steadyStateOutputFilePath,
                                                 boolean isJar) throws BallerinaTestException {

        Map<String, String> env = processBuilder.environment();
        env.putAll(envProperties);

        try {
            Process process = processBuilder.start();
            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            List<LogLeecher> steadyStateLeechers = new ArrayList<>();
            populateLeechers(steadyStateLeechers, steadyStateOutputFilePath, serverInfoLogReader);
            List<LogLeecher> strandDumpLeechers = new ArrayList<>();
            populateLeechers(strandDumpLeechers, expectedOutputFilePath, serverInfoLogReader);
            serverInfoLogReader.start();
            bMainInstance.waitForLeechers(steadyStateLeechers, TIMEOUT);
            Thread.sleep(1000);
            long balProcessID = isJar ? process.pid()
                    : process.children().findFirst().get().children().findFirst().get().pid();
            Runtime.getRuntime().exec("kill -SIGTRAP " + balProcessID);
            bMainInstance.waitForLeechers(strandDumpLeechers, TIMEOUT);
            Runtime.getRuntime().exec("kill -SIGINT " + balProcessID);
            process.waitFor();
            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error testing strand dump", e);
        }
    }

    private static void populateLeechers(List<LogLeecher> leecherList, Path leecherFilePath,
                                  ServerLogReader serverInfoLogReader) throws BallerinaTestException {
        List<String> nonEmptyLines = readFileNonEmptyLines(leecherFilePath);
        for (String str : nonEmptyLines) {
            LogLeecher leecher = new LogLeecher(str, true, LogLeecher.LeecherType.INFO);
            leecherList.add(leecher);
            serverInfoLogReader.addLeecher(leecher);
        }
    }

    private static List<String> readFileNonEmptyLines(Path filePath) throws BallerinaTestException {
        try (Stream<String> fileLines = Files.lines(filePath)) {
            return fileLines.filter(s -> !s.isBlank()).toList();
        } catch (IOException e) {
            throw new BallerinaTestException("Failure to read from the file: " + filePath);
        }
    }
}
