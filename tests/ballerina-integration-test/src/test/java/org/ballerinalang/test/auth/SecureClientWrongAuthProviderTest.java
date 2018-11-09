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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for verifying wrong auth provider for a service.
 */
@Test(groups = "auth-test")
public class SecureClientWrongAuthProviderTest extends AuthBaseTest {

    @Test(description = "Authn failure with wrong auth provider")
    public void testAuthSuccess() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(9194, "echo/test"), headers,
                serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }
}
