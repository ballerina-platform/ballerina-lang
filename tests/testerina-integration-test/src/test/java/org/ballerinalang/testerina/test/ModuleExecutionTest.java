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

import java.util.HashMap;


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
        balClient.runMain("test", new String[]{"Module1"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test()
    public void test_DefaultModuleTestExecution() throws BallerinaTestException {
        String msg1 = "module_execution_tests";
        String msg2 = "2 passing";
        LogLeecher clientLeecher1 = new LogLeecher(msg1);
        LogLeecher clientLeecher2 = new LogLeecher(msg2);
        balClient.runMain("test", new String[]{"module_execution_tests"}, null, new String[]{},
                new LogLeecher[]{clientLeecher1, clientLeecher2}, projectPath);
        clientLeecher1.waitForText(20000);
        clientLeecher2.waitForText(20000);
    }

    @Test()
    public void test_NonExistingModuleTextExecution() throws BallerinaTestException {
        String msg1 = "error: Cannot execute module ModuleA. Does not exist in the modules directory";
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"ModuleA"}, new HashMap<>(),
                projectPath, true);
        if (!errorOutput.contains(msg1)) {
            throw new BallerinaTestException("Test failed since correct error message was not read");
        }
    }

    @Test()
    public void test_TooManyModuleArguements() throws BallerinaTestException {
        String msg1 = "ballerina: too many arguments.";
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"Module1", "Module2"}, new HashMap<>(),
                projectPath, true);
        if (!errorOutput.contains(msg1)) {
            throw new BallerinaTestException("Test failed since correct error message was not read");
        }
    }
}
