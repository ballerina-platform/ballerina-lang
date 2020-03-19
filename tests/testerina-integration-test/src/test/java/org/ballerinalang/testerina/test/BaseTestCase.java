/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parent test class for all integration test cases. This will provide basic functionality for integration tests. This
 * will initialize a single ballerina instance which will be used by all the test cases throughout.
 */
public class BaseTestCase {

    public static BalServer balServer;
    Path tempProjectDirectory;
    protected static Path singleFilesProjectPath;
    static Path multiModulesProjectPath;
    static Path mockProjectPath;
    static Path serviceProjectBuildPath;

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        balServer = new BalServer();
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-testerina-project-");

        // copy TestProjects to a temp
        Path originalSingleFilesProj = Paths.get("src", "test", "resources", "single-file-tests")
                .toAbsolutePath();
        singleFilesProjectPath = tempProjectDirectory.resolve("single-file-tests");
        FileUtils.copyFolder(originalSingleFilesProj, singleFilesProjectPath);

        Path originalMultiModulesProj = Paths.get("src", "test", "resources", "tests-project")
                .toAbsolutePath();
        multiModulesProjectPath = tempProjectDirectory.resolve("tests-project");
        FileUtils.copyFolder(originalMultiModulesProj, multiModulesProjectPath);

        Path originalMockProj = Paths.get("src", "test", "resources", "mock-tests").toAbsolutePath();
        mockProjectPath = tempProjectDirectory.resolve("mock-tests");
        FileUtils.copyFolder(originalMockProj, mockProjectPath);

        Path serviceProjectPath = Paths.get("src", "test", "resources", "services-tests").toAbsolutePath();
        serviceProjectBuildPath = tempProjectDirectory.resolve("services-tests");
        FileUtils.copyFolder(serviceProjectPath, serviceProjectBuildPath);
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() {
        balServer.cleanup();
    }
}
