/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;


import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test custom home option.
 */
public class CustomHomeCacheTest extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private String datePushed;
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private int totalPullCount = 0;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test custom home option")
    public void testCustomHomePath() throws BallerinaTestException, IOException {
        // copy project to tmp directory
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "customHomeCheck", "project")
                .toAbsolutePath();
        Path testProjectPath = this.tempProjectDirectory.resolve("project");
        copyFolder(originalTestProj1, testProjectPath);

        // copy balo to tmp directory
        Path originalHome = Paths.get("src", "test", "resources", "packaging", "customHomeCheck", "home")
                .toAbsolutePath();
        Path testHomePath = this.tempProjectDirectory.resolve("home");
        copyFolder(originalHome, testHomePath);

        Path projectPath = tempProjectDirectory.resolve("project");
        // Build module
        LogLeecher buildLeecher = new LogLeecher("Generating executables");
        balClient.runMain("build",
                new String[] { "--home-cache", testHomePath.toAbsolutePath().toString(), "-a" },
                envVariables,
                new String[] {},
                new LogLeecher[] { buildLeecher },
                projectPath.toString());
        buildLeecher.waitForText(60000);

        LogLeecher buildLeecher1 = new LogLeecher("Custom home check");
        balClient.runMain("run",
                new String[] { "--home-cache", testHomePath.toAbsolutePath().toString(), "myModule" },
                envVariables,
                new String[] {},
                new LogLeecher[] { buildLeecher1 },
                projectPath.toString());
        buildLeecher1.waitForText(60000);
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        return envVariables;
    }

    private void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackerinaTestUtils.deleteFiles(tempHomeDirectory);
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
