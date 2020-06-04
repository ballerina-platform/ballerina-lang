/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.observability.metrics;

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityConstants;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

/**
 * Integration test for observability of metrics.
 */
@Test(groups = "websocket-metrics-test")
public class WebSocketMetricsTestCase extends BaseTest {
    private static BServerInstance serverInstance;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMetricsTestCase.class);
    private static final String MESSAGE = "test message";
    private static final String CLOSE_MESSAGE = "closeMe";
    private static final int PORT = 9898;
    private static final String SERVER_URL = "ws://localhost:9898/basic/ws";
    private static final String METRICS_URL = "http://localhost:9797/metrics";


    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private Map<String, String> expectedMetrics = new HashMap<>();
    private static final ByteBuffer SENDING_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});


    @BeforeGroups(value = "websocket-metrics-test", alwaysRun = true)
    private void setup() throws Exception {
        serverInstance = new BServerInstance(balServer);
        String balFile = new File(RESOURCE_LOCATION + "websocket-metrics-test.bal").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add("--" + ObservabilityConstants.CONFIG_METRICS_ENABLED + "=true");
        args.add("--b7a.log.console.loglevel=INFO");
        serverInstance.startServer(balFile, null, args.toArray(new String[args.size()]), new int[]{PORT});
        addMetrics();
    }

    /**
     * Creates a new WebSocket client and connects to the server. Sends 5 text messages, 1 ping, 1 ping, 1 binary, 1
     * close message and then closes the connection.
     * <p>
     * Checks whether the published metrics are the same as the expected metrics.
     *
     * @throws Exception Error when executing the commands.
     */
    @Test (enabled = false)
    public void testMetrics() throws Exception {
        final WebSocketTestClient[] client = new WebSocketTestClient[1];
        await().atMost(200, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> {
            client[0] = new WebSocketTestClient(SERVER_URL);
            client[0].handshake();
            return client[0].isOpen();
        });
        client[0].sendText(MESSAGE);
        client[0].sendText(MESSAGE);
        client[0].sendText(MESSAGE);
        client[0].sendText(MESSAGE);
        client[0].sendText(MESSAGE);
        client[0].sendPing(SENDING_BYTE_BUFFER);
        client[0].sendPong(SENDING_BYTE_BUFFER);
        client[0].sendBinary(SENDING_BYTE_BUFFER);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client[0].setCountDownLatch(countDownLatch);
        client[0].sendText(CLOSE_MESSAGE);
        client[0].shutDown();
        countDownLatch.await(200, TimeUnit.SECONDS);

        URL metricsEndPoint = new URL(METRICS_URL);
        BufferedReader reader = new BufferedReader(new InputStreamReader(metricsEndPoint.openConnection()
                                                                                 .getInputStream()));
        List<String> metricsList = reader.lines().filter(s -> !s.startsWith("#")).collect(Collectors.toList());
        Assert.assertTrue(metricsList.size() != 0);
        int count = 0;
        for (String line : metricsList) {
            int index = line.lastIndexOf(" ");
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            String metric = getMetricName(key);
            String connectionID = getTag(key, WebSocketObservabilityConstants.TAG_CONNECTION_ID);
            String clientOrServer = getTag(key, WebSocketObservabilityConstants.TAG_CONTEXT);
            String service = getTag(key, WebSocketObservabilityConstants.TAG_SERVICE);
            String type = getTag(key, WebSocketObservabilityConstants.TAG_MESSAGE_TYPE);
            String[] tags = {connectionID, clientOrServer, service, type};
            key = generateNewKey(metric, tags);
            String actualValue = expectedMetrics.get(key);
            if (actualValue != null) {
                count++;
                logger.info(key + " -- " + value);
                Assert.assertEquals(value, actualValue, "Unexpected value found for metric " + key + ".");
            }
        }
        Assert.assertEquals(count, expectedMetrics.size(), "metrics count is not equal to the expected metrics count.");
        reader.close();
    }

    @AfterGroups(value = "websocket-metrics-test", alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.shutdownServer();
    }

    private void addMetrics() {
        expectedMetrics.put("ws_messages_received_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"binary\",}", "1.0");
        expectedMetrics.put("ws_messages_sent_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"text\",}", "5.0");
        expectedMetrics.put("ws_connections_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",}", "0.0");
        expectedMetrics.put("ws_messages_received_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"text\",}", "6.0");
        expectedMetrics.put("ws_messages_sent_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"binary\",}", "1.0");
        expectedMetrics.put("ws_messages_sent_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"close\",}", "1.0");
        expectedMetrics.put("ws_messages_received_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"pong\",}", "1.0");
        expectedMetrics.put("ws_errors_value{client_or_server=\"server\"," +
                                    "service=\"/basic/ws\",type=\"close\",}", "2.0");
    }

    private String getMetricName(String key) {
        int index = key.lastIndexOf("{");
        return key.substring(0, index);
    }

    private String getTag(String key, String tag) {
        Pattern connectionIDPattern = Pattern.compile(tag + "=\"[^\"]*\",");
        Matcher connectionIDMatcher = connectionIDPattern.matcher(key);
        if (connectionIDMatcher.find()) {
            return connectionIDMatcher.group(0);
        }
        return "";
    }

    private String generateNewKey(String metric, String[] tags) {
        String key = metric + "{";
        for (String tag : tags) {
            key = key + tag;
        }
        key = key + "}";
        return key;
    }
}
