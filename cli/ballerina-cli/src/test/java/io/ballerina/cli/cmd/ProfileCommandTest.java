/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
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
 * Profile command tests.
 *
 * @since 2201.8.0
 */
public class ProfileCommandTest extends BaseCommandTest {
    private Path testResources;

    static Path logFile = Paths.get(".", "src", "test", "resources", "compiler_plugin_tests",
            "log_creator_combined_plugin", "compiler-plugin.txt");

    @BeforeSuite
    public void setupSuite() throws IOException {
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
    }

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Path resourceURI = Paths.get(testResourcesURI);
            Files.walkFileTree(resourceURI, new BuildCommandTest.Copy(resourceURI,
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }


    @Test(description = "Profile a ballerina project")
    public void testRunBalProjectWithProfileFlag() throws IOException {
        Path projectPath = this.testResources.resolve("projectForProfile").resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        profileCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();
        Assert.assertEquals(buildLog, getOutput("run-project-with-profile.txt"));
        Path htmlPath = projectPath.resolve("target").resolve("profiler").resolve("ProfilerReport.html");
        Assert.assertTrue(htmlPath.toFile().exists());
        try {
            String htmlContent = Files.readString(htmlPath);
            Assert.assertTrue(htmlContent.contains("foo/package_a/0/main.main"));
            Assert.assertTrue(htmlContent.contains("foo/package_a/0/$_init.$moduleInit"));
        } catch (IOException e) {
            Assert.fail("Error reading html file");
        }
        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Profile a ballerina project with build tools")
    public void testRunBalProjectWithProfileFlagWithBuildTools() throws IOException {
        Path projectPath = this.testResources.resolve("projectForProfile").resolve("package_b");
        System.setProperty("user.dir", projectPath.toString());

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        profileCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();
        Assert.assertEquals(buildLog, getOutput("profile-project-with-build-tool.txt"));
        Path htmlPath = projectPath.resolve("target").resolve("profiler").resolve("ProfilerReport.html");
        Assert.assertTrue(htmlPath.toFile().exists());
        try {
            String htmlContent = Files.readString(htmlPath);
            Assert.assertTrue(htmlContent.contains("foo/package_b/0/main.main"));
            Assert.assertTrue(htmlContent.contains("foo/package_b/0/$_init.$moduleInit"));
        } catch (IOException e) {
            Assert.fail("Error reading html file");
        }
        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Test profile command with help")
    public void testProfileCommandAndHelp() throws IOException {
        String[] args = {"--help"};
        Path projectPath = this.testResources.resolve("projectForProfile").resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());
        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        new CommandLine(profileCommand).parseArgs(args);
        profileCommand.execute();
        Assert.assertTrue(readOutput().contains("ballerina-profile - Run Ballerina Profiler on the source and " +
                "generate flame graph"));
    }

    @AfterSuite
    public void cleanUp() throws IOException {
        Files.deleteIfExists(logFile);
        Files.deleteIfExists(logFile.getParent());
        Files.deleteIfExists(logFile.getParent().getParent());
    }
}
