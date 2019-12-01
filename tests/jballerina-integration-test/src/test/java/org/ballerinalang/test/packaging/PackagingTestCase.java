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
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Testing pushing, pulling, searching a package from central and installing package to home repository.
 *
 * @since 0.981.0
 */
public class PackagingTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private String datePushed;
    private String orgName = "bcintegrationtest";
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    
    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        moduleName = moduleName + PackerinaTestUtils.randomModuleName(10);
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test create a ballerina project and module to be pushed to central")
    public void testCreateProject() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        // Create project
        balClient.runMain("new", new String[]{"initProject"}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.getParent().toString());
    
        // Update org name
        PackerinaTestUtils.updateManifestOrgName(projectPath, orgName);
        
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.isDirectory(projectPath));
    
        // Create module
        balClient.runMain("add", new String[]{moduleName}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());
    
        Assert.assertTrue(Files.exists(projectPath.resolve("src").resolve(moduleName)));
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src").resolve(moduleName)));
    }

    @Test(description = "Test pushing a package to central", dependsOnMethods = "testCreateProject")
    public void testPush() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initProject");

        // Get date and time of the module pushed.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-EE");
        datePushed = dtf.format(LocalDateTime.now());

        // First try to push without building
        String firstMsg = "error: cannot find balo file for the module: " + moduleName + ". Run " +
                          "'ballerina build -c <module_name>' to compile and generate the balo.";
        LogLeecher clientLeecher = new LogLeecher(firstMsg, LeecherType.ERROR);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{},
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(2000);
        
        String baloFileName = moduleName + "-"
                              + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                              + ProgramFileConstants.ANY_PLATFORM + "-"
                              + "0.1.0"
                              + BLANG_COMPILED_PKG_BINARY_EXT;
        Path baloPath = projectPath.resolve("target").resolve("balo").resolve(baloFileName);
        Assert.assertTrue(Files.notExists(baloPath));

        // Build module
        String buildMessage = "Created target" + File.separator + "balo" + File.separator + baloFileName;
        clientLeecher = new LogLeecher(buildMessage);
        balClient.runMain("build", new String[]{"-c", moduleName}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath.toString());
        
        // Then try to push without the flag so it builds the artifact
        String secondMsg = orgName + "/" + moduleName + ":0.1.0 [project repo -> central]";
        clientLeecher = new LogLeecher(secondMsg);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{},
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(5000);
    }

    @Test(description = "Test pulling a package from central", dependsOnMethods = "testPush")
    public void testPull() {
        String baloFileName = moduleName + "-"
                              + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                              + ProgramFileConstants.ANY_PLATFORM + "-"
                              + "0.1.0"
                              + BLANG_COMPILED_PKG_BINARY_EXT;
        Path baloPath = Paths.get(ProjectDirConstants.BALO_CACHE_DIR_NAME,
                                 orgName, moduleName, "0.1.0");

        given().with().pollInterval(Duration.TEN_SECONDS).and()
               .with().pollDelay(Duration.FIVE_SECONDS)
               .await().atMost(90, SECONDS).until(() -> {
            String[] clientArgs = {orgName + "/" + moduleName + ":0.1.0"};
            balClient.runMain("pull", clientArgs, envVariables, new String[]{},
                    new LogLeecher[]{}, balServer.getServerHome());
            return Files.exists(tempHomeDirectory.resolve(baloPath).resolve(baloFileName));
        });

        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(baloPath).resolve(baloFileName)));
    }

    @Test(description = "Test pullCount of a package from central", dependsOnMethods = "testPull")
    public void testPullCount() throws IOException {
        URI remoteUri = URI.create(RepoUtils.getRemoteRepoURL() + "/modules/info/" + orgName + "/" + moduleName);
        HttpURLConnection conn;
        conn = (HttpURLConnection) remoteUri.toURL().openConnection();
        int statusCode = conn.getResponseCode();
        if (statusCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()
                    , Charset.defaultCharset()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Object payload = JSONParser.parse(result.toString());
                if (payload instanceof MapValue) {
                    long pullCount = ((MapValue) payload).getIntValue("totalPullCount");
                    Assert.assertEquals(pullCount, 1);
                }
            }
        }
    }

//    @Test(description = "Test searching a package from central", dependsOnMethods = "testPush")
//    public void testSearch() throws BallerinaTestException {
//        String actualMsg = balClient.runMainAndReadStdOut("search", new String[]{moduleName}, envVariables,
//                balServer.getServerHome(), false);
//
//        // Check if the search results contains the following.
//        Assert.assertTrue(actualMsg.contains("Ballerina Central"));
//        Assert.assertTrue(actualMsg.contains("NAME"));
//        Assert.assertTrue(actualMsg.contains("DESCRIPTION"));
//        Assert.assertTrue(actualMsg.contains("DATE"));
//        Assert.assertTrue(actualMsg.contains("VERSION"));
//        Assert.assertTrue(actualMsg.contains(datePushed));
//        Assert.assertTrue(actualMsg.contains("0.1.0"));
//    }

    @Test(description = "Test push all packages in project to central")
    public void testPushAllPackages() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("pushAllPackageTest");
        
        // Create project
        balClient.runMain("new", new String[]{"pushAllPackageTest"}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.getParent().toString());
    
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.isDirectory(projectPath));
        
        String firstPackage = "firstTestPkg" + PackerinaTestUtils.randomModuleName(10);
        String secondPackage = "secondTestPkg" + PackerinaTestUtils.randomModuleName(10);
        
        // update org name
        PackerinaTestUtils.updateManifestOrgName(projectPath, orgName);
        
        // Create first module
        balClient.runMain("add", new String[]{firstPackage}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());
    
        Assert.assertTrue(Files.exists(projectPath.resolve("src").resolve(firstPackage)));
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src").resolve(firstPackage)));
    
        // Create second module
        balClient.runMain("add", new String[]{secondPackage}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());
    
        Assert.assertTrue(Files.exists(projectPath.resolve("src").resolve(secondPackage)));
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src").resolve(secondPackage)));
    
        // Build module
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        
        LogLeecher clientLeecherOne = new LogLeecher(orgName + "/" + firstPackage + ":0.1.0 [project repo -> central]");
        LogLeecher clientLeecherTwo = new LogLeecher(orgName + "/" + secondPackage +
                                                             ":0.1.0 [project repo -> central]");
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecherOne, clientLeecherTwo}, projectPath.toString());
        clientLeecherOne.waitForText(5000);
        clientLeecherTwo.waitForText(5000);
    }

    @Test(description = "Test ballerina version")
    public void testBallerinaVersion() throws Exception {
        LogLeecher clientLeecher = new LogLeecher(RepoUtils.getBallerinaVersion());
        balClient.runMain("version", new String[0], envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
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
        PackerinaTestUtils.deleteFiles(tempHomeDirectory);
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
