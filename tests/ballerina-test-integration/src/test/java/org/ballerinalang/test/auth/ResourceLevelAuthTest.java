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

package org.ballerinalang.test.auth;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourceLevelAuthTest extends IntegrationTestCase {
    private ServerInstance ballerinaServer;

    @BeforeClass
    public void setup() throws Exception {
        String basePath = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "auth")
                .getAbsolutePath();
        String balFilePath = basePath + File.separator + "resource-level-auth-config-test.bal";
        String ballerinaConfPath = basePath + File.separator + "ballerina.conf";
        startServer(balFilePath, ballerinaConfPath);
    }

    private void startServer(String balFile, String configPath) throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        ballerinaServer.startBallerinaServerWithConfigPath(balFile, configPath);
    }

    @Test(description = "Authn and authz success test case")
    public void testAuthSuccessWithResourceLevelConfigs()
            throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    @Test(description = "Authn success and authz failure test case")
    public void testAuthzFailureWithResourceLevelConfigs()
            throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 403, "Response code mismatched");
    }

    @Test(description = "Authn and authz failure test case")
    public void testAuthFailureWithResourceLevelConfigs()
            throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }

    @AfterClass public void tearDown() throws Exception {
        ballerinaServer.stopServer();
    }
}
