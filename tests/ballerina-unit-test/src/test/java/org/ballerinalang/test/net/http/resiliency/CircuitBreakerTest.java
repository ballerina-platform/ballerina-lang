/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.net.http.resiliency;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test cases for the Circuit Breaker.
 */
public class CircuitBreakerTest {

    private static final String CB_ERROR_MSG = "Upstream service unavailable.";
    private static final String MOCK_ENDPOINT_NAME = "mockEP";

    // Following constants are defined to filter out Http Client errors from union responses.
    private static final int CB_CLIENT_FIRST_ERROR_INDEX = 3;
    private static final int CB_CLIENT_SECOND_ERROR_INDEX = 4;
    private static final int CB_CLIENT_TOP_MOST_SUCCESS_INDEX = 2;
    private static final int CB_CLIENT_FAILURE_CASE_ERROR_INDEX = 5;
    private static final int CB_CLIENT_FORCE_OPEN_INDEX = 4;
    private static final String STATUS_CODE_FIELD = "statusCode";

    private CompileResult compileResult, serviceResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/net/http/resiliency/circuit-breaker-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    /**
     * Test case for a typical scenario where an upstream service may become unavailable temporarily.
     */
    @Test
    public void testCircuitBreaker() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[] { 200, 200, 500, 503, 503, 200, 200, 200 };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testTypicalScenario");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        BRefValueArray errors = (BRefValueArray) returnVals[1];

        for (int i = 0; i < responses.size(); i++) {
            long statusCode;
            // With this check flow will direct to the else condition for Http Client Errors. The avoided response
            // indexes are consisted with the HttpClientError Responses.
            if (i != CB_CLIENT_FIRST_ERROR_INDEX && i != CB_CLIENT_SECOND_ERROR_INDEX) {
                BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
                statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();
                Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
            } else {
                Assert.assertNotNull(errors.get(i)); // the request which resulted in an error
                BError error = (BError) errors.get(i);
                String errMsg = ((BMap) error.getDetails()).getMap().get("message").toString();
                Assert.assertTrue(errMsg != null && errMsg.startsWith(CB_ERROR_MSG),
                        "Invalid error message from circuit breaker.");
            }
        }
    }

    /**
     * Test case scenario:
     * - Initially the circuit is healthy and functioning normally.
     * - Backend service becomes unavailable and eventually, the failure threshold is exceeded.
     * - Requests afterwards are immediately failed, with a 503 response.
     * - After the reset timeout expires, the circuit goes to HALF_OPEN state and a trial request is sent.
     * - The backend service is not available and therefore, the request fails again and the circuit goes back to OPEN.
     */
    @Test
    public void testTrialRunFailure() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[] { 200, 500, 503, 500, 503, 500 };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testTrialRunFailure");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        BRefValueArray errors = (BRefValueArray) returnVals[1];

        for (int i = 0; i < responses.size(); i++) {
            long statusCode;
            // With this check flow will direct to the else condition for Http Client Errors. The avoided response
            // indexes are consisted with the HttpClientError Responses.
            if (i < CB_CLIENT_TOP_MOST_SUCCESS_INDEX || i == CB_CLIENT_FAILURE_CASE_ERROR_INDEX) {
                BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
                statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();

                Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
            } else {
                Assert.assertNotNull(errors.get(i)); // the request which resulted in an error
                BError error = (BError) errors.get(i);
                String msg = ((BMap) error.getDetails()).getMap().get("message").toString();
                Assert.assertTrue(msg != null && msg.startsWith(CB_ERROR_MSG),
                        "Invalid error message from circuit breaker.");
            }
        }
    }

    /**
     * Test case scenario:
     * - Initially the circuit is healthy and functioning normally.
     * - Backend service respond with HTTP status code configured to consider as failures responses.
     * eventually the failure threshold is exceeded.
     * - Requests afterwards are immediately failed, with a 503 response.
     * - After the reset timeout expires, the circuit goes to HALF_OPEN state and a trial request is sent.
     * - The backend service is not available and therefore, the request fails again and the circuit goes back to OPEN.
     */
    @Test(description = "Test case for Circuit Breaker HTTP status codes.")
    public void testHttpStatusCodeFailure() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[] { 200, 500, 503, 500, 503, 503 };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testHttpStatusCodeFailure");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        BRefValueArray errs = (BRefValueArray) returnVals[1];
        validateCBResponses(responses, errs, CB_CLIENT_TOP_MOST_SUCCESS_INDEX, expectedStatusCodes);
    }

    /**
     * Test case scenario:
     * - Initially the circuit is healthy and functioning normally.
     * - during the middle of execution circuit will be force fully opened.
     * - Afterward requests should immediately fail.
     */
    @Test(description = "Verify the functionality of circuit breaker force open implementation")
    public void testCBForceOpenScenario() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[] { 200, 200, 200, 200, 503, 503, 503, 503 };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testForceOpenScenario");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        BRefValueArray errs = (BRefValueArray) returnVals[1];
        validateCBResponses(responses, errs, CB_CLIENT_FORCE_OPEN_INDEX, expectedStatusCodes);
    }

    /**
     * Test case scenario:
     * - Initially the circuit is healthy and functioning normally.
     * - Backend service becomes unavailable and eventually, the failure threshold is exceeded.
     * - After that circuit will be force fully closed.
     * - Afterward success responses should received.
     */
    @Test(description = "Verify the functionality of circuit breaker force close implementation")
    public void testCBForceCloseScenario() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[] { 200, 200, 500, 200, 200, 200, 200, 200 };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testForceCloseScenario");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        for (int i = 0; i < responses.size(); i++) {
            BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
            long statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();
            Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
        }
    }

    /**
     * Test case scenario:
     * - Circuit Breaker configured with requestVolumeThreshold.
     * - Circuit Breaker shouldn't interact with circuit state until the configured threshold exceeded.
     */
    @Test(description = "Verify the functionality of circuit breaker request volume threshold implementation")
    public void testCBRequestVolumeThresholdSuccessResponseScenario() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[]{200, 200, 200, 200, 200, 200};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testRequestVolumeThresholdSuccessResponseScenario");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        for (int i = 0; i < responses.size(); i++) {
            BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
            long statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();
            Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
        }
    }

    /**
     * Test case scenario:
     * - Circuit Breaker configured with requestVolumeThreshold.
     * - Circuit Breaker shouldn't interact with circuit state until the configured threshold exceeded.
     */
    @Test(description = "Verify the functionality of circuit breaker request volume threshold implementation")
    public void testCBRequestVolumeThresholdFailureResponseScenario() {
        // Expected HTTP status codes from circuit breaker responses.
        int[] expectedStatusCodes = new int[]{500, 500, 500, 500, 500, 500};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testRequestVolumeThresholdFailureResponseScenario");

        Assert.assertEquals(returnVals.length, 2);

        BRefValueArray responses = (BRefValueArray) returnVals[0];
        for (int i = 0; i < responses.size(); i++) {
            BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
            long statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();
            Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
        }
    }

    @Test(description = "Test the getCurrentState function of circuit breaker")
    public void testCBGetCurrentStatausScenario() {
        String value = "Circuit Breaker is in CLOSED state";
        String path = "/cb/getState";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage responseMsg = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, inRequestMsg);

        Assert.assertNotNull(responseMsg, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream()), value);
    }

    private void validateCBResponses(BRefValueArray responses, BRefValueArray errors,
                                     int index, int[] expectedStatusCodes) {
        for (int i = 0; i < responses.size(); i++) {
            long statusCode;
            // With this check flow will direct to the else condition for Http Client Errors. The avoided response
            // indexes are consisted with the HttpClientError Responses.
            if (i < CB_CLIENT_FORCE_OPEN_INDEX) {
                BMap<String, BValue> res = (BMap<String, BValue>) responses.get(i);
                statusCode = ((BInteger) res.get(STATUS_CODE_FIELD)).intValue();

                Assert.assertEquals(statusCode, expectedStatusCodes[i], "Status code does not match.");
            } else {
                Assert.assertNotNull(errors.get(i)); // the request which resulted in an error
                BMap<String, BValue> err = (BMap<String, BValue>) errors.get(i);
                String msg = err.get(BLangVMErrors.ERROR_MESSAGE_FIELD).stringValue();

                Assert.assertTrue(msg != null && msg.startsWith(CB_ERROR_MSG),
                        "Invalid error message from circuit breaker.");
            }
        }
    }
}
