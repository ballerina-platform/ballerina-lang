/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging.grpc;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstanceOld;
import org.ballerinalang.test.context.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test packaged gRPC service with nested ballerina record type for input and output.
 */
public class ServicePackagingTestCase {

    private Path tempProjectDirectory;
    private String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);

    @BeforeClass()
    public void setup() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    }

    @Test(description = "Test packaged service with nested ballerina record type for input and output")
    public void testNestedMessageType() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("pkg1");
        Files.createDirectories(projectPath);

        // perform ballerina init and copy grpc service to the project.
        ServerInstanceOld ballerinaBuildServer = new ServerInstanceOld(serverZipPath);
        String[] args = {"-i"};
        String[] options = {"\n", "\n", "\n", "s\n", "foo\n", "f\n"};
        ballerinaBuildServer.runMainWithClientOptions(args, options, getEnvVariables(), "init",
                projectPath.toString());
        Files.copy(Paths.get("src", "test", "resources", "grpc", "nested_type_service.bal"), Paths.get
                (projectPath.resolve("foo").toString(), "nested_type_service.bal"));
        Files.deleteIfExists(projectPath.resolve("foo").resolve("hello_service.bal"));

        // perform ballerina build and generate balx file.
        ballerinaBuildServer = new ServerInstanceOld(serverZipPath);
        ballerinaBuildServer.runMain(new String[0], getEnvVariables(), "build", projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");
        // Run gRPC service from the balx file.
        ServerInstance ballerinaServerForService = ServerInstance.initBallerinaServer();
        ballerinaServerForService.startBallerinaServer(generatedBalx.toString());

        try {
            // run gRPC client to connect with the service.
            Path balFilePath = Paths.get("src", "test", "resources", "grpc", "nested_type_client.bal");
            ServerInstanceOld ballerinaClientServer = new ServerInstanceOld(serverZipPath);
            String[] clientArgsForRun = {balFilePath.toAbsolutePath().toString()};
            LogLeecher logLeecher1 = new LogLeecher("testInputNestedStruct output: Submitted name: Danesh");
            LogLeecher logLeecher2 = new LogLeecher("testOutputNestedStruct output: Sam");
            ballerinaClientServer.addLogLeecher(logLeecher1);
            ballerinaClientServer.addLogLeecher(logLeecher2);
            ballerinaClientServer.runMain(clientArgsForRun, getEnvVariables(), "run");
            logLeecher1.waitForText(10000);
            logLeecher2.waitForText(10000);
        } finally {
            ballerinaServerForService.stopServer();
        }

    }

    @AfterClass
    private void cleanup() {
        Utils.deleteFolder(tempProjectDirectory.toFile());
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private String[] getEnvVariables() {
        List<String> variables = new ArrayList<>();
        Map<String, String> envVarMap = System.getenv();
        envVarMap.forEach((key, value) -> variables.add(key + "=" + value));
        return variables.toArray(new String[variables.size()]);
    }

}
