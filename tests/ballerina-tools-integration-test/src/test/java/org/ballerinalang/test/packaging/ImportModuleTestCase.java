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
import org.ballerinalang.test.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Integration test cases for module imports.
 *
 * @since 0.985
 */
public class ImportModuleTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Path tempHomeDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-repo-hierarchy-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-import-module-project-");
        envVariables = addEnvVariables(TestUtils.getEnvVariables());
    }

    /**
     * Importing modules among the same project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test importing modules among the same project")
    public void testResolveModulesFromProject() throws BallerinaTestException, IOException {
        Path projPath = tempProjectDirectory.resolve("firstProj");
        Files.createDirectories(projPath);

        FileUtils.copyDirectory(new File(getClass().getClassLoader().getResource("import-module").getPath()),
                projPath.toFile());
        Files.createDirectories(projPath.resolve(".ballerina"));

        String[] clientArgs = {"foo"};
        LogLeecher logLeecher = new LogLeecher("Hello Natasha !!!! Have a good day!!!");
        balClient.runMain("run", clientArgs, envVariables, new String[]{}, new LogLeecher[]{logLeecher},
                          projPath.toString());
        logLeecher.waitForText(3000);
    }

    /**
     * Importing modules with the same org-name installed in the home repository.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     * @throws IOException            When an error occurs when deleting the module.
     */
    @Test(description = "Test importing with the same org-name installed in the home repository",
            dependsOnMethods = "testResolveModulesFromProject")
    public void testResolveModules() throws BallerinaTestException, IOException {
        Path projPath = tempProjectDirectory.resolve("firstProj");
        // ballerina install abc
        balClient.runMain("install", new String[]{"abc"}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        // Delete module 'abc' from the project
        TestUtils.deleteFiles(projPath.resolve("abc"));

        String[] clientArgs = {"foo"};
        LogLeecher logLeecher = new LogLeecher("Hello Natasha !!!! Have a good day!!!");
        balClient.runMain("run", clientArgs, envVariables, new String[]{}, new LogLeecher[]{logLeecher},
                          projPath.toString());
        logLeecher.waitForText(3000);
    }

    /**
     * Importing modules among the same project in test sources.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test importing modules among the same project in test sources")
    public void testResolveModulesFromProjectInTestSources() throws BallerinaTestException, IOException {
        Path projPath = tempProjectDirectory.resolve("secProj");
        Files.createDirectories(projPath);

        FileUtils.copyDirectory(new File(getClass().getClassLoader().getResource("import-test-in-project").getPath()),
                projPath.toFile());
        Files.createDirectories(projPath.resolve(".ballerina"));

        balClient.runMain("build", new String[]{}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        Assert.assertTrue(Files.exists(projPath.resolve(".ballerina").resolve("repo").resolve("fanny")
                                                  .resolve("mod1").resolve("1.0.0").resolve("mod1.zip")));
        Assert.assertTrue(Files.exists(projPath.resolve(".ballerina").resolve("repo").resolve("fanny")
                                                  .resolve("mod2").resolve("1.0.0").resolve("mod2.zip")));


        LogLeecher sLeecher = new LogLeecher("Hello!! I got a message from an unknown person --> 100");
        balClient.runMain("test", new String[]{"mod1"}, envVariables, new String[]{},
                          new LogLeecher[]{sLeecher}, projPath.toString());
        sLeecher.waitForText(3000);
    }

    /**
     * Importing installed modules in test sources.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test importing installed modules in test sources")
    public void testResolveImportsFromInstalledModulesInTests() throws BallerinaTestException, IOException {
        Path projPath = tempProjectDirectory.resolve("thirdProj");
        Files.createDirectories(projPath);
        
        FileUtils.copyDirectory(new File(getClass().getClassLoader().getResource("import-test-in-cache").getPath()),
            projPath.toFile());
        Files.createDirectories(projPath.resolve(".ballerina"));

        // ballerina install abc
        balClient.runMain("install", new String[]{"mod2"}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        // Delete module 'abc' from the project
        TestUtils.deleteFiles(projPath.resolve("mod2"));
        TestUtils.deleteFiles(projPath.resolve(".ballerina").resolve("repo"));

        // Rename org-name to "natasha" in Ballerina.toml
        Path tomlFilePath = projPath.resolve("Ballerina.toml");
        String content = "[project]\norgName = \"natasha\"\nversion = \"1.0.0\"\n";
        Files.write(tomlFilePath, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        // Module fanny/mod2 will be picked from home repository since it was installed before
        balClient.runMain("build", new String[]{}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        Assert.assertTrue(Files.exists(projPath.resolve(".ballerina").resolve("repo").resolve("natasha")
                                                    .resolve("mod1").resolve("1.0.0").resolve("mod1.zip")));

        LogLeecher sLeecher = new LogLeecher("Hello!! I got a message from a cached repository module --> 100");
        balClient.runMain("test", new String[]{"mod1"}, envVariables, new String[]{},
                          new LogLeecher[]{sLeecher}, projPath.toString());
        sLeecher.waitForText(3000);
    }


    /**
     * Importing installed modules from both normal sources and test sources.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test importing installed modules in test sources")
    public void testResolveImportsInBoth() throws BallerinaTestException, IOException {
        Path projPath = tempProjectDirectory.resolve("fourthProj");
        Files.createDirectories(projPath);

        FileUtils.copyDirectory(new File(getClass().getClassLoader().getResource("import-in-both").getPath()),
                projPath.toFile());
        Files.createDirectories(projPath.resolve(".ballerina"));

        // ballerina install abc
        balClient.runMain("install", new String[]{"mod2"}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        // Delete module 'abc' from the project
        TestUtils.deleteFiles(projPath.resolve("mod2"));
        TestUtils.deleteFiles(projPath.resolve(".ballerina").resolve("repo"));

        // Rename org-name to "natasha" in Ballerina.toml
        Path tomlFilePath = projPath.resolve("Ballerina.toml");
        String content = "[project]\norgName = \"natasha\"\nversion = \"1.0.0\"\n";
        Files.write(tomlFilePath, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        // Module manny/mod2 will be picked from home repository since it was installed before
        balClient.runMain("build", new String[]{}, envVariables, new String[]{}, new LogLeecher[]{},
                          projPath.toString());

        Assert.assertTrue(Files.exists(projPath.resolve(".ballerina").resolve("repo").resolve("natasha")
                                                  .resolve("mod1").resolve("1.0.0").resolve("mod1.zip")));

        LogLeecher fLeecher = new LogLeecher("Hello Manny !! I got a message --> 100");
        balClient.runMain("test", new String[]{"mod1"}, envVariables, new String[]{},
                          new LogLeecher[]{fLeecher}, projPath.toString());
        fLeecher.waitForText(3000);

        LogLeecher sLeecher = new LogLeecher("Hello Manny !! I got a message --> 100 <---->244");
        balClient.runMain("run", new String[]{"mod1"}, envVariables, new String[]{},
                          new LogLeecher[]{sLeecher}, projPath.toString());
        sLeecher.waitForText(3000);
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
