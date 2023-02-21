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
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test class containing tests related to disabling test.
 */
public class DisableTestsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("disabled-tests").toString();
    }

    @Test
    public void testDisablingTests() throws BallerinaTestException {
        String[] args = mergeCoverageArgs(new String[]{"disable-test.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertForTestFailures(output, "disable test failure");
    }

    @Test
    public void testDisablingTestsWithDependsOn() throws BallerinaTestException {
        String errMsg = "error: Test [testFunction3] depends on function [testDisableFunction2], " +
                "but it is either disabled or not included.";
        String[] args = mergeCoverageArgs(new String[]{"disable-with-depends-on.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        if (!output.contains(errMsg)) {
            AssertionUtils.assertForTestFailures(output, "dependant disabled function execution failure");
        }
    }
}
