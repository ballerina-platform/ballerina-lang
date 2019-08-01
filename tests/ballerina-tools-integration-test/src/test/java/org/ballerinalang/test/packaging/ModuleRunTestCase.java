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
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Integration test cases for running modules.
 */
public class ModuleRunTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-run-module-project-");
        envVariables = TestUtils.getEnvVariables();
    }

    /**
     * Running an empty module in a project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test running an empty module")
    public void testRunWithEmptyPkg() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("firstTestProject");
        initProject(projectPath);

        // Create empty directory
        Files.createDirectories(projectPath.resolve("emptypkg"));

        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found in module 'emptypkg'",
                                                  LeecherType.ERROR);
        balClient.runMain("run", new String[]{"emptypkg"}, envVariables, new String[0],
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Running a module with text files in a project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test running a module with text files")
    public void testRunWithOtherPkg() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("secondTestProject");
        initProject(projectPath);

        // Create empty directory
        createDirWithTextFile(projectPath);

        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found in module 'otherpkg'",
                                                  LeecherType.ERROR);
        balClient.runMain("run", new String[]{"otherpkg"}, envVariables, new String[0],
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Test running a module that is not inside a project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     * @throws IOException            When an error occurs when creating directories.
     */
    @Test(description = "Test running a module with text files")
    public void testRunModuleWithoutProject() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("moduleWithoutProject");
        Files.createDirectories(projectPath);
        String[] options = {"\n", "\n", "\n", "m\n", "foo\n", "f\n"};
        balClient.runMain("init", new String[]{"-i"}, envVariables, options, new LogLeecher[0], projectPath.toString());

        // Remove the .ballerina folder
        FileUtils.deleteDirectory(projectPath.resolve(".ballerina").toFile());

        String msg = "error: you are trying to run a module that is not inside a project. Run `ballerina init` " +
                "from " + projectPath.toRealPath().toString() + " to initialize it as a project and then run the " +
                "module.";
        LogLeecher leecher = new LogLeecher(msg, LeecherType.ERROR);
        balClient.runMain("run", new String[] {"foo"}, envVariables, new String[0], new LogLeecher[]{leecher},
                projectPath.toString());
        leecher.waitForText(3000);
    }

    /**
     * Init project.
     *
     * @param projectPath project path
     * @throws IOException            if an I/O exception occurs when creating directories
     * @throws BallerinaTestException if an exception occurs when running the command
     */
    private void initProject(Path projectPath) throws IOException, BallerinaTestException {
        Files.createDirectories(projectPath);
        String[] options = {"\n", "bcintegrationtest\n", "\n", "f\n"};
        balClient.runMain("init", new String[]{"-i"}, envVariables, options, new LogLeecher[0],
                          projectPath.toString());
    }

    /**
     * Create directory with a text file.
     *
     * @param path path of the directory to be created
     * @throws IOException if an I/O exception occurs when creating directories
     */
    private void createDirWithTextFile(Path path) throws IOException {
        Files.createDirectories(path.resolve("otherpkg"));
        Files.createFile(path.resolve("otherpkg").resolve("hello.txt"));
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
