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

import org.awaitility.Duration;
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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.given;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Testing pushing, pulling, searching a package from central and installing package to home repository.
 */
public class PackagingTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String packageName = "test";
    private String datePushed;
    private String orgName = "integrationtests";
    private String[] envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        createSettingToml();
        packageName = packageName + PackagingTestUtils.randomPackageName(10);
        envVariables = addEnvVariables(PackagingTestUtils.getEnvVariables());
    }

    @Test(description = "Test init a ballerina project to be pushed to central")
    public void testInitProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", packageName + "\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());
        Path tomlPath = projectPath.resolve("Ballerina.toml");
        Assert.assertTrue(Files.exists(projectPath.resolve(packageName).resolve("main.bal")));
        Assert.assertTrue(Files.exists(tomlPath));
        Assert.assertTrue(Files.exists(projectPath.resolve(packageName).resolve("tests").resolve("main_test.bal")));
    }

    @Test(description = "Test pushing a package to central", dependsOnMethods = "testInitProject")
    public void testPush() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {packageName};
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-EE");
        datePushed = dtf.format(LocalDateTime.now());

        String msg = orgName + "/" + packageName + ":0.0.1 [project repo -> central]";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
        clientLeecher.waitForText(5000);
    }

    @Test(description = "Test pushing a package to the home repository (installing a package)",
            dependsOnMethods = "testInitProject")
    public void testInstall() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {packageName};
        serverInstance.runMain(clientArgs, envVariables, "install", projectPath.toString());

        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, packageName, "0.0.1");
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip")));
    }

    @Test(description = "Test pulling a package from central", dependsOnMethods = "testPush")
    public void testPull() throws Exception {
        Path dirPath = Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                 ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME,
                                 orgName, packageName, "0.0.1");

        given().with().pollInterval(Duration.TEN_SECONDS).and()
               .with().pollDelay(Duration.FIVE_SECONDS)
               .await().atMost(60, SECONDS).until(() -> {
            String[] clientArgs = {orgName + "/" + packageName + ":0.0.1"};
            serverInstance.runMain(clientArgs, envVariables, "pull");
            return Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip"));
        });

        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(packageName + ".zip")));
    }

    @Test(description = "Test searching a package from central", dependsOnMethods = "testPush")
    public void testSearch() throws BallerinaTestException, IOException {
        String[] clientArgs = {packageName};
        String msg = "Ballerina Central\n" +
                "=================\n" +
                "\n" +
                "|NAME                                                  | DESCRIPTION                                " +
                "                                       | AUTHOR         | DATE           | VERSION |\n" +
                "|------------------------------------------------------| -------------------------------------------" +
                "---------------------------------------| ---------------| ---------------| --------|\n" +
                "|" + orgName + "/" + packageName + "                             | Prints \"hello world\" to " +
                "command line output" +
                "                                       |                | " + datePushed + " | 0.0.1   |\n";

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "search");
        clientLeecher.waitForText(3000);
    }


    @Test(description = "Test listing dependencies of a package", dependsOnMethods = "testInitProject")
    public void testListDependenciesOfPackage() throws Exception {
        String[] clientArgs = {packageName};
        Path projectPath = tempProjectDirectory.resolve("initProject");

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        String msg = orgName + "/" + packageName + ":0.0.1\n" +
                "└── ballerina/io";
        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "list", projectPath.toString());
        clientLeecher.waitForText(3000);

    }

    @Test(description = "Test listing dependencies of a single bal file", dependsOnMethods = "testInitProject")
    public void testListDependenciesOfBalFile() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String content = "import ballerina/io;\n import ballerina/http; \n \n function main(string... args) {\n    " +
                "io:println(\"Hello World!\"); \n }\n";
        Files.write(projectPath.resolve("main.bal"), content.getBytes(), StandardOpenOption.CREATE_NEW);
        String[] clientArgs = {"main.bal"};

        // Reset the server log reader
        serverInstance.resetServerLogReader();

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
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(clientArgs, envVariables, "list", projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    @Test(description = "Test listing dependencies of a project", dependsOnMethods = "testListDependenciesOfBalFile")
    public void testListDependenciesOfProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        // Reset the server log reader
        serverInstance.resetServerLogReader();

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
                orgName + "/" + packageName + ":0.0.1\n" +
                "└── ballerina/io\n";

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(new String[0], envVariables, "list", projectPath.toString());
        clientLeecher.waitForText(3000);
    }


    @Test(description = "Test ballerina version")
    public void testBallerinaVersion() throws Exception {
        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(RepoUtils.getBallerinaVersion());
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(new String[0], envVariables, "version", tempProjectDirectory.toString());
    }

    @Test(description = "Test doc generation for package", dependsOnMethods = "testInitProject")
    public void testDocGenerationForPackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String[] clientArgs = {packageName};
        serverInstance.runMain(clientArgs, envVariables, "doc", projectPath.toString());

        Path apiDocsGenerated = projectPath.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve(packageName + ".html")));
    }

    @Test(description = "Test doc generation for single bal file", dependsOnMethods = "testInitProject")
    public void testDocGenerationForSingleBalFile() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        String[] clientArgs = {"main.bal"};
        serverInstance.runMain(clientArgs, envVariables, "doc", projectPath.resolve(packageName).toString());

        Path apiDocsGenerated = projectPath.resolve(packageName).resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("main.bal.html")));
    }

    @Test(description = "Test doc generation for a project", dependsOnMethods = "testInitWithMainServiceInDiffPackage")
    public void testDocGenerationForProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("secondTestWithPackages");

        serverInstance.runMain(new String[0], envVariables, "doc", projectPath.toString());

        Path apiDocsGenerated = projectPath.resolve("target").resolve("api-docs");
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("index.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("package-list.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("foo.html")));
        Assert.assertTrue(Files.exists(apiDocsGenerated.resolve("bar.html")));
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
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private String[] addEnvVariables(String[] envVariables) {
        String[] newEnvVariables = new String[]{
                ProjectDirConstants.HOME_REPO_ENV_KEY + "=" + tempHomeDirectory.toString(),
                "BALLERINA_DEV_STAGE_CENTRAL" + "=" + "true"
        };
        return Stream.of(envVariables, newEnvVariables).flatMap(Stream::of).toArray(String[]::new);
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
