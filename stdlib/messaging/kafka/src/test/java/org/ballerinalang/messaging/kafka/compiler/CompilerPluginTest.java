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

import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_COMPILER;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.TestUtils.getResourcePath;

/**
 * Test cases for ballerina kafka service compiler plugin.
 */
public class CompilerPluginTest {
    private CompileResult compileResult;

    @Test(description = "Test endpoint bind to a service returning custom error type")
    public void testServiceValidateCustomErrorType() {
        String balFile = "custom_error_return_type_validation.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(description = "Test endpoint bind to a service with invalid number of arguments in resource function")
    public void testServiceInvalidNumberOfArguments() {
        String msg = "Invalid number of input parameters found in resource onMessage";
        String balFile = "invalid_number_of_arguments.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test kafka service with an invalid input parameter type")
    public void testServiceInvalidParameterType() {
        String msg = "Resource parameter ballerina/kafka:ConsumerConfiguration is invalid. " +
                "Expected: ballerina/kafka:ConsumerRecord[].";
        String balFile = "invalid_parameter_type.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test kafka service with an invalid resource name")
    public void testServiceInvalidResourceName() {
        String msg = "Kafka service has invalid resource: onMessageReceived. Valid resource name:onMessage";
        String balFile = "invalid_resource_name.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test endpoint bind to a service returning invalid return type")
    public void testServiceInvalidReturnType() {
        String msg = "invalid resource function return type 'int', expected a subtype of 'error?' containing '()'";
        String balFile = "invalid_return_type.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(compileResult, msg);
    }

    @Test(description = "Test endpoint bind to a service with more than one resource")
    public void testServiceMoreThanOneResource() {
        String msg = "More than one resources found in Kafka service kafkaTestService. " +
                "Kafka Service should only have one resource";
        String balFile = "more_than_one_resource.bal";
        compileResult = BCompileUtil.compile(getResourcePath(Paths.get(TEST_SRC, TEST_COMPILER, balFile)));
        validateCompilerErrors(compileResult, msg);
    }

    private static void validateCompilerErrors(CompileResult compileResult, String expectedMessage) {
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].getMessage(), expectedMessage);
    }
}
