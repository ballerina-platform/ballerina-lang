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
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Testing initializing a project using the init command.
 *
 * @since 0.982.0
 */
public class PackagingInitTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String packageName = "test";
    private String orgName = "integrationtests";
    private String[] envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        createSettingToml();
        packageName = packageName + PackagingTestUtils.randomPackageName(10);
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test creating a project with a main in a package")
    public void testInitWithMainInPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));

        // Test ballerina run
        runMainFunction(projectPath, "foo");

        // Test ballerina run with balx
        runMainFunction(projectPath, generatedBalx.toString());
    }

    @Test(description = "Test creating a project with a service in a package")
    public void testInitWithServiceInPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesService");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "s\n", "foo\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Path serviceBalPath = projectPath.resolve("foo").resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("hello_service_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));

        // Test ballerina run
        runService(projectPath.resolve("foo").resolve("hello_service.bal"));

        // Test ballerina run with balx
        runService(generatedBalx);
    }

    @Test(description = "Test creating a project with a service and main in different packages")
    public void testInitWithMainServiceInDiffPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("secondTestWithPackages");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "s\n", "bar\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Path serviceBalPath = projectPath.resolve("bar").resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("bar").resolve("tests").resolve("hello_service_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("bar.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("bar").resolve("0.0.1").resolve("bar.zip")));
        // Test ballerina run
        runMainFunction(projectPath, "foo");
        runService(serviceBalPath);

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("foo.balx").toString());
        runService(projectPath.resolve("target").resolve("bar.balx"));
    }

    @Test(description = "Test creating a project without going to interactive mode")
    public void testInitWithoutGoingToInteractiveMode() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testWithoutPackage");
        Files.createDirectories(projectPath);

        serverInstance.runMainWithClientOptions(new String[0], new String[0], envVariables, "init",
                                                projectPath.toString());

        Path serviceBalPath = projectPath.resolve("hello_service.bal");
        Assert.assertTrue(Files.exists(serviceBalPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("hello_service.balx");
        Assert.assertTrue(Files.exists(generatedBalx));

        // Test ballerina run
        runService(serviceBalPath);

        // Test ballerina run with balx
        runService(generatedBalx);
    }

    @Test(description = "Test creating a project with a main without a package")
    public void testInitWithoutPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testWithoutPackageForMain");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("main.balx");
        Assert.assertTrue(Files.exists(generatedBalx));

        // Test ballerina run
        runMainFunction(projectPath, projectPath.resolve("main.bal").toString());

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("main.balx").toString());
    }

    @Test(description = "Test running init without doing any changes on an already existing project",
            dependsOnMethods = "testInitWithMainInPackage")
    public void testInitOnExistingProject() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");
        serverInstance.runMainWithClientOptions(new String[0], new String[0], envVariables, "init",
                                                projectPath.toString());

        Path packagePath = projectPath.resolve("foo");
        Assert.assertTrue(Files.exists(packagePath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(packagePath.resolve("tests").resolve("main_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        // Test ballerina run
        runMainFunction(projectPath, "foo");

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("foo.balx").toString());
    }

    @Test(description = "Test running init on an already existing project and create a new package",
            dependsOnMethods = "testInitWithMainInPackage")
    public void testInitOnExistingProjectWithNewPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("firstTestWithPackagesMain");

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "newpkg\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("newpkg").resolve("tests").resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foo").resolve("tests").resolve("main_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("newpkg.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("newpkg").resolve("0.0.1").resolve("newpkg.zip")));


        // Test ballerina run on the new package
        runMainFunction(projectPath, "newpkg");

        // Test ballerina run with balx
        runMainFunction(projectPath, projectPath.resolve("target").resolve("newpkg.balx").toString());
    }

    @Test(description = "Test creating a project with invalid options")
    public void testInitWithInvalidOptions() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testsWithoutPackage");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "123\n", "jkl\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
    }

    @Test(description = "Test creating a project with invalid package")
    public void testInitWithInvalidPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("invalidTestWithPackage");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo-bar\n", "foo bar package\n", "foo$bar\n",
                "foobar\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        Assert.assertTrue(Files.exists(projectPath.resolve("foobar").resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("foobar").resolve("tests").resolve("main_test.bal")));

        // Test ballerina build
        serverInstance.runMain(new String[0], envVariables, "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foobar.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                                                  .resolve("foobar").resolve("0.0.1").resolve("foobar.zip")));

        // Test ballerina run
        runMainFunction(projectPath, "foobar");

        // Test ballerina run with balx
        runMainFunction(projectPath, generatedBalx.toString());
    }

    @Test(description = "Test building a package in a project")
    public void testBuildPackage() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("testBuildPackage");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", "foo\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        // Test ballerina build
        serverInstance.runMain(new String[]{"foo"}, envVariables, "build", projectPath.toString());
        Assert.assertTrue(Files.exists(projectPath.resolve("target").resolve("foo.balx")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".ballerina").resolve("repo").resolve("integrationtests")
                                                  .resolve("foo").resolve("0.0.1").resolve("foo.zip")));
    }

    /**
     * Run and test main function in project.
     *
     * @param projectPath path of the project
     * @param pkg         package name or balx file path
     * @throws BallerinaTestException
     */
    private void runMainFunction(Path projectPath, String pkg)
            throws BallerinaTestException {
        String[] clientArgsForRun = {"--sourceroot", projectPath.toString(), pkg};
        serverInstance.addLogLeecher(new LogLeecher("Hello World!"));
        serverInstance.runMain(clientArgsForRun, envVariables, "run");
    }

    /**
     * Run and test service in project.
     *
     * @param serviceBalPath path of the service bal file
     * @throws BallerinaTestException
     * @throws IOException
     */
    private void runService(Path serviceBalPath) throws BallerinaTestException, IOException {
        serverInstance.startBallerinaServer(serviceBalPath.toString());
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp("hello/sayHello"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        serverInstance.stopServer();
    }

    /**
     * Create Settings.toml inside the home repository.
     *
     * @throws IOException i/o exception when writing to file
     */
    private void createSettingToml() throws IOException {
        Path tomlFilePath = tempHomeDirectory.resolve("Settings.toml");
        String content = "[central]\n accesstoken = \"0f647e67-857d-32e8-a679-bd3c1c3a7eb2\"";
        Files.write(tomlFilePath, content.getBytes(), StandardOpenOption.CREATE);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempHomeDirectory);
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
