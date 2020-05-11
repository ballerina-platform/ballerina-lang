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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.net.kafka consumer ( with manual commit enabled ) manual offset commit using commit() native
 * function.
 */
public class ManualCommitTest {
    private CompileResult result;
    private static KafkaCluster kafkaCluster;

    private static final String topic = "manual-commit-test-topic";
    private static final String message = "test-message";
    private static final String dataDir = getDataDirectoryName(ManualCommitTest.class.getName());

    @BeforeClass
    public void setup() throws Throwable {
        String balFile = "manual_commit.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14002)
                .withBroker(PROTOCOL_PLAINTEXT, 14102, getZookeeperTimeoutProperty())
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    // This test has to be a large single method to maintain the state of the consumer.
    @SuppressWarnings("unchecked")
    @Test(description = "Test Kafka consumer polling with manual offset commit")
    public void testKafkaConsumerPollWithManualOffsetCommit() throws ExecutionException, InterruptedException {
        produceToKafkaCluster(kafkaCluster, topic, message);
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });

        BValue[] returnBValues = BRunUtil.invoke(result, "testGetCommittedOffset");
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        Assert.assertEquals((returnBValues[0]).size(), 0, "Partitioned committed already.");

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        validatePositionOffset(returnBValues);

        BRunUtil.invoke(result, "testCommit");
        returnBValues = BRunUtil.invoke(result, "testGetCommittedOffset");
        Assert.assertNotNull(returnBValues[0]);
        validateCommittedOffset(returnBValues);

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        validatePositionOffset(returnBValues);

        returnBValues = BRunUtil.invoke(result, "testGetCommittedOffsetForNonExistingTopic");
        Assert.assertNotNull(returnBValues);
        Assert.assertEquals(returnBValues[0].size(), 0);

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffsetForNonExistingTopic");
        Assert.assertNotNull(returnBValues);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]),
                            "Failed to retrieve position offset: " +
                                    "You can only check the position for partitions assigned to this consumer.");
    }

    private static void validatePositionOffset(BValue[] returnBValues) {
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 10);
    }

    private static void validateCommittedOffset(BValue[] returnBValues) {
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        Assert.assertEquals(returnBValues.length, 1, "Committed partitions map is empty: ");
        Assert.assertEquals(((BMap) ((BMap) returnBValues[0]).get("partition")).get("topic").stringValue(), topic);
        Assert.assertEquals(((BInteger) ((BMap) (returnBValues[0])).get("offset")).intValue(), 10);
    }

    @AfterClass
    public void tearDown() throws IOException {
        finishTest(kafkaCluster, dataDir);
    }
}
