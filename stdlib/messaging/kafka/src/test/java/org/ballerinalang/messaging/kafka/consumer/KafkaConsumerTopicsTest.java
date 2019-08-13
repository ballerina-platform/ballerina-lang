/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.kafka.consumer;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.net.kafka consumer for get list of available topics
 * using getAvailableTopics() native function.
 */
@Test(singleThreaded = true)
public class KafkaConsumerTopicsTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    private static final String TOPIC_TEST_1 = "test-1";
    private static final String TOPIC_TEST_2 = "test-2";
    private static final String TEST_MESSAGE = "test-message";

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile(getFilePath(Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_topics.bal")));
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
    }

    @Test(description = "Test Kafka getAvailableTopics function")
    public void testKafkaGetAvailableTopics() {
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_1, TEST_MESSAGE);
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_2, TEST_MESSAGE);
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopics");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), TOPIC_TEST_2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(1), TOPIC_TEST_1);
    }

    @Test(
            description = "Test Kafka getAvailableTopics with duration parameter",
            dependsOnMethods = "testKafkaGetAvailableTopics"
    )
    public void testKafkaGetAvailableTopicsWithDuration() {
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_1, TEST_MESSAGE);
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_2, TEST_MESSAGE);
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopicsWithDuration");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), TOPIC_TEST_2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(1), TOPIC_TEST_1);
    }

    @Test(
            description = "Test functionality of getAvailableTopics() function",
            dependsOnMethods = "testKafkaGetAvailableTopicsWithDuration"
    )
    public void testKafkaConsumerGetAvailableTopicsFromNoTimeoutConsumer () {
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_1, TEST_MESSAGE);
        produceToKafkaCluster(kafkaCluster, TOPIC_TEST_2, TEST_MESSAGE);
        BValue[] returnBValues = BRunUtil.invoke(result,
                "funcKafkaGetAvailableTopicsFromNoTimeoutConsumer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), TOPIC_TEST_2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(1), TOPIC_TEST_1);
    }


    @Test(
            description = "Test functionality of getTopicPartitions() function",
            dependsOnMethods = "testKafkaGetAvailableTopicsWithDuration"
    )
    @SuppressWarnings("unchecked")
    public void testKafkaConsumerGetTopicPartitions () {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        BMap<String, BValue> tpReturned = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(tpReturned.get("topic").stringValue(), TOPIC_TEST_1);
    }

    @AfterClass
    public void tearDown() {
        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails: delete on exit to allow subsequent test execution
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }

    private static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-get-available-topics-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(14008, 14108);
        return kafkaCluster;
    }
}
