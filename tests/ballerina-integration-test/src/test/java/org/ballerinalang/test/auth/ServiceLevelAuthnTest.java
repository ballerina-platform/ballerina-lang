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
 * Test cases for service level authorization.
 */
@Test(groups = "auth-test")
public class ServiceLevelAuthnTest extends AuthBaseTest {
    private final int servicePort = 9094;
    private final int servicePortForExpiredCertificateTest = 9101;

    @Test(description = "Authn and authz success test case")
    public void testAuthSuccess() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    @Test(description = "Authn success and authz failure test case")
    public void testAuthzFailure() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic aXNoYXJhOmFiYw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 403, "Response code mismatched");
    }

    @Test(description = "Authn and authz failure test case")
    public void testAuthFailure() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }
    @Test(description = "Auth with JWT signed with expired trusted certificate")
    public void testAuthnWithJWTSignedWithExpiredTrustedCertificate() throws Exception {
        // JWT used in the test:
        // {
        //  "sub": "ballerina",
        //  "iss": "ballerina",
        //  "exp": 2818415019,
        //  "iat": 1524575019,
        //  "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //  "aud": [
        //    "ballerina",
        //    "ballerina.org",
        //    "ballerina.io"
        //  ],
        //  "scope": "hello"
        //}
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc" +
                "3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUyNDU3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMm" +
                "I4Y2EyMzNkMGMyYTNjOWQiLCJhdWQiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdLCJzY29" +
                "wZSI6ImhlbGxvIn0.itXiQOLVA_PpVEDz3bCpA8cowZ_4nsUf_syv9cw2byAGjxE7w2JPb5RBi4hhIPqeQX0BAl56PedRvIwb" +
                "B9DkUDdEF9DIc3uYDTxOgys8fAyK-6hLsgjln65slb627bTTWwIcUszKeZLTIw1z4XKDShe9gQJGLiOCWOQ1YxmrnDM6HgOQb" +
                "18xqUzweCRL-DLAAYwjbzGQ56ekbEdAg02sFco4aozOyt8OUDwS9cH_JlhUn2JEHmVKaatljEnfgRc8fOW6Y5IJ7dOPp7ra5e" +
                "00sk7JwYY8wKaZWxAGSgRpWgTY6C4XRjGIsR5ZWQdXCAnV27idGDrtR2uG4YQwCWUCzA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance
                        .getServiceURLHttps(servicePortForExpiredCertificateTest, "echo13/test13"),
                headersMap, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }

    @Test(description = "Authn and authz success test case for a request with path parameters")
    public void testAuthSuccessWithPathParameter() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/path/1"),
                headers, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    @Test(description = "Authn and authz failure test case for a request with path parameters")
    public void testAuthFailureWithPathParameter() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Basic dGVzdDp0ZXN0MTIz");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/path/1"),
                headers, serverInstance.getServerHome());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 401, "Response code mismatched");
    }
}
