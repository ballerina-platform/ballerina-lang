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
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;

/**
 * Base test class for Http integration test cases which starts/stops the http services as ballerina package before
 * and after tests are run.
 */
public class HttpBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;

    @BeforeGroups(value = "http-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{9090, 9224, 9091, 9092, 9093, 9094, 9095, 9096, 9097, 9098, 9099, 9100, 9101,
                9102, 9103, 9104, 9105, 9106, 9107, 9108, 9109, 9110, 9111, 9112, 9113, 9114, 9115, 9116, 9117, 9118,
                9119, 9217, 9218, 9219, 9220, 9221, 9222, 9223, 9225, 9226, 9227};
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "http").getAbsolutePath();
        String privateKey = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "certsAndKeys"
                        + File.separator + "private.key").getAbsolutePath();
        String publicCert = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "certsAndKeys"
                        + File.separator + "public.crt").getAbsolutePath();
        String[] args = new String[] { "-e", "certificate.key=" + privateKey, "-e", "public.cert=" + publicCert };
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "httpservices", args, requiredPorts);
    }

    @AfterGroups(value = "http-test", alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
