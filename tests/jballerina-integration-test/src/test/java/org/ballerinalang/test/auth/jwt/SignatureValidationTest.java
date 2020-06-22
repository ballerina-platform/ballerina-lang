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
 * Test cases for JWT signature validation with TrustStore and JWK based authentication/authorization scenarios.
 *
 * The following 8 scenarios are tested here.
 * T (TRUE) - Parameter/Config is available.
 * F (FALSE) - Parameter/Config is not available.
 *
 * | Scenario |  kid  | JwksConfig | TrustStoreConfig | Test Status |
 * |----------|-------|------------|------------------|-------------|
 * |   1      |   T   |     T      |         T        |    PASS     |
 * |   2      |   T   |     T      |         F        |    PASS     |
 * |   3      |   T   |     F      |         T        |    PASS     |
 * |   4      |   T   |     F      |         F        |    PASS     |
 * |   5      |   F   |     T      |         T        |    PASS     |
 * |   6      |   F   |     T      |         F        |    FAIL     |
 * |   7      |   F   |     F      |         T        |    PASS     |
 * |   8      |   F   |     F      |         F        |    PASS     |
 *
 * Additionally, there are 4 more scenarios where kid is available, but it is invalid.
 * |   9      |   T   |     T      |         T        |    FAIL     |
 * |  10      |   T   |     T      |         F        |    FAIL     |
 * |  11      |   T   |     F      |         T        |    PASS     |
 * |  12      |   T   |     F      |         F        |    PASS     |
 *
 * @since 2.0
 */
@Test(groups = "auth-test")
public class SignatureValidationTest extends AuthBaseTest {

    private final int servicePortOfOnlyTrustStoreConfig = 20114;
    private final int servicePortOfOnlyJwksConfig = 20115;
    private final int servicePortOfBothTrustStoreAndJwksConfig = 20116;
    private final int servicePortOfNoConfig = 20117;

    @Test(description = "Test authentication success - signature validated with JWKs config")
    public void testScenario1() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiTlRBeFptTXhORE15WkRnM0" +
                "1UVTFaR00wTXpFek9ESmhaV0k0TkRObFpEVTFPR0ZrTmpGaU1RIn0.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEi" +
                "LCAiZXhwIjoxOTA3NjY1NzQ2LCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaH" +
                "haNHRZYSJdfQ.E8E7VO18V6MG7Ns87Y314Dqg8RYOMe0WWYlSYFhSv0mHkJQ8bSSyBJzFG0Se_7UsTWFBwzIALw6wUiP7UGrao" +
                "silf8k6HGJWbTjWtLXfniJXx5NczikzciG8ADddksm-0AMi5uPsgAQdg7FNaH9f4vAL6SPMEYp2gN6GDnWTH7M1vnknwjOwTbQ" +
                "pGrPu_w2V1tbsBwSzof3Fk_cYrntu8D_pfsBu3eqFiJZD7AXUq8EYbgIxpSwvdi6_Rvw2_TAi46drouxXK2Jglz_HvheUVCERT" +
                "15Y8JNJONJPJ52MsN6t297hyFV9AmyNPzwHxxmi753TclbapDqDnVPI1tpc-Q");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfBothTrustStoreAndJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success - signature validated with JWKs config")
    public void testScenario2() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiTlRBeFptTXhORE15WkRnM0" +
                "1UVTFaR00wTXpFek9ESmhaV0k0TkRObFpEVTFPR0ZrTmpGaU1RIn0.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEi" +
                "LCAiZXhwIjoxOTA3NjY1NzQ2LCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaH" +
                "haNHRZYSJdfQ.E8E7VO18V6MG7Ns87Y314Dqg8RYOMe0WWYlSYFhSv0mHkJQ8bSSyBJzFG0Se_7UsTWFBwzIALw6wUiP7UGrao" +
                "silf8k6HGJWbTjWtLXfniJXx5NczikzciG8ADddksm-0AMi5uPsgAQdg7FNaH9f4vAL6SPMEYp2gN6GDnWTH7M1vnknwjOwTbQ" +
                "pGrPu_w2V1tbsBwSzof3Fk_cYrntu8D_pfsBu3eqFiJZD7AXUq8EYbgIxpSwvdi6_Rvw2_TAi46drouxXK2Jglz_HvheUVCERT" +
                "15Y8JNJONJPJ52MsN6t297hyFV9AmyNPzwHxxmi753TclbapDqDnVPI1tpc-Q");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success - signature validated with TrustStore config")
    public void testScenario3() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiTlRBeFptTXhORE15WkRnM0" +
                "1UVTFaR00wTXpFek9ESmhaV0k0TkRObFpEVTFPR0ZrTmpGaU1RIn0.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEi" +
                "LCAiZXhwIjoxOTA3NjY1NzQ2LCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaH" +
                "haNHRZYSJdfQ.E8E7VO18V6MG7Ns87Y314Dqg8RYOMe0WWYlSYFhSv0mHkJQ8bSSyBJzFG0Se_7UsTWFBwzIALw6wUiP7UGrao" +
                "silf8k6HGJWbTjWtLXfniJXx5NczikzciG8ADddksm-0AMi5uPsgAQdg7FNaH9f4vAL6SPMEYp2gN6GDnWTH7M1vnknwjOwTbQ" +
                "pGrPu_w2V1tbsBwSzof3Fk_cYrntu8D_pfsBu3eqFiJZD7AXUq8EYbgIxpSwvdi6_Rvw2_TAi46drouxXK2Jglz_HvheUVCERT" +
                "15Y8JNJONJPJ52MsN6t297hyFV9AmyNPzwHxxmi753TclbapDqDnVPI1tpc-Q");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyTrustStoreConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success - no signature validation required")
    public void testScenario4() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "NTAxZmMxNDMyZDg3MTU1ZGM0MzEzODJhZWI4NDNlZDU1OGFkNjFiMQ"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiTlRBeFptTXhORE15WkRnM0" +
                "1UVTFaR00wTXpFek9ESmhaV0k0TkRObFpEVTFPR0ZrTmpGaU1RIn0.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEi" +
                "LCAiZXhwIjoxOTA3NjY1NzQ2LCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaH" +
                "haNHRZYSJdfQ.E8E7VO18V6MG7Ns87Y314Dqg8RYOMe0WWYlSYFhSv0mHkJQ8bSSyBJzFG0Se_7UsTWFBwzIALw6wUiP7UGrao" +
                "silf8k6HGJWbTjWtLXfniJXx5NczikzciG8ADddksm-0AMi5uPsgAQdg7FNaH9f4vAL6SPMEYp2gN6GDnWTH7M1vnknwjOwTbQ" +
                "pGrPu_w2V1tbsBwSzof3Fk_cYrntu8D_pfsBu3eqFiJZD7AXUq8EYbgIxpSwvdi6_Rvw2_TAi46drouxXK2Jglz_HvheUVCERT" +
                "15Y8JNJONJPJ52MsN6t297hyFV9AmyNPzwHxxmi753TclbapDqDnVPI1tpc-Q");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfNoConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success - signature validated with TrustStore config")
    public void testScenario5() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665971,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJ" +
                "iYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY1OTcxLCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtM" +
                "WpWWUhVSENqaHhaNHRZYSJdfQ.XRp3ZatPLfnFVSwiKOVpgdcZekSu47imn23irWkOUbFltOwAe_dfKVHgyh1lk7T4VX6ZVyzE" +
                "cmCGcS5b6pqPL5hNFQkxz-72dWpFFjw9vka1fXvGv7PwjYyonFiw83TK84H2lnHcUuSXjzVli3LPcp3_9pHzV4XF1sm8QPNl91" +
                "5MZVyhxcUCY2ZSzWRhQ_whZIRUA_PwbkDnpoGkNgLASmp23nPU0Ftgorid-yQIDiBAZFe-p16DTqHMp5RvwMxrqPL9GgEfDoVa" +
                "wr56AN-Prmqq8hfIYDFIk_4rsZZHWVG7m0TV8X4mv0zMq2ZhBqF9LBss3jesfZiDPr8cOy_CJQ");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfBothTrustStoreAndJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication failure - signature validated with JWKs config")
    public void testScenario6() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665971,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJ" +
                "iYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY1OTcxLCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtM" +
                "WpWWUhVSENqaHhaNHRZYSJdfQ.XRp3ZatPLfnFVSwiKOVpgdcZekSu47imn23irWkOUbFltOwAe_dfKVHgyh1lk7T4VX6ZVyzE" +
                "cmCGcS5b6pqPL5hNFQkxz-72dWpFFjw9vka1fXvGv7PwjYyonFiw83TK84H2lnHcUuSXjzVli3LPcp3_9pHzV4XF1sm8QPNl91" +
                "5MZVyhxcUCY2ZSzWRhQ_whZIRUA_PwbkDnpoGkNgLASmp23nPU0Ftgorid-yQIDiBAZFe-p16DTqHMp5RvwMxrqPL9GgEfDoVa" +
                "wr56AN-Prmqq8hfIYDFIk_4rsZZHWVG7m0TV8X4mv0zMq2ZhBqF9LBss3jesfZiDPr8cOy_CJQ");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test authentication success - signature validated with TrustStore config")
    public void testScenario7() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665971,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJ" +
                "iYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY1OTcxLCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtM" +
                "WpWWUhVSENqaHhaNHRZYSJdfQ.XRp3ZatPLfnFVSwiKOVpgdcZekSu47imn23irWkOUbFltOwAe_dfKVHgyh1lk7T4VX6ZVyzE" +
                "cmCGcS5b6pqPL5hNFQkxz-72dWpFFjw9vka1fXvGv7PwjYyonFiw83TK84H2lnHcUuSXjzVli3LPcp3_9pHzV4XF1sm8QPNl91" +
                "5MZVyhxcUCY2ZSzWRhQ_whZIRUA_PwbkDnpoGkNgLASmp23nPU0Ftgorid-yQIDiBAZFe-p16DTqHMp5RvwMxrqPL9GgEfDoVa" +
                "wr56AN-Prmqq8hfIYDFIk_4rsZZHWVG7m0TV8X4mv0zMq2ZhBqF9LBss3jesfZiDPr8cOy_CJQ");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyTrustStoreConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication success - no signature validation required")
    public void testScenario8() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665971,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJhZG1pbiIsICJpc3MiOiJ" +
                "iYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY1OTcxLCAianRpIjoiMTAwMDc4MjM0YmEyMyIsICJhdWQiOlsidkV3emJjYXNKVlFtM" +
                "WpWWUhVSENqaHhaNHRZYSJdfQ.XRp3ZatPLfnFVSwiKOVpgdcZekSu47imn23irWkOUbFltOwAe_dfKVHgyh1lk7T4VX6ZVyzE" +
                "cmCGcS5b6pqPL5hNFQkxz-72dWpFFjw9vka1fXvGv7PwjYyonFiw83TK84H2lnHcUuSXjzVli3LPcp3_9pHzV4XF1sm8QPNl91" +
                "5MZVyhxcUCY2ZSzWRhQ_whZIRUA_PwbkDnpoGkNgLASmp23nPU0Ftgorid-yQIDiBAZFe-p16DTqHMp5RvwMxrqPL9GgEfDoVa" +
                "wr56AN-Prmqq8hfIYDFIk_4rsZZHWVG7m0TV8X4mv0zMq2ZhBqF9LBss3jesfZiDPr8cOy_CJQ");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfNoConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication failure - signature validated with JWKs config")
    public void testScenario9() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "<invalid-kid>"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiPGludmFsaWQta2lkPiJ9.e" +
                "yJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY2MTAwLCAianRpIjoiMTAwMDc4MjM0YmEyMyI" +
                "sICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaHhaNHRZYSJdfQ.LdoO0XnDtzK075G7J3XaNJpGd81GxZ5dQBkd9QyDJ8" +
                "26iE1djSer9228SLkl6U_yJYYv1iC9rblEEn_Xq5mv6SLpQ7wkObidHzEvqSpsDyTvb1mv43l3XUs6-tVKT6Bp6_pOhlacDPOK" +
                "YAjZv2UxGpqzyEfI9D6lnFceA2t70iENrcYj_hYrzDY-FE0sN8OGEf1_fKRWuiBNVl4m0iDpIuEU01EZyrjqJTCIquT9CIqx24" +
                "caIUStZByoXdNc-zcDEwFYpY89W76LDWqAF9FMqPppbOUJbcOTgGpLClaeBvwS_PjgwX96Oah21A_SeiwroVu2hFp3LNnLoSMM" +
                "LsGi8g");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfBothTrustStoreAndJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test authentication failure - signature validated with JWKs config")
    public void testScenario10() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "<invalid-kid>"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiPGludmFsaWQta2lkPiJ9.e" +
                "yJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY2MTAwLCAianRpIjoiMTAwMDc4MjM0YmEyMyI" +
                "sICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaHhaNHRZYSJdfQ.LdoO0XnDtzK075G7J3XaNJpGd81GxZ5dQBkd9QyDJ8" +
                "26iE1djSer9228SLkl6U_yJYYv1iC9rblEEn_Xq5mv6SLpQ7wkObidHzEvqSpsDyTvb1mv43l3XUs6-tVKT6Bp6_pOhlacDPOK" +
                "YAjZv2UxGpqzyEfI9D6lnFceA2t70iENrcYj_hYrzDY-FE0sN8OGEf1_fKRWuiBNVl4m0iDpIuEU01EZyrjqJTCIquT9CIqx24" +
                "caIUStZByoXdNc-zcDEwFYpY89W76LDWqAF9FMqPppbOUJbcOTgGpLClaeBvwS_PjgwX96Oah21A_SeiwroVu2hFp3LNnLoSMM" +
                "LsGi8g");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyJwksConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test authentication success - signature validated with TrustStore config")
    public void testScenario11() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "<invalid-kid>"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiPGludmFsaWQta2lkPiJ9.e" +
                "yJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY2MTAwLCAianRpIjoiMTAwMDc4MjM0YmEyMyI" +
                "sICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaHhaNHRZYSJdfQ.LdoO0XnDtzK075G7J3XaNJpGd81GxZ5dQBkd9QyDJ8" +
                "26iE1djSer9228SLkl6U_yJYYv1iC9rblEEn_Xq5mv6SLpQ7wkObidHzEvqSpsDyTvb1mv43l3XUs6-tVKT6Bp6_pOhlacDPOK" +
                "YAjZv2UxGpqzyEfI9D6lnFceA2t70iENrcYj_hYrzDY-FE0sN8OGEf1_fKRWuiBNVl4m0iDpIuEU01EZyrjqJTCIquT9CIqx24" +
                "caIUStZByoXdNc-zcDEwFYpY89W76LDWqAF9FMqPppbOUJbcOTgGpLClaeBvwS_PjgwX96Oah21A_SeiwroVu2hFp3LNnLoSMM" +
                "LsGi8g");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfOnlyTrustStoreConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authentication failure - no signature validation required")
    public void testScenario12() throws Exception {
        // JWT used in the test:
        // {
        //   "alg": "RS256",
        //   "typ": "JWT",
        //   "kid": "<invalid-kid>"
        // }
        // {
        //   "sub": "admin",
        //   "iss": "ballerina",
        //   "exp": 1907665746,
        //   "jti": "100078234ba23",
        //   "aud": [
        //     "vEwzbcasJVQm1jVYHUHCjhxZ4tYa"
        //   ]
        // }
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QiLCAia2lkIjoiPGludmFsaWQta2lkPiJ9.e" +
                "yJzdWIiOiJhZG1pbiIsICJpc3MiOiJiYWxsZXJpbmEiLCAiZXhwIjoxOTA3NjY2MTAwLCAianRpIjoiMTAwMDc4MjM0YmEyMyI" +
                "sICJhdWQiOlsidkV3emJjYXNKVlFtMWpWWUhVSENqaHhaNHRZYSJdfQ.LdoO0XnDtzK075G7J3XaNJpGd81GxZ5dQBkd9QyDJ8" +
                "26iE1djSer9228SLkl6U_yJYYv1iC9rblEEn_Xq5mv6SLpQ7wkObidHzEvqSpsDyTvb1mv43l3XUs6-tVKT6Bp6_pOhlacDPOK" +
                "YAjZv2UxGpqzyEfI9D6lnFceA2t70iENrcYj_hYrzDY-FE0sN8OGEf1_fKRWuiBNVl4m0iDpIuEU01EZyrjqJTCIquT9CIqx24" +
                "caIUStZByoXdNc-zcDEwFYpY89W76LDWqAF9FMqPppbOUJbcOTgGpLClaeBvwS_PjgwX96Oah21A_SeiwroVu2hFp3LNnLoSMM" +
                "LsGi8g");
        HttpResponse response = HttpsClientRequest.doGet(
                jwtAuthServerInstance.getServiceURLHttps(servicePortOfNoConfig, "echo/test"),
                headers, jwtAuthServerInstance.getServerHome());
        assertOK(response);
    }
}
