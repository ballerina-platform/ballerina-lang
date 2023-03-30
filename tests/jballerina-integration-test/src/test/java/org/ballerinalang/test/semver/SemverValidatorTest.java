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

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Integration test implementation for the semver validator.
 *
 * @since 2201.2.2
 */
public class SemverValidatorTest extends SemverValidatorBaseTest {

    @BeforeClass()
    private void setup() throws IOException, BallerinaTestException {
        tempProjectsDir = Files.createTempDirectory("bal-test-integration-semver-");
        customRepoDir = tempProjectsDir.resolve("ballerina-home");
        Path testProject = Paths.get("src", "test", "resources", "semver").toAbsolutePath();
        FileUtils.copyFolder(testProject, tempProjectsDir);
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test semver validator output on a set of patch-compatible changes")
    public void testValidatePatchCompatibleChanges() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        List<String> expectedLogs = new LinkedList<>();
        expectedLogs.add("current version: 1.0.1");
        expectedLogs.add("compared with the release version '1.0.0'");
        expectedLogs.add("patch-compatible changes detected");
        expectedLogs.add("suggested version: 1.0.1");

        executeSemverCommand("package_1_0_1", new String[]{"--compare-version=1.0.0"}, expectedLogs);
    }

    @Test(description = "Test semver validator output on a set of patch-incompatible changes")
    public void testValidatePatchIncompatibleChanges() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        List<String> expectedLogs = new LinkedList<>();
        expectedLogs.add("current version: 1.1.0");
        expectedLogs.add("compared with the release version '1.0.0'");
        expectedLogs.add("patch-incompatible changes detected");
        expectedLogs.add("suggested version: 1.1.0");

        executeSemverCommand("package_1_1_0", new String[]{"--compare-version=1.0.0"}, expectedLogs);
    }

    @Test(description = "Test semver validator output on a set of backward-compatible changes")
    public void testValidateBackwardIncompatibleChanges() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        List<String> expectedLogs = new LinkedList<>();
        expectedLogs.add("current version: 2.0.0");
        expectedLogs.add("compared with the release version '1.0.0'");
        expectedLogs.add("backward-incompatible changes detected");
        expectedLogs.add("suggested version: 2.0.0");

        executeSemverCommand("package_2_0_0", new String[]{"--compare-version=1.0.0"}, expectedLogs);
    }

    @Test(description = "Test semver validation functionality where multiple release versions of the same package" +
            "are available in local/central repositories")
    public void testValidateWithMultipleRelease() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");
        pushPackageToCustomRepo("package_1_0_1");
        pushPackageToCustomRepo("package_1_1_0");

        List<String> expectedLogs = new LinkedList<>();
        expectedLogs.add("current version: 2.0.0");
        expectedLogs.add("compared with the release version '1.0.1'");
        expectedLogs.add("backward-incompatible changes detected");
        expectedLogs.add("suggested version: 2.0.0");

        executeSemverCommand("package_2_0_0", new String[]{"--compare-version=1.0.1"}, expectedLogs);
    }

    @Test(description = "Test semver validation output with the diff option enabled")
    public void testValidateWithDiffEnabled() throws BallerinaTestException {
        pushPackageToCustomRepo("package_1_0_0");

        List<String> expectedLogs = new LinkedList<>();
        expectedLogs.add("current version: 2.0.0");
        expectedLogs.add("compared with the release version '1.0.0'");
        expectedLogs.add("backward-incompatible changes detected");
        expectedLogs.add("suggested version: 2.0.0");
        // diff output related logs
        expectedLogs.add("Comparing version '2.0.0'(local) with version '1.0.0'(central)");
        expectedLogs.add("[+-] package 'library' is modified [version impact: MAJOR]");
        expectedLogs.add("  [+-] module 'library' is modified [version impact: MAJOR]");
        expectedLogs.add("    [++] function 'privateFunctionChanged' is added [version impact: PATCH]");
        expectedLogs.add("    [++] function 'publicFunctionNew' is added [version impact: PATCH]");
        expectedLogs.add("    [--] function 'privateFunction' is removed [version impact: PATCH]");
        expectedLogs.add("    [--] function 'publicFunction' is removed [version impact: MAJOR]");

        executeSemverCommand("package_2_0_0", new String[]{"--compare-version=1.0.0", "--show-diff"}, expectedLogs);
    }

    @AfterClass
    private void cleanup() throws Exception {
        FileUtils.deleteFiles(this.tempProjectsDir);
    }
}
