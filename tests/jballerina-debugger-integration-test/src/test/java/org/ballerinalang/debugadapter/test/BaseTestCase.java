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
package org.ballerinalang.debugadapter.test;

import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.debugadapter.test.utils.FileUtils;
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
    protected static Path testProjectDirPath;
    protected static Path testSingleFileDirPath;
    protected static final String TEST_MODULE_NAME = "hello-world";
    protected static final String TEST_FILE_NAME = "hello_world.bal";

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        balServer = new BalServer();
        Path tempProjectDirectory = Files.createTempDirectory("bal-test-integration-debugger-project-");

        // Copy all the test resources to a temp dir.
        Path originalSingleFilesProj = Paths.get("src", "test", "resources", "single-file-tests")
                .toAbsolutePath();
        testSingleFileDirPath = tempProjectDirectory.resolve("single-file-tests");
        FileUtils.copyFolder(originalSingleFilesProj, testSingleFileDirPath);

        Path originalMultiModulesProj = Paths.get("src", "test", "resources", "project-based-tests/basic-project")
                .toAbsolutePath();
        testProjectDirPath = tempProjectDirectory.resolve("basic-project");
        FileUtils.copyFolder(originalMultiModulesProj, testProjectDirPath);
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() {
        balServer.cleanup();
    }
}
