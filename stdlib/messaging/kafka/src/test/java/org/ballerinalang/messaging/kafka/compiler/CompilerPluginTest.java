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

package org.ballerinalang.messaging.kafka.compiler;

import org.ballerinalang.messaging.kafka.utils.KafkaCluster;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.PROTOCOL_PLAINTEXT;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_DESERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.STRING_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_COMPILER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.finishTest;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getDataDirectoryName;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getZookeeperTimeoutProperty;

/**
 * Test cases for ballerina kafka service compiler plugin.
 */
public class CompilerPluginTest {

    private static final String dataDir = getDataDirectoryName(CompilerPluginTest.class.getSimpleName());
    private static final String consumerTopic = "consumer-with-additional-properties-topic";
    private static final String producerTopic = "producer-with-additional-properties-topic";

    private CompileResult compileResult;
    private static KafkaCluster kafkaCluster;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Throwable {
        String balFile = "consumer_with_additional_properties.bal";
        List<String> topics = Collections.singletonList(producerTopic);
        kafkaCluster = new KafkaCluster(dataDir)
                .withZookeeper(14091)
                .withBroker(PROTOCOL_PLAINTEXT, 14191, getZookeeperTimeoutProperty())
                .withAdminClient()
                .withProducer(STRING_SERIALIZER, STRING_SERIALIZER)
                .withConsumer(STRING_DESERIALIZER, STRING_DESERIALIZER, "java-consumer-group", topics)
                .start();
        kafkaCluster.createTopic(consumerTopic, 1, 1);
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
    }

    @Test(description = "Test endpoint bind to a service returning custom error type")
    public void testServiceValidateCustomErrorType() {
        String balFile = "custom_error_return_type_validation.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test(description = "Test endpoint bind to a service with invalid number of arguments in resource function")
    public void testServiceInvalidNumberOfArguments() {
        String msg = "Invalid number of input parameters found in resource onMessage";
        String balFile = "invalid_number_of_arguments.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(result, msg);
    }

    @Test(description = "Test kafka service with an invalid input parameter type")
    public void testServiceInvalidParameterType() {
        String msg = "Resource parameter ballerina/kafka:ConsumerConfiguration is invalid. " +
                "Expected: ballerina/kafka:ConsumerRecord[].";
        String balFile = "invalid_parameter_type.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(result, msg);
    }

    @Test(description = "Test kafka service with an invalid resource name")
    public void testServiceInvalidResourceName() {
        String msg = "Kafka service has invalid resource: onMessageReceived. Valid resource name:onMessage";
        String balFile = "invalid_resource_name.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(result, msg);
    }

    @Test(description = "Test endpoint bind to a service returning invalid return type")
    public void testServiceInvalidReturnType() {
        String msg = "invalid resource function return type 'int', expected a subtype of 'error?' containing '()'";
        String balFile = "invalid_return_type.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(result, msg);
    }

    @Test(description = "Test endpoint bind to a service with more than one resource")
    public void testServiceMoreThanOneResource() {
        String msg = "More than one resources found in Kafka service kafkaTestService. " +
                "Kafka Service should only have one resource";
        String balFile = "more_than_one_resource.bal";
        CompileResult result = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(result, msg);
    }


    @Test(description = "Test ballerina consumer with additional properties provided through a map")
    public void testConsumerWithAdditionalProperties() throws ExecutionException, InterruptedException {
        String balFile = "consumer_with_additional_properties.bal";
        String message = "Hello, Ballerina";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        kafkaCluster.sendMessage(consumerTopic, message);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] returnValues = BRunUtil.invoke(compileResult, "testConsumerWithAdditionalProperties");
            Assert.assertEquals(returnValues.length, 1);
            Assert.assertTrue(returnValues[0] instanceof BBoolean);
            return ((BBoolean) returnValues[0]).booleanValue();
        });
    }

    @Test(description = "Test ballerina producer with additional properties provided through a map")
    public void testProducerWithAdditionalProperties() throws ExecutionException, InterruptedException {
        String expectedMessage = "Hello, from Ballerina";
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testProducerWithAdditionalProperties");
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertFalse(returnValues[0] instanceof BError);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            String receivedMessage = kafkaCluster.consumeMessage(1000);
            return expectedMessage.equals(receivedMessage);
        });
    }

    private static void validateCompilerErrors(CompileResult compileResult, String expectedMessage) {
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].getMessage(), expectedMessage);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        finishTest(kafkaCluster, dataDir);
    }
}
