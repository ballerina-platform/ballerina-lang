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
import io.ballerina.cli.utils.PrintUtils;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
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
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.Constants.DIST_TOOL_TOML_PREFIX;
import static io.ballerina.cli.cmd.Constants.TOML_EXT;
import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_DEFAULT_DIRNAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the "bal tool" command.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_COMMAND, description = "Manage ballerina tool commands")
public class ToolCommand implements BLauncherCmd {
    private static final String TOOL_PULL_COMMAND = "pull";
    private static final String TOOL_LIST_COMMAND = "list";
    private static final String TOOL_SEARCH_COMMAND = "search";
    private static final String TOOL_REMOVE_COMMAND = "remove";
    private static final String HYPHEN = "-";

    private static final String TOOL_USAGE_TEXT = "bal tool <sub-command> [args]";
    private static final String TOOL_PULL_USAGE_TEXT = "bal tool pull <tool-id>[:<version>]";
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
            } else if (argList.get(0).equals(TOOL_LIST_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LIST_COMMAND);
            } else if (argList.get(0).equals(TOOL_REMOVE_COMMAND)) {
                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_REMOVE_COMMAND);
//            } else if (argList.get(0).equals(TOOL_SEARCH_COMMAND)) {
//                commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_SEARCH_COMMAND);
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
            case TOOL_LIST_COMMAND:
                handleListCommand();
                break;
//            case TOOL_SEARCH_COMMAND:
//                handleSearchCommand();
//                break;
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

        if (isToolLocallyAvailable(toolId, version)) {
            CommandUtil.printError(this.errStream, "tool is already pulled.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            try {
                pullToolFromCentral(supportedPlatform, balaCacheDirPath);
                insertToBalToolsTomlFile();
            } catch (PackageAlreadyExistsException e) {
                errStream.println(e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            } catch (CentralClientException e) {
                errStream.println("unexpected error occurred while pulling tool:" + e.getMessage());
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }

    private void handleListCommand() {
        if (argList.size() > 1) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_LIST_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        List<BalToolsManifest.Tool> tools = listBalToolsTomlFile();
        if (tools.isEmpty()) {
            outStream.println("no tools found locally.");
            return;
        }
        PrintUtils.printLocalTools(tools, RepoUtils.getTerminalWidth());
    }

//    private void handleSearchCommand() {
//        if (argList.size() < 2) {
//            CommandUtil.printError(this.errStream, "no keyword given.", TOOL_SEARCH_USAGE_TEXT, false);
//            CommandUtil.exitError(this.exitWhenFinish);
//            return;
//        }
//        if (argList.size() > 2) {
//            CommandUtil.printError(
//                    this.errStream, "too many arguments.", TOOL_SEARCH_USAGE_TEXT, false);
//            CommandUtil.exitError(this.exitWhenFinish);
//            return;
//        }
//
//        String searchArgs = argList.get(1);
//        searchToolsInCentral(searchArgs);
//    }

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
        org = toolInfo[0];
        name = toolInfo[1];
        version = toolInfo[2];
    }

    private void insertToBalToolsTomlFile() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();
        boolean shouldUpdateToml = true;

        // Should not update the toml version if there is already an already installed higher version
        if (distSpecificToolsManifest.tools().containsKey(toolId)) {
            SemanticVersion currentVersionInToml = SemanticVersion.from(
                    distSpecificToolsManifest.tools().get(toolId).version());
            if (currentVersionInToml.greaterThanOrEqualTo(SemanticVersion.from(version))) {
                shouldUpdateToml = false;
            }
        }

        // Update the distribution specific toml file
        if (shouldUpdateToml) {
            distSpecificToolsManifest.tools().put(toolId, new BalToolsManifest.Tool(toolId, org, name, version));
            distSpecificToolsToml.modify(distSpecificToolsManifest);
        }

        // Update the global toml file
        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        BalToolsManifest globalToolsManifest = BalToolsManifestBuilder.from(globalToolsToml).build();
        if (!globalToolsManifest.tools().containsKey(toolId)) {
            globalToolsManifest.addTool(toolId, org, name, null);
            globalToolsToml.modify(globalToolsManifest);
        }

        outStream.println(toolId  + ":" + version + " pulled successfully.");
        if (shouldUpdateToml) {
            outStream.println(toolId + ":" + version + " successfully set as the active version.");
        }
    }

    private List<BalToolsManifest.Tool> listBalToolsTomlFile() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();
        return new ArrayList<>(balToolsManifest.tools().values());
    }

    private void removeAllToolVersions() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();

        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        BalToolsManifest globalToolsManifest = BalToolsManifestBuilder.from(globalToolsToml).build();

        if (!distSpecificToolsManifest.tools().containsKey(toolId)) {
            CommandUtil.printError(errStream, "tool " + toolId + " does not exist locally.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        BalToolsManifest.Tool tool = distSpecificToolsManifest.tools().get(toolId);
        boolean isDeleted = deleteAllToolVersionsCache(tool.org(), tool.name());
        if (!isDeleted) {
            CommandUtil.printError(
                    errStream,
                    "failed to delete the tool jar for the tool " + tool.id() + ":" + tool.version() + ".",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        distSpecificToolsManifest.removeTool(toolId);
        globalToolsManifest.removeTool(toolId);

        distSpecificToolsToml.modify(distSpecificToolsManifest);
        globalToolsToml.modify(globalToolsManifest);

        outStream.println(toolId + " removed successfully.");
    }

    private void removeSpecificToolVersion() {
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();

        if (!distSpecificToolsManifest.tools().containsKey(toolId) || !isToolLocallyAvailable(toolId, version)) {
            CommandUtil.printError(errStream, "tool " + toolId + ":" + version + " does not exist locally.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        BalToolsManifest.Tool tool = distSpecificToolsManifest.tools().get(toolId);
        boolean isDeleted = deleteSpecificToolVersionCache(tool.org(), tool.name(), version);
        if (!isDeleted) {
            CommandUtil.printError(
                    errStream,
                    "failed to delete tool cache for the tool " + tool.id() + ":" + tool.version() + ".",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (!version.equals(distSpecificToolsManifest.tools().get(toolId).version())) {
            // The version to be deleted is not the active version. So, no need to update the toml file
            outStream.println(toolId + ":" + version + " removed successfully.");
            return;
        }
        Path toolPkgDir = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, tool.org(), tool.name());
        if (Files.exists(toolPkgDir) && Files.isDirectory(toolPkgDir)) {
            try (Stream<Path> stream = Files.list(toolPkgDir)) {
                if (stream.findAny().isEmpty()) {
                    // No other versions exist. Remove the tool from the toml file
                    distSpecificToolsManifest.removeTool(toolId);
                    distSpecificToolsToml.modify(distSpecificToolsManifest);
                    outStream.println(toolId + ":" + version + " removed successfully.");
                } else {
                    // set the next latest version as the active version
                    try (Stream<Path> stream2 = Files.list(toolPkgDir)) {
                        Optional<SemanticVersion> latestVersionOpt = stream2
                                .map(path -> path.getFileName().toString())
                                .map(SemanticVersion::from)
                                .max((v1, v2) -> {
                                    if (v1.greaterThan(v2)) {
                                        return 1;
                                    } else if (v2.greaterThan(v1)) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                });

                        if (latestVersionOpt.isPresent()) {
                            String latestVersion = latestVersionOpt.get().toString();
                            distSpecificToolsManifest.removeTool(toolId);
                            distSpecificToolsManifest.addTool(toolId, tool.org(), tool.name(), latestVersion);
                            distSpecificToolsToml.modify(distSpecificToolsManifest);
                            outStream.println(toolId + ":" + version + " removed successfully.");
                            outStream.println(toolId + ":" + latestVersion + " successfully set as the active " +
                                    "version.");
                        }
                    } catch (IOException e) {
                        CommandUtil.printError(errStream, "failed to list the tool package directory.", e.getMessage(),
                                false);
                        CommandUtil.exitError(this.exitWhenFinish);
                    }
                }
            } catch (IOException e) {
                CommandUtil.printError(errStream, "failed to list the tool package directory.", e.getMessage(), false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }

    private boolean deleteAllToolVersionsCache(String org, String name) {
        Path toolPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name);
        if (!Files.exists(toolPath) || !Files.isDirectory(toolPath)) {
            return false;
        }
        return ProjectUtils.deleteDirectory(toolPath);
    }

    private boolean deleteSpecificToolVersionCache(String org, String name, String version) {
        Path toolPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, org, name, version);
        if (!Files.exists(toolPath) || !Files.isDirectory(toolPath)) {
            return false;
        }
        return ProjectUtils.deleteDirectory(toolPath);
    }

//    /**
//     * Search for tools in central.
//     *
//     * @param keyword search keyword.
//     */
//    private void searchToolsInCentral(String keyword) {
//        try {
//            Settings settings;
//            try {
//                settings = RepoUtils.readSettings();
//                // Ignore Settings.toml diagnostics in the search command
//            } catch (SettingsTomlException e) {
//                // Ignore 'Settings.toml' parsing errors and return empty Settings object
//                settings = Settings.from();
//            }
//            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
//                    initializeProxy(settings.getProxy()),
//                    getAccessTokenOfCLI(settings));
//            ToolSearchResult toolSearchResult = client.searchTool(keyword,
//                    JvmTarget.JAVA_11.code(),
//                    RepoUtils.getBallerinaVersion());
//
//            List<Tool> tools = toolSearchResult.getTools();
//            if (tools != null && tools.size() > 0) {
//                printTools(toolSearchResult.getTools(), RepoUtils.getTerminalWidth());
//            } else {
//                outStream.println("no tools found.");
//            }
//        } catch (CentralClientException e) {
//            String errorMessage = e.getMessage();
//            if (null != errorMessage && !"".equals(errorMessage.trim())) {
//                // removing the error stack
//                if (errorMessage.contains("\n\tat")) {
//                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
//                }
//                CommandUtil.printError(this.errStream, errorMessage, null, false);
//                CommandUtil.exitError(this.exitWhenFinish);
//            }
//        }
//    }

    private boolean isToolLocallyAvailable(String toolId, String version) {
        if (version.equals(Names.EMPTY.getValue())) {
            return false;
        }
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();
        if (distSpecificToolsManifest.tools().containsKey(toolId)) {
            BalToolsManifest.Tool tool = distSpecificToolsManifest.tools().get(toolId);
            Path toolPath = Path.of(
                    System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, REPOSITORIES_DIR,
                    CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME, tool.org(), tool.name(), version);
            return toolPath.toFile().exists();
        }
        return false;
    }
}
