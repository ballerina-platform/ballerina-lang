/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.launcher;

import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.runtime.api.values.BError;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_DEFAULT_DIRNAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {
    private static final String TOOL = "tool";
    public static final String LIBS = "libs";

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

    public static Path getSourceRootPath(String sourceRoot) {
        // Get source root path.
        Path sourceRootPath;
        if (sourceRoot == null || sourceRoot.isEmpty()) {
            sourceRootPath = Paths.get(System.getProperty("user.dir"));
        } else {
            try {
                sourceRootPath = Paths.get(sourceRoot).toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new RuntimeException("error reading from directory: " + sourceRoot + " reason: " +
                        e.getMessage(), e);
            }

            if (!Files.isDirectory(sourceRootPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("source root must be a directory");
            }
        }
        return sourceRootPath;
    }


    public static BLauncherException createUsageExceptionWithHelp(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'bal help' for usage.");
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorMsg);
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorPrefix, Throwable cause) {
        String message;
        if (cause instanceof BError) {
            message = ((BError) cause).getPrintableStackTrace();
        } else {
            message = cause.toString();
        }
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorPrefix + message);
        return launcherException;
    }

    static void printLauncherException(BLauncherException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }

    static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char[] c = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    static boolean isToolCommand(String commandName) {
        // TODO: if openapi was to be pushed as a tool, here it will be ignored and openapi in distribution will be used
        //  instead. Need to look into possible solutions.
        return Stream.of(options, coreCommands, packageCommands, otherCommands, hiddenCommands)
                .flatMap(List::stream).noneMatch(commandName::equals);
    }

    static List<File> getToolCommandJarAndDependenciesPath(String commandName) {
        Path userHomeDirPath = Path.of(System.getProperty(CommandUtil.USER_HOME), HOME_REPO_DEFAULT_DIRNAME);

        String distSpecificToolsTomlName = DIST_TOOL_TOML_PREFIX + RepoUtils.getBallerinaShortVersion() + TOML_EXT;
        Path distSpecificToolsTomlPath = userHomeDirPath.resolve(Path.of(CONFIG_DIR, distSpecificToolsTomlName));

        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
        BalToolsToml distSpecificToolsToml = BalToolsToml.from(distSpecificToolsTomlPath);
        BalToolsManifest distSpecificToolsManifest = BalToolsManifestBuilder.from(distSpecificToolsToml).build();

        if (distSpecificToolsManifest.tools().containsKey(commandName)) {
            BalToolsManifest.Tool tool = distSpecificToolsManifest.tools().get(commandName);
            File libsDir = centralBalaDirPath.resolve(
                    Path.of(tool.org(), tool.name(), tool.version(), ANY_PLATFORM, TOOL, LIBS)).toFile();
            return findJarFiles(libsDir);
        }
        throw LauncherUtils.createUsageExceptionWithHelp("unknown command '" + commandName + "'");
    }

    static List<File> findJarFiles(File directory) {
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
}
