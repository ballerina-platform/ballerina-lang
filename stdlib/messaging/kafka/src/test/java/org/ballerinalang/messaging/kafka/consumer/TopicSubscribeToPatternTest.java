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
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Tests for ballerina kafka subscribeToPattern function.
 */
public class TopicSubscribeToPatternTest {

    private static final String dataDir = getDataDirectoryName(TopicSubscribeToPatternTest.class.getSimpleName());

    private static KafkaCluster kafkaCluster;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14006)
                .withBroker(PROTOCOL_PLAINTEXT, 14106, getZookeeperTimeoutProperty())
                .withAdminClient()
                .start();
    }

    @Test(description = "Test functionality of getAvailableTopics() function", enabled = false)
    public void testSubscribeToPattern() {
        String balFile = "topic_subscribe_to_pattern.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
        await().atMost(15000, TimeUnit.MILLISECONDS).until(() -> {
            // Unsubscribe from topics first
            BValue[] returnBValuesUnsubscribe = BRunUtil.invoke(result, "testUnsubscribe");
            Assert.assertEquals(returnBValuesUnsubscribe.length, 1);
            Assert.assertNull(returnBValuesUnsubscribe[0]);

            BValue[] returnBValues = BRunUtil.invoke(result, "testGetSubscribedTopicCount");
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
                BRunUtil.invoke(result, "testSubscribeToPattern");
                BValue[] returnBValues = BRunUtil.invoke(result, "testGetSubscribedTopicCount");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BInteger);
                long subscribedTopicCount = ((BInteger) returnBValues[0]).intValue();

                BValue[] returnBValuesAll = BRunUtil.invoke(result, "testGetAvailableTopicsCount");
                Assert.assertEquals(returnBValuesAll.length, 1);
                Assert.assertTrue(returnBValuesAll[0] instanceof BInteger);
                long availableTopicCount = ((BInteger) returnBValuesAll[0]).intValue();
                return (subscribedTopicCount == 3 && availableTopicCount == 4);
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
