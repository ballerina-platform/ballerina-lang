/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.jms;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test JMS Listener Service Compilation.
 *
 * @since 0.995.0
 */
@Test(groups = {"jms-test"})
public class JmsCompilationTest {
    private static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src");
    private static final Path QUEUE_TEST_PATH = TEST_PATH.resolve("queue");
    private static final Path TOPIC_TEST_PATH = TEST_PATH.resolve("topic");

    @Test(description = "Successfully compiling JMS queue consumer service", enabled = false)
    public void testValidQueueConsumerService() {
        CompileResult compileResult = BCompileUtil.compile(QUEUE_TEST_PATH.resolve("jms_queue_success.bal")
                                                                   .toAbsolutePath()
                                                                   .toString());

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Successfully compiling JMS topic subscriber service", enabled = false)
    public void testValidTopicSubscriberService() {
        CompileResult compileResult = BCompileUtil.compile(TOPIC_TEST_PATH.resolve("jms_topic_success.bal")
                                                                   .toAbsolutePath()
                                                                   .toString());

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "More than expected number of resources in the service")
    public void testMoreResourcesInService() {
        CompileResult compileResult = BCompileUtil.compile(QUEUE_TEST_PATH.resolve("jms_more_resources.bal")
                                                                   .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Only one resource is allowed in the service", 28, 1);
    }

    @Test(description = "Resource returns can only be error or nil")
    public void testResourceReturn() {
        CompileResult compileResult = BCompileUtil.compile(QUEUE_TEST_PATH.resolve("jms_resource_return.bal")
                                                                   .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Invalid return type: expected error?", 27, 5);
    }

    @Test(description = "Resource without resource parameters")
    public void testNoResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                QUEUE_TEST_PATH.resolve("jms_no_resource_params.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                                          " parameter count = 2)", 27, 5);
    }

    @Test(description = "Resource with one resource parameter")
    public void testOneResourceParam() {
        CompileResult compileResult = BCompileUtil.compile(
                QUEUE_TEST_PATH.resolve("jms_one_resource_param.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                                          " parameter count = 2)", 27, 5);
    }

    @Test(description = "Resource with multiple resource parameters")
    public void testMultipleResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                QUEUE_TEST_PATH.resolve("jms_multiple_resource_params.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                                          " parameter count = 2)", 27, 5);
    }

    @Test(description = "Resource with invalid resource parameters")
    public void testDifferentResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                TOPIC_TEST_PATH.resolve("jms_different_resource_params.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                                  "Invalid resource signature for xyz resource: The first parameter should be a " +
                                          JmsConstants.TOPIC_SUBSCRIBER_CALLER_FULL_NAME, 27, 5);
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult) {
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
    }
}
