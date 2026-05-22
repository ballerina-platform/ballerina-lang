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
import io.ballerina.projects.BlendedBalToolsManifest;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.internal.SettingsBuilder;
import io.ballerina.projects.util.BalToolsUtil;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.maven.bala.client.model.ToolMavenMetadata;
import org.ballerinalang.maven.bala.client.model.ToolSearchEntry;
import org.ballerinalang.maven.bala.client.model.ToolSearchMavenMetadata;
import org.mockito.MockedConstruction;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Tool command tests.
 *
 * @since 2201.6.0
 */
public class ToolCommandTest extends BaseCommandTest {

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            Path testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Files.walkFileTree(Path.of(testResourcesURI),
                    new BuildCommandTest.Copy(Path.of(testResourcesURI), testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Pull a tool from local repository")
    public void testPullToolFromLocal() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("luhee:1.1.0", "--repository=local");
        toolPullCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-success.txt"));
        Assert.assertTrue(Files.exists(BalToolsUtil.BAL_TOOLS_TOML_PATH));
        BalToolsToml balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();

        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.1.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");
    }

    @Test(description = "Switch active version from local")
    public void testUseToolFromLocal() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("luhee:1.1.0", "--repository=local");
        toolPullCommand.execute();
        new CommandLine(toolPullCommand).parseArgs("luhee:1.2.0", "--repository=local");
        toolPullCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("tool 'luhee:1.2.0' successfully set as the active version.\n"), buildLog);
        Assert.assertTrue(Files.exists(BalToolsUtil.BAL_TOOLS_TOML_PATH));
        BalToolsToml balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();

        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.2.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");

        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("luhee:1.1.0", "--repository=local");
        toolUseCommand.execute();
        buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("tool 'luhee:1.1.0' successfully set as the active version.\n"));
        balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);

        tool = blendedBalToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.1.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");
    }

    @Test(description = "Remove a tool from local repository")
    public void testRemoveToolFromLocal() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("luhee:1.1.0", "--repository=local");
        toolPullCommand.execute();
        new CommandLine(toolPullCommand).parseArgs("luhee:1.2.0", "--repository=local");
        toolPullCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("tool 'luhee:1.2.0' successfully set as the active version.\n"), buildLog);
        Assert.assertTrue(Files.exists(BalToolsUtil.BAL_TOOLS_TOML_PATH));
        BalToolsToml balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();

        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.2.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");

        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("luhee:1.1.0", "--repository=local");
        toolUseCommand.execute();
        buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("tool 'luhee:1.1.0' successfully set as the active version.\n"));
        balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        tool = blendedBalToolsManifest.getActiveTool("luhee");
        Assert.assertTrue(tool.isPresent());
        Assert.assertEquals(tool.get().version(), "1.1.0");
        Assert.assertEquals(tool.get().repository(), "local");
        Assert.assertEquals(tool.get().org(), "gayaldassanayake");
        Assert.assertEquals(tool.get().name(), "tool_gayal");

        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        new CommandLine(toolRemoveCommand).parseArgs("luhee:1.2.0", "--repository=local");
        toolRemoveCommand.execute();
        buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("tool 'luhee:1.2.0' successfully removed.\n"));
        balToolsToml = BalToolsToml.from(BalToolsUtil.BAL_TOOLS_TOML_PATH);
        balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        tool = blendedBalToolsManifest.getTool("luhee", "1.2.0", "local");
        Assert.assertTrue(tool.isEmpty());
    }

    @Test(description = "Test tool command with the help flag")
    public void testToolCommandWithHelpFlag() throws IOException {
        String expected = getOutput("tool-help.txt");

        ToolCommand toolCommand = new ToolCommand(printStream);
        new CommandLine(toolCommand).parseArgs("--help");
        toolCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), expected);

        toolCommand = new ToolCommand(printStream);
        new CommandLine(toolCommand).parseArgs("-h");
        toolCommand.execute();
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), expected);

        toolCommand = new ToolCommand(printStream);
        new CommandLine(toolCommand).parseArgs();
        toolCommand.execute();
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), expected);
    }

    @Test(description = "Test tool pull sub-command with no arguments")
    public void testToolPullSubCommandWithNoArgs() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        toolPullCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-with-no-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with too many arguments")
    public void testToolPullSubCommandWithTooManyArgs() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("arg1", "arg2");
        toolPullCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-with-too-many-args.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidArgFormat() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("id:1.0.1:extra");
        toolPullCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool pull sub-command with invalid argument format")
    public void testToolPullSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs(toolId);
        toolPullCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolPullSubCommandWithInvalidToolVersion() throws IOException {
        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("tool_id:1.1");
        toolPullCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-pull-with-invalid-tool-version.txt"));
    }

    @Test(description = "Test tool remove with more than one argument")
    public void testToolRemoveSubCommandWithTooManyArgs() throws IOException {
        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        new CommandLine(toolRemoveCommand).parseArgs("arg1", "arg2");
        toolRemoveCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-remove-with-too-many-args.txt"));
    }

    @Test(description = "Test tool remove with more than no arguments")
    public void testToolRemoveSubCommandWithNoArgs() throws IOException {
        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        toolRemoveCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-remove-with-no-args.txt"));
    }

    @Test(description = "Test tool remove sub-command with invalid argument format")
    public void testToolRemoveSubCommandWithInvalidArgFormat() throws IOException {
        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        new CommandLine(toolRemoveCommand).parseArgs("id:1.0.1:extra");
        toolRemoveCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-remove-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool remove sub-command with invalid argument format")
    public void testToolRemoveSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        new CommandLine(toolRemoveCommand).parseArgs(toolId);
        toolRemoveCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-remove-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolRemoveSubCommandWithInvalidToolVersion() throws IOException {
        ToolRemoveCommand toolRemoveCommand = new ToolRemoveCommand(printStream, printStream, false);
        new CommandLine(toolRemoveCommand).parseArgs("tool_id:1.1");
        toolRemoveCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-remove-with-invalid-tool-version.txt"));
    }

    @Test(description = "Test tool search with more than one argument")
    public void testToolSearchSubCommandWithTooManyArgs() throws IOException {
        ToolSearchCommand toolSearchCommand = new ToolSearchCommand(printStream, printStream, false);
        new CommandLine(toolSearchCommand).parseArgs("arg1", "arg2");
        toolSearchCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-search-with-too-many-args.txt"));
    }

    @Test(description = "Test tool use with more than one argument")
    public void testToolUseSubCommandWithTooManyArgs() throws IOException {
        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("arg1", "arg2");
        toolUseCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("tool-use-with-too-many-args.txt"));
    }

    @Test(description = "Test tool use sub-command with invalid argument format")
    public void testToolUseSubCommandWithInvalidArgFormat() throws IOException {
        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("id:1.0.1:extra");
        toolUseCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool use sub-command with no version")
    public void testToolUseSubCommandWithNoVersion() throws IOException {
        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("id");
        toolUseCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(dataProvider = "invalidToolIds", description = "Test tool use sub-command with invalid argument format")
    public void testToolUseSubCommandWithInvalidToolId(String toolId) throws IOException {
        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs(toolId);
        toolUseCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-id.txt"));
    }

    @Test(description = "Test tool pull sub-command with invalid tool version")
    public void testToolUseSubCommandWithInvalidToolVersion() throws IOException {
        ToolUseCommand toolUseCommand = new ToolUseCommand(printStream, printStream, false);
        new CommandLine(toolUseCommand).parseArgs("tool_id:1.1");
        toolUseCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("tool-use-with-invalid-tool-version.txt"));
    }

    @DataProvider(name = "invalidToolIds")
    public Object[] invalidToolIds() {
        return new String[] { "_underscore", "underscore_", "under__score", "1initialnumeric", "..", "special$char"};
    }

    @Test (dependsOnMethods = {"testPullToolFromLocal"})
    public void testToolLocationCommandWithNoArgs() throws IOException {
        ToolLocationCommand toolLocationCommand = new ToolLocationCommand(printStream, printStream, false);
        new CommandLine(toolLocationCommand).parseArgs("luhee");
        toolLocationCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), Paths.get("build/user-home/" +
                ".ballerina/repositories/local/bala/gayaldassanayake/tool_gayal/1.1.0/java17").toAbsolutePath() + "\n");
    }

    @Test(description = "Test tool search via Maven proxy - tool found")
    public void testToolSearchFoundInMvnProxy() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        ToolSearchEntry entry = new ToolSearchEntry();
        entry.setOrg("myorg");
        entry.setName("toolpkg");
        entry.setBalToolId("my-tool");
        entry.setVersion("1.0.0");
        List<ToolSearchEntry> entries = new ArrayList<>();
        entries.add(entry);
        ToolSearchMavenMetadata metadata = new ToolSearchMavenMetadata();
        metadata.setTools(entries);

        ToolSearchCommand toolSearchCommand = new ToolSearchCommand(printStream, printStream, false);
        new CommandLine(toolSearchCommand).parseArgs("mytool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getToolSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenReturn(metadata))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            toolSearchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("myorg"), "Expected output to contain 'myorg'");
        Assert.assertTrue(output.contains("my-tool"), "Expected output to contain 'my-tool'");
        Assert.assertTrue(output.contains("1.0.0"), "Expected output to contain '1.0.0'");
    }

    @Test(description = "Test tool search via Maven proxy - no tools found")
    public void testToolSearchNoResultsInMvnProxy() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        ToolSearchMavenMetadata metadata = new ToolSearchMavenMetadata();
        metadata.setTools(new ArrayList<>());

        ToolSearchCommand toolSearchCommand = new ToolSearchCommand(printStream, printStream, false);
        new CommandLine(toolSearchCommand).parseArgs("unknowntool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getToolSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenReturn(metadata))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            toolSearchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("no tools found."), "Expected output to contain 'no tools found.'");
    }

    @Test(description = "Test tool search via Maven proxy - client throws exception")
    public void testToolSearchMvnProxyClientException() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        ToolSearchCommand toolSearchCommand = new ToolSearchCommand(printStream, printStream, false);
        new CommandLine(toolSearchCommand).parseArgs("mytool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getToolSearchMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                             .thenThrow(new MavenResolverClientException("tool search proxy error")))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            toolSearchCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("tool search proxy error"),
                "Expected output to contain 'tool search proxy error'");
    }

    @Test(description = "Test tool pull via Maven proxy - success case")
    public void testToolPullFromMvnProxySuccess() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        MavenResolverClient mockMavenClient = Mockito.mock(MavenResolverClient.class);
        ToolMavenMetadata metadata = new ToolMavenMetadata();
        metadata.setOrg("myorg");
        metadata.setName("tool_pkg");
        try {
            Mockito.when(mockMavenClient.getToolMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(metadata);
        } catch (MavenResolverClientException e) {
            Assert.fail("Unexpected exception during mock setup: " + e.getMessage());
        }

        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("my-tool:1.0.0");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<BalToolsUtil> balTools = Mockito.mockStatic(BalToolsUtil.class,
                     Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            balTools.when(() -> BalToolsUtil.hasProxyCentralRepository(Mockito.any())).thenReturn(true);
            balTools.when(() -> BalToolsUtil.initializeMavenClientWithProxyRepo(Mockito.any()))
                    .thenReturn(mockMavenClient);
            balTools.when(() -> BalToolsUtil.pullAndExtractToolFromMavenProxy(
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);
            balTools.when(() -> BalToolsUtil.isCompatibleWithPlatform(
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            toolPullCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("tool 'my-tool:1.0.0' pulled successfully."),
                "Expected output to contain 'tool 'my-tool:1.0.0' pulled successfully.'");
        Assert.assertTrue(output.contains("successfully set as the active version."),
                "Expected output to contain 'successfully set as the active version.'");
    }

    @Test(description = "Test tool pull via Maven proxy - client throws exception")
    public void testToolPullFromMvnProxyClientException() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        MavenResolverClient mockMavenClient = Mockito.mock(MavenResolverClient.class);
        try {
            Mockito.when(mockMavenClient.getToolMetadata(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenThrow(new MavenResolverClientException("tool metadata error"));
        } catch (MavenResolverClientException e) {
            Assert.fail("Unexpected exception during mock setup: " + e.getMessage());
        }

        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("my-tool:1.0.0");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<BalToolsUtil> balTools = Mockito.mockStatic(BalToolsUtil.class,
                     Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            balTools.when(() -> BalToolsUtil.hasProxyCentralRepository(Mockito.any())).thenReturn(true);
            balTools.when(() -> BalToolsUtil.initializeMavenClientWithProxyRepo(Mockito.any()))
                    .thenReturn(mockMavenClient);
            toolPullCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("unexpected error occurred while pulling tool:tool metadata error"),
                "Expected output to contain 'unexpected error occurred while pulling tool:tool metadata error'");
    }

    @Test(description = "Test tool update via Maven proxy - client throws exception")
    public void testToolUpdateMvnProxyClientException() throws IOException {
        Path settingsPath = Path.of("src/test/resources/test-resources/maven-proxy/Settings.toml");
        Settings proxySettings = readMockSettings(settingsPath);

        ToolUpdateCommand toolUpdateCommand = new ToolUpdateCommand(printStream, printStream, false);
        new CommandLine(toolUpdateCommand).parseArgs("central_test_tool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<BalToolsUtil> balToolsUtil = Mockito.mockStatic(BalToolsUtil.class,
                     Mockito.CALLS_REAL_METHODS);
             MockedConstruction<MavenResolverClient> ignored = Mockito.mockConstruction(MavenResolverClient.class,
                     (mock, ctx) -> Mockito.when(
                             mock.getCompatibleToolVersions(
                                     Mockito.anyString(), Mockito.anyString(), Mockito.any(Path.class)))
                             .thenThrow(new MavenResolverClientException("version fetch error")))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(proxySettings);
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            balToolsUtil.when(() -> BalToolsUtil.isCompatibleWithPlatform(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(true);
            toolUpdateCommand.execute();
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("unexpected error occurred while pulling tool:version fetch error"),
                "Expected output to contain 'unexpected error occurred while pulling tool:version fetch error'");
    }

    @Test(description = "Pull a specific version of a tool from Maven proxy central")
    public void testToolPullFromMvnProxyWithVersion() {
        Path repoPath = Path.of("src/test/resources/test-resources/maven-proxy/repositories/central-proxy");
        Path settingsTomlPath = Path.of("src/test/resources/test-resources/maven-proxy/SettingsFile.toml");
        Path mockBallerinaHome = tmpDir.resolve("ballerina-home-pull-with-version");

        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("my-tool:1.0.0");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<ProjectUtils> projUtils = Mockito.mockStatic(ProjectUtils.class,
                     Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            repoUtils.when(RepoUtils::readSettings).thenReturn(readMockSettings(settingsTomlPath,
                    repoPath.toAbsolutePath().toUri().toString()));
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            projUtils.when(ProjectUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            toolPullCommand.execute();
        }
        Path pulledCacheDir = mockBallerinaHome.resolve("repositories").resolve("central.ballerina.io")
                .resolve("bala").resolve("luheerathan").resolve("pact1").resolve("1.0.0");
        Assert.assertTrue(Files.exists(pulledCacheDir.resolve("any")));
    }

    @Test(description = "Pull latest compatible version of a tool from Maven proxy central")
    public void testToolPullFromMvnProxyWithoutVersion() {
        Path repoPath = Path.of("src/test/resources/test-resources/maven-proxy/repositories/central-proxy");
        Path settingsTomlPath = Path.of("src/test/resources/test-resources/maven-proxy/SettingsFile.toml");
        Path mockBallerinaHome = tmpDir.resolve("ballerina-home-pull-without-version");

        ToolPullCommand toolPullCommand = new ToolPullCommand(printStream, printStream, false);
        new CommandLine(toolPullCommand).parseArgs("my-tool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<ProjectUtils> projUtils = Mockito.mockStatic(ProjectUtils.class,
                     Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            repoUtils.when(RepoUtils::readSettings).thenReturn(readMockSettings(settingsTomlPath,
                    repoPath.toAbsolutePath().toUri().toString()));
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            projUtils.when(ProjectUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            toolPullCommand.execute();
        }
        Path pulledCacheDir = mockBallerinaHome.resolve("repositories").resolve("central.ballerina.io")
                .resolve("bala").resolve("luheerathan").resolve("pact1").resolve("1.0.0");
        Assert.assertTrue(Files.exists(pulledCacheDir.resolve("any")));
    }

    @Test(description = "Update a tool from older locked version via Maven proxy central")
    public void testToolUpdateFromMvnProxy() throws IOException {
        // Reset bal-tools.toml to initial state since prior pull tests may have written my-tool:1.0.0 into it
        Path balToolsSrc = Path.of("src/test/resources/test-resources/buildToolResources/tools/bal-tools.toml");
        Files.copy(balToolsSrc, BalToolsUtil.BAL_TOOLS_TOML_PATH, StandardCopyOption.REPLACE_EXISTING);

        Path repoPath = Path.of("src/test/resources/test-resources/maven-proxy/repositories/central-proxy");
        Path settingsTomlPath = Path.of("src/test/resources/test-resources/maven-proxy/SettingsFile.toml");
        Path mockBallerinaHome = tmpDir.resolve("ballerina-home-update");

        ToolUpdateCommand toolUpdateCommand = new ToolUpdateCommand(printStream, printStream, false);
        new CommandLine(toolUpdateCommand).parseArgs("my-tool");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<ProjectUtils> projUtils = Mockito.mockStatic(ProjectUtils.class,
                     Mockito.CALLS_REAL_METHODS);
             MockedStatic<BalToolsUtil> balToolsUtil = Mockito.mockStatic(BalToolsUtil.class,
                     Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            repoUtils.when(RepoUtils::readSettings).thenReturn(readMockSettings(settingsTomlPath,
                    repoPath.toAbsolutePath().toUri().toString()));
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("2201.13.0");
            repoUtils.when(RepoUtils::getBallerinaVersion).thenReturn("2201.13.0");
            projUtils.when(ProjectUtils::createAndGetHomeReposPath).thenReturn(mockBallerinaHome);
            balToolsUtil.when(() -> BalToolsUtil.pullToolPackageFromRemote(Mockito.any(), Mockito.any()))
                    .thenReturn(new BalToolsManifest.Tool("my-tool", "luheerathan", "pact1", "1.0.0", true, null));
            balToolsUtil.when(() -> BalToolsUtil.isCompatibleWithPlatform(
                    Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(true);
            toolUpdateCommand.execute();
        }
        Path updatedCacheDir = mockBallerinaHome.resolve("repositories").resolve("central.ballerina.io")
                .resolve("bala").resolve("luheerathan").resolve("pact1").resolve("1.0.0");
        Assert.assertTrue(Files.exists(updatedCacheDir.resolve("any")));
    }

    private static Settings readMockSettings(Path settingsFilePath, String repoPath) {
        try {
            String settingString = Files.readString(settingsFilePath);
            settingString = settingString.replace("REPO_PATH", repoPath);
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), settingString);
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read mock settings from: " + settingsFilePath, e);
        }
    }
}
