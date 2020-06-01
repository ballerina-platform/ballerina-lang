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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.packaging.ModulePushTestCase.REPO_TO_CENTRAL_SUCCESS_MSG;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * Test cases related to solving dependencies using paths in Ballerina.toml.
 */
public class PathDependencyTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempTestResources;
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private String orgName = "bcintegrationtest";
    private String beeModuleName = "bee" + PackerinaTestUtils.randomModuleName(10);
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-pathdep-home-");
        this.tempTestResources = Files.createTempDirectory("bal-test-integration-packaging-pathdep-project-");
        
        // copy resources to a temp
        Path testResources = Paths.get("src", "test", "resources", "packaging", "balopath").toAbsolutePath();
        PackerinaTestUtils.copyFolder(testResources, this.tempTestResources);
        
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }
    
    /**
     * Case1: Build TestProject1. Then build TestProject2 which refer to the balo of TestProject1. Run the jar of
     * TestProject2
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case1: Test path between 2 projects.")
    public void testBaloPathCase1() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case1");
        // Build bee module of TestProject1
        String beeModuleBaloFileName = "bee-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-1.2.0"
                                     + BLANG_COMPILED_PKG_BINARY_EXT;
        
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + beeModuleBaloFileName;
        LogLeecher beeModuleBuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
    
        // Build foo module of TestProject2
        String bazModuleBaloFileName = "baz" + BLANG_COMPILED_JAR_EXT;
    
        String bazBuildMsg = "target" + File.separator + "bin" + File.separator + bazModuleBaloFileName;
        LogLeecher bazModuleBuildLeecher = new LogLeecher(bazBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{bazModuleBuildLeecher}, caseResources.resolve("TestProject2").toString());
        bazModuleBuildLeecher.waitForText(5000);
    
        // Run and see output
        String msg = "Bar-bzzzz";
        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] {bazBuildMsg}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject2").toString());
        bazRunLeecher.waitForText(10000);
    }
    
    /**
     * Case2: Build TestProject1. Then build TestProject2 which has 2 modules. Module m1 imports and uses module bee of
     * TestProject1. Modules m2 uses m1. Run the jar if m2.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case2: Test path between 2 projects where 3 modules are " +
            "involved and imported as a chain.")
    public void testBaloPathCase2() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case2");
        // Build bee module of TestProject1
        String beeModuleBaloFileName = "bee-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-1.2.0"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;
        
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + beeModuleBaloFileName;
        LogLeecher beeModuleBuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-c", "--all"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
        
        // Build modules of TestProject2
        String m1ModuleBaloFileName = "m1-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-2.0.0"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;
        String m2ModuleExecutableFileName = "m2" + BLANG_COMPILED_JAR_EXT;
        
        String m1BuildMsg = "target" + File.separator + "balo" + File.separator + m1ModuleBaloFileName;
        String m2BuildMsg = "target" + File.separator + "bin" + File.separator + m2ModuleExecutableFileName;
        LogLeecher m1ModuleBuildLeecher = new LogLeecher(m1BuildMsg);
        LogLeecher m2ModuleBuildLeecher = new LogLeecher(m2BuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{m1ModuleBuildLeecher, m2ModuleBuildLeecher},
                caseResources.resolve("TestProject2").toString());
        m1ModuleBuildLeecher.waitForText(5000);
        m2ModuleBuildLeecher.waitForText(5000);
        
        // Run and see output
        LogLeecher beeOutputLeecher = new LogLeecher("bzzz");
        LogLeecher m1OutputLeecher = new LogLeecher("This is org2/m1");
        balClient.runMain("run", new String[] {m2BuildMsg}, envVariables, new String[0],
                new LogLeecher[]{beeOutputLeecher, m1OutputLeecher}, caseResources.resolve("TestProject2").toString());
        beeOutputLeecher.waitForText(10000);
        m1OutputLeecher.waitForText(10000);
    }
    
    /**
     * Case3: Build TestProject1 which has a native module. Then build TestProject2 which refer to the native module
     * balo. Run the jar of TestProject2
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case3: Test path between 2 projects which the import is a native.")
    public void testBaloPathCase3() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case3");
        // Build bee module of TestProject1
        String toml4jModuleBaloFileName = "toml4j-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.7.2"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;

        String toml4jBuildMsg = "target" + File.separator + "balo" + File.separator + toml4jModuleBaloFileName;
        LogLeecher toml4jBuildLeecher = new LogLeecher(toml4jBuildMsg);
        balClient.runMain("build", new String[]{"--all", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{toml4jBuildLeecher}, caseResources.resolve("TestProject1").toString());
        toml4jBuildLeecher.waitForText(5000);

        // Build foo module of TestProject2
        String bazModuleBaloFileName = "baz" + BLANG_COMPILED_JAR_EXT;

        String bazBuildMsg = "target" + File.separator + "bin" + File.separator + bazModuleBaloFileName;
        LogLeecher bazModuleBuildLeecher = new LogLeecher(bazBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{bazModuleBuildLeecher}, caseResources.resolve("TestProject2").toString());
        bazModuleBuildLeecher.waitForText(5000);

        // Run and see output
        LogLeecher bazRunLeecher = new LogLeecher("cat");
        balClient.runMain("run", new String[] {bazBuildMsg}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject2").toString());
        bazRunLeecher.waitForText(10000);
    }
    
    /**
     * Case4: 1. Build TestProject1 and push bee to central.
     * 2. Build TestProject2 and push to central where module fee depends on bee.
     * 3. Build TestProject3 which imports fee.
     * 4. Run the jar if fee. Check output.
     * 5. Modify and build bee.
     * 6. Set dependency path to bee in TestProject3.
     * 7. Check if output has changes.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(enabled = false)
    public void testBaloPathCase4() throws BallerinaTestException, IOException, InterruptedException {
        Path caseResources = tempTestResources.resolve("case4");
        // Build bee module of TestProject1
        //// change module name
        Path testProjBeeModulePath = caseResources.resolve("TestProject1").resolve("src").resolve(beeModuleName);
        Files.createDirectories(caseResources.resolve("TestProject1").resolve("src").resolve(beeModuleName));
        PackerinaTestUtils.copyFolder(caseResources.resolve("TestProject1").resolve("src").resolve("bee"),
                                      testProjBeeModulePath);
        deleteFiles(caseResources.resolve("TestProject1").resolve("src").resolve("bee"));
        
        String beeModuleBaloFileName = beeModuleName + "-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-1" +
                ".2.0" + BLANG_COMPILED_PKG_BINARY_EXT;
        
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + beeModuleBaloFileName;
        LogLeecher beeModuleBuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
    
        // Push bee module of TestProject1
        String beePushMsg = orgName + "/" + beeModuleName + ":1.2.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        LogLeecher beePushLeecher = new LogLeecher(beePushMsg, LogLeecher.LeecherType.INFO);
        balClient.runMain("push", new String[]{"--all"}, envVariables, new String[]{},
                new LogLeecher[]{beePushLeecher}, caseResources.resolve("TestProject1").toString());
        beePushLeecher.waitForText(12000);
    
        
        // Build fee module of TestProject2
        String feeModuleName = "fee" + PackerinaTestUtils.randomModuleName(10);
        //// replace import
        Path feeBalPath = caseResources.resolve("TestProject2").resolve("src").resolve("fee").resolve("impl.bal");
        Stream<String> lines = Files.lines(feeBalPath);
        List<String> replaced = lines.map(line -> line.replaceAll("bee", beeModuleName))
                .collect(Collectors.toList());
        Files.write(feeBalPath, replaced);
        
        //// change module name
        Path testProjFeeModulePath = caseResources.resolve("TestProject2").resolve("src").resolve(feeModuleName);
        Files.createDirectories(caseResources.resolve("TestProject2").resolve("src").resolve(feeModuleName));
        PackerinaTestUtils.copyFolder(caseResources.resolve("TestProject2").resolve("src").resolve("fee"),
                                      testProjFeeModulePath);
        deleteFiles(caseResources.resolve("TestProject2").resolve("src").resolve("fee"));
    
        String feeModuleBaloFileName = feeModuleName + "-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-2.0.0"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;
    
        String feeBaloFilePath = "target" + File.separator + "balo" + File.separator + feeModuleBaloFileName;

        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                    new LogLeecher[]{}, caseResources.resolve("TestProject2").toString());
            return Files.exists(caseResources.resolve("TestProject2").resolve(Paths.get(feeBaloFilePath)));
        });
    
        // Push fee module of TestProject2
        String feePushMsg = orgName + "/" + feeModuleName + ":2.0.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        LogLeecher feePushLeecher = new LogLeecher(feePushMsg);
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{feePushLeecher}, caseResources.resolve("TestProject2").toString());
        feePushLeecher.waitForText(5000);

        // Build jee module of TestProject3
        //// replace import
        Path jeeBalPath = caseResources.resolve("TestProject3").resolve("src").resolve("jee").resolve("impl.bal");
        lines = Files.lines(jeeBalPath);
        replaced = lines.map(line -> line.replaceAll("fee", feeModuleName))
                .collect(Collectors.toList());
        Files.write(jeeBalPath, replaced);
    
        String jeeModuleBaloFileName = "jee" + BLANG_COMPILED_JAR_EXT;
        String jeeExecutableFilePath = "target" + File.separator + "bin" + File.separator + jeeModuleBaloFileName;

        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                    new LogLeecher[]{}, caseResources.resolve("TestProject3").toString());
            return Files.exists(caseResources.resolve("TestProject3").resolve(Paths.get(jeeExecutableFilePath)));
        });
    
        // Run and see output
        LogLeecher beeRunLeecher = new LogLeecher("bzzz");
        LogLeecher feeRunLeecher = new LogLeecher("feeeee");
        balClient.runMain("run", new String[] {jeeExecutableFilePath}, envVariables, new String[0],
                new LogLeecher[]{beeRunLeecher, feeRunLeecher}, caseResources.resolve("TestProject3").toString());
        beeRunLeecher.waitForText(10000);
        feeRunLeecher.waitForText(10000);
    
        // Update and build bee module of TestProject1
        //// replace code
        Path beeBalPath = caseResources.resolve("TestProject1").resolve("src").resolve(beeModuleName)
                .resolve("say.bal");
        lines = Files.lines(beeBalPath);
        replaced = lines.map(line -> line.replaceAll("bzzz", "buzzz")).collect(Collectors.toList());
        Files.write(beeBalPath, replaced);
    
        beeModuleBuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
    
        // Build and run TestProject3 with path set to bee balo in TestProject1
        //// replace Ballerina.toml
        Path ballerinaToml = caseResources.resolve("TestProject3").resolve("Ballerina.toml");
        lines = Files.lines(ballerinaToml);
        replaced = lines.map(line -> line.replaceAll("#", ""))
                .map(line -> line.replaceAll("bee", beeModuleName))
                .collect(Collectors.toList());
        Files.write(ballerinaToml, replaced);
        
        given().with().pollInterval(Duration.TEN_SECONDS).and()
                .with().pollDelay(Duration.FIVE_SECONDS)
                .await().atMost(120, SECONDS).until(() -> {
            balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                    new LogLeecher[]{}, caseResources.resolve("TestProject3").toString());
            return Files.exists(caseResources.resolve("TestProject3").resolve(Paths.get(jeeExecutableFilePath)));
        });
    
        // Run and see output
        beeRunLeecher = new LogLeecher("buzzz");
        feeRunLeecher = new LogLeecher("feeeee");
        balClient.runMain("run", new String[] {jeeExecutableFilePath}, envVariables, new String[0],
                new LogLeecher[]{beeRunLeecher, feeRunLeecher}, caseResources.resolve("TestProject3").toString());
        beeRunLeecher.waitForText(10000);
        feeRunLeecher.waitForText(10000);
    }
    
    /**
     * Case5: Build TestProject1. Then build TestProject2 which refer to the balo of TestProject1. Then try to push baz
     * of TestProject2 to central with balo path dependency
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case5: Push with path dependency.", expectedExceptions = BallerinaTestException.class)
    public void testBaloPathCase5() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case5");
        // Build bee module of TestProject1
        String beeModuleBaloFileName = "bee-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-1.2.0"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;
        
        String module1BuildMsg = "target" + File.separator + "balo" + File.separator + beeModuleBaloFileName;
        LogLeecher beeModuleBuildLeecher = new LogLeecher(module1BuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
        
        // Build foo module of TestProject2
        String bazModuleBaloFileName = "baz" + BLANG_COMPILED_JAR_EXT;
        
        String bazBuildMsg = "target" + File.separator + "bin" + File.separator + bazModuleBaloFileName;
        LogLeecher bazModuleBuildLeecher = new LogLeecher(bazBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{bazModuleBuildLeecher}, caseResources.resolve("TestProject2").toString());
        bazModuleBuildLeecher.waitForText(5000);
    
        // Push baz module of TestProject2
        String bazPushMsg = "[project repo -> central]";
        LogLeecher bazPushLeecher = new LogLeecher(bazPushMsg);
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{bazPushLeecher}, caseResources.resolve("TestProject2").toString());
        bazPushLeecher.waitForText(5000);
    }

    /**
     * Case6: Build TestProject2. Then build TestProject1 which refer to the balo of TestProject2. TestProject2 has two
     * modules X and Y which imports the same module from TestProject2 which is Z.
     * Then run the jar of TestProject1
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case6: Test dependency between two porject with common module as an import.")
    public void testBaloPathCase6() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case6");

        // Build Z module of TestProject2
        String moduleZBaloFileName = "Z-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String moduleZBuildMsg = "target" + File.separator + "balo" + File.separator + moduleZBaloFileName;
        LogLeecher moduleZBuildLeecher = new LogLeecher(moduleZBuildMsg);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleZBuildLeecher}, caseResources.resolve("TestProject2").toString());
        moduleZBuildLeecher.waitForText(5000);

        // Build all modules of TestProject1
        String moduleXBaloFileName = "X-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String moduleYBaloFileName = "Y-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String moduleXBuildMsg = "target" + File.separator + "balo" + File.separator + moduleXBaloFileName;
        String moduleYBuildMsg = "target" + File.separator + "balo" + File.separator + moduleYBaloFileName;
        LogLeecher moduleXBuildLeecher = new LogLeecher(moduleXBuildMsg);
        LogLeecher moduleYBuildLeecher = new LogLeecher(moduleYBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleXBuildLeecher, moduleYBuildLeecher},
                caseResources.resolve("TestProject1").toString());
        moduleXBuildLeecher.waitForText(5000);
        moduleYBuildLeecher.waitForText(5000);

        // Run and see output
        String msg = "Hello world from module X!";
        String moduleXJarFileName = "X" + BLANG_COMPILED_JAR_EXT;
        String executableFilePath = "target" + File.separator + "bin" + File.separator + moduleXJarFileName;
        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{executableFilePath}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject1").toString());
        bazRunLeecher.waitForText(10000);
    }

    /**
     * Case7: Build TestProject1. TestProject1 has two modules utils and foo. "foo" module import the utils module
     * which has an interop jar as platform dependency. Then run the jar of TestProject1.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case7: Test platform dependency of two project with common module as an interop dependency")
    public void testBaloPathCase7() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case7");
        // Build all modules of TestProject3
        String moduleUtilsBaloFileName = "utils-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String moduleFooBaloFileName = "foo-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String moduleXBuildMsg = "target" + File.separator + "balo" + File.separator + moduleUtilsBaloFileName;
        String moduleYBuildMsg = "target" + File.separator + "balo" + File.separator + moduleFooBaloFileName;
        LogLeecher moduleXBuildLeecher = new LogLeecher(moduleXBuildMsg);
        LogLeecher moduleYBuildLeecher = new LogLeecher(moduleYBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleXBuildLeecher, moduleYBuildLeecher},
                caseResources.resolve("TestProject1").toString());
        moduleXBuildLeecher.waitForText(5000);
        moduleYBuildLeecher.waitForText(5000);

        String msg = "This is a test string value !!!";

        String moduleFooJarFileName = "foo" + BLANG_COMPILED_JAR_EXT;
        String executableFilePath = "target" + File.separator + "bin" + File.separator + moduleFooJarFileName;
        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{executableFilePath}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject1").toString());
        bazRunLeecher.waitForText(10000);
    }

    /**
     * Case8: Build the utils single bal file which is using previusly pushed utils module with interop jar.
     * Then run the jar.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case8: Test single bal file using external module with interop dependency",
    dependsOnMethods = "testBaloPathCase7")
    public void testBaloSingleBalFileCase8() throws BallerinaTestException, IOException {

        Path caseResources = tempTestResources.resolve("case8");

        String interopFileName = "interop_file";
        String interopBalFileName = interopFileName + BLANG_SOURCE_EXT;
        String interopBalJarFileName = interopFileName + BLANG_COMPILED_JAR_EXT;
        //// replace import
        Path feeBalPath = caseResources.resolve(interopBalFileName);
        Stream<String> lines = Files.lines(feeBalPath);
        List<String> replaced = lines.map(line -> line.replaceAll("bee", beeModuleName))
                .collect(Collectors.toList());
        Files.write(feeBalPath, replaced);

        String testMsg = "Tested utils getString method using interop and received: This is a test string value !!!";
        LogLeecher fileBuildLeecher = new LogLeecher(interopBalJarFileName);
        LogLeecher fileTestLeecher = new LogLeecher(testMsg);
        balClient.runMain("build", new String[]{interopBalFileName}, envVariables, new String[]{},
                          new LogLeecher[]{fileBuildLeecher, fileTestLeecher}, caseResources.toString());
        fileBuildLeecher.waitForText(5000);

        String msg = "This is a test string value !!!";

        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{interopBalJarFileName}, envVariables, new String[0],
                          new LogLeecher[]{bazRunLeecher}, caseResources.toString());
        bazRunLeecher.waitForText(10000);

        // ballerina run bal command
        LogLeecher balRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{interopBalFileName}, envVariables, new String[]{},
                          new LogLeecher[]{balRunLeecher}, caseResources.toString());
        balRunLeecher.waitForText(10000);
    }

    /**
     * Case9: Build TestProject1 which has a native module with java libraries in the resources directory.
     * Then build TestProject2 which refer to the native module balo. Run the jar of TestProject2
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case9: Test path between 2 projects which the import is a native and libraries are in " +
            "resources directory.")
    public void testBaloPathCase9() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case9");
        // Build bee module of TestProject1
        String toml4jModuleBaloFileName = "toml4j-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.7.2"
                + BLANG_COMPILED_PKG_BINARY_EXT;

        String toml4jBuildMsg = "target" + File.separator + "balo" + File.separator + toml4jModuleBaloFileName;
        LogLeecher toml4jBuildLeecher = new LogLeecher(toml4jBuildMsg);
        balClient.runMain("build", new String[]{"--all", "-c"}, envVariables, new String[]{},
                          new LogLeecher[]{toml4jBuildLeecher}, caseResources.resolve("TestProject1").toString());
        toml4jBuildLeecher.waitForText(5000);

        // Build foo module of TestProject2
        String bazModuleBaloFileName = "baz" + BLANG_COMPILED_JAR_EXT;

        String bazBuildMsg = "target" + File.separator + "bin" + File.separator + bazModuleBaloFileName;
        LogLeecher bazModuleBuildLeecher = new LogLeecher(bazBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                          new LogLeecher[]{bazModuleBuildLeecher}, caseResources.resolve("TestProject2").toString());
        bazModuleBuildLeecher.waitForText(5000);

        // Run and see output
        LogLeecher bazRunLeecher = new LogLeecher("cat");
        balClient.runMain("run", new String[]{bazBuildMsg}, envVariables, new String[0],
                          new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject2").toString());
        bazRunLeecher.waitForText(10000);
    }

    /**
     * Case10: Build and run TestProject1 which imports module with "ballerina" org name via balo path.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case10: Test build and run project which imports module with \"ballerina\" org name via" +
            " balo path.")
    public void testBaloPathCase10() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("case10");
        String printBarLog = "Bar";
        String buildLog = "target/bin/mod1.jar";
        LogLeecher testLogeecher = new LogLeecher(printBarLog);
        LogLeecher buildLogLeecher = new LogLeecher(buildLog);

        // Build TestProject1 with tests
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                          new LogLeecher[]{testLogeecher, buildLogLeecher},
                          caseResources.resolve("TestProject1").toString());
        testLogeecher.waitForText(5000);
        buildLogLeecher.waitForText(5000);

        // Run TestProject1
        LogLeecher runLogLeecher = new LogLeecher(printBarLog);
        balClient.runMain("run", new String[]{"mod1"}, envVariables, new String[]{},
                          new LogLeecher[]{runLogLeecher}, caseResources.resolve("TestProject1").toString());
        runLogLeecher.waitForText(5000);
    }

    /**
     * Build TestProject1. TestProject1 will fail as the given platform dependency does not have a valid path.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test platform library dependency valid path")
    public void testValidatePlatformLibraryPath() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("platform-dependency");
        String msg = "error: path is not specified for given platform library dependency.";
        LogLeecher bazRunLeecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{}, new LogLeecher[]{bazRunLeecher},
                caseResources.resolve("TestProject1").toString());
        bazRunLeecher.waitForText(10000);
    }

    /**
     * Build test-dependency project. There is a test time dependency from foo to bar. This should pass as previously,
     * it was failing to load test time dependency from foo for bar, when test suite of bar was running.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test runtime test dependency from different modules")
    public void testRuntimeTimeDependencyForExecutingModuleTests() throws BallerinaTestException {
        Path caseResources = tempTestResources.resolve("test-dependency");
        String msg = "invoked fooFn";

        LogLeecher buildLogLeecher = new LogLeecher(msg);
        balClient.runMain("build", new String[]{"bar"}, envVariables, new String[]{}, new LogLeecher[]{buildLogLeecher},
                caseResources.toString());
        buildLogLeecher.waitForText(10000);

        buildLogLeecher = new LogLeecher(msg);
        balClient.runMain("build", new String[]{"daz"}, envVariables, new String[]{}, new LogLeecher[]{buildLogLeecher},
                caseResources.toString());
        buildLogLeecher.waitForText(10000);

        buildLogLeecher = new LogLeecher(msg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{}, new LogLeecher[]{buildLogLeecher},
                caseResources.toString());
        buildLogLeecher.waitForText(10000);

        // Test ballerina test command with single module which has dependencies.
        buildLogLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"bar"}, envVariables, new String[]{}, new LogLeecher[]{buildLogLeecher},
                          caseResources.toString());
        buildLogLeecher.waitForText(10000);
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
    
    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.tempTestResources);
    }
}
