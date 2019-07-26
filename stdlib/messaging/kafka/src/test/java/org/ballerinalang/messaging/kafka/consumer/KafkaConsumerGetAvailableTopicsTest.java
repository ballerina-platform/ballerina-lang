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
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.KAFKA_BROKER_PORT;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.ZOOKEEPER_PORT_1;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test cases for ballerina.net.kafka consumer for get list of available topics
 * using getAvailableTopics() native function.
 */
@Test(singleThreaded = true)
public class KafkaConsumerGetAvailableTopicsTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile(getFilePath("consumer/kafka_consumer_get_available_topics.bal"));
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
        kafkaCluster.createTopic("test", 1, 1);
    }

    @Test(description = "Test Kafka getAvailableTopics function")
    public void testKafkaGetAvailableTopics() {
        produceToKafkaCluster();
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopics");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 1);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), "test");
    }

    @Test(description = "Test Kafka getAvailableTopics with duration parameter")
    public void testKafkaGetAvailableTopicsWithDuration() {
        produceToKafkaCluster();
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopicsWithDuration");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), "test-2");
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(1), "test");
    }

    @Test(description = "Test functionality of getAvailableTopics() function")
    public void testKafkaConsumerGetAvailableTopics () {
        BValue[] returnBValues = BRunUtil.invoke(result,
                "funcKafkaGetAvailableTopicsFromNoTimeoutConsumerWithDuration");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 2);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), "test-2");
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(1), "test");
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

    private static void produceToKafkaCluster() {
        CountDownLatch completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings("test", 10, completion::countDown, () -> "test_string");
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
    }

    private static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-get-available-topics-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(ZOOKEEPER_PORT_1, KAFKA_BROKER_PORT);
        return kafkaCluster;
    }
}
