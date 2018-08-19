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
package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for proto file builder compiler plugin.
 */
@Test(groups = "grpc-test")
public class ProtoBuilderTestCase extends BaseTest {

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test compiler plugin for unary service with primitive params.")
    public void testUnaryServiceWithPrimitiveParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices", "unary_server1.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with header params.")
    public void testUnaryServiceWithHeaderParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices", "unary_service3.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for server streaming service.")
    public void testServerStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices",
                "server_streaming_service.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for client streaming service.")
    public void testClientStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices",
                "client_streaming_service.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertStreamingCompileResult(result);
    }

    @Test(description = "Test compiler plugin for bidirectional streaming service.")
    public void testBidirectionStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices",
                "bidirectional_streaming_service.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertStreamingCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with array field params.")
    public void testUnaryServiceWithArrayFieldParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices",
                "array_field_type_service.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with custom type params.")
    public void testUnaryServiceWithCustomTypeParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "grpcservices",
                "advanced_type_service.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        assertUnaryCompileResult(result);
    }

    private void assertUnaryCompileResult(CompileResult result) {
        Assert.assertEquals(result.getErrorCount(), 0, "Compilation errors in source file");
        Assert.assertEquals(result.getAST().getServices().size(), 1, "File should have one service defined.");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().size(), 1,
                "Service node should have one default annotation");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().get(0).getAnnotationName
                ().getValue(), "ServiceDescriptor");
    }

    private void assertStreamingCompileResult(CompileResult result) {
        Assert.assertEquals(result.getErrorCount(), 0, "Compilation errors in source file");
        Assert.assertEquals(result.getAST().getServices().size(), 1, "File should have one service defined.");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().size(), 2,
                "Service node should have one default annotation");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().get(0).getAnnotationName
                ().getValue(), "ServiceConfig");
        Assert.assertEquals(result.getAST().getServices().get(0).getAnnotationAttachments().get(1).getAnnotationName
                ().getValue(), "ServiceDescriptor");
    }
}
