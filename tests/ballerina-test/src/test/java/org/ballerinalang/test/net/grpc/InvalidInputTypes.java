/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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

/**
 * Class used for test purposes related to invalid input types.
 */
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
