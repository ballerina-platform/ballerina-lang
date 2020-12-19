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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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
public class PushCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validProject";
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
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream);
        new CommandLine(pushCommand).parse("tests");
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains("ballerina: too many arguments"));
        Assert.assertTrue(actual.contains("ballerina push "));
    }

    @Test(description = "Push package without balo directory")
    public void testPushWithoutBaloDir() throws IOException {
        String expected = "cannot find balo file for the package: winery. Run "
                + "'ballerina build' to compile and generate the balo.";

        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream);
        new CommandLine(pushCommand).parse();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Push package without balo file")
    public void testPushWithoutBalo() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        // Build project
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().exists());

        // Delete balo
        Assert.assertTrue(
                projectPath.resolve("target").resolve("balo").resolve("foo-winery-any-0.1.0.balo").toFile().delete());

        // Push
        String expected = "cannot find balo file for the package: winery. Run "
                + "'ballerina build' to compile and generate the balo.";
        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream);
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
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream);
        new CommandLine(pushCommand).parse(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push packages to Ballerina Central"));
    }

    @Test(description = "Test push command with help flag")
    public void testPushCommandWithHelp() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        // Test if no arguments was passed in
        String[] args = { "-h" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream);
        new CommandLine(pushCommand).parse(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push packages to Ballerina Central"));
    }
}
