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
 * Test cases for ballerina.net.kafka consumer ( with seek ) native functions.
 */
public class KafkaConsumerSeekTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile(getFilePath(Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_seek.bal")));
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-seek-test");
        kafkaCluster = createKafkaCluster(dataDir, 14004, 14104).addBrokers(1).startup();
        kafkaCluster.createTopic("test", 1, 1);
    }

    @Test(description = "Test Basic consumer with seek")
    @SuppressWarnings("unchecked")
    public void testKafkaConsumeWithSeek() {
        produceToKafkaCluster(kafkaCluster, "test", "test_string");
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });

        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 10);

        // Seek to offset 5
        BRunUtil.invoke(result, "funcKafkaSeekOffset");

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 5);

        returnBValues = BRunUtil.invoke(result, "funcKafkaBeginOffsets");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        BMap<String, BValue> off = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(((BInteger) off.get("offset")).intValue(), 0);

        returnBValues = BRunUtil.invoke(result, "funcKafkaEndOffsets");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        off = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(((BInteger) off.get("offset")).intValue(), 10);

        // Seek to beginning
        BRunUtil.invoke(result, "funcKafkaSeekToBegin");

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 0);

        // Seek to end
        BRunUtil.invoke(result, "funcKafkaSeekToEnd");

        returnBValues = BRunUtil.invoke(result, "funcKafkaGetPositionOffset");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertNotNull(returnBValues[0]);
        Assert.assertTrue(returnBValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnBValues[0]).intValue(), 10);
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
