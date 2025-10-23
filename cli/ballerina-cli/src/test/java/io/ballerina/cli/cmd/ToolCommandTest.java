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
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.BalToolsUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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
}
