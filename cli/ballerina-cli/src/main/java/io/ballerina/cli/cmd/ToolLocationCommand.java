/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.BlendedBalToolsManifest;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.ProjectConstants;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_LOCATION_COMMAND;
import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.getRepoPath;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Hidden command to get the filesystem location of tools.
 * This is for internal use only.
 *
 * @since 2201.13.0
 */
@CommandLine.Command(name = TOOL_LOCATION_COMMAND, description = "Get filesystem location of a tool", hidden = true)
public class ToolLocationCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;

    private static final String TOOL_LOCATION_USAGE_TEXT = "bal tool location <tool-id>[:<version>]";

    @CommandLine.Parameters(description = "Tool specification")
    private List<String> argList;

    @CommandLine.Option(names = "--repository", description = "Repository name")
    private String repository;

    @CommandLine.Option(names = "--jars", description = "Get the location of JAR files instead of tool base path")
    private boolean jarsFlag;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolLocationCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public ToolLocationCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LOCATION_COMMAND));
            return;
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_LOCATION_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        
        if (argList.size() > 1) {
            CommandUtil.printError(this.errStream, "too many arguments.", TOOL_LOCATION_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolSpec = argList.get(0);
        String toolId;
        String version = null;

        // Parse tool specification: either "tool-id" or "tool-id:version"
        if (toolSpec.contains(":")) {
            String[] parts = toolSpec.split(":", 2);
            toolId = parts[0];
            version = parts[1];
        } else {
            toolId = toolSpec;
        }

        if (toolId.trim().isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id cannot be empty.", TOOL_LOCATION_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        try {
            BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
            BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
            BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
            BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                    from(balToolsManifest, distBalToolsManifest);

            Optional<BalToolsManifest.Tool> toolOpt;

            if (version != null && repository != null) {
                // Get specific version and repository
                toolOpt = blendedBalToolsManifest.getTool(toolId, version, repository);
            } else if (version != null) {
                // Get specific version from any repository
                toolOpt = blendedBalToolsManifest.getTool(toolId, version, null);
            } else {
                // Get active tool
                toolOpt = blendedBalToolsManifest.getActiveTool(toolId);
            }

            if (toolOpt.isEmpty()) {
                String errorMsg = "tool '" + toolId;
                if (version != null) {
                    errorMsg += ":" + version;
                }
                errorMsg += "' not found";
                if (repository != null) {
                    errorMsg += " in repository '" + repository + "'";
                }
                CommandUtil.printError(this.errStream, errorMsg + ".", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            BalToolsManifest.Tool tool = toolOpt.get();
            Path toolPath = getToolPath(tool);

            if (jarsFlag) {
                toolPath = toolPath.resolve(ProjectConstants.TOOL_DIR).resolve(CommandUtil.LIBS_DIR);
            }

            if (Files.exists(toolPath)) {
                outStream.println(toolPath.toAbsolutePath());
            } else {
                CommandUtil.printError(this.errStream, "tool location does not exist: " + 
                    toolPath.toAbsolutePath(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }

        } catch (Exception e) {
            CommandUtil.printError(this.errStream, "failed to get tool location: " + e.getMessage(), 
                null, false);
            CommandUtil.exitError(this.exitWhenFinish);
        }
    }

    private Path getToolPath(BalToolsManifest.Tool tool) {
        Path repoPath = getRepoPath(tool.repository());
        return CommandUtil.getPlatformSpecificBalaPath(
                tool.org(), tool.name(), tool.version(), repoPath);
    }

    @Override
    public String getName() {
        return TOOL_LOCATION_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LOCATION_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_LOCATION_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
