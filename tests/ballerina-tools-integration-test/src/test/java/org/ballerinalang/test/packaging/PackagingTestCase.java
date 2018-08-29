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
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
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
 *
 * @since 0.981.0
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
    }

    @Test(description = "Test pushing a package to central", dependsOnMethods = "testInitProject")
    public void testPush() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {"--y", packageName};
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
        String[] clientArgs = {"--y", packageName};
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

    @Test(description = "Test push all packages in project to central")
    public void testPushAllPackages() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("pushAllPackageTest");
        Files.createDirectories(projectPath);

        String firstPackage = "firstTestPkg" + PackagingTestUtils.randomPackageName(10);
        String secondPackage = "secondTestPkg" + PackagingTestUtils.randomPackageName(10);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", firstPackage + "\n", "m\n", secondPackage + "\n", "f\n"};
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
                                                projectPath.toString());

        // Reset the server log reader
        serverInstance.resetServerLogReader();

        String msg = orgName + "/" + firstPackage + ":0.0.1 [project repo -> central]\n" +
                orgName + "/" + secondPackage + ":0.0.1 [project repo -> central]";

        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(new String[]{"--y"}, envVariables, "push", projectPath.toString());
        clientLeecher.waitForText(5000);
    }

    @Test(description = "Test ballerina version")
    public void testBallerinaVersion() throws Exception {
        // Reset the server log reader
        serverInstance.resetServerLogReader();

        LogLeecher clientLeecher = new LogLeecher(RepoUtils.getBallerinaVersion());
        serverInstance.addLogLeecher(clientLeecher);
        serverInstance.runMain(new String[0], envVariables, "version", tempProjectDirectory.toString());
    }

    @Test(description = "Test pushing a package with syntax errors in source", dependsOnMethods = "testInitProject")
    public void testPushWithSyntaxErrorsInSource() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {"--y", packageName};

        // Add a new bal source file with syntax errors
        writeInvalidSource();

        // Remove the already created artifact
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, packageName, "0.0.1");
        Files.deleteIfExists(projectPath.resolve(".ballerina").resolve(dirPath).resolve(packageName + ".zip"));

        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());

        Assert.assertFalse(Files.exists(tempHomeDirectory.resolve(".ballerina").resolve(dirPath)
                                                         .resolve(packageName + ".zip")));

        // Remove the newly added file
        Files.deleteIfExists(projectPath.resolve(packageName).resolve("main_errors.bal"));
    }

    @Test(description = "Test pushing a package with test failures", dependsOnMethods = "testInitProject")
    public void testPushWithTestFailures() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {"--y", packageName};

        // Add a new test bal with test failures
        writeInvalidTestContent();

        // Remove the already created artifact
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, packageName, "0.0.1");
        Files.deleteIfExists(projectPath.resolve(".ballerina").resolve(dirPath).resolve(packageName + ".zip"));

        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());

        Assert.assertFalse(Files.exists(tempHomeDirectory.resolve(".ballerina").resolve(dirPath)
                                                         .resolve(packageName + ".zip")));

        // Remove the newly added file
        Files.deleteIfExists(projectPath.resolve(packageName).resolve("tests")
                                                 .resolve("main_test_errors.bal"));
    }

    @Test(description = "Test installing a package with syntax errors in source", dependsOnMethods = "testInitProject")
    public void testInstallWithSyntaxErrorsInSource() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {"--y", packageName};

        // Add a new bal source file with syntax errors
        writeInvalidSource();

        // Remove the already created artifact
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, packageName, "0.0.1");
        Files.deleteIfExists(projectPath.resolve(".ballerina").resolve(dirPath).resolve(packageName + ".zip"));

        serverInstance.runMain(clientArgs, envVariables, "install", projectPath.toString());

        Assert.assertFalse(Files.exists(tempHomeDirectory.resolve(".ballerina").resolve(dirPath)
                                                         .resolve(packageName + ".zip")));

        // Remove the newly added file
        Files.deleteIfExists(projectPath.resolve(packageName).resolve("main_errors.bal"));
    }

    @Test(description = "Test installing a package with test failures", dependsOnMethods = "testInitProject")
    public void testInstallWithTestFailures() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {"--y", packageName};

        // Add a new test bal with test failures
        writeInvalidTestContent();

        // Remove the already created artifact
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, packageName, "0.0.1");
        Files.deleteIfExists(projectPath.resolve(".ballerina").resolve(dirPath).resolve(packageName + ".zip"));

        serverInstance.runMain(clientArgs, envVariables, "install", projectPath.toString());

        Assert.assertFalse(Files.exists(tempHomeDirectory.resolve(".ballerina").resolve(dirPath)
                                                         .resolve(packageName + ".zip")));

        // Remove the newly added file
        Files.deleteIfExists(projectPath.resolve(packageName).resolve("tests")
                                                 .resolve("main_test_errors.bal"));
    }

    /**
     * Write invalid source with compilation errors.
     * @throws IOException
     */
    private void writeInvalidSource() throws IOException {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String incorrectContent = "import ballerina/io;\n\n function hello() {\n    io.println(\"Hello World!\");\n" +
                "}\n";
        Files.write(projectPath.resolve(packageName).resolve("main_errors.bal"), incorrectContent.getBytes(),
                    StandardOpenOption.CREATE_NEW);
    }

    /**
     * Write invalid test content to file.
     * @throws IOException
     */
    private void writeInvalidTestContent() throws IOException {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        // Add a new bal test source file with syntax errors
        String incorrectContent =  "import ballerina/test;\n" +
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
        Files.write(projectPath.resolve(packageName).resolve("tests").resolve("main_test_errors.bal"),
                    incorrectContent.getBytes(), StandardOpenOption.CREATE_NEW);
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
        PackagingTestsUtils.deleteFiles(tempHomeDirectory);
        PackagingTestsUtils.deleteFiles(tempProjectDirectory);
    }
}
