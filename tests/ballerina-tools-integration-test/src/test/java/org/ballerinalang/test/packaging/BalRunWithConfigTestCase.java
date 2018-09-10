/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Testing running a bal file with a config file.
 *
 * @since 0.982.0
 */
public class BalRunWithConfigTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Path ballerinaConfPath;
    private Path balSourcePath;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
        // Create the ballerina.conf
        ballerinaConfPath = tempProjectDirectory.resolve("ballerina.conf");
        Files.createFile(ballerinaConfPath);
        Files.write(ballerinaConfPath, "host=\"localhost\"".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        // Write the source
        balSourcePath = tempProjectDirectory.resolve("hello.bal");
        Files.createFile(balSourcePath);
        String content = "import ballerina/config;\n" +
                "import ballerina/io;\n" +
                "public function main (string... args) {\n" +
                " string host = config:getAsString(\"host\"); \n" +
                " io:println(host);\n" +
                "}";
        Files.write(balSourcePath, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test(description = "Test running a ballerina file with the default config from the same directory")
    public void testRunWithDefaultConfig() throws Exception {
        String[] clientArgs = {"hello.bal"};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
    }

    @Test(description = "Test running a ballerina file with the default config from another directory")
    public void testRunWithDefaultConfigFromDiff() throws Exception {
        Path tempDir = tempProjectDirectory.resolve("tempDir");
        Files.createDirectories(tempDir);

        String[] clientArgs = {balSourcePath.toString()};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempDir.toString());
    }

    @Test(description = "Test running a ballerina file by specifying the config file path")
    public void testRunWithConfig() throws Exception {
        Path tempDir = tempProjectDirectory.resolve("tempDirWithConfig");
        Files.createDirectories(tempDir);

        // Copy the config
        Path exampleConfig = tempDir.resolve("ballerina.config");
        Files.createFile(exampleConfig);
        Files.copy(ballerinaConfPath, exampleConfig, StandardCopyOption.REPLACE_EXISTING);

        String[] clientArgs = {"--config", exampleConfig.toString(), balSourcePath.toString()};
        LogLeecher clientLeecher = new LogLeecher("localhost");
        balClient.runMain("run", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          tempDir.toString());
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
