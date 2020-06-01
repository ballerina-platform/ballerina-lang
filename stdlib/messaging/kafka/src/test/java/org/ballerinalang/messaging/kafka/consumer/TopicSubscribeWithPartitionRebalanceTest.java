
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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.produceToKafkaCluster;

/**
 * Tests for ballerina Kafka subscribeWithPartitionRebalance function.
 */
@Test(singleThreaded = true)
public class TopicSubscribeWithPartitionRebalanceTest {

    private static final String dataDir = getDataDirectoryName(
            TopicSubscribeWithPartitionRebalanceTest.class.getSimpleName());

    private static KafkaCluster kafkaCluster;

    private static String topic1 = "rebalance-topic-1";
    private static String topic2 = "rebalance-topic-2";

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14007)
                .withBroker(PROTOCOL_PLAINTEXT, 14107, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
    }

    @Test(description = "Test functionality of subscribeWithPartitionRebalance() function")
    public void testSubscribeWithPartitionRebalance() {
        String balFile = "topic_subscribe_with_partition_rebalance.bal";
        CompileResult result = BCompileUtil.compileOffline(true, getResourcePath(
                Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
        BValue[] returnBValuesRevoked = BRunUtil.invoke(result, "testGetRebalanceInvokedPartitionsCount");
        Assert.assertEquals(returnBValuesRevoked.length, 1);
        Assert.assertTrue(returnBValuesRevoked[0] instanceof BInteger);
        long revokedPartitionCount = ((BInteger) returnBValuesRevoked[0]).intValue();

        BValue[] returnBValuesAssigned = BRunUtil.invoke(result, "testGetRebalanceAssignedPartitionsCount");
        Assert.assertEquals(returnBValuesAssigned.length, 1);
        Assert.assertTrue(returnBValuesAssigned[0] instanceof BInteger);
        long assignedPartitionCount = ((BInteger) returnBValuesAssigned[0]).intValue();
        Assert.assertEquals(revokedPartitionCount, -1);
        Assert.assertEquals(assignedPartitionCount, -1);

        kafkaCluster.createTopic(topic1, 3, 1);
        kafkaCluster.createTopic(topic2, 2, 1);

        await().atMost(40000, TimeUnit.MILLISECONDS).until(() -> {
            produceToKafkaCluster(kafkaCluster, "test", "test-message", 1);
            BValue[] revoked = BRunUtil.invoke(result, "testGetRebalanceInvokedPartitionsCount");
            Assert.assertEquals(revoked.length, 1);
            Assert.assertTrue(revoked[0] instanceof BInteger);
            long revokedCount = ((BInteger) revoked[0]).intValue();

            BValue[] assigned = BRunUtil.invoke(result, "testGetRebalanceAssignedPartitionsCount");
            Assert.assertEquals(assigned.length, 1);
            Assert.assertTrue(assigned[0] instanceof BInteger);
            long assignedCount = ((BInteger) assigned[0]).intValue();

            return (revokedCount == 1 && assignedCount == 5);
        });
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
