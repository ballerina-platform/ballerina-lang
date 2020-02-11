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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.net.grpc.proto.definition.StandardDescriptorBuilder;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.net.grpc.ServicesBuilderUtils.hexStringToByteArray;

/**
 * Test class for proto file builder compiler plugin.
 */
@Test(groups = "grpc-test")
public class ProtoBuilderTestCase extends GrpcBaseTest {

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test compiler plugin for unary service with primitive params.")
    public void testUnaryServiceWithPrimitiveParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "07_unary_server.bal", false, false);
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with header params.")
    public void testUnaryServiceWithHeaderParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "08_unary_service_with_headers.bal", false, false);
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for server streaming service.")
    public void testServerStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "06_server_streaming_service.bal", false, false);
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for client streaming service.")
    public void testClientStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "04_client_streaming_service.bal", false, false);
        assertStreamingCompileResult(result);
    }

    @Test(description = "Test compiler plugin for bidirectional streaming service.")
    public void testBidirectionStreamingService() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "03_bidirectional_streaming_service.bal", false, false);
        assertStreamingCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with array field params.")
    public void testUnaryServiceWithArrayFieldParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "02_array_field_type_service.bal", false, false);
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with custom type params.")
    public void testUnaryServiceWithCustomTypeParams() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "grpcservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "01_advanced_type_service.bal", false, false);
        assertUnaryCompileResult(result);
    }

    @Test(description = "Test compiler plugin for unary service with resource annotation.")
    public void testUnaryServiceWithResourceAnnotation() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "errorservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "unary_service_with_annotation.bal", false, false);
        assertUnaryCompileResult(result);
        Descriptors.FileDescriptor fileDescriptor = getDescriptor(result.getAST().getServices().get(0)
                .getAnnotationAttachments().get(0).getExpression());
        Assert.assertNotNull(fileDescriptor);
        Assert.assertEquals(fileDescriptor.getServices().size(), 1);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.getServices().get(0);
        Assert.assertEquals(serviceDescriptor.findMethodByName("hello").getOutputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("hello").getInputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testInt").getOutputType().getName(),
                "Int64Value");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testInt").getInputType().getName(),
                "Int64Value");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testFloat").getOutputType().getName(),
                "FloatValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testFloat").getInputType().getName(),
                "FloatValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testBoolean").getOutputType().getName(),
                "BoolValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testBoolean").getInputType().getName(),
                "BoolValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testStruct").getOutputType().getName(),
                "Response");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testStruct").getInputType().getName(),
                "Request");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testNoRequest").getOutputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testNoRequest").getInputType().getName(),
                "Empty");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testNoResponse").getOutputType().getName(),
                "Empty");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testNoResponse").getInputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testInputNestedStruct").getOutputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("testInputNestedStruct").getInputType().getName(),
                "Person");
    }

    @Test(description = "Test compiler plugin for service with checked expression.")
    public void testServiceWithCheckedExpr() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "errorservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "service_with_checkedexpr.bal", false, false);
        assertUnaryCompileResult(result);
        Descriptors.FileDescriptor fileDescriptor = getDescriptor(result.getAST().getServices().get(0)
                .getAnnotationAttachments().get(0).getExpression());
        Assert.assertNotNull(fileDescriptor);
        Assert.assertEquals(fileDescriptor.getServices().size(), 1);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.getServices().get(0);
        Assert.assertEquals(serviceDescriptor.findMethodByName("greet").getOutputType().getName(),
                "StringValue");
    }

    @Test(description = "Test compiler plugin for streaming service with resource annotation.")
    public void testStreamingServiceWithResourceAnnotation() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "src", "errorservices");
        CompileResult result = BCompileUtil.compileOnJBallerina(balFilePath.toAbsolutePath().toString(),
                "streaming_service_with_annotation.bal", false, false);
        assertStreamingCompileResult(result);
        Descriptors.FileDescriptor fileDescriptor = getDescriptor(result.getAST().getServices().get(0)
                .getAnnotationAttachments().get(1).getExpression());
        Assert.assertNotNull(fileDescriptor);
        Assert.assertEquals(fileDescriptor.getServices().size(), 1);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.getServices().get(0);
        Assert.assertEquals(serviceDescriptor.findMethodByName("chat").getOutputType().getName(),
                "StringValue");
        Assert.assertEquals(serviceDescriptor.findMethodByName("chat").getInputType().getName(),
                "ChatMessage");
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

    private static Descriptors.FileDescriptor getDescriptor(ExpressionNode node) {
        try {
            BLangLiteral valueLiteral = (BLangLiteral)
                    ((BLangRecordLiteral.BLangRecordKeyValueField) ((BLangRecordLiteral) node).fields.get(0)).valueExpr;
            if (valueLiteral == null) {
                Assert.fail("Couldn't find the service descriptor.");
            }
            String descriptorData = (String) valueLiteral.value;
            byte[] descriptor = hexStringToByteArray(descriptorData);
            DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
            return Descriptors.FileDescriptor.buildFrom(proto,
                    StandardDescriptorBuilder.getFileDescriptors(proto.getDependencyList().toArray()));
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            Assert.fail("Error while reading the service proto descriptor.");
        }
        return null;
    }
}
