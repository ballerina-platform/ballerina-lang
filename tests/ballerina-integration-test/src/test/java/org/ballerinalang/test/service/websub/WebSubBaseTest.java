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

package org.ballerinalang.test.service.websub;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;

/**
 * Base test class for WebSub integration test cases which initializes required ballerina server instances before
 * and after tests are run.
 */
public class WebSubBaseTest extends BaseTest {
    protected static BServerInstance publisherServerInstance;

    @BeforeGroups(value = "websub-test", alwaysRun = true)
    public void init() throws BallerinaTestException {
        int[] requiredPorts = new int[]{8080, 9191, 8081};
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                          "websub").getAbsolutePath();
        publisherServerInstance = new BServerInstance(balServer);
        publisherServerInstance.startServer(balFile, "services", requiredPorts);
    }

    @AfterGroups(value = "websub-test")
    public void cleanup() throws Exception {
        publisherServerInstance.shutdownServer();
    }
}
