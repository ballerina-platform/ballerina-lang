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
 * Testing executing tests in a project using the test command.
 *
 * @since 0.982.0
 */
public class TestExecutionTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test creating a project")
    public void testInitProject() throws Exception {
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "integrationtests\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                                                tempProjectDirectory.toString());
    }

    @Test(description = "Test executing tests in a main module", dependsOnMethods = "testInitProject")
    public void testExecutionOfMainModule() throws Exception {
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

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a service module", dependsOnMethods = "testInitProject")
    public void testExecutionOfServiceModule() throws Exception {
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

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
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


        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[0], envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a ballerina file with main", dependsOnMethods = "testInitProject")
    public void testExecutionMain() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("foo").resolve("tests");

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

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, modulePath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a ballerina file with a service", dependsOnMethods = "testInitProject")
    public void testExecutionService() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("bar").resolve("tests");

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

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, modulePath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in file without tests", dependsOnMethods = "testInitProject")
    public void testExecutionWithoutTests() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("foo");

        // Test ballerina test
        String[] clientArgs = {"main.bal"};
        String msg = "Compiling tests\n" +
                "    main.bal\n" +
                "\n" +
                "Running tests\n" +
                "    main.bal\n" +
                "\tNo tests found\n" +
                "\n";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, modulePath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test executing tests in a module without tests", dependsOnMethods = "testInitProject")
    public void testExecutionEmptyModule() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("noTests");
        Files.createDirectories(modulePath);
        Files.createDirectories(modulePath.resolve("tests"));

        Files.copy(tempProjectDirectory.resolve("foo").resolve("main.bal"), modulePath.resolve("main.bal"),
                   StandardCopyOption.REPLACE_EXISTING);

        // Test ballerina test
        String[] clientArgs = {"noTests"};
        String msg = "Compiling tests\n" +
                "    integrationtests/noTests:0.0.1\n" +
                "\n" +
                "Running tests\n" +
                "    integrationtests/noTests:0.0.1\n" +
                "\tNo tests found\n";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test executing tests in a module with test failures", dependsOnMethods = "testInitProject")
    public void testExecutionWithTestFailures() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("testFailures");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
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

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", clientArgs, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test executing grouped tests", dependsOnMethods = "testInitProject")
    public void testGroupTestsExecution() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("grouptests");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
        Files.createDirectories(testPath);

        String testContent = "import ballerina/test;\n" +
                                "import ballerina/io;\n" +
                                "@test:Config {\n" +
                                "    groups: [\"g1\"]\n" +
                                "}\n" +
                                "function testFunction1() {\n" +
                                "    io:println(\"I'm in test belonging to g1!\");\n" +
                                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                                "}\n" +
                                "@test:Config {\n" +
                                "    groups: [\"g1\", \"g2\"]\n" +
                                "}\n" +
                                "function testFunction2() {\n" +
                                "    io:println(\"I'm in test belonging to g1 and g2!\");\n" +
                                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                                "}\n" +
                                "@test:Config\n" +
                                "function testFunction3() {\n" +
                                "    io:println(\"I'm the ungrouped test\");\n" +
                                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                                "}\n";
        Files.write(testPath.resolve("main_test.bal"), testContent.getBytes(), StandardOpenOption.CREATE_NEW);

        // --groups g1
        String msg = "Compiling tests\n" +
                    "    main_test.bal\n" +
                    "Running tests\n" +
                    "    main_test.bal\n" +
                    "I'm in test belonging to g1 and g2!\n" +
                    "I'm in test belonging to g1!\n" +
                    "\t  [pass] testFunction2\n" +
                    "\t  [pass] testFunction1\n" +
                    "\t  2 passing\n" +
                    "\t  0 failing\n" +
                    "\t  0 skipped";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]  {"--groups" , "g1", "main_test.bal"}, envVariables,
                new String[0], new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);

        // --disable-groups g1
        msg = "Compiling tests\n" +
                "    main_test.bal\n" +
                "Running tests\n" +
                "    main_test.bal\n" +
                "I'm the ungrouped test\n" +
                "\t  [pass] testFunction3\n" +
                "\t  1 passing\n" +
                "\t  0 failing\n" +
                "\t  0 skipped";

        clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]  {"--disable-groups" , "g1" , "main_test.bal"}, envVariables,
                new String[0], new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);

        // --disable-groups g2
        msg = "Compiling tests\n" +
                "    main_test.bal\n" +
                "Running tests\n" +
                "    main_test.bal\n" +
                "I'm the ungrouped test\n" +
                "I'm in test belonging to g1!\n" +
                "\t  [pass] testFunction3\n" +
                "\t  [pass] testFunction1\n" +
                "\t  2 passing\n" +
                "\t  0 failing\n" +
                "\t  0 skipped";

        clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[] {"--disable-groups" , "g2", "main_test.bal"}, envVariables,
                          new String[0], new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test executing multiple grouped tests", dependsOnMethods = "testInitProject")
    public void testMultipleGroupTestsExecution() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("grouptests");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
        Files.createDirectories(testPath);

        String testContent = "import ballerina/test;\n" +
                "import ballerina/io;\n" +
                "@test:Config {\n" +
                "    groups: [\"g1\"]\n" +
                "}\n" +
                "function testFunction1() {\n" +
                "    io:println(\"I'm in test belonging to g1!\");\n" +
                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                "}\n" +
                "@test:Config {\n" +
                "    groups: [\"g1\", \"g2\"]\n" +
                "}\n" +
                "function testFunction2() {\n" +
                "    io:println(\"I'm in test belonging to g1 and g2!\");\n" +
                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                "}\n" +
                "@test:Config\n" +
                "function testFunction3() {\n" +
                "    io:println(\"I'm the ungrouped test\");\n" +
                "    test:assertTrue(true, msg = \"Failed!\");\n" +
                "}\n";
        Files.write(testPath.resolve("main_test.bal"), testContent.getBytes(), StandardOpenOption.CREATE_NEW);

        // --groups g1,g2
        String msg = "Compiling tests\n" +
                "    main_test.bal\n" +
                "Running tests\n" +
                "    main_test.bal\n" +
                "I'm in test belonging to g1 and g2!\n" +
                "I'm in test belonging to g1!\n" +
                "\t  [pass] testFunction2\n" +
                "\t  [pass] testFunction1\n" +
                "\t  2 passing\n" +
                "\t  0 failing\n" +
                "\t  0 skipped";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]  {"--groups" , "g1,g2", "main_test.bal"}, envVariables,
                          new String[0], new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);

        // --disable-groups g1,g2
        msg = "Compiling tests\n" +
                "    main_test.bal\n" +
                "Running tests\n" +
                "    main_test.bal\n" +
                "I'm the ungrouped test\n" +
                "\t  [pass] testFunction3\n" +
                "\t  1 passing\n" +
                "\t  0 failing\n" +
                "\t  0 skipped";

        clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[] {"--disable-groups" , "g1,g2" , "main_test.bal"}, envVariables,
                          new String[0], new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);

        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test executing data driven tests", dependsOnMethods = "testInitProject")
    public void testDataDrivenTestExecution() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("dataDriven");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
        Files.createDirectories(testPath);

        String testContent = "import ballerina/test;\n" +
                "import ballerina/io;\n" +
                "@test:Config {\n" +
                "    dataProvider: \"ValueProvider\"\n" +
                "}\n" +
                "function testAddingValues(string fValue, string sValue, string result) {    " +
                "    int value1 = check <int>fValue;\n" +
                "    int value2 = check <int>sValue;\n" +
                "    int result1 = check <int>result;\n" +
                "    io:println(\"Input : [\" + fValue + \",\" + sValue + \",\" + result + \"]\");\n" +
                "    test:assertEquals(value1 + value2, result1, msg = \"Incorrect Sum\");\n" +
                "}\n" +
                "function ValueProvider() returns (string[][]) {\n" +
                "    return [[\"1\", \"2\", \"3\"], [\"10\", \"20\", \"30\"], [\"5\", \"6\", \"11\"]];\n" +
                "}\n" +
                "@test:Config {\n" +
                "    dataProvider: \"jsonDataProvider\"\n" +
                "}\n" +
                "function testJsonObjects(json fValue, json sValue, json result) {\n" +
                "    json a = { \"a\": \"a\" };\n" +
                "    json b = { \"b\": \"b\" };\n" +
                "    json c = { \"c\": \"c\" };\n" +
                "    test:assertEquals(fValue, a, msg = \"json data provider failed\");\n" +
                "    test:assertEquals(sValue, b, msg = \"json data provider failed\");\n" +
                "    test:assertEquals(result, c, msg = \"json data provider failed\");\n" +
                "}\n" +
                "function jsonDataProvider() returns (json[][]) {\n" +
                "    return [[{ \"a\": \"a\" }, { \"b\": \"b\" }, { \"c\": \"c\" }]];\n" +
                "}\n";
        Files.write(testPath.resolve("main_test.bal"), testContent.getBytes(), StandardOpenOption.CREATE_NEW);

        String msg = "Compiling tests\n" +
                "    main_test.bal\n" +
                "\n" +
                "Running tests\n" +
                "    main_test.bal\n" +
                "Input : [1,2,3]\n" +
                "Input : [10,20,30]\n" +
                "Input : [5,6,11]\n" +
                "\t  [pass] testJsonObjects\n" +
                "\t  [pass] testAddingValues\n" +
                "\t  [pass] testAddingValues\n" +
                "\t  [pass] testAddingValues\n" +
                "\t  4 passing\n" +
                "\t  0 failing\n" +
                "\t  0 skipped";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"main_test.bal"}, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);
        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test execution order of tests", dependsOnMethods = "testInitProject")
    public void testExecutionOrder() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("executionOrder");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
        Files.createDirectories(testPath);

        String testContent = "import ballerina/test;\n" +
                            "import ballerina/io;\n" +
                            "@test:Config {\n" +
                            "    dependsOn: [\"testFunction3\"]\n" +
                            "}\n" +
                            "function testFunction1() {\n" +
                            "    io:println(\"I'm in test function 1!\");\n" +
                            "    test:assertTrue(true, msg = \"Failed!\");\n" +
                            "}\n" +
                            "@test:Config {\n" +
                            "    dependsOn: [\"testFunction1\"]\n" +
                            "}\n" +
                            "function testFunction2() {\n" +
                            "    io:println(\"I'm in test function 2!\");\n" +
                            "    test:assertTrue(true, msg = \"Failed!\");\n" +
                            "}\n" +
                            "@test:Config\n" +
                            "function testFunction3() {\n" +
                            "    io:println(\"I'm in test function 3!\");\n" +
                            "    test:assertTrue(true, msg = \"Failed!\");\n" +
                            "}\n";
        Files.write(testPath.resolve("main_test.bal"), testContent.getBytes(), StandardOpenOption.CREATE_NEW);

        String msg = "Compiling tests\n" +
                    "    main_test.bal\n" +
                    "\n" +
                    "Running tests\n" +
                    "    main_test.bal\n" +
                    "I'm in test function 3!\n" +
                    "I'm in test function 1!\n" +
                    "I'm in test function 2!\n" +
                    "\t  [pass] testFunction3\n" +
                    "\t  [pass] testFunction1\n" +
                    "\t  [pass] testFunction2\n" +
                    "\t  3 passing\n" +
                    "\t  0 failing\n" +
                    "\t  0 skipped";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"main_test.bal"}, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);
        PackagingTestUtils.deleteFiles(modulePath);
    }

    @Test(description = "Test execution of function mock tests", dependsOnMethods = "testInitProject")
    public void testFunctionMockTests() throws Exception {
        Path modulePath = tempProjectDirectory.resolve("functionMock");
        Files.createDirectories(modulePath);

        Path testPath = modulePath.resolve("tests");
        Files.createDirectories(testPath);

        String testContent = "import ballerina/test;\n" +
                            "import ballerina/io;\n" +
                            "@test:Mock {\n" +
                            "    moduleName: \".\",\n" +
                            "    functionName: \"intAdd\"\n" +
                            "}\n" +
                            "public function mockIntAdd(int a, int b) returns int {\n" +
                            "    io:println(\"I'm the mock function!\");\n" +
                            "    return (a - b);\n" +
                            "}\n" +
                            "@test:Config\n" +
                            "function testAssertIntEquals() {\n" +
                            "    int answer = 0;\n" +
                            "    answer = intAdd(5, 3);\n" +
                            "    io:println(\"Function mocking test\");\n" +
                            "    test:assertEquals(answer, 2, msg = \"function mocking failed\");\n" +
                            "}\n" +
                            "public function intAdd(int a, int b) returns int {\n" +
                            "    return (a + b);\n" +
                            "}\n";
        Files.write(testPath.resolve("main_test.bal"), testContent.getBytes(), StandardOpenOption.CREATE_NEW);

        String msg = "Compiling tests\n" +
                    "    main_test.bal\n" +
                    "\n" +
                    "Running tests\n" +
                    "    main_test.bal\n" +
                    "I'm the mock function!\n" +
                    "Function mocking test\n" +
                    "\t  [pass] testAssertIntEquals\n" +
                    "\t  1 passing\n" +
                    "\t  0 failing\n" +
                    "\t  0 skipped";

        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"main_test.bal"}, envVariables, new String[0],
                new LogLeecher[]{clientLeecher}, testPath.toString());
        clientLeecher.waitForText(3000);
        PackagingTestUtils.deleteFiles(modulePath);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
