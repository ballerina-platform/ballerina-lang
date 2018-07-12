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
package org.ballerinalang.test.service.grpc.tool;

import org.ballerinalang.protobuf.cmd.GrpcCmd;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.exception.BalGenToolException;
import org.ballerinalang.protobuf.utils.BalFileGenerationUtils;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class used for test purposes related to invalid input types.
 */
public class InvalidServiceContractTestCase {

    private static String protoExeName = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
    private Path resourceDir;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        resourceDir = Paths.get(new File(InvalidServiceContractTestCase.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath()).getAbsolutePath());
    }

    @Test
    public void testInvalidProtoFileInput() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("grpc", "tool");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("grpc", "tool", "helloWorld2.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        try {
            grpcCmd1.execute();
        } catch (BalGenToolException e) {
            Assert.assertEquals(e.getMessage(), "Provided service proto file is not readable. Please input valid " +
                    "proto file location.");
        }
    }

    @Test
    public void testInvalidProtoSyntax() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.protobuf.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("grpc", "tool");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("grpc", "tool", "helloWorldErrorSyntax.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toAbsolutePath().toString());
        grpcCmd1.setProtoPath(protoRoot.toAbsolutePath().toString());
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
