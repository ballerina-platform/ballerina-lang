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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class containing tests related to test path verification.
 */
public class PathVerificationTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = outsideTestsProjectPath.toString();
    }

    @Test
    public void verifyTestsOutsidePath() throws BallerinaTestException {
        LogLeecher passingLeecher = new LogLeecher("2 passing");
        LogLeecher failingLeecher = new LogLeecher("0 failing");
        balClient.runMain("test", new String[]{"pathVerification"}, null, new String[0],
                new LogLeecher[]{passingLeecher, failingLeecher}, projectPath);
        passingLeecher.waitForText(20000);
        failingLeecher.waitForText(20000);
    }

    @Test
    public void verifyMissingTestsDirectory() throws BallerinaTestException {
        String msg = "No tests found";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"missingTestsDirectory"},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }
}
