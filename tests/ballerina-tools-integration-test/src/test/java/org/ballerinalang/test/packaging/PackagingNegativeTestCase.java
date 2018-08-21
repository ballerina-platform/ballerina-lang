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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.BaseTest;
import org.ballerinalang.test.utils.PackagingTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

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
    private String[] envVariables;

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

        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
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
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
    }

    @Test(description = "Test pushing a package to invalid repository")
    public void testPushToInvalidRepository() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInvalidRepo");
        initProject(projectPath);

        String[] clientArgs = {packageName, "--repository", "test"};
        String msg = "ballerina: unknown repository provided to push the package";
        addLogLeecher(msg);
        serverInstance.runMain(clientArgs, envVariables, "push", projectPath.toString());
    }

    @Test(description = "Test search without keywords")
    public void testSearchWithoutKeyWords() throws Exception {
        addLogLeecher("ballerina: no keyword given");
        serverInstance.runMain(new String[0], envVariables, "search");
    }

    @Test(description = "Test search for non-existing package")
    public void testSearchForNonExistingPackage() throws Exception {
        addLogLeecher("no packages found");
        serverInstance.runMain(new String[0], envVariables, "search");
    }

    @Test(description = "Test install already installed package")
    public void testInstallPreInstalledSource() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("projectToInstall");
        initProject(projectPath);

        // First install the package
        String[] clientArgs = {packageName};
        serverInstance.runMain(clientArgs, envVariables, "install", projectPath.toString());

        // Try to install it again
        serverInstance.runMain(clientArgs, envVariables, "install", projectPath.toString());
        String msg = "ballerina: ballerina package exists in the home repository";
        addLogLeecher(msg);
        serverInstance.runMain(new String[0], envVariables, "install");
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
        serverInstance.runMainWithClientOptions(clientArgsForInit, options, envVariables, "init",
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
    private String[] addEnvVariables(String[] envVariables) {
        String[] newEnvVariables = new String[]{ProjectDirConstants.HOME_REPO_ENV_KEY + "=" +
                tempHomeDirectory.toString()};
        return Stream.of(envVariables, newEnvVariables).flatMap(Stream::of).toArray(String[]::new);
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

    /**
     * Set log leecher with the log message excepted.
     *
     * @param msg log message expected
     */
    private void addLogLeecher(String msg) {
        serverInstance.resetServerLogReader();
        LogLeecher clientLeecher = new LogLeecher(msg);
        serverInstance.addLogLeecher(clientLeecher);
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackagingTestUtils.deleteFiles(tempHomeDirectory);
        PackagingTestUtils.deleteFiles(tempProjectDirectory);
    }
}
