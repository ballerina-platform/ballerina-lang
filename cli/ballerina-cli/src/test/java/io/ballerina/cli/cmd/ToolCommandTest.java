/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
import java.util.Optional;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Tool command tests.
 *
 * @since 2201.6.0
 */
public class ToolCommandTest extends BaseCommandTest {

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

    @Test(description = "Pull a tool from local repository")
    public void testPullToolFromLocal() throws IOException {
        Path mockHomeRepo = testResources.resolve("local-tool-test").resolve("ballerina-cache");
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockHomeRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.9.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());

            ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("pull", "luhee:1.1.0", "--repository=local");
            toolCommand.execute();
        }

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "tool 'luhee:1.1.0' successfully set as the active version.\n");
        Assert.assertTrue(Files.exists(mockHomeRepo.resolve(".config").resolve("bal-tools.toml")));
        BalToolsToml balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config").resolve("bal-tools.toml"));
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Optional<BalToolsManifest.Tool> tool = balToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.1.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");
    }

    @Test(description = "Switch active version from local")
    public void testUseToolFromLocal() throws IOException {
        Path mockHomeRepo = testResources.resolve("local-tool-test").resolve("ballerina-cache");
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockHomeRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.9.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());

            ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("pull", "luhee:1.1.0", "--repository=local");
            toolCommand.execute();
            new CommandLine(toolCommand).parseArgs("pull", "luhee:1.2.0", "--repository=local");
            toolCommand.execute();

            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("tool 'luhee:1.2.0' successfully set as the active version.\n"));
            Assert.assertTrue(Files.exists(mockHomeRepo.resolve(".config").resolve("bal-tools.toml")));
            BalToolsToml balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config").resolve("bal-tools.toml"));
            BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            Optional<BalToolsManifest.Tool> tool = balToolsManifest.getActiveTool("luhee");
            Assert.assertTrue(tool.isPresent());
            Assert.assertEquals(tool.get().version(), "1.2.0");
            Assert.assertEquals(tool.get().repository(), "local");
            Assert.assertEquals(tool.get().org(), "gayaldassanayake");
            Assert.assertEquals(tool.get().name(), "tool_gayal");

            toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("use", "luhee:1.1.0", "--repository=local");
            toolCommand.execute();
            buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("tool 'luhee:1.1.0' successfully set as the active version.\n"));
            balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config").resolve("bal-tools.toml"));
            balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            tool = balToolsManifest.getActiveTool("luhee");
            Assert.assertTrue(tool.isPresent());
            Assert.assertEquals(tool.get().version(), "1.1.0");
            Assert.assertEquals(tool.get().repository(), "local");
            Assert.assertEquals(tool.get().org(), "gayaldassanayake");
            Assert.assertEquals(tool.get().name(), "tool_gayal");
        }
    }

    @Test(description = "Remove a tool from local repository")
    public void testRemoveToolFromLocal() throws IOException {
        Path mockHomeRepo = testResources.resolve("local-tool-test").resolve("ballerina-cache");
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockHomeRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.9.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());

            ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("pull", "luhee:1.1.0", "--repository=local");
            toolCommand.execute();
            new CommandLine(toolCommand).parseArgs("pull", "luhee:1.2.0", "--repository=local");
            toolCommand.execute();

            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("tool 'luhee:1.2.0' successfully set as the active version.\n"));
            Assert.assertTrue(Files.exists(mockHomeRepo.resolve(".config").resolve("bal-tools.toml")));
            BalToolsToml balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config")
                    .resolve("bal-tools.toml"));
            BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            Optional<BalToolsManifest.Tool> tool = balToolsManifest.getActiveTool("luhee");
            Assert.assertTrue(tool.isPresent());
            Assert.assertEquals(tool.get().version(), "1.2.0");
            Assert.assertEquals(tool.get().repository(), "local");
            Assert.assertEquals(tool.get().org(), "gayaldassanayake");
            Assert.assertEquals(tool.get().name(), "tool_gayal");

            toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("use", "luhee:1.1.0", "--repository=local");
            toolCommand.execute();
            buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("tool 'luhee:1.1.0' successfully set as the active version.\n"));
            balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config").resolve("bal-tools.toml"));
            balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            tool = balToolsManifest.getActiveTool("luhee");
            Assert.assertTrue(tool.isPresent());
            Assert.assertEquals(tool.get().version(), "1.1.0");
            Assert.assertEquals(tool.get().repository(), "local");
            Assert.assertEquals(tool.get().org(), "gayaldassanayake");
            Assert.assertEquals(tool.get().name(), "tool_gayal");

            toolCommand = new ToolCommand(printStream, printStream, false);
            new CommandLine(toolCommand).parseArgs("remove", "luhee:1.2.0", "--repository=local");
            toolCommand.execute();
            buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("tool 'luhee:1.2.0' successfully removed.\n"));
            balToolsToml = BalToolsToml.from(mockHomeRepo.resolve(".config").resolve("bal-tools.toml"));
            balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            tool = balToolsManifest.getTool("luhee", "1.2.0", "local");
            Assert.assertTrue(tool.isEmpty());
        }
    }

    @Test(description = "Test tool command with the help flag")
    public void testToolCommandWithHelpFlag() throws IOException {
        String expected = getOutput("tool-help.txt");

        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("--help");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expected);

        toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("-h");
        toolCommand.execute();
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), expected);
    }

    @Test(description = "Test tool command with no arguments")
    public void testToolCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs();
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-with-no-args.txt"));
    }

    @Test(description = "Test tool command with invalid sub command")
    public void testToolCommandWithInvalidSubCommand() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("invalid-cmd");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-with-invalid-sub-command.txt"));
    }

    @Test(description = "Test tool pull sub-command with no arguments")
    public void testToolPullSubCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("pull");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-no-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with too many arguments")
    public void testToolPullSubCommandWithTooManyArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "arg1", "arg2");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-too-many-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidArgFormat() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "id:1.0.1:extra");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", toolId);
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolPullSubCommandWithInvalidToolVersion() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("pull", "tool_id:1.1");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-pull-with-invalid-tool-version.txt"));
    }

    @Test(description = "Test tool list sub-command with arguments")
    public void testToolListSubCommandWithArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("list", "arg");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-list-with-args.txt"));

        toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("list", "arg1", "arg2");
        toolCommand.execute();
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-list-with-args.txt"));
    }

    @Test(description = "Test tool remove with more than one argument")
    public void testToolRemoveSubCommandWithTooManyArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("remove", "arg1", "arg2");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-remove-with-too-many-args.txt"));
    }

    @Test(description = "Test tool remove with more than no arguments")
    public void testToolRemoveSubCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("remove");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-remove-with-no-args.txt"));
    }

    @Test(description = "Test tool remove sub-command with invalid argument format")
    public void testToolRemoveSubCommandWithInvalidArgFormat() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("remove", "id:1.0.1:extra");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-remove-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool remove sub-command with invalid argument format")
    public void testToolRemoveSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("remove", toolId);
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-remove-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolRemoveSubCommandWithInvalidToolVersion() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("remove", "tool_id:1.1");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-remove-with-invalid-tool-version.txt"));
    }

    @Test(description = "Test tool search with more than one argument")
    public void testToolSearchSubCommandWithTooManyArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("search", "arg1", "arg2");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-search-with-too-many-args.txt"));
    }

    @Test(description = "Test tool search with more than no arguments")
    public void testToolSearchSubCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("search");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-search-with-no-args.txt"));
    }

    @Test(description = "Test tool use with more than one argument")
    public void testToolUseSubCommandWithTooManyArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use", "arg1", "arg2");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-too-many-args.txt"));
    }

    @Test(description = "Test tool use with more than no arguments")
    public void testToolUseSubCommandWithNoArgs() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-no-args.txt"));
    }

    @Test(description = "Test tool use sub-command with invalid argument format")
    public void testToolUseSubCommandWithInvalidArgFormat() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use", "id:1.0.1:extra");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool use sub-command with no version")
    public void testToolUseSubCommandWithNoVersion() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use", "id");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool use sub-command with invalid argument format")
    public void testToolUseSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use", toolId);
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolUseSubCommandWithInvalidToolVersion() throws IOException {
        ToolCommand toolCommand = new ToolCommand(printStream, printStream, false);
        new CommandLine(toolCommand).parseArgs("use", "tool_id:1.1");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-version.txt"));
    }

    @DataProvider(name = "invalidToolIds")
    public Object[] invalidToolIds() {
        return new String[] { "_underscore", "underscore_", "under__score", "1initialnumeric", "..", "special$char"};
    }
}
