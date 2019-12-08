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
package org.ballerinalang.test.service.grpc.tool;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.protobuf.cmd.GrpcCmd;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.utils.BalFileGenerationUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.TMP_DIRECTORY_PATH;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Protobuf to bal generation function testcase.
 */
public class StubGeneratorTestCase {

    private static final String PACKAGE_NAME = ".";
    private static String protoExeName = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
    private Path resourceDir;
    private Path outputDirPath;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        resourceDir = Paths.get("src", "test", "resources", "grpc", "src", "tool").toAbsolutePath();
        outputDirPath = Paths.get(TMP_DIRECTORY_PATH, "grpc");
    }

    @Test
    public void testUnaryHelloWorld() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorld.proto", "helloWorld_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11,
                "Expected functions not found in compile results.");
        validatePublicAttachedFunctions(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test(description = "Test service stub generation for service definition with dependency")
    public void testUnaryHelloWorldWithDependency() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithDependency.proto",
                "helloWorldWithDependency_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 8);
        assertEquals(compileResult.getDiagnostics()[0].toString(),
                     "ERROR: .::helloWorldWithDependency_pb.bal:21:34:: unknown type 'HelloRequest'");
        assertEquals(compileResult.getDiagnostics()[1].toString(),
                     "ERROR: .::helloWorldWithDependency_pb.bal:21:90:: unknown type 'HelloResponse'");
        assertEquals(compileResult.getDiagnostics()[5].toString(),
                     "ERROR: .::helloWorldWithDependency_pb.bal:49:18:: unknown type 'ByeResponse'");
    }

    @Test(description = "Test service stub generation for service definition with enum messages")
    public void testUnaryHelloWorldWithEnum() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithEnum.proto", "helloWorldWithEnum_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 11,
                "Expected type definitions not found in compile results.");
        validateEnumNode(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 4,
                "Expected constants not found in compile results.");
        validateConstantNode(compileResult, "SENTIMENT_HAPPY");
        validateConstantNode(compileResult, "SENTIMENT_SAD");
        validateConstantNode(compileResult, "SENTIMENT_NEUTRAL");
    }

    @Test(description = "Test service stub generation for service definition with nested enum messages")
    public void testUnaryHelloWorldWithNestedEnum() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithNestedEnum.proto",
                "helloWorldWithNestedEnum_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 11,
                "Expected type definitions not found in compile results.");
        validateEnumNode(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 4,
                "Expected constants not found in compile results.");
        validateConstantNode(compileResult, "SENTIMENT_HAPPY");
        validateConstantNode(compileResult, "SENTIMENT_SAD");
        validateConstantNode(compileResult, "SENTIMENT_NEUTRAL");
    }

    @Test(description = "Test service stub generation for service definition with nested messages")
    public void testUnaryHelloWorldWithNestedMessage() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithNestedMessage.proto",
                "helloWorldWithNestedMessage_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 9,
                "Expected type definitions not found in compile results.");
    }

    @Test(description = "Test service stub generation for service definition with reserved names")
    public void testUnaryHelloWorldWithReservedNames() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithReservedNames.proto",
                "helloWorldWithReservedNames_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7,
                "Expected type definitions not found in compile results.");
    }

    @Test(description = "Test service stub generation for service definition with invalid dependency",
            expectedExceptions = BLangCompilerException.class,
            expectedExceptionsMessageRegExp = "cannot find file 'helloWorldWithInvalidDependency_pb.bal'")
    public void testUnaryHelloWorldWithInvalidDependency() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithInvalidDependency.proto",
                "helloWorldWithInvalidDependency_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 1);
    }

    @Test(description = "Test service stub generation tool for package service")
    public void testUnaryHelloWorldWithPackage() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithPackage.proto",
                "helloWorldWithPackage_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11,
                "Expected functions not found in compile results.");
        validatePublicAttachedFunctions(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test(description = "Test service stub generation tool command without specifying output directory path")
    public void testUnaryHelloWorldWithoutOutputPath() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path protoFilePath = resourceDir.resolve("helloWorld.proto");
        grpcCmd1.setProtoPath(protoFilePath.toAbsolutePath().toString());
        try {
            grpcCmd1.execute();
            Path sourceFileRoot = Paths.get("temp", "helloWorld_pb.bal");
            CompileResult compileResult = BCompileUtil.compile(sourceFileRoot.toAbsolutePath().toString());
            assertEquals(compileResult.getDiagnostics().length, 0);
            assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7,
                    "Expected type definitions not found in compile results.");
            assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11,
                    "Expected functions not found in compile results.");
            validatePublicAttachedFunctions(compileResult);
            assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                    "Expected global variables not found in compile results.");
            assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                    "Expected constants not found in compile results.");
            assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                    "Expected imports not found in compile results.");
        } finally {
            if (Paths.get("temp", "helloWorld_pb.bal").toFile().exists()) {
                BalFileGenerationUtils.delete(Paths.get("temp").toFile());
            }
        }
    }

    @Test
    public void testClientStreamingHelloWorld() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldClientStreaming.proto",
                "helloWorldClientStreaming_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 4,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5,
                "Expected functions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test
    public void testServerStreamingHelloWorld() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldServerStreaming.proto",
                "helloWorldServerStreaming_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 4,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5,
                "Expected functions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test
    public void testStandardDataTypes() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldString.proto",
                "helloWorldString_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 3,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5,
                "Expected functions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test
    public void testDifferentOutputPath() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path tempDirPath = outputDirPath.resolve("client");
        Path protoPath = Paths.get("helloWorld.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(tempDirPath.toAbsolutePath().toString());
        grpcCmd1.setProtoPath(protoRoot.toAbsolutePath().toString());
        grpcCmd1.execute();
        Path sourceFileRoot = Paths.get(TMP_DIRECTORY_PATH, "grpc", "client", "helloWorld_pb.bal");
        CompileResult compileResult = BCompileUtil.compile(sourceFileRoot.toString());
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11,
                "Expected functions not found in compile results.");
        validatePublicAttachedFunctions(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    @Test
    public void testServiceWithDescriptorMap() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCommand = (GrpcCmd) grpcCmd.newInstance();
        Path tempDirPath = outputDirPath.resolve("service");
        Path protoPath = Paths.get("helloWorld.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCommand.setBalOutPath(tempDirPath.toAbsolutePath().toString());
        grpcCommand.setProtoPath(protoRoot.toAbsolutePath().toString());
        grpcCommand.setMode("service");
        grpcCommand.execute();
        Path sampleServiceFile = Paths.get(TMP_DIRECTORY_PATH, "grpc", "service", "helloWorld_sample_service.bal");
        assertTrue(Files.exists(sampleServiceFile));
    }

    @Test
    public void testAutoGeneratedGrpcClient() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCommand = (GrpcCmd) grpcCmd.newInstance();
        Path tempDirPath = outputDirPath.resolve("client");
        Path protoPath = Paths.get("helloWorld.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCommand.setBalOutPath(tempDirPath.toAbsolutePath().toString());
        grpcCommand.setProtoPath(protoRoot.toAbsolutePath().toString());
        grpcCommand.setMode("client");
        grpcCommand.execute();
        Path sampleServiceFile = Paths.get(TMP_DIRECTORY_PATH, "grpc", "client", "helloWorld_sample_client.bal");
        assertTrue(Files.exists(sampleServiceFile));
    }

    @Test(description = "Test case for oneof field record generation")
    public void testOneofFieldRecordGeneration() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("oneof_field_service.proto",
                "oneof_field_service_pb.bal");
        assertEquals(compileResult.getDiagnostics().length, 0);
        assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 30,
                "Expected type definitions not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 30,
                "Expected functions not found in compile results.");
        validatePublicAttachedFunctions(compileResult);
        assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1,
                "Expected global variables not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1,
                "Expected constants not found in compile results.");
        assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 1,
                "Expected imports not found in compile results.");
    }

    private void validatePublicAttachedFunctions(CompileResult compileResult) {
        for (BLangFunction function : ((BLangPackage) compileResult.getAST()).functions) {
            if (function.attachedFunction) {
                assertTrue(function.getFlags().contains(Flag.PUBLIC), "Attached function " + function.getName() + "is" +
                        " not public");
            }
        }
    }

    private void validateEnumNode(CompileResult compileResult) {
        for (BLangTypeDefinition typeDefinition : ((BLangPackage) compileResult.getAST()).typeDefinitions) {
            if ("sentiment".equals(typeDefinition.getName().value)) {
                assertEquals(((BLangFiniteTypeNode) typeDefinition.getTypeNode()).getValueSet().size(), 3,
                        "Type definition doesn't contain required value set");
                return;
            }
        }
        fail("Type definition for " + "sentiment" + " doesn't exist in compiled results");
    }

    private void validateConstantNode(CompileResult compileResult, String constantName) {
        for (BLangConstant constant : ((BLangPackage) compileResult.getAST()).constants) {
            if (constantName.equals(constant.getName().getValue())) {
                assertEquals(constant.symbol.type.tsymbol.name.value, "sentiment",
                        "Symbol value of the constant is not correct");
                return;
            }
        }
        fail("constant for " + constantName + " doesn't exist in compiled results");
    }

    private CompileResult getStubCompileResult(String protoFilename, String outputFilename)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path protoFilePath = resourceDir.resolve(protoFilename);
        grpcCmd1.setBalOutPath(outputDirPath.toAbsolutePath().toString());
        grpcCmd1.setProtoPath(protoFilePath.toAbsolutePath().toString());
        grpcCmd1.execute();
        Path outputFilePath = outputDirPath.resolve(outputFilename);
        return BCompileUtil.compile(outputFilePath.toString());
    }

    @AfterClass
    public void clean() {
        BalFileGenerationUtils.delete(new File(TMP_DIRECTORY_PATH, protoExeName));
        BalFileGenerationUtils.delete(outputDirPath.toFile());
    }

}
