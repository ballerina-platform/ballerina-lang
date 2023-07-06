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
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.DistPackedToolsManifest;
import io.ballerina.projects.DistSpecificToolsManifest;
import io.ballerina.projects.GlobalToolsManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.DistPackedToolsManifestBuilder;
import io.ballerina.projects.internal.DistSpecificToolsManifestBuilder;
import io.ballerina.projects.internal.GlobalToolsManifestBuilder;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import static io.ballerina.cli.cmd.Constants.DIST_TOOL_TOML_PREFIX;
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
import static io.ballerina.cli.cmd.Constants.PULL_COMMAND;
import static io.ballerina.cli.cmd.Constants.PUSH_COMMAND;
import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.cli.cmd.Constants.SEARCH_COMMAND;
import static io.ballerina.cli.cmd.Constants.SEMVER_COMMAND;
import static io.ballerina.cli.cmd.Constants.SHELL_COMMAND;
import static io.ballerina.cli.cmd.Constants.START_DEBUG_ADAPTER_COMMAND;
import static io.ballerina.cli.cmd.Constants.START_LANG_SERVER_COMMAND;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.cli.cmd.Constants.TOML_EXT;
import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;
import static io.ballerina.cli.cmd.Constants.UPDATE_COMMAND;
import static io.ballerina.cli.cmd.Constants.VERSION_COMMAND;
import static io.ballerina.cli.cmd.Constants.VERSION_OPTION;
import static io.ballerina.cli.cmd.Constants.VERSION_SHORT_OPTION;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_HOME;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOL_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_DEFAULT_DIRNAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;

/**
 * This class contains utility functions needed for Bal Tool tasks in the Main class.
 *
 * @since 2201.8.0
 */
public class BalToolUtil {
    private static final String TOOL = "tool";
    private static final String LIBS = "libs";

    private static final List<String> options = Arrays.asList(VERSION_OPTION, VERSION_SHORT_OPTION, HELP_OPTION,
            HELP_SHORT_OPTION, DEBUG_OPTION);
    private static final List<String> coreCommands = Arrays.asList(
            BUILD_COMMAND, RUN_COMMAND, TEST_COMMAND, DOC_COMMAND, PACK_COMMAND);
    private static final List<String> packageCommands = Arrays.asList(NEW_COMMAND, ADD_COMMAND, PULL_COMMAND,
            PUSH_COMMAND, SEARCH_COMMAND, SEMVER_COMMAND, GRAPH_COMMAND, DEPRECATE_COMMAND);
    private static final List<String> otherCommands = Arrays.asList(CLEAN_COMMAND, FORMAT_COMMAND, GRPC_COMMAND,
            GRAPHQL_COMMAND, OPENAPI_COMMAND, ASYNCAPI_COMMAND, PERSIST_COMMAND, BINDGEN_COMMAND, SHELL_COMMAND,
            VERSION_COMMAND);
    private static final List<String> hiddenCommands = Arrays.asList(INIT_COMMAND, TOOL_COMMAND, DIST_COMMAND,
            UPDATE_COMMAND, START_LANG_SERVER_COMMAND, START_DEBUG_ADAPTER_COMMAND, HELP_COMMAND, HOME_COMMAND,
            GENCACHE_COMMAND);

    public static boolean isToolCommand(String commandName) {
        // TODO: if openapi was to be pushed as a tool, here it will be ignored and openapi in distribution will be used
        //  instead. Need to look into possible solutions.
        return Stream.of(options, coreCommands, packageCommands, otherCommands, hiddenCommands)
                .flatMap(List::stream).noneMatch(commandName::equals);
    }

    public static URLClassLoader getCustomToolClassLoader(String commandName) {
        List<File> jars = getToolCommandJarAndDependenciesPath(commandName);
        URL[] urls = jars.stream()
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
        return new URLClassLoader(urls, systemClassLoader);
    }

    private static List<File> getToolCommandJarAndDependenciesPath(String commandName) {
        Path userHomeDirPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME);

        String distSpecificToolsTomlName = DIST_TOOL_TOML_PREFIX + RepoUtils.getBallerinaShortVersion() + TOML_EXT;
        Path distSpecificToolsTomlPath = userHomeDirPath.resolve(Path.of(CONFIG_DIR, distSpecificToolsTomlName));

        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
        Path distributionBalaDirPath = Path.of(System.getProperty(BALLERINA_HOME), "repo", BALA_DIR_NAME);

        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest =
                DistSpecificToolsManifestBuilder.from(distSpecificToolsToml).build();

        if (distSpecificToolsManifest.tools().containsKey(commandName)) {
            Optional<DistSpecificToolsManifest.Tool> tool =
                    distSpecificToolsManifest.tools().get(commandName).values().stream()
                            .filter(DistSpecificToolsManifest.Tool::active)
                            .findFirst();
            if (tool.isPresent()) {
                Path toolDirPath = Path.of(
                        tool.get().org(), tool.get().name(), tool.get().version(), ANY_PLATFORM, TOOL);
                if (distributionBalaDirPath.resolve(toolDirPath).resolve(BAL_TOOL_TOML).toFile().isFile())  {
                    return findJarFiles(distributionBalaDirPath.resolve(toolDirPath).resolve(LIBS).toFile());
                }
                return findJarFiles(centralBalaDirPath.resolve(toolDirPath).resolve(LIBS).toFile());
            }
            return Collections.emptyList();
        }
        throw LauncherUtils.createUsageExceptionWithHelp("unknown command '" + commandName + "'");
    }

    private static List<File> findJarFiles(File directory) {
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

    public static void updateDistSpecificToolsToml() {
        String distSpecificToolsTomlName = DIST_TOOL_TOML_PREFIX + RepoUtils.getBallerinaShortVersion()
                + TOML_EXT;
        Path distSpecificToolsTomlPath = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR,
                distSpecificToolsTomlName);
        if (distSpecificToolsTomlPath.toFile().isFile()) {
            return;
        }

        // TODO: check if the distPackedToolsTomlPath name is correct
        Path distPackedToolsTomlPath = Path.of(System.getProperty(BALLERINA_HOME), BAL_TOOLS_TOML);
        Path globalToolsTomlPath = Path.of(
                System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME, CONFIG_DIR, BAL_TOOLS_TOML);

        BalToolsToml distPackedToolsToml = BalToolsToml.from(distPackedToolsTomlPath);
        DistPackedToolsManifest distPackedToolsManifest =
                DistPackedToolsManifestBuilder.from(distPackedToolsToml)
                        .build();

        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        DistSpecificToolsManifest distSpecificToolsManifest =
                DistSpecificToolsManifestBuilder.from(distSpecificToolsToml)
                        .build();

        BalToolsToml globalToolsToml = BalToolsToml.from(globalToolsTomlPath);
        GlobalToolsManifest globalToolsManifest = GlobalToolsManifestBuilder.from(globalToolsToml).build();

        updateDistSpecifcManifestFromDistPackedManifest(distSpecificToolsManifest, distPackedToolsManifest);
        updateGlobalManifestFromDistPackedManifest(globalToolsManifest, distPackedToolsManifest);
        updateDistSpecificManifestFromGlobalManifest(distSpecificToolsManifest, globalToolsManifest);
        setLatestVersionToActive(distSpecificToolsManifest);

        distSpecificToolsToml.modify(distSpecificToolsManifest);
        globalToolsToml.modify(globalToolsManifest);
    }

    private static void updateDistSpecifcManifestFromDistPackedManifest(
            DistSpecificToolsManifest distSpecificToolsManifest, DistPackedToolsManifest distPackedToolsManifest) {
        distSpecificToolsManifest.tools().putAll(distPackedToolsManifest
                .tools().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, innerEntry -> {
                            DistPackedToolsManifest.Tool packedTool = innerEntry.getValue();
                            return new DistSpecificToolsManifest.Tool(
                                    packedTool.id(),
                                    packedTool.org(),
                                    packedTool.name(),
                                    packedTool.version(),
                                    false);
                        })))));
    }

    private static void updateGlobalManifestFromDistPackedManifest(
            GlobalToolsManifest globalToolsManifest, DistPackedToolsManifest distPackedToolsManifest) {
        distPackedToolsManifest.tools().forEach((id, toolVersions) -> {
            if (!globalToolsManifest.tools().containsKey(id)) {
                if (toolVersions.size() > 0) {
                    DistPackedToolsManifest.Tool tool = toolVersions.values().iterator().next();
                    globalToolsManifest.addTool(id, tool.org(), tool.name());
                }
            }
        });
    }

    private static void updateDistSpecificManifestFromGlobalManifest(
            DistSpecificToolsManifest distSpecificToolsManifest, GlobalToolsManifest globalToolsManifest) {
        globalToolsManifest.tools().forEach((id, tool) -> {
            Path toolCacheDir = ProjectUtils.createAndGetHomeReposPath()
                    .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                    .resolve(ProjectConstants.BALA_DIR_NAME).resolve(tool.org()).resolve(tool.name());
            if (toolCacheDir.toFile().isDirectory()) {
                try (Stream<Path> versions = Files.list(toolCacheDir)) {
                    versions.forEach(versionPath -> {
                        try {
                            String version = versionPath.getFileName().toString();
                            SemanticVersion.from(version);
                            if (isToolDistVersionCompatibleWithLocalDistVersion(versionPath)) {
                                GlobalToolsManifest.Tool globTool = globalToolsManifest.tools().get(id);
                                distSpecificToolsManifest.addTool(id, globTool.org(), globTool.name(), version, false);
                            }
                        } catch (ProjectException ignore) {
                        }
                    });
                } catch (IOException e) {
                    throw LauncherUtils.createUsageExceptionWithHelp(
                            "problem adding tools to the new distribution: " + e.getMessage());
                }
            }
            // if there is no matching version, we do not add anything to dist-specific tool.
            // No match is because the tool is not compatible with the current distribution version or
            // the tool is distribution specific or a specific version of the tool has been deleted
        });
    }

    private static void setLatestVersionToActive(DistSpecificToolsManifest distSpecificToolsManifest) {
        distSpecificToolsManifest.tools().forEach((toolId, toolVersions)-> {
            Optional<String> latestVersion = toolVersions.values().stream()
                    .map(tool -> SemanticVersion.from(tool.version()))
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

            latestVersion.ifPresent(v -> distSpecificToolsManifest.setActiveToolVersion(toolId, v));
        });
    }

    private static boolean isToolDistVersionCompatibleWithLocalDistVersion(Path toolCacheVersionDir) {
        Path balaPath = toolCacheVersionDir.resolve(ANY_PLATFORM);
        if (Files.exists(balaPath)) {
            PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
            SemanticVersion toolDistVersion = SemanticVersion.from(packageJson.getBallerinaVersion());
            SemanticVersion localDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
            return localDistVersion.greaterThanOrEqualTo(toolDistVersion);
        }
        return false;
    }
}
