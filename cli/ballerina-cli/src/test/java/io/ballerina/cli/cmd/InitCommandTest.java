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
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test cases for bal init command.
 *
 * @since 2.0.0
 */
public class InitCommandTest extends BaseCommandTest {

    @Test(description = "Initialize a new empty project within a directory")
    public void testInitCommand() throws IOException {
        Path projectPath = tmpDir.resolve("sample1");
        Files.createDirectory(projectPath);
        Path balFile = projectPath.resolve("data.bal");
        Files.createFile(balFile);

        String[] args = {};
        InitCommand initCommand = new InitCommand(projectPath, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(balFile));
        Assert.assertTrue(Files.exists(projectPath.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                projectPath.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent, expectedContent);

        Path testPath = projectPath.resolve(ProjectConstants.TEST_DIR_NAME);
        Assert.assertFalse(Files.exists(testPath));

        Path resourcePath = projectPath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));
        Assert.assertTrue(Files.notExists(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with main template")
    public void testInitCommandWithMain() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample2");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "main"};
        InitCommand initCommand = new InitCommand(packageDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - main.bal
        // - resources
        // - tests
        //      - main_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent, expectedContent);

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with service template")
    public void testInitCommandWithService() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample3");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "service"};
        InitCommand initCommand = new InitCommand(packageDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - hello_service.bal
        // - resources
        // - tests
        //      - hello_service_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent, expectedContent);

        Assert.assertTrue(Files.exists(packageDir.resolve("service.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with lib template")
    public void testInitCommandWithLib() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample4");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "lib"};
        InitCommand initCommand = new InitCommand(packageDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - main.bal
        // - resources
        // - tests
        //      - main_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedTomlContent = "[package]\n" +
                "org = \"" + System.getProperty("user.name") + "\"\n" +
                "name = \"myproject\"\n" +
                "version = \"0.1.0\"\n" +
                "\n" +
                "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent, expectedTomlContent);

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("myproject.bal")));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with invalid template")
    public void testInitCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"myproject", "-t", "invalid"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        Assert.assertTrue(readOutput().contains("Template not found"));
    }

    @Test(description = "Test init command with multiple arguments")
    public void testInitCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "sample3"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    @Test(description = "Test init command with argument and a help")
    public void testInitCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "--help"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains(
                "ballerina-init - Create a new Ballerina package inside the current directory"));
    }

    @Test(description = "Test init command with help flag")
    public void testInitCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains(
                "ballerina-init - Create a new Ballerina package inside the current directory"));
    }

    @Test(description = "Test init command inside a directory with invalid package name")
    public void testInitCommandInsideDirectoryWithInvalidPackageName() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("my-app");
        Files.createDirectory(projectPath);
        String[] args = {};
        InitCommand initCommand = new InitCommand(projectPath, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve(ProjectConstants.BALLERINA_TOML)));

        Assert.assertTrue(readOutput().contains("Unallowed characters in the project name were replaced by " +
                "underscores when deriving the package name. Edit the Ballerina.toml to change it."));
    }

    @Test(description = "Test init command with invalid project name")
    public void testInitCommandWithInvalidProjectName() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample5");
        Files.createDirectory(projectPath);
        String[] args = {"hello-app"};
        InitCommand initCommand = new InitCommand(projectPath, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("Invalid package name :"));
    }

    @Test(description = "Test init command inside a ballerina project", dependsOnMethods = "testInitCommand")
    public void testInitCommandInsideProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample1");
        String[] args = {};
        InitCommand initCommand = new InitCommand(projectPath, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        //initialize a project again
        Assert.assertTrue(readOutput().contains("Directory is already a Ballerina project"));
    }

    @Test(description = "Test init command within a ballerina project", dependsOnMethods = "testInitCommand")
    public void testInitCommandWithinBallerinaProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample1");
        String[] args = {};

        Path projectDir = projectPath.resolve("project1");
        Files.createDirectory(projectDir);
        //initialize a project again
        InitCommand initCommand = new InitCommand(projectDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();
        Assert.assertTrue(readOutput().contains("Directory is already within a Ballerina project"));
    }
}
