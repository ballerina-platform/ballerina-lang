/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.launcher.util;

import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.cli.cmd.ToolCommand;
import io.ballerina.cli.launcher.CustomToolClassLoader;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.Constants.ADD_COMMAND;
import static io.ballerina.cli.cmd.Constants.ASYNCAPI_COMMAND;
import static io.ballerina.cli.cmd.Constants.BINDGEN_COMMAND;
import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.cli.cmd.Constants.CLEAN_COMMAND;
import static io.ballerina.cli.cmd.Constants.DEBUG_OPTION;
import static io.ballerina.cli.cmd.Constants.DEPRECATE_COMMAND;
import static io.ballerina.cli.cmd.Constants.DIST_COMMAND;
import static io.ballerina.cli.cmd.Constants.DOC_COMMAND;
import static io.ballerina.cli.cmd.Constants.FORMAT_COMMAND;
import static io.ballerina.cli.cmd.Constants.GENCACHE_COMMAND;
import static io.ballerina.cli.cmd.Constants.GRAPHQL_COMMAND;
import static io.ballerina.cli.cmd.Constants.GRAPH_COMMAND;
import static io.ballerina.cli.cmd.Constants.GRPC_COMMAND;
import static io.ballerina.cli.cmd.Constants.HELP_COMMAND;
import static io.ballerina.cli.cmd.Constants.HELP_OPTION;
import static io.ballerina.cli.cmd.Constants.HELP_SHORT_OPTION;
import static io.ballerina.cli.cmd.Constants.HOME_COMMAND;
import static io.ballerina.cli.cmd.Constants.INIT_COMMAND;
import static io.ballerina.cli.cmd.Constants.NEW_COMMAND;
import static io.ballerina.cli.cmd.Constants.OPENAPI_COMMAND;
import static io.ballerina.cli.cmd.Constants.PACK_COMMAND;
import static io.ballerina.cli.cmd.Constants.PERSIST_COMMAND;
import static io.ballerina.cli.cmd.Constants.PROFILE_COMMAND;
import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.cli.cmd.Constants.SEARCH_COMMAND;
import static io.ballerina.cli.cmd.Constants.SEMVER_COMMAND;
import static io.ballerina.cli.cmd.Constants.SHELL_COMMAND;
import static io.ballerina.cli.cmd.Constants.START_DEBUG_ADAPTER_COMMAND;
import static io.ballerina.cli.cmd.Constants.START_LANG_SERVER_COMMAND;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.UPDATE_COMMAND;
import static io.ballerina.cli.cmd.Constants.VERSION_COMMAND;
import static io.ballerina.cli.cmd.Constants.VERSION_OPTION;
import static io.ballerina.cli.cmd.Constants.VERSION_SHORT_OPTION;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;

/**
 * This class contains utility functions needed for Bal Tool tasks in the Main class.
 *
 * @since 2201.8.0
 */
public class BalToolsUtil {
    private static final String TOOL = "tool";
    private static final String LIBS = "libs";

    private static final List<String> options = Arrays.asList(VERSION_OPTION, VERSION_SHORT_OPTION, HELP_OPTION,
            HELP_SHORT_OPTION, DEBUG_OPTION);
    private static final List<String> coreCommands = Arrays.asList(
            BUILD_COMMAND, RUN_COMMAND, TEST_COMMAND, DOC_COMMAND, PACK_COMMAND);
    private static final List<String> packageCommands = Arrays.asList(NEW_COMMAND, ADD_COMMAND, PULL_COMMAND,
            PUSH_COMMAND, SEARCH_COMMAND, SEMVER_COMMAND, GRAPH_COMMAND, DEPRECATE_COMMAND);
    // if a command is a built-in tool command, remove it from this list
    private static final List<String> otherCommands = Arrays.asList(CLEAN_COMMAND, FORMAT_COMMAND, BINDGEN_COMMAND,
            SHELL_COMMAND, VERSION_COMMAND, OPENAPI_COMMAND, GRAPHQL_COMMAND, ASYNCAPI_COMMAND, GRPC_COMMAND,
            PERSIST_COMMAND, PROFILE_COMMAND);
    private static final List<String> hiddenCommands = Arrays.asList(INIT_COMMAND, TOOL_COMMAND, DIST_COMMAND,
            UPDATE_COMMAND, START_LANG_SERVER_COMMAND, START_DEBUG_ADAPTER_COMMAND, HELP_COMMAND, HOME_COMMAND,
            GENCACHE_COMMAND);
    // if a command is a built-in tool command, add it to this list
    private static final List<String> builtInToolCommands = Arrays.asList();

    private static final Path balToolsTomlPath = RepoUtils.createAndGetHomeReposPath().resolve(
            Path.of(CONFIG_DIR, BAL_TOOLS_TOML));
    private static final Path balaCacheDirPath = ProjectUtils.createAndGetHomeReposPath()
            .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
            .resolve(ProjectConstants.BALA_DIR_NAME);

    public static boolean isNonBuiltInToolCommand(String commandName) {
        return isToolCommand(commandName) && !builtInToolCommands.contains(commandName);
    }

    public static boolean isToolCommand(String commandName) {
        return Stream.of(options, coreCommands, packageCommands, otherCommands, hiddenCommands)
                .flatMap(List::stream).noneMatch(commandName::equals);
    }

    public static CustomToolClassLoader getCustomToolClassLoader(String commandName) {
        List<File> toolJars = getToolCommandJarAndDependencyJars(commandName);
        URL[] urls = toolJars.stream()
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw LauncherUtils.createUsageExceptionWithHelp("invalid tool jar: " + file
                                .getAbsolutePath());
                    }
                })
                .toArray(URL[]::new);
        // Combine custom class loader with system class loader
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        return new CustomToolClassLoader(urls, systemClassLoader);
    }

    public static void addToolIfCommandIsABuiltInTool(String commandName) {
        // if not a built in tool, return
        if (!builtInToolCommands.contains(commandName)) {
            return;
        }
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        // if built in tool is already added, return
        if (balToolsManifest.getActiveTool(commandName).isPresent()) {
            return;
        }
        ToolCommand toolCommand = new ToolCommand();
        toolCommand.pullToolAndUpdateBalToolsToml(commandName, Names.EMPTY.getValue());
    }

    private static List<File> getToolCommandJarAndDependencyJars(String commandName) {
        Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
        Path balToolsTomlPath = userHomeDirPath.resolve(Path.of(CONFIG_DIR, BAL_TOOLS_TOML));
        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
        Path localBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, ProjectConstants.LOCAL_REPOSITORY_NAME, BALA_DIR_NAME));
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        // we load all tool jars for the help, default commands and --help, -h options
        if (HELP_COMMAND.equals(commandName)) {
            return balToolsManifest.tools().values().stream()
                    .flatMap(map -> map.values().stream())
                    .flatMap(map -> map.values().stream())
                    .filter(BalToolsManifest.Tool::active)
                    .map(tool1 -> findJarFiles(CommandUtil.getPlatformSpecificBalaPath(
                        tool1.org(), tool1.name(), tool1.version(), ProjectConstants.LOCAL_REPOSITORY_NAME
                                            .equals(tool1.repository()) ? localBalaDirPath : centralBalaDirPath)
                                            .resolve(TOOL).resolve(LIBS)
                        .toFile()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        Optional<BalToolsManifest.Tool> toolOpt = balToolsManifest.getActiveTool(commandName);
        if (toolOpt.isPresent()) {
            BalToolsManifest.Tool tool = toolOpt.get();
            if (!isToolDistCompatibilityWithCurrentDist(tool)) {
                String errMsg = "tool '" + tool.id() + ":" + tool.version() +
                        "' is not compatible with the current Ballerina distribution '" +
                        RepoUtils.getBallerinaShortVersion() +
                        "'. Use 'bal tool search' to select a version compatible with the " +
                        "current Ballerina distribution.";
                throw LauncherUtils.createLauncherException(errMsg);
            }
            Path platformPath = CommandUtil.getPlatformSpecificBalaPath(
                    tool.org(), tool.name(), tool.version(), ProjectConstants.LOCAL_REPOSITORY_NAME
                            .equals(tool.repository()) ? localBalaDirPath : centralBalaDirPath);
            File libsDir = platformPath.resolve(Path.of(TOOL, LIBS)).toFile();
            return findJarFiles(libsDir);
        }
        throw LauncherUtils.createUsageExceptionWithHelp("unknown command '" + commandName + "'");
    }

    private static boolean isToolDistCompatibilityWithCurrentDist(BalToolsManifest.Tool tool) {
        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = getToolDistVersionFromCentralCache(tool);
        return isVersionsCompatible(currentDistVersion, toolDistVersion);
    }

    private static SemanticVersion getToolDistVersionFromCentralCache(BalToolsManifest.Tool tool) {
        Path centralBalaDirPath = ProjectUtils.createAndGetHomeReposPath().resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, ProjectConstants.BALA_DIR_NAME));
        Path localBalaPath = ProjectUtils.createAndGetHomeReposPath().resolve(
                Path.of(REPOSITORIES_DIR, ProjectConstants.LOCAL_REPOSITORY_NAME, ProjectConstants.BALA_DIR_NAME));
        Path balaPath =  CommandUtil.getPlatformSpecificBalaPath(
                tool.org(), tool.name(), tool.version(), ProjectConstants.LOCAL_REPOSITORY_NAME
                        .equals(tool.repository()) ? localBalaPath : centralBalaDirPath);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        return SemanticVersion.from(packageJson.getBallerinaVersion());
    }

    private static boolean isVersionsCompatible(SemanticVersion localDistVersion,
                                                SemanticVersion toolDistVersion) {
        return localDistVersion.major() == toolDistVersion.major()
                && localDistVersion.minor() >= toolDistVersion.minor();
    }

    /**
     * Find jar files in the given directory.
     *
     * @param directory directory to search for jar files
     * @return list of jar files
     */
    public static List<File> findJarFiles(File directory) {
        List<File> jarFiles = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                        jarFiles.add(file);
                    }
                }
            }
        }
        return jarFiles;
    }

    /**
     * Update the bal-tools.toml file with the version and the active flags.
     * bal-tools.tomls of updates 6, 7  only has id, name and org fields. Therefore, we need to update the
     * bal-tools.toml file when the user moves from updates 6, 7 to update 8 and above.
     */
    public static void updateOldBalToolsToml() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifestBuilder balToolsManifestBuilder = BalToolsManifestBuilder.from(balToolsToml);
        BalToolsManifest balToolsManifest = balToolsManifestBuilder.build();
        Map<String, BalToolsManifestBuilder.OldTool> oldTools = balToolsManifestBuilder.getOldTools();
        if (oldTools.isEmpty()) {
            return;
        }
        oldTools.values().stream().forEach(tool -> {
            Path toolCachePath = balaCacheDirPath.resolve(Path.of(tool.org(), tool.name()));
            if (toolCachePath.toFile().isDirectory()) {
                List<String> versions = Arrays.stream(toolCachePath.toFile().listFiles((dir, name) -> {
                    try {
                        PackageVersion.from(name);
                        return true;
                    } catch (ProjectException ignore) {
                        return false;
                    }
                })).map(File::getName).collect(Collectors.toList());

                Optional<String> latestVersion = getLatestVersion(versions);
                versions.stream().forEach(version -> {
                    // If there is no current active version in balToolsManifest, we set the latest version in the
                    // central cache as active. This is because in U6, U7, the latest is automatically picked as active.
                    boolean isActive = balToolsManifest.getActiveTool(tool.id()).isEmpty()
                            && latestVersion.isPresent()
                            && latestVersion.get().equals(version);
                    if (balToolsManifest.getTool(tool.id(), version, null).isEmpty()) {
                        balToolsManifest.addTool(tool.id(), tool.org(), tool.name(), version, isActive, null);
                    }
                });
            }
        });
        balToolsToml.modify(balToolsManifest);
    }

    private static Optional<String> getLatestVersion(List<String> versions) {
        return versions.stream().map(SemanticVersion::from)
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
    }
}
