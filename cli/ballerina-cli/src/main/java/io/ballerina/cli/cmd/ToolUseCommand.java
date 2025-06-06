/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.BlendedBalToolsManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_USE_COMMAND;
import static io.ballerina.cli.utils.ToolUtils.getToolFromLocalRepo;
import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.isCompatibleWithPlatform;
import static io.ballerina.projects.util.ProjectConstants.DISTRIBUTION_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to set a tool version as the active version.
 * @deprecated This command is deprecated and may be removed in a future version.
 * @since 2201.13.0
 */
@CommandLine.Command(name = TOOL_USE_COMMAND, description = "Set a tool version as the active version.")
public class ToolUseCommand implements BLauncherCmd {
    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private static final String TOOL_USE_USAGE_TEXT = "bal tool use <tool-id>:<version>";

    public ToolUseCommand() {
        this.exitWhenFinish = true;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    @CommandLine.Parameters(description = "Use a tool")
    private List<String> argList;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolUseCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        outStream.println("WARNING: This command is deprecated and may be removed in a future version.");

        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_USE_COMMAND));
            return;
        }
        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (repositoryName != null && !repositoryName.equals(LOCAL_REPOSITORY_NAME)) {
            String errMsg = "unsupported repository '" + repositoryName + "' found. Only '"
                    + LOCAL_REPOSITORY_NAME + "' repository is supported.";
            CommandUtil.printError(this.errStream, errMsg, null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolIdAndVersion = argList.get(0);
        String[] toolInfo = toolIdAndVersion.split(":");
        String toolId;
        String version;
        if (toolInfo.length == 2) {
            toolId = toolInfo[0];
            version = toolInfo[1];
        }  else {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        try {
            SemanticVersion.from(version);
        } catch (ProjectException e) {
            CommandUtil.printError(errStream, "invalid tool version. " + e.getMessage(), TOOL_USE_USAGE_TEXT,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();

        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getTool(toolId, version, repositoryName);

        if (tool.isEmpty()) {
            Optional<BalToolsManifest.Tool> toolFromLocalRepo = getToolFromLocalRepo(toolId, version);
            if (toolFromLocalRepo.isEmpty()) {
                CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not found. " +
                        "Run 'bal tool pull " + toolId + ":" + version
                        + "' or 'bal tool pull " + toolId + ":" + version
                        + " --repository=local' to fetch and set as the active version.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not found. Run " +
                            "'bal tool pull " + toolId + ":" + version + "' to fetch and set as the active version.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        String org = tool.get().org();
        String name = tool.get().name();
        Optional<BalToolsManifest.Tool> currentActiveTool = blendedBalToolsManifest.getActiveTool(toolId);
        if (currentActiveTool.isPresent() && currentActiveTool.get().version().equals(tool.get().version()) &&
                Objects.equals(currentActiveTool.get().repository(), tool.get().repository())) {
            outStream.println("tool '" + toolId + ":" + version + "' is the current active version.");
            return;
        }

        boolean isCompatibleWithPlatform = isCompatibleWithPlatform(
                org, name, version, tool.get().repository());
        if (!isCompatibleWithPlatform) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        balToolsManifest.resetCurrentActiveVersion(toolId);
        if (!DISTRIBUTION_REPOSITORY_NAME.equals(tool.get().repository())) {
            balToolsManifest.setActiveToolVersion(toolId, version, tool.get().repository());
        }
        balToolsToml.modify(balToolsManifest);
        outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
    }

    @Override
    public String getName() {
        return TOOL_USE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_USE_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_USE_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
