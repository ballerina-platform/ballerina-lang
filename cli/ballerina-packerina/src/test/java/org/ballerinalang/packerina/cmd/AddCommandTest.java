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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for ballerina add command.
 *
 * @since 1.0.0
 */
public class AddCommandTest extends CommandTest {

    private Path projectPath;
    private Path srcPath;
    private Path homeCache;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        String[] args = {"project-name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        projectPath = tmpDir.resolve("project-name");
        srcPath = projectPath.resolve("src");


        // Lets create a template
        try {
            Path templateProject = super.tmpDir.resolve("template-project");
            URI testResourcesURI = getClass().getClassLoader().getResource("test-resources").toURI();
            Path validProject = Paths.get(testResourcesURI).resolve("valid-project");
            Files.walkFileTree(validProject, new BuildCommandTest.Copy(validProject, templateProject));

            // Build the project
            String[] compileArgs = {"mytemplate", "-c", "--skip-tests"};
            BuildCommand buildCommand = new BuildCommand(templateProject, printStream,
                    printStream, false, true);
            new CommandLine(buildCommand).parse(compileArgs);
            buildCommand.execute();
            readOutput(false);

            Path baloFile = templateProject.resolve("target")
                    .resolve("balo").resolve("mytemplate-2020r1-any-0.1.0.balo");

            homeCache = this.tmpDir.resolve("home-cache");
            Path baloDir = homeCache.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME)
                    .resolve("testOrg").resolve("mytemplate");
            Files.createDirectories(baloDir);
            Files.createDirectories(baloDir.resolve("0.1.0"));
            Files.createDirectories(baloDir.resolve("0.2.0"));
            Files.createDirectories(baloDir.resolve("0.2.1"));
            Files.copy(baloFile, baloDir.resolve("0.2.1").resolve("mytemplate-2020r1-any-0.2.1.balo"));
            Files.copy(baloFile, baloDir.resolve("0.1.0").resolve("mytemplate-2020r1-any-0.1.0.balo"));
            Files.copy(baloFile, baloDir.resolve("0.2.0").resolve("mytemplate-2020r1-any-0.2.0.balo"));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
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

    @Test(description = "Test add command")
    public void testAddCommand() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mainmodule"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();
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

        Assert.assertTrue(readOutput().contains("Added new ballerina module"));

    }

    @Test(description = "Test add command with service template")
    public void testAddCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"servicemodule", "-t", "service"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

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

        Assert.assertTrue(readOutput().contains("Added new ballerina module"));
    }


    // if an invalid template is passed
    @Test(description = "Test add command with invalid template")
    public void testAddCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"mymodule2", "-t", "invalid"};
        AddCommand addCommand = new AddCommand(projectPath, printStream, homeCache);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("Template not found"));
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


    @Test(description = "Test add list")
    public void testAddCommandList() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"--list"};
        AddCommand addCommand = new AddCommand(projectPath, printStream, homeCache);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        String output = readOutput();
        Assert.assertTrue(output.contains("Available templates:"));
        Assert.assertTrue(output.contains("main"));
        Assert.assertTrue(output.contains("service"));
        Assert.assertTrue(output.contains("testOrg/mytemplate"));
        Assert.assertFalse(output.contains("testOrg/another"));
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
        String[] args = {"mainmodule"};
        AddCommand addCommand = new AddCommand(projectPath, printStream);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();

        Assert.assertTrue(readOutput().contains("A module already exists with the given name"));
    }

    @Test(description = "Test add command with balo template")
    public void testAddCommandWithBaloTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"balomod", "-t testOrg/mytemplate"};
        AddCommand addCommand = new AddCommand(projectPath, printStream, homeCache);
        new CommandLine(addCommand).parseArgs(args);
        addCommand.execute();
        // Validate if all the files have copied from template
        // -- balomod/
        // --- Module.md      <- module level documentation
        // --- main.bal       <- Contains default main method.
        // --- resources/     <- resources for the module (available at runtime)
        // --- tests/         <- tests for this module (e.g. unit tests)
        // ---- testmain.bal  <- test file for main
        // ---- resources/    <- resources for these tests
        Path moduleDir = srcPath.resolve("balomod");

        Assert.assertTrue(Files.exists(moduleDir));
        Assert.assertTrue(Files.isDirectory(moduleDir));
        Assert.assertTrue(Files.exists(moduleDir.resolve("main.bal")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("resources")));
        Assert.assertTrue(Files.exists(moduleDir.resolve("Module.md")));

        Path moduleTests = moduleDir.resolve("tests");
        Assert.assertTrue(Files.exists(moduleTests));
        Assert.assertTrue(Files.isDirectory(moduleTests));
        Assert.assertTrue(Files.exists(moduleTests.resolve("main_test.bal")));
        Assert.assertTrue(Files.exists(moduleTests.resolve("resources")));
        Assert.assertTrue(Files.isDirectory(moduleTests.resolve("resources")));

        Path moduleResources = moduleDir.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        Assert.assertTrue(Files.exists(moduleResources));
        Assert.assertTrue(Files.isDirectory(moduleResources));
        Assert.assertTrue(Files.exists(moduleResources.resolve("resource.txt")));

        Assert.assertTrue(readOutput().contains("Added new ballerina module"));

    }
}
