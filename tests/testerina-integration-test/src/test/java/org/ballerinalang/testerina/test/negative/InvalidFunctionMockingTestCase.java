/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;

/**
 * Negative test cases for function mocking.
 */
public class InvalidFunctionMockingTestCase extends BaseTestCase {
    private BMainInstance balClient;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    @Test
    public void testMockingNonExistingFunction() throws BallerinaTestException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-function-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        assertEquals(output.replaceAll("\r", ""),
                "ERROR [tests" + File.separator + "test.bal:(3:1,5:2)] could not find functions in module\n" +
                "error: compilation contains errors");
    }

    @Test
    public void testMockingAFunctionInNonExistingModule() throws BallerinaTestException {
        String projectPath = projectBasedTestsPath.resolve("non-existent-module-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        assertEquals(output.replaceAll("\r", ""),
                "ERROR [tests" + File.separator + "test.bal:(3:1,6:2)] could not find module specified\n" +
                        "error: compilation contains errors");
    }

    @Test
    public void testMockingAFunctionWithIncompatibleTypes() throws BallerinaTestException {
        String projectPath = projectBasedTestsPath.resolve("incompatible-type-mock").toString();
        String output = balClient.runMainAndReadStdOut("test", new String[0], new HashMap<>(), projectPath, true);
        assertEquals(output.replaceAll("\r", ""),
                "ERROR [tests" + File.separator + "test.bal:(6:1,8:2)] incompatible types: " +
                        "expected isolated function () returns (string) " +
                        "but found isolated function () returns (int)\n" +
                        "error: compilation contains errors");
    }
}
