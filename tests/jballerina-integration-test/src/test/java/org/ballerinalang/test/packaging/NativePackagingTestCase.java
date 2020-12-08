/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_STAGE_CENTRAL;

/**
 * Test case for building and pushing a native module and then using it in a module.
 */
public class NativePackagingTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectsDirectory;
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private Path testProj1Path;
    private String module1Name = "test" + PackerinaTestUtils.randomModuleName(10);
    private Path testProj2Path;
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-native-packaging-home-");
        this.tempProjectsDirectory = Files.createTempDirectory("bal-test-integration-native-packaging-project-");
    
        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "native", "TestProject1")
                .toAbsolutePath();
        this.testProj1Path = this.tempProjectsDirectory.resolve("TestProject1");
        copyFolder(originalTestProj1, this.testProj1Path);
    
        // rename module names
        Path testProjModule1Path = this.testProj1Path.resolve("src").resolve(this.module1Name);
        Files.createDirectories(this.testProj1Path.resolve("src").resolve(this.module1Name));
        copyFolder(this.testProj1Path.resolve("src").resolve("module1"), testProjModule1Path);
        deleteFiles(this.testProj1Path.resolve("src").resolve("module1"));
    
        // copy TestProject2 to a temp
        Path originalTestProj2 = Paths.get("src", "test", "resources", "packaging", "native", "TestProject2")
                .toAbsolutePath();
        testProj2Path = tempProjectsDirectory.resolve("TestProject2");
        copyFolder(originalTestProj2, testProj2Path);
    
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }
    
    /**
     * Build TestProject1 and then push the native module to staging central.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test building TestProject1 and then push the native module.")
    public void testBuildAndPushTestProject1() throws BallerinaTestException {
        // Build module
        String module1BaloFileName = module1Name + "-"
                                     + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                     + ProgramFileConstants.SUPPORTED_PLATFORMS[0]
                                     + "-"
                                     + "0.7.2"
                                     + BLANG_COMPILED_PKG_BINARY_EXT;
        
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + module1BaloFileName;
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-c", module1Name}, envVariables, new String[]{},
                new LogLeecher[]{module1BuildLeecher}, testProj1Path.toString());
        module1BuildLeecher.waitForText(5000);
        
        // Push built modules
        String orgName = "bcintegrationtest";
        String module1PushMsg = orgName + "/" + module1Name + ":0.7.2 [project repo -> central]";
        LogLeecher module1PushLeecher = new LogLeecher(module1PushMsg);
        balClient.runMain("push", new String[]{module1Name}, envVariables, new String[]{},
                new LogLeecher[]{module1PushLeecher}, testProj1Path.toString());
        module1PushLeecher.waitForText(5000);
    }
    
    /**
     * Update the module name mentioned in the imports of TestProject2 source files to the ones of TestProject1. Then
     * build the project to see if the dependencies are pulled. Once the dependencies are pulled, run the project and
     * see if the outcomes are correct.
     *
     * @throws IOException            When updating the module names.
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test building and running TestProject2", dependsOnMethods = "testBuildAndPushTestProject1")
    public void testBuildTestProject2() throws IOException, BallerinaTestException {
        // Replace module names in source file
        Path implBalFile = testProj2Path.resolve("src").resolve("baz").resolve("impl.bal");
        Stream<String> lines = Files.lines(implBalFile);
        List<String> replaced = lines.map(line -> line.replaceAll("MODULE_1", module1Name))
                .collect(Collectors.toList());
        Files.write(implBalFile, replaced);
        
        // Build module
        String bazBaloFileName = "baz-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "1.0.0"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String bazBuildMsg = "target" + File.separator + "balo" + File.separator + bazBaloFileName;
        LogLeecher bazBuildLeecher = new LogLeecher(bazBuildMsg);
        
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-c", "baz"}, envVariables, new String[]{}, new
                    LogLeecher[]{bazBuildLeecher}, testProj2Path.toString());
            Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
            return Files.exists(lockFilePath);
        });
        
        // Run and see output
        String msg = "1 passing";
        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("build", new String[] {"baz"}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, testProj2Path.toString());
        bazRunLeecher.waitForText(10000);
    }
    
    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put(BALLERINA_STAGE_CENTRAL, "true");
        return envVariables;
    }
    
    public  void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }
    
    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @AfterClass()
    private void cleanup() throws Exception {
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.tempProjectsDirectory);
    }
}
