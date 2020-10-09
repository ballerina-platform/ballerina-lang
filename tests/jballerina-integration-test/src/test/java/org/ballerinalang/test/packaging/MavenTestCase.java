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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_STAGE_CENTRAL;

/**
 * Test maven dependency resolution.
 */
public class MavenTestCase extends BaseTest {

    private Path tempProjectsDirectory;
    private Path tempHomeDirectory;
    private Path projectPath;
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private String moduleName = "jyaml";

    @BeforeClass(enabled = false)
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-maven-home-");
        this.tempProjectsDirectory = Files.createTempDirectory("bal-test-integration-maven-");

        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "maven", "jyaml")
                .toAbsolutePath();
        this.projectPath = this.tempProjectsDirectory.resolve("jyaml");
        PackerinaTestUtils.copyFolder(originalTestProj1, this.projectPath);

        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    /**
     * Build TestProject1 and then push the native module to staging central.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test maven dependency resolution.", enabled = false)
    public void mavenResolvingTest() throws BallerinaTestException, IOException {
        String mvnBuildMsg = "snakeyaml-1.26.jar";
        LogLeecher mvnBuildLeecher = new LogLeecher(mvnBuildMsg);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{mvnBuildLeecher}, projectPath.toString());
        mvnBuildLeecher.waitForText(5000);
        // delete the platform lib directory
        deleteFiles(this.projectPath.resolve("target").resolve("platform-libs"));
        LogLeecher mvnRunLeecher = new LogLeecher(mvnBuildMsg);
        balClient.runMain("run", new String[]{moduleName}, envVariables, new String[]{},
                new LogLeecher[]{mvnRunLeecher}, projectPath.toString());
        mvnRunLeecher.waitForText(5000);
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
        deleteFiles(this.projectPath);
    }
}
