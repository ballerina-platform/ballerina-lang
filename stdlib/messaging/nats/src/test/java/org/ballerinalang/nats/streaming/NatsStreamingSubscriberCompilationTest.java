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
package org.ballerinalang.nats.streaming;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests NATS streaming compiler plugin functionality.
 */
public class NatsStreamingSubscriberCompilationTest {
    private static final Path BASIC_TEST_SRC_PATH = Paths.get("src", "test", "resources", "test-src", "basic");
    private static final Path DATA_BINDING_TEST_SRC_PATH = Paths
            .get("src", "test", "resources", "test-src", "databinding");

    @Test(description = "Test valid service")
    public void testValidService() {
        CompileResult compileResult = compileBasicTests("nats_valid_subscriber.bal");
        assertDiagnosticCount(compileResult, 0);
    }

    @Test(description = "Missing annotation")
    public void testMissingAnnotation() {
        CompileResult compileResult = compileBasicTests("nats_missing_annotation.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "nats:StreamingSubscriptionConfig annotation is required to be declared in the subscription service",
                24, 1);

    }

    @Test(description = "Missing onMessage resource arguments")
    public void testMissingOnMessageArguments() {
        CompileResult compileResult = compileBasicTests("nats_missing_on_message_parameter.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for the onMessage resource function in testService service. "
                        + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                        + "second paramter (optional) type is "
                        + "byte[] | boolean | string | int | float | decimal | xml | json | record {}",
                29, 6);

    }

    @Test(description = "Missing invalid onMessage arguments")
    public void testInvalidOnMessageArguments() {
        CompileResult compileResult = compileBasicTests("nats_invalid_on_message_parameter.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for the onMessage resource function in testService service. "
                        + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                        + "second paramter (optional) type is "
                        + "byte[] | boolean | string | int | float | decimal | xml | json | record {}",
                28, 6);

    }

    @Test(description = "Missing onError resource arguments")
    public void testMissingOnErrorArguments() {
        CompileResult compileResult = compileBasicTests("nats_missing_on_error_parameters.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for the onError resource function in testService service. "
                        + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                        + "second paramter (required) type is error",
                32, 5);
    }

    @Test(description = "Missing invalid onError arguments")
    public void testInvalidOnErrorArguments() {
        CompileResult compileResult = compileBasicTests("nats_invalid_on_error_paramters.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for the onError resource function in testService service. "
                        + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                        + "second paramter (required) type is error",
                32, 5);
    }

    @Test(description = "Test valid data binding service")
    public void testValidDataBindingService() {
        CompileResult compileResult = compileDataBindingTests("nats_data_binding_valid.bal");
        assertDiagnosticCount(compileResult, 0);
    }

    @Test(description = "Test invalid databinding service")
    public void testInvalidDataBindingService() {
        CompileResult compileResult = compileDataBindingTests("nats_data_binding_unsupported_type.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for the onMessage resource function in testService service. "
                        + "Expected first parameter (required) type is nats:StreamingMessage and the expected "
                        + "second paramter (optional) type is "
                        + "byte[] | boolean | string | int | float | decimal | xml | json | record {}",
                28, 6);
    }

    @Test(description = "Test invalid connection URL")
    public void testInvalidURL() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src", "basic");
        try {
            BCompileUtil.compileOffline(testResourceRoot.resolve("nats_invalid_url.bal").toString());
        } catch (BLangRuntimeException e) {
            String actualMsg = e.getMessage();
            String expectedErrorMsg = "Bad server URL: /localhost:4222";
            Assert.assertTrue(actualMsg.contains(expectedErrorMsg));
        }
    }

    private CompileResult compileBasicTests(String fileName) {
        return BCompileUtil.compileOnJBallerina(BASIC_TEST_SRC_PATH.toAbsolutePath().toString(), fileName,
                false, false);
    }

    private CompileResult compileDataBindingTests(String fileName) {
        return BCompileUtil.compileOnJBallerina(DATA_BINDING_TEST_SRC_PATH.toAbsolutePath().toString(), fileName,
                false, false);
    }

    private void assertDiagnosticCount(CompileResult compileResult, int expectedDiagnosticCount) {
        Assert.assertEquals(compileResult.getDiagnostics().length, expectedDiagnosticCount);
    }
}
