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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Testing running a top level bal file inside a project.
 *
 * @since 0.982.0
 */
public class RunTopLevelBalInProjectTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        envVariables = PackagingTestUtils.getEnvVariables();
    }

    @Test(description = "Test running a top level bal file inside a project")
    public void testRunningBalInsideProject() throws Exception {
        // Test ballerina init
        String[] options = {"\n", "\n", "\n", "m\n", "\n", "f\n"};
        balClient.runMain("init", new String[]{"-i"}, envVariables, options, new LogLeecher[]{},
                          tempProjectDirectory.toString());
        Assert.assertTrue(Files.exists(tempProjectDirectory.resolve("main.bal")));

        // Test ballerina run by giving the file name of the bal file along with for forward slash in Ubuntu and Mac
        // and backward slash in Windows
        // Source path with forward slash for Ubuntu and Mac OS
        String sourcePath = "./main.bal";
        // Source path with backward slash for Windows
        if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
            sourcePath = ".\\main.bal";
        }

        LogLeecher logLeecher = new LogLeecher("Hello World!");
        balClient.runMain("run", new String[]{sourcePath}, envVariables, new String[0],
                          new LogLeecher[]{logLeecher}, tempProjectDirectory.toString());
        logLeecher.waitForText(2000);

        // Test ballerina run by giving the file name of the bal file
        balClient.runMain("run", new String[]{"main.bal"}, envVariables, new String[0],
                          new LogLeecher[]{logLeecher}, tempProjectDirectory.toString());
        logLeecher.waitForText(2000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
