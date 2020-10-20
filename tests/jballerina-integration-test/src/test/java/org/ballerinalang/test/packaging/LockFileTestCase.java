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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.packaging.ModulePushTestCase.REPO_TO_CENTRAL_SUCCESS_MSG;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_STAGE_CENTRAL;

/**
 * Test case for locking dependencies with Ballerina.lock file.
 */
public class LockFileTestCase extends BaseTest {
    private static final String REPO_TO_CENTRAL_SUCCESS_MSG_WITH_VERSION = ":1.0.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
    private Path tempHomeDirectory;
    private Path tempProjectsDirectory;
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private Path testProj1Path;
    private String module1Name = "test" + PackerinaTestUtils.randomModuleName(10);
    private String module2Name = "test" + PackerinaTestUtils.randomModuleName(10);
    private Path testProj2Path;
    
    /**
     * Copy the 2 projects(TestProject1 & TestProject2) to a temp folder and set random names for the modules of
     * TestProject1.
     *
     * @throws IOException            When create temp directories.
     * @throws BallerinaTestException When creating the ballerina client.
     */
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        this.tempProjectsDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        PrintStream out = System.out;
        out.println(tempHomeDirectory);
        out.println(tempProjectsDirectory);
        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "lockfile", "TestProject1")
                .toAbsolutePath();
        this.testProj1Path = this.tempProjectsDirectory.resolve("TestProject1");
        PackerinaTestUtils.copyFolder(originalTestProj1, this.testProj1Path);
        // rename module names
        Path testProjModule1Path = this.testProj1Path.resolve("src").resolve(this.module1Name);
        Files.createDirectories(this.testProj1Path.resolve("src").resolve(this.module1Name));
        PackerinaTestUtils.copyFolder(this.testProj1Path.resolve("src").resolve("module1"), testProjModule1Path);
        PackerinaTestUtils.deleteFiles(this.testProj1Path.resolve("src").resolve("module1"));
    
        Path testProjModule2Path = testProj1Path.resolve("src").resolve(this.module2Name);
        Files.createDirectories(testProj1Path.resolve("src").resolve(this.module2Name));
        PackerinaTestUtils.copyFolder(testProj1Path.resolve("src").resolve("module2"), testProjModule2Path);
        PackerinaTestUtils.deleteFiles(testProj1Path.resolve("src").resolve("module2"));
        
        // copy TestProject2 to a temp
        Path originalTestProj2 = Paths.get("src", "test", "resources", "packaging", "lockfile", "TestProject2")
                .toAbsolutePath();
        testProj2Path = tempProjectsDirectory.resolve("TestProject2");
        PackerinaTestUtils.copyFolder(originalTestProj2, testProj2Path);
        
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }
    
    /**
     * Build TestProject1 and then push the 2 modules to staging central.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test building TestProject1 and then pushing it's modules.")
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
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + module1BaloFileName;
        String module2BuildMsg = "target" + File.separator + "balo" + File.separator + module2BaloFileName;
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        LogLeecher module2BuildLeecher = new LogLeecher(module2BuildMsg);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{module1BuildLeecher, module2BuildLeecher}, testProj1Path.toString());
        module1BuildLeecher.waitForText(5000);
        module2BuildLeecher.waitForText(5000);
        
        
        // Push built modules
        String module1PushMsg = orgName + "/" + module1Name + REPO_TO_CENTRAL_SUCCESS_MSG_WITH_VERSION;
        String module2PushMsg = orgName + "/" + module2Name + REPO_TO_CENTRAL_SUCCESS_MSG_WITH_VERSION;
        LogLeecher module1PushLeecher = new LogLeecher(module1PushMsg, LogLeecher.LeecherType.INFO);
        LogLeecher module2PushLeecher = new LogLeecher(module2PushMsg, LogLeecher.LeecherType.INFO);
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{module1PushLeecher, module2PushLeecher}, testProj1Path.toString());
        module1PushLeecher.waitForText(15000);
        module2PushLeecher.waitForText(15000);
    }
    
    /**
     * Update the module names mentioned in the imports of TestProject2 source files to the ones of TestProject1. Then
     * build the project to see if the dependencies are pulled. Once the dependencies are pulled, run the project and
     * see if the outcomes are correct. Make sure the Ballerina.lock file is created.
     *
     * @throws IOException            When updating the module names.
     */
    @Test(description = "Test building and running TestProject2", dependsOnMethods = "testBuildAndPushTestProject1")
    public void testBuildTestProject2() throws IOException {
        // Replace module names in source file
        Path fooSayBal = testProj2Path.resolve("src").resolve("foo").resolve("foo_say.bal");
        Path fooTestBal = testProj2Path.resolve("src").resolve("foo").resolve("tests").resolve("foo_test.bal");
        PackerinaTestUtils.modifyContent(fooSayBal, "MODULE_1", module1Name);
        PackerinaTestUtils.modifyContent(fooSayBal, "MODULE_2", module2Name);
        PackerinaTestUtils.modifyContent(fooTestBal, "MODULE_1", module1Name);
        PackerinaTestUtils.modifyContent(fooTestBal, "MODULE_2", module2Name);

        // Build module
        String fooBaloFileName = "foo-"
                + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                + ProgramFileConstants.ANY_PLATFORM + "-"
                + "9.9.9"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBaloFile = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        String fooBuildMsg = "1 passing";
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve(fooBaloFile));
        });
    }
    
    /**
     * Update the implementation for TestProject1's "say.bal" file and bump the version of the project to "1.2.0". Then
     * Build and push the modules to central.
     *
     * @throws IOException            When updating the implementation of the project.
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test updating  TestProject1 and pushing.", dependsOnMethods = "testBuildTestProject2")
    public void testModifyProj1AndPush() throws IOException, BallerinaTestException {
        // Update code in module1
        Path module2SourceFile = testProj1Path.resolve("src").resolve(module2Name).resolve("say.bal");
        PackerinaTestUtils.modifyContent(module2SourceFile, "Hello ", "Hello world ");

        // Update Ballerina.toml version
        Path ballerinaTomlPath = testProj1Path.resolve("Ballerina.toml");
        PackerinaTestUtils.modifyContent(ballerinaTomlPath, "1.0.0", "1.2.0");
    
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
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + module1BaloFileName;
        String module2BuildMsg = "target" + File.separator + "balo" + File.separator + module2BaloFileName;
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        LogLeecher module2BuildLeecher = new LogLeecher(module2BuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{module1BuildLeecher, module2BuildLeecher}, testProj1Path.toString());
        module1BuildLeecher.waitForText(5000);
        module2BuildLeecher.waitForText(5000);
    
    
        // Push built modules
        String module1PushMsg = orgName + "/" + module1Name + ":1.2.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        String module2PushMsg = orgName + "/" + module2Name + ":1.2.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        LogLeecher module1PushLeecher = new LogLeecher(module1PushMsg);
        LogLeecher module2PushLeecher = new LogLeecher(module2PushMsg);
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{module1PushLeecher, module2PushLeecher}, testProj1Path.toString());
        module1PushLeecher.waitForText(10000);
        module2PushLeecher.waitForText(10000);
    }
    
    /**
     * Since the Ballerina.lock file is created where the versions of the modules of TestProject1 are set to "1.0.0",
     * rebuilding TestProject2 should not pull the dependencies but instead resolved from the home balo cache. When
     * running it should give the output as previous since its the same dependencies.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test rebuilding and running TestProject2", dependsOnMethods = "testModifyProj1AndPush")
    public void testRebuildTestProj2() throws BallerinaTestException {
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBuildMsg = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        String fooTestMsg = "1 passing";
        LogLeecher fooBuildLeecher = new LogLeecher(fooBuildMsg);
        LogLeecher fooTestLeecher = new LogLeecher(fooTestMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{fooBuildLeecher, fooTestLeecher}, testProj2Path.toString());
        fooBuildLeecher.waitForText(10000);
        fooTestLeecher.waitForText(10000);
    
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
    }
    
    /**
     * Delete generated Ballerina.lock file of TestProject2 and rebuild with offline flag. Since the lock file is not
     * there, it will try to resolve to the latest dependency. But since the offline flag is given it cannot connect to
     * central to look for a latest version. Hence it will look in the home balo repo resolving to "1.0.0" version.
     *
     * @throws IOException            When deleting the Ballerina.lock file.
     */
    @Test(description = "Test rebuilding and running TestProject2 with offline flag and lock file removed",
            dependsOnMethods = "testRebuildTestProj2")
    public void testRebuildTestProj2WithLockRemovedAndOffline() throws IOException {
        // Delete Ballerina.lock
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Files.delete(lockFilePath);
        
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBaloFile = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        String fooTestMsg = "1 passing";
        LogLeecher fooBuildLeecher = new LogLeecher(fooBaloFile);
        LogLeecher fooTestLeecher = new LogLeecher(fooTestMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a", "-c", "--offline"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher, fooTestLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            fooTestLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve(fooBaloFile));
        });
        
        
        lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
    }
    
    /**
     * Now delete the generated Ballerina.lock file. Rebuild and run TestProject2 without the offline flag. This should
     * pull the latest dependencies(1.2.0) from central and resolve to them. When running "foo" module of TestProject2
     * it should give the output which was modified in {@link #testModifyProj1AndPush}.
     *
     * @throws IOException When deleting the lock file.
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test rebuilding and running TestProject2 without lock file.",
            dependsOnMethods = "testRebuildTestProj2WithLockRemovedAndOffline")
    public void testRebuildTestProj2WithLockRemoved() throws BallerinaTestException, IOException {

        Path fooTestBal = testProj2Path.resolve("src").resolve("foo").resolve("tests").resolve("foo_test.bal");
        PackerinaTestUtils.modifyContent(fooTestBal, "Hello ", "Hello world ");
        // Delete Ballerina.lock
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Files.delete(lockFilePath);
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBaloFile = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        String fooTestMsg = "1 passing";
        LogLeecher fooBuildLeecher = new LogLeecher(fooBaloFile);
        LogLeecher fooTestLeecher = new LogLeecher(fooTestMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher, fooTestLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            fooTestLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve(fooBaloFile));
        });

        fooBuildLeecher.waitForText(10000);
        lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
    }
    
    /**
     * Update the Ballerina.toml file by adding the 2 dependencies and set the versions to "1.0.0". But since the
     * Ballerina.lock is still there with version "1.2.0", an error will be thrown when creating the balo. The
     * Ballerina.lock file has priority than Ballerina.toml.
     *
     * @throws IOException When updating the Ballerina.toml.
     * @throws BallerinaTestException When running commands.
     * @throws InterruptedException When thread sleep is interrupted.
     */
    @Test(description = "Test rebuilding and running TestProject2 with lock file.",
          dependsOnMethods = "testRebuildTestProj2WithLockRemoved")
    public void testRebuildTestProj2WithUpdatedBallerinaToml() throws IOException, BallerinaTestException,
            InterruptedException {
        // Update the Ballerina.toml file
        Path ballerinaToml = testProj2Path.resolve("Ballerina.toml");
        String tomlDependencies = "\n\n[dependencies]\n" +
                                  "\"" + orgName + "/" + module1Name + "\" = \"1.0.0\"\n" +
                                  "\"" + orgName + "/" + module2Name + "\" = \"1.0.0\"\n";
        Files.write(ballerinaToml, tomlDependencies.getBytes(), StandardOpenOption.APPEND);
        
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{}, new
                LogLeecher[]{}, testProj2Path.toString());
        Thread.sleep(3000);
    
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBaloPath = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        Files.notExists(testProj2Path.resolve(fooBaloPath));
    }
    
    /**
     * Now delete the Ballerina.lock file. Since version "1.0.0" is mentioned in the Ballerina.toml. It will resolve to
     * that version from home balo repo.
     *
     * @throws IOException When deleting the Ballerina.lock.
     */
    @Test(description = "Test rebuilding and running TestProject2 without lock file.",
          dependsOnMethods = "testRebuildTestProj2WithUpdatedBallerinaToml")
    public void testRebuildTestProj2WithUpdatedBallerinaTomlAndLockRemoved() throws IOException {
        Path fooTestBal = testProj2Path.resolve("src").resolve("foo").resolve("tests").resolve("foo_test.bal");
        PackerinaTestUtils.modifyContent(fooTestBal, "Hello world ", "Hello ");

        // Delete Ballerina.lock
        Path lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Files.delete(lockFilePath);
        
        // Build module
        String fooBaloFileName = "foo-"
                                 + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                 + ProgramFileConstants.ANY_PLATFORM + "-"
                                 + "9.9.9"
                                 + BLANG_COMPILED_PKG_BINARY_EXT;
        String fooBaloFile = "target" + File.separator + "balo" + File.separator + fooBaloFileName;
        String fooTestMsg = "1 passing";
        LogLeecher fooBuildLeecher = new LogLeecher(fooBaloFile);
        LogLeecher fooTestLeecher = new LogLeecher(fooTestMsg);
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{}, new
                    LogLeecher[]{fooBuildLeecher, fooTestLeecher}, testProj2Path.toString());
            fooBuildLeecher.waitForText(10000);
            fooTestLeecher.waitForText(10000);
            return Files.exists(testProj2Path.resolve(fooBaloFile));
        });
        
        lockFilePath = testProj2Path.resolve("Ballerina.lock");
        Assert.assertTrue(Files.exists(lockFilePath));
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
    
    @AfterClass(enabled = false)
    private void cleanup() throws Exception {
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.tempProjectsDirectory);
    }
}
