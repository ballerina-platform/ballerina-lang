/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.openapi.test;


import org.ballerinalang.openapi.test.utils.FileUtils;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parent test class for all integration test cases. This will provide basic functionality for integration tests.
 */

public class BaseTestCase {
    public static BalServer balServer;
    Path tempProjectDirectory;
    static Path basicTestsProjectPath;

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        balServer = new BalServer();
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-openapi-project");

        // copy TestProject validator off  to a temp
        Path openapiValidatorProj = Paths.get("src", "test", "resources",
                "project-based-tests/openapi-validator").toAbsolutePath();

        basicTestsProjectPath = tempProjectDirectory.resolve("openapi-validator");
        FileUtils.copyFolder(openapiValidatorProj, basicTestsProjectPath);


    }
    @AfterSuite(alwaysRun = true)
    public void destroy() {
        balServer.cleanup();
    }

}
