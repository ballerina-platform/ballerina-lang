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

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.values.BBoolean;
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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SERVICES;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina kafka consumer endpoint bind to a service .
 */
public class KafkaServiceTest {

    private CompileResult compileResult;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true).deleteDataUponShutdown(true).
                withKafkaConfiguration(prop).addBrokers(1).startup();
    }

    @Test(description = "Test endpoint bind to a service")
    public void testKafkaServiceEndpoint() {
        compileResult = BCompileUtil.compile(true,
                getFilePath(Paths.get(TEST_SRC, TEST_SERVICES, "kafka_service.bal")));
        String topic = "service-test";
        String message = "test_string";
        produceToKafkaCluster(kafkaCluster, topic, message);

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(compileResult, "funcKafkaGetResult");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BInteger);
                return (((BInteger) returnBValues[0]).intValue() == 10);
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test endpoint bind to a service")
    public void testKafkaAdvancedService() {
        compileResult = BCompileUtil.compile(true,
                getFilePath(Paths.get(TEST_SRC, TEST_SERVICES, "kafka_service_advanced.bal")));
        BRunUtil.invoke(compileResult, "funcKafkaProduce");

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invoke(compileResult, "funcKafkaGetResultText");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test kafka service stop() function")
    public void testKafkaServiceStop() {
        compileResult = BCompileUtil.compile(true,
                getFilePath(Paths.get(TEST_SRC, TEST_SERVICES, "kafka_service_stop.bal")));
        BRunUtil.invoke(compileResult, "funcKafkaProduce");
        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] results = BRunUtil.invoke(compileResult, "funcKafkaGetResult");
                Assert.assertEquals(results.length, 1);
                Assert.assertTrue(results[0] instanceof BBoolean);
                return ((BBoolean) results[0]).booleanValue();
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

    private static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer-service-test");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(14010, 14110);
        return kafkaCluster;
    }
}
