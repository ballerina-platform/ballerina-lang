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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.KAFKA_BROKER_PORT;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.ZOOKEEPER_PORT_1;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.kafka consumer native functions.
 */
@Test(singleThreaded = true)
public class KafkaConsumerTest {

    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    private static final String TOPIC = "test";
    private static final String MESSAGE = "test_string";

    @BeforeClass
    public void setup() throws IOException {
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
        kafkaCluster.createTopic(TOPIC, 1, 1);
        result = BCompileUtil.compile(getFilePath("consumer/kafka_consumer.bal"));
    }

    @Test(description = "Checks Kafka consumer creation")
    public void testCreateConsumer() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaConnect");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
    }

    @Test(description = "Test kafka consumer poll function")
    public void testKafkaPoll() {
        produceToKafkaCluster(kafkaCluster, TOPIC, MESSAGE);
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });
    }

    @Test(description = "Test Kafka getSubscription function")
    public void testKafkaConsumerGetSubscription() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetSubscription");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 1);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), TOPIC);
    }

    @Test(description = "Test Kafka consumer close function")
    public void testKafkaConsumerClose() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaClose");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnBValues[0]).booleanValue());
    }

    @Test(description = "Test kafka consumer connect with no config values")
    public void testKafkaConsumerConnectNegative() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaConnectNegative");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        String errorReason = "{ballerina/kafka}ConsumerError";
        String errorMessage =
                "{message:\"Cannot connect to the kafka server: Failed to construct kafka consumer\", cause:()}";
        Assert.assertEquals(((BError) returnBValues[0]).getReason(), errorReason);
        Assert.assertEquals(((BError) returnBValues[0]).getDetails().stringValue(), errorMessage);
    }

    @Test(description = "Test functionality of unsubscribe() function", enabled = false)
    public void testKafkaConsumerUnsubscribe() {
        result = BCompileUtil.compileAndSetup("consumer/kafka_consumer_unsubscribe.bal");
        BValue[] inputBValues = {};
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaTestUnsubscribe", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnBValues[0]).booleanValue());
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
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(ZOOKEEPER_PORT_1, KAFKA_BROKER_PORT);
        return kafkaCluster;
    }

}
