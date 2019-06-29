package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InitCommandTest extends CommandTest {

    @Test(description = "Test init command with help flag")
    public void testInitCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        InitCommand initCommand = new InitCommand(tmpDir, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        String output = readOutput();
        Assert.assertTrue(output.contains("Ballerina init - Initializes a Ballerina project"));
        Assert.assertFalse(output.contains("--interactive"));
    }


    @Test(description = "Initialize a new project")
    public void testInitCommand() throws IOException {
        String[] args = {};
        Path projectPath = tmpDir.resolve("project-name");
        Files.createDirectory(projectPath);

        InitCommand initCommand = new InitCommand(projectPath, printStream);
        new CommandLine(initCommand).parse(args);
        initCommand.execute();

        // Check with spec
        // project-name/
        // - Ballerina.toml
        // - src/
        // - tests/
        // -- *.bal           <- integration test code
        // -- resources/      <- integration test resources
        // - .gitignore       <- git ignore file


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

        Assert.assertTrue(readOutput().contains("Ballerina project initialised "));
    }

    @Test(description = "Test if initializing inside a sub directory")
    public void testInitCommandInExistingProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"parent"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {};
        InitCommand initCommand = new InitCommand(tmpDir.resolve("parent"), printStream);
        new CommandLine(initCommand).parse(args2);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("Directory is already a ballerina project"));
    }

    @Test(description = "Test if initializing inside a sub directory")
    public void testInitCommandInsideProjectSubdirectory() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"parent"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {};
        InitCommand initCommand = new InitCommand(tmpDir.resolve("parent").resolve("src"), printStream);
        new CommandLine(initCommand).parse(args2);
        initCommand.execute();

        Assert.assertTrue(readOutput().contains("Directory is already within a ballerina project"));
    }
}
