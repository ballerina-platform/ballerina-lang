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
package org.ballerinalang.test.service.websub;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests introducing custom/specific webhooks, extending the WebSub Subscriber Service.
 */
public class WebSubServiceExtensionTestCase extends WebSubBaseTest {

    private static final String MOCK_HEADER = "MockHeader";

    private static final String BY_KEY_CREATED_LOG = "Created Notification Received, action: created";
    private static final String BY_KEY_FEATURE_LOG = "Feature Notification Received, domain: feature";

    private static final String BY_HEADER_ISSUE_LOG =
            "Issue Notification Received, header value: issue action: deleted";
    private static final String BY_HEADER_COMMIT_LOG =
            "Commit Notification Received, header value: commit action: created";

    private static final String BY_HEADER_AND_PAYLOAD_ISSUE_CREATED_LOG =
            "Issue Created Notification Received, header value: issue action: created";
    private static final String BY_HEADER_AND_PAYLOAD_FEATURE_PULL_LOG =
            "Feature Pull Notification Received, header value: pull domain: feature";
    private static final String BY_HEADER_AND_PAYLOAD_HEADER_ONLY_LOG =
            "HeaderOnly Notification Received, header value: headeronly action: header_only";
    private static final String BY_HEADER_AND_PAYLOAD_KEY_ONLY_LOG =
            "KeyOnly Notification Received, header value: key_only action: keyonly";

    private LogLeecher byKeyCreatedLogLeecher = new LogLeecher(BY_KEY_CREATED_LOG);
    private LogLeecher byKeyFeatureLogLeecher = new LogLeecher(BY_KEY_FEATURE_LOG);

    private LogLeecher byHeaderIssueLogLeecher = new LogLeecher(BY_HEADER_ISSUE_LOG);
    private LogLeecher byHeaderCommitLogLeecher = new LogLeecher(BY_HEADER_COMMIT_LOG);

    private LogLeecher byHeaderAndPayloadIssueCreatedLogLeecher =
            new LogLeecher(BY_HEADER_AND_PAYLOAD_ISSUE_CREATED_LOG);
    private LogLeecher byHeaderAndPayloadFeaturePullLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_FEATURE_PULL_LOG);
    private LogLeecher byHeaderAndPayloadHeaderOnlyLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_HEADER_ONLY_LOG);
    private LogLeecher byHeaderAndPayloadKeyOnlyLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_KEY_ONLY_LOG);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources"
                                                + File.separator + "websub" + File.separator
                                                + "websub_custom_subscriber_service.bal").getAbsolutePath();

        webSubSubscriber.addLogLeecher(byKeyCreatedLogLeecher);
        webSubSubscriber.addLogLeecher(byKeyFeatureLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderIssueLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderCommitLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadIssueCreatedLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadHeaderOnlyLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadFeaturePullLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadKeyOnlyLogLeecher);

        webSubSubscriber.startBallerinaServer(subscriberBal, new String[]{}, 8181);
    }

    @Test
    public void testDispatchingByKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8181/key", "{\"action\":\"created\"}",
                                                         headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byKeyCreatedLogLeecher.waitForText(3000);

        headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        response = HttpClientRequest.doPost("http://localhost:8181/key", "{\"domain\":\"feature\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byKeyCreatedLogLeecher.waitForText(3000);
    }

    @Test
    public void testDispatchingByHeader() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "issue");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8282/header", "{\"action\":\"deleted\"}",
                                                         headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderIssueLogLeecher.waitForText(3000);

        headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "commit");
        response = HttpClientRequest.doPost("http://localhost:8282/header", "{\"action\":\"created\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderCommitLogLeecher.waitForText(3000);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "issue");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8383/headerAndPayload",
                                                         "{\"action\":\"created\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderAndPayloadIssueCreatedLogLeecher.waitForText(3000);

        headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "pull");
        response = HttpClientRequest.doPost("http://localhost:8383/headerAndPayload", "{\"domain\":\"feature\"}",
                                            headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderAndPayloadFeaturePullLogLeecher.waitForText(3000);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKeyForOnlyHeader() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "headeronly");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8383/headerAndPayload",
                                                         "{\"action\":\"header_only\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderAndPayloadHeaderOnlyLogLeecher.waitForText(3000);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKeyForOnlyKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "key_only");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:8383/headerAndPayload",
                                                         "{\"action\":\"keyonly\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 202);
        byHeaderAndPayloadHeaderOnlyLogLeecher.waitForText(3000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        webSubSubscriber.stopServer();
    }
}
