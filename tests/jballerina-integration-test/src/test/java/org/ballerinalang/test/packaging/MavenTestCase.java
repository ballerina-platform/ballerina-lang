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
import org.testng.Assert;
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

/**
 * Test maven dependency resolution.
 */
public class MavenTestCase extends BaseTest {

    private Path tempHomeDirectory;
    private Path projectPath;
    private Map<String, String> envVariables;
    private BMainInstance balClient;

    @BeforeClass
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-maven-home-");
        this.projectPath = Files.createTempDirectory("bal-test-integration-maven-");

        // copy TestProject1 to a temp
        Path originalTestProj1 = Paths.get("src", "test", "resources", "packaging", "maven", "jyaml")
                .toAbsolutePath();
        PackerinaTestUtils.copyFolder(originalTestProj1, this.projectPath);

        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    /**
     * Build TestProject1 and then push the native module to staging central.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test maven dependency resolution.")
    public void mavenResolvingTest() throws BallerinaTestException, IOException {
        String[] args = {"-mvn=org.yaml:snakeyaml:2.0", "org.yaml.snakeyaml.Yaml"};
        balClient.runMain("bindgen", args, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        // delete the platform lib directory
        deleteFiles(this.projectPath.resolve("target").resolve("platform-libs"));

        String mvnBuildMsg = "target/bin/tests.jar";
        LogLeecher mvnBuildLeecher = new LogLeecher(mvnBuildMsg);
        balClient.runMain("build", new String[]{}, envVariables, new String[]{},
                new LogLeecher[]{mvnBuildLeecher}, projectPath.toString());
        mvnBuildLeecher.waitForText(5000);
        // delete the platform lib directory
        deleteFiles(this.projectPath.resolve("target").resolve("platform-libs"));

        Path tempFile = projectPath.resolve("temp.txt");
        Assert.assertFalse(tempFile.toFile().exists());
        balClient.runMain("run", new String[]{"--", tempFile.toString()}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        Assert.assertTrue(tempFile.toFile().exists());
        Files.delete(tempFile);
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        return envVariables;
    }

    @AfterClass(enabled = false)
    private void cleanup() throws Exception {
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.projectPath);
    }
}
