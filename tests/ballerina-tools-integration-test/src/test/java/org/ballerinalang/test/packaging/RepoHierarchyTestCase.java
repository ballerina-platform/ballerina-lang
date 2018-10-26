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

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Integration test cases for repository hierarchy.
 */
public class RepoHierarchyTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private static final String ORG_NAME = "integrationtests";
    private static final String VERSION = "0.0.1";
    private Map<String, String> envVariables;
    
    @BeforeClass()
    public void setUp() throws IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-repo-hierarchy-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-repo-hierarchy-project-");
        
        String projectPath = (new File("src/test/resources/repohierarchy")).getAbsolutePath();
        FileUtils.copyDirectory(Paths.get(projectPath).toFile(), tempProjectDirectory.toFile());
        envVariables = addEnvVariables(PackagingTestUtils.getEnvVariables());
    }
    
    /**
     * Create the toml and .ballerina file using init command.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test initializing the project")
    public void testInitProject() throws BallerinaTestException {
        String outputMessage = "Ballerina project initialized";
        LogLeecher clientLeecher = new LogLeecher(outputMessage);
        
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", ORG_NAME + "\n", VERSION + "\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options,
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
    }
    
    /**
     * Installing module 'a' to home repository.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test installing module 'a'", dependsOnMethods = "testInitProject")
    public void testInstallingModuleA() throws BallerinaTestException {
        String[] clientArgs = {"a"};
    
        balClient.runMain("install", clientArgs, envVariables, new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
    
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "a", VERSION);
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve("a" + ".zip")));
    }
    
    /**
     * Installing module 'b' to home repository.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test installing module 'b'", dependsOnMethods = "testInstallingModuleA")
    public void testInstallingModuleB() throws BallerinaTestException {
        String[] clientArgs = {"b"};
        
        balClient.runMain("install", clientArgs, envVariables, new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
        
        Path dirPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "b", VERSION);
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(dirPath).resolve("b" + ".zip")));
    }
    
    /**
     * Run main function of module 'x'. This should print 'Print A from Home'.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test running module 'x'", dependsOnMethods = "testInstallingModuleB")
    public void testRunningModuleX() throws BallerinaTestException {
        String outputMessage = "Print A from Home";
        LogLeecher clientLeecher = new LogLeecher(outputMessage);
        balClient.runMain("run", new String[]{"x:main"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(10000);
    }
    
    /**
     * Change print content of 'x' module from 'Print A from Home' to 'Print A from Proj'. Running the 'x' module
     * should print 'Print A from Proj'.
     * @throws IOException Error changing the contents of the module.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Change content in module 'x' function", dependsOnMethods = "testRunningModuleX")
    public void testRunningModifiedModuleX() throws IOException, BallerinaTestException {
        File aPkgPrinterFile = tempProjectDirectory.resolve("a").resolve("printer.bal").toFile();
        String fileContext = FileUtils.readFileToString(aPkgPrinterFile);
        fileContext = fileContext.replaceAll("Print A from Home", "Print A from Proj");
        FileUtils.write(aPkgPrinterFile, fileContext);

        String outputMessage = "Print A from Proj";
        LogLeecher clientLeecher = new LogLeecher(outputMessage);
        balClient.runMain("run", new String[]{"x:main"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(5000);
    }

    /**
     * Change module name of 'b' to 'bc' and run 'x' module. This should print 'Print A from Home'.The 'b' module
     * should be resolved from project repository.
     * @throws IOException Error when modifying the module name.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Change content in module 'x' function", dependsOnMethods = "testRunningModifiedModuleX")
    public void testRunningWithModuleBFromProjectRepo() throws IOException, BallerinaTestException {
        Files.copy(tempProjectDirectory.resolve("b"), tempProjectDirectory.resolve("bc"));
        FileUtils.deleteDirectory(tempProjectDirectory.resolve("b").toFile());

        String outputMessage = "Print A from Home";
        LogLeecher clientLeecher = new LogLeecher(outputMessage);
        balClient.runMain("run", new String[]{"x:main"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(5000);
    }

    /**
     * Remove the project repository and run 'x' module. This should print 'Print A from Home'.The 'b' module
     * should be resolved from home repository.
     * @throws IOException Error when removing project repo.
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Change content in module 'x' function",
          dependsOnMethods = "testRunningWithModuleBFromProjectRepo")
    public void testRunningWithModuleBFromProjectHome() throws IOException, BallerinaTestException {
        FileUtils.deleteDirectory(tempProjectDirectory.resolve(".ballerina").resolve("repo").toFile());

        String outputMessage = "Print A from Home";
        LogLeecher clientLeecher = new LogLeecher(outputMessage);
        balClient.runMain("run", new String[]{"x:main"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
        clientLeecher.waitForText(5000);
    }
    
    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put("BALLERINA_DEV_STAGE_CENTRAL", "true");
        return envVariables;
    }
    
    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempHomeDirectory);
    }
}
