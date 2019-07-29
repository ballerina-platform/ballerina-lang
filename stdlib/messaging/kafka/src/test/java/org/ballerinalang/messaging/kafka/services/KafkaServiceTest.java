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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.KAFKA_BROKER_PORT;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.ZOOKEEPER_PORT_1;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.produceToKafkaCluster;

/**
 * Test cases for ballerina kafka consumer endpoint bind to a service .
 */
public class KafkaServiceTest {

    private CompileResult compileResult;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;
    private static String topic = "service-test";
    private static String message = "test_string";

    @BeforeClass
    public void setup() throws IOException {
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
    }

    @Test(description = "Test endpoint bind to a service returning invalid return type")
    public void testKafkaServiceInvalidReturnType() {
        String msg = "Invalid return type for the resource function:Expected error? or subset of error? but found: int";
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_invalid_return_type.bal"));
        validateCompilerErrors(compileResult, 1, msg);
    }

    @Test(description = "Test kafka service with an invalid resource name")
    public void testKafkaServiceInvalidResourceName() {
        String msg = "Kafka service has invalid resource: onMessageReceived. Valid resource name:onMessage";
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_invalid_resource_name.bal"));
        validateCompilerErrors(compileResult, 1, msg);
    }

    @Test(description = "Test kafka service with an invalid input parameter type")
    public void testKafkaServiceInvalidParameterType() {
        String msg = "Resource parameter ballerina/kafka:ConsumerConfig is invalid. " +
                "Expected: ballerina/kafka:ConsumerRecord[].";
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_invalid_parameter_type.bal"));
        validateCompilerErrors(compileResult, 1, msg);
    }

    @Test(description = "Test endpoint bind to a service returning custom error type")
    public void testKafkaServiceValidateCustomErrorType() {
        compileResult = BCompileUtil.compile(
                getFilePath("services/kafka_service_custom_error_return_type_validation.bal"));
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(
            description = "Test endpoint bind to a service with no resources",
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*No resources found to handle the Kafka records in.*",
            enabled = false // Disabled this test as currently no resources will not handle by kafka compiler plugin
    )
    public void testKafkaServiceNoResources() {
        compileResult = BCompileUtil.compileAndSetup("services/kafka_service_no_resources.bal");
    }

    @Test(description = "Test endpoint bind to a service with more than one resource")
    public void testKafkaServiceMoreThanOneResource() {
        String msg = "More than one resources found in Kafka service kafkaTestService. " +
                "Kafka Service should only have one resource";
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_more_than_one_resource.bal"));
        validateCompilerErrors(compileResult, 1, msg);
    }

    @Test(description = "Test endpoint bind to a service with invalid number of arguments in resource function")
    public void testKafkaServiceInvalidNumberOfArguments() {
        String msg = "Invalid number of input parameters found in resource onMessage";
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_invalid_number_of_arguments.bal"));
        validateCompilerErrors(compileResult, 1, msg);
    }

    // TODO: Check the service implementation again for make disabled tests pass
    @Test(description = "Test kafka service stop() function", enabled = false)
    public void testKafkaServiceStop() {
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service_stop.bal"));
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

    @Test(description = "Test endpoint bind to a service returning valid return type", enabled = false)
    public void testKafkaServiceValidateReturnType() {
        compileResult = BCompileUtil.compile("services/kafka_service_validate_return_type.bal");
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

    @Test(description = "Test endpoint bind to a service", enabled = false)
    public void testKafkaAdvancedService() {
        compileResult = BCompileUtil.compile("services/kafka_service_advanced.bal");
        BRunUtil.invoke(compileResult, "funcKafkaProduce");

        try {
            await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
                BValue[] returnBValues = BRunUtil.invokeStateful(compileResult, "funcKafkaGetResultText");
                Assert.assertEquals(returnBValues.length, 1);
                Assert.assertTrue(returnBValues[0] instanceof BBoolean);
                return ((BBoolean) returnBValues[0]).booleanValue();
            });
        } catch (Throwable e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test endpoint bind to a service", enabled = false)
    public void testKafkaServiceEndpoint() {
        compileResult = BCompileUtil.compile(getFilePath("services/kafka_service.bal"));
        produceToKafkaCluster(kafkaCluster, topic, message);

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

    private static void validateCompilerErrors(CompileResult compileResult, int errorCount, String expectedMessage) {
        Assert.assertEquals(compileResult.getErrorCount(), errorCount);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].getMessage(), expectedMessage);
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
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(ZOOKEEPER_PORT_1, KAFKA_BROKER_PORT);
        return kafkaCluster;
    }
}
