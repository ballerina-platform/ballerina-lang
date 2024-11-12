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
package org.ballerinalang.test.grpc;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Test packaged gRPC service with nested ballerina record type for input and output.
 */
public class ServicePackagingTestCase extends BaseTest {

    private Path tempProjectDirectory;

    @BeforeClass()
    public void setup() throws IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
    }

    @Test(description = "Test packaged service with nested ballerina record type for input and output")
    public void testNestedMessageType() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("pkg1");
        Files.createDirectories(projectPath);

        // perform ballerina init and copy grpc service to the project.
        BMainInstance ballerinaBuildServer = new BMainInstance(balServer);
        String[] args = {"-i"};
        String[] options = {"\n", "\n", "\n", "s\n", "foo\n", "f\n"};
        ballerinaBuildServer.runMain("init", args, getEnvVariables(), options, new LogLeecher[]{},
                projectPath.toString());
        
        Files.copy(Path.of(getClass().getClassLoader().getResource("grpc/nested_type_service.bal").getPath()),
            Path.of(projectPath.resolve("foo").toString(), "nested_type_service.bal"));
        Files.deleteIfExists(projectPath.resolve("foo").resolve("hello_service.bal"));

        // perform bal build and generate balx file.
        ballerinaBuildServer.runMain("build", new String[0], getEnvVariables(), new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Path generatedBalx = projectPath.resolve("target").resolve("foo.balx");

        // Run gRPC service from the balx file.
        BServerInstance ballerinaServerForService = new BServerInstance(balServer);
        ballerinaServerForService.startServer(generatedBalx.toString(), true);

        try {
            // run gRPC client to connect with the service.
            Path balFilePath = Path.of("src", "test", "resources", "grpc", "nested_type_client.bal");
            BMainInstance ballerinaClientServer = new BMainInstance(balServer);
            String balFile = balFilePath.toAbsolutePath().toString();
            LogLeecher logLeecher1 = new LogLeecher("testInputNestedStruct output: Submitted name: Danesh");
            LogLeecher logLeecher2 = new LogLeecher("testOutputNestedStruct output: Sam");

            ballerinaClientServer.runMain(balFile, new String[]{}, new String[]{}, getEnvVariables(),
                    new String[]{}, new LogLeecher[]{logLeecher1, logLeecher2});
            logLeecher1.waitForText(10000);
            logLeecher2.waitForText(10000);
        } finally {
            ballerinaServerForService.shutdownServer();
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
    private Map<String, String> getEnvVariables() {
        Map<String, String> envVarMap = System.getenv();
        Map<String, String> retMap = new HashMap<>();
        envVarMap.forEach(retMap::put);
        return retMap;
    }

}
