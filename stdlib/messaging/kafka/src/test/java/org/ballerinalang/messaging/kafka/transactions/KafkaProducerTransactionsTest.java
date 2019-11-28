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

package org.ballerinalang.messaging.kafka.transactions;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
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
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_TRANSACTIONS;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.createKafkaCluster;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test cases for Kafka abortTransaction method on kafka producer.
 */
public class KafkaProducerTransactionsTest {

    private static File dataDir;
    protected static KafkaCluster kafkaCluster;
    private CompileResult result;

    @BeforeClass
    public void setup() throws IOException {
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-transaction-test");
        kafkaCluster = createKafkaCluster(dataDir, 14012, 14112).addBrokers(3).startup();
    }

    @Test(description = "Test Kafka producer send function within transaction")
    public void testKafkaSend() {
        result = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_TRANSACTIONS, "kafka_transactions_send.bal")));
        BValue[] inputBValues = {};
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaTransactionSendTest", inputBValues);

        try {
            await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test kafka producer commitConsumerOffsets() function")
    public void testKafkaCommitConsumerOffsetsTest() {
        result = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_TRANSACTIONS, "kafka_transactions_commit_consumer_offsets.bal")));
        BValue[] inputBValues = {};
        BRunUtil.invoke(result, "funcTestKafkaProduce", inputBValues);
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "funcTestKafkaCommitOffsets");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }

        try {
            await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "funcTestPollAgain");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test producer commit consumer functionality")
    public void testKafkaCommitConsumerTest() {
        result = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_TRANSACTIONS, "kafka_transactions_commit_consumer.bal")));
        BRunUtil.invoke(result, "funcTestKafkaProduce");
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "funcTestKafkaConsume");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean,
                        "Error returned from the function funcTestKafkaConsume.");
                return (((BBoolean) returnBValues[0]).booleanValue());
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }


    // TODO: Enable after the issue #19893 is fixed
    @Test(description = "Test transactional producer with idempotence false", enabled = false)
    public void testKafkaTransactionalProducerWithoutIdempotenceTest() {
        String message = "Failed to initialize the producer: configuration enableIdempotence must be set to true to " +
                "enable transactional producer";
        result = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_TRANSACTIONS, "transactional_producer_without_idempotence.bal")));
        BValue[] returnValues = BRunUtil.invoke(result, "funcKafkaCreateProducer");
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returnValues[0]).getDetails()).get("message").stringValue(), message);
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
