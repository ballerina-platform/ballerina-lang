/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test class to test Module test execution.
 */
public class ModuleExecutionTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.resolve("module-execution-tests").toString();
    }

    @Test()
    public void test_SingleModuleTestExecution() throws BallerinaTestException {
        String msg1 = "module_execution_tests.Module1";
        String msg2 = "1 passing";

        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);

        balClient.runMain("test", new String[]{"--modules", "Module1"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);

        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test()
    public void test_MultiModuleTestExecution() throws BallerinaTestException {
        String msg1 = "module_execution_tests.Module1";
        String msg2 = "module_execution_tests.Module2";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);

        balClient.runMain("test", new String[]{"--modules", "Module1,Module2"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);

        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test()
    public void test_NonExistingModuleTextExecution() throws BallerinaTestException {
        String msg1 = "module_execution_tests.Module1";
        String msg2 = "The following modules were not found in the modules directory : [ModuleA]";
        String msg3 = "1 passing";

        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        LogLeecher clientLeecher3 = new LogLeecher(msg3);

        balClient.runMain("test", new String[]{"--modules", "ModuleA,Module1"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2, clientLeecher3}, projectPath);

        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
        clientLeecher3.waitForText(20000);
    }

}
