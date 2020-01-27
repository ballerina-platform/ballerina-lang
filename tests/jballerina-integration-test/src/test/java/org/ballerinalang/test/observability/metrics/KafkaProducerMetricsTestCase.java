/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Collect;
import io.debezium.util.Testing;
import kafka.server.KafkaConfig;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.MetricsTestUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Integration test for observability of metrics in Kafka producer.
 */
@Test(groups = "kafka-producer-metrics-test")
public class KafkaProducerMetricsTestCase extends BaseTest {
    private static BServerInstance serverInstance;
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerMetricsTestCase.class);
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private Map<String, String> expectedMetrics = new HashMap<>();

    /**
     * Initialize the Kafka cluster and run ballerina producer program with metrics collection enabled to do as follows.
     * Create two Kafka producers.
     * First producer publishes 6 messages to topic 't1'.
     * Second producer publishes 8 messages to topic 't2'.
     * @throws Exception
     */
    @BeforeGroups(value = "kafka-producer-metrics-test", alwaysRun = true)
    private void setup() throws Exception {
        File dataDir = Testing.Files.createTestingDirectory("cluster-kafka-producer-metrics-test");
        createKafkaCluster(dataDir, 14010, 14110).addBrokers(1).startup();
        serverInstance = new BServerInstance(balServer);
        String balFile = new File(RESOURCE_LOCATION + "kafka-producer-metrics-test.bal").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add("--" + ObservabilityConstants.CONFIG_METRICS_ENABLED + "=true");
        args.add("--b7a.log.console.loglevel=INFO");
        serverInstance.startServer(balFile, null, args.toArray(new String[args.size()]), new int[]{9090});
        addMetrics();
    }

    /**
     * Wait 20 seconds until the ballerina-kafka producer program has finished execution, and then connect to the
     * metrics endpoint. Iterate through the metrics list and check whether the metrics relevant to kafka-observability
     * is the same as expected.
     * @throws Exception
     */
    @Test
    public void testMetrics() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(20, TimeUnit.SECONDS);
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
            String metric = MetricsTestUtil.getMetricName(key);

            //It is possible for order of the tags to be randomized, in which case it cannot be compared against the
            //expected metrics using a map.
            //This block of used is necessary to re-organize the tags of a metric into a standardized order so that it
            //can be compared against the expected metrics.
            String clientId = MetricsTestUtil.getTag(key, "client_id");
            String url = MetricsTestUtil.getTag(key, "url");
            String topic = MetricsTestUtil.getTag(key, "topic");
            String context = MetricsTestUtil.getTag(key, "context");
            String[] tags = {clientId, url, topic, context};
            key = MetricsTestUtil.generateNewKey(metric, tags);

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

    @AfterGroups(value = "kafka-producer-metrics-test", alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.shutdownServer();
    }

    /**
     * Add expected metrics to a map to compare and verify against the actual values obtained.
     */
    private void addMetrics() {
        expectedMetrics.put("kafka_published_value{client_id=\"producer_1\",url=\"localhost:14110\",topic=\"t1\"," +
                                    "context=\"producer\",}", "6.0");
        expectedMetrics.put(
                "kafka_published_size_value{client_id=\"producer_1\",url=\"localhost:14110\",topic=\"t1\"," +
                        "context=\"producer\",}", "294.0");
        expectedMetrics.put("kafka_publishers_value{client_id=\"producer_1\",url=\"localhost:14110\"," +
                                    "context=\"producer\",}", "1.0");
        expectedMetrics.put("kafka_published_value{client_id=\"producer_2\",url=\"localhost:14110\",topic=\"t2\"," +
                                    "context=\"producer\",}", "8.0");
        expectedMetrics.put(
                "kafka_published_size_value{client_id=\"producer_2\",url=\"localhost:14110\",topic=\"t2\"," +
                        "context=\"producer\",}", "392.0");
        expectedMetrics.put("kafka_publishers_value{client_id=\"producer_2\",url=\"localhost:14110\"," +
                                    "context=\"producer\",}", "1.0");
    }

    private static KafkaCluster createKafkaCluster(File dataDir, int zkPort, int brokerPort) {
        String timeout = "20000";
        return new KafkaCluster()
                .usingDirectory(dataDir)
                .deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true)
                .withKafkaConfiguration(Collect.propertiesOf(KafkaConfig.ZkSessionTimeoutMsProp(), timeout))
                .withPorts(zkPort, brokerPort);
    }
}
