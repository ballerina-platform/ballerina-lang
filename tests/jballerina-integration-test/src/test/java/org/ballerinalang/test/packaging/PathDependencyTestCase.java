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
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Test cases related to solving dependencies using paths in Ballerina.toml.
 */
public class PathDependencyTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempTestResources;
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-pathdep-home-");
        this.tempTestResources = Files.createTempDirectory("bal-test-integration-packaging-pathdep-project-");
        
        // copy resources to a temp
        Path testResources = Paths.get("src", "test", "resources", "packaging", "balopath").toAbsolutePath();
        copyFolder(testResources, this.tempTestResources);
        
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
        balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{},
                new LogLeecher[]{beeModuleBuildLeecher}, caseResources.resolve("TestProject1").toString());
        beeModuleBuildLeecher.waitForText(5000);
    
        // Build foo module of TestProject2
        String bazModuleBaloFileName = "baz-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-2.0.0"
                                       + BLANG_COMPILED_PKG_BINARY_EXT;
    
        String bazBuildMsg = "target" + File.separator + "balo" + File.separator + bazModuleBaloFileName;
        LogLeecher bazModuleBuildLeecher = new LogLeecher(bazBuildMsg);
        balClient.runMain("build", new String[]{"-c"}, envVariables, new String[]{},
                new LogLeecher[]{bazModuleBuildLeecher}, caseResources.resolve("TestProject2").toString());
        bazModuleBuildLeecher.waitForText(5000);
    
        // Run and see output
        String msg = "Bar-bzzzz";
        LogLeecher bazRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[] {"baz"}, envVariables, new String[0],
                new LogLeecher[]{bazRunLeecher}, caseResources.resolve("TestProject2").toString());
        bazRunLeecher.waitForText(10000);
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
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
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
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.tempTestResources);
    }
}
