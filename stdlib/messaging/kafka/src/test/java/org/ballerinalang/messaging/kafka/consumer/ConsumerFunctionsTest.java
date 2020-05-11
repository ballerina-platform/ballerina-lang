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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
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

/**
 * Test cases for ballerina.kafka consumer native functions.
 */
@Test(singleThreaded = true)
public class ConsumerFunctionsTest {

    private CompileResult result;
    private static KafkaCluster kafkaCluster;

    private static final String topic = "consumer-functions-test-topic";
    private static final String dataDir = getDataDirectoryName(ConsumerFunctionsTest.class.getName());
    private static final String message = "test_string";

    @BeforeClass
    public void setup() throws Throwable {
        String balFile = "consumer_functions.bal";
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14001)
                .withBroker(PROTOCOL_PLAINTEXT, 14101, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        kafkaCluster.createTopic(topic, 1, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    @Test(description = "Checks Kafka consumer creation", groups = {"initial-tests"})
    public void testCreateConsumer() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testCreateConsumer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
    }

    @Test(description = "Test kafka consumer poll function", groups = {"initial-tests"})
    public void testKafkaPoll() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            kafkaCluster.sendMessage(topic, message);
        }
        await().atMost(5000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == 10);
        });
    }

    @Test(description = "Test Kafka getSubscription function", groups = {"initial-tests"})
    public void testKafkaConsumerGetSubscription() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetSubscription");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 1);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), topic);
    }

    @Test(description = "Test functionality of unsubscribe() function", groups = {"secondary-tests"})
    public void testKafkaConsumerUnsubscribe() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testTestUnsubscribe");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnBValues[0]).booleanValue());
    }

    @Test(description = "Test Kafka consumer close function", dependsOnGroups = {"initial-tests", "secondary-tests"})
    public void testKafkaConsumerClose() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testClose");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertFalse(returnBValues[0] instanceof BError);
    }

    @AfterClass
    public void tearDown() throws IOException {
        finishTest(kafkaCluster, dataDir);
    }
}
