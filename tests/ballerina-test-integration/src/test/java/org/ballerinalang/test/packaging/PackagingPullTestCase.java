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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Testing pulling a package from central.
 */
public class PackagingPullTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaClient;
    private String serverZipPath;
    private Path tempDirectory;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempDirectory = Files.createTempDirectory("bal-test-integration-packaging-");
        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    }

    @Test(description = "Test pulling a package from central")
    public void testPull() throws Exception {
        String[] clientArgs = {"wso2/twitter:0.9.0"};
        ballerinaClient = new ServerInstance(serverZipPath);

        ballerinaClient.runMain(clientArgs, getEnvVariables(), "pull");

        Path dirPath = Paths.get(ProjectDirConstants.CACHES_DIR_NAME,
                                 ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME,
                                 "wso2",
                                 "twitter",
                                 "0.9.0");

        Assert.assertTrue(Files.exists(tempDirectory.resolve(dirPath)));
        Assert.assertTrue(Files.exists(tempDirectory.resolve(dirPath).resolve("twitter.zip")));
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
