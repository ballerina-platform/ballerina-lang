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
package org.ballerinalang.test.service.resiliency;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

/**
 * Test cases for the circuit breaking scenarios.
 */
@Test(groups = "resiliency-test")
public class HTTPCircuitBreakerTestCase extends ResiliencyBaseTest {

    private static final String SUCCESS_HELLO_MESSAGE = "Hello World!!!";
    private static final String INTERNAL_ERROR_MESSAGE = "Internal error occurred while processing the request.";
    private static final String UPSTREAM_UNAVAILABLE_MESSAGE = "Upstream service unavailable.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Service unavailable.";
    private static final String IDLE_TIMEOUT_MESSAGE = "Idle timeout triggered before initiating inbound response";
    private static final String REQUEST_PAYLOAD_STRING = "{\"Name\":\"Ballerina\"}";
    private static final String TYPICAL_SERVICE_PATH = "cb" + File.separator + "typical";
    private static final String FORCE_OPEN_SERVICE_PATH = "cb" + File.separator + "forceopen";
    private static final String FORCE_CLOSE_SERVICE_PATH = "cb" + File.separator + "forceclose";
    private static final String GET_STATE_SERVICE_PATH = "cb" + File.separator + "getstate";
    private static final String REQUEST_VOLUME_SERVICE_PATH = "cb" + File.separator + "requestvolume";
    private static final String STATUS_CODE_SERVICE_PATH = "cb" + File.separator + "statuscode";
    private static final String TRIAL_FAILLURE_SERVICE_PATH = "cb" + File.separator + "trialrun";

    @Test(description = "Test basic circuit breaker functionality", dataProvider = "responseDataProvider")
    public void testTypicalBackendTimeout(int responseCode, String messasge) throws Exception {
        verifyResponses(9096, TYPICAL_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker forceOpen functionality",
            dataProvider = "forceOpenResponseDataProvider")
    public void testForceOPen(int responseCode, String messasge) throws Exception {
        verifyResponses(9097, FORCE_OPEN_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker forceClese functionality",
            dataProvider = "forceCloseResponseDataProvider")
    public void testForceClose(int responseCode, String messasge) throws Exception {
        verifyResponses(9098, FORCE_CLOSE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker getState functionality",
            dataProvider = "getStateResponseDataProvider")
    public void testgetState(int responseCode, String messasge) throws Exception {
        verifyResponses(9099, GET_STATE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker requestVolumeThreshold functionality",
            dataProvider = "requestVolumeResponseDataProvider")
    public void requestVolumeTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9100, REQUEST_VOLUME_SERVICE_PATH, responseCode, messasge);

    }

    @Test(description = "Test for circuit breaker failure status codes functionality",
            dataProvider = "statusCodeResponseDataProvider")
    public void httpStatusCodesTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9101, STATUS_CODE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker trail failure functionality",
            dataProvider = "trialRunFailureResponseDataProvider")
    public void trialRunFailureTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9102, TRIAL_FAILLURE_SERVICE_PATH, responseCode, messasge);
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

    @DataProvider(name = "forceOpenResponseDataProvider")
    public Object[][] forceOpenResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
        };
    }

    @DataProvider(name = "forceCloseResponseDataProvider")
    public Object[][] forceCloseResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_INTERNAL_SERVER_ERROR, IDLE_TIMEOUT_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, IDLE_TIMEOUT_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
        };
    }

    @DataProvider(name = "getStateResponseDataProvider")
    public Object[][] getStateResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
                new Object[]{SC_OK, SUCCESS_HELLO_MESSAGE},
        };
    }

    @DataProvider(name = "requestVolumeResponseDataProvider")
    public Object[][] requestVolumeResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
        };
    }

    @DataProvider(name = "statusCodeResponseDataProvider")
    public Object[][] statusCodeResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE},
                new Object[]{SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE},
                new Object[]{SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
        };
    }

    @DataProvider(name = "trialRunFailureResponseDataProvider")
    public Object[][] trialRunFailureResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
                new Object[]{SC_SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE},
                new Object[]{SC_INTERNAL_SERVER_ERROR, UPSTREAM_UNAVAILABLE_MESSAGE},
        };
    }

    private void verifyResponses(int port, String path, int responseCode, String expectedMessage) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(port, path)
                , REQUEST_PAYLOAD_STRING, headers);
        Assert.assertEquals(response.getResponseCode(), responseCode, "Response code mismatched");
        Assert.assertTrue(response.getData().contains(expectedMessage), "Message content mismatched");
    }
}
