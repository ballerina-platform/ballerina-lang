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

import org.ballerinalang.jvm.values.api.BError;
import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_DESERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Test cases for ballerina.net.kafka producer connector.
 */
public class ProducerFunctionsTest {

    private static final String dataDir = getDataDirectoryName(ProducerFunctionsTest.class.getSimpleName());

    private CompileResult result;
    private KafkaCluster kafkaCluster;

    private static final String topicField = "topic";
    private static final String partitionField = "partition";

    String topic = "producer-test-topic";
    String groupId = "producer-group";

    @BeforeTest(alwaysRun = true)
    public void setup() throws Throwable {
        String balFile = "producer_functions.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14011)
                .withBroker(PROTOCOL_PLAINTEXT, 14111, getZookeeperTimeoutProperty())
                .withConsumer(STRING_DESERIALIZER, STRING_DESERIALIZER, groupId, Collections.singletonList(topic))
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .withAdminClient(null)
                .start();
        kafkaCluster.createTopic(topic, 3, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_PRODUCER, balFile)));
    }

    @Test(description = "Test Basic produce")
    public void testKafkaProducer() {
        // TODO: Enable second message validation after fixing intermittent failure behaviour.
        String expectedMessage1 = "Hello World";
//        String expectedMessage2 = "Hello World 2";
        BValue[] returnValues = BRunUtil.invoke(result, "testKafkaProduce");
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertFalse(returnValues[0] instanceof BError);
        validateMessage(expectedMessage1);
//        validateMessage(expectedMessage2);
    }

    @Test(description = "Test Producer close() action",
          dependsOnMethods = {"testKafkaProducer", "testKafkaProducerFlushRecords", "testKafkaTopicPartitionRetrieval"},
          enabled = false
    )
    public void testClose() {
        String expMsg = "Failed to send data to Kafka server: Cannot perform operation after producer has been closed";
        BValue[] returnBValue = BRunUtil.invoke(result, "testKafkaClose");
        Assert.assertEquals(returnBValue.length, 1);
        Assert.assertTrue(returnBValue[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValue[0]), expMsg, "Error message not matched");
    }

    @Test(description = "Test producer flush function")
    public void testFlushRecords() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testFlush");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertFalse(returnBValues[0] instanceof BError);
    }

    @Test(description = "Test producer topic partition retrieval")
    public void testTopicPartitionRetrieval() {
        String topic1 = "partition-retrieval-topic-1";
        String topic2 = "partition-retrieval-topic-2";
        String topicNegative = "partition-retrieval-topic-negative";

        kafkaCluster.createTopic(topic1, 2, 1);
        kafkaCluster.createTopic(topic2, 5, 1);

        BValue[] inputBValues = {new BString(topic1)};
        BValue[] returnBValues = BRunUtil.invoke(result, "testPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 2);
        validateTopicPartition(returnBValues[0], topic1, 0);
        validateTopicPartition(returnBValues[1], topic1, 1);

        inputBValues = new BValue[]{new BString(topic2)};
        returnBValues = BRunUtil.invoke(result, "testPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 5);

        //negative test for the case where topic has not been created programmatically
        inputBValues = new BValue[]{new BString(topicNegative)};
        returnBValues = BRunUtil.invoke(result, "testPartitionInfoRetrieval", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        validateTopicPartition(returnBValues[0], topicNegative, 0);
        Assert.assertEquals(((BMap) returnBValues[0]).get(topicField).stringValue(), topicNegative);
    }

    private void validateMessage(String expectedMessage) {
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            String message = kafkaCluster.consumeMessage(2000);
            return expectedMessage.equals(message);
        });
    }

    private void validateTopicPartition(BValue value, String expectedTopic, int expectedPartition) {
        BMap topicPartitionRecord = (BMap) value;
        String topic = topicPartitionRecord.get(topicField).stringValue();
        long partition = ((BByte) topicPartitionRecord.get(partitionField)).intValue();
        Assert.assertEquals(topic, expectedTopic);
        Assert.assertEquals(partition, expectedPartition);

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
