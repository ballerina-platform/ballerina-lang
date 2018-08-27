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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.BaseTest;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Testing executing tests in a project using the test command.
 *
 * @since 0.982.0
 */
public class PackagingTestExecutionTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private String[] envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test creating a project")
    public void testInitProject() throws Exception {
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "integrationtests\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                tempProjectDirectory.toString());
    }

    @Test(description = "Test executing tests in a main package", dependsOnMethods = "testInitProject")
    public void testExecutionOfMainPkg() throws Exception {
        String[] clientArgs = {"foo"};
        String msg = "Compiling tests\n" +
                "     integrationtests/foo:0.0.1\n" +
                "\n" +
                "Running tests\n" +
                "     integrationtests/foo:0.0.1\n" +
                "I'm the before suite function!\n" +
                "I'm the before function!\n" +
                "I'm in test function!\n" +
                "I'm the after function!\n" +
                "I'm the after suite function!\n" +
                "\t[pass] testFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a service package", dependsOnMethods = "testInitProject")
    public void testExecutionOfServicePkg() throws Exception {
        String[] clientArgs = {"bar"};
        String msg = "Compiling tests\n" +
                "    integrationtests/bar:0.0.1\n" +
                "\n" +
                "Running tests\n" +
                "    integrationtests/bar:0.0.1\n" +
                "ballerina: started HTTP/WS endpoint 0.0.0.0:9090\n" +
                "I'm the before suite service function!\n" +
                "Do your service Tests!\n" +
                "I'm the after suite service function!\n" +
                "\t[pass] testServiceFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a project", dependsOnMethods = "testInitProject")
    public void testExecutionOfProject() throws Exception {
        String msg = "Compiling tests\n" +
                "    integrationtests/foo:0.0.1\n" +
                "    integrationtests/bar:0.0.1\n" +
                "\n" +
                "Running tests\n" +
                "    integrationtests/foo:0.0.1\n" +
                "I'm the before suite function!\n" +
                "I'm the before function!\n" +
                "I'm in test function!\n" +
                "I'm the after function!\n" +
                "I'm the after suite function!\n" +
                "\t[pass] testFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n" +
                "\n" +
                "    integrationtests/bar:0.0.1\n" +
                "ballerina: started HTTP/WS endpoint 0.0.0.0:9090\n" +
                "I'm the before suite service function!\n" +
                "Do your service Tests!\n" +
                "I'm the after suite service function!\n" +
                "\t[pass] testServiceFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(new String[0], envVariables, "test", tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a ballerina file with main", dependsOnMethods = "testInitProject")
    public void testExecutionMain() throws Exception {
        Path pkgPath = tempProjectDirectory.resolve("foo").resolve("tests");

        // Test ballerina test
        String[] clientArgs = {"main_test.bal"};
        String msg = "Compiling tests\n" +
                "     main_test.bal\n" +
                "\n" +
                "Running tests\n" +
                "     main_test.bal\n" +
                "I'm the before suite function!\n" +
                "I'm the before function!\n" +
                "I'm in test function!\n" +
                "I'm the after function!\n" +
                "I'm the after suite function!\n" +
                "\t[pass] testFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", pkgPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a ballerina file with a service", dependsOnMethods = "testInitProject")
    public void testExecutionService() throws Exception {
        Path pkgPath = tempProjectDirectory.resolve("bar").resolve("tests");

        // Test ballerina test
        String[] clientArgs = {"hello_service_test.bal"};
        String msg = "Compiling tests\n" +
                "    hello_service_test.bal\n" +
                "\n" +
                "Running tests\n" +
                "    hello_service_test.bal\n" +
                "I'm the before suite service function!\n" +
                "Do your service Tests!\n" +
                "I'm the after suite service function!\n" +
                "\t[pass] testServiceFunction\n" +
                "\n" +
                "\t1 passing\n" +
                "\t0 failing\n" +
                "\t0 skipped\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", pkgPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in file without tests", dependsOnMethods = "testInitProject")
    public void testExecutionWithoutTests() throws Exception {
        Path pkgPath = tempProjectDirectory.resolve("foo");

        // Test ballerina test
        String[] clientArgs = {"main.bal"};
        String msg = "Compiling tests\n" +
                "    main.bal\n" +
                "\n" +
                "Running tests\n" +
                "    main.bal\n" +
                "\tNo tests found\n" +
                "\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", pkgPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a package without tests", dependsOnMethods = "testInitProject")
    public void testExecutionEmptyPkg() throws Exception {
        Path pkgPath = tempProjectDirectory.resolve("noTests");
        Files.createDirectories(pkgPath);
        Files.createDirectories(pkgPath.resolve("tests"));

        Files.copy(tempProjectDirectory.resolve("foo").resolve("main.bal"), pkgPath.resolve("main.bal"),
                   StandardCopyOption.REPLACE_EXISTING);

        // Test ballerina test
        String[] clientArgs = {"noTests"};
        String msg = "Compiling tests\n" +
                "    integrationtests/noTests:0.0.1\n" +
                "\n" +
                "Running tests\n" +
                "    integrationtests/noTests:0.0.1\n" +
                "\tNo tests found\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(pkgPath);
    }

    @Test(description = "Test executing tests in a package with test failures", dependsOnMethods = "testInitProject")
    public void testExecutionWithTestFailures() throws Exception {
        Path pkgPath = tempProjectDirectory.resolve("testFailures");
        Files.createDirectories(pkgPath);

        Path testPath = pkgPath.resolve("tests");
        Files.createDirectories(testPath);

        String incorrectContent = "import ballerina/test;\n" +
                                "\n" +
                                "function beforeFunc () {\n" +
                                "    io:println(\"I'm the before function!\");\n" +
                                "}\n" +
                                "@test:Config{\n" +
                                "    before:\"beforeFunc\",\n" +
                                "    after:\"afterFunc\"\n" +
                                "}\n" +
                                "function testFunction () {\n" +
                                "    io:println(\"I'm in test function!\");\n" +
                                "    test:assertTrue(false , msg = \"Failed!\");\n" +
                                "}\n" +
                                "function afterFunc () {\n" +
                                "    io:println(\"I'm the after function!\");\n" +
                                "}\n";
        Files.write(testPath.resolve("main_test.bal"), incorrectContent.getBytes(), StandardOpenOption.CREATE_NEW);

        String[] clientArgs = {"testFailures"};
        String msg = "Compiling tests\n" +
                    "     integrationtests/testFailures:0.0.1\n" +
                    "\n" +
                    "Running tests\n" +
                    "     integrationtests/testFailures:0.0.1\n" +
                    "I'm the before function!\n" +
                    "I'm in test function!\n" +
                    "I'm the after function!\n" +
                    "\t[fail] testFunction:\n" +
                    "\t    error: ballerina/runtime:CallFailedException, message: call failed\n" +
                    "\t    \tat integrationtests/testFailures:0.0.1:testFunction(main_test.bal:12)\n" +
                    "\t    \tcaused by error, message: failed!\n" +
                    "\t    \tat ballerina/test:assertTrue(assert.bal:31)\n" +
                    "\n" +
                    "\t0 passing\n" +
                    "\t1 failing\n" +
                    "\t0 skipped\n";
        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "test", tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(pkgPath);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
