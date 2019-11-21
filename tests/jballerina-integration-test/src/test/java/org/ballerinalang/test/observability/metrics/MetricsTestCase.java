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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.SQLDBUtils;
import org.ballerinalang.test.util.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.util.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Integration test for observability of metrics.
 */
@Test(groups = "metrics-test")
public class MetricsTestCase extends BaseTest {
    private static BServerInstance serverInstance;

    private TestDatabase sqlServer;
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private static final String DB_NAME = "TEST_DB";
    private Map<String, Pattern> expectedMetrics = new HashMap<>();

    @BeforeGroups(value = "metrics-test", alwaysRun = true)
    private void setup() throws Exception {
        serverInstance = new BServerInstance(balServer);
        String dbScriptPath = Paths
                .get("observability", "metrics", "data.sql").toString();
        sqlServer = new FileBasedTestDatabase(SQLDBUtils.DBType.H2, dbScriptPath, SQLDBUtils.DB_DIRECTORY, DB_NAME);
        String balFile = new File(RESOURCE_LOCATION + "metrics-test.bal").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add("--" + ObservabilityConstants.CONFIG_METRICS_ENABLED + "=true");
        args.add("--" + CONFIG_TABLE_METRICS + ".statistic.percentiles=0.5, 0.75, 0.98, 0.99, 0.999");
        serverInstance.startServer(balFile, null, args.toArray(new String[args.size()]), new int[] { 9090 });
        addMetrics();
    }

    @Test
    public void testMetrics() throws Exception {
        // Test Service
        await().atMost(20, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> HttpClientRequest.doGet("http://localhost:9090/test")
                .getData().equals("productId=1 productName=WSO2-IAM productId=3 productName=WSO2-EI"));

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
        Assert.assertTrue(metricsList.size() != 0);
        int count = 0;
        for (String line : metricsList) {
            int index = line.lastIndexOf(" ");
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            Pattern pattern = expectedMetrics.get(key);
            if (pattern != null) {
                count++;
                Assert.assertTrue(pattern.matcher(value).find(),
                        "Unexpected value found for metric " + key + ". Value: " + value + ", Pattern: "
                                + pattern.pattern() + " Complete line: " + line);
            }
        }
        Assert.assertEquals(count, expectedMetrics.size(), "metrics count is not equal to the expected metrics count.");
        reader.close();
    }

    @AfterGroups(value = "metrics-test", alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.shutdownServer();
        sqlServer.stop();
    }

    private void addMetrics() {
        final Pattern regexNumber = Pattern.compile("[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?");
        expectedMetrics.put("ballerina_http_Caller_response_time_seconds_value" +
                "{action=\"respond\",http_status_code=\"200\",}", regexNumber);
        expectedMetrics.put("http_response_time_seconds_value{protocol=\"http\",http_url=\"/test\"," +
                "service=\"metricsTest_0\",resource=\"getProduct\",http_method=\"GET\",}", regexNumber);
        expectedMetrics.put("ballerinax_java_jdbc_Client_response_time_seconds_value{" +
                "action=\"select\",db_instance=\"h2\",db_type=\"sql\",peer_address=\"" +
                "jdbc:h2:file:../../tempdb/TEST_DB\",db_statement=\"SELECT * FROM Products\",}", regexNumber);
    }
}
