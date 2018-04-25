/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.ballerinalang.test.observability.metrics;

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * TODO: Class level comment.
 */
public class MetricsTestCase {

    private ServerInstance serverInstance;
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private static final String DB_NAME = "TEST_DB";
    private static Map<String, Pattern> expectedMetrics = new HashMap<>();

    @BeforeTest
    private void setup() throws Exception {
        serverInstance = ServerInstance.initBallerinaServer();
        copyFile(new File(System.getProperty("hsqldb.jar")),
                new File(serverInstance.getServerHome() + File.separator + "bre" + File.separator + "lib" +
                        File.separator + "hsqldb.jar"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "observability/metrics/data.sql");
        String balFile = new File(RESOURCE_LOCATION + "metrics-test.bal").getAbsolutePath();
        serverInstance.setArguments(new String[]{balFile, "--observe"});
        serverInstance.startServer();
        addMetrics();
    }

    @Test
    public void testMetrics() throws Exception {
        // Test Service
        Assert.assertEquals(HttpClientRequest.doGet("http://localhost:9090/test").getData(), "Metric Test!");
        Assert.assertEquals(HttpClientRequest.doGet("http://localhost:9090/product").getData(),
                "[{\"PRODUCTID\":1,\"PRODUCTNAME\":\"WSO2-IAM\"},{\"PRODUCTID\":3,\"PRODUCTNAME\":\"WSO2-EI\"}]");

        // Send some requests
        int i = 0;
        while (i < 5) {
            HttpClientRequest.doGet("http://localhost:9090/test");
            HttpClientRequest.doGet("http://localhost:9090/product");
            i++;
        }

        // Read the metrics from the prometheus endpoint
        URL metricsEndPoint = new URL("http://localhost:9797/");
        BufferedReader reader = new BufferedReader(new InputStreamReader(metricsEndPoint.openConnection()
                .getInputStream()));
        reader.lines().filter(s -> !s.startsWith("#")).forEach(line -> {
            int index = line.lastIndexOf(" ");
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            Assert.assertTrue(expectedMetrics.get(key).matcher(value).find());
        });

        reader.close();
    }

    @AfterClass
    private void cleanup() throws Exception {
        serverInstance.stopServer();
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }

    private static void addMetrics() {
        Pattern regex = Pattern.compile("^-?\\d*\\.{0,1}\\d+$");
        expectedMetrics.put("http_requests_total{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",}", Pattern.compile("6.0"));
        expectedMetrics.put("http_requests_total{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",}", Pattern.compile("6.0"));
        expectedMetrics.put("ballerina_scheduler_excepted_worker_count", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_inprogress_requests{action=\"select\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_requests_total{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\",peer_address=\"jdbc:hsqldb:hsql://" +
                "localhost:9001/TEST_DB\",}", Pattern.compile("6.0"));
        expectedMetrics.put("ballerina_scheduler_running_worker_count", regex);
        expectedMetrics.put("http_response_time_seconds_max{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",}", regex);
        expectedMetrics.put("http_response_time_seconds_max{http_method=\"GET\",http_url=\"/product\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",quantile=\"0.5\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",quantile=\"0.75\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",quantile=\"0.98\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",quantile=\"0.99\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/test\",protocol=\"http\"," +
                "resource=\"test\",service=\"metricsTest\",quantile=\"0.999\",}", regex);
        expectedMetrics.put("http_response_time_seconds_count{http_method=\"GET\",http_url=\"/test\"," +
                "protocol=\"http\",resource=\"test\",service=\"metricsTest\",}", Pattern.compile("6.0"));
        expectedMetrics.put("http_response_time_seconds_sum{http_method=\"GET\",http_url=\"/test\"," +
                "protocol=\"http\",resource=\"test\",service=\"metricsTest\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.5\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.75\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.98\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.99\",}", regex);
        expectedMetrics.put("http_response_time_seconds{http_method=\"GET\",http_url=\"/product\",protocol=\"http\"," +
                "resource=\"getProduct\",service=\"metricsTest\",quantile=\"0.999\",}", regex);
        expectedMetrics.put("http_response_time_seconds_count{http_method=\"GET\",http_url=\"/product\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", Pattern.compile("6.0"));
        expectedMetrics.put("http_response_time_seconds_sum{http_method=\"GET\",http_url=\"/product\"," +
                "protocol=\"http\",resource=\"getProduct\",service=\"metricsTest\",}", regex);
        expectedMetrics.put("ballerina_scheduler_waiting_for_lock_worker_count", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_max{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.5\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.75\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.98\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.99\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds{action=\"select\",db_instance=\"\"," +
                "db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",quantile=\"0.999\",}", regex);
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_count{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", Pattern.compile("6.0"));
        expectedMetrics.put("ballerina_sql:CallerActions_response_time_seconds_sum{action=\"select\"," +
                "db_instance=\"\",db_statement=\"SELECT * FROM Products\",db_type=\"sql\"," +
                "peer_address=\"jdbc:hsqldb:hsql://localhost:9001/TEST_DB\",}", regex);
        expectedMetrics.put("ballerina_scheduler_waiting_for_response_worker_count", regex);
        expectedMetrics.put("ballerina_http:Connection_inprogress_requests{action=\"respond\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"0\",quantile=\"0.5\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"0\",quantile=\"0.75\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"0\",quantile=\"0.98\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"0\",quantile=\"0.99\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds{action=\"respond\"," +
                "http_status_code=\"0\",quantile=\"0.999\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_count{action=\"respond\"," +
                "http_status_code=\"0\",}", Pattern.compile("12.0"));
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_sum{action=\"respond\"," +
                "http_status_code=\"0\",}", regex);
        expectedMetrics.put("ballerina_http:Connection_response_time_seconds_max{action=\"respond\"," +
                "http_status_code=\"0\",}", regex);
        expectedMetrics.put("startup_time_milliseconds", regex);
        expectedMetrics.put("ballerina_scheduler_ready_worker_count", regex);
        expectedMetrics.put("ballerina_scheduler_paused_worker_count", regex);
        expectedMetrics.put("ballerina_http:Connection_requests_total{action=\"respond\",http_status_code=\"0\",}",
                Pattern.compile("12.0"));
        expectedMetrics.put("http_inprogress_requests{resource=\"test\",service=\"metricsTest\",}", regex);
        expectedMetrics.put("http_inprogress_requests{resource=\"getProduct\",service=\"metricsTest\",}", regex);
    }
}
