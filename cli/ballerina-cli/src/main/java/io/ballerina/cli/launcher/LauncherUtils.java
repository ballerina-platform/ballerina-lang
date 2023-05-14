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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.runtime.api.values.BError;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.ballerina.cli.cmd.Constants.ADD_COMMAND;
import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.cli.cmd.Constants.CLEAN_COMMAND;
import static io.ballerina.cli.cmd.Constants.DEPRECATE_COMMAND;
import static io.ballerina.cli.cmd.Constants.DIST_COMMAND;
import static io.ballerina.cli.cmd.Constants.DOC_COMMAND;
import static io.ballerina.cli.cmd.Constants.FORMAT_COMMAND;
import static io.ballerina.cli.cmd.Constants.GRAPH_COMMAND;
import static io.ballerina.cli.cmd.Constants.HELP_COMMAND;
import static io.ballerina.cli.cmd.Constants.HOME_COMMAND;
import static io.ballerina.cli.cmd.Constants.INIT_COMMAND;
import static io.ballerina.cli.cmd.Constants.NEW_COMMAND;
import static io.ballerina.cli.cmd.Constants.PACK_COMMAND;
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

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {

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

    public static <K extends Comparable<? super K>, V> Iterable<V> sortValuesByKeys(Map<K, V> map) {
        TreeMap<K, V> sortedMap = new TreeMap<>(map);
        return sortedMap.values();
    }

    static String wrapString(String str, int wrapLength, int indent) {
        StringBuilder wrappedStr = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (Character.isWhitespace(str.charAt(i))) {
                i++;
                continue;
            }
            if (i > 0) {
                wrappedStr.append("\n");
                wrappedStr.append(" ".repeat(indent));
            }
            int lineEnd = Math.min(i + wrapLength, str.length());
            if (lineEnd < str.length() && !Character.isWhitespace(str.charAt(lineEnd))) {
                // find the last whitespace character before the maximum line length
                int lastWhitespace = str.lastIndexOf(' ', lineEnd);
                if (lastWhitespace > i) {
                    lineEnd = lastWhitespace;
                }
            }
            wrappedStr.append(str, i, lineEnd);
            i = lineEnd;
            // skip any whitespace characters at the beginning of the next line
            while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
                i++;
            }
        }
        return wrappedStr.toString();
    }

    static String generateGeneralHelp(Map<String, CommandLine> subCommands) {
        List<String> coreCommands = Arrays.asList(
                BUILD_COMMAND, RUN_COMMAND, TEST_COMMAND, DOC_COMMAND, PACK_COMMAND);
        List<String> packageCommands = Arrays.asList(NEW_COMMAND, INIT_COMMAND, ADD_COMMAND, PULL_COMMAND,
                PUSH_COMMAND, SEARCH_COMMAND, SEMVER_COMMAND, GRAPH_COMMAND, DEPRECATE_COMMAND);
        List<String> otherCommands = Arrays.asList(CLEAN_COMMAND, FORMAT_COMMAND, SHELL_COMMAND,
                VERSION_COMMAND, TOOL_COMMAND);
        List<String> excludedCommands = Arrays.asList(
                START_LANG_SERVER_COMMAND, START_DEBUG_ADAPTER_COMMAND, HELP_COMMAND, HOME_COMMAND);
        List<String> updateCommands = Arrays.asList(DIST_COMMAND, UPDATE_COMMAND);

        StringBuilder helpBuilder = new StringBuilder();
        StringBuilder coreCmdsHelpBuilder = new StringBuilder("\n    Core Commands:\n");
        StringBuilder pkgCmdsHelpBuilder = new StringBuilder("\n    Package Commands:\n");
        StringBuilder updateCmdsHelpBuilder = new StringBuilder("\n    Update Commands:\n");
        StringBuilder toolCmdsHelpBuilder = new StringBuilder("\n    Tool Commands:\n");
        StringBuilder otherCmdHelpBuilder = new StringBuilder("\n   Other Commands:\n");

        helpBuilder.append(BLauncherCmd.getCommandUsageInfo(HELP_COMMAND));

        for (CommandLine cmd : LauncherUtils.sortValuesByKeys(subCommands)) {
            String cmdName = cmd.getCommandName();
            if (coreCommands.contains(cmdName)) {
                LauncherUtils.generateCommandDescription(cmd, coreCmdsHelpBuilder);
            } else if (packageCommands.contains(cmdName)) {
                LauncherUtils.generateCommandDescription(cmd, pkgCmdsHelpBuilder);
            } else if (updateCommands.contains(cmdName)) {
                LauncherUtils.generateCommandDescription(cmd, updateCmdsHelpBuilder);
            } else if (otherCommands.contains(cmdName)) {
                LauncherUtils.generateCommandDescription(cmd, otherCmdHelpBuilder);
            } else if (excludedCommands.contains(cmdName)) {
                // do nothing
            } else {
                LauncherUtils.generateCommandDescription(cmd, toolCmdsHelpBuilder);
            }
        }
        helpBuilder.append(coreCmdsHelpBuilder);
        helpBuilder.append(pkgCmdsHelpBuilder);
        helpBuilder.append(toolCmdsHelpBuilder);
        helpBuilder.append(otherCmdHelpBuilder);
        helpBuilder.append(updateCmdsHelpBuilder);
        helpBuilder.append("\nUse 'bal help <command>' for more information on a specific command.");
        return helpBuilder.toString();
    }

    private static void generateCommandDescription(CommandLine command, StringBuilder stringBuilder) {
        String commandName = command.getCommandName();
        BLauncherCmd bCmd = (BLauncherCmd) command.getCommandSpec().userObject();
        CommandLine.Command annotation = bCmd.getClass().getAnnotation(CommandLine.Command.class);
        String commandDescription = "";
        if (annotation != null) {
            String[] descValues = annotation.description();
            if (descValues != null && descValues.length > 0) {
                commandDescription = wrapString(descValues[0], 60, 29);
            }
        }
        stringBuilder.append("\t").append(String.format("%-20s %s", commandName, commandDescription)).append("\n");
    }
}
