package org.ballerinalang.test.net.grpc;

import org.ballerinalang.protobuf.cmd.GrpcCmd;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.exception.BalGenToolException;
import org.ballerinalang.protobuf.utils.BalFileGenerationUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InvalidInputTypes {
    private static String protoExeName = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
    private static Path resourceDir = Paths.get(
            BalGenToolTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    
    @Test
    public void testInvalidProtoFileInput() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        String protoFileName = "protoFiles/helloWorld2.proto";
        Path protoPath = Paths.get(protoFileName);
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        try {
            grpcCmd1.execute();
        } catch (BalGenToolException e) {
            Assert.assertEquals(e.getMessage(), "Invalid proto file location. Please input valid proto " +
                    "file location.");
        }
    }
    
    @Test
    public void testInvalidProtoSyntax() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        String protoFileName = "protoFiles/helloWorldErrorSyntax.proto";
        Path protoPath = Paths.get(protoFileName);
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        try {
            grpcCmd1.execute();
        } catch (BalGenToolException e) {
            Assert.assertEquals(e.getMessage(), "\nhelloWorldErrorSyntax.proto:3:27: \"HelloRequest\" " +
                    "is not defined.");
        }
    }
    
    @AfterClass
    public void clean() {
        BalFileGenerationUtils.delete(new File(protoExeName));
    }
}
