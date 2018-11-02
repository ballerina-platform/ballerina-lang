/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.SQLDBUtils;
import org.ballerinalang.test.util.SQLDBUtils.DBType;
import org.ballerinalang.test.util.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.util.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Integration test for observability of metrics.
 */
public class MetricsTestCase extends BaseTest {
    private static BServerInstance serverInstance;

    private TestDatabase sqlServer;
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private static final String DB_NAME = "TEST_DB";
    private Map<String, Pattern> expectedMetrics = new HashMap<>();

    @BeforeClass
    private void setup() throws Exception {
        serverInstance = new BServerInstance(balServer);
        sqlServer = new FileBasedTestDatabase(DBType.H2,
                "observability" + File.separator + "metrics" + File.separator + "data.sql", SQLDBUtils.DB_DIRECTORY,
                DB_NAME);
        String balFile = new File(RESOURCE_LOCATION + "metrics-test.bal").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add("--observe");
        args.add("-e");
        args.add(CONFIG_TABLE_METRICS + ".statistic.percentiles=0.5, 0.75, 0.98, 0.99, 0.999");
        serverInstance.startServer(balFile, args.toArray(new String[args.size()]), new int[]{9090});
        addMetrics();
    }

    @Test(enabled = false)
    public void testMetrics() throws Exception {
        // Test Service
        Assert.assertEquals(HttpClientRequest.doGet("http://localhost:9090/test").getData(),
                "[{\"PRODUCTID\":1,\"PRODUCTNAME\":\"WSO2-IAM\"},{\"PRODUCTID\":3,\"PRODUCTNAME\":\"WSO2-EI\"}]");

        // Send some requests
        int i = 0;
        while (i < 5) {
            HttpClientRequest.doGet("http://localhost:9090/test");
            i++;
        }

        // Read the metrics from the prometheus endpoint and test
        URL metricsEndPoint = new URL("http://localhost:9797/metrics");
        BufferedReader reader = new BufferedReader(new InputStreamReader(metricsEndPoint.openConnection()
                .getInputStream()));
        List<String> metricsList = reader.lines().filter(s -> !s.startsWith("#")).collect(Collectors.toList());
        Assert.assertEquals(metricsList.size(), expectedMetrics.size(),
                "metrics count is not equal to the expected metrics count.");
        metricsList.forEach(line -> {
            int index = line.lastIndexOf(" ");
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            Pattern pattern = expectedMetrics.get(key);
            Assert.assertNotNull(pattern, "Unexpected metric for key " + key + ". Complete line: " + line);
            Assert.assertTrue(pattern.matcher(value).find(),
                    "Unexpected value found for metric " + key + ". Value: " + value + ", Pattern: "
                            + pattern.pattern() + " Complete line: " + line);
        });

        reader.close();
    }

    @AfterClass
    private void cleanup() throws Exception {
        serverInstance.shutdownServer();
        sqlServer.stop();
    }

    private void addMetrics() {
        final Pattern regexNumber = Pattern.compile("[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?");
        final Pattern regexValue = Pattern.compile("6.0");
        // Startup Time
        expectedMetrics.put("startup_time_milliseconds", regexNumber);
        // HTTP Server connector metrics
        expectedMetrics.put("http_requests_total{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",}", regexValue);
        expectedMetrics.put("http_inprogress_requests{resource=\"getProduct\",service=\"metricsTest\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.5\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.75\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.98\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.99\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.999\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds_count{http_method=\"GET\",http_url=\"/test\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", regexValue);
        expectedMetrics.put("http_response_time_seconds_sum{http_method=\"GET\",http_url=\"/test\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds_max{http_method=\"GET\",http_url=\"/test\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", regexNumber);
        // HTTP connection metrics
        expectedMetrics.put("ballerina_http:Connection_requests_total{action=\"respond\",http_status_code=\"200\",}",
                regexValue);
        expectedMetrics.put("ballerina_http:Connection_2XX_requests_total{action=\"respond\",}", regexValue);
        expectedMetrics.put("ballerina_http:Connection_inprogress_requests{action=\"respond\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"200\",quantile=\"0.5\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"200\",quantile=\"0.75\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"200\",quantile=\"0.98\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"200\",quantile=\"0.99\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"200\",quantile=\"0.999\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_count{action=\"respond\"," +
                "http_status_code=\"200\",}", regexValue);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_sum{action=\"respond\"," +
                "http_status_code=\"200\",}", regexNumber);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_max{action=\"respond\"," +
                "http_status_code=\"200\",}", regexNumber);
        // SQL connector metrics
        expectedMetrics.put("ballerina_sql:CallerActions_inprogress_requests{action=\"select\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_requests_total{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regexValue);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.5\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.75\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.98\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.99\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.999\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_count{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regexValue);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_sum{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regexNumber);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_max{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regexNumber);
        // Scheduler metrics
        expectedMetrics.put("ballerina_scheduler_paused_worker_count", regexNumber);
        expectedMetrics.put("ballerina_scheduler_waiting_for_response_worker_count", regexNumber);
        expectedMetrics.put("ballerina_scheduler_running_worker_count", regexNumber);
        expectedMetrics.put("ballerina_scheduler_waiting_for_lock_worker_count", regexNumber);
        expectedMetrics.put("ballerina_scheduler_ready_worker_count", regexNumber);
        expectedMetrics.put("ballerina_scheduler_excepted_worker_count", regexNumber);
    }
}
