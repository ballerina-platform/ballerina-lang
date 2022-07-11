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
import org.ballerinalang.test.context.Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class to test functionality of Ballerina strand dump.
 *
 * @since 2201.2.0
 */
public class StrandDumpTest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "troubleshoot", "strandDump")
            .toAbsolutePath().toString();
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testStrandDumpOfBalPackage() throws BallerinaTestException {
        Path expectedOutputFilePath = Paths.get(testFileLocation, "testOutputs",
                "testPackageWithModulesStrandDumpRegEx.txt");
        String sourceRoot = testFileLocation + "/";
        String packageName = "testPackageWithModules";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        bMainInstance.runMain("build", new String[]{packageName}, envProperties, null, null, sourceRoot);

        String jarPath = Paths.get(Paths.get(sourceRoot, packageName).toString(), "target", "bin",
                packageName + ".jar").toFile().getPath();
        runJarAndVerifyStrandDump(envProperties, jarPath, sourceRoot, expectedOutputFilePath);
    }

    @Test
    public void testStrandDumpOfSingleBalFile() throws BallerinaTestException {
        Path expectedOutputFilePath = Paths.get(testFileLocation, "testOutputs", "balProgram1StrandDumpRegEx.txt");
        String commandDir = balServer.getServerHome();
        String balFile = testFileLocation + "/singleBalFiles/balProgram1.bal";
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        bMainInstance.runMain("build", new String[]{balFile}, envProperties, null, null, commandDir);

        String balFileName = Paths.get(balFile).getFileName().toString();
        String jarPath = Paths.get(Paths.get(commandDir).toString(),
                balFileName.substring(0, balFileName.length() - 4) + ".jar").toString();
        runJarAndVerifyStrandDump(envProperties, jarPath, commandDir, expectedOutputFilePath);
    }

    private void runJarAndVerifyStrandDump(Map<String, String> envProperties, String jarPath, String commandDir,
                                           Path expectedOutputFilePath) throws BallerinaTestException {
        if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
            return;
        }

        try {
            List<String> runCmdSet = new ArrayList<>();
            runCmdSet.add("java");
            if (envProperties.containsKey(JAVA_OPTS)) {
                runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
            }
            String tempBalHome = new File("src" + File.separator + "test" + File.separator +
                    "resources" + File.separator + "ballerina.home").getAbsolutePath();
            runCmdSet.add("-Dballerina.home=" + tempBalHome);
            runCmdSet.addAll(Arrays.asList("-jar", jarPath));

            ProcessBuilder processBuilder = new ProcessBuilder(runCmdSet).directory(new File(commandDir));
            Map<String, String> env = processBuilder.environment();
            for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                env.put(entry.getKey(), entry.getValue());
            }
            Process process = processBuilder.start();
            Thread.sleep(6000);
            Runtime.getRuntime().exec("kill -SIGTRAP " + process.pid());
            Thread.sleep(4000);
            Runtime.getRuntime().exec("kill -SIGINT " + process.pid());

            String obtainedStrandDump = getStdOutAsString(process);
            process.waitFor();
            verifyObtainedStrandDump(expectedOutputFilePath, obtainedStrandDump);
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }

    private void verifyObtainedStrandDump(Path expectedOutputFilePath, String obtainedStrandDump)
            throws BallerinaTestException {
        List<String> expectedOutputRegEx;
        try {
            expectedOutputRegEx = Files.lines(expectedOutputFilePath)
                    .filter(s -> !s.isBlank()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new BallerinaTestException("Failure to read from the expected strand dump output file");
        }

        for (String expectedRegEx : expectedOutputRegEx) {
            Pattern pattern = Pattern.compile(expectedRegEx);
            Matcher matcher = pattern.matcher(obtainedStrandDump);
            if (!matcher.find()) {
                throw new BallerinaTestException("Failure to obtain the expected strand dump.\nExpected regular " +
                        "expression: " + expectedRegEx + "\nObtained strand dump:\n" + obtainedStrandDump);
            }
        }
    }

    private String getStdOutAsString(Process process) throws BallerinaTestException {
        String output;
        InputStream inputStream = process.getInputStream();
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader buffer = new BufferedReader(inputStreamReader)) {
            output = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new BallerinaTestException("Error when reading from the stdout ", e);
        }
        return output;
    }
}
