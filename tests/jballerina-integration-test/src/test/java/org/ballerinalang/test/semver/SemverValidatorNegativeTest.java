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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Integration negative test implementation for the semver validator.
 *
 * @since 2201.2.2
 */
public class SemverValidatorNegativeTest extends SemverValidatorBaseTest {

    @BeforeClass()
    private void setup() throws IOException, BallerinaTestException {
        tempProjectsDir = Files.createTempDirectory("bal-test-integration-semver-");
        customRepoDir = tempProjectsDir.resolve("ballerina-home");
        Path testProject = Paths.get("src", "test", "resources", "semver").toAbsolutePath();
        FileUtils.copyFolder(testProject, tempProjectsDir);
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test semver validation functionality where multiple release versions of the same package" +
            "are available in local/central repositories")
    public void testNegativeValidateWithNoReleases() throws BallerinaTestException {

        List<String> errorLogs = new LinkedList<>();
        errorLogs.add("error: failed to resolve package 'semvertool/library:1.0.0' from central");

        executeSemverCommand("package_2_0_0", new String[]{"--compare-version=1.0.0"}, new ArrayList<>(), errorLogs);
    }

    @AfterClass
    private void cleanup() throws Exception {
        FileUtils.deleteFiles(this.tempProjectsDir);
    }
}
