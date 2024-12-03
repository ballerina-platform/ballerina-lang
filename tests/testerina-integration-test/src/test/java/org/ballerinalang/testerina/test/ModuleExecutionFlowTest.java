/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Test class to test Module test execution.
 *
 * @since 2201.6.0
 */

public class ModuleExecutionFlowTest extends BaseTestCase {

    private BMainInstance balClient;
    private Path projectPath;

    @BeforeClass
    public void setup() {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("module-execution-flow-tests");
    }

    @Test()
    public void testModuleExecutionFlow() throws BallerinaTestException, IOException {
        String[] args = new String[]{};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        AssertionUtils.assertOutput("ModuleExecutionFlowTest-test-listener-shutdown.txt", output);
    }

    @AfterMethod
    public void copyExec() {
        try {
            FileUtils.copyBallerinaExec(projectPath, String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            // ignore exception
        }
    }
}
