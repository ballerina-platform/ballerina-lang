/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.scenario.test.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.scenario.test.common.ScenarioTestBase;
import org.ballerinalang.scenario.test.common.http.HttpClientRequest;
import org.ballerinalang.scenario.test.common.http.HttpResponse;
import org.ballerinalang.scenario.test.common.http.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the resiliency scenarios.
 */
public class HttpCircuitBreakerScenarioTest extends ScenarioTestBase {

    private static final int SC_OK = 200;
    private static final int SC_INTERNAL_SERVER_ERROR = 500;
    private static final String SUCCESS_HELLO_MESSAGE = "Hello World!!!";
    private static final String UPSTREAM_UNAVAILABLE_MESSAGE = "Upstream service unavailable.";
    private static final String IDLE_TIMEOUT_MESSAGE = "Idle timeout triggered before initiating inbound response";
    private static final String REQUEST_PAYLOAD_STRING = "{\"Name\":\"Ballerina\"}";
    private static final String TYPICAL_CB_SERVICE_PATH = "cb" + File.separator + "typical";

    @BeforeTest(alwaysRun = true)
    public void start() throws Exception {
        super.init();
    }

    @Test(description = "Test basic circuit breaker functionality", dataProvider = "responseDataProvider")
    public void testTypicalBackendTimeout(int responseCode, String messasge) throws Exception {
        verifyResponses(9306, TYPICAL_CB_SERVICE_PATH, responseCode, messasge);
    }

    @DataProvider(name = "responseDataProvider")
    public Object[][] responseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, IDLE_TIMEOUT_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
        };
    }

    private void verifyResponses(int port, String path, int responseCode, String expectedMessage) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(getServiceURL(path)
                , REQUEST_PAYLOAD_STRING, headers);
        Assert.assertEquals(response.getResponseCode(), responseCode, "Response code mismatched");
        Assert.assertTrue(response.getData().contains(expectedMessage), "Message content mismatched");
    }

    @AfterTest(alwaysRun = true)
    public void cleanup() throws Exception {
        super.cleanup();
    }
}
