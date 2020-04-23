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
package org.ballerinalang.debugger.test;

import org.ballerinalang.debugger.test.utils.FileUtils;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parent test class for all debugger integration test cases. This will provide basic functionality for integration
 * tests. This will initialize a single ballerina instance which will be used by all the test cases throughout.
 */
public class BaseTestCase {

    protected static BalServer balServer;
    protected static Path testProjectBaseDir;
    protected static Path testSingleFileBaseDir;
    protected static String testProjectName;
    protected static String testModuleName;
    protected static String testModuleFileName;
    protected static String testSingleFileName;

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        balServer = new BalServer();
        Path tempProjectDirectory = Files.createTempDirectory("bal-test-integration-debugger-project-");

        // Copy all the test resources to a temp dir.
        Path originalSingleFilesProj = Paths.get("src", "test", "resources", "single-file-tests")
                .toAbsolutePath();
        testSingleFileBaseDir = tempProjectDirectory.resolve("single-file-tests");
        FileUtils.copyFolder(originalSingleFilesProj, testSingleFileBaseDir);

        Path originalMultiModulesProj = Paths.get("src", "test", "resources", "project-based-tests")
                .toAbsolutePath();
        testProjectBaseDir = tempProjectDirectory.resolve("project-based-tests");
        FileUtils.copyFolder(originalMultiModulesProj, testProjectBaseDir);

        testProjectName = "basic-project";
        testModuleName = "hello-world";
        testModuleFileName = "hello_world.bal";
        testSingleFileName = "hello_world.bal";
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() {
        balServer.cleanup();
    }
}
