/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.http;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.File;

/**
 * Base test class for Http integration test cases which starts/stops the http services as ballerina package before
 * and after tests are run.
 */
public class HttpBaseTest extends BaseTest {
    @BeforeTest(groups = "http-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "http").getAbsolutePath();
        String[] args = new String[]{"--sourceroot", balFile};
        serverInstance.startBallerinaServer("httpservices", args);
    }

    @AfterTest(groups = "http-test")
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.stopServer();
    }
}
