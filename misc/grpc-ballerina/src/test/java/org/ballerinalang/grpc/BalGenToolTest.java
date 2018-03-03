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
package org.ballerinalang.grpc;

import org.ballerinalang.grpc.cmd.GrpcCmd;
import org.ballerinalang.grpc.cmd.OSDetector;
import org.ballerinalang.grpc.utils.BTestUtils;
import org.ballerinalang.grpc.utils.BalFileGenerationUtils;
import org.ballerinalang.grpc.utils.CompileResult;
import org.ballerinalang.util.codegen.ActionInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BalGenToolTest {
    private static Path resourceDir = Paths.get(
            BalGenToolTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    
    @Test
    public void testCMDForHelloWorld() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            ClassNotFoundException, InstantiationException, IOException {
        Class<?> grpcCmd = Class.forName("org.ballerinalang.grpc.cmd.GrpcCmd");
        GrpcCmd grpcCmd1 = (GrpcCmd) grpcCmd.newInstance();
        Path sourcePath = Paths.get("protoFiles");
        Path sourceRoot = resourceDir.resolve(sourcePath);
        Path protoPath = Paths.get("protoFiles/helloWorld.proto");
        Path protoRoot = resourceDir.resolve(protoPath);
        grpcCmd1.setBalOutPath(sourceRoot.toString());
        grpcCmd1.setProtoPath(protoRoot.toString());
        grpcCmd1.execute();
        Path sourceFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.blocking.pb.bal"));
        Path destFileRoot = resourceDir.resolve(Paths.get("protoFiles/helloWorld.pb.bal"));
        removePackage(sourceFileRoot.toString(), destFileRoot.toString());
        CompileResult compileResult = BTestUtils.compile("protoFiles/helloWorld.pb.bal");
        Assert.assertNotNull(compileResult.getProgFile()
                        .getPackageInfo(".").getConnectorInfo("helloWorldBlockingStub"),
                "Connector not found.");
        Assert.assertEquals(compileResult.getProgFile()
                        .getPackageInfo(".").getConnectorInfo("helloWorldBlockingStub")
                        .getActionInfoEntries().length, 3,
                "Invalid Number of Action");
        List<String> actionNameList = new ArrayList<>();
        for (ActionInfo actionInfo : compileResult.getProgFile()
                .getPackageInfo(".").getConnectorInfo("helloWorldBlockingStub")
                .getActionInfoEntries()) {
            actionNameList.add(actionInfo.getName());
        }
        compileResult.getProgFile()
                .getPackageInfo(".").getConnectorInfo("helloWorldBlockingStub")
                .getActionInfoEntries()[0].getName();
        Assert.assertEquals(actionNameList.contains("hello"), true,
                "Action 'hello' not found");
        Assert.assertEquals(actionNameList.contains("bye"), true,
                "Action 'bue' not found");
        String protoExeName = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
        BalFileGenerationUtils.delete(new File(protoExeName));
//        BalFileGenerationUtils.delete(new File("desc_gen"));
//        BalFileGenerationUtils.delete(new File("google"));
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
