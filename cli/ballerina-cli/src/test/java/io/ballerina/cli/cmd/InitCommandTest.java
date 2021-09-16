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
import org.wso2.ballerinalang.util.RepoUtils;
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
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(balFile));
        Assert.assertTrue(Files.exists(projectPath.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                projectPath.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Path testPath = projectPath.resolve(ProjectConstants.TEST_DIR_NAME);
        Assert.assertFalse(Files.exists(testPath));

        Path resourcePath = projectPath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));
        Assert.assertTrue(Files.notExists(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertFalse(Files.exists(projectPath.resolve("main.bal")));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with main template", enabled = false)
    public void testInitCommandWithMain() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample2");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "main"};
        InitCommand initCommand = new InitCommand(packageDir, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - main.bal
        // - tests
        //      - main_test.bal
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with service template", enabled = false)
    public void testInitCommandWithService() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample3");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "service"};
        InitCommand initCommand = new InitCommand(packageDir, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - Package.md
        // - service.bal
        // - tests
        //      - service_test.bal
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("service.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with lib template", enabled = false)
    public void testInitCommandWithLib() throws IOException {
        // Test if no arguments was passed in
        Path packageDir = tmpDir.resolve("sample4");
        Files.createDirectory(packageDir);
        String[] args = {"myproject", "-t", "lib"};
        InitCommand initCommand = new InitCommand(packageDir, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        // Check with spec
        // foo/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - lib.bal
        // - resources
        // - tests
        //      - lib_test.bal
        // - .gitignore       <- git ignore file

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedTomlContent = "[package]\n" +
                "org = \"" + System.getProperty("user.name").replaceAll("[^a-zA-Z0-9_]", "_") + "\"\n" +
                "name = \"myproject\"\n" +
                "version = \"0.1.0\"\n" +
                "distribution = \"" + RepoUtils.getBallerinaShortVersion() + "\"\n" +
                "\n";
        Assert.assertTrue(tomlContent.contains(expectedTomlContent));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("lib.bal")));

        Assert.assertTrue(readOutput().contains("Created new Ballerina package"));
    }

    @Test(description = "Test init command with invalid template", enabled = false)
    public void testInitCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"myproject", "-t", "invalid"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        Assert.assertTrue(readOutput().contains("template not found"));
    }

    @Test(description = "Test init command with multiple arguments")
    public void testInitCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "sample3"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream, false);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    @Test(description = "Test init command with argument and a help")
    public void testInitCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "--help"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream, false);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains(
                "ballerina-init - Create a new Ballerina package inside the current directory"));
    }

    @Test(description = "Test init command with help flag")
    public void testInitCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream, false);
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
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve(ProjectConstants.BALLERINA_TOML)));

        Assert.assertTrue(readOutput().contains("unallowed characters in the project name were replaced by " +
                "underscores when deriving the package name. Edit the Ballerina.toml to change it."));
    }

    @Test(description = "Test init command with invalid project name")
    public void testInitCommandWithInvalidProjectName() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample5");
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }
        String[] args = {"hello-app"};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid package name :"));
    }

    @Test(description = "Test init command with project name has more than 256 characters")
    public void testInitCommandWithProjectNameHasMoreThan256Chars() throws IOException {
        Path projectPath = tmpDir.resolve("sample5");
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }
        String packageName = "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting";
        String[] args = {packageName};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid package name : '" + packageName + "' :\n"
                + "Maximum length of package name is 256 characters."));
    }

    @Test(description = "Test init command with module name has initial underscore")
    public void testInitCommandWithNameHasInitialUnderscore() throws IOException {
        Path projectPath = tmpDir.resolve("sample5");
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }

        String pkgName = "_my_package";
        String[] args = {pkgName};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid module name : '" + pkgName + "' :\n"
                + "Package name cannot have initial underscore characters."));
    }

    @Test(description = "Test add command with module name has trailing underscore")
    public void testAddCommandWithNameHasTrailingUnderscore() throws IOException {
        Path projectPath = tmpDir.resolve("sample5");
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }

        String pkgName = "my_package_";
        String[] args = {pkgName};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid module name : '" + pkgName + "' :\n"
                + "Package name cannot have trailing underscore characters."));
    }

    @Test(description = "Test add command with module name has consecutive underscores")
    public void testAddCommandWithNameHasConsecutiveUnderscores() throws IOException {
        Path projectPath = tmpDir.resolve("sample5");
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }

        String pkgName = "my__package";
        String[] args = {pkgName};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid module name : '" + pkgName + "' :\n"
                + "Package name cannot have consecutive underscore characters."));
    }

    @Test(description = "Test init command inside a ballerina project", dependsOnMethods = "testInitCommand")
    public void testInitCommandInsideProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample1");
        String[] args = {};
        InitCommand initCommand = new InitCommand(projectPath, printStream, false);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        //initialize a project again
        Assert.assertTrue(readOutput().contains("directory is already a Ballerina project."));
    }

    @Test(description = "Test init command within a ballerina project", dependsOnMethods = "testInitCommand")
    public void testInitCommandWithinBallerinaProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("sample1");
        String[] args = {};

        Path projectDir = projectPath.resolve("project1");
        Files.createDirectory(projectDir);
        //initialize a project again
        InitCommand initCommand = new InitCommand(projectDir, printStream, false);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();
        Assert.assertTrue(readOutput().contains("directory is already within a Ballerina project"));
    }
}
