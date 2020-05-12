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

import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * Test cases for ballerina.net.kafka consumer for get list of available topics using getAvailableTopics() native
 * function.
 */
@Test(singleThreaded = true)
public class TopicsTest {
    private CompileResult result;
    private static KafkaCluster kafkaCluster;

    private static final String dataDir = getDataDirectoryName(TopicsTest.class.getSimpleName());

    private static final String topic1 = "test-topic-1";
    private static final String topic2 = "test-topic-2";
    private static final String topic3 = "test-topic-3";

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        String balFile = "topics.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14005)
                .withBroker(PROTOCOL_PLAINTEXT, 14105)
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        kafkaCluster.createTopic(topic1, 1, 1);
        kafkaCluster.createTopic(topic2, 1, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    @Test(description = "Test Kafka getAvailableTopics function")
    public void testGetAvailableTopics() throws ExecutionException, InterruptedException {
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetAvailableTopics");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        validateTopicsFromArray(returnBValues[0], 0, topic1);
        validateTopicsFromArray(returnBValues[0], 1, topic2);
    }

    @Test(description = "Test Kafka getAvailableTopics with duration parameter")
    public void testGetAvailableTopicsWithDuration() throws ExecutionException, InterruptedException {
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetAvailableTopicsWithDuration");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        validateTopicsFromArray(returnBValues[0], 0, topic1);
        validateTopicsFromArray(returnBValues[0], 1, topic2);
    }

    @Test(description = "Test functionality of getTopicPartitions() function")
    public void testGetTopicPartitions() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        validateTopicsFromMap(returnBValues[0], topic1);
    }

    @Test(description = "Test assign functions functionality")
    @SuppressWarnings("unchecked")
    public void testAssign() {
        // Invoke assign to assign topic partitions to the consumer
        BValue[] returnValues = BRunUtil.invoke(result, "testAssign");
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertFalse(returnValues[0] instanceof BError);

        // Check whether the partitions are assigned
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetAssignment");
        Assert.assertEquals(returnBValues.length, 1);
        validateTopicsFromMap(returnBValues[0], topic3);
    }

    private void validateTopicsFromArray(BValue result, int index, String topic) {
        Assert.assertEquals(result.size(), 2);
        BValueArray resultArray = (BValueArray) result;
        Assert.assertEquals(resultArray.getString(index), topic);
    }

    private void validateTopicsFromMap(BValue result, String topic) {
        BMap<String, BValue> topicMap = (BMap) result;
        Assert.assertEquals(topicMap.get("topic").stringValue(), topic);
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
