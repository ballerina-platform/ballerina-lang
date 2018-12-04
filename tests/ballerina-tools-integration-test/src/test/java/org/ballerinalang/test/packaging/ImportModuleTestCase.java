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

        String projectPath = (new File("src/test/resources/import-module")).getAbsolutePath();
        FileUtils.copyDirectory(Paths.get(projectPath).toFile(), tempProjectDirectory.toFile());
        Files.createDirectories(tempProjectDirectory.resolve(".ballerina"));

        envVariables = addEnvVariables(PackagingTestUtils.getEnvVariables());
    }

    /**
     * Importing modules among the same project.
     *
     * @throws BallerinaTestException When an error occurs executing the command.
     */
    @Test(description = "Test importing modules among the same project")
    public void testResolveModulesFromProject() throws BallerinaTestException {
        String[] clientArgs = {"foo"};
        LogLeecher logLeecher = new LogLeecher("Hello Natasha !!!! Have a good day!!!");
        balClient.runMain("run", clientArgs, envVariables, new String[]{}, new LogLeecher[]{logLeecher},
                          tempProjectDirectory.toString());
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
        // ballerina install abc
        balClient.runMain("install", new String[]{"abc"}, envVariables, new String[]{}, new LogLeecher[]{},
                          tempProjectDirectory.toString());

        // Delete module 'abc' from the project
        PackagingTestUtils.deleteFiles(tempProjectDirectory.resolve("abc"));

        String[] clientArgs = {"foo"};
        LogLeecher logLeecher = new LogLeecher("Hello Natasha !!!! Have a good day!!!");
        balClient.runMain("run", clientArgs, envVariables, new String[]{}, new LogLeecher[]{logLeecher},
                          tempProjectDirectory.toString());
        logLeecher.waitForText(3000);
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
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
