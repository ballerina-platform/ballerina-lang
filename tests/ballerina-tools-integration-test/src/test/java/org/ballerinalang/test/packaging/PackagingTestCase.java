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
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.ballerinalang.test.utils.TestUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;

/**
 * Testing pushing, pulling, searching a package from central and installing package to home repository.
 *
 * @since 0.981.0
 */
@Test(enabled = false)
public class PackagingTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private String datePushed;
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        moduleName = moduleName + TestUtils.randomModuleName(10);
        TestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(TestUtils.getEnvVariables());
    }

    @Test(description = "Test init a ballerina project to be pushed to central")
    public void testInitProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        Files.createDirectories(projectPath);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", moduleName + "\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[]{},
                projectPath.toString());
    }

    @Test(enabled = false, description = "Test pushing a package to central",
            dependsOnMethods = "testInitProject")
    public void testPush() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        // Get date and time of the module pushed.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-EE");
        datePushed = dtf.format(LocalDateTime.now());

        // First try to push with the --no-build flag
        String firstMsg = "error: Couldn't locate the module artifact to be pushed. Run 'ballerina push' " +
                "without the --no-build flag";
        LogLeecher clientLeecher = new LogLeecher(firstMsg, LeecherType.ERROR);
        balClient.runMain("push", new String[]{moduleName, "--no-build"}, envVariables, new String[]{},
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(2000);
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, moduleName, "0.0.1");
        Assert.assertTrue(Files.notExists(projectPath.resolve(dirPath)));
        Assert.assertTrue(Files.notExists(projectPath.resolve(dirPath).resolve(moduleName + ".zip")));

        // Then try to push without the flag so it builds the artifact
        String secondMsg = orgName + "/" + moduleName + ":0.0.1 [project repo -> central]";
        clientLeecher = new LogLeecher(secondMsg);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{},
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(5000);
    }

    @Test(description = "Test pushing a package to the home repository (installing a package)",
            dependsOnMethods = "testPush")
    public void testInstall() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");
        String[] clientArgs = {moduleName};

        balClient.runMain("install", clientArgs, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());

        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, moduleName, "0.0.1");
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(moduleName + ".zip")));
    }

    @Test(description = "Test pulling a package from central", dependsOnMethods = "testPush")
    public void testPull() {
        Path dirPath = Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                 ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME,
                                 orgName, moduleName, "0.0.1");

        given().with().pollInterval(Duration.TEN_SECONDS).and()
               .with().pollDelay(Duration.FIVE_SECONDS)
               .await().atMost(60, SECONDS).until(() -> {
            String[] clientArgs = {orgName + "/" + moduleName + ":0.0.1"};
            balClient.runMain("pull", clientArgs, envVariables, new String[]{},
                    new LogLeecher[]{}, balServer.getServerHome());
            return Files.exists(tempHomeDirectory.resolve(dirPath).resolve(moduleName + ".zip"));
        });

        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve(moduleName + ".zip")));
    }

    @Test(description = "Test searching a package from central", dependsOnMethods = "testPush")
    public void testSearch() throws BallerinaTestException {
        String actualMsg = balClient.runMainAndReadStdOut("search", new String[]{moduleName}, envVariables,
                balServer.getServerHome(), false);

        // Check if the search results contains the following.
        Assert.assertTrue(actualMsg.contains("Ballerina Central"));
        Assert.assertTrue(actualMsg.contains("NAME"));
        Assert.assertTrue(actualMsg.contains("DESCRIPTION"));
        Assert.assertTrue(actualMsg.contains("DATE"));
        Assert.assertTrue(actualMsg.contains("VERSION"));
        Assert.assertTrue(actualMsg.contains("Prints \"hello world\""));
        Assert.assertTrue(actualMsg.contains(datePushed));
        Assert.assertTrue(actualMsg.contains("0.0.1"));
    }

    @Test(description = "Test push all packages in project to central")
    public void testPushAllPackages() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("pushAllPackageTest");
        Files.createDirectories(projectPath);

        String firstPackage = "firstTestPkg" + TestUtils.randomModuleName(10);
        String secondPackage = "secondTestPkg" + TestUtils.randomModuleName(10);

        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", orgName + "\n", "\n", "m\n", firstPackage + "\n", "m\n", secondPackage + "\n", "f\n"};

        balClient.runMain("init", clientArgsForInit, envVariables, options,
                new LogLeecher[]{}, projectPath.toString());

        LogLeecher clientLeecherOne = new LogLeecher(orgName + "/" + firstPackage + ":0.0.1 [project repo -> central]");
        LogLeecher clientLeecherTwo = new LogLeecher(orgName + "/" + secondPackage +
                                                             ":0.0.1 [project repo -> central]");
        balClient.runMain("push", new String[0], envVariables, new String[]{},
                new LogLeecher[]{clientLeecherOne, clientLeecherTwo}, projectPath.toString());
        clientLeecherOne.waitForText(5000);
        clientLeecherTwo.waitForText(5000);
    }

    @Test(description = "Test ballerina version")
    public void testBallerinaVersion() throws Exception {
        LogLeecher clientLeecher = new LogLeecher(RepoUtils.getBallerinaVersion());
        balClient.runMain("version", new String[0], envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
    }

    @Test(description = "Test uninstalling a package from the home repository which was installed locally",
            dependsOnMethods = "testInstall")
    public void testUninstallFromHomeRepo() throws Exception {
        String fullPkgPath = orgName + "/" + moduleName + ":0.0.1";
        String[] clientArgs = {fullPkgPath};

        LogLeecher clientLeecher = new LogLeecher(fullPkgPath + " successfully uninstalled");
        balClient.runMain("uninstall", clientArgs, envVariables, new String[]{}, new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
        clientLeecher.waitForText(2000);

        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName, moduleName, "0.0.1");
        Assert.assertTrue(Files.notExists(tempHomeDirectory.resolve(dirPath).resolve(moduleName + ".zip")));
        Assert.assertTrue(Files.notExists(tempHomeDirectory.resolve(dirPath)));
    }

    @Test(description = "Test uninstalling a package from the home repository which was pulled from central",
            dependsOnMethods = { "testPull" , "testUninstallFromHomeRepo" })
    public void testUninstallFromCaches() throws Exception {
        String fullPkgPath = orgName + "/" + moduleName + ":0.0.1";
        String[] clientArgs = {fullPkgPath};

        LogLeecher clientLeecher = new LogLeecher(fullPkgPath + " successfully uninstalled");
        balClient.runMain("uninstall", clientArgs, envVariables, new String[]{}, new LogLeecher[]{clientLeecher},
                          tempProjectDirectory.toString());
        clientLeecher.waitForText(2000);

        Path dirPath = Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                 ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME, orgName, moduleName, "0.0.1");
        Assert.assertTrue(Files.notExists(tempHomeDirectory.resolve(dirPath).resolve(moduleName + ".zip")));
        Assert.assertTrue(Files.notExists(tempHomeDirectory.resolve(dirPath)));
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put("BALLERINA_DEV_PREPROD_CENTRAL", "true");
        return envVariables;
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempHomeDirectory);
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
