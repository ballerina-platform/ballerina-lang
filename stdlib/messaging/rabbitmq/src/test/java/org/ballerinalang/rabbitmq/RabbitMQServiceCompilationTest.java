/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.rabbitmq;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests RabbitMQ service compiler plugin functionality.
 */
public class RabbitMQServiceCompilationTest {
    private static final Path TEST_SRC_PATH = Paths.get("src", "test", "resources", "test-src");

    @Test(description = "Test valid consumer service")
    public void testValidService() {
        CompileResult compileResult = compileTests("valid_consumer.bal");
        assertDiagnosticCount(compileResult, 0);
    }

    @Test(description = "Missing service config annotation")
    public void testMissingAnnotation() {
        CompileResult compileResult = compileTests("missing_annotations.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                                  "There has to be a rabbitmq:ServiceConfig annotation declared for service", 23, 1);
    }

    @Test(description = "Invalid onMessage resource arguments")
    public void testInvalidOnMessage() {
        CompileResult compileResult = compileTests("invalid_on_message.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMessage resource: The first parameter " +
                                          "should be a rabbitmq:Message", 30, 5);

    }

    @Test(description = "Invalid onError resource arguments")
    public void testInvalidOnError() {
        CompileResult compileResult = compileTests("invalid_on_error.bal");
        assertDiagnosticCount(compileResult, 3);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onError resource in service . Expected first " +
                                          "parameter (required) type is rabbitmq:Message and the expected second " +
                                          "parameter (required) type is error", 33, 5);
    }

    @Test(description = "Invalid data-binding service")
    public void testInvalidDataBindingService() {
        CompileResult compileResult = compileTests("invalid_data_binding.bal");
        assertDiagnosticCount(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMessage resource in service : " +
                                          "The second parameter can only be an int, float, string, json, xml, byte[] " +
                                          "or a record type", 30, 5);
    }

    private CompileResult compileTests(String fileName) {
        return BCompileUtil.compileOnJBallerina(TEST_SRC_PATH.toAbsolutePath().toString(), fileName,
                                                false, false);
    }

    private void assertDiagnosticCount(CompileResult compileResult, int expectedDiagnosticCount) {
        Assert.assertEquals(compileResult.getDiagnostics().length, expectedDiagnosticCount);
    }
}
