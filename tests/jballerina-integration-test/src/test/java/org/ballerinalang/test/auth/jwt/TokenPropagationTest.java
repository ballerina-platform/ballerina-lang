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

package org.ballerinalang.test.auth.jwt;

import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for verifying token propagation scenario.
 */
@Test(groups = "auth-test")
public class TokenPropagationTest extends AuthBaseTest {


    @Test(description = "Test JWT propagation with basic auth as the inbound authentication mechanism")
    public void testTokenPropagationWithBasicAuthInbound() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(20106, "passthrough"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test behaviour when JWT propagation is disabled, resulting in authn failure")
    public void testWithoutTokenPropagation() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic aXN1cnU6eHh4");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(20104, "passthrough"),
                headers, jwtAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test JWT propagation with JWT auth as the inbound authentication mechanism, without " +
            "token reissuing")
    public void testTokenPropagationWithJwtAuthInbound() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3M" +
                "iOiJleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjh" +
                "jYTIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3B" +
                "lIjoiaGVsbG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3G" +
                "ksSeIT9McZxjPiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-" +
                "3NMs8jjgcZHfZ1JjomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0Eiwp" +
                "oI60HQFFlgC1g_crPIDakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(20108, "passthrough"),
                headers, jwtAuthServerInstance.getServerHome());

        assertOK(response);
    }

    @Test(description = "Test JWT propagation with JWT auth as the inbound authentication mechanism, with " +
            "token reissuing")
    public void testTokenPropagationWithJwtAuthInboundAndTokenReissuing() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3M" +
                "iOiJleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjh" +
                "jYTIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3B" +
                "lIjoiaGVsbG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3G" +
                "ksSeIT9McZxjPiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-" +
                "3NMs8jjgcZHfZ1JjomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0Eiwp" +
                "oI60HQFFlgC1g_crPIDakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(20110, "passthrough"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Negative test for JWT propagation with JWT auth as the inbound authentication " +
            "mechanism, with token reissuing. Newly issued token's issuer is not what is expected.")
    public void testTokenPropagationWithJwtAuthInboundAndTokenReissuingNegative() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3M" +
                "iOiJleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjh" +
                "jYTIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3B" +
                "lIjoiaGVsbG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3G" +
                "ksSeIT9McZxjPiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-" +
                "3NMs8jjgcZHfZ1JjomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0Eiwp" +
                "oI60HQFFlgC1g_crPIDakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(20112, "passthrough"),
                headers, jwtAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }
}
