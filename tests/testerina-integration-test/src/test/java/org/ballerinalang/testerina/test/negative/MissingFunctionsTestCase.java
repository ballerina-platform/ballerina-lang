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
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Negative test cases for before,after attribute.
 */
public class MissingFunctionsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = singleFilesProjectPath.resolve("missing-functions").toString();
    }

    @Test
    public void testMissingBeforeFunction() throws BallerinaTestException {
        String errMsg = "error: Cannot find the specified before function : [beforeFunc-nonExist] for testerina " +
                "function : [beforeFuncNegative]";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"before-func-negative.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testMissingAfterFunction() throws BallerinaTestException {
        String errMsg = "error: Cannot find the specified after function : [afterFunc-nonExist] for testerina " +
                "function : [afterFuncNegative]";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"after-func-negative.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testMissingDependsOnFunction() throws BallerinaTestException {
        String errMsg = "error: Cannot find the specified dependsOn function : non-existing";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"depends-on-negative.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

}
