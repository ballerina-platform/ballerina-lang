/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Testing executing tests in a module.
 *
 * @since 0.995.0
 */
public class ExecuteTestsNegativeTestCase extends BaseTest {
    private Path projectPath;
    private String moduleName = "test";

    @BeforeClass()
    public void setUp() throws Exception {
        projectPath = Files.createTempDirectory("bal-test-integration-project-");
        moduleName = moduleName + TestUtils.randomModuleName(8);

        // Initialize a ballerina project.
        String[] options = {"\n", "\n", "\n", "m\n", moduleName + "\n", "f\n"};
        balClient.runMain("init", new String[]{"-i"}, TestUtils.getEnvVariables(), options,
                new LogLeecher[]{}, projectPath.toString());

        // Remove the .ballerina folder from the project.
        FileUtils.deleteDirectory(projectPath.resolve(".ballerina").toFile());
    }

    @Test(description = "Test executing tests in a module which is not inside a project")
    public void testExecutionInModule() throws IOException, BallerinaTestException {
        String expectedMsg = "error: you are trying to execute tests in a module that is not inside a project. Run " +
                "`ballerina init` from " + projectPath.toRealPath().toString() + " to initialize it as a project and " +
                "then execute the tests.";

        // Execute the command to run tests in the module.
        String loggedMsg = balClient.runMainAndReadStdOut("test", new String[]{moduleName}, new HashMap<>(),
                projectPath.toString(), true);

        Assert.assertEquals(expectedMsg, loggedMsg);
    }

    @Test(description = "Test executing tests in a project which is not a ballerina project")
    public void testExecutionInProject() throws IOException, BallerinaTestException {
        String expectedMsg = "error: you are trying to execute tests in a directory that is not a project. Run " +
                "`ballerina init` from " + projectPath.toRealPath().toString() + " to initialize it as a project " +
                "and then execute the tests.";

        // Execute the command to run tests in the project.
        String loggedMsg = balClient.runMainAndReadStdOut("test", new String[]{}, new HashMap<>(),
                projectPath.toString(), true);

        Assert.assertEquals(expectedMsg, loggedMsg);
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(projectPath);
    }
}
