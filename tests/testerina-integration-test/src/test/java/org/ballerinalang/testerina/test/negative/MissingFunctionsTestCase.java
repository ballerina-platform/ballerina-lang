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
package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Negative test cases for before,after attribute.
 */
public class MissingFunctionsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("missing-functions").toString();
    }

    @Test
    public void testMissingBeforeFunction() throws BallerinaTestException, IOException {
        String errMsg = "ERROR [before-func-negative.bal:(22:13,22:31)] undefined symbol 'beforeFuncNonExist'";
        String[] args = mergeCoverageArgs(new String[]{"before-func-negative.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("MissingFunctionsTestCase-testMissingBeforeFunction.txt", output);
    }

    @Test
    public void testMissingAfterFunction() throws BallerinaTestException, IOException {
        String errMsg = "ERROR [after-func-negative.bal:(22:12,22:29)] undefined symbol 'afterFuncNonExist'";
        String[] args = mergeCoverageArgs(new String[]{"after-func-negative.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("MissingFunctionsTestCase-testMissingAfterFunction.txt", output);
    }

    @Test
    public void testMissingDependsOnFunction() throws BallerinaTestException, IOException {
        String errMsg = "ERROR [depends-on-negative.bal:(22:17,22:28)] undefined symbol 'nonExisting'";
        String[] args = mergeCoverageArgs(new String[]{"depends-on-negative.bal"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("MissingFunctionsTestCase-testMissingDependsOnFunction.txt", output);
    }

}
