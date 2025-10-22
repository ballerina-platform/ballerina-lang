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
import io.ballerina.cli.utils.PrintUtils;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.BlendedBalToolsManifest;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_LIST_COMMAND;
import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to list all tools available in the local environment.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_LIST_COMMAND, description = "List all tools available in the local environment.")
public class ToolListCommand implements BLauncherCmd {
    private final PrintStream outStream;

    @CommandLine.Option(names = {"-a", "--all"}, description = "List all available tools")
    boolean all;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolListCommand() {
        this.outStream = System.out;
        this.all = false;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LIST_COMMAND));
            return;
        }
        List<BalToolsManifest.Tool> tools = listBalToolsTomlFile(all);
        if (tools.isEmpty()) {
            outStream.println("no tools found locally.");
            return;
        }
        PrintUtils.printLocalTools(tools, RepoUtils.getTerminalWidth(), all);
    }

    private List<BalToolsManifest.Tool> listBalToolsTomlFile(boolean all) {
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);

        List<BalToolsManifest.Tool> flattenedTools = new ArrayList<>();
        if (all) {
            blendedBalToolsManifest.tools().values().stream()
                    .flatMap(map -> map.values().stream()).flatMap(map -> map.values().stream())
                    .sorted(Comparator.comparing(BalToolsManifest.Tool::id)
                            .thenComparing(BalToolsManifest.Tool::version).reversed())
                    .forEach(tool -> {
                        // Check if this tool is the active version
                        Optional<BalToolsManifest.Tool> activeTool = blendedBalToolsManifest.getActiveTool(tool.id());
                        tool.setActive(activeTool.isPresent() && activeTool.get().version().equals(tool.version()));
                        flattenedTools.add(tool);
                    });
        } else {
            for (Map.Entry<String, Map<String, Map<String, BalToolsManifest.Tool>>> toolEntry :
                    blendedBalToolsManifest.compatibleTools().entrySet()) {
                Optional<BalToolsManifest.Tool> activeTool = blendedBalToolsManifest.getActiveTool(toolEntry.getKey());
                if (activeTool.isEmpty()) {
                    continue;
                }
                flattenedTools.add(activeTool.get());
            }
        }
        return flattenedTools;
    }

    @Override
    public String getName() {
        return TOOL_LIST_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LIST_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("bal tool list [--all]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
