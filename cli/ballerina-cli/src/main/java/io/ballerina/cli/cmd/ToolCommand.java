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
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
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

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printTools;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_DEFAULT_DIRNAME;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validatePackageName;
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
    private static final String PULL_COMMAND = "pull";
    private static final String LIST_COMMAND = "list";
    private static final String SEARCH_COMMAND = "search";
    private static final String REMOVE_COMMAND = "remove";

    public static final String TOOL_USAGE_TEXT = "bal tool <sub-command> [args]";
    public static final String TOOL_PULL_USAGE_TEXT = "bal tool pull <tool-id>[:<version>]";
    public static final String TOOL_LIST_USAGE_TEXT = "bal tool list";
    public static final String TOOL_REMOVE_USAGE_TEXT = "bal tool remove <tool-id>:[<version>]";
    public static final String TOOL_SEARCH_USAGE_TEXT = "bal tool search [<tool-id>|<org>|<package>|<text>] ";

    private final boolean exitWhenFinish;
    private final PrintStream errStream;

    Path balToolsTomlPath = Path.of(
            System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, BAL_TOOLS_TOML);

    @CommandLine.Parameters(description = "Manage ballerina tools")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    private String toolId;
    private String org;
    private String pkgName;
    private String version;

    public ToolCommand() {
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public ToolCommand(PrintStream errStream, boolean exitWhenFinish) {
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
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no sub-command given.", TOOL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String command = argList.get(0);
        switch (command) {
            case PULL_COMMAND:
                handlePullCommand();
                break;
            case LIST_COMMAND:
                handleListCommand();
                break;
            case SEARCH_COMMAND:
                handleSearchCommand();
                break;
            case REMOVE_COMMAND:
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
                .resolve(ProjectConstants.REPOSITORIES_DIR).resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);

        if (isToolLocallyAvailable(toolIdAndVersion)) {
            CommandUtil.printError(this.errStream, "tool is already pulled.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            try {
                pullToolFromCentral(supportedPlatform, balaCacheDirPath);

                String toolPathInCentralCache = getToolPathInCentralCache();
                if (toolPathInCentralCache == null) {
                    CommandUtil.printError(this.errStream, "tool jar not found.", null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                updateBalToolsTomlFile(toolPathInCentralCache);
                errStream.println(toolId  + ":" + version + " pulled successfully.");
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
            errStream.println("no tools found locally.");
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

        if (!validatePackageName(toolId)) {
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

        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        boolean removeSuccess;
        if (Names.EMPTY.getValue().equals(version)) {
            removeSuccess = removeAllToolVersions(balToolsManifest);
        } else {
            removeSuccess = removeSpecificToolVersion(balToolsManifest);
        }
        if (removeSuccess) {
            balToolsToml.modify(balToolsManifest);
            errStream.println(toolIdAndVersion + " removed successfully.");
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
                initializeProxy(settings.getProxy()),
                getAccessTokenOfCLI(settings));
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        org = toolInfo[0];
        pkgName = toolInfo[1];
        version = toolInfo[2];
    }

    private String getToolPathInCentralCache() {
        Path versionPath = ProjectUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR).resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME).resolve(org).resolve(pkgName).resolve(version);
        File anyPlatformDir = versionPath.resolve(ANY_PLATFORM).toFile();
        File java11PlatformDir = versionPath.resolve(JvmTarget.JAVA_11.code()).toFile();

        if (anyPlatformDir.exists() && anyPlatformDir.isDirectory()) {
            return anyPlatformDir.toString();
        }
        if (java11PlatformDir.exists() && java11PlatformDir.isDirectory()) {
            return java11PlatformDir.toString();
        }
        return null;
    }

    private void updateBalToolsTomlFile(String toolPathInCentralCache) {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml)
                .addTool(toolId, version, toolPathInCentralCache)
                .build();
        balToolsToml.modify(balToolsManifest);
    }

    private List<BalToolsManifest.Tool> listBalToolsTomlFile() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        return new ArrayList<>(balToolsManifest.tools().values());
    }

    private boolean isToolLocallyAvailable(String toolIdAndVersion) {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        return balToolsManifest.tools().containsKey(toolIdAndVersion);
    }

    private boolean removeAllToolVersions(BalToolsManifest balToolsManifest) {
        boolean foundTools = false;

        Iterator<BalToolsManifest.Tool> iter = balToolsManifest.tools().values().iterator();
        while (iter.hasNext()) {
            BalToolsManifest.Tool tool = iter.next();
            if (tool.id().equals(toolId)) {
                String mapId = tool.id() + ":" + tool.version();
                boolean isDeleted = deletePackageCentralCache(tool.path());
                if (!isDeleted) {
                    CommandUtil.printError(
                            errStream, "failed to delete the tool jar for the tool " + mapId + ".", null, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return false;
                }
                iter.remove();
                foundTools = true;
            }
        }
        if (!foundTools) {
            CommandUtil.printError(errStream, "tool " + toolId + " does not exist locally.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return false;
        }
        return true;
    }

    private boolean removeSpecificToolVersion(BalToolsManifest balToolsManifest) {
        String mapId = toolId + ":" + version;
        // if version is specified remove only the given version.
        if (balToolsManifest.tools().containsKey(mapId)) {
            boolean isDeleted = deletePackageCentralCache(balToolsManifest.tools().get(mapId).path());
            if (!isDeleted) {
                CommandUtil.printError(errStream, "failed to delete the tool jar for the tool " + mapId + ".",
                        null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return false;
            }
            balToolsManifest.removeTool(mapId);
        } else {
            CommandUtil.printError(errStream, "tool " + mapId + " does not exist locally.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return false;
        }
        return true;
    }

    private boolean deletePackageCentralCache(String platformPath) {
        Optional<Path> versionDir = Optional.ofNullable(Path.of(platformPath).getParent());
        return versionDir.filter(ProjectUtils::deleteDirectory).isPresent();
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
                    initializeProxy(settings.getProxy()),
                    getAccessTokenOfCLI(settings));
            ToolSearchResult toolSearchResult = client.searchTool(keyword,
                    JvmTarget.JAVA_11.code(),
                    RepoUtils.getBallerinaVersion());

            List<Tool> tools = toolSearchResult.getTools();
            if (tools != null && tools.size() > 0) {
                printTools(toolSearchResult.getTools(), RepoUtils.getTerminalWidth());
            } else {
                errStream.println("no tools found.");
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
            }
        }
    }
}
