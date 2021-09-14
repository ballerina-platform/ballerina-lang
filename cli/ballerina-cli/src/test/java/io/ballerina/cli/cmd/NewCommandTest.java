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

package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


/**
 * Test cases for bal new command.
 *
 * @since 2.0.0
 */
public class NewCommandTest extends BaseCommandTest {
    private Path testResources;
    private Path centralCache;

    @DataProvider(name = "invalidProjectNames")
    public Object[][] provideInvalidProjectNames() {
        return new Object[][] {
                { "hello-app" },
                { "my$project" }
        };
    }

    @BeforeClass
    public void setup() throws IOException {

        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            this.centralCache = this.testResources.resolve("repositories/central.ballerina.io/bala/");
            Files.createDirectories(this.centralCache);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
        Path balacacheTemplatePath = this.testResources.resolve("balacache-templates");
        Files.walkFileTree(balacacheTemplatePath, new BuildCommandTest.Copy(balacacheTemplatePath,
                this.centralCache));
    }

    @Test(description = "Create a new project")
    public void testNewCommand() throws IOException {
        String[] args = {"project_name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - main.bal

        Path packageDir = tmpDir.resolve("project_name");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertFalse(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test new command with main template")
    public void testNewCommandWithMain() throws IOException {
        String[] args = {"main_sample", "-t", "main"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - main.bal
        // - tests
        //      - main_test.bal

        Path packageDir = tmpDir.resolve("main_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test new command with service template")
    public void testNewCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"service_sample", "-t", "service"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - service.bal
        // - tests
        //      - service_test.bal
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("service_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("service.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test new command with lib template")
    public void testNewCommandWithLib() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"lib_sample", "-t", "lib"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - lib.bal
        // - resources
        // - tests
        //      - lib_test.bal
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("lib_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));

        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);

        String expectedTomlContent = "[package]\n" +
                "org = \"" + System.getProperty("user.name").replaceAll("[^a-zA-Z0-9_]", "_") + "\"\n" +
                "name = \"lib_sample\"\n" +
                "version = \"0.1.0\"\n" +
                "distribution = \"" + RepoUtils.getBallerinaShortVersion() + "\"" +
                "\n";
        Assert.assertTrue(tomlContent.contains(expectedTomlContent));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("lib.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test new command with invalid project name", dataProvider = "invalidProjectNames")
    public void testNewCommandWithInvalidProjectName(String projectName) throws IOException {
        // Test if no arguments was passed in
        String[] args = { projectName };
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Path packageDir = tmpDir.resolve(projectName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(readOutput().contains("unallowed characters in the project name were replaced by " +
                "underscores when deriving the package name. Edit the Ballerina.toml to change it."));
    }

    @Test(description = "Test new command with invalid template")
    public void testNewCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"myproject", "-t", "invalid"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Assert.assertTrue(readOutput().contains("template not found"));
    }

    @Test(description = "Test new command with central template in the local cache")
    public void testNewCommandWithTemplateInLocalCache() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "admin/Sample:0.1.5";
        String[] args = {"sample_pull_local", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample_pull_local");
        Assert.assertTrue(Files.exists(packageDir));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String[] templateSplit = templateArg.split("/");
        String expectedTomlContent = "[package]\n" +
                "org = \"" + templateSplit[0] + "\"\n" +
                "name = \"" + templateSplit[1].split(":")[0] + "\"\n" +
                "version = \"" +  templateSplit[1].split(":")[1] + "\"\n";
        Assert.assertTrue(tomlContent.contains(expectedTomlContent));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test new command without arguments")
    public void testNewCommandNoArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("project name is not provided"));
    }

    @Test(description = "Test new command with multiple arguments")
    public void testNewCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "sample3"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    @Test(description = "Test new command with argument and a help")
    public void testNewCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "--help"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina package"));
    }

    @Test(description = "Test new command with help flag")
    public void testNewCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina package"));
    }

    @Test(description = "Test if directory already exists")
    public void testNewCommandDirectoryExist() throws IOException {
        // Test if no arguments was passed in
        Files.createDirectory(tmpDir.resolve("exist"));
        String[] args = {"exist"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("destination '"
                + tmpDir.resolve("exist").toString()
                + "' already exists"));
    }

    @Test(description = "Test if creating inside a ballerina project")
    public void testNewCommandInsideProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"parent"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {"subdir"};
        newCommand = new NewCommand(tmpDir.resolve("parent").resolve("sub_dir"), printStream, false);
        new CommandLine(newCommand).parse(args2);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("directory is already a ballerina project."));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("subdir")));
    }

    @Test(description = "Test if creating within a ballerina project", dependsOnMethods = "testNewCommandInsideProject")
    public void testNewCommandWithinProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("parent").resolve("sub-dir");
        Files.createDirectory(projectPath);
        String[] args = {"sample"};
        NewCommand newCommand = new NewCommand(projectPath, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("directory is already within a ballerina project."));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("sample")));
    }

    // Test if a path given to new command
}
