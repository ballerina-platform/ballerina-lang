/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.testerina.test;

import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Test cases for ballerina.test package.
 */
public class DataProviderTest {

    private String sourceRoot = "src/test/resources/annotations-test";

    @BeforeClass
    public void setup() {
    }

    @Test
    public void testBefore() {
        cleanup();
        BTestRunner runner = new BTestRunner();
        runner.runTest(sourceRoot, new Path[]{Paths.get("data-provider-test.bal")}, new
                ArrayList<>());
        Assert.assertEquals(runner.getTesterinaReport().getTestSummary(".", "passed"), 5);

    }

    @Test(expectedExceptions = BallerinaException.class, expectedExceptionsMessageRegExp = ".*Data provider " +
            "function \\[invalidDataGen\\] should return an array of arrays.*")
    public void testInvalidDataProviderNonArrayOfArraysReturn() {
        cleanup();
        new BTestRunner().runTest(sourceRoot, new Path[]{Paths.get
                        ("invalid-data-provider-test.bal")},
                new ArrayList<>());
    }

    //TODO temporarily commenting out till we solve the correct validations
//    @Test(expectedExceptions = BallerinaException.class, expectedExceptionsMessageRegExp = ".*Data provider function"
//            + "\\[invalidDataGen\\] should have only one return type.*")
    public void testInvalidDataProviderMultiReturn() {
        cleanup();
        new BTestRunner().runTest(sourceRoot, new Path[]{Paths.get
                ("invalid-data-provider-test-2.bal")}, new ArrayList<>());
    }

    @Test(expectedExceptions = BallerinaException.class, expectedExceptionsMessageRegExp = ".*Data provider " +
            "function \\[invalidDataGen\\] should return an array of arrays.*")
    public void testInvalidDataProviderPrimitiveReturn() {
        cleanup();
        new BTestRunner().runTest(sourceRoot, new Path[]{Paths.get
                        ("invalid-data-provider-test-3.bal")},
                new ArrayList<>());

    }

    private void cleanup() {
        TesterinaRegistry.getInstance().setProgramFiles(new ArrayList<>());
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }

}
