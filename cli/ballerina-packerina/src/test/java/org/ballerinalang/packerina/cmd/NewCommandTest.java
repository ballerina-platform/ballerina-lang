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
        String[] args = {"project-name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project-name/
        // - Ballerina.toml
        // - src/
        // - tests/
        // -- *.bal           <- integration test code
        // -- resources/      <- integration test resources
        // - .gitignore       <- git ignore file

        Path projectPath = tmpDir.resolve("project-name");
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("src")));
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src")));
        Path integrationTestPath = projectPath.resolve("tests");
        Assert.assertTrue(Files.exists(integrationTestPath));
        Assert.assertTrue(Files.isDirectory(integrationTestPath));
        Assert.assertTrue(Files.exists(integrationTestPath.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(integrationTestPath.resolve("resources")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".gitignore")));

        Assert.assertTrue(readOutput().contains("Created new ballerina project at "));
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

        Assert.assertTrue(readOutput().contains("Ballerina new - Create a Ballerina project"));
    }

    @Test(description = "Test new command with help flag")
    public void testNewCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("Ballerina new - Create a Ballerina project"));
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

    @Test(description = "Test if initializing inside a sub directory")
    public void testNewCommandInsideProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"parent"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {"subdir"};
        newCommand = new NewCommand(tmpDir.resolve("parent").resolve("src"), printStream);
        new CommandLine(newCommand).parse(args2);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("Created new ballerina project at subdir"),
                "Project created in sub directory");
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("src").resolve("subdir")));
    }

    // Test if a path given to new command
}
