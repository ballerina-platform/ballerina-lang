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
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Testing pushing, pulling, searching a module from central and installing module to home repository.
 *
 * @since 0.982.0
 */
public class ListDependencyTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private String orgName = "integrationtests";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test init a ballerina project to be pushed to central")
    public void testInitProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                                                projectPath.toString());
    }

    @Test(description = "Test listing dependencies of a module", dependsOnMethods = "testInitProject", enabled = false)
    public void testListDependenciesOfModule() throws Exception {
        String[] clientArgs = {"foo"};
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String msg = orgName + "/foo:0.0.1\n" +
                "└── ballerina/io";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("list", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test listing dependencies of a single bal file", dependsOnMethods = "testInitProject",
            enabled = false)
    public void testListDependenciesOfBalFile() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String content = "import ballerina/io;\n import ballerina/http; \n \n public function main(string... args) {\n"
                + "    io:println(\"Hello World!\"); \n }\n";
        Files.write(projectPath.resolve("main.bal"), content.getBytes(), StandardOpenOption.CREATE_NEW);
        String[] clientArgs = {"main.bal"};

        String msg = "main.bal\n" +
                "├── ballerina/io\n" +
                "└── ballerina/http\n" +
                "    ├── ballerina/reflect\n" +
                "    ├── ballerina/auth\n" +
                "    │   ├── ballerina/crypto\n" +
                "    │   ├── ballerina/config\n" +
                "    │   │   └── ballerina/system\n" +
                "    │   ├── ballerina/system\n" +
                "    │   ├── ballerina/time\n" +
                "    │   ├── ballerina/log\n" +
                "    │   ├── ballerina/internal\n" +
                "    │   │   ├── ballerina/time\n" +
                "    │   │   ├── ballerina/file\n" +
                "    │   │   ├── ballerina/log\n" +
                "    │   │   └── ballerina/io\n" +
                "    │   ├── ballerina/cache\n" +
                "    │   │   ├── ballerina/task\n" +
                "    │   │   ├── ballerina/system\n" +
                "    │   │   └── ballerina/time\n" +
                "    │   └── ballerina/runtime\n" +
                "    ├── ballerina/internal\n" +
                "    │   ├── ballerina/time\n" +
                "    │   ├── ballerina/file\n" +
                "    │   ├── ballerina/log\n" +
                "    │   └── ballerina/io\n" +
                "    ├── ballerina/config\n" +
                "    │   └── ballerina/system\n" +
                "    ├── ballerina/math\n" +
                "    ├── ballerina/crypto\n" +
                "    ├── ballerina/mime\n" +
                "    │   ├── ballerina/io\n" +
                "    │   └── ballerina/file\n" +
                "    ├── ballerina/file\n" +
                "    ├── ballerina/time\n" +
                "    ├── ballerina/io\n" +
                "    ├── ballerina/runtime\n" +
                "    ├── ballerina/cache\n" +
                "    │   ├── ballerina/task\n" +
                "    │   ├── ballerina/system\n" +
                "    │   └── ballerina/time\n" +
                "    └── ballerina/log\n";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("list", clientArgs, envVariables, new String[0], new LogLeecher[]{clientLeecher},
                projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test listing dependencies of a project", dependsOnMethods = "testListDependenciesOfBalFile",
            enabled = false)
    public void testListDependenciesOfProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String msg = "main.bal\n" +
                "├── ballerina/io\n" +
                "└── ballerina/http\n" +
                "    ├── ballerina/reflect\n" +
                "    ├── ballerina/auth\n" +
                "    │   ├── ballerina/crypto\n" +
                "    │   ├── ballerina/config\n" +
                "    │   │   └── ballerina/system\n" +
                "    │   ├── ballerina/system\n" +
                "    │   ├── ballerina/time\n" +
                "    │   ├── ballerina/log\n" +
                "    │   ├── ballerina/internal\n" +
                "    │   │   ├── ballerina/time\n" +
                "    │   │   ├── ballerina/file\n" +
                "    │   │   ├── ballerina/log\n" +
                "    │   │   └── ballerina/io\n" +
                "    │   ├── ballerina/cache\n" +
                "    │   │   ├── ballerina/task\n" +
                "    │   │   ├── ballerina/system\n" +
                "    │   │   └── ballerina/time\n" +
                "    │   └── ballerina/runtime\n" +
                "    ├── ballerina/internal\n" +
                "    │   ├── ballerina/time\n" +
                "    │   ├── ballerina/file\n" +
                "    │   ├── ballerina/log\n" +
                "    │   └── ballerina/io\n" +
                "    ├── ballerina/config\n" +
                "    │   └── ballerina/system\n" +
                "    ├── ballerina/math\n" +
                "    ├── ballerina/crypto\n" +
                "    ├── ballerina/mime\n" +
                "    │   ├── ballerina/io\n" +
                "    │   └── ballerina/file\n" +
                "    ├── ballerina/file\n" +
                "    ├── ballerina/time\n" +
                "    ├── ballerina/io\n" +
                "    ├── ballerina/runtime\n" +
                "    ├── ballerina/cache\n" +
                "    │   ├── ballerina/task\n" +
                "    │   ├── ballerina/system\n" +
                "    │   └── ballerina/time\n" +
                "    └── ballerina/log\n" +
                orgName + "/foo:0.0.1\n" +
                "└── ballerina/io\n";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("list", new String[0], envVariables, new String[0], new LogLeecher[]{clientLeecher},
                projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
