/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.ballerinalang.test.semver;

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
import java.util.HashMap;
import java.util.Map;

import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_ENV_KEY;

/**
 * Integration tests for the semver validator tool.
 *
 * @since 2201.2.2
 */
public class SemverValidatorTest extends BaseTest {

    private Path tempProjectsDir;

    private Path customRepoDir;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectsDir = Files.createTempDirectory("bal-test-integration-semver-");
        customRepoDir = tempProjectsDir.resolve("ballerina-home");
        Path testProject = Paths.get("src", "test", "resources", "semver").toAbsolutePath();
        FileUtils.copyFolder(testProject, tempProjectsDir);
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test semver validator output on a set of patch-compatible changes")
    public void testPatchCompatibleChanges() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        LogLeecher[] logLeechers = new LogLeecher[]{
                new LogLeecher("current version: 1.0.1", LogLeecher.LeecherType.ERROR),
                new LogLeecher("compared with the release version '1.0.0'", LogLeecher.LeecherType.ERROR),
                new LogLeecher("patch-compatible changes detected", LogLeecher.LeecherType.ERROR),
                new LogLeecher("suggested version: 1.0.1", LogLeecher.LeecherType.ERROR)
        };
        executeSemverCommand("package_1_0_1", new String[]{"--compare-version=1.0.0"}, logLeechers);
    }

    @Test(description = "Test semver validator output on a set of patch-incompatible changes")
    public void testPatchIncompatibleChanges() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        LogLeecher[] logLeechers = new LogLeecher[]{
                new LogLeecher("current version: 1.1.0", LogLeecher.LeecherType.ERROR),
                new LogLeecher("compared with the release version '1.0.0'", LogLeecher.LeecherType.ERROR),
                new LogLeecher("patch-incompatible changes detected", LogLeecher.LeecherType.ERROR),
                new LogLeecher("suggested version: 1.1.0", LogLeecher.LeecherType.ERROR)
        };
        executeSemverCommand("package_1_1_0", new String[]{"--compare-version=1.0.0"}, logLeechers);
    }

    private void pushPackageToCustomRepo(String packageName) throws BallerinaTestException {
        Map<String, String> customRepoEnv = new HashMap<>();
        customRepoEnv.put(HOME_REPO_ENV_KEY, customRepoDir.toAbsolutePath().toString());

        balClient.runMain("pack", new String[]{}, customRepoEnv, new String[]{}, new LogLeecher[0],
                tempProjectsDir.resolve(packageName).toAbsolutePath().toString());

        balClient.runMain("push", new String[]{"--repository=local"}, customRepoEnv, new String[]{},
                new LogLeecher[0], tempProjectsDir.resolve(packageName).toAbsolutePath().toString());
    }

    private void executeSemverCommand(String packageName, String[] commandArgs, LogLeecher[] logLeechers) {
        try {
            Map<String, String> customRepoEnv = new HashMap<>();
            customRepoEnv.put(HOME_REPO_ENV_KEY, customRepoDir.toAbsolutePath().toString());

            balClient.runMain("semver", commandArgs, customRepoEnv, new String[]{}, logLeechers,
                    tempProjectsDir.resolve(packageName).toAbsolutePath().toString());

            for (LogLeecher leecher : logLeechers) {
                leecher.waitForText(10000);
            }
        } catch (BallerinaTestException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    private void cleanup() throws Exception {
        FileUtils.deleteFiles(this.tempProjectsDir);
    }
}
