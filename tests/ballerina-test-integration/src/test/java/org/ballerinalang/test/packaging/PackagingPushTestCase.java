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
package org.ballerinalang.test.packaging;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Testing pushing a package to central.
 */
public class PackagingPushTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaClient;
    private String serverZipPath;
    private Path tempDirectory;
    private Path tomlFilePath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempDirectory = Files.createTempDirectory("bal-test-integration-packaging-");
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);

        // Write Settings.toml with the access token
        tomlFilePath = tempDirectory.resolve("Settings.toml");
        String content = "[central]\n accesstoken = \"3ada0d6c-a9ce-3054-9909-cb0e7f810e44\"";
        Files.write(tomlFilePath, content.getBytes(), StandardOpenOption.CREATE);

        // Get the generated semVer version

        // Write the newest version to Ballerina.toml
        String relativePathOfToml = new File("src" + File.separator + "test" + File.separator + "resources"
                                                     + File.separator + "packaging" + File.separator + "sample-project" +
                                                     File.separator + "Ballerina.toml").getAbsolutePath();
        String ballerinaTomlContent = "[project] \n org-name = \"IntegrationTest\" \n version = \"1.0.0\"";
        Files.write(Paths.get(relativePathOfToml), ballerinaTomlContent.getBytes(), StandardOpenOption.WRITE);
    }

    @Test(description = "Test pushing a package to central")
    public void testPush() throws Exception {
        String sourceRootPath = new File("src" + File.separator + "test" + File.separator + "resources"
                                                 + File.separator + "packaging" + File.separator +
                                                 "sample-project").getAbsolutePath();
        String[] clientArgs = {"my.app", "--sourceroot " + sourceRootPath};
        ballerinaClient = new ServerInstance(serverZipPath);

        ballerinaClient.runMain(clientArgs, getEnvVariables(), "push");
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
        variables.add(ProjectDirConstants.HOME_REPO_ENV_KEY + "=" + tempDirectory.toString());

        return variables.toArray(new String[0]);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaClient.stopServer();
        Files.deleteIfExists(tomlFilePath);
        Files.walk(tempDirectory)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }
}
