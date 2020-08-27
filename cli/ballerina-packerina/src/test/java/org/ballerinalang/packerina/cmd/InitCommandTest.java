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

package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test cases for ballerina init command.
 *
 * @since 1.0.0
 */
public class InitCommandTest extends CommandTest {

    @Test(description = "Initialize a new empty project")
    public void testInitCommandWithArg() throws IOException {
        String[] args = {"project_name"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();
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

        Path projectPath = tmpDir.resolve("project_name");
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Path testPath = projectPath.resolve("tests");
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(testPath.resolve("resources")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".gitignore")));
        Path resourcePath = projectPath.resolve("resources");
        Assert.assertTrue(Files.exists(resourcePath));
        Assert.assertTrue(Files.isDirectory(resourcePath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Module.md")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Package.md")));

        Assert.assertTrue(readOutput().contains("Ballerina project initialised "));
    }

    @Test(description = "Initialize a new empty project within a directory")
    public void testInitCommand() throws IOException {
        String[] args = {};
        InitCommand initCommand = new InitCommand(tmpDir.resolve("foo"), printStream);
        new CommandLine(initCommand).parse(args);
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

        Path projectPath = tmpDir.resolve("foo");
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Path testPath = projectPath.resolve("tests");
        Assert.assertTrue(Files.exists(testPath));
        Assert.assertTrue(Files.isDirectory(testPath));
        Assert.assertTrue(Files.exists(testPath.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(testPath.resolve("resources")));
        Assert.assertTrue(Files.exists(projectPath.resolve(".gitignore")));
        Path resourcePath = projectPath.resolve("resources");
        Assert.assertTrue(Files.exists(resourcePath));
        Assert.assertTrue(Files.isDirectory(resourcePath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Module.md")));
        Assert.assertTrue(Files.exists(projectPath.resolve("Package.md")));

        Assert.assertTrue(readOutput().contains("Ballerina project initialised "));
    }

    @Test(description = "Test init command with service template")
    public void testInitCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"servicemodule", "-t", "service"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();
        Path packageDir = tmpDir.resolve("servicemodule");

        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(packageDir.resolve("Module.md")));
        Assert.assertTrue(Files.exists(packageDir.resolve("Package.md")));
        Assert.assertTrue(Files.exists(packageDir.resolve("hello_service.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(packageDir.resolve("resources")));

        Path moduleTests = packageDir.resolve("tests");
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
        Assert.assertTrue(Files.exists(moduleTests.resolve("hello_service_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve("resources")));

        Assert.assertTrue(readOutput().contains("Ballerina project initialised "));
    }

    @Test(description = "Test init command with invalid template")
    public void testAddCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"servicemodule", "-t", "invalid"};
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

        Assert.assertTrue(readOutput().contains("ballerina-init - Create a new Ballerina project inside current " +
                "directory."));
    }

    @Test(description = "Test init command with help flag")
    public void testInitCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-init - Create a new Ballerina project inside current " +
                "directory."));
    }

    @Test(description = "Test init list")
    public void testInitCommandList() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"--list"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parseArgs(args);
        initCommand.execute();

        String output = readOutput();
        Assert.assertTrue(output.contains("Available templates:"));
        Assert.assertTrue(output.contains("main"));
        Assert.assertTrue(output.contains("service"));
        Assert.assertTrue(output.contains("lib"));
    }
}
