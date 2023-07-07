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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.util.BalToolUtil;
import io.ballerina.cli.utils.PrintUtils;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.DistSpecificToolsManifest;
import io.ballerina.projects.GlobalToolsManifest;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.DistSpecificToolsManifestBuilder;
import io.ballerina.projects.internal.GlobalToolsManifestBuilder;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.central.client.model.Tool;
import org.ballerinalang.central.client.model.ToolSearchResult;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.Constants.DIST_TOOL_TOML_PREFIX;
import static io.ballerina.cli.cmd.Constants.TOML_EXT;
import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printTools;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_DEFAULT_DIRNAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the "bal tool" command.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_COMMAND, description = "Manage ballerina tool commands")
public class ToolCommand implements BLauncherCmd {
    private static final String TOOL_PULL_COMMAND = "pull";
    private static final String TOOL_USE_COMMAND = "use";
    private static final String TOOL_LIST_COMMAND = "list";
    private static final String TOOL_SEARCH_COMMAND = "search";
    private static final String TOOL_REMOVE_COMMAND = "remove";
    private static final String HYPHEN = "-";

    private static final String TOOL_USAGE_TEXT = "bal tool <sub-command> [args]";
    private static final String TOOL_PULL_USAGE_TEXT = "bal tool pull <tool-id>[:<version>]";
    private static final String TOOL_USE_USAGE_TEXT = "bal tool use <tool-id>:<version>";
    private static final String TOOL_LIST_USAGE_TEXT = "bal tool list";
    private static final String TOOL_REMOVE_USAGE_TEXT = "bal tool remove <tool-id>:[<version>]";
    private static final String TOOL_SEARCH_USAGE_TEXT = "bal tool search [<tool-id>|<org>|<package>|<text>]";

    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    private final String distSpecificToolsTomlName = DIST_TOOL_TOML_PREFIX + RepoUtils.getBallerinaShortVersion()
            + TOML_EXT;
    Path distSpecificToolsTomlPath = Path.of(
            System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR,
            distSpecificToolsTomlName);
    Path globalToolsTomlPath = Path.of(
            System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR, BAL_TOOLS_TOML);

    @CommandLine.Parameters(description = "Manage ballerina tools")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    private String toolId;
    private String org;
    private String name;
    private String version;

    public ToolCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public ToolCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public String getName() {
        return TOOL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo;
            if (argList == null || argList.isEmpty()) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND);
            } else if (argList.get(0).equals(TOOL_PULL_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_PULL_COMMAND);
            } else if (argList.get(0).equals(TOOL_USE_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_USE_COMMAND);
            } else if (argList.get(0).equals(TOOL_LIST_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LIST_COMMAND);
            } else if (argList.get(0).equals(TOOL_REMOVE_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_REMOVE_COMMAND);
            } else if (argList.get(0).equals(TOOL_SEARCH_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_SEARCH_COMMAND);
            } else {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND);
            }
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no sub-command given.", TOOL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String command = argList.get(0);
        switch (command) {
            case TOOL_PULL_COMMAND:
                handlePullCommand();
                break;
            case TOOL_USE_COMMAND:
                handleUseCommand();
                break;
            case TOOL_LIST_COMMAND:
                handleListCommand();
                break;
            case TOOL_SEARCH_COMMAND:
                handleSearchCommand();
                break;
            case TOOL_REMOVE_COMMAND:
                handleRemoveCommand();
                break;
            default:
                CommandUtil.printError(this.errStream, "invalid sub-command given.", TOOL_USAGE_TEXT, false);
                CommandUtil.exitError(this.exitWhenFinish);
                break;
        }
    }

    private void handlePullCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "no tool id given.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 2) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolIdAndVersion = argList.get(1);
        String[] toolInfo = toolIdAndVersion.split(":");
        if (toolInfo.length == 2) {
            toolId = toolInfo[0];
            version = toolInfo[1];
        } else if (toolInfo.length == 1) {
            toolId = toolIdAndVersion;
            version = Names.EMPTY.getValue();
        } else {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_PULL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!Names.EMPTY.getValue().equals(version)) {
            try {
                SemanticVersion.from(version);
            } catch (ProjectException e) {
                CommandUtil.printError(errStream, "invalid tool version. " + e.getMessage(), TOOL_PULL_USAGE_TEXT,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        Path balaCacheDirPath = ProjectUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            try {
                if (isToolLocallyAvailable(toolId, version)) {
                    outStream.println("tool " + toolId + ":" + version + " is already available locally.");
                } else {
                    pullToolFromCentral(supportedPlatform, balaCacheDirPath);
                }
                insertToBalToolsTomlFile();
            } catch (PackageAlreadyExistsException e) {
                errStream.println(e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            } catch (CentralClientException | ProjectException e) {
                errStream.println("unexpected error occurred while pulling tool:" + e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }

    private void handleUseCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "no tool id given.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 2) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_USE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolIdAndVersion = argList.get(1);
        String[] toolInfo = toolIdAndVersion.split(":");
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

        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                .from(distSpecificToolsToml).build();
        Optional<DistSpecificToolsManifest.Tool> tool = distSpecificToolsManifest.getTool(toolId, version);
        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not found. " +
                    "Run 'bal tool pull " + toolId + ":" + version + "' to fetch and set as the active version.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        Optional<DistSpecificToolsManifest.Tool> currentActiveTool = distSpecificToolsManifest.getActiveTool(toolId);
        if (currentActiveTool.isPresent() && currentActiveTool.get().version().equals(tool.get().version())) {
            outStream.println("tool '" + toolId + ":" + version + "' is the current active version.");
            return;
        }

        distSpecificToolsManifest.setActiveToolVersion(toolId, version);
        distSpecificToolsToml.modify(distSpecificToolsManifest);

        outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
    }

    private void handleListCommand() {
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_LIST_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        List<DistSpecificToolsManifest.Tool> tools = listBalToolsTomlFile();
        if (tools.isEmpty()) {
            outStream.println("no tools found locally.");
            return;
        }
        PrintUtils.printLocalTools(tools, RepoUtils.getTerminalWidth());
    }

    private void handleSearchCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "no keyword given.", TOOL_SEARCH_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 2) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_SEARCH_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String searchArgs = argList.get(1);
        searchToolsInCentral(searchArgs);
    }

    private void handleRemoveCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "no tool id given.", TOOL_REMOVE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 2) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_REMOVE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String toolIdAndVersion = argList.get(1);
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

    private void pullToolFromCentral(String supportedPlatform, Path balaCacheDirPath) throws CentralClientException {
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings));
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        boolean isPulled = Boolean.parseBoolean(toolInfo[0]);
        org = toolInfo[1];
        name = toolInfo[2];
        version = toolInfo[3];

        if (isPulled) {
            outStream.println("tool '" + toolId + ":" + version + "' pulled successfully.");
        } else {
            outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
        }
    }

    private void insertToBalToolsTomlFile() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                .from(distSpecificToolsToml).build();

        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = getToolDistVersionFromCentralCache();
        if (!isCompatibleWithLocalDistVersion(currentDistVersion, toolDistVersion)) {
            errStream.println("tool is built with distribution version " + toolDistVersion + " which is greater than " +
                    "the current dist version " + currentDistVersion + ". please update the distribution to update "
                    + toolDistVersion.minor() + " or greater and and try again.");
        } else if (isToolVersionAlreadyActive(toolId, version)) {
            outStream.println("tool '" + toolId + ":" + version + "' is already active.");
        } else {
            distSpecificToolsManifest.addTool(toolId, org, name, version, true);
            distSpecificToolsToml.modify(distSpecificToolsManifest);
            outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
        }

        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        GlobalToolsManifest globalToolsManifest = GlobalToolsManifestBuilder.from(globalToolsToml).build();
        globalToolsManifest.addTool(toolId, org, name);
        globalToolsToml.modify(globalToolsManifest);

        Path configDir = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR);
        if (configDir.toFile().isDirectory()) {
            try (Stream<Path> distFiles = Files.list(configDir)) {
                distFiles.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (fileName.equals(distSpecificToolsTomlName)) {
                        return;
                    }
                    if (isValidDistSpecificTomlFile(fileName)) {
                        // if the tool distribution version is greater than dist-toml distribution version do not add
                        SemanticVersion distTomlDistVersion = getSemVerFromDistSpecificTomlFile(fileName);
                        if (!isCompatibleWithLocalDistVersion(distTomlDistVersion, toolDistVersion)) {
                            return;
                        }
                        BalToolsToml distToolsToml = BalToolsToml.from(path);
                        DistSpecificToolsManifest inactiveDistSpecificToolsManifest = DistSpecificToolsManifestBuilder
                                .from(distToolsToml).build();

                        // the tool should be active in other distributions if,
                        // 1. there are no other versions of the tool available OR,
                        // 2. dist-<version>.toml refers to a patch with the same minor version as the current version
                        Map<String, DistSpecificToolsManifest.Tool> toolVersions =
                                inactiveDistSpecificToolsManifest.tools().get(toolId);
                        boolean isActive = (null == toolVersions) || toolVersions.isEmpty()
                                || distTomlDistVersion.minor() == currentDistVersion.minor();
                        inactiveDistSpecificToolsManifest.addTool(toolId, org, name, version, isActive);
                        distToolsToml.modify(inactiveDistSpecificToolsManifest);
                    }
                });
            } catch (IOException e) {
                throw new ProjectException("error while reading the .config directory: " + configDir, e);
            }
        }
    }

    private List<DistSpecificToolsManifest.Tool> listBalToolsTomlFile() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                .from(distSpecificToolsToml).build();
        List<DistSpecificToolsManifest.Tool> flattenedTools = new ArrayList<>();
        distSpecificToolsManifest.tools().values().stream()
                .flatMap(map -> map.values().stream())
                .forEach(flattenedTools::add);
        return flattenedTools;
    }

    private void removeAllToolVersions() {
        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        GlobalToolsManifest globalToolsManifest = GlobalToolsManifestBuilder.from(globalToolsToml).build();

        Optional<GlobalToolsManifest.Tool> tool = Optional.ofNullable(globalToolsManifest.tools().get(toolId));
        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool " + toolId + " not found.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // 1. delete in each dist specific toml
        Path configDir = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR);
        if (configDir.toFile().isDirectory()) {
            try (Stream<Path> distFiles = Files.list(configDir)) {
                distFiles.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (isValidDistSpecificTomlFile(fileName)) {
                        BalToolsToml distToolsToml = BalToolsToml.from(path);
                        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                                .from(distToolsToml).build();
                        distSpecificToolsManifest.removeTool(toolId);
                        distToolsToml.modify(distSpecificToolsManifest);
                    }
                });
            } catch (IOException e) {
                throw new ProjectException("error while reading the .config directory: " + configDir, e);
            }
        }

        // 2. delete in global toml
        globalToolsManifest.removeTool(toolId);
        globalToolsToml.modify(globalToolsManifest);

        // 3. delete in central cache
        deleteAllToolVersionsCache(tool.get().org(), tool.get().name());
        outStream.println("tool '" + toolId + "' successfully removed.");
    }

    private void removeSpecificToolVersion() {
        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        GlobalToolsManifest globalToolsManifest = GlobalToolsManifestBuilder.from(globalToolsToml).build();

        // check in global toml and if absent throw error as above
        Optional<GlobalToolsManifest.Tool> tool = Optional.ofNullable(globalToolsManifest.tools().get(toolId));
        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool " + toolId + " not found.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // 1. loop each dist toml and remove the tool version if active set the latest from others as active
        AtomicBoolean toolVersionFound = new AtomicBoolean(false);
        Path configDir = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR);
        if (configDir.toFile().isDirectory()) {
            try (Stream<Path> distFiles = Files.list(configDir)) {
                distFiles.forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (isValidDistSpecificTomlFile(fileName)) {
                        BalToolsToml distToolsToml = BalToolsToml.from(path);
                        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                                .from(distToolsToml).build();
                        Optional<DistSpecificToolsManifest.Tool> toolVersion =
                                distSpecificToolsManifest.getTool(toolId, version);
                        if (toolVersion.isPresent()) {
                            distSpecificToolsManifest.removeToolVersion(toolId, version);
                            toolVersionFound.set(true);
                            if (toolVersion.get().active()) {
                                BalToolUtil.setLatestVersionToActive(distSpecificToolsManifest);
                                // print active dist change if current distribution
                                if (fileName.equals(distSpecificToolsTomlName)) {
                                    distSpecificToolsManifest.tools().get(toolId).values().stream()
                                            .filter(DistSpecificToolsManifest.Tool::active)
                                            .findFirst()
                                            .ifPresent(toolVersion1 ->
                                                    outStream.println("tool '" + toolId + ":" + toolVersion1.version() +
                                                    "' successfully set as the active version."));
                                }
                            }
                            distToolsToml.modify(distSpecificToolsManifest);
                        }
                    }
                });
            } catch (IOException e) {
                throw new ProjectException("error while reading the .config directory: " + configDir, e);
            }
        }
        if (!toolVersionFound.get()) {
            CommandUtil.printError(errStream, "tool " + toolId + ":" + version + " not found.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // 2. delete in central cache
        deleteSpecificToolVersionCache(tool.get().org(), tool.get().name(), version);
        outStream.println("tool '" + toolId + ":" + version + "' successfully removed.");
    }

    private void deleteAllToolVersionsCache(String org, String name) {
        Path toolPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name);
        if (!Files.isDirectory(toolPath)) {
            return;
        }
        ProjectUtils.deleteDirectory(toolPath);
    }

    private boolean deleteSpecificToolVersionCache(String org, String name, String version) {
        Path toolPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name, version);
        if (!Files.exists(toolPath) || !Files.isDirectory(toolPath)) {
            return false;
        }
        return ProjectUtils.deleteDirectory(toolPath);
    }

    /**
     * Search for tools in central.
     *
     * @param keyword search keyword.
     */
    private void searchToolsInCentral(String keyword) {
        try {
            Settings settings;
            try {
                settings = RepoUtils.readSettings();
                // Ignore Settings.toml diagnostics in the search command
            } catch (SettingsTomlException e) {
                // Ignore 'Settings.toml' parsing errors and return empty Settings object
                settings = Settings.from();
            }
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()), settings.getProxy().username(),
                    settings.getProxy().password(), getAccessTokenOfCLI(settings));
            ToolSearchResult toolSearchResult = client.searchTool(keyword,
                    JvmTarget.JAVA_11.code(),
                    RepoUtils.getBallerinaVersion());

            List<Tool> tools = toolSearchResult.getTools();
            if (tools != null && tools.size() > 0) {
                printTools(toolSearchResult.getTools(), RepoUtils.getTerminalWidth());
            } else {
                outStream.println("no tools found.");
            }
        } catch (CentralClientException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !"".equals(errorMessage.trim())) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }
                CommandUtil.printError(this.errStream, errorMessage, null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            } else {
                CommandUtil.printError(this.errStream, "error while searching for tools.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }

    private boolean isToolLocallyAvailable(String toolId, String version) {
        if (version.equals(Names.EMPTY.getValue())) {
            return false;
        }
        BalToolsToml balToolsToml = BalToolsToml.from(globalToolsTomlPath);
        GlobalToolsManifest globalToolsManifest = GlobalToolsManifestBuilder.from(balToolsToml).build();
        if (globalToolsManifest.tools().containsKey(toolId)) {
            BalToolsManifest.Tool tool = globalToolsManifest.tools().get(toolId);
            Path toolCacheDir = ProjectUtils.createAndGetHomeReposPath()
                    .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                    .resolve(ProjectConstants.BALA_DIR_NAME).resolve(tool.org()).resolve(tool.name());
            if (toolCacheDir.toFile().isDirectory()) {
                try (Stream<Path> versions = Files.list(toolCacheDir)) {
                    return versions.anyMatch(path -> {
                        if (path.getFileName().toString().equals(version)) {
                            org = tool.org();
                            name = tool.name();
                            return true;
                        }
                        return false;
                    });
                } catch (IOException e) {
                    throw new ProjectException("Error while looking for locally available tools: " + e);
                }
            }
            return false;
        }
        return false;
    }

    private boolean isToolVersionAlreadyActive(String toolId, String version) {
        if (version.equals(Names.EMPTY.getValue())) {
            return false;
        }
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest = DistSpecificToolsManifestBuilder
                .from(distSpecificToolsToml).build();
        if (distSpecificToolsManifest.tools().containsKey(toolId)) {
            Map<String, DistSpecificToolsManifest.Tool> toolVersions = distSpecificToolsManifest.tools().get(toolId);
            return toolVersions.containsKey(version) && toolVersions.get(version).active();
        }
        return false;
    }

    private boolean isValidDistSpecificTomlFile(String fileName) {
        if (!fileName.startsWith(DIST_TOOL_TOML_PREFIX) || !fileName.endsWith(TOML_EXT)) {
            return false;
        }
        try {
            getSemVerFromDistSpecificTomlFile(fileName);
        } catch (ProjectException e) {
            return false;
        }
        return true;
    }

    private SemanticVersion getSemVerFromDistSpecificTomlFile(String fileName) {
        String distVersion = fileName.substring(DIST_TOOL_TOML_PREFIX.length(), fileName.length() - TOML_EXT.length());
        return SemanticVersion.from(distVersion);
    }

    private SemanticVersion getToolDistVersionFromCentralCache() {
        Path balaPath = ProjectUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME).resolve(org).resolve(name).resolve(version)
                .resolve(ANY_PLATFORM);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        return SemanticVersion.from(packageJson.getBallerinaVersion());
    }

    private boolean isCompatibleWithLocalDistVersion(SemanticVersion localDistVersion,
                                                     SemanticVersion toolDistVersion) {
        return localDistVersion.major() == toolDistVersion.major()
                && localDistVersion.minor() >= toolDistVersion.minor();
    }
}
