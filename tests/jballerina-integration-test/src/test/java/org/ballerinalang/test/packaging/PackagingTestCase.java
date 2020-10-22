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

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.values.MapValue;
import org.awaitility.Duration;
import org.ballerinalang.cli.module.util.Utils;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;
import static org.ballerinalang.test.packaging.ModulePushTestCase.REPO_TO_CENTRAL_SUCCESS_MSG;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_STAGE_CENTRAL;

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
    private int totalPullCount = 0;
    
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

        Assert.assertTrue(Files.isDirectory(projectPath));
    
        // Create module
        balClient.runMain("add", new String[]{moduleName}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());

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

        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main.bal"),
                projectPath.resolve("src").resolve(moduleName).resolve("main.bal"));
        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main_test.bal"),
                projectPath.resolve("src").resolve(moduleName).resolve("tests").resolve("main_test.bal"));

        balClient.runMain("build", new String[]{"-c", moduleName}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath.toString());
        
        // Then try to push without the flag so it builds the artifact
        String secondMsg = orgName + "/" + moduleName + ":0.1.0" + REPO_TO_CENTRAL_SUCCESS_MSG;
        clientLeecher = new LogLeecher(secondMsg, LeecherType.INFO);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{},
                          new LogLeecher[]{clientLeecher}, projectPath.toString());
        clientLeecher.waitForText(60000);
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
            totalPullCount += 1;
            return Files.exists(tempHomeDirectory.resolve(baloPath).resolve(baloFileName));
        });

        Assert.assertTrue(Files.exists(tempHomeDirectory.resolve(baloPath).resolve(baloFileName)));
    }

    @Test(description = "Test searching a package from central", dependsOnMethods = "testPush")
    public void testSearch() throws BallerinaTestException {
        String actualMsg = balClient.runMainAndReadStdOut("search", new String[]{moduleName}, envVariables,
                balServer.getServerHome(), false);

        // Check if the search results contains the following.
        Assert.assertTrue(actualMsg.contains("Ballerina Central"));
        Assert.assertTrue(actualMsg.contains("NAME"));
        Assert.assertTrue(actualMsg.contains("DESCRIPTION"));
        Assert.assertTrue(actualMsg.contains("DATE"));
        Assert.assertTrue(actualMsg.contains("VERSION"));
        Assert.assertTrue(actualMsg.contains(datePushed));
        Assert.assertTrue(actualMsg.contains("0.1.0"));
    }

    @Test(description = "Test pullCount of a package from central", dependsOnMethods = "testPull")
    public void testPullCount() throws IOException {
        initializeSsl();
        String url = RepoUtils.getStagingURL() + "/modules/info/" + orgName + "/" + moduleName + "/*/";
        HttpURLConnection conn = createHttpUrlConnection(convertToUrl(url), "", 0, "", "");
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        int statusCode = conn.getResponseCode();
        if (statusCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                    Charset.defaultCharset()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Object payload = JSONParser.parse(result.toString());
                if (payload instanceof MapValue) {
                    long pullCount = ((MapValue) payload).getIntValue(StringUtils.fromString("totalPullCount"));
                    Assert.assertEquals(pullCount, totalPullCount);
                } else {
                    Assert.fail("error: invalid response received");
                }
            }
        } else {
            Assert.fail("error: could not connect to remote repository to find the latest version of module");
        }
    }

    @Test(description = "Test push all packages in project to central")
    public void testPushAllPackages() throws Exception {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("pushAllPackageTest");
        
        // Create project
        balClient.runMain("new", new String[]{"pushAllPackageTest"}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.getParent().toString());

        Assert.assertTrue(Files.isDirectory(projectPath));
        
        String firstPackage = "firstTestPkg" + PackerinaTestUtils.randomModuleName(10);
        String secondPackage = "secondTestPkg" + PackerinaTestUtils.randomModuleName(10);
        
        // update org name
        PackerinaTestUtils.updateManifestOrgName(projectPath, orgName);
        
        // Create first module
        balClient.runMain("add", new String[]{firstPackage}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());

        Path firstPackagePath = projectPath.resolve("src").resolve(firstPackage);

        Assert.assertTrue(Files.isDirectory(firstPackagePath));

        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main.bal"),
                firstPackagePath.resolve("main.bal"));
        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main_test.bal"),
                firstPackagePath.resolve("tests").resolve("main_test.bal"));
    
        // Create second module
        balClient.runMain("add", new String[]{secondPackage}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());

        Path secondPackagePath = projectPath.resolve("src").resolve(secondPackage);

        Assert.assertTrue(Files.isDirectory(secondPackagePath));

        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main.bal"),
                secondPackagePath.resolve("main.bal"));
        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main_test.bal"),
                secondPackagePath.resolve("tests").resolve("main_test.bal"));

        // Build module
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        
        LogLeecher clientLeecherOne = new LogLeecher(orgName + "/" + firstPackage + ":0.1.0"
                + REPO_TO_CENTRAL_SUCCESS_MSG);
        LogLeecher clientLeecherTwo = new LogLeecher(orgName + "/" + secondPackage + ":0.1.0"
                + REPO_TO_CENTRAL_SUCCESS_MSG);
        balClient.runMain("push", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{clientLeecherOne, clientLeecherTwo}, projectPath.toString());
        clientLeecherOne.waitForText(60000);
        clientLeecherTwo.waitForText(60000);
    }

    @Test(description = "Test ballerina version")
    public void testBallerinaVersion() throws Exception {
        LogLeecher clientLeecher = new LogLeecher(RepoUtils.getBallerinaVersionDisplayName());
        balClient.runMain("version", new String[0], envVariables, new String[]{},
                new LogLeecher[]{clientLeecher}, tempProjectDirectory.toString());
    }

    @Test(description = "Test and run a module which has a module name contains period. eg: foo.bar")
    public void testBuildAndRunModuleWithPeriod() throws BallerinaTestException {
        // Test ballerina init
        Path projectPath = tempProjectDirectory.resolve("buildAndRunModuleWithPeriodProject");

        // Create project
        balClient.runMain("new", new String[] { "buildAndRunModuleWithPeriodProject" }, envVariables, new String[] {},
                new LogLeecher[] {}, projectPath.getParent().toString());

        Assert.assertTrue(Files.isDirectory(projectPath));

        // Create module named `foo.bar`
        String moduleName = "foo.bar";
        balClient.runMain("add", new String[] { moduleName }, envVariables, new String[] {}, new LogLeecher[] {},
                projectPath.toString());

        Path modulePath = projectPath.resolve("src").resolve(moduleName);
        Assert.assertTrue(Files.isDirectory(modulePath));

        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main.bal"),
                modulePath.resolve("main.bal"));
        PackerinaTestUtils.copy(Paths.get("src", "test", "resources", "packaging", "pushingModule", "main_test.bal"),
                modulePath.resolve("tests").resolve("main_test.bal"));

        // Build module
        LogLeecher buildLeecher = new LogLeecher("[pass] testFunction");
        balClient.runMain("build", new String[] { "-c", "-a" }, envVariables, new String[] {},
                new LogLeecher[] { buildLeecher }, projectPath.toString());
        buildLeecher.waitForText(60000);

        String fooModuleBaloFileName = "foo.bar-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        String runMsg = "target" + File.separator + "balo" + File.separator + fooModuleBaloFileName;

        // Run module
        LogLeecher runLeecher = new LogLeecher(runMsg);
        balClient.runMain("run", new String[] { moduleName }, envVariables, new String[] {},
                new LogLeecher[] { runLeecher }, projectPath.toString());
        buildLeecher.waitForText(5000);
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

    @AfterClass
    private void cleanup() throws Exception {
        PackerinaTestUtils.deleteFiles(tempHomeDirectory);
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
