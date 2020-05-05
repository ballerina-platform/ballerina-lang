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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_COMPILER;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test cases for ballerina kafka service compiler plugin.
 */
public class KafkaServiceCompilerTest {
    private CompileResult compileResult;

    @Test(description = "Test endpoint bind to a service returning invalid return type")
    public void testKafkaServiceInvalidReturnType() {
        String msg = "invalid resource function return type 'int', expected a subtype of 'error?' containing '()'";
        compileResult = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_COMPILER, "kafka_service_invalid_return_type.bal")));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test kafka service with an invalid resource name")
    public void testKafkaServiceInvalidResourceName() {
        String msg = "Kafka service has invalid resource: onMessageReceived. Valid resource name:onMessage";
        compileResult = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_COMPILER, "kafka_service_invalid_resource_name.bal")));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test kafka service with an invalid input parameter type")
    public void testKafkaServiceInvalidParameterType() {
        String msg = "Resource parameter ballerina/kafka:2.0.0:ConsumerConfiguration is invalid. " +
                "Expected: ballerina/kafka:2.0.0:ConsumerRecord[].";
        compileResult = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_COMPILER, "kafka_service_invalid_parameter_type.bal")));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test endpoint bind to a service returning custom error type")
    public void testKafkaServiceValidateCustomErrorType() {
        compileResult = BCompileUtil.compileOffline(getFilePath(Paths.get(TEST_SRC, TEST_COMPILER,
                "kafka_service_custom_error_return_type_validation.bal")));
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(
            description = "Test endpoint bind to a service with no resources",
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*No resources found to handle the Kafka records in.*",
            enabled = false // Disabled since, currently resources with no parameters will not handled by compiler
    )
    public void testKafkaServiceNoResources() {
        compileResult = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_COMPILER, "kafka_service_no_resources.bal")));
    }

    @Test(description = "Test endpoint bind to a service with more than one resource")
    public void testKafkaServiceMoreThanOneResource() {
        String msg = "More than one resources found in Kafka service kafkaTestService. " +
                "Kafka Service should only have one resource";
        compileResult = BCompileUtil.compileOffline(getFilePath(
                Paths.get(TEST_SRC, TEST_COMPILER, "kafka_service_more_than_one_resource.bal")));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test endpoint bind to a service with invalid number of arguments in resource function")
    public void testKafkaServiceInvalidNumberOfArguments() {
        String msg = "Invalid number of input parameters found in resource onMessage";
        compileResult = BCompileUtil.compileOffline(getFilePath(Paths.get(TEST_SRC, TEST_COMPILER,
                "kafka_service_invalid_number_of_arguments.bal")));
        validateCompilerErrors(compileResult, msg);
    }

    private static void validateCompilerErrors(CompileResult compileResult, String expectedMessage) {
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].getMessage(), expectedMessage);
    }
}
