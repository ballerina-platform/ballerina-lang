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
import org.ballerinalang.model.values.BByte;
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
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.net.kafka consumer ( with Pause ) native functions.
 */
public class KafkaConsumerPauseTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile(getFilePath(Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_pause.bal")));
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true).deleteDataUponShutdown(true)
                .addBrokers(1).startup();
        kafkaCluster.createTopic("test", 1, 1);
    }

    // This test has to be a large single method to maintain the state of the consumer.
    @Test(description = "Test Basic consumer with seek")
    @SuppressWarnings("unchecked")
    public void testKafkaConsumeWithPause() {
        produceToKafkaCluster(kafkaCluster, "test", "test_string");
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });

        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 0);

        returnBValues = BRunUtil.invoke(result, "funcKafkaPause");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNull(returnBValues[0]);

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        BMap<String, BValue> tpReturned = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(tpReturned.get("topic").stringValue(), "test");
        Assert.assertEquals(((BByte) tpReturned.get("partition")).value().intValue(), 0);

        returnBValues = BRunUtil.invoke(result, "funcKafkaResume");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNull(returnBValues[0]);

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPausedPartitions");
        Assert.assertEquals(returnBValues.length, 0);

        returnBValues = BRunUtil.invoke(result, "funcKafkaPauseInvalidTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returnBValues[0]).getDetails()).get("message").stringValue(),
                "Failed to pause topic partitions for the consumer: " +
                        "No current assignment for partition test_negative-1000");

        returnBValues = BRunUtil.invoke(result, "funcKafkaResumeInvalidTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returnBValues[0]).getDetails()).get("message").stringValue(),
                "Failed to resume topic partitions for the consumer: " +
                        "No current assignment for partition test_negative-1000");
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

    private static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-pause-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(14003, 14103);
        return kafkaCluster;
    }
}
