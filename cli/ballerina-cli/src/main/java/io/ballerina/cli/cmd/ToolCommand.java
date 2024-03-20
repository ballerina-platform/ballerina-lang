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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.utils.PrintUtils;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.central.client.model.Tool;
import org.ballerinalang.central.client.model.ToolSearchResult;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.utils.PrintUtils.printTools;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.validateToolName;

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
    private static final String TOOL_USE_COMMAND = "use";
    private static final String TOOL_UPDATE_COMMAND = "update";
    private static final String HYPHEN = "-";

    private static final String TOOL_USAGE_TEXT = "bal tool <sub-command> [args]";
    private static final String TOOL_PULL_USAGE_TEXT = "bal tool pull <tool-id>[:<version>]";
    private static final String TOOL_USE_USAGE_TEXT = "bal tool use <tool-id>:<version>";
    private static final String TOOL_LIST_USAGE_TEXT = "bal tool list";
    private static final String TOOL_REMOVE_USAGE_TEXT = "bal tool remove <tool-id>:[<version>]";
    private static final String TOOL_SEARCH_USAGE_TEXT = "bal tool search [<tool-id>|<text>]";
    private static final String TOOL_UPDATE_USAGE_TEXT = "bal tool update <tool-id>";
    private static final String EMPTY_STRING = Names.EMPTY.getValue();

    private final boolean exitWhenFinish;
    private final PrintStream outStream;
    private final PrintStream errStream;

    Path balToolsTomlPath = RepoUtils.createAndGetHomeReposPath().resolve(Path.of(CONFIG_DIR, BAL_TOOLS_TOML));

    @CommandLine.Parameters(description = "Manage ballerina tools")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--repository")
    private String repositoryName;

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
            handleHelpFlag();
            return;
        }

        if (argList == null || argList.isEmpty()) {
            CommandUtil.printError(this.errStream, "no sub-command given.", TOOL_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
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

        String command = argList.get(0);
        switch (command) {
            case TOOL_PULL_COMMAND -> handlePullCommand();
            case TOOL_USE_COMMAND -> handleUseCommand();
            case TOOL_LIST_COMMAND -> handleListCommand();
            case TOOL_SEARCH_COMMAND -> handleSearchCommand();
            case TOOL_REMOVE_COMMAND -> handleRemoveCommand();
            case TOOL_UPDATE_COMMAND -> handleUpdateCommand();
            default -> {
                CommandUtil.printError(this.errStream, "invalid sub-command given.", TOOL_USAGE_TEXT, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
        }
    }

    private void handleHelpFlag() {
        String commandUsageInfo;
        String command = (argList == null || argList.isEmpty()) ? TOOL_COMMAND : argList.get(0);
        commandUsageInfo = switch (command) {
            case TOOL_PULL_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_PULL_COMMAND);
            case TOOL_USE_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_USE_COMMAND);
            case TOOL_LIST_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_LIST_COMMAND);
            case TOOL_REMOVE_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_REMOVE_COMMAND);
            case TOOL_SEARCH_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_SEARCH_COMMAND);
            case TOOL_UPDATE_COMMAND -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND + HYPHEN + TOOL_UPDATE_COMMAND);
            default -> BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND);
        };
        outStream.println(commandUsageInfo);
    }

    private void handlePullCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_PULL_USAGE_TEXT, false);
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

        if (LOCAL_REPOSITORY_NAME.equals(repositoryName) && EMPTY_STRING.equals(version)) {
            CommandUtil.printError(errStream, "tool version should be provided when pulling a tool from local " +
                            "repository", null, false);
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

        pullToolAndUpdateBalToolsToml(toolId, version);
    }

    private void handleUseCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_USE_USAGE_TEXT, false);
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

        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Optional<BalToolsManifest.Tool> tool = balToolsManifest.getTool(toolId, version, repositoryName);
        if (tool.isEmpty()) {
            boolean isLocalTool = isToolAvailableInLocalRepo(toolId, version);
            if (isLocalTool) {
                CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not found. " +
                        "Run 'bal tool pull " + toolId + ":" + version
                        + "' or 'bal tool pull " + toolId + ":" + version
                        + " --repository=local' to fetch and set as the active version.", null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not found. " +
                    "Run 'bal tool pull " + toolId + ":" + version
                    + "' to fetch and set as the active version.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        this.org = tool.get().org();
        this.name = tool.get().name();
        Optional<BalToolsManifest.Tool> currentActiveTool = balToolsManifest.getActiveTool(toolId);
        if (currentActiveTool.isPresent() && currentActiveTool.get().version().equals(tool.get().version()) &&
                Objects.equals(currentActiveTool.get().repository(), tool.get().repository())) {
            outStream.println("tool '" + toolId + ":" + version + "' is the current active version.");
            return;
        }

        boolean isDistsCompatible = checkToolDistCompatibility();
        if (!isDistsCompatible) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        balToolsManifest.setActiveToolVersion(toolId, version, repositoryName);
        balToolsToml.modify(balToolsManifest);
        outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
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
        if (LOCAL_REPOSITORY_NAME.equals(repositoryName)) {
            CommandUtil.printError(errStream, "Local repository option is not supported with tool search command",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String searchArgs = argList.get(1);
        searchToolsInCentral(searchArgs);
    }

    private void handleRemoveCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_REMOVE_USAGE_TEXT, false);
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

    private void handleUpdateCommand() {
        if (argList.size() < 2) {
            CommandUtil.printError(this.errStream, "tool id is not provided.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (argList.size() > 2) {
            CommandUtil.printError(
                    this.errStream, "too many arguments.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (LOCAL_REPOSITORY_NAME.equals(repositoryName)) {
            CommandUtil.printError(errStream, "tool update command is not supported for local repository.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        toolId = argList.get(1);
        if (!validateToolName(toolId)) {
            CommandUtil.printError(errStream, "invalid tool id.", TOOL_UPDATE_USAGE_TEXT, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        updateToolToLatestVersion();
    }

    public void pullToolAndUpdateBalToolsToml(String toolIdArg, String versionArg) {
        toolId = toolIdArg;
        version = versionArg;
        Path balaCacheDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);

        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        if (LOCAL_REPOSITORY_NAME.equals(repositoryName)) {
            if (!isToolAvailableInLocalRepo(toolId, version)) {
                errStream.println("tool '" + toolId + ":" + version + "' is not available in local repository." +
                        "\nUse 'bal push --repository=local' to publish it.");
                CommandUtil.exitError(this.exitWhenFinish);
            }
            addToBalToolsToml();
            return;
        }
        try {
            if (isToolAvailableLocally(toolId, version)) {
                outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
            } else {
                pullToolFromCentral(supportedPlatform, balaCacheDirPath);
            }
            addToBalToolsToml();
        } catch (PackageAlreadyExistsException e) {
            errStream.println(e.getMessage());
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (CentralClientException | ProjectException e) {
            CommandUtil.printError(errStream, "unexpected error occurred while pulling tool:" + e.getMessage(),
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
        }
    }

    private boolean isToolAvailableInLocalRepo(String toolId, String version) {
        JsonObject localToolJson;
        Gson gson = new Gson();
        Path localBalaPath = RepoUtils.createAndGetHomeReposPath().resolve(Path.of(REPOSITORIES_DIR,
                LOCAL_REPOSITORY_NAME, BALA_DIR_NAME));
        Path localToolJsonPath = localBalaPath.resolve(ProjectConstants.LOCAL_TOOLS_JSON);
        if (!Files.exists(localToolJsonPath)) {
            return false;
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(localToolJsonPath, StandardCharsets.UTF_8)) {
            localToolJson = gson.fromJson(bufferedReader, JsonObject.class);
            JsonElement localTool = localToolJson.get(toolId);
            if (localTool == null) {
                return false;
            }
            JsonObject pkgDesc = localTool.getAsJsonObject();
            if (pkgDesc.isEmpty()) {
                return false;
            }
            org = pkgDesc.get(ProjectConstants.ORG).getAsString();
            name = pkgDesc.get(ProjectConstants.PACKAGE_NAME).getAsString();
            return Files.exists(localBalaPath.resolve(org).resolve(name).resolve(version));
        } catch (IOException e) {
            throw new ProjectException("Failed to read local-tools.json file: " + e.getMessage());
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
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
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

    private void addToBalToolsToml() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        boolean isDistsCompatible = checkToolDistCompatibility();
        if (!isDistsCompatible) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (isToolVersionAlreadyActive(toolId, version)) {
            outStream.println("tool '" + toolId + ":" + version + "' is already active.");
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        balToolsManifest.addTool(toolId, org, name, version, true, repositoryName);
        balToolsToml.modify(balToolsManifest);
        outStream.println("tool '" + toolId + ":" + version + "' successfully set as the active version.");
    }

    private List<BalToolsManifest.Tool> listBalToolsTomlFile() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        List<BalToolsManifest.Tool> flattenedTools = new ArrayList<>();
        balToolsManifest.tools().values().stream()
                .flatMap(map -> map.values().stream()).flatMap(map -> map.values().stream())
                .sorted(Comparator.comparing(BalToolsManifest.Tool::id)
                        .thenComparing(BalToolsManifest.Tool::version).reversed())
                .forEach(flattenedTools::add);
        return flattenedTools;
    }

    private void removeAllToolVersions() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        Optional<Map<String, Map<String, BalToolsManifest.Tool>>> toolVersions =
                Optional.ofNullable(balToolsManifest.tools().get(toolId));
        if (toolVersions.isEmpty() || toolVersions.get().isEmpty()) {
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
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        Optional<BalToolsManifest.Tool> tool = balToolsManifest.getTool(toolId, version, repositoryName);
        if (tool.isEmpty()) {
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' not found.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (tool.get().active()) {
            CommandUtil.printError(errStream, "cannot remove active tool '" + toolId + ":" + version + "'.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        org = tool.get().org();
        name = tool.get().name();
        boolean isDistsCompatible = checkToolDistCompatibility();
        if (!isDistsCompatible) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        balToolsManifest.removeToolVersion(toolId, version, repositoryName);
        balToolsToml.modify(balToolsManifest);
        if (repositoryName != null) {
            outStream.println("tool '" + toolId + ":" + version + "' successfully removed.");
            return;
        }
        deleteCachedToolVersion(tool.get().org(), tool.get().name(), version);
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
                    settings.getProxy().password(), getAccessTokenOfCLI(settings),
                    settings.getCentral().getConnectTimeout(),
                    settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                    settings.getCentral().getCallTimeout());
            boolean foundTools = false;
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(JvmTarget::code)
                    .collect(Collectors.joining(","));
            ToolSearchResult toolSearchResult = client.searchTool(keyword, supportedPlatform,
                    RepoUtils.getBallerinaVersion());
            List<Tool> tools = toolSearchResult.getTools();
            if (tools != null && tools.size() > 0) {
                foundTools = true;
                printTools(toolSearchResult.getTools(), RepoUtils.getTerminalWidth());
            }
            if (!foundTools) {
                outStream.println("no tools found.");
            }
        } catch (CentralClientException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !EMPTY_STRING.equals(errorMessage.trim())) {
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

    private boolean isToolAvailableLocally(String toolId, String version) {
        if (version.equals(Names.EMPTY.getValue())) {
            return false;
        }
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Optional<BalToolsManifest.Tool> toolOptional = balToolsManifest.getTool(toolId, version, repositoryName);
        if (toolOptional.isEmpty()) {
            return false;
        }
        BalToolsManifest.Tool tool = toolOptional.get();
        org = tool.org();
        name = tool.name();

        Path toolCacheDir = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME).resolve(tool.org()).resolve(tool.name());
        if (toolCacheDir.toFile().isDirectory()) {
            try (Stream<Path> versions = Files.list(toolCacheDir)) {
                return versions.anyMatch(path -> path.getFileName().toString().equals(version));
            } catch (IOException e) {
                throw new ProjectException("Error while looking for locally available tools: " + e);
            }
        }
        return false;
    }

    private boolean checkToolDistCompatibility() {
        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = LOCAL_REPOSITORY_NAME.equals(repositoryName)
                ? getToolDistVersionFromCache(LOCAL_REPOSITORY_NAME)
                : getToolDistVersionFromCache(CENTRAL_REPOSITORY_CACHE_NAME);
        if (!isCompatibleWithLocalDistVersion(currentDistVersion, toolDistVersion)) {
            CommandUtil.printError(errStream, "tool '" + toolId + ":" + version + "' is not compatible with the " +
                    "current Ballerina distribution '" + RepoUtils.getBallerinaShortVersion() +
                    "'. Use 'bal tool search' to select a version compatible with the current Ballerina distribution.",
                    null, false);
            return false;
        }
        return true;
    }

    private SemanticVersion getToolDistVersionFromCache(String repositoryName) {
        Path balaDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(repositoryName)
                .resolve(ProjectConstants.BALA_DIR_NAME);
        Path balaPath = CommandUtil.getPlatformSpecificBalaPath(org, name, version, balaDirPath);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        return SemanticVersion.from(packageJson.getBallerinaVersion());
    }

    private boolean isCompatibleWithLocalDistVersion(SemanticVersion localDistVersion,
                                                     SemanticVersion toolDistVersion) {
        return localDistVersion.major() == toolDistVersion.major()
                && localDistVersion.minor() >= toolDistVersion.minor();
    }

    private boolean isToolVersionAlreadyActive(String toolId, String version) {
        if (version.equals(Names.EMPTY.getValue())) {
            return false;
        }
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        if (balToolsManifest.tools().containsKey(toolId)) {
            Map<String, Map<String, BalToolsManifest.Tool>> toolVersions = balToolsManifest.tools().get(toolId);
            return toolVersions.containsKey(version) && toolVersions.get(version).containsKey(repositoryName) &&
                    toolVersions.get(version).get(repositoryName).active();
        }
        return false;
    }

    private void updateToolToLatestVersion() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Optional<BalToolsManifest.Tool> tool = balToolsManifest.getActiveTool(toolId);
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

        Path balaCacheDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);

        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        try {
            version = getLatestVersionForUpdateCommand(supportedPlatform, tool.get());
            if (tool.get().version().equals(version)) {
                outStream.println("tool '" + toolId + "' is already up-to-date.");
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            if (isToolAvailableLocally(toolId, version)) {
                outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
            } else {
                pullToolFromCentral(supportedPlatform, balaCacheDirPath);
            }
            addToBalToolsToml();
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
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
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
}
