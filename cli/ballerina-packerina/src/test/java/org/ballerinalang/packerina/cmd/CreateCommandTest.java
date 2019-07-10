/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test cases for ballerina create command.
 *
 * @since 1.0.0
 */
public class CreateCommandTest extends CommandTest {

    private Path projectPath;
    private Path srcPath;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        String[] args = {"project-name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        projectPath = tmpDir.resolve("project-name");
        srcPath = projectPath.resolve("src");
        readOutput(true);
    }


    @Test(description = "Test create command outside a project")
    public void testCreateCommandWithoutProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        CreateCommand createCommand = new CreateCommand(tmpDir, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("not a ballerina project"));
    }

    @Test(description = "Test create command without arguments")
    public void testCreateCommandNoArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("The following required arguments were not provided"));
    }

    @Test(description = "Test create command")
    public void testCreateCommand() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mainmodule"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();
        // Validate against spec
        // -- mymodule/
        // --- Module.md      <- module level documentation
        // --- main.bal       <- Contains default main method.
        // --- resources/     <- resources for the module (available at runtime)
        // --- tests/         <- tests for this module (e.g. unit tests)
        // ---- testmain.bal  <- test file for main
        // ---- resources/    <- resources for these tests
        Path moduleDir = srcPath.resolve("mainmodule");

        Assert.assertTrue(Files.exists(moduleDir));
        Assert.assertTrue(Files.isDirectory(moduleDir));
        Assert.assertTrue(Files.exists(moduleDir.resolve("Module.md")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("main.bal")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleDir.resolve("resources")));

        Path moduleTests = moduleDir.resolve("tests");
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
        Assert.assertTrue(Files.exists(moduleTests.resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve("resources")));

        Assert.assertTrue(readOutput().contains("Created new ballerina module"));

    }

    @Test(description = "Test create command with service template")
    public void testCreateCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"servicemodule", "-t", "service"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        // Validate against spec
        // -- mymodule/
        // --- Module.md      <- module level documentation
        // --- main.bal       <- Contains default main method.
        // --- resources/     <- resources for the module (available at runtime)
        // --- tests/         <- tests for this module (e.g. unit tests)
        // ---- testmain.bal  <- test file for main
        // ---- resources/    <- resources for these tests
        Path moduleDir = srcPath.resolve("servicemodule");

        Assert.assertTrue(Files.exists(moduleDir));
        Assert.assertTrue(Files.isDirectory(moduleDir));
        Assert.assertTrue(Files.exists(moduleDir.resolve("Module.md")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleDir.resolve("resources")));

        Path moduleTests = moduleDir.resolve("tests");
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
        Assert.assertTrue(Files.exists(moduleTests.resolve("hello_service_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve("resources")));

        Assert.assertTrue(readOutput().contains("Created new ballerina module"));
    }


    // if an invalid template is passed
    @Test(description = "Test create command with invalid template")
    public void testCreateCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mymodule2", "-t", "invalid"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("Template not found"));
    }


    // if invalid module name is passed
    @Test(description = "Test create command with invalid module name")
    public void testCreateCommandWithInvalidName() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mymo-dule"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("Invalid module name"));
    }


    @Test(description = "Test create list")
    public void testCreateCommandList() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"--list"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        String output = readOutput();
        Assert.assertTrue(output.contains("Available templates:"));
        Assert.assertTrue(output.contains("main"));
        Assert.assertTrue(output.contains("service"));
    }

    @Test(description = "Test create command with help flag")
    public void testCreateCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("Ballerina create - Create a Ballerina module"));
    }

    @Test(description = "Test create command", dependsOnMethods = {"testCreateCommand"})
    public void testCreateCommandWithExistingModuleName() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mainmodule"};
        CreateCommand createCommand = new CreateCommand(projectPath, printStream);
        new CommandLine(createCommand).parse(args);
        createCommand.execute();

        Assert.assertTrue(readOutput().contains("A module already exists with the given name"));
    }
}
