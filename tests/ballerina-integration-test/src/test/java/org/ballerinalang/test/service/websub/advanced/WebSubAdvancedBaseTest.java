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

package org.ballerinalang.test.service.websub.advanced;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;

/**
 * Base test class for advanced WebSub integration test cases which initializes required ballerina server instances
 * before and after tests are run.
 *
 * @since 0.990.3
 */
public class WebSubAdvancedBaseTest extends BaseTest {
    private static BServerInstance publisherServerInstance;

    @BeforeGroups(value = "websub-test", alwaysRun = true)
    public void init() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" +
                                          File.separator + "websub" + File.separator + "advanced_services" +
                                          File.separator + "01_websub_publisher.bal").getAbsolutePath();
        publisherServerInstance = new BServerInstance(balServer);
        String confPath = (new File("src/test/resources/websub/advanced_services/sample-users.toml")).getAbsolutePath();
        String[] publisherArgs = {"-c", confPath};
        publisherServerInstance.startServer(balFile, publisherArgs, new int[]{8080});
    }

    @AfterGroups(value = "websub-test")
    public void cleanup() throws Exception {
        publisherServerInstance.shutdownServer();
    }
}
