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

import org.ballerinalang.protobuf.cmd.GrpcCmd;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.utils.BalFileGenerationUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.TMP_DIRECTORY_PATH;

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
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
    }

    @Test(description = "Test service stub generation tool for package service")
    public void testUnaryHelloWorldWithPackage() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldWithPackage.proto", "helloWorld_pb.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
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
            Assert.assertEquals(compileResult.getDiagnostics().length, 0);
            Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7);
            Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11);
            Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
            Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
            Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
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
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 4);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
    }

    @Test
    public void testServerStreamingHelloWorld() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldServerStreaming.proto",
                "helloWorldServerStreaming_pb.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 4);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
    }

    @Test
    public void testStandardDataTypes() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        CompileResult compileResult = getStubCompileResult("helloWorldString.proto",
                "helloWorldString_pb.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 3);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 5);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
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
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 7);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 11);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
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
        Assert.assertTrue(Files.exists(sampleServiceFile));
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
        Assert.assertTrue(Files.exists(sampleServiceFile));
    }

    @Test(description = "Test case for oneof field record generation")
    public void testOneofFieldRecordGeneration() throws IllegalAccessException, ClassNotFoundException,
            InstantiationException {
        CompileResult compileResult = getStubCompileResult("oneof_field_service.proto",
                "oneof_field_service_pb.bal");
        Assert.assertEquals(compileResult.getDiagnostics().length, 0);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).typeDefinitions.size(), 30);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).functions.size(), 30);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).globalVars.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).constants.size(), 1);
        Assert.assertEquals(((BLangPackage) compileResult.getAST()).imports.size(), 2);
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
