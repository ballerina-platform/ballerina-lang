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
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Integration test for observability of metrics in Kafka producer.
 */
@Test(groups = "kafka-metrics-test")
public class KafkaMetricsTestCase extends BaseTest {
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "metrics" + File.separator;
    private static KafkaCluster kafkaCluster;
    private static final String TOPIC1 = "t1";
    private static final String TOPIC2 = "t2";
    private static final String MESSAGE = "test message";
    List<String> args = new ArrayList<>();

    @BeforeGroups(value = "kafka-metrics-test", alwaysRun = true)
    private void setup() throws Exception {
        args.add("--" + ObservabilityConstants.CONFIG_METRICS_ENABLED + "=true");
        args.add("--b7a.log.console.loglevel=INFO");
        File dataDir = Testing.Files.createTestingDirectory("cluster-kafka-producer-metrics-test");
        kafkaCluster = createKafkaCluster(dataDir, 14010, 14110).addBrokers(1).startup();
        kafkaCluster.createTopics("t1", "t2", "t3", "t4");
    }

    @Test
    public void testProducerMetrics() throws Exception {
        BServerInstance serverInstance = startBalServer("kafka-producer-metrics-test.bal");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(20, TimeUnit.SECONDS);
        readAndValidateMetrics(getProducerMetrics());
        serverInstance.shutdownServer();
    }

    @Test(dependsOnMethods = {"testProducerMetrics"})
    public void testConsumerMetrics() throws Exception {
        BServerInstance serverInstance = startBalServer("kafka-consumer-metrics-test.bal");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(20, TimeUnit.SECONDS);
        produceToKafkaCluster(kafkaCluster, TOPIC1, MESSAGE);
        produceToKafkaCluster(kafkaCluster, TOPIC2, MESSAGE);
        countDownLatch = new CountDownLatch(1);
        countDownLatch.await(20, TimeUnit.SECONDS);
        readAndValidateMetrics(getConsumerMetrics());
        serverInstance.shutdownServer();
    }

    @Test(dependsOnMethods = {"testConsumerMetrics"})
    public void testErrorMetrics() throws Exception {
        BServerInstance serverInstance = startBalServer("kafka-error-metrics-test.bal");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(20, TimeUnit.SECONDS);
        readAndValidateMetrics(getErrorMetrics());
        serverInstance.shutdownServer();
    }

    @AfterGroups(value = "kafka-metrics-test", alwaysRun = true)
    private void cleanup() throws Exception {
        if (kafkaCluster.isRunning()) {
            kafkaCluster.shutdown();
        }
    }

    private BServerInstance startBalServer(String fileName) throws Exception {
        BServerInstance serverInstance = new BServerInstance(balServer);
        String balFile = new File(RESOURCE_LOCATION + fileName).getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add("--" + ObservabilityConstants.CONFIG_METRICS_ENABLED + "=true");
        args.add("--b7a.log.console.loglevel=INFO");
        serverInstance.startServer(balFile, null, args.toArray(new String[args.size()]), new int[]{9090});
        return serverInstance;
    }

    private void readAndValidateMetrics(Map<String, String> expectedMetrics) throws Exception {
        URL metricsEndPoint = new URL("http://localhost:9797/metrics");
        BufferedReader reader = new BufferedReader(new InputStreamReader(metricsEndPoint.openConnection()
                                                                                 .getInputStream()));
        List<String> metricsList = reader.lines().filter(s -> !s.startsWith("#")).collect(Collectors.toList());
        Assert.assertTrue(metricsList.size() != 0);
        int count = 0;
        for (String line : metricsList) {
            if (line.charAt(0) != 'k') {
                continue;
            }
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
            String errorType = MetricsTestUtil.getTag(key, "error_type");
            String context = MetricsTestUtil.getTag(key, "context");
            String[] tags = {context, url, topic, errorType, clientId};

            key = MetricsTestUtil.generateNewKey(metric, tags);
            String actualValue = expectedMetrics.get(key);
            if (actualValue != null) {
                count++;
                Assert.assertEquals(value, actualValue, "Unexpected value found for metric " + key + ".");
            }
        }
        Assert.assertEquals(count, expectedMetrics.size(), "metrics count is not equal to the expected metrics count.");
        reader.close();
    }

    private Map<String, String> getProducerMetrics() {
        Map<String, String> expectedMetrics = new HashMap<>();
        expectedMetrics.put("kafka_published_value{context=\"producer\",url=\"localhost:14110\",topic=\"t3\"," +
                                    "client_id=\"producer_1\",}", "6.0");
        expectedMetrics.put("kafka_published_value{context=\"producer\",url=\"localhost:14110\",topic=\"t4\"," +
                                    "client_id=\"producer_2\",}", "8.0");
        expectedMetrics.put("kafka_published_size_value{context=\"producer\",url=\"localhost:14110\",topic=\"t3\"," +
                                    "client_id=\"producer_1\",}", "294.0");
        expectedMetrics.put("kafka_publishers_value{context=\"producer\",url=\"localhost:14110\"," +
                                    "client_id=\"producer_1\",}", "1.0");
        expectedMetrics.put("kafka_published_size_value{context=\"producer\",url=\"localhost:14110\",topic=\"t4\"," +
                                    "client_id=\"producer_2\",}", "392.0");
        expectedMetrics.put("kafka_publishers_value{context=\"producer\",url=\"localhost:14110\"," +
                                    "client_id=\"producer_2\",}", "1.0");
        return expectedMetrics;
    }

    private Map<String, String> getConsumerMetrics() {
        Map<String, String> expectedMetrics = new HashMap<>();
        expectedMetrics.put("kafka_consumed_size_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t1\"," +
                                    "client_id=\"consumer_2\",}", "120.0");
        expectedMetrics.put("kafka_consumed_size_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t2\"," +
                                    "client_id=\"consumer_2\",}", "120.0");
        expectedMetrics.put("kafka_consumed_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t1\"," +
                                    "client_id=\"consumer_2\",}", "10.0");
        expectedMetrics.put("kafka_consumed_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t2\"," +
                                    "client_id=\"consumer_2\",}", "10.0");
        expectedMetrics.put("kafka_consumers_value{context=\"consumer\",url=\"localhost:14110\"," +
                                    "client_id=\"consumer_2\",}", "1.0");
        expectedMetrics.put("kafka_subscriptions_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t2\"," +
                                    "client_id=\"consumer_2\",}", "1.0");
        expectedMetrics.put("kafka_subscriptions_value{context=\"consumer\",url=\"localhost:14110\",topic=\"t1\"," +
                                    "client_id=\"consumer_2\",}", "1.0");
        return expectedMetrics;
    }

    private Map<String, String> getErrorMetrics() {
        Map<String, String> expectedMetrics = new HashMap<>();
        expectedMetrics.put("kafka_errors_value{context=\"producer\",url=\"localhost:14110\",error_type=\"publish\"," +
                                    "client_id=\"producer_1\",}", "1.0");
        return expectedMetrics;
    }

    private static KafkaCluster createKafkaCluster(File dataDir, int zkPort, int brokerPort) {
        String timeout = "20000";
        Properties properties = Collect.propertiesOf(KafkaConfig.ZkSessionTimeoutMsProp(), timeout);
        properties.setProperty(KafkaConfig.AutoCreateTopicsEnableProp(), "false");
        KafkaCluster kafkaCluster = new KafkaCluster()
                .usingDirectory(dataDir)
                .deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true)
                .withKafkaConfiguration(properties)
                .withPorts(zkPort, brokerPort);
        return  kafkaCluster;
    }

    private static void produceToKafkaCluster(KafkaCluster kafkaCluster, String topic, String message) {
        CountDownLatch completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings(topic, 10, completion::countDown, () -> message);
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
    }
}
