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
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_STAGE_CENTRAL;

/**
 * Testing negative scenarios for pushing, pulling, searching a package from central to
 * home repository.
 *
 * @since 0.982.0
 */
public class PackagingNegativeTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private Map<String, String> envVariables;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-negative-project-");
        moduleName = moduleName + PackerinaTestUtils.randomModuleName(10);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test pushing a package to central without an org-name")
    public void testPushWithoutOrg() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithoutOrg");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }

        String[] clientArgs = {moduleName};
        String buildMsg = "error: invalid Ballerina.toml file: cannot find 'org-name' under [project]";
        LogLeecher buildLeecher = new LogLeecher(buildMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", clientArgs, envVariables, new String[0],
                new LogLeecher[]{buildLeecher}, projectPath.toString());
        buildLeecher.waitForText(5000);
    }

    @Test(description = "Test building a module without a version")
    public void testBuildWithoutVersion() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithoutVersion");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n org-name = \"bcintegrationtest\"";
            writeToFile(manifestFilePath, content);
        }

        String[] clientArgs = {moduleName};
        String msg = "error: invalid Ballerina.toml file: cannot find 'version' under [project]";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", clientArgs, envVariables, new String[0],
                new LogLeecher[]{leecher}, projectPath.toString());
        leecher.waitForText(5000);
    }

    @Test(description = "Test building a package without any content in the manifest")
    public void testBuildWithoutManifest() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithoutManifest");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            writeToFile(manifestFilePath, "");
        }

        String moduleBuildErrMsg = "error: invalid Ballerina.toml file: organization name and the version of the "
                + "project is missing.";
        LogLeecher moduleBuildLeecher = new LogLeecher(moduleBuildErrMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleBuildLeecher}, projectPath.toString());
        moduleBuildLeecher.waitForText(5000);
    }

    @Test(description = "Test pushing a module to central without any content in Module.md")
    public void testPushWithoutModuleMDContent() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithoutModuleMDContent");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Delete Module.md
        Path moduleMDFilePath = projectPath.resolve("src").resolve(moduleName).resolve("Module.md");
        writeToFile(moduleMDFilePath, "");

        String[] clientArgs = {moduleName};
        balClient.runMain("build", clientArgs, envVariables, new String[0],
                new LogLeecher[]{}, projectPath.toString());

        String msg = "invalid request received. module md file cannot be empty.";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{leecher}, projectPath.toString());
        leecher.waitForText(5000);
    }

    @Test(description = "Test building and pushing a package without Ballerina.toml in the project directory")
    public void testPushWithoutBallerinaToml() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithoutToml");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Remove Ballerina.toml
        Files.deleteIfExists(projectPath.resolve("Ballerina.toml"));

        String[] clientArgs = {moduleName};
        // TODO: 2020-02-24: verify error message for this use case, also remove the full stop
        String buildMsg = "ballerina: you are trying to build/compile a module that is not inside a project.";
        LogLeecher buildLogLeecher = new LogLeecher(buildMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", clientArgs, envVariables, new String[]{},
                new LogLeecher[]{buildLogLeecher}, projectPath.toString());
        buildLogLeecher.waitForText(5000);

        String pushMsg = "ballerina: Push command can be only run inside a Ballerina project";
        LogLeecher pushLogLeecher = new LogLeecher(pushMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                          new LogLeecher[]{pushLogLeecher}, projectPath.toString());
        pushLogLeecher.waitForText(5000);
    }

    @Test(description = "Test search without keywords")
    public void testSearchWithoutKeyWords() throws Exception {
        String msg = "ballerina: no keyword given";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("search", new String[0], envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }

    @Test(description = "Test search for non-existing package")
    public void testSearchForNonExistingPackage() throws Exception {
        String nonExistingPackage = "KwrZYVBeKQ341";
        String msg = "no modules found";
        String[] clientArgs = {nonExistingPackage};
        LogLeecher leecher = new LogLeecher(msg);
        balClient.runMain("search", clientArgs, envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }

    @Test(description = "Pull a non-existing module")
    public void testPullNonExistingPackage() throws Exception {
        String msg = "error: module not found: natasha/nomodule:*_java11 or natasha/nomodule:*_any";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("pull", new String[]{"natasha/nomodule"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }


    @Test(description = "Test build with too many arguments")
    public void testBuildWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments.";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[] {"hello", "world"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }


    @Test(description = "Test push with too many arguments")
    public void testPushWithTooManyArgs() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithTooManyArgs");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        balClient.runMain("build", new String[] {moduleName}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());

        String msg = "ballerina: too many arguments";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("push", new String[] {moduleName, "foo"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, projectPath.toString());
        leecher.waitForText(5000);
    }

    @Test(description = "Test list with too many arguments")
    public void testSearchWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("search", new String[] {"wso2", "twitter"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }

    @Test(description = "Test build without any modules in the project")
    public void testBuildAllWithoutPackages() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackages");
        initProject(tempProjectDirectory, "projectWithoutPackages");

        Files.createFile(projectPath.resolve("main.bal"));
        String content = "import ballerina/io;\n \n function main(string... args) {\n" +
                         "    io:println(\"Hello World!\");\n }\n";
        writeToFile(projectPath.resolve("main.bal"), content);

        String msg = "error: no modules found to compile";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, projectPath.toString());
        leecher.waitForText(5000);
    }

    @Test(description = "Test pushing a package with an invalid org-name")
    public void testPushWithInvalidOrg() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithReservedOrg");
        createProjectStructureAndGetProjectPath(projectPath, moduleName);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n org-name = \"foo-bar\"\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }

        String moduleBaloFileName = moduleName + "-"
                + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                + ProgramFileConstants.ANY_PLATFORM + "-"
                + "0.0.2"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        String moduleBuildMsg = "target" + File.separator + "balo" + File.separator + moduleBaloFileName;
        LogLeecher moduleBuildLeecher = new LogLeecher(moduleBuildMsg);
        balClient.runMain("build", new String[]{"-c", "-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleBuildLeecher}, projectPath.toString());
        moduleBuildLeecher.waitForText(5000);

        String msg = "error: invalid organization name provided 'foo-bar'. Only lowercase alphanumerics and " +
                "underscores are allowed in an organization name and the maximum length is 256 characters";
        String[] clientArgs = {moduleName};
        LogLeecher modulePushLeecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                          new LogLeecher[]{modulePushLeecher}, projectPath.toString());
        modulePushLeecher.waitForText(5000);
    }


    @Test(description = "Test pushing a package with an invalid package name")
    public void testPushWithInvalidPkg() throws Exception {
        Path projectPath = Files.createDirectories(tempProjectDirectory).resolve("projectWithInvalidPkg");
        createProjectStructureAndGetProjectPath(projectPath, "hello-pkg");

        String[] clientArgs = {"hello-pkg"};
        balClient.runMain("build", clientArgs, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());

        String msg = "error: invalid module name provided 'hello-pkg'. Only alphanumerics, underscores and periods" +
                " are allowed in a module name and the maximum length is 256 characters";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("push", clientArgs, envVariables, new String[0], new LogLeecher[]{leecher},
                          projectPath.toString());
        leecher.waitForText(5000);
    }


    /**
     * Init project used to test the scenario.
     *
     * @param projectPath project path
     * @throws IOException            if an I/O exception occurs when creating directories
     * @throws BallerinaTestException
     */
    private void initProject(Path projectPath, String projectName) throws IOException, BallerinaTestException {
        Files.createDirectories(projectPath);
        balClient.runMain("new", new String[] { projectName }, envVariables, new String[] {}, new LogLeecher[0],
                projectPath.toString());
    }

    private void createProjectStructureAndGetProjectPath(Path projectDirPath, String moduleName) throws IOException {
        // Copy TestProject1 to projectDir
        Path originalTestProj = Paths.get("src", "test", "resources", "packaging", "packagingNegative", "TestProject1")
                .toAbsolutePath();
        PackerinaTestUtils.copyFolder(originalTestProj, projectDirPath);

        // rename module names
        Path testProjModule1Path = projectDirPath.resolve("src").resolve(moduleName);
        Files.createDirectories(projectDirPath.resolve("src").resolve(moduleName));
        PackerinaTestUtils.copyFolder(projectDirPath.resolve("src").resolve("foo"), testProjModule1Path);
        deleteFiles(projectDirPath.resolve("src").resolve("foo"));
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put(BALLERINA_STAGE_CENTRAL, "true");
        envVariables.put("BALLERINA_CENTRAL_ACCESS_TOKEN", PackerinaTestUtils.getToken());
        return envVariables;
    }

    /**
     * Override content in file.
     *
     * @param filePath path of file to override
     * @param content  content to be written
     * @throws IOException
     */
    private void writeToFile(Path filePath, String content) throws IOException {
        Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackerinaTestUtils.deleteFiles(tempHomeDirectory);
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
