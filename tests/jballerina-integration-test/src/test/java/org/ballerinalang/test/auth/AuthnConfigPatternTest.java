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

import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for authentication config pattern test scenarios.
 * The followings are the config patterns used for authentication handlers:
 * Pattern 1 - authHandlers: [jwtAuthHandler19_1]
 * Pattern 2 - authHandlers: [jwtAuthHandler19_1, jwtAuthHandler19_2]
 * Pattern 3 - authHandlers: [[jwtAuthHandler19_1]]
 * Pattern 4 - authHandlers: [[jwtAuthHandler19_1], [jwtAuthHandler19_3]]
 * Pattern 5 - authHandlers: [[jwtAuthHandler19_1, jwtAuthHandler19_2], [jwtAuthHandler19_3, jwtAuthHandler19_4]]
 */
@Test(groups = "auth-test")
public class AuthnConfigPatternTest extends AuthBaseTest {

    private final int servicePort = 20025;

    @Test(description = "Test pattern 1 with JWT of issuer: example1")
    public void testExample1IssuerJWTForPattern1() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example1",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiOi" +
                "JleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM" +
                "2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoiaGVs" +
                "bG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3GksSeIT9McZxj" +
                "PiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-3NMs8jjgcZHfZ1J" +
                "jomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0EiwpoI60HQFFlgC1g_crPI" +
                "DakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test pattern 2 with JWT of issuer: example1")
    public void testExample1IssuerJWTForPattern2() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example1",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiOi" +
                "JleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM" +
                "2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoiaGVs" +
                "bG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3GksSeIT9McZxj" +
                "PiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-3NMs8jjgcZHfZ1J" +
                "jomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0EiwpoI60HQFFlgC1g_crPI" +
                "DakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test pattern 3 with JWT of issuer: example1")
    public void testExample1IssuerJWTForPattern3() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example1",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiOi" +
                "JleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM" +
                "2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoiaGVs" +
                "bG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3GksSeIT9McZxj" +
                "PiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-3NMs8jjgcZHfZ1J" +
                "jomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0EiwpoI60HQFFlgC1g_crPI" +
                "DakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test pattern 4 with JWT of issuer: example1")
    public void testExample1IssuerJWTForPattern4() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example1",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiOi" +
                "JleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM" +
                "2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoiaGVs" +
                "bG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3GksSeIT9McZxj" +
                "PiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-3NMs8jjgcZHfZ1J" +
                "jomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0EiwpoI60HQFFlgC1g_crPI" +
                "DakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 5 with JWT of issuer: example1")
    public void testExample1IssuerJWTForPattern5() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example1",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiOi" +
                "JleGFtcGxlMSIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTIzM" +
                "2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoiaGVs" +
                "bG8ifQ.W_lvdp_o3o7MBVWeumg2fvIVSFWoGl9OFv2qyAz_g2afJwUWrFYOUd-1rj9lebZrQzqTd6RRX65MVF3GksSeIT9McZxj" +
                "PiSX1FR-nIUTcJ9anaoQVEKo3OpkIPzd_4_95CpHXF1MaW18ww5h_NShQnUrN7myrBfc-UbHsqC1YEBAM2M-3NMs8jjgcZHfZ1J" +
                "jomZCjd5eUXz8R5Vl46uAlSbFAmxAfY1T-31qUB93eCL2iJfDc70OK2txohryntw9h-OePwQULJN0EiwpoI60HQFFlgC1g_crPI" +
                "DakBTiEITrbO3OzrNeCQFBN-Ji4BTXq97TulCIRNneDLCUBSRE1A");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 1 with JWT of issuer: example2")
    public void testExample2IssuerJWTForPattern1() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example2",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiO" +
                "iJleGFtcGxlMiIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTI" +
                "zM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoia" +
                "GVsbG8ifQ.AUZp6qiMxPDrlITjyz-WoeDA6VxgV8pwwyQPkuw079BpNMeqIR4LsXUz1YZcFdNeZBfcj25cW7giIvMDRYNZtzEz" +
                "m8qovBmVzzd7vDdgvpvRuNhmFmcv63Jc9KjyoBiA_BDjFy9oTTzP35-PRuekQ0xy3gGjcgqhPcQtmLOyeTUbMhcrpGLB-fYp4x" +
                "9OqRo5ZNtMrm0aOuMj-VbKACc2vBdju5gu_nEtxBGeFWVHd_9l7OqNUTibmFzEV34GXP8rvVl73JZnp5tJesH-GXArsCjvSj1Q" +
                "pvcLBUiAaXFeXPb9t4iHFugJzHY68eQQZcxyIxWVyj2eNV4HmBjvqVLQuA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 2 with JWT of issuer: example2")
    public void testExample2IssuerJWTForPattern2() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example2",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiO" +
                "iJleGFtcGxlMiIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTI" +
                "zM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoia" +
                "GVsbG8ifQ.AUZp6qiMxPDrlITjyz-WoeDA6VxgV8pwwyQPkuw079BpNMeqIR4LsXUz1YZcFdNeZBfcj25cW7giIvMDRYNZtzEz" +
                "m8qovBmVzzd7vDdgvpvRuNhmFmcv63Jc9KjyoBiA_BDjFy9oTTzP35-PRuekQ0xy3gGjcgqhPcQtmLOyeTUbMhcrpGLB-fYp4x" +
                "9OqRo5ZNtMrm0aOuMj-VbKACc2vBdju5gu_nEtxBGeFWVHd_9l7OqNUTibmFzEV34GXP8rvVl73JZnp5tJesH-GXArsCjvSj1Q" +
                "pvcLBUiAaXFeXPb9t4iHFugJzHY68eQQZcxyIxWVyj2eNV4HmBjvqVLQuA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test pattern 3 with JWT of issuer: example2")
    public void testExample2IssuerJWTForPattern3() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example2",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiO" +
                "iJleGFtcGxlMiIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTI" +
                "zM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoia" +
                "GVsbG8ifQ.AUZp6qiMxPDrlITjyz-WoeDA6VxgV8pwwyQPkuw079BpNMeqIR4LsXUz1YZcFdNeZBfcj25cW7giIvMDRYNZtzEz" +
                "m8qovBmVzzd7vDdgvpvRuNhmFmcv63Jc9KjyoBiA_BDjFy9oTTzP35-PRuekQ0xy3gGjcgqhPcQtmLOyeTUbMhcrpGLB-fYp4x" +
                "9OqRo5ZNtMrm0aOuMj-VbKACc2vBdju5gu_nEtxBGeFWVHd_9l7OqNUTibmFzEV34GXP8rvVl73JZnp5tJesH-GXArsCjvSj1Q" +
                "pvcLBUiAaXFeXPb9t4iHFugJzHY68eQQZcxyIxWVyj2eNV4HmBjvqVLQuA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 4 with JWT of issuer: example2")
    public void testExample2IssuerJWTForPattern4() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example2",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiO" +
                "iJleGFtcGxlMiIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTI" +
                "zM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoia" +
                "GVsbG8ifQ.AUZp6qiMxPDrlITjyz-WoeDA6VxgV8pwwyQPkuw079BpNMeqIR4LsXUz1YZcFdNeZBfcj25cW7giIvMDRYNZtzEz" +
                "m8qovBmVzzd7vDdgvpvRuNhmFmcv63Jc9KjyoBiA_BDjFy9oTTzP35-PRuekQ0xy3gGjcgqhPcQtmLOyeTUbMhcrpGLB-fYp4x" +
                "9OqRo5ZNtMrm0aOuMj-VbKACc2vBdju5gu_nEtxBGeFWVHd_9l7OqNUTibmFzEV34GXP8rvVl73JZnp5tJesH-GXArsCjvSj1Q" +
                "pvcLBUiAaXFeXPb9t4iHFugJzHY68eQQZcxyIxWVyj2eNV4HmBjvqVLQuA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 5 with JWT of issuer: example2")
    public void testExample2IssuerJWTForPattern5() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example2",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3MiO" +
                "iJleGFtcGxlMiIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjYTI" +
                "zM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIjoia" +
                "GVsbG8ifQ.AUZp6qiMxPDrlITjyz-WoeDA6VxgV8pwwyQPkuw079BpNMeqIR4LsXUz1YZcFdNeZBfcj25cW7giIvMDRYNZtzEz" +
                "m8qovBmVzzd7vDdgvpvRuNhmFmcv63Jc9KjyoBiA_BDjFy9oTTzP35-PRuekQ0xy3gGjcgqhPcQtmLOyeTUbMhcrpGLB-fYp4x" +
                "9OqRo5ZNtMrm0aOuMj-VbKACc2vBdju5gu_nEtxBGeFWVHd_9l7OqNUTibmFzEV34GXP8rvVl73JZnp5tJesH-GXArsCjvSj1Q" +
                "pvcLBUiAaXFeXPb9t4iHFugJzHY68eQQZcxyIxWVyj2eNV4HmBjvqVLQuA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 1 with JWT of issuer: example3")
    public void testExample3IssuerJWTForPattern1() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example3",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3Mi" +
                "OiJleGFtcGxlMyIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjY" +
                "TIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIj" +
                "oiaGVsbG8ifQ.fhWGGF02Sxsk1LAD3bDOOQj2Q7hcfOBE65YSzuX0URf7fNAZfWvzJSzWMdw4_kGAOb8kzZ8LByipzPMmuoAh" +
                "yph3T7JzHGEXGCEXDmACywj58pnVKISsSb5tR8pDaidh-XRrCwE2hXB4X2_3fi9-Mn2U3ZFVb2q8-W9V9bmI1KJK-ALdKFuYu" +
                "Z9BzIq3YfZNyqAyFaQo9TFYhqNRvDbBsfdmjAfcj_SlYfSmbmTMG2FCahr9Tq_S3pMbh3S_6ii1-OqTGUukFdz1c08F5SvIZ9" +
                "t1xdW40dCnDrSR6urqVGys0Zg_Ru0mnPg4dU2JPuwDLuKzj4KzWXShZ2Il5Ol-IA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 2 with JWT of issuer: example3")
    public void testExample3IssuerJWTForPattern2() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example3",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3Mi" +
                "OiJleGFtcGxlMyIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjY" +
                "TIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIj" +
                "oiaGVsbG8ifQ.fhWGGF02Sxsk1LAD3bDOOQj2Q7hcfOBE65YSzuX0URf7fNAZfWvzJSzWMdw4_kGAOb8kzZ8LByipzPMmuoAh" +
                "yph3T7JzHGEXGCEXDmACywj58pnVKISsSb5tR8pDaidh-XRrCwE2hXB4X2_3fi9-Mn2U3ZFVb2q8-W9V9bmI1KJK-ALdKFuYu" +
                "Z9BzIq3YfZNyqAyFaQo9TFYhqNRvDbBsfdmjAfcj_SlYfSmbmTMG2FCahr9Tq_S3pMbh3S_6ii1-OqTGUukFdz1c08F5SvIZ9" +
                "t1xdW40dCnDrSR6urqVGys0Zg_Ru0mnPg4dU2JPuwDLuKzj4KzWXShZ2Il5Ol-IA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 3 with JWT of issuer: example3")
    public void testExample3IssuerJWTForPattern3() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example3",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3Mi" +
                "OiJleGFtcGxlMyIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjY" +
                "TIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIj" +
                "oiaGVsbG8ifQ.fhWGGF02Sxsk1LAD3bDOOQj2Q7hcfOBE65YSzuX0URf7fNAZfWvzJSzWMdw4_kGAOb8kzZ8LByipzPMmuoAh" +
                "yph3T7JzHGEXGCEXDmACywj58pnVKISsSb5tR8pDaidh-XRrCwE2hXB4X2_3fi9-Mn2U3ZFVb2q8-W9V9bmI1KJK-ALdKFuYu" +
                "Z9BzIq3YfZNyqAyFaQo9TFYhqNRvDbBsfdmjAfcj_SlYfSmbmTMG2FCahr9Tq_S3pMbh3S_6ii1-OqTGUukFdz1c08F5SvIZ9" +
                "t1xdW40dCnDrSR6urqVGys0Zg_Ru0mnPg4dU2JPuwDLuKzj4KzWXShZ2Il5Ol-IA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 4 with JWT of issuer: example3")
    public void testExample3IssuerJWTForPattern4() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example3",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3Mi" +
                "OiJleGFtcGxlMyIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjY" +
                "TIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIj" +
                "oiaGVsbG8ifQ.fhWGGF02Sxsk1LAD3bDOOQj2Q7hcfOBE65YSzuX0URf7fNAZfWvzJSzWMdw4_kGAOb8kzZ8LByipzPMmuoAh" +
                "yph3T7JzHGEXGCEXDmACywj58pnVKISsSb5tR8pDaidh-XRrCwE2hXB4X2_3fi9-Mn2U3ZFVb2q8-W9V9bmI1KJK-ALdKFuYu" +
                "Z9BzIq3YfZNyqAyFaQo9TFYhqNRvDbBsfdmjAfcj_SlYfSmbmTMG2FCahr9Tq_S3pMbh3S_6ii1-OqTGUukFdz1c08F5SvIZ9" +
                "t1xdW40dCnDrSR6urqVGys0Zg_Ru0mnPg4dU2JPuwDLuKzj4KzWXShZ2Il5Ol-IA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 5 with JWT of issuer: example3")
    public void testExample3IssuerJWTForPattern5() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example3",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        //   "scope": "hello"
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJpbmEiLCJpc3Mi" +
                "OiJleGFtcGxlMyIsImV4cCI6MjgxODQxNTAxOSwiaWF0IjoxNTI0NTc1MDE5LCJqdGkiOiJmNWFkZWQ1MDU4NWM0NmYyYjhjY" +
                "TIzM2QwYzJhM2M5ZCIsImF1ZCI6WyJiYWxsZXJpbmEiLCJiYWxsZXJpbmEub3JnIiwiYmFsbGVyaW5hLmlvIl0sInNjb3BlIj" +
                "oiaGVsbG8ifQ.fhWGGF02Sxsk1LAD3bDOOQj2Q7hcfOBE65YSzuX0URf7fNAZfWvzJSzWMdw4_kGAOb8kzZ8LByipzPMmuoAh" +
                "yph3T7JzHGEXGCEXDmACywj58pnVKISsSb5tR8pDaidh-XRrCwE2hXB4X2_3fi9-Mn2U3ZFVb2q8-W9V9bmI1KJK-ALdKFuYu" +
                "Z9BzIq3YfZNyqAyFaQo9TFYhqNRvDbBsfdmjAfcj_SlYfSmbmTMG2FCahr9Tq_S3pMbh3S_6ii1-OqTGUukFdz1c08F5SvIZ9" +
                "t1xdW40dCnDrSR6urqVGys0Zg_Ru0mnPg4dU2JPuwDLuKzj4KzWXShZ2Il5Ol-IA");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 1 with JWT of issuer: example4")
    public void testExample4IssuerJWTForPattern1() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example4",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJiYWxsZXJpbmEiLCAi" +
                "aXNzIjoiZXhhbXBsZTQiLCAiZXhwIjoyODE4NDE1MDE5LCAiaWF0IjoxNTI0NTc1MDE5LCAianRpIjoiZjVhZGVkNTA1ODVjN" +
                "DZmMmI4Y2EyMzNkMGMyYTNjOWQiLCAiYXVkIjpbImJhbGxlcmluYSIsICJiYWxsZXJpbmEub3JnIiwgImJhbGxlcmluYS5pby" +
                "JdfQ.B0S-9DbmFMvNcjmlQU0-ApKZjBKSxu9pQ-99GBV3oVklVYjYHQZ0FSfzN7bQu__Ap2EPkSVv3ugZbMc59FOxnO_580" +
                "73C_tf7OPuYO1GQGbkYtXaynngxZ-xELZDdUXa0RYY4x-Sf7UM6tg7SPht8BreBig6QH_rnRuuTSQAYLbynxX3BgMbguA5BDQ" +
                "wj-JEdWdFdTknBlCHU4-pSV_77jRRZ9Lb51aVE7MT-G69X4oQMZCvFh935I5FVx2kfh6-RL3RDvvkKJMz5MEL7vtnHIqNmGMO" +
                "TdlJG9xtLnzfGc17UnJD7WP-Cb4s1ZKZnwuDJQsODTtAbtIKYC52aTomGw");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test1"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 2 with JWT of issuer: example4")
    public void testExample4IssuerJWTForPattern2() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example4",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJiYWxsZXJpbmEiLCAi" +
                "aXNzIjoiZXhhbXBsZTQiLCAiZXhwIjoyODE4NDE1MDE5LCAiaWF0IjoxNTI0NTc1MDE5LCAianRpIjoiZjVhZGVkNTA1ODVjN" +
                "DZmMmI4Y2EyMzNkMGMyYTNjOWQiLCAiYXVkIjpbImJhbGxlcmluYSIsICJiYWxsZXJpbmEub3JnIiwgImJhbGxlcmluYS5pby" +
                "JdfQ.B0S-9DbmFMvNcjmlQU0-ApKZjBKSxu9pQ-99GBV3oVklVYjYHQZ0FSfzN7bQu__Ap2EPkSVv3ugZbMc59FOxnO_580" +
                "73C_tf7OPuYO1GQGbkYtXaynngxZ-xELZDdUXa0RYY4x-Sf7UM6tg7SPht8BreBig6QH_rnRuuTSQAYLbynxX3BgMbguA5BDQ" +
                "wj-JEdWdFdTknBlCHU4-pSV_77jRRZ9Lb51aVE7MT-G69X4oQMZCvFh935I5FVx2kfh6-RL3RDvvkKJMz5MEL7vtnHIqNmGMO" +
                "TdlJG9xtLnzfGc17UnJD7WP-Cb4s1ZKZnwuDJQsODTtAbtIKYC52aTomGw");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test2"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 3 with JWT of issuer: example4")
    public void testExample4IssuerJWTForPattern3() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example4",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJiYWxsZXJpbmEiLCAi" +
                "aXNzIjoiZXhhbXBsZTQiLCAiZXhwIjoyODE4NDE1MDE5LCAiaWF0IjoxNTI0NTc1MDE5LCAianRpIjoiZjVhZGVkNTA1ODVjN" +
                "DZmMmI4Y2EyMzNkMGMyYTNjOWQiLCAiYXVkIjpbImJhbGxlcmluYSIsICJiYWxsZXJpbmEub3JnIiwgImJhbGxlcmluYS5pby" +
                "JdfQ.B0S-9DbmFMvNcjmlQU0-ApKZjBKSxu9pQ-99GBV3oVklVYjYHQZ0FSfzN7bQu__Ap2EPkSVv3ugZbMc59FOxnO_580" +
                "73C_tf7OPuYO1GQGbkYtXaynngxZ-xELZDdUXa0RYY4x-Sf7UM6tg7SPht8BreBig6QH_rnRuuTSQAYLbynxX3BgMbguA5BDQ" +
                "wj-JEdWdFdTknBlCHU4-pSV_77jRRZ9Lb51aVE7MT-G69X4oQMZCvFh935I5FVx2kfh6-RL3RDvvkKJMz5MEL7vtnHIqNmGMO" +
                "TdlJG9xtLnzfGc17UnJD7WP-Cb4s1ZKZnwuDJQsODTtAbtIKYC52aTomGw");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test3"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 4 with JWT of issuer: example4")
    public void testExample4IssuerJWTForPattern4() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example4",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJiYWxsZXJpbmEiLCAi" +
                "aXNzIjoiZXhhbXBsZTQiLCAiZXhwIjoyODE4NDE1MDE5LCAiaWF0IjoxNTI0NTc1MDE5LCAianRpIjoiZjVhZGVkNTA1ODVjN" +
                "DZmMmI4Y2EyMzNkMGMyYTNjOWQiLCAiYXVkIjpbImJhbGxlcmluYSIsICJiYWxsZXJpbmEub3JnIiwgImJhbGxlcmluYS5pby" +
                "JdfQ.B0S-9DbmFMvNcjmlQU0-ApKZjBKSxu9pQ-99GBV3oVklVYjYHQZ0FSfzN7bQu__Ap2EPkSVv3ugZbMc59FOxnO_580" +
                "73C_tf7OPuYO1GQGbkYtXaynngxZ-xELZDdUXa0RYY4x-Sf7UM6tg7SPht8BreBig6QH_rnRuuTSQAYLbynxX3BgMbguA5BDQ" +
                "wj-JEdWdFdTknBlCHU4-pSV_77jRRZ9Lb51aVE7MT-G69X4oQMZCvFh935I5FVx2kfh6-RL3RDvvkKJMz5MEL7vtnHIqNmGMO" +
                "TdlJG9xtLnzfGc17UnJD7WP-Cb4s1ZKZnwuDJQsODTtAbtIKYC52aTomGw");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test4"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test pattern 5 with JWT of issuer: example4")
    public void testExample4IssuerJWTForPattern5() throws Exception {
        // JWT used in the test:
        // {
        //   "sub": "ballerina",
        //   "iss": "example4",
        //   "exp": 2818415019,
        //   "iat": 1524575019,
        //   "jti": "f5aded50585c46f2b8ca233d0c2a3c9d",
        //   "aud": [
        //     "ballerina",
        //     "ballerina.org",
        //     "ballerina.io"
        //   ],
        // }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsICJ0eXAiOiJKV1QifQ.eyJzdWIiOiJiYWxsZXJpbmEiLCAi" +
                "aXNzIjoiZXhhbXBsZTQiLCAiZXhwIjoyODE4NDE1MDE5LCAiaWF0IjoxNTI0NTc1MDE5LCAianRpIjoiZjVhZGVkNTA1ODVjN" +
                "DZmMmI4Y2EyMzNkMGMyYTNjOWQiLCAiYXVkIjpbImJhbGxlcmluYSIsICJiYWxsZXJpbmEub3JnIiwgImJhbGxlcmluYS5pby" +
                "JdfQ.B0S-9DbmFMvNcjmlQU0-ApKZjBKSxu9pQ-99GBV3oVklVYjYHQZ0FSfzN7bQu__Ap2EPkSVv3ugZbMc59FOxnO_580" +
                "73C_tf7OPuYO1GQGbkYtXaynngxZ-xELZDdUXa0RYY4x-Sf7UM6tg7SPht8BreBig6QH_rnRuuTSQAYLbynxX3BgMbguA5BDQ" +
                "wj-JEdWdFdTknBlCHU4-pSV_77jRRZ9Lb51aVE7MT-G69X4oQMZCvFh935I5FVx2kfh6-RL3RDvvkKJMz5MEL7vtnHIqNmGMO" +
                "TdlJG9xtLnzfGc17UnJD7WP-Cb4s1ZKZnwuDJQsODTtAbtIKYC52aTomGw");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort, "echo/test5"),
                headers, serverInstance.getServerHome());
        assertUnauthorized(response);
    }
}
