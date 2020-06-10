/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.websocket;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test WebSocket Service Compilation.
 */
public class WebSocketCompilationTest {

    private static final String TEST_PATH = "test-src/websocket/";

    @Test(description = "Successfully compiling WebSocketService")
    public void testSuccessServer() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "success.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Successfully compiling WebSocketClientService")
    public void testSuccessClient() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "success_client.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Successfully compiling WebSocket upgrade resource")
    public void testSuccessWebSocketUpgrade() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "success_websocket_upgrade.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Invalid signature for onOpen and onIdle resources")
    public void testFailOnOpenOnIdle() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onOpen_onIdle.bal");

        assertExpectedDiagnosticsLength(compileResult, 4);
    }

    @Test(description = "Invalid parameter count for onText resource")
    public void testFailOnTextParamCount() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onText_param_count.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onText resource in service " +
                ": Unexpected parameter count", 26, 5);
    }

    @Test(description = "Invalid signature for onText resource with int")
    public void testFailOnTextInt() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onText.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onText resource in service : The second " +
                        "parameter should be a string, json, xml, byte[] or a record type", 21, 5);
    }

    @Test(description = "Invalid signature for onText resource with JSON and final fragment")
    public void testFailOnTextJSON() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onText_JSON.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onText resource in service : Final " +
                        "fragment is not valid if the second parameter is not a string", 21, 5);
    }

    @Test(description = "Invalid signature for onBinary resource")
    public void testFailOnBinary() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onBinary.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onBinary resource in service : The second " +
                        "parameter should be a byte[]", 27, 5);
    }

    @Test(description = "Invalid signature for onPing and onPong resources")
    public void testFailOnPingOnPong() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onPing_onPong.bal");

        assertExpectedDiagnosticsLength(compileResult, 3);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onPing resource in service : The second " +
                        "parameter should be a byte[]", 27, 5);
        BAssertUtil.validateError(compileResult, 1, "Invalid resource signature for onPong resource in service " +
                ": Expected parameter count = 2", 31, 5);
        BAssertUtil.validateError(compileResult, 2,
                "Invalid resource signature for onPong resource in service : The second " +
                        "parameter should be a byte[]", 31, 5);
    }

    @Test(description = "Invalid signature for onClose resource")
    public void testFailOnClose() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onClose.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onClose resource in service : The third parameter " +
                        "should be a string",
                26, 5);
    }

    @Test(description = "Invalid signature for onError resources")
    public void testFailOnError() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onError.bal");

        assertExpectedDiagnosticsLength(compileResult, 2);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource signature for onError resource in service " +
                ": Expected parameter count = 2", 27, 5);
        BAssertUtil.validateError(compileResult, 1, "Invalid resource signature for onError resource in service " +
                ": The second parameter should be an error", 27, 5);
    }

    @Test(description = "Invalid resource in WebSocketService")
    public void testInValidResource() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "invalid_resource.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Invalid resource name onFind in service ", 27, 5);
    }

    @Test(description = "Invalid resource onOpen in WebSocketClientService")
    public void testFailOnOpenClient() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_onOpen_client.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "onOpen resource is not supported for WebSocketClientService",
                22, 5);
    }

    @Test(description = "WebSocket upgrade resource config has a no upgradeService")
    public void testFailWebSocketUpgradeNoService() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "fail_websocket_upgrade_no_service.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0,
                "An upgradeService need to be specified for the WebSocket upgrade resource", 24, 5);
    }

    @Test(description = "Resource returns can only be error or nil")
    public void testResourceReturn() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "resource_return.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "invalid resource function return type 'int', expected a subtype " +
                "of 'error?' containing '()'", 21, 80);
    }

    @Test(description = "Service path cannot support path params")
    public void testServicePathParams() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "path_param_service.bal");

        assertExpectedDiagnosticsLength(compileResult, 1);
        BAssertUtil.validateError(compileResult, 0, "Path params are not supported in service path", 23, 11);
    }

    @Test(description = "Successfully compiling WebSocketFailoverClientService")
    public void testSuccessfailoverClient() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH + "success_failover_client.bal");

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "Invalid resource onOpen in WebSocketFailoverClientService")
    public void testFailOnOpenFailoverClient() {
        CompileResult compileResult = BCompileUtil.compileOnly(TEST_PATH +
                "fail_onBinary_failoverClient.bal");
        assertExpectedDiagnosticsLength(compileResult, 2);
        Assert.assertTrue(compileResult.toString().contains("Invalid resource signature for onBinary resource " +
                "in service : The first parameter should be a ballerina/http:1.0.0:WebSocketFailoverClient"));
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult, int expectedLength) {
        Assert.assertEquals(compileResult.getDiagnostics().length, expectedLength);
    }
}
