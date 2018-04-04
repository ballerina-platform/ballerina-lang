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
package org.ballerinalang.test.net.grpc;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.protobuf.cmd.GrpcCmd;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.utils.BalFileGenerationUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Protobuff to bal generation function testcase.
 */
public class BalGenToolTest {
    private static final String PACKAGE_NAME = ".";
    private static String protoExeName = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
    private static Path resourceDir = Paths.get(
            BalGenToolTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    
    @Test(enabled = false)
    public void testUnaryHelloWorld() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, IOException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("protoFiles/helloWorld.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        grpcCmd1.execute();
        Path sourceFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.pb.bal"));
        Path destFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.gen.pb.bal"));
        removePackage(sourceFileRoot.toString(), destFileRoot.toString());
        CompileResult compileResult = BCompileUtil.compile(destFileRoot.toString());
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldBlockingClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldBlockingStub"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldBlockingStub.hello"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldStub.hello"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldBlockingStub.bye"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldStub.bye"), "Connector not found.");
        BalFileGenerationUtils.delete(new File(protoExeName));
    }
    
    @Test(enabled = false)
    public void testClientStreamingHelloWorld() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, IOException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("protoFiles/helloWorldClientStreaming.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        grpcCmd1.execute();
        Path sourceFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorldClientStreaming.pb.bal"));
        Path destFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorldClientStreaming.gen.pb.bal"));
        removePackage(sourceFileRoot.toString(), destFileRoot.toString());
        CompileResult compileResult = BCompileUtil.compile(destFileRoot.toString());
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldClientStreamingClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldClientStreamingStub"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldClientStreamingStub.LotsOfGreetings"),
                "Connector not found.");
        BalFileGenerationUtils.delete(new File(protoExeName));
    }
    
    @Test(enabled = false)
    public void testServerStreamingHelloWorld() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, IOException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("protoFiles/helloWorldServerStreaming.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        grpcCmd1.execute();
        Path sourceFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorldServerStreaming.pb.bal"));
        Path destFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorldServerStreaming.gen.pb.bal"));
        removePackage(sourceFileRoot.toString(), destFileRoot.toString());
        CompileResult compileResult = BCompileUtil.compile(destFileRoot.toString());
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldServerStreamingClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldServerStreamingStub"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldServerStreamingStub.lotsOfReplies"),
                "Connector not found.");
        BalFileGenerationUtils.delete(new File(protoExeName));
    }
    
    @Test(enabled = false)
    public void testStandardDataTypes() throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, IOException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("protoFiles/helloWorldString.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        grpcCmd1.execute();
        Path sourceFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.pb.bal"));
        Path destFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.gen.pb.bal"));
        removePackage(sourceFileRoot.toString(), destFileRoot.toString());
        CompileResult compileResult = BCompileUtil.compile(destFileRoot.toString());
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldBlockingClient"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getStructInfo("helloWorldBlockingStub"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldBlockingStub.hello"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldStub.hello"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldBlockingStub.bye"), "Connector not found.");
        Assert.assertNotNull(compileResult.getProgFile().getPackageInfo(PACKAGE_NAME)
                        .getFunctionInfo("helloWorldStub.bye"), "Connector not found.");
        BalFileGenerationUtils.delete(new File(protoExeName));
    }
    private void removePackage(String sourceFile, String destinationFile) throws IOException {
        File file = new File(destinationFile);
        file.createNewFile();
        File file2 = new File(sourceFile);
        Scanner fileScanner = new Scanner(file2);
        FileWriter fileStream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fileStream);
        fileScanner.nextLine();
        while (fileScanner.hasNextLine()) {
            String next = fileScanner.nextLine();
            if (next.equals("\n")) {
                out.newLine();
            } else {
                out.write(next);
            }
            out.newLine();
        }
        out.close();
    }
}
