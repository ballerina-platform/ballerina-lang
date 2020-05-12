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
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
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
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getErrorMessageFromReturnValue;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Test cases for ballerina.net.kafka consumer ( with Pause ) native functions.
 */
public class TopicPauseResumeTest {
    private CompileResult result;
    private static KafkaCluster kafkaCluster;
    private static final String topic = "consumer-pause-resume-test-topic";
    private static final String dataDir = getDataDirectoryName(TopicPauseResumeTest.class.getSimpleName());

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        String balFile = "topic_pause_resume.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14004)
                .withBroker(PROTOCOL_PLAINTEXT, 14104, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        kafkaCluster.createTopic(topic, 1, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    // This test has to be a large single method to maintain the state of the consumer.
    @Test(description = "Test Basic consumer with seek")
    @SuppressWarnings("unchecked")
    public void testPauseAndResume() throws ExecutionException, InterruptedException {
        // First poll to create a connection with the server. Otherwise pause and resume will fail.
        BRunUtil.invoke(result, "testPoll");

        BValue[] returnBValues = BRunUtil.invoke(result, "testGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 0);

        returnBValues = BRunUtil.invoke(result, "testPause");
        Assert.assertEquals(returnBValues.length, 1);
        if (returnBValues[0] != null) {
            Assert.assertNull(returnBValues[0], getErrorMessageFromReturnValue(returnBValues[0]));
        }

        returnBValues = BRunUtil.invoke(result, "testGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        BMap<String, BValue> tpReturned = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(tpReturned.get("topic").stringValue(), topic);
        Assert.assertEquals(((BByte) tpReturned.get("partition")).value().intValue(), 0);

        returnBValues = BRunUtil.invoke(result, "testResume");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNull(returnBValues[0]);

        returnBValues = BRunUtil.invoke(result, "testGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 0);

        returnBValues = BRunUtil.invoke(result, "testPauseInvalidTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]),
                            "Failed to pause topic partitions for the consumer: " +
                                    "No current assignment for partition test_negative-1000");

        returnBValues = BRunUtil.invoke(result, "testResumeInvalidTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(getErrorMessageFromReturnValue(returnBValues[0]),
                            "Failed to resume topic partitions for the consumer: " +
                                    "No current assignment for partition test_negative-1000");
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
