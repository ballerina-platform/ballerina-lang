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
import org.ballerinalang.model.values.BInteger;
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

/**
 * Tests for ballerina kafka subscribeToPattern function.
 */
public class KafkaConsumerSubscribeToPatternTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-subscribe-to-pattern-test");
        kafkaCluster = createKafkaCluster(dataDir, 14006, 14106).addBrokers(1).startup();
    }

    @Test(description = "Test functionality of getAvailableTopics() function")
    public void testKafkaConsumerSubscribeToPattern() {
        result = BCompileUtil.compile(getFilePath(
                Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_subscribe_to_pattern.bal")));
        await().atMost(15000, TimeUnit.MILLISECONDS).until(() -> {
            // Unsubscribe from topics first
            BValue[] returnBValuesUnsubscribe = BRunUtil.invoke(result, "funcKafkaTestUnsubscribe");
            Assert.assertEquals(returnBValuesUnsubscribe.length, 1);
            Assert.assertNull(returnBValuesUnsubscribe[0]);

            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaTestGetSubscribedTopicCount");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            long topicCount = ((BInteger) returnBValues[0]).intValue();
            return (topicCount == 0);
        });

        kafkaCluster.createTopic("test1", 1, 1);
        kafkaCluster.createTopic("test2", 1, 1);
        kafkaCluster.createTopic("tester", 1, 1);
        kafkaCluster.createTopic("another-topic", 1, 1);

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BRunUtil.invoke(result, "funcKafkaTestSubscribeToPattern");
                BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaTestGetSubscribedTopicCount");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BInteger);
                long subscribedTopicCount = ((BInteger) returnBValues[0]).intValue();

                BValue[] returnBValuesAll = BRunUtil.invoke(result, "funcKafkaGetAvailableTopicsCount");
                Assert.assertEquals(returnBValuesAll.length, 1);
                Assert.assertTrue(returnBValuesAll[0] instanceof BInteger);
                long availableTopicCount = ((BInteger) returnBValuesAll[0]).intValue();
                return (subscribedTopicCount == 3 && availableTopicCount == 4);
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
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
