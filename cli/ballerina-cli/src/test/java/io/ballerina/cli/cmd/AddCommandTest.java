/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test cases for ballerina add command.
 *
 * @since 2.0.0
 */
public class AddCommandTest extends BaseCommandTest {

    private Path projectPath;
    private Path modulesPath;
    private Path homeCache;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        String[] args = {"project_name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        projectPath = tmpDir.resolve("project_name");
        modulesPath = projectPath.resolve(ProjectConstants.MODULES_ROOT);

        homeCache = this.tmpDir.resolve("home-cache");
    }

    @Test(description = "Test add command")
    public void testAddCommand() throws IOException {
        String[] args = {"module1"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Path moduleDir = modulesPath.resolve("module1");
        Assert.assertTrue(Files.exists(moduleDir));
        Assert.assertTrue(Files.isDirectory(moduleDir));
        Assert.assertTrue(Files.exists(moduleDir.resolve("main.bal")));
        Assert.assertTrue(readOutput().contains("Added new ballerina module"));
    }

    @Test(description = "Test add command outside a project")
    public void testAddCommandWithoutProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        AddCommand addCommand = new AddCommand(tmpDir, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("not a ballerina project"));
    }

    @Test(description = "Test add command without arguments")
    public void testAddCommandNoArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("The following required arguments were not provided"));
    }

    @Test(description = "Test add command with multiple arguments")
    public void testAddCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"module2", "module3"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();
        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    // if an invalid template is passed
    @Test(description = "Test add command with invalid template", enabled = false)
    public void testAddCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mymodule2", "-t", "invalid"};
        AddCommand addCommand = new AddCommand(projectPath, printStream, homeCache);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("Using Ballerina Central module templates is not yet supported"));
    }

    // if invalid module name is passed
    @Test(description = "Test add command with invalid module name")
    public void testAddCommandWithInvalidName() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mymo-dule"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("Invalid module name"));
    }

    @Test(description = "Test add command with help flag")
    public void testAddCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-add - Add a new module to an existing Ballerina project"));
    }

    @Test(description = "Test add command", dependsOnMethods = {"testAddCommand"})
    public void testAddCommandWithExistingModuleName() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"module1"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("A module already exists with the given name"));
    }
}
