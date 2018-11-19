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
 * Integration test cases for building modules.
 */
public class ModuleBuildTestCase extends BaseTest {
    private static final String ORG_NAME = "integrationtests";
    private static final String VERSION = "0.0.1";
    private static final String[] EMPTY_PROJECT_OPTS = {"\n", ORG_NAME + "\n", "\n", "f\n"};
    private static final String[] SINGLE_PKG_PROJECT_OPTS = {"\n", ORG_NAME + "\n", "\n", "m\n", "foo\n", "f\n"};
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-build-module-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    /**
     * Building all modules in the project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building all modules in the project")
    public void testBuild() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("firstTestProject");
        initProject(projectPath, SINGLE_PKG_PROJECT_OPTS);

        // Create an empty directory
        createEmptyDir(projectPath);

        // Create another directory with a text file
        createDirWithTextFile(projectPath);

        balClient.runMain("build", new String[0], envVariables, new String[0], new LogLeecher[]{}, projectPath.
                                                                                                            toString());

        Path genPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                    ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "foo", VERSION);
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath)));
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath).resolve("foo" + ".zip")));

        Path emptyPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                      ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "emptypkg", VERSION);
        Assert.assertTrue(Files.notExists(projectPath.resolve(emptyPkgPath).resolve("emptypkg" + ".zip")));

        Path otherPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                      ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "otherpkg", VERSION);
        Assert.assertTrue(Files.notExists(projectPath.resolve(otherPkgPath).resolve("otherpkg" + ".zip")));
    }

    /**
     * Building all modules in the project with one source module and another module with text files.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building all modules in the project with one source module and another module with " +
            "text files")
    public void testBuildWithFirstPkgCombination() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("secondTestProject");
        initProject(projectPath, SINGLE_PKG_PROJECT_OPTS);

        // Create another directory with a text file
        createDirWithTextFile(projectPath);

        balClient.runMain("build", new String[0], envVariables, new String[0], new LogLeecher[]{},
                          projectPath.toString());

        Path genPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                    ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "foo", VERSION);
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath)));
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath).resolve("foo" + ".zip")));

        Path otherPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                      ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "otherpkg", VERSION);
        Assert.assertTrue(Files.notExists(projectPath.resolve(otherPkgPath).resolve("otherpkg" + ".zip")));
    }

    /**
     * Building all modules in the project with one source module and another empty module.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building all modules in the project with one source module and another empty module")
    public void testBuildWithSecPkgCombination() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("thirdTestProject");
        initProject(projectPath, SINGLE_PKG_PROJECT_OPTS);

        // Create empty directory
        createEmptyDir(projectPath.resolve("emptypkg"));

        balClient.runMain("build", new String[0], envVariables, new String[0], new LogLeecher[]{},
                          projectPath.toString());

        Path genPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                    ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "foo", VERSION);
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath)));
        Assert.assertTrue(Files.exists(projectPath.resolve(genPkgPath).resolve("foo" + ".zip")));

        Path emptyPkgPath = Paths.get(ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                      ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, ORG_NAME, "emptypkg", VERSION);
        Assert.assertTrue(Files.notExists(projectPath.resolve(emptyPkgPath).resolve("emptypkg" + ".zip")));
    }

    /**
     * Building an empty module in a project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building empty module")
    public void testBuildWithEmptyPkg() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("thirdTestProject");
        initProject(projectPath, EMPTY_PROJECT_OPTS);

        // Create empty directory
        createEmptyDir(projectPath.resolve("emptypkg"));

        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found in module emptypkg",
                                                  LeecherType.ERROR);
        balClient.runMain("build", new String[]{"emptypkg"}, envVariables, new String[0],
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Building a module with text files in a project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building a module with text files")
    public void testBuildWithOtherPkg() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("fourthTestProject");
        initProject(projectPath, EMPTY_PROJECT_OPTS);

        // Create empty directory
        createDirWithTextFile(projectPath);

        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found in module otherpkg",
                                                  LeecherType.ERROR);
        balClient.runMain("build", new String[]{"otherpkg"}, envVariables, new String[0],
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Building a module with text files in a project and an empty module.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building a module with text files and an empty module")
    public void testBuildWithEmptyAndOtherPkg() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("fifthTestProject");
        initProject(projectPath, EMPTY_PROJECT_OPTS);

        // Create an empty directory
        createEmptyDir(projectPath.resolve("emptypkg"));
        // Create a directory with a text file
        createDirWithTextFile(projectPath);

        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found to compile",
                                                  LeecherType.ERROR);
        balClient.runMain("build", new String[0], envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Building an empty project without any modules.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building an empty project without any modules")
    public void testBuildEmptyProject() throws BallerinaTestException, IOException {
        Path projectPath = tempProjectDirectory.resolve("emptyProject");
        initProject(projectPath, EMPTY_PROJECT_OPTS);
        LogLeecher clientLeecher = new LogLeecher("error: no ballerina source files found to compile",
                                                  LeecherType.ERROR);
        balClient.runMain("build", new String[0], envVariables, new String[0], new LogLeecher[]{clientLeecher},
                          projectPath.toString());
        clientLeecher.waitForText(3000);
    }

    /**
     * Building a project with a module only with tests.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test building and testing a module only with tests")
    public void testModuleOnlyWithTest() throws BallerinaTestException, IOException {
        // Copy the module with tests to the temp project directory
        Path projectPath = tempProjectDirectory.resolve("testModuleBuild");
        String resourcePath = (new File("src/test/resources/testModule")).getAbsolutePath();
        FileUtils.copyDirectory(Paths.get(resourcePath).toFile(), projectPath.toFile());
        // Initialize the project
        initProject(projectPath, EMPTY_PROJECT_OPTS);

        // Test for ballerina build on module with test sources
        String buildMsg = "Compiling source\n" +
                    "    foo:0.0.0\n" +
                    "\n" +
                    "Running tests\n" +
                    "    foo:0.0.0\n" +
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

        LogLeecher firstLeecher = new LogLeecher(buildMsg);
        balClient.runMain("build", new String[] {"foo"}, envVariables, new String[0], new LogLeecher[]{firstLeecher},
                          projectPath.toString());
        Assert.assertTrue(Files.notExists(projectPath.resolve("target").resolve("foo.balx")));


        // Test for ballerina test on module with test sources
        String testExecMsg = "Compiling tests\n" +
                "    foo:0.0.0\n" +
                "\n" +
                "Running tests\n" +
                "    foo:0.0.0\n" +
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

        LogLeecher secLeecher = new LogLeecher(buildMsg);
        balClient.runMain("test", new String[] {"foo"}, envVariables, new String[0], new LogLeecher[]{secLeecher},
                          projectPath.toString());
    }

    /**
     * Init project.
     *
     * @param projectPath project path
     * @throws IOException            if an I/O exception occurs when creating directories
     * @throws BallerinaTestException if an exception occurs when running the command
     */
    private void initProject(Path projectPath, String[] options) throws IOException, BallerinaTestException {
        Files.createDirectories(projectPath);
        String[] clientArgsForInit = {"-i"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                          projectPath.toString());
    }

    /**
     * Create an empty directory.
     *
     * @param path path of the directory to be created
     * @throws IOException if an I/O exception occurs when creating directories
     */
    private void createEmptyDir(Path path) throws IOException {
        Files.createDirectories(path.resolve("emptypkg"));
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
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
