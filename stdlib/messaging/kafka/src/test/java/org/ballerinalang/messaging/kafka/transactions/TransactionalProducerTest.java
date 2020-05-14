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

import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_TRANSACTIONS;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Test cases for Kafka abortTransaction method on kafka producer.
 */
public class TransactionalProducerTest {

    private static final String dataDir = getDataDirectoryName(TransactionalProducerTest.class.getSimpleName());

    private static KafkaCluster kafkaCluster;
    private CompileResult result;

    @BeforeTest(alwaysRun = true)
    public void setup() throws Throwable {
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14051)
                .withBroker(PROTOCOL_PLAINTEXT, 14151, getZookeeperTimeoutProperty())
                .start();
    }

    @Test(description = "Test Kafka producer send function within transaction")
    public void testSendFromTransactionalProducer() {
        String balFile = "transactional_send.bal";
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_TRANSACTIONS, balFile)));
        BValue[] returnBValues = BRunUtil.invoke(result, "testTransactionSendTest");

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
    public void testCommitConsumerOffsetsTest() {
        String balFile = "commit_consumer_offsets.bal";
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_TRANSACTIONS, balFile)));
        BRunUtil.invoke(result, "testProduce");
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "testCommitOffsets");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }

        try {
            await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "testPollAgain");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test producer commit consumer functionality")
    public void testCommitConsumerTest() {
        String balFile = "commit_consumer.bal";
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_TRANSACTIONS, balFile)));
        BRunUtil.invoke(result, "testProduce");
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(result, "testConsume");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return (((BBoolean) returnBValues[0]).booleanValue());
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test transactional producer with idempotence false")
    public void testTransactionalProducerWithoutIdempotenceTest() {
        String message = "Failed to initialize the producer: configuration enableIdempotence must be set to true to " +
                "enable transactional producer";
        String balFile = "transactional_producer_without_idempotence.bal";
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_TRANSACTIONS, balFile)));
        BValue[] returnValues = BRunUtil.invoke(result, "testCreateProducer");
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnValues[0]), message);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
