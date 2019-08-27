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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.createKafkaCluster;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.net.kafka consumer ( with manual commit enabled ) manual offset commit
 * using commit() native function.
 */
public class KafkaConsumerManualCommitTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    private static final String TOPIC = "test";
    private static final String MESSAGE = "test-message";

    @BeforeClass
    public void setup() throws IOException {
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-manual-commit-test");
        kafkaCluster = createKafkaCluster(dataDir, 14002, 14102).addBrokers(1).startup();
        result = BCompileUtil.compile(
                getFilePath(Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_manual_commit.bal")));
    }

    // This test has to be a large single method to maintain the state of the consumer.
    @SuppressWarnings("unchecked")
    @Test(description = "Test Kafka consumer polling with manual offset commit")
    public void testKafkaConsumerPollWithManualOffsetCommit() {
        produceToKafkaCluster(kafkaCluster, TOPIC, MESSAGE);
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });

        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetCommittedOffset");
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        Assert.assertEquals((returnBValues[0]).size(), 0, "Partitioned committed already.");

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        validatePositionOffset(returnBValues);

        BRunUtil.invoke(result, "funcKafkaCommit");
        returnBValues = BRunUtil.invoke(result, "funcKafkaGetCommittedOffset");
        Assert.assertNotNull(returnBValues[0]);
        validateCommittedOffset(returnBValues);

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        validatePositionOffset(returnBValues);

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetCommittedOffsetForNonExistingTopic");
        Assert.assertNotNull(returnBValues);
        Assert.assertEquals(returnBValues[0].size(), 0);

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffsetForNonExistingTopic");
        Assert.assertNotNull(returnBValues);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returnBValues[0]).getDetails()).get("message").stringValue(),
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
        Assert.assertEquals(((BMap) ((BMap) returnBValues[0]).get("partition")).get("topic").stringValue(), TOPIC);
        Assert.assertEquals(((BInteger) ((BMap) (returnBValues[0])).get("offset")).intValue(), 10);
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
}
