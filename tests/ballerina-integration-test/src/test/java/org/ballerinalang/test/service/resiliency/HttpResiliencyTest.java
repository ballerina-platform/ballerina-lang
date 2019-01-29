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
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

/**
 * Test cases for the resiliency scenarios.
 */
public class HttpResiliencyTest extends BaseTest {

    protected static BServerInstance serverInstance;
    private static final String TYPICAL_SERVICE_PATH = "fo" + File.separator + "typical";
    private static final String SUCCESS_HELLO_MESSAGE = "Hello World!!!";
    private static final String INTERNAL_ERROR_MESSAGE = "Internal error occurred while processing the request.";
    private static final String UPSTREAM_UNAVAILABLE_MESSAGE = "Upstream service unavailable.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Service unavailable.";
    private static final String IDLE_TIMEOUT_MESSAGE = "Idle timeout triggered before initiating inbound response";
    private static final String REQUEST_PAYLOAD_STRING = "{\"Name\":\"Ballerina\"}";
    private static final String TYPICAL_CB_SERVICE_PATH = "cb" + File.separator + "typical";
    private static final String FORCE_OPEN_SERVICE_PATH = "cb" + File.separator + "forceopen";
    private static final String FORCE_CLOSE_SERVICE_PATH = "cb" + File.separator + "forceclose";
    private static final String GET_STATE_SERVICE_PATH = "cb" + File.separator + "getstate";
    private static final String REQUEST_VOLUME_SERVICE_PATH = "cb" + File.separator + "requestvolume";
    private static final String STATUS_CODE_SERVICE_PATH = "cb" + File.separator + "statuscode";
    private static final String TRIAL_FAILLURE_SERVICE_PATH = "cb" + File.separator + "trialrun";
    private static final String LB_ROUND_ROBIN_SERVICE_PATH = "lb" + File.separator + "roundRobin";
    private static final String LB_ROUND_ROBIN_WITH_FO_SERVICE_PATH = "lb" + File.separator + "failover";
    private static final String ALL_LB_EP_FAILURE_SERVICE_PATH = "lb" + File.separator + "delay";
    private static final String LB_CUSTOM_ALGO_SERVICE_PATH = "lb" + File.separator + "custom";

    @BeforeTest(alwaysRun = true)
    public void start() throws BallerinaTestException {
        int[] requiredPorts = new int[]{8080, 9300, 8081, 9301, 8082, 9302, 8083, 9303, 8084, 9304, 8085, 9305,
                8086, 9306, 8087, 9307, 8088, 9308, 8089, 9309, 8090, 9310, 8091, 9311, 8092, 9312, 8093, 9313};
        String sourcePath = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "resiliency").getAbsolutePath();
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(sourcePath, "resiliencyservices", requiredPorts);
    }

    @Test(description = "Test basic failover functionality")
    public void testSimpleFailover() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9300, TYPICAL_SERVICE_PATH)
                , "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Mock Resource is Invoked.", "Message content mismatched");
    }

    @Test(description = "Test failover functionality with multipart requests")
    public void testMultiPart() throws IOException {
        String multipartDataBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"foo\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Part1" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"filepart\"; filename=\"file-01.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Part2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" +
                multipartDataBoundary);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9301, TYPICAL_SERVICE_PATH)
                , multipartBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                        .contains("multipart/form-data;boundary=" + multipartDataBoundary),
                "Response is not form of multipart");
        Assert.assertTrue(response.getData().contains("form-data;name=\"foo\"content-id: 0Part1")
                , "Message content mismatched");
        Assert.assertTrue(response.getData().
                        contains("form-data;name=\"filepart\";filename=\"file-01.txt\"content-id: 1Part2")
                , "Message content mismatched");
    }

    @Test(description = "Test failover functionality when request has nested body parts")
    public void testNestedMultiPart() throws IOException {
        String multipartDataBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String multipartMixedBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String nestedMultipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent1\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Parent Part" + "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent2\"" + "\r\n" +
                "Content-Type: multipart/mixed; boundary=" + multipartMixedBoundary + "\r\n" +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 1" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "--" + "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        String expectedChildPart1 =
                "Content-Transfer-Encoding: binary" +
                        "content-type: text/plain" +
                        "content-disposition: attachment;filename=\"file-02.txt\"content-id: 0" +
                        "Child Part 1";
        String expectedChildPart2 = "Content-Transfer-Encoding: binary" +
                "content-type: text/plain" +
                "content-disposition: attachment;filename=\"file-02.txt\"content-id: 1" +
                "Child Part 2";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" +
                multipartDataBoundary);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9302, TYPICAL_SERVICE_PATH)
                , nestedMultipartBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                        .contains("multipart/form-data;boundary=" + multipartDataBoundary),
                "Response is not form of multipart");
        Assert.assertTrue(response.getData().contains(expectedChildPart1), "Message content mismatched");
        Assert.assertTrue(response.getData().contains(expectedChildPart2), "Message content mismatched");
    }

    @Test(description = "Test the functionality for all endpoints failure scenario")
    public void testAllEndpointFailure() throws IOException {
        String expectedMessage = "All the failover endpoints failed. Last error was Idle timeout" +
                " triggered before initiating inbound response";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9303, "fo/failures")
                , "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), expectedMessage, "Message content mismatched");
    }

    @Test(description = "Test the functionality for all endpoints failure scenario")
    public void testResponseWithErrorStatusCodes() throws IOException {
        String expectedMessage = "All the failover endpoints failed. " +
                "Last endpoint returned response is: 503 Service Unavailable";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9304, "fo/failurecodes")
                , "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), expectedMessage, "Message content mismatched");
    }

    @Test(description = "Test to verify whether failover will test from last successful endpoint")
    public void testFailoverStartingPosition() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9305, "fo/index")
                , "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Failover start index is : 0", "Message content mismatched");
        HttpResponse secondResponse = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9305, "fo/index")
                , "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(secondResponse.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(secondResponse.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(secondResponse.getData(), "Failover start index is : 2", "Message content mismatched");
    }

    @Test(description = "Test basic circuit breaker functionality", dataProvider = "responseDataProvider")
    public void testTypicalBackendTimeout(int responseCode, String messasge) throws Exception {
        verifyResponses(9306, TYPICAL_CB_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker forceOpen functionality",
            dataProvider = "forceOpenResponseDataProvider")
    public void testForceOPen(int responseCode, String messasge) throws Exception {
        verifyResponses(9307, FORCE_OPEN_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker forceClese functionality",
            dataProvider = "forceCloseResponseDataProvider")
    public void testForceClose(int responseCode, String messasge) throws Exception {
        verifyResponses(9308, FORCE_CLOSE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker getState functionality",
            dataProvider = "getStateResponseDataProvider")
    public void testgetState(int responseCode, String messasge) throws Exception {
        verifyResponses(9309, GET_STATE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker requestVolumeThreshold functionality",
            dataProvider = "requestVolumeResponseDataProvider")
    public void requestVolumeTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9310, REQUEST_VOLUME_SERVICE_PATH, responseCode, messasge);

    }

    @Test(description = "Test for circuit breaker failure status codes functionality",
            dataProvider = "statusCodeResponseDataProvider")
    public void httpStatusCodesTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9311, STATUS_CODE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for circuit breaker trail failure functionality",
            dataProvider = "trialRunFailureResponseDataProvider")
    public void trialRunFailureTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9312, TRIAL_FAILLURE_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for round robin implementation algorithm of load balancer",
            dataProvider = "roundRobinResponseDataProvider")
    public void roundRobinLoadBlanceTest(int responseCode, String messasge) throws Exception {
        verifyResponses(9313, LB_ROUND_ROBIN_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for verify failover behavior with load balancer",
            dataProvider = "roundRobinWithFailoverResponseDataProvider")
    public void roundRobinWithFailoverResponseDataProvider(int responseCode, String messasge) throws Exception {
        verifyResponses(9313, LB_ROUND_ROBIN_WITH_FO_SERVICE_PATH, responseCode, messasge);
    }

    @Test(description = "Test for verify the error message when all endpoints are failing")
    public void testAllLbEndpointFailure() throws Exception {
        String expectedMessage = "All the load balance endpoints failed. Last error was: Idle timeout triggered " +
                "before initiating inbound response";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9313,
                ALL_LB_EP_FAILURE_SERVICE_PATH), REQUEST_PAYLOAD_STRING, headers);
        Assert.assertEquals(response.getResponseCode(), SC_INTERNAL_SERVER_ERROR, "Response code mismatched");
        Assert.assertTrue(response.getData().contains(expectedMessage), "Message content mismatched");
    }

    @Test(description = "Test for custom algorithm implementation of load balancer",
            dataProvider = "customLbResponseDataProvider")
    public void customLbResponseDataProvider(int responseCode, String messasge) throws Exception {
        verifyResponses(9313, LB_CUSTOM_ALGO_SERVICE_PATH, responseCode, messasge);
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

    @DataProvider(name = "roundRobinResponseDataProvider")
    public Object[][] roundRobinResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, "Mock1 Resource is Invoked."},
                new Object[]{SC_OK, "Mock2 Resource is Invoked."},
                new Object[]{SC_OK, "Mock3 Resource is Invoked."},
                new Object[]{SC_OK, "Mock1 Resource is Invoked."},
        };
    }

    @DataProvider(name = "roundRobinWithFailoverResponseDataProvider")
    public Object[][] roundRobinWithFailoverResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, "Mock2 Resource is Invoked."},
                new Object[]{SC_OK, "Mock3 Resource is Invoked."},
                new Object[]{SC_OK, "Mock2 Resource is Invoked."},
        };
    }

    @DataProvider(name = "customLbResponseDataProvider")
    public Object[][] customLbResponseDataProvider() {
        return new Object[][]{
                new Object[]{SC_OK, "Mock3 Resource is Invoked."},
                new Object[]{SC_OK, "Mock1 Resource is Invoked."},
                new Object[]{SC_OK, "Mock2 Resource is Invoked."},
                new Object[]{SC_OK, "Mock3 Resource is Invoked."},
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

    @AfterTest(alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
