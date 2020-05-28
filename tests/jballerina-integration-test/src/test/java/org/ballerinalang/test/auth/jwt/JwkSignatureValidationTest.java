/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.auth.AuthBaseTest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for JWT signature validation with JWK based authentication/authorization scenarios.
 *
 * @since 0.983.0
 */
@Test(groups = "auth-test")
public class JwkSignatureValidationTest extends AuthBaseTest {

    private final int servicePort = 20114;

    @Test(description = "Test authentication success request by validating JWT signature with JWK")
    public void testAuthenticationSuccess() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJ4NXQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU" +
                "9HRmtOakZpTVEiLCJraWQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU9HRmtOakZpTVEi" +
                "LCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhdWQiOiJ2RXd6YmNhc0pWUW0xalZZSFVIQ2poe" +
                "Fo0dFlhIiwibmJmIjoxNTg3NjIxODkwLCJhenAiOiJ2RXd6YmNhc0pWUW0xalZZSFVIQ2poeFo0dFlhIiwiaXNzIjoiaHR0cHM" +
                "6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2VuIiwiZXhwIjo0NzQxMjIxODkwLCJpYXQiOjE1ODc2MjE4OTAsImp0a" +
                "SI6ImFiZWFlMjIyLWViNzctNDg2Mi05MTZkLTM0NjIyZDRlNGFmYyJ9.IoD0-39h7vEAoDdnKBRtWC6tpTyADsGyXomHbsCj_o" +
                "R5B8lj7jVUG2TCKoMXD_S_BV3F3ep7zENOW8wu0M7F27yJsgas5-vJ7BO1IMLD82PReeb160CnJ2tUFrmdT1Gc7uNfXfXuJv7q" +
                "wkgaWR0VvFCfsvl88UQXyXA0rEmNYT4p_jFjKovgPsPePl6Qf0uwO--xEhGEM4cUuBog2bgY54vaLg9iHnNb6ZZ_EZvjwIONZs" +
                "eBOiB5IXDzffUXnPfwUsGaygHqw71byV61VQhDLFDsm7Jrqe3cpd8hThAUHhVkgsz3irwXPolOdlMheslOIMunVcnQLT6yvGls" +
                "rHxS0g");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePort, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }
}
