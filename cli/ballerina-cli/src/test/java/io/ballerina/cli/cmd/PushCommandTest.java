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

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Push command tests.
 *
 * @since 2.0.0
 */
@PrepareForTest({ RepoUtils.class })
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.net.*", "com.sun.*"})
public class PushCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validApplicationProject";
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Files.walkFileTree(Paths.get(testResourcesURI),
                               new BuildCommandTest.Copy(Paths.get(testResourcesURI), this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Push package with too many args")
    public void testPushWithTooManyArgs() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parse("tests");
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("bal push "));
    }

    @Test(description = "Push package without bala directory")
    public void testPushWithoutBalaDir() throws IOException {
        String expected = "cannot find bala file for the package: winery. Run "
                + "'bal pack' to compile and generate the bala.";

        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parse();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Push package without bala file")
    public void testPushWithoutBala() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        // Build project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("compile-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());

        // Delete bala
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().delete());

        // Push
        String expected = "cannot find bala file for the package: winery. Run "
                + "'bal pack' to compile and generate the bala.";
        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parse();
        pushCommand.execute();

        buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Test push command with argument and a help")
    public void testPushCommandArgAndHelp() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parse(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push packages to Ballerina Central"));
    }

    @Test(description = "Test push command with help flag")
    public void testPushCommandWithHelp() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        // Test if no arguments was passed in
        String[] args = { "-h" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parse(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push packages to Ballerina Central"));
    }

    @Test
    public void testPushToCustomRepo() throws IOException {
        Path validBalProject = Paths.get("build").resolve("validProjectWithTarget");
        FileUtils.copyDirectory(
                this.testResources.resolve("validProjectWithTarget").toFile(), validBalProject.toFile());
        FileUtils.moveDirectory(
                validBalProject.resolve("target-dir").toFile(), validBalProject.resolve("target").toFile());

        Path mockRepo = Paths.get("build").resolve("ballerina-home");
        // Test if no arguments was passed in
        String[] args = { "--repository=local" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parse(args);
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(mockRepo);
        pushCommand.execute();
        try {
            ProjectFiles.validateBalaProjectPath(mockRepo.resolve("repositories").resolve("local").resolve("bala")
                    .resolve("foo").resolve("winery").resolve("0.1.0").resolve("any"));
        } catch (ProjectException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testPushWithoutPackageMd() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        // Build project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());

        // Push
        String expected = "Package.md is missing in bala file";

        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parse();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test
    public void testPushWithEmptyPackageMd() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());
        Files.createFile(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));
        // Build project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());

        Files.delete(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));

        // Push
        String expected = "package md file cannot be empty";

        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parse();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test
    public void testPushToAnUnsupportedRepo() throws IOException {
        Path projectPath = this.testResources.resolve("validLibraryProject");
        // Build project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());

        String[] args = { "--repository=stdlib.local" };
        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parse(args);
        pushCommand.execute();
        String errMsg = "unsupported repository 'stdlib.local' found. Only 'local' repository is supported.";
        Assert.assertTrue(readOutput().contains(errMsg));
    }
}
