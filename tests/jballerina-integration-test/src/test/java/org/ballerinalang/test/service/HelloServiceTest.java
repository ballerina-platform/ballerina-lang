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

package org.ballerinalang.test.service;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Simple integration test for http service.
 * @since 0.995.0
 */
public class HelloServiceTest extends BaseTest {

    private static BServerInstance serverInstance;
    private static final int SERVICE_PORT = 9600;


    @BeforeClass
    private void setup() throws Exception {

        serverInstance = new BServerInstance(balServer);
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "service" + File.separator + "service.bal")
                .getAbsolutePath();
        serverInstance.startServer(balFile, new int[SERVICE_PORT]);
    }

    @Test(description = "Test hello world service")
    public void testHello() throws IOException {

        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                "hello/chamil"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Hello, World!, chamil");
    }

    @AfterClass
    private void cleanup() throws Exception {

        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

}
