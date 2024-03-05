/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.runtime.api;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.AssertionUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.projects.util.ProjectConstants.USER_DIR;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_HOME;
import static org.ballerinalang.test.util.SQLDBUtils.deleteDirectory;

/**
 * Test class to test the functionality of Ballerina runtime APIs for invoking functions.
 *
 * @since 2201.9.0
 */
public class RuntimeAPITest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "runtime.api")
            .toAbsolutePath().toString();
    private static final Path javaSrcLocation = Paths.get("src", "test", "java", "org", "ballerinalang",
            "test", "runtime", "api").toAbsolutePath();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        Path sourceRoot = Paths.get(testFileLocation, "function_invocation").toAbsolutePath();
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);
        bMainInstance.runMain("build", new String[0], envProperties, new String[0], null,
                sourceRoot.toString());
    }

    @Test
    public void testRuntimeAPIsForBalFunctionInvocation() throws BallerinaTestException {
        Path jarPath = Paths.get(testFileLocation, "function_invocation", "target", "bin",
                "function_invocation.jar").toAbsolutePath();
        List<String> compileCmdSet = new ArrayList<>();
        compileCmdSet.add("javac");
        compileCmdSet.add("-cp");
        compileCmdSet.add(jarPath.toString());
        compileCmdSet.add("-d");
        compileCmdSet.add("targetDir");
        compileCmdSet.add(Paths.get(javaSrcLocation.toString(), "RuntimeAPICall.java").toString());
        ProcessBuilder compile = new ProcessBuilder(compileCmdSet).directory(javaSrcLocation.toFile());
        try {
            Process process = compile.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error occurred while compiling the java file");
        }

        List<String> unzipProcessCmdSet = new ArrayList<>();
        unzipProcessCmdSet.add("unzip");
        unzipProcessCmdSet.add("-qq");
        unzipProcessCmdSet.add(jarPath.toString());
        unzipProcessCmdSet.add("-d");
        unzipProcessCmdSet.add("targetDir");
        ProcessBuilder unzipProcess = new ProcessBuilder(unzipProcessCmdSet).directory(javaSrcLocation.toFile());
        unzipProcess.redirectErrorStream(true);
        try {
            Process process = unzipProcess.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while unzipping the jar file");
        }

        List<String> jarCmdSet = new ArrayList<>();
        jarCmdSet.add("jar");
        jarCmdSet.add("-cfe");
        jarCmdSet.add("test-exec.jar");
        jarCmdSet.add("org.ballerinalang.test.runtime.api.RuntimeAPICall");
        jarCmdSet.add(".");
        Path targetDir = Paths.get(javaSrcLocation.toString(), "targetDir").toAbsolutePath();
        ProcessBuilder jarProcess = new ProcessBuilder(jarCmdSet).inheritIO().directory(targetDir.toFile());
        try {
            Process process = jarProcess.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while packing the jar file");
        }

        Path execJarPath = Paths.get(javaSrcLocation.toString(), "targetDir", "test-exec.jar").toAbsolutePath();
        List<String> runCmdSet = new ArrayList<>();
        runCmdSet.add("java");
        runCmdSet.add("-jar");
        runCmdSet.add(execJarPath.toString());
        ProcessBuilder runProcessBuilder = new ProcessBuilder(runCmdSet);
        String jacocoArgLine = "-javaagent:" + Paths.get(System.getProperty(BALLERINA_HOME), "bre", "lib",
                "jacocoagent.jar") + "=destfile=" + Paths.get(System.getProperty(USER_DIR))
                .resolve("build").resolve("jacoco").resolve("test.exec") + " ";
        runProcessBuilder.environment().put("JAVA_OPTS", jacocoArgLine);
        runProcessBuilder.redirectErrorStream(true);
        Path outputFileDir = Paths.get(javaSrcLocation.toString(), "targetDir", "output.txt");
        runProcessBuilder.redirectOutput(new File(outputFileDir.toAbsolutePath().toString()));
        try {
            Process runProcess = runProcessBuilder.start();
            runProcess.waitFor();
            String programOutput = Files.readString(outputFileDir);
            AssertionUtils.assertOutput("RuntimeAPITest-invokeFunction.txt", programOutput);
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while running the java file");
        }
    }

    @AfterClass
    public void tearDown() {
        bMainInstance = null;
        deleteDirectory(Paths.get(javaSrcLocation.toString(), "targetDir").toFile());
    }
}
