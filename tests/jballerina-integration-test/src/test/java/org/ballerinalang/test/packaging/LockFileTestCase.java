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
import org.testng.Assert;
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

/**
 *
 */
public class LockFileTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectsDirectory;
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private Path testProj1Path;
    private String module1Name = "test" + PackerinaTestUtils.randomModuleName(10);
    private String module2Name = "test" + PackerinaTestUtils.randomModuleName(10);
    private Path testProj2Path;
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        this.tempProjectsDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        
        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "TestProject1").toAbsolutePath();
        this.testProj1Path = this.tempProjectsDirectory.resolve("TestProject1");
        copyFolder(originalTestProj1, this.testProj1Path);
    
        // rename module names
        Path testProjModule1Path = this.testProj1Path.resolve("src").resolve(this.module1Name);
        Files.createDirectories(this.testProj1Path.resolve("src").resolve(this.module1Name));
        copyFolder(this.testProj1Path.resolve("src").resolve("module1"), testProjModule1Path);
        deleteFiles(this.testProj1Path.resolve("src").resolve("module1"));
    
        Path testProjModule2Path = testProj1Path.resolve("src").resolve(this.module2Name);
        Files.createDirectories(testProj1Path.resolve("src").resolve(this.module2Name));
        copyFolder(testProj1Path.resolve("src").resolve("module2"), testProjModule2Path);
        deleteFiles(testProj1Path.resolve("src").resolve("module2"));
        
        // copy TestProject2 to a temp
        Path originalTestProj2 = Paths.get("src", "test", "resources", "packaging", "TestProject2").toAbsolutePath();
        testProj2Path = tempProjectsDirectory.resolve("TestProject2");
        copyFolder(originalTestProj2, testProj2Path);
        
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }
    
    /**
     * Build and push TestProj1 to central.
     * @throws Exception
     */
    @Test(description = "Test create a ballerina project and module to be pushed to central")
    public void testBuildAndPushTestProject1() throws BallerinaTestException {
        // Build module
        String module1BaloFileName = module1Name + "-"
                              + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                              + ProgramFileConstants.ANY_PLATFORM + "-"
                              + "1.0.0"
                              + BLANG_COMPILED_PKG_BINARY_EXT;
    
        String module2BaloFileName = module2Name + "-"
                                     + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                     + ProgramFileConstants.ANY_PLATFORM + "-"
                                     + "1.0.0"
                                     + BLANG_COMPILED_PKG_BINARY_EXT;
        String module1BuildMsg = "Created target" + File.separator + "balo" + File.separator + module1BaloFileName;
        String module2BuildMsg = "Created target" + File.separator + "balo" + File.separator + module2BaloFileName;
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        LogLeecher module2BuildLeecher = new LogLeecher(module2BuildMsg);
        balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{},
                new LogLeecher[]{module1BuildLeecher, module2BuildLeecher}, testProj1Path.toString());
        module1BuildLeecher.waitForText(5000);
        module2BuildLeecher.waitForText(5000);
        
        
        // Push built modules
        String module1PushMsg = orgName + "/" + module1Name + ":1.0.0 [project repo -> central]";
        String module2PushMsg = orgName + "/" + module2Name + ":1.0.0 [project repo -> central]";
        LogLeecher module1PushLeecher = new LogLeecher(module1PushMsg);
        LogLeecher module2PushLeecher = new LogLeecher(module2PushMsg);
        balClient.runMain("push", new String[]{}, envVariables, new String[]{},
                new LogLeecher[]{module1PushLeecher, module2PushLeecher}, testProj1Path.toString());
        module1PushLeecher.waitForText(5000);
        module2PushLeecher.waitForText(5000);
    }
    
    @Test(description = "Test pushing a package to central", dependsOnMethods = "testBuildAndPushTestProject1")
    public void testBuildTestProject2() throws Exception {
        // Replace module name in source file
        Path fooSayBal = testProj2Path.resolve("src").resolve("foo").resolve("foo_say.bal");
        Stream<String> lines = Files.lines(fooSayBal);
        List<String> replaced = lines.map(line -> line.replaceAll("MODULE_1", module1Name))
                .collect(Collectors.toList());
        Files.write(fooSayBal, replaced);
    
        lines = Files.lines(fooSayBal);
        replaced = lines.map(line -> line.replaceAll("MODULE_2", module2Name))
                .collect(Collectors.toList());
        Files.write(fooSayBal, replaced);
        lines.close();
        
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "1.0.0"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBuildMsg = "Created target" + File.separator + "balo" + File.separator + fooBaloFileName;
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
    
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher}, testProj2Path.toString());
            Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
            return Files.exists(lockFilePath);
        });
        
        // Run and see output
        String msg = "Test me\nHello john!";
        balClient.runMain("run", new String[] {"foo"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, testProj2Path.toString());
    }
    
    @Test(description = "Test pulling a package from central", dependsOnMethods = "testBuildTestProject2")
    public void testModifyProj1AndPush() throws IOException, BallerinaTestException {
        // Update code in module1
        Path module2SourceFile = testProj1Path.resolve("src").resolve(module2Name).resolve("say.bal");
        Stream<String> lines = Files.lines(module2SourceFile);
        List<String> replaced = lines.map(line -> line.replaceAll("Hello ", "Hello world "))
                .collect(Collectors.toList());
        Files.write(module2SourceFile, replaced);
        lines.close();
    
        // Update Ballerina.toml version
        Path ballerinaTomlPath = testProj1Path.resolve("Ballerina.toml");
        lines = Files.lines(ballerinaTomlPath);
        replaced = lines.map(line -> line.replaceAll("1.0.0", "1.2.0"))
                .collect(Collectors.toList());
        Files.write(ballerinaTomlPath, replaced);
        lines.close();
    
        String module1BaloFileName = module1Name + "-"
                                     + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                     + ProgramFileConstants.ANY_PLATFORM + "-"
                                     + "1.2.0"
                                     + BLANG_COMPILED_PKG_BINARY_EXT;
    
        String module2BaloFileName = module2Name + "-"
                                     + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                     + ProgramFileConstants.ANY_PLATFORM + "-"
                                     + "1.2.0"
                                     + BLANG_COMPILED_PKG_BINARY_EXT;
        String module1BuildMsg = "Created target" + File.separator + "balo" + File.separator + module1BaloFileName;
        String module2BuildMsg = "Created target" + File.separator + "balo" + File.separator + module2BaloFileName;
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        LogLeecher module2BuildLeecher = new LogLeecher(module2BuildMsg);
        balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{},
                new LogLeecher[]{module1BuildLeecher, module2BuildLeecher}, testProj1Path.toString());
        module1BuildLeecher.waitForText(5000);
        module2BuildLeecher.waitForText(5000);
    
    
        // Push built modules
        String module1PushMsg = orgName + "/" + module1Name + ":1.2.0 [project repo -> central]";
        String module2PushMsg = orgName + "/" + module2Name + ":1.2.0 [project repo -> central]";
        LogLeecher module1PushLeecher = new LogLeecher(module1PushMsg);
        LogLeecher module2PushLeecher = new LogLeecher(module2PushMsg);
        balClient.runMain("push", new String[]{}, envVariables, new String[]{},
                new LogLeecher[]{module1PushLeecher, module2PushLeecher}, testProj1Path.toString());
        module1PushLeecher.waitForText(5000);
        module2PushLeecher.waitForText(5000);
    }
    
    @Test(description = "Test searching a package from central", dependsOnMethods = "testModifyProj1AndPush")
    public void testRebuildTestProj2() throws BallerinaTestException {
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBuildMsg = "Created target" + File.separator + "balo" + File.separator + fooBaloFileName;
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
        balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{}, new LogLeecher[]{fooBuildLeecher},
                testProj2Path.toString());
        fooBuildLeecher.waitForText(10000);
    
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
    
        
        // Run and see output
        String msg = "Hello john!";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] {"foo"}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, testProj2Path.toString());
        fooRunLeecher.waitForText(10000);
    }
    
    @Test(description = "Test push all packages in project to central", dependsOnMethods = "testRebuildTestProj2")
    public void testRebuildTestProj2WithLockRemovedAndOffline() throws IOException, BallerinaTestException {
        // Delete Ballerina.toml
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Files.delete(lockFilePath);
        
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBuildMsg = "Created target" + File.separator + "balo" + File.separator + fooBaloFileName;
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-c", "--off-line"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve("Ballerina.lock"));
        });
        
        
        lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
        
        // Run and see output
        String msg = "Hello john!";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] {"foo"}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, testProj2Path.toString());
        fooRunLeecher.waitForText(10000);
    }
    
    @Test(description = "Test push all packages in project to central",
          dependsOnMethods = "testRebuildTestProj2WithLockRemovedAndOffline")
    public void testRebuildTestProj2WithLockRemoved() throws IOException, BallerinaTestException {
        // Delete Ballerina.toml
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Files.delete(lockFilePath);
        
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBuildMsg = "Created target" + File.separator + "balo" + File.separator + fooBaloFileName;
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve("Ballerina.lock"));
        });
    
        
        lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
    
        // Run and see output
        String msg = "Hello world john!";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] {"foo"}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, testProj2Path.toString());
        fooRunLeecher.waitForText(10000);
    }
    
    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put("BALLERINA_DEV_STAGE_CENTRAL", "true");
        return envVariables;
    }
    
    public  void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src)
                .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }
    
    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(tempHomeDirectory);
        deleteFiles(tempProjectsDirectory);
        balServer.cleanup();
    }
}
