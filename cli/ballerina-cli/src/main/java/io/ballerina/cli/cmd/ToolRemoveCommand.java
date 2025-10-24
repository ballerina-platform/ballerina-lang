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
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_REMOVE_COMMAND;
import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.getRepoPath;
import static io.ballerina.projects.util.BalToolsUtil.isCompatibleWithPlatform;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.DISTRIBUTION_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to remove a tool from the local environment.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_REMOVE_COMMAND, description = "Search the Ballerina Central for tools.")
public class ToolRemoveCommand implements BLauncherCmd {
    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private String toolId;
    private String version;

    private static final String TOOL_REMOVE_USAGE_TEXT = "bal tool remove <tool-id>:[<version>]";

    public ToolRemoveCommand() {
        this.exitWhenFinish = true;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    public ToolRemoveCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @CommandLine.Parameters(description = "Use a tool")
    private List<String> argList;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_REMOVE_COMMAND));
            return;
        }
        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_REMOVE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_REMOVE_USAGE_TEXT, false);
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
        if (toolInfo.length == 2) {
            toolId = toolInfo[0];
            version = toolInfo[1];
        } else if (toolInfo.length == 1) {
            toolId = toolIdAndVersion;
            version = Names.EMPTY.getValue();
        } else {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_REMOVE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_REMOVE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!Names.EMPTY.getValue().equals(version)) {
            try {
                SemanticVersion.from(version);
            } catch (ProjectException e) {
                CommandUtil.printError(errStream, "invalid tool version. " + e.getMessage(),
                        TOOL_REMOVE_USAGE_TEXT, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        if (Names.EMPTY.getValue().equals(version)) {
            removeAllToolVersions();
        } else {
            removeSpecificToolVersion();
        }
    }

    @Override
    public String getName() {
        return TOOL_REMOVE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_REMOVE_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_REMOVE_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void removeAllToolVersions() {
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        Optional<Map<String, Map<String, BalToolsManifest.Tool>>> toolVersions =
                Optional.ofNullable(balToolsManifest.tools().get(toolId));
        if (toolVersions.isEmpty() || toolVersions.get().isEmpty()) {
            BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
            BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
            toolVersions = Optional.ofNullable(distBalToolsManifest.tools().get(toolId));
            if (toolVersions.isPresent() && !toolVersions.get().isEmpty()) {
                CommandUtil.printError(errStream, "tools cannot be removed from the distribution repository.",
                        null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            CommandUtil.printError(errStream, "tool '" + toolId + "' not found.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        balToolsManifest.removeTool(toolId);
        balToolsToml.modify(balToolsManifest);
        if (repositoryName != null) {
            outStream.println("tool '" + toolId + "' successfully removed.");
            return;
        }
        Optional<BalToolsManifest.Tool> tool = toolVersions.get().values().stream().findAny()
                .flatMap(value -> value.values().stream().findAny());
        tool.ifPresent(value -> deleteAllCachedToolVersions(value.org(), value.name()));
        outStream.println("tool '" + toolId + "' successfully removed.");
    }

    private void removeSpecificToolVersion() {
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();

        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getTool(toolId, version, repositoryName);

        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version
                    + "' not found in the local cache.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (DISTRIBUTION_REPOSITORY_NAME.equals(tool.get().repository())) {
            CommandUtil.printError(errStream, "tools from the distribution repository cannot be removed.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        String org = tool.get().org();
        String name = tool.get().name();

        boolean isCompatibleWithPlatform;
        Path toolBalaPath = CommandUtil.getPlatformSpecificBalaPath(
                org, name, tool.get().version(), getRepoPath(tool.get().repository()));
        if (Files.exists(toolBalaPath)) {
            isCompatibleWithPlatform = isCompatibleWithPlatform(org, name, version, repositoryName);
            if (!isCompatibleWithPlatform) {
                CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not compatible with the " +
                                "current Ballerina distribution '" + RepoUtils.getBallerinaShortVersion() +
                                "'. Use 'bal tool search' to select a version compatible with the current " +
                                "Ballerina distribution.",
                        null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            deleteCachedToolVersion(tool.get().org(), tool.get().name(), version);
        }

        balToolsManifest.removeToolVersion(toolId, version, repositoryName);
        balToolsToml.modify(balToolsManifest);
        outStream.println("tool '" + toolId + ":" + version + "' successfully removed.");
    }

    private void deleteAllCachedToolVersions(String org, String name) {
        Path toolPath = RepoUtils.createAndGetHomeReposPath().resolve(Path.of(REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name));
        if (!Files.isDirectory(toolPath)) {
            return;
        }
        ProjectUtils.deleteDirectory(toolPath);
    }

    private void deleteCachedToolVersion(String org, String name, String version) {
        Path toolPath = RepoUtils.createAndGetHomeReposPath().resolve(Path.of(REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name, version));
        if (!Files.isDirectory(toolPath)) {
            return;
        }
        ProjectUtils.deleteDirectory(toolPath);
    }
}
