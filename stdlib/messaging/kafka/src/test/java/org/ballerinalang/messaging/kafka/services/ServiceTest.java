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

package org.ballerinalang.messaging.kafka.services;

import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
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
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SERVICES;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Test cases for ballerina kafka consumer endpoint bind to a service .
 */
public class ServiceTest {

    private static final String dataDir = getDataDirectoryName(ServiceTest.class.getSimpleName());

    private CompileResult compileResult;
    private static KafkaCluster kafkaCluster;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        kafkaCluster = new KafkaCluster(dataDir, null)
                .withZookeeper(14041, null)
                .withBroker(PROTOCOL_PLAINTEXT, 14141, getZookeeperTimeoutProperty())
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .withAdminClient(null)
                .start();
    }

    @Test(description = "Test endpoint bind to a service")
    public void testSimpleService() throws ExecutionException, InterruptedException {
        String balFile = "simple_service.bal";
        compileResult = BCompileUtil.compileOffline(true, getResourcePath(Paths.get(TEST_SRC, TEST_SERVICES, balFile)));
        String topic = "service-test";
        String message = "test_string";
        kafkaCluster.createTopic(topic, 3, 1);
        for (int i = 0; i < 10; i++) {
            kafkaCluster.sendMessage(topic, message);
        }

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(compileResult, "testGetResult");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BInteger);
                return (((BInteger) returnBValues[0]).intValue() == 10);
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test endpoint bind to a service")
    public void testAdvancedService() {
        String balFile = "advanced_service.bal";
        compileResult = BCompileUtil.compileOffline(true, getResourcePath(Paths.get(TEST_SRC, TEST_SERVICES, balFile)));
        BRunUtil.invoke(compileResult, "testProduce");

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(compileResult, "testGetResultText");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test kafka service stop() function")
    public void testServiceStop() {
        String balFile = "stop_service.bal";
        compileResult = BCompileUtil.compileOffline(true, getResourcePath(Paths.get(TEST_SRC, TEST_SERVICES, balFile)));
        BRunUtil.invoke(compileResult, "testProduce");
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] results = BRunUtil.invoke(compileResult, "testGetResult");
                Assert.assertEquals(results.length, 1);
                Assert.assertTrue(results[0] instanceof BBoolean);
                return ((BBoolean) results[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
