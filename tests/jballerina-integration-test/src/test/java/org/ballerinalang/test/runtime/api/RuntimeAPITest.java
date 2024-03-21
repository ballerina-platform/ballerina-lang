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
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerLogReader;
import org.ballerinalang.test.util.BFileUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        Path sourceRoot = Paths.get(testFileLocation, "function_invocation").toAbsolutePath();
        bMainInstance.runMain("build", new String[0], new HashMap<>(), new String[0], null,
                sourceRoot.toString());
    }

    @Test
    public void testRuntimeAPIsForBalFunctionInvocation() throws BallerinaTestException {
        Path jarPath = Paths.get(testFileLocation, "function_invocation", "target", "bin",
                "function_invocation.jar").toAbsolutePath();
        compileJavaSource(jarPath, "RuntimeAPICall.java", "targetDir");
        unzipJarFile(jarPath, "targetDir");
        createExecutableJar("targetDir", "org.ballerinalang.test.runtime.api.RuntimeAPICall");
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);

        // Run the executable jar and assert the output
        Path execJarPath = Paths.get(javaSrcLocation.toString(), "targetDir", "test-exec.jar").toAbsolutePath();
        List<String> runCmdSet = new ArrayList<>();
        runCmdSet.add("java");
        if (envProperties.containsKey(JAVA_OPTS)) {
            runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
        }
        runCmdSet.add("-jar");
        runCmdSet.add(execJarPath.toString());
        ProcessBuilder runProcessBuilder = new ProcessBuilder(runCmdSet);
        Map<String, String> env = runProcessBuilder.environment();
        env.putAll(envProperties);
        try {
            Process runProcess = runProcessBuilder.start();
            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", runProcess.getInputStream());
            List<LogLeecher> leechers = new ArrayList<>();
            leechers.add(new LogLeecher("12"));
            leechers.add(new LogLeecher("Dr. John Doe"));
            leechers.add(new LogLeecher("{\"id\":1001,\"name\":\"John\"," +
                    "\"sportsActivity\":{\"event\":\"100m\",\"year\":2020}}"));
            addToServerInfoLogReader(serverInfoLogReader, leechers);
            serverInfoLogReader.start();
            bMainInstance.waitForLeechers(leechers, 5000);
            runProcess.waitFor();
            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while running the java file");
        }
    }

    @Test
    public void testBalFunctionInvocationAPINegative() throws BallerinaTestException {
        Path jarPath = Paths.get(testFileLocation, "function_invocation", "target", "bin",
                "function_invocation.jar").toAbsolutePath();
        compileJavaSource(jarPath, "RuntimeAPICallNegative.java", "target-dir-negative");
        unzipJarFile(jarPath, "target-dir-negative");
        createExecutableJar("target-dir-negative",
                "org.ballerinalang.test.runtime.api.RuntimeAPICallNegative");
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);

        // Run the executable jar and assert the output
        Path execJarPath = Paths.get(javaSrcLocation.toString(), "target-dir-negative",
                "test-exec.jar").toAbsolutePath();
        List<String> runCmdSet = new ArrayList<>();
        runCmdSet.add("java");
        if (envProperties.containsKey(JAVA_OPTS)) {
            runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
        }
        runCmdSet.add("-jar");
        runCmdSet.add(execJarPath.toString());
        ProcessBuilder runProcessBuilder = new ProcessBuilder(runCmdSet);
        Map<String, String> env = runProcessBuilder.environment();
        env.putAll(envProperties);
        runProcessBuilder.redirectErrorStream(true);
        try {
            Process runProcess = runProcessBuilder.start();
            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", runProcess.getInputStream());
            List<LogLeecher> leechers = new ArrayList<>();
            leechers.add(new LogLeecher("function 'add' is called before module initialization",
                    LogLeecher.LeecherType.ERROR));
            addToServerInfoLogReader(serverInfoLogReader, leechers);
            serverInfoLogReader.start();
            bMainInstance.waitForLeechers(leechers, 5000);
            runProcess.waitFor();
            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while running the java file");
        }
    }

    @Test
    public void testModuleStartCallNegative() throws BallerinaTestException {
        Path jarPath = Paths.get(testFileLocation, "function_invocation", "target", "bin",
                "function_invocation.jar").toAbsolutePath();
        compileJavaSource(jarPath, "ModuleStartCallNegative.java", "start-call-negative");
        unzipJarFile(jarPath, "start-call-negative");
        createExecutableJar("start-call-negative",
                "org.ballerinalang.test.runtime.api.ModuleStartCallNegative");
        Map<String, String> envProperties = new HashMap<>();
        bMainInstance.addJavaAgents(envProperties);

        // Run the executable jar and assert the output
        Path execJarPath = Paths.get(javaSrcLocation.toString(), "start-call-negative",
                "test-exec.jar").toAbsolutePath();
        List<String> runCmdSet = new ArrayList<>();
        runCmdSet.add("java");
        if (envProperties.containsKey(JAVA_OPTS)) {
            runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
        }
        runCmdSet.add("-jar");
        runCmdSet.add(execJarPath.toString());
        ProcessBuilder runProcessBuilder = new ProcessBuilder(runCmdSet);
        Map<String, String> env = runProcessBuilder.environment();
        env.putAll(envProperties);
        runProcessBuilder.redirectErrorStream(true);
        try {
            Process runProcess = runProcessBuilder.start();
            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", runProcess.getInputStream());
            List<LogLeecher> leechers = new ArrayList<>();
            leechers.add(new LogLeecher("'start' method is called before module initialization",
                    LogLeecher.LeecherType.ERROR));
            addToServerInfoLogReader(serverInfoLogReader, leechers);
            serverInfoLogReader.start();
            bMainInstance.waitForLeechers(leechers, 5000);
            runProcess.waitFor();
            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while running the java file");
        }
    }

    @AfterClass
    public void tearDown() {
        bMainInstance = null;
        BFileUtil.deleteDirectory(Paths.get(javaSrcLocation.toString(), "targetDir").toFile());
        BFileUtil.deleteDirectory(Paths.get(javaSrcLocation.toString(), "target-dir-negative").toFile());
        BFileUtil.deleteDirectory(Paths.get(javaSrcLocation.toString(), "start-call-negative").toFile());
    }

    private static void compileJavaSource(Path jarPath, String srcFile, String targetDir)
            throws BallerinaTestException {
        List<String> compileCmdSet = new ArrayList<>();
        compileCmdSet.add("javac");
        compileCmdSet.add("-cp");
        compileCmdSet.add(jarPath.toString());
        compileCmdSet.add("-d");
        compileCmdSet.add(targetDir);
        compileCmdSet.add(Paths.get(javaSrcLocation.toString(), srcFile).toString());
        ProcessBuilder compile = new ProcessBuilder(compileCmdSet).directory(javaSrcLocation.toFile());
        try {
            Process process = compile.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error occurred while compiling the java file");
        }
    }

    private static void unzipJarFile(Path jarPath, String targetDir) throws BallerinaTestException {
        List<String> unzipProcessCmdSet = new ArrayList<>();
        unzipProcessCmdSet.add("unzip");
        unzipProcessCmdSet.add("-qq");
        unzipProcessCmdSet.add(jarPath.toString());
        unzipProcessCmdSet.add("-d");
        unzipProcessCmdSet.add(targetDir);
        ProcessBuilder unzipProcess = new ProcessBuilder(unzipProcessCmdSet).directory(javaSrcLocation.toFile());
        unzipProcess.redirectErrorStream(true);
        try {
            Process process = unzipProcess.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while unzipping the jar file");
        }
    }

    private static void createExecutableJar(String targetDir, String mainClass) throws BallerinaTestException {
        List<String> jarCmdSet = new ArrayList<>();
        jarCmdSet.add("jar");
        jarCmdSet.add("-cfe");
        jarCmdSet.add("test-exec.jar");
        jarCmdSet.add(mainClass);
        jarCmdSet.add(".");
        Path targetPath = Paths.get(javaSrcLocation.toString(), targetDir).toAbsolutePath();
        ProcessBuilder jarProcess = new ProcessBuilder(jarCmdSet).inheritIO().directory(targetPath.toFile());
        try {
            Process process = jarProcess.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new BallerinaTestException("Error occurred while packing the jar file");
        }
    }

    private static void addToServerInfoLogReader(ServerLogReader serverInfoLogReader, List<LogLeecher> leechers) {
        for (LogLeecher leecher : leechers) {
            serverInfoLogReader.addLeecher(leecher);
        }
    }
}
