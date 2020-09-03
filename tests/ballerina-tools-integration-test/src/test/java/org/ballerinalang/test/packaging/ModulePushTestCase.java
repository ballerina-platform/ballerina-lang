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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Testing pushing a module from central by specifying the access token as an environment variable.
 *
 * @since 0.990.5
 */
public class ModulePushTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        moduleName = moduleName + TestUtils.randomModuleName(8);
        envVariables = addEnvVariables(TestUtils.getEnvVariables());
    }

    @Test(enabled = false, description = "Test pushing a package to central by specifying " +
            "the access token as an environment variable")
    public void testPush() throws Exception {
        // Org-name
        String orgName = "bcintegrationtest";

        // Initialize a ballerina project and create a module to be pushed
        Path projectPath = tempProjectDirectory.resolve("initPushProject");
        Files.createDirectories(projectPath);
        String[] options = {"\n", orgName + "\n", "\n", "m\n", moduleName + "\n", "f\n"};
        balClient.runMain("init", new String[]{"-i"}, envVariables, options, new LogLeecher[]{},
                projectPath.toString());

        // Push the module to Ballerina Central
        String msg = orgName + "/" + moduleName + ":0.0.1 [project repo -> central]";
        LogLeecher logLeecher = new LogLeecher(msg);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{},
                new LogLeecher[]{logLeecher}, projectPath.toString());
        logLeecher.waitForText(5000);
    }

    /**
     * Get environment variables.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put("BALLERINA_DEV_PREPROD_CENTRAL", "true");
        envVariables.put("BALLERINA_CENTRAL_ACCESS_TOKEN", TestUtils.getToken());
        return envVariables;
    }

    @AfterClass
    private void cleanup() throws Exception {
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
