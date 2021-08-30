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
import org.ballerinalang.testerina.test.utils.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class MockTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
        FileUtils.copyFolder(Paths.get("build/libs"),
                Paths.get(projectPath, "object-mocking-tests", "libs"));
    }

    @Test()
    public void testFunctionMocking() throws BallerinaTestException {
        String msg1 = "13 passing";
        String msg2 = "3 failing";
        String[] args = mergeCoverageArgs(new String[]{"function-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to function mocking failure in test framework..\nOutput:\n" + output);
        }
    }

    @Test()
    public void testObjectMocking() throws BallerinaTestException {
        String msg1 = "9 passing";
        String msg2 = "7 failing";

        String[] args = mergeCoverageArgs(new String[]{"object-mocking-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(msg1) || !output.contains(msg2)) {
            Assert.fail("Test failed due to object mocking failure in test framework.\nOutput:\n" + output);
        }
    }

    @Test(dataProvider = "testNegativeCases")
    public void testObjectMocking_NegativeCases(String message, String test) throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"--tests", test});
        String output =
                balClient.runMainAndReadStdOut("test", args, new HashMap<>(),
                        projectBasedTestsPath.resolve("object-mocking-tests").toString(), false);
        if (!output.contains(message)) {
            throw new BallerinaTestException(
                    "Test failed due to default module single test failure.\nOutput:\n" + output);
        }
    }

    @DataProvider(name = "testNegativeCases")
    public static Object[][] negativeCases() {
        return new Object[][] {
                {"incorrect type of argument provided at position '1' to mock the function 'get()'",
                        "object_mocking:testDefaultIncompatibleArgs"},
                {"return value provided does not match the type of 'url'",
                        "object_mocking:testDefaultInvalidMemberReturnValue"},
                {"invalid field name 'invalidField' provided",
                        "object_mocking:testDefaultMockInvalidFieldName"},
                {"return value provided does not match the return type of function 'get()'",
                        "object_mocking:testDefaultMockInvalidReturnValue"},
                {"return value provided does not match the return type of function 'get()'",
                        "object_mocking:testDefaultMockWrongAction"},
                {"too many argument provided to mock the function 'get()'",
                        "object_mocking:testDefaultTooManyArgs"},
                {"return value provided does not match the return type of function 'get_stream()'",
                        "object_mocking:testMockInvalidStream"}
        };
    }
}
