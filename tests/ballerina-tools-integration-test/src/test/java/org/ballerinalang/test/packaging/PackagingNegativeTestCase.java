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
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private String packageName = "test";
    private Map<String, String> envVariables;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-home-");
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-negative-project-");
        packageName = packageName + PackagingTestUtils.randomPackageName(10);
        envVariables = addEnvVariables(PackagingTestUtils.getEnvVariables());
        createSettingToml();
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
        String msg = "ballerina: an org-name is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";

        String[] clientArgs = {packageName};

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
            String content = "[project]\n org-name = \"integrationtests\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "ballerina: a package version is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";

        String[] clientArgs = {packageName};
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
        String msg = "ballerina: an org-name is required when pushing. This is not specified in Ballerina.toml " +
                "inside the project";
        String[] clientArgs = {packageName};
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without Package.md")
    public void testPushWithoutPackagMD() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackageMD");
        initProject(projectPath);

        // Delete Package.md
        Path packageMDFilePath = projectPath.resolve(packageName).resolve("Package.md");
        Files.deleteIfExists(packageMDFilePath);

        String[] clientArgs = {packageName};
        String msg = "ballerina: cannot find Package.md file in the artifact";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without any content in Package.md")
    public void testPushWithoutPackagMDContent() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackageMDContent");
        initProject(projectPath);

        // Delete Package.md
        Path packageMDFilePath = projectPath.resolve(packageName).resolve("Package.md");
        writeToFile(packageMDFilePath, "");

        String[] clientArgs = {packageName};
        String msg = "ballerina: package.md in the artifact is empty";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central without package summary in Package.md")
    public void testPushWithoutPackagSummary() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithoutPackageSummary");
        initProject(projectPath);

        // Delete Package.md
        Path packageMDFilePath = projectPath.resolve(packageName).resolve("Package.md");
        writeToFile(packageMDFilePath, "## Hello World");

        String[] clientArgs = {packageName};
        String msg = "ballerina: cannot find package summary";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to central with a package summary greater than 50 characters in " +
            "Package.md")
    public void testPushWithLongPackagSummary() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithLongPackageSummary");
        initProject(projectPath);

        // Delete Package.md
        Path packageMDFilePath = projectPath.resolve(packageName).resolve("Package.md");
        writeToFile(packageMDFilePath, "Hello I am the test package which was created during an integration" +
                "test\n");

        String[] clientArgs = {packageName};
        String msg = "ballerina: summary of the package exceeds 50 characters";
        balClient.runMain("push", clientArgs, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test pushing a package to invalid repository")
    public void testPushToInvalidRepository() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInvalidRepo");
        initProject(projectPath);

        String[] clientArgs = {packageName, "--repository", "test"};
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

        String msg = "ballerina: couldn't locate Ballerina.toml in the project directory. Run 'ballerina init' to " +
                "create the Ballerina.toml file automatically and re-run the 'ballerina push' command";

        String[] clientArgs = {packageName};
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

    @Test(description = "Test install already installed package")
    public void testInstallPreInstalledSource() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInstall");
        initProject(projectPath);

        // First install the package
        String[] clientArgs = {packageName};
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
        String msg = "ballerina: too many arguments\n Run 'ballerina help' for usage.";
        balClient.runMain("build", new String[] {"hello", "world"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test install with too many arguments")
    public void testInstallWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'ballerina help' for usage.";
        balClient.runMain("install", new String[] {"foo", "bar"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test push with too many arguments")
    public void testPushWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'ballerina help' for usage.";
        balClient.runMain("push", new String[] {"integrationtests", "foo"}, envVariables, new String[0],
                new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test list with too many arguments")
    public void testSearchWithTooManyArgs() throws Exception {
        String msg = "ballerina: too many arguments\n Run 'ballerina help' for usage.";
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
        String msg = "ballerina: too many arguments\n Run 'ballerina help' for usage.";
        balClient.runMain("uninstall", new String[] {"integrationtests", "testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall without an org-name")
    public void testUninstallWithoutOrg() throws Exception {
        String msg = "error: no org-name is provided";
        balClient.runMain("uninstall", new String[] {"testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall without a version")
    public void testUninstallWithoutVersion() throws Exception {
        String msg = "error: no package version is provided";
        balClient.runMain("uninstall", new String[] {"integrationtests/testxyz"}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, balServer.getServerHome());
    }

    @Test(description = "Test uninstall with a non-existing package")
    public void testUninstallWithNonExistingPackage() throws Exception {
        String msg = "error: incorrect package signature provided integrationtests/testxyz:1.1.0";
        balClient.runMain("uninstall", new String[] {"integrationtests/testxyz:1.1.0"}, envVariables, new String[0],
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
        String sourcePath = Paths.get(packageName, "main.bal").toString();
        balClient.runMain("run", new String[] {sourcePath}, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test building a bal file inside a package within a project",
            dependsOnMethods = "testRunningBalInsidePackage")
    public void testBuildingBalInsidePackage() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectxyz");
        String msg = "error: you are trying to build a ballerina file inside a package within a project. Try running " +
                "'ballerina build <package-name>'";
        String sourcePath = Paths.get(packageName, "main.bal").toString();
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
            String content = "[project]\n org-name = \"foo-bar\"\n version = \"0.0.2\"";
            writeToFile(manifestFilePath, content);
        }
        String msg = "error: invalid organization name provided 'foo-bar'. Only lowercase alphanumerics and " +
                "underscores are allowed in an organization name and the maximum length is 256 characters";

        String[] clientArgs = {packageName};

        balClient.runMain("push", clientArgs, envVariables, new String[0],
                          new LogLeecher[]{new LogLeecher(msg)}, projectPath.toString());
    }

    @Test(description = "Test installing a package with an invalid org-name",
            dependsOnMethods = "testPushWithInvalidOrg")
    public void testInstallWithInvalidOrg() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectWithInvalidOrg");
        String msg = "error: invalid organization name provided 'foo-bar'. Only lowercase alphanumerics and " +
                "underscores are allowed in an organization name and the maximum length is 256 characters";

        String[] clientArgs = {packageName};
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
        FileUtils.copyDirectory(projectPath.resolve(packageName).toFile(), invalidPkgPath.toFile());
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
        String[] options = {"\n", "integrationtests\n", "\n", "m\n", packageName + "\n", "f\n"};
        balClient.runMain("init", clientArgsForInit, envVariables, options, new LogLeecher[0],
                                                projectPath.toString());
    }

    /**
     * Create Settings.toml inside the home repository.
     *
     * @throws IOException i/o exception when writing to file
     */
    private void createSettingToml() throws IOException {
        String content = "[central]\n accesstoken = \"0f647e67-857d-32e8-a679-bd3c1c3a7eb2\"";
        writeToFile(tempHomeDirectory.resolve("Settings.toml"), content);
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
        PackagingTestUtils.deleteFiles(tempHomeDirectory);
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
