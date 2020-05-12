/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.awaitility.Duration;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Testing usage of multiple versions of same module.
 *
 * @since 2.0.0
 */
public class MultipleVersionsModuleTestCase extends BaseTest {

    static final String REPO_TO_CENTRAL_SUCCESS_MSG = " pushed to central successfully";
    private Path tempProjectDirectory;
    private String printModuleName = "print";
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-versions-project-");
        printModuleName = printModuleName + PackerinaTestUtils.randomModuleName(8);
        // copy resources to a temp
        Path testResources = Paths.get("src", "test", "resources", "packaging", "versions").toAbsolutePath();
        PackerinaTestUtils.copyFolder(testResources, this.tempProjectDirectory);

        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test pushing multiple versions of same module to central and use them in a project")
    public void testMultipleVersionsOfSameModuleImport() throws Exception {

        // Build and Push  print module of PrintProjectV1
        Path projectV1 = tempProjectDirectory.resolve("PrintProjectV1");

        //// change module name
        Path testProjV1ModulePath = projectV1.resolve("src").resolve(printModuleName);
        Files.createDirectories(projectV1.resolve("src").resolve(printModuleName));
        PackerinaTestUtils.copyFolder(projectV1.resolve("src").resolve("print"), testProjV1ModulePath);
        deleteFiles(projectV1.resolve("src").resolve("print"));

        String printModuleV1BaloFileName = printModuleName + "-" + ProgramFileConstants.IMPLEMENTATION_VERSION +
                "-any-1.0.0" + BLANG_COMPILED_PKG_BINARY_EXT;

        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + printModuleV1BaloFileName;
        LogLeecher printModV1BuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-c", "--skip-tests=true", printModuleName}, envVariables,
                          new String[]{},
                          new LogLeecher[]{printModV1BuildLeecher}, projectV1.toString());
        printModV1BuildLeecher.waitForText(5000);

        // Push print module of PrintProjectV1
        String printV1PushMsg = orgName + "/" + printModuleName + ":1.0.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        LogLeecher printV1PushMsgLeecher = new LogLeecher(printV1PushMsg, LogLeecher.LeecherType.INFO);
        balClient.runMain("push", new String[]{printModuleName}, envVariables, new String[]{},
                          new LogLeecher[]{printV1PushMsgLeecher}, projectV1.toString());
        printV1PushMsgLeecher.waitForText(12000);

        // Build and Push  print module of PrintProjectV2
        Path projectV2 = tempProjectDirectory.resolve("PrintProjectV2");

        //// change module name
        Files.createDirectories(projectV2.resolve("src").resolve(printModuleName));
        Path testProjV2ModulePath = projectV2.resolve("src").resolve(printModuleName);
        PackerinaTestUtils.copyFolder(projectV2.resolve("src").resolve("print"), testProjV2ModulePath);
        deleteFiles(projectV2.resolve("src").resolve("print"));

        String printModuleV2BaloFileName = printModuleName + "-" + ProgramFileConstants.IMPLEMENTATION_VERSION +
                "-any-2.0.0" + BLANG_COMPILED_PKG_BINARY_EXT;
        module1BuildMsg = "target" + File.separator + "balo" + File.separator + printModuleV2BaloFileName;
        LogLeecher printModV2BuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-c", "--skip-tests=true", printModuleName}, envVariables,
                          new String[]{},
                          new LogLeecher[]{printModV2BuildLeecher}, projectV2.toString());
        printModV2BuildLeecher.waitForText(5000);

        // Push print module of PrintProjectV2
        String printV2PushMsg = orgName + "/" + printModuleName + ":2.0.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        LogLeecher printV2PushMsgLeecher = new LogLeecher(printV2PushMsg, LogLeecher.LeecherType.INFO);
        balClient.runMain("push", new String[]{printModuleName}, envVariables, new String[]{},
                          new LogLeecher[]{printV2PushMsgLeecher}, projectV2.toString());
        printV1PushMsgLeecher.waitForText(12000);

        // Build fee module of MultiplePrintModuleUseProject
        //// replace import
        String printerModuleName = "printer";
        Path multiVersionsProject = tempProjectDirectory.resolve("MultiplePrintModulesUseProject");
        Path multiVersionBalPath = multiVersionsProject.resolve("src").resolve(printerModuleName).resolve("main.bal");
        Stream<String> lines = Files.lines(multiVersionBalPath);
        List<String> replaced = lines.map(line -> line.replaceAll("bcintegrationtest/print",
                                                                  orgName + "/" + printModuleName))
                .collect(Collectors.toList());
        Files.write(multiVersionBalPath, replaced);

        String printerModuleBaloFileName = printerModuleName + "-" + ProgramFileConstants.IMPLEMENTATION_VERSION +
                "-any-0.1.0" + BLANG_COMPILED_PKG_BINARY_EXT;
        String printerModuleJar = printerModuleName + BLANG_COMPILED_JAR_EXT;
        String printerBaloFilePath = "target" + File.separator + "balo" + File.separator + printerModuleBaloFileName;
        String printerJarFilePath = "target" + File.separator + "bin" + File.separator + printerModuleJar;
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{printerModuleName}, envVariables, new String[]{},
                              new LogLeecher[]{}, multiVersionsProject.toString());
            return Files.exists(multiVersionsProject.resolve(Paths.get(printerBaloFilePath)));
        });

        // Test multiple versions of same module output at runtime.
        String printV1PMsg = "Print: Hello World! from v1";
        String printV2PMsg = "Print: Hello World! from v2";
        LogLeecher printV1MsgLeecher = new LogLeecher(printV1PMsg, LogLeecher.LeecherType.INFO);
        LogLeecher printV2MsgLeecher = new LogLeecher(printV2PMsg, LogLeecher.LeecherType.INFO);
        balClient.runMain("run", new String[]{printerJarFilePath}, envVariables, new String[]{},
                          new LogLeecher[]{printV1MsgLeecher, printV2MsgLeecher}, multiVersionsProject.toString());
    }

    /**
     * Get environment variables.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put("BALLERINA_DEV_STAGE_CENTRAL", "true");
        envVariables.put("BALLERINA_CENTRAL_ACCESS_TOKEN", PackerinaTestUtils.getToken());
        return envVariables;
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
