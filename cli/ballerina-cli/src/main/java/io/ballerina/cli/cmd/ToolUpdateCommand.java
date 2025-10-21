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
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.BalToolsUtil;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_UPDATE_COMMAND;
import static io.ballerina.cli.utils.ToolUtils.getToolAvailableLocally;
import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.isCompatibleWithPlatform;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.HYPHEN;

/**
 * Command to update a tool to the latest version.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_UPDATE_COMMAND, description = "Search the Ballerina Central for tools.")
public class ToolUpdateCommand implements BLauncherCmd {
    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private String toolId;

    private static final String TOOL_UPDATE_USAGE_TEXT = "bal tool update <tool-id>";

    @CommandLine.Parameters(description = "Use a tool")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, hidden = true)
    private boolean offline;

    public ToolUpdateCommand() {
        this.exitWhenFinish = true;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_UPDATE_COMMAND));
            return;
        }
        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        toolId = argList.get(0);
        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        updateToolToLatestVersion();
    }

    @Override
    public String getName() {
        return TOOL_UPDATE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_UPDATE_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_UPDATE_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }

    private void updateToolToLatestVersion() {
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(DIST_BAL_TOOLS_TOML_PATH);
        BalToolsManifest distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
        BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest.
                from(balToolsManifest, distBalToolsManifest);
        Optional<BalToolsManifest.Tool> tool = blendedBalToolsManifest.getActiveTool(toolId);
        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool '" + toolId + "' is not installed.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (LOCAL_REPOSITORY_NAME.equals(tool.get().repository())) {
            CommandUtil.printError(errStream, "tools from local repository can not be updated. ",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
        }

        Optional<BalToolsManifest.Tool> currentActiveTool = balToolsManifest.getActiveTool(toolId);
        // remove force version from the tool entries
        balToolsManifest.compatibleTools().values().stream()
                .flatMap(map -> map.values().stream()).flatMap(map -> map.values().stream())
                .filter(t -> t.id().equals(toolId) && t.force())
                .forEach(t -> {
                    t.setForce(false);
                    t.setActive(false);
                });

        BalToolsManifest.Tool highestCompatibleToolVersion = blendedBalToolsManifest
                .getHighestCompatibleToolVersion(toolId);
        if (offline) {
            currentActiveTool.ifPresent(activeTool -> {
                if (SemanticVersion.from(activeTool.version())
                        .greaterThanOrEqualTo(SemanticVersion.from(highestCompatibleToolVersion.version()))) {
                    outStream.println("tool '" + toolId + "' is already up-to-date.");
                } else {
                    outStream.println("tool '" + toolId + ":" + highestCompatibleToolVersion.version() +
                            "' successfully set as the active version.");
                    balToolsManifest.tools().get(toolId).values().stream()
                            .flatMap(map -> map.values().stream())
                            .filter(t -> !t.version().equals(highestCompatibleToolVersion.version()))
                            .forEach(t -> {
                                t.setActive(false);
                                t.setForce(false);
                            });
                }
            });
            balToolsToml.modify(balToolsManifest);
            return;
        }

        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        try {
            String version = getLatestVersionForUpdateCommand(supportedPlatform, tool.get());
            if (SemanticVersion.from(tool.get().version()).greaterThanOrEqualTo(SemanticVersion.from(version))) {
                outStream.println("tool '" + toolId + "' is already up-to-date.");
                balToolsToml.modify(balToolsManifest);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            Optional<BalToolsManifest.Tool> toolAvailableLocally = getToolAvailableLocally(toolId, version, null);
            if (toolAvailableLocally.isPresent()) {
                outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
                addToBalToolsToml(balToolsToml, balToolsManifest, toolAvailableLocally.orElseThrow(), errStream);
                outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
                return;
            }

            BalToolsManifest.Tool toolFromCentral = BalToolsUtil.pullToolPackageFromRemote(toolId, version);
            addToBalToolsToml(balToolsToml, balToolsManifest, toolFromCentral, errStream);
            outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
        } catch (PackageAlreadyExistsException e) {
            errStream.println(e.getMessage());
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (CentralClientException | ProjectException e) {
            CommandUtil.printError(errStream, "unexpected error occurred while pulling tool:" + e.getMessage(),
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
        }
    }

    private String getLatestVersionForUpdateCommand(String supportedPlatforms, BalToolsManifest.Tool tool)
            throws CentralClientException {
        Settings settings;
        settings = RepoUtils.readSettings();
        // Ignore Settings.toml diagnostics in the pull command

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        List<String> versions = client.getPackageVersions(tool.org(), tool.name(), supportedPlatforms,
                RepoUtils.getBallerinaVersion());
        return getLatestVersion(versions, tool.version());
    }

    private String getLatestVersion(List<String> versions, String currentVersionStr) {
        Optional<String> latestVersionInSameMinor = versions.stream().map(SemanticVersion::from)
                .max((v1, v2) -> {
                    if (v1.greaterThan(v2)) {
                        return 1;
                    } else if (v2.greaterThan(v1)) {
                        return -1;
                    } else {
                        return 0;
                    }
                })
                .map(SemanticVersion::toString);
        return latestVersionInSameMinor.orElse(currentVersionStr);
    }

    private void addToBalToolsToml(BalToolsToml balToolsToml, BalToolsManifest balToolsManifest,
                                            BalToolsManifest.Tool tool, PrintStream printStream) {

        boolean isCompatibleWithPlatform = isCompatibleWithPlatform(tool.org(), tool.name(), tool.version(), tool.repository());
        if (!isCompatibleWithPlatform) {
            printStream.println("Tool '" + toolId + ":" + tool.version() + "' is not compatible with the current " +
                    "Ballerina distribution version. Run 'bal tool list' to see compatible versions.");
            return;
        }
        Optional<BalToolsManifest.Tool> currentActiveTool = balToolsManifest.getActiveTool(toolId);
        currentActiveTool.ifPresent(activeTool -> currentActiveTool.get().setForce(false));
        balToolsManifest.addTool(tool.id(), tool.org(), tool.name(), tool.version(), true, tool.repository());
        balToolsToml.modify(balToolsManifest);
    }
}
