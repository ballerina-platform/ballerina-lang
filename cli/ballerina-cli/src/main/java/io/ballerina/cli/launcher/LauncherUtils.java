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
import io.ballerina.cli.launcher.util.BalToolsUtil;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.runtime.api.values.BError;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.cli.launcher.BallerinaCliCommands.HELP;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;

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
        if (cause instanceof BError bError) {
            message = bError.getPrintableStackTrace();
        } else {
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorPrefix + message);
        return launcherException;
    }


    public static String prepareCompilerErrorMessage(String message) {
        return "error: " + LauncherUtils.makeFirstLetterLowerCase(message);
    }

    public static void printLauncherException(BLauncherException e, PrintStream outStream) {
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

    static String generateGeneralHelp(Map<String, CommandLine> subCommands) {
        StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append(BLauncherCmd.getCommandUsageInfo(HELP));

        Path balToolsTomlPath = RepoUtils.createAndGetHomeReposPath().resolve(CONFIG_DIR).resolve(BAL_TOOLS_TOML);
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Map<String, String> activeToolsVsRepos = new HashMap<>();

        // if there are any tools, add Tool Commands section
        List<String> toolNames = subCommands.keySet().stream()
                .filter(BalToolsUtil::isNonBuiltInToolCommand)
                .sorted().toList();

        if (!toolNames.isEmpty()) {
            toolNames.forEach(toolName ->
                balToolsManifest.getActiveTool(toolName).ifPresent(tool ->
                    activeToolsVsRepos.put(toolName, tool.repository() == null ? "" : "[" + tool.repository()
                            .toUpperCase() + "] ")));
            helpBuilder.append("\n\n   Tool Commands:");
            toolNames.forEach(key -> generateCommandDescription(subCommands.get(key), helpBuilder,
                    activeToolsVsRepos.get(key)));
        }
        return helpBuilder.toString();
    }

    static String generateCommandHelp(String commandName, Map<String, CommandLine> subCommands) {
        if (!BalToolsUtil.isNonBuiltInToolCommand(commandName)) {
            return BLauncherCmd.getCommandUsageInfo(commandName);
        }
        StringBuilder commandUsageInfo = new StringBuilder();
        BLauncherCmd cmd = subCommands.get(commandName).getCommand();
        cmd.printLongDesc(commandUsageInfo);
        return commandUsageInfo.toString();
    }

    private static void generateCommandDescription(CommandLine command, StringBuilder stringBuilder,
                                                   String repository) {
        String commandName = command.getCommandName();
        BLauncherCmd bLauncherCmd = (BLauncherCmd) command.getCommandSpec().userObject();
        CommandLine.Command annotation = bLauncherCmd.getClass().getAnnotation(CommandLine.Command.class);
        String commandDescription = "";
        if (annotation != null) {
            String[] descValues = annotation.description();
            if (descValues != null && descValues.length > 0) {
                // wrapLength, indent selected to match `ballerina-help.help` formatting
                commandDescription = wrapString(descValues[0], 64, 24);
            }
        }
        stringBuilder.append("\n")
                .append("        ")
                .append(String.format("%-15s %s", commandName, repository + commandDescription));
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
}
