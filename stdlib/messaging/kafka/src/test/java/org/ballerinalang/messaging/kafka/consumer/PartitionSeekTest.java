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
 * Test cases for ballerina.net.kafka consumer ( with seek ) native functions.
 */
public class PartitionSeekTest {
    private CompileResult result;
    private static KafkaCluster kafkaCluster;
    private static final String dataDir = getDataDirectoryName(PartitionSeekTest.class.getSimpleName());
    private static final String topic = "test";
    private static final String message = "test message";

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        String balFile = "partition_seek.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14003)
                .withBroker(PROTOCOL_PLAINTEXT, 14103, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        kafkaCluster.createTopic(topic, 1, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    @Test(description = "Test Basic consumer with seek")
    @SuppressWarnings("unchecked")
    public void testSeek() throws ExecutionException, InterruptedException {
        int messageCount = 10;
        produceToKafkaCluster(kafkaCluster, topic, message, messageCount);
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == messageCount);
        });

        BValue[] returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), messageCount);

        // Seek to offset 5
        BRunUtil.invoke(result, "testSeekOffset");

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 5);

        returnBValues = BRunUtil.invoke(result, "testBeginOffsets");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        BMap<String, BValue> off = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(((BInteger) off.get("offset")).intValue(), 0);

        returnBValues = BRunUtil.invoke(result, "testEndOffsets");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        off = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(((BInteger) off.get("offset")).intValue(), messageCount);

        // Seek to beginning
        BRunUtil.invoke(result, "testSeekToBegin");

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 0);

        // Seek to end
        BRunUtil.invoke(result, "testSeekToEnd");

        returnBValues = BRunUtil.invoke(result, "testGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 10);
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
