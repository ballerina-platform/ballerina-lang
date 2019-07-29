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

package org.ballerinalang.messaging.kafka.producer;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.KAFKA_BROKER_PORT;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.ZOOKEEPER_PORT_1;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test cases for ballerina.net.kafka producer connector.
 */
public class KafkaProducerTest {

    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile(getFilePath("producer/kafka_producer.bal"));
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
    }

    @Test(description = "Test Basic produce", enabled = false)
    public void testKafkaProducer() {
        String topic = "producer-test-topic";
        BRunUtil.invoke(result, "funcTestKafkaProduce");

        final CountDownLatch completion = new CountDownLatch(1);
        final AtomicLong messagesRead = new AtomicLong(0);

        kafkaCluster.useTo().consumeStrings(topic, 2, 10, TimeUnit.SECONDS, completion::countDown, (key, value) -> {
            messagesRead.incrementAndGet();
            return true;
        });
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
        Assert.assertEquals(messagesRead.get(), 2);
    }

    @Test(description = "Test Producer close() action")
    public void testKafkaProducerClose() {
        String expMsg = "Failed to send data to Kafka server: Cannot perform operation after producer has been closed";
        BValue[] returnBValue = BRunUtil.invoke(result, "funcTestKafkaClose");
        Assert.assertEquals(returnBValue.length, 1);
        Assert.assertTrue(returnBValue[0] instanceof BString);
        Assert.assertEquals((returnBValue[0]).stringValue(), expMsg, "Error message not matched");
    }

    @Test(description = "Test producer flush function")
    public void testKafkaProducerFlushRecords() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaTestFlush");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnBValues[0]).booleanValue());
    }

    @Test(description = "Test producer topic partition retrieval")
    public void testKafkaTopicPartitionRetrieval() {
        String topic1 = "partition-retrieval-topic-1";
        String topic2 = "partition-retrieval-topic-2";
        String topicNegative = "partition-retrieval-topic-negative";

        kafkaCluster.createTopic(topic1, 2, 1);
        kafkaCluster.createTopic(topic2, 5, 1);

        BValue[] inputBValues = {new BString(topic1)};
        BValue[] returnBValues = BRunUtil.invoke(result, "funcTestPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 2);
        Assert.assertEquals(((BMap) returnBValues[0]).get("topic").stringValue(), topic1);
        Assert.assertEquals(((BByte) ((BMap) returnBValues[0]).get("partition")).intValue(), 1);
        Assert.assertEquals(((BMap) returnBValues[1]).get("topic").stringValue(), topic1);
        Assert.assertEquals(((BByte) ((BMap) returnBValues[1]).get("partition")).intValue(), 0);

        inputBValues = new BValue[]{new BString(topic2)};
        returnBValues = BRunUtil.invoke(result, "funcTestPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 5);

        //negative test for the case where topic has not been created programmatically
        inputBValues = new BValue[]{new BString(topicNegative)};
        returnBValues = BRunUtil.invoke(result, "funcTestPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertEquals(((BMap) returnBValues[0]).get("topic").stringValue(), topicNegative);
        Assert.assertEquals(((BByte) ((BMap) returnBValues[0]).get("partition")).intValue(), 0);
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
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-producer-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(ZOOKEEPER_PORT_1, KAFKA_BROKER_PORT);
        return kafkaCluster;
    }

}
