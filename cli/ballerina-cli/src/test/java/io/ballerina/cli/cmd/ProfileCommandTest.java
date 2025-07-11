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

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;

/**
 * Profile command tests.
 *
 * @since 2201.8.0
 */
public class ProfileCommandTest extends BaseCommandTest {
    private Path testResources;

    private static final Path logFile = Path.of("build/logs/log_creator_combined_plugin/compiler-plugin.txt")
            .toAbsolutePath();

    @BeforeSuite
    public void setupSuite() throws IOException {
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
    }

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Path resourceURI = Path.of(testResourcesURI);
            Files.walkFileTree(resourceURI, new BuildCommandTest.Copy(resourceURI,
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }


    @Test(description = "Profile a ballerina project")
    public void testRunBalProjectWithProfileFlag() throws IOException {
        Path projectPath = this.testResources.resolve("projectForProfile/package_a");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        profileCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").replaceAll("\\\\", "/").strip();
        Assert.assertEquals(buildLog, getOutput("run-project-with-profile.txt"));
        Path htmlPath = projectPath.resolve("target/profiler/ProfilerReport.html");
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
        Path projectPath = this.testResources.resolve("projectForProfile/package_b");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        profileCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").replaceAll("\\\\", "/").strip();
        Assert.assertEquals(buildLog, getOutput("profile-project-with-build-tool.txt"));
        Path htmlPath = projectPath.resolve("target/profiler/ProfilerReport.html");
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
        Path projectPath = this.testResources.resolve("projectForProfile/package_a");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        new CommandLine(profileCommand).parseArgs(args);
        profileCommand.execute();
        Assert.assertTrue(readOutput().contains("ballerina-profile - Run Ballerina Profiler on the source and " +
                "generate flame graph"));
    }

    @Test(description = "Profile an empty package")
    public void testProfileEmptyProject() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        new CommandLine(profileCommand).parseArgs();
        try {
            profileCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-package.txt"));
        }
    }

    @Test(description = "Profile an empty package with code generator build tools")
    public void testProfileEmptyProjectWithBuildTools() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithBuildTool");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());

        ProfileCommand profileCommand = new ProfileCommand(projectPath, printStream, false);
        new CommandLine(profileCommand).parseArgs();
        profileCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", "").replace("\\", "/"),
                getOutput("profile-empty-project-with-build-tools.txt"));
    }

    @AfterSuite
    public void cleanUp() throws IOException {
        Files.deleteIfExists(logFile);
        Files.deleteIfExists(logFile.getParent());
        Files.deleteIfExists(logFile.getParent().getParent());
    }
}
