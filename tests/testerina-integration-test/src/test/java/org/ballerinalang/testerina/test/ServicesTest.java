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

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class containing tests related to services.
 */
public class ServicesTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        projectPath = serviceProjectBuildPath.toString();
    }

    @Test
    public void testHttpService() throws BallerinaTestException {
        String moduleServiceCallMsg = "Hello, Module!";
        String testServiceCallMsg = "Hello, Test!";

        // Test Command
        LogLeecher moduleServiceCallLeecher1 = new LogLeecher(moduleServiceCallMsg);
        LogLeecher testServiceCallLeecher1 = new LogLeecher(testServiceCallMsg);
        balClient.runMain("test", new String[]{"mod1"}, null, new String[]{},
                          new LogLeecher[]{moduleServiceCallLeecher1, testServiceCallLeecher1}, projectPath);
        moduleServiceCallLeecher1.waitForText(20000);
        testServiceCallLeecher1.waitForText(20000);

        // Build all Command
        LogLeecher moduleServiceCallLeecher2 = new LogLeecher(moduleServiceCallMsg);
        LogLeecher testServiceCallLeecher2 = new LogLeecher(testServiceCallMsg);
        balClient.runMain("test", new String[]{"mod1"}, null, new String[]{},
                          new LogLeecher[]{moduleServiceCallLeecher2, testServiceCallLeecher2}, projectPath);
        moduleServiceCallLeecher2.waitForText(20000);
        testServiceCallLeecher2.waitForText(20000);
    }
}
