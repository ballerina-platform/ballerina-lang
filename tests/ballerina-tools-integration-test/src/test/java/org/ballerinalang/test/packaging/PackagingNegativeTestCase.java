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

import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Testing negative scenarios for pushing, pulling, searching a package from central and installing package to
 * home repository.
 *
 * @since 0.982.0
 */
public class PackagingNegativeTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-negative-project-");
        moduleName = moduleName + TestUtils.randomModuleName(10);
        envVariables = addEnvVariables(TestUtils.getEnvVariables());
        TestUtils.createSettingToml(tempHomeDirectory);
    }

    @Test(description = "Test pushing a package to central without an org-name")
    public void testPushWithoutOrg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutOrg");
        initProject(projectPath);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "ballerina: an orgName is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";

        String[] clientArgs = {moduleName};

        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without a version")
    public void testPushWithoutVersion() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutVersion");
        initProject(projectPath);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n orgName = \"bcintegrationtest\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "ballerina: a package version is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";

        String[] clientArgs = {moduleName};
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central with a reserved org-name")
    public void testPushWithAReservedOrgName() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithReservedOrg");
        initProject(projectPath);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n orgName = \"ballerina\"\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "ballerina: invalid organization name: 'ballerina'. 'ballerina' and 'ballerinax' are reserved " +
                "organization names that are used by Ballerina";

        String[] clientArgs = {moduleName};
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without any content in the manifest")
    public void testPushWithoutManifest() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutManifest");
        initProject(projectPath);
        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            writeToFile(manifestFilePath, "");
        }
        String msg = "ballerina: an orgName is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";
        String[] clientArgs = {moduleName};
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without Module.md")
    public void testPushWithoutModuleMD() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutModuleMD");
        initProject(projectPath);

        // Delete Module.md
        Path moduleMDFilePath = projectPath.resolve(moduleName).resolve("Module.md");
        Files.deleteIfExists(moduleMDFilePath);

        String[] clientArgs = {moduleName};
        String msg = "ballerina: cannot find Module.md file in the artifact";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without any content in Module.md")
    public void testPushWithoutPackagMDContent() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutModuleMDContent");
        initProject(projectPath);

        // Delete Module.md
        Path moduleMDFilePath = projectPath.resolve(moduleName).resolve("Module.md");
        writeToFile(moduleMDFilePath, "");

        String[] clientArgs = {moduleName};
        String msg = "ballerina: Module.md in the artifact is empty";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without package summary in Module.md")
    public void testPushWithoutPackagSummary() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackageSummary");
        initProject(projectPath);

        // Delete Module.md
        Path moduleMDFilePath = projectPath.resolve(moduleName).resolve("Module.md");
        writeToFile(moduleMDFilePath, "## Hello World");

        String[] clientArgs = {moduleName};
        String msg = "ballerina: cannot find package summary";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central with a package summary greater than 50 characters in " +
            "Module.md")
    public void testPushWithLongPackagSummary() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithLongPackageSummary");
        initProject(projectPath);

        // Delete Module.md
        Path moduleMDFilePath = projectPath.resolve(moduleName).resolve("Module.md");
        writeToFile(moduleMDFilePath, "Hello I am the test package which was created during an integration" +
                "test\n");

        String[] clientArgs = {moduleName};
        String msg = "ballerina: summary of the package exceeds 50 characters";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to invalid repository")
    public void testPushToInvalidRepository() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInvalidRepo");
        initProject(projectPath);

        String[] clientArgs = {moduleName, "--repository", "test"};
        String msg = "ballerina: unknown repository provided to push the package";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package without Ballerina.toml in the project directory")
    public void testPushWithoutBallerinaToml() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutToml");
        initProject(projectPath);

        // Remove Ballerina.toml
        Files.deleteIfExists(projectPath.resolve("Ballerina.toml"));

        String msg = "ballerina: couldn't locate Ballerina.toml in the project directory. Run 'bal init' to " +
                "create the Ballerina.toml file automatically and re-run the 'bal push' command";

        String[] clientArgs = {moduleName};
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test search without keywords")
    public void testSearchWithoutKeyWords() throws Exception {
        String msg = "ballerina: no keyword given";
        balClient.runMain("search", new String[0], envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test search for non-existing package")
    public void testSearchForNonExistingPackage() throws Exception {
        String msg = "no packages found";
        balClient.runMain("search", new String[0], envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Pull a non-existing module")
    public void testPullNonExistingPackage() throws Exception {
        String msg = "could not find module natasha/nomodule:*";
        LogLeecher leecher = new LogLeecher(msg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("pull", new String[]{"natasha/nomodule"}, envVariables, new String[0],
                new LogLeecher[]{leecher}, balServer.getServerHome());
        leecher.waitForText(5000);
    }

    @Test(description = "Test install already installed package")
    public void testInstallPreInstalledSource() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInstall");
        initProject(projectPath);

        // First install the package
        String[] clientArgs = {moduleName};
        balClient.runMain("install", clientArgs, envVariables, new String[0], new LogLeecher[0],
                projectPath.toString());

        // Try to install it again
        balClient.runMain("install", clientArgs, envVariables, new String[0], new LogLeecher[0],
                projectPath.toString());
        String msg = "ballerina: ballerina package exists in the home repository";
        balClient.runMain("install", new String[0], envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test build with too many arguments")
    public void testBuildWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'bal help' for usage.";
        balClient.runMain("build", new String[] {"hello", "world"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test install with too many arguments")
    public void testInstallWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'bal help' for usage.";
        balClient.runMain("install", new String[] {"foo", "bar"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test push with too many arguments")
    public void testPushWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'bal help' for usage.";
        balClient.runMain("push", new String[] {"bcintegrationtest", "foo"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test list with too many arguments")
    public void testSearchWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'bal help' for usage.";
        balClient.runMain("list", new String[] {"wso2", "twitter"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall without any arguments")
    public void testUninstallWithoutArgs() throws Exception {
        String msg = "ballerina: no package given";
        balClient.runMain("uninstall", new String[0], envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall with too many arguments")
    public void testUninstallWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'bal help' for usage.";
        balClient.runMain("uninstall", new String[] {"bcintegrationtest", "testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall without an org-name")
    public void testUninstallWithoutOrg() throws Exception {
        String msg = "error: no orgName is provided";
        balClient.runMain("uninstall", new String[] {"testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall without a version")
    public void testUninstallWithoutVersion() throws Exception {
        String msg = "error: no package version is provided";
        balClient.runMain("uninstall", new String[] {"bcintegrationtest/testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall with a non-existing package")
    public void testUninstallWithNonExistingPackage() throws Exception {
        String msg = "error: incorrect package signature provided bcintegrationtest/testxyz:1.1.0";
        balClient.runMain("uninstall", new String[] {"bcintegrationtest/testxyz:1.1.0"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test push without any packages in the project")
    public void testPushAllWithoutPackages() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackages");
        Files.createDirectories(projectPath);

        Files.createFile(projectPath.resolve("main.bal"));
        String content = "import ballerina/io;\n \n function main(string... args) {\n" +
                         "    io:println(\"Hello World!\");\n }\n";
        writeToFile(projectPath.resolve("main.bal"), content);

        String msg = "ballerina: no packages found to push in " + projectPath.toString();
        balClient.runMain("push", new String[0], envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test running a bal file inside a package within a project")
    public void testRunningBalInsidePackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectxyz");
        initProject(projectPath);
        String msg = "error: you are trying to run a ballerina file inside a package within a project. Try running " +
                "'ballerina run <package-name>'";
        String sourcePath = Path.of(moduleName, "main.bal").toString();
        balClient.runMain("run", new String[] {sourcePath}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test building a bal file inside a package within a project",
            dependsOnMethods = "testRunningBalInsidePackage")
    public void testBuildingBalInsidePackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectxyz");
        String msg = "error: you are trying to build a ballerina file inside a package within a project. Try running " +
                "'bal build <package-name>'";
        String sourcePath = Path.of(moduleName, "main.bal").toString();
        balClient.runMain("build", new String[] {sourcePath}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package with an invalid org-name")
    public void testPushWithInvalidOrg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithInvalidOrg");
        initProject(projectPath);

        // Remove org-name from manifest
        Path manifestFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(manifestFilePath)) {
            String content = "[project]\n orgName = \"foo-bar\"\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "error: invalid organization name provided 'foo-bar'. Only lowercase alphanumerics and " +
                "underscores are allowed in an organization name and the maximum length is 256 characters";

        String[] clientArgs = {moduleName};

        balClient.runMain("push", clientArgs, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test installing a package with an invalid org-name",
            dependsOnMethods = "testPushWithInvalidOrg")
    public void testInstallWithInvalidOrg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithInvalidOrg");
        String msg = "error: invalid organization name provided 'foo-bar'. Only lowercase alphanumerics and " +
                "underscores are allowed in an organization name and the maximum length is 256 characters";

        String[] clientArgs = {moduleName};
        balClient.runMain("install", clientArgs, envVariables, new String[0], new LogLeecher[]{new LogLeecher(msg)},
                          projectPath.toString());
    }

    @Test(description = "Test pushing a package with an invalid package name")
    public void testPushWithInvalidPkg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithInvalidPkg");
        initProject(projectPath);

        // Rename package-name
        Path invalidPkgPath = projectPath.resolve("hello-pkg");
        Files.createDirectories(invalidPkgPath);
        FileUtils.copyDirectory(projectPath.resolve(moduleName).toFile(), invalidPkgPath.toFile());
        String msg = "error: invalid package name provided 'hello-pkg'. Only alphanumerics, underscores and periods " +
                "are allowed in a package name and the maximum length is 256 characters";

        String[] clientArgs = {"hello-pkg"};
        balClient.runMain("push", clientArgs, envVariables, new String[0], new LogLeecher[]{new LogLeecher(msg)},
                          projectPath.toString());
    }

    @Test(description = "Test installing a package with an invalid package name",
            dependsOnMethods = "testPushWithInvalidPkg")
    public void testInstallWithInvalidPkg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithInvalidPkg");
        String msg = "error: invalid package name provided 'hello-pkg'. Only alphanumerics, underscores and periods " +
                "are allowed in a package name and the maximum length is 256 characters";

        String[] clientArgs = {"hello-pkg"};
        balClient.runMain("install", clientArgs, envVariables, new String[0], new LogLeecher[]{new LogLeecher(msg)},
                          projectPath.toString());
    }
    /**
     * Init project used to test the scenario.
     *
     * @param projectPath project path
     * @throws IOException            if an I/O exception occurs when creating directories
     * @throws BallerinaTestException
     */
    private void initProject(Path projectPath) throws IOException, BallerinaTestException {
        Files.createDirectories(projectPath);
        String[] clientArgsForInit = {"-i"};
        String[] options = {"\n", "bcintegrationtest\n", "\n", "m\n", moduleName + "\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                                                projectPath.toString());
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
        TestUtils.deleteFiles(tempHomeDirectory);
        TestUtils.deleteFiles(tempProjectDirectory);
    }
}
