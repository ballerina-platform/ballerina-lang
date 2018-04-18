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

package org.ballerinalang.test.net.websocket.compilation;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test WebSocket Service Compilation.
 */
public class WebSocketCompilationTest {
    @Test
    public void testSuccess() {
        CompileResult compileResult = BCompileUtil.compileAndSetup(
                "test-src/net/websocket/compilation/success.bal");
        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test
    public void testFailOnOpenOnIdle() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onOpen_onIdle.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onOpen in service echo: The first parameter should be a " +
                                    "ballerina.http:WebSocketListener");
        Assert.assertEquals(diag[1].getMessage(),
                            "Invalid resource signature for onIdleTimeout in service echo: Expected parameter count =" +
                                    " 1");
        Assert.assertEquals(diag[2].getMessage(),
                            "Invalid resource signature for onIdleTimeout in service echo: The first parameter should" +
                                    " be a ballerina.http:WebSocketListener");
    }

    @Test
    public void testFailOnTextParamCount() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onText_param_count.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onText in service echo: Unexpected parameter count");
    }

    @Test
    public void testFailOnTextInvalidParam() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onText_invalid_param.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onText in service echo: The second parameter should be a " +
                                    "string");
    }

    @Test
    public void testFailOnBinaryInvalidParam() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onBinary_invalid_param.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onBinary in service echo: The second parameter should be " +
                                    "a blob");
    }

    @Test
    public void testFailOnPingOnPong() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onPing_onPong.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onPing in service echo: The second parameter should be a " +
                                    "blob");
        Assert.assertEquals(diag[1].getMessage(),
                            "Invalid resource signature for onPong in service echo: Expected parameter count = 2");
        Assert.assertEquals(diag[2].getMessage(),
                            "Invalid resource signature for onPong in service echo: The second parameter should be a " +
                                    "blob");
    }

    @Test
    public void testFailOnClose() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/fail_onClose.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(),
                            "Invalid resource signature for onClose in service echo: The third parameter should be a " +
                                    "string");
    }

    @Test
    public void testInValidResource() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/net/websocket/compilation/invalid_resource.bal");
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag[0].getMessage(), "Invalid resource name onFind in service echo");
    }
}
