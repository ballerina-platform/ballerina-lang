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

package org.ballerinalang.packerina.cmd;

import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Test cases for ballerina new command.
 *
 * @since 1.0.0
 */
public class NewCommandTest extends CommandTest {

    @Test(description = "Initialize a new empty project")
    public void testNewCommand() throws IOException {
        String[] args = {"project_name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - main.bal
        // - resources
        // - tests
        //      - main_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("project_name");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Path testPath = packageDir.resolve(ProjectConstants.TEST_DIR_NAME);
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.isDirectory(testPath.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.GITIGNORE_FILE_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertTrue(Files.exists(resourcePath));
        Assert.assertTrue(Files.isDirectory(resourcePath));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.MODULE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina project at "));
    }

    @Test(description = "Test new command with service template")
    public void testNewCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"service_sample", "-t", "service"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - hello_service.bal
        // - resources
        // - tests
        //      - hello_service_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("service_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.MODULE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.isDirectory(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Path moduleTests = packageDir.resolve(ProjectConstants.TEST_DIR_NAME);
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
        Assert.assertTrue(Files.exists(moduleTests.resolve("hello_service_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina project at "));
    }

    @Test(description = "Test new command with lib template")
    public void testNewCommandWithLib() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"lib_sample", "-t", "lib"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - main.bal
        // - resources
        // - tests
        //      - main_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("lib_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.MODULE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
//        todo: Need to enable this check, when the lib template gets finalised
//        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.isDirectory(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Path moduleTests = packageDir.resolve(ProjectConstants.TEST_DIR_NAME);
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
//        Assert.assertTrue(Files.exists(moduleTests.resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve(ProjectConstants.RESOURCE_DIR_NAME)));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Assert.assertTrue(readOutput().contains("Created new Ballerina project at "));
    }

    @Test(description = "Test new command with invalid project name")
    public void testNewCommandWithInvalidProjectName() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"hello-app"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Assert.assertTrue(readOutput().contains("Invalid project name"));
    }

    @Test(description = "Test new command with invalid template")
    public void testNewCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"myproject", "-t", "invalid"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Assert.assertTrue(readOutput().contains("Template not found"));
    }

    @Test(description = "Test new command without arguments")
    public void testNewCommandNoArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("The following required arguments were not provided"));
    }

    @Test(description = "Test new command with multiple arguments")
    public void testNewCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "sample3"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    @Test(description = "Test new command with argument and a help")
    public void testNewCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "--help"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina project at <project-path>"));
    }

    @Test(description = "Test new command with help flag")
    public void testNewCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina project at <project-path>"));
    }

    @Test(description = "Test if directory already exists")
    public void testNewCommandDirectoryExist() throws IOException {
        // Test if no arguments was passed in
        Files.createDirectory(tmpDir.resolve("exist"));
        String[] args = {"exist"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
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
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {"subdir"};
        newCommand = new NewCommand(tmpDir.resolve("parent").resolve("sub_dir"), printStream);
        new CommandLine(newCommand).parse(args2);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("Directory is already a ballerina project"));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("subdir")));
    }

    @Test(description = "Test if creating within a ballerina project", dependsOnMethods = "testNewCommandInsideProject")
    public void testNewCommandWithinProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("parent").resolve("sub-dir");
        Files.createDirectory(projectPath);
        String[] args = {"sample"};
        NewCommand newCommand = new NewCommand(projectPath, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("Directory is already within a ballerina project"));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("sample")));
    }

    // Test if a path given to new command
}
