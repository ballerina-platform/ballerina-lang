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
import static org.ballerinalang.messaging.kafka.utils.TestUtils.deleteDirectory;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina.kafka consumer native functions.
 */
@Test(singleThreaded = true)
public class ConsumerFunctionsTest {

    private CompileResult result;
    private static KafkaCluster kafkaCluster;

    private static final String topic = "consumer-functions-test-topic";
    private static final String dataDir = getDataDirectoryName(ConsumerFunctionsTest.class.getSimpleName());
    private static final String message = "test message";

    @BeforeClass
    public void setup() throws Throwable {
        String balFile = "consumer_functions.bal";
        deleteDirectory(dataDir);
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14001)
                .withBroker(PROTOCOL_PLAINTEXT, 14101, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .start();
        kafkaCluster.createTopic(topic, 1, 1);
        result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_CONSUMER, balFile)));
    }

    @Test(
            description = "Checks Kafka consumer creation",
            groups = {"initializing-test"}
    )
    public void testCreateConsumer() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testCreateConsumer");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
    }

    @Test(
            description = "Test kafka consumer poll function",
            groups = {"first-tests"},
            dependsOnGroups = {"initializing-test"}
    )
    public void testPoll() throws ExecutionException, InterruptedException {
        int messageCount = 10;
        produceToKafkaCluster(kafkaCluster, topic, message, messageCount);
        // Test all the 10 messages retrieved from poll
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnBValues = BRunUtil.invoke(result, "testPoll");
            Assert.assertEquals(returnBValues.length, 1);
            Assert.assertTrue(returnBValues[0] instanceof BInteger);
            return (new Long(((BInteger) returnBValues[0]).intValue()).intValue() == messageCount);
        });

        // Test the message received successfully.
        BValue[] receivedMessage = BRunUtil.invoke(result, "getReceivedMessage");
        Assert.assertEquals(receivedMessage.length, 1);
        Assert.assertEquals(receivedMessage[0].stringValue(), message);
    }

    @Test(
            description = "Test Kafka getSubscription function",
            groups = {"secondary-tests"},
            dependsOnGroups = {"initializing-test", "first-tests"}
    )
    public void testGetSubscription() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testGetSubscription");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BValueArray);
        Assert.assertEquals((returnBValues[0]).size(), 1);
        Assert.assertEquals(((BValueArray) returnBValues[0]).getString(0), topic);
    }

    @Test(
            description = "Test functionality of unsubscribe() function",
            groups = {"secondary-tests"},
            dependsOnGroups = {"initializing-test", "first-tests"}
    )
    public void testUnsubscribe() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testTestUnsubscribe");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnBValues[0]).booleanValue());
    }

    @Test(
            description = "Test Kafka consumer close function",
            groups = {"final-tests"},
            dependsOnGroups = {"initializing-test", "first-tests", "secondary-tests"}
    )
    public void testClose() {
        BValue[] returnBValues = BRunUtil.invoke(result, "testClose");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertFalse(returnBValues[0] instanceof BError);
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
