/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.test.service;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Integration test to test service related functionalities.
 */
public class ServiceTest extends BaseTest {
    @Test(description = "Tests when there is only a service in the file")
    public void testListenerOnly() throws BallerinaTestException {
        String balFile = (new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "service" + File.separator + "listener" + File.separator +
                                           "service_only.bal")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain(balFile);
        // Makes sure that the execution of file doesn't timeout
        Assert.assertTrue(true);
    }
}
