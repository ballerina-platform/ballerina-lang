/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.cmd.model.CommandCompletions;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.launcher.util.BCompileUtil;
import io.ballerina.projects.util.ProjectConstants;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.cli.cmd.Constants.COMPGEN_COMMAND;

/**
 * This class represents the "bal completion bash-dist" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = COMPGEN_COMMAND, description = "Generate bash completion script")
public class CompgenCommand implements BLauncherCmd {
    private Path userDir;
    private PrintStream outStream;

    public CompgenCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        outStream = System.out;
    }

    public CompgenCommand(Path userDir, PrintStream outStream) {
        this.userDir = userDir;
        this.outStream = outStream;
    }

    @CommandLine.Parameters(description = "Command name")
    private List<String> bashDistCommands;

    @Override
    public void execute() {
        List<String> subCommands;
        List<String> dirContents;
        if (bashDistCommands == null) {
            subCommands = getSubCommands("bal");
            printSuggestionList(subCommands, outStream);
        } else if (bashDistCommands.size() == 1) {
            subCommands = getSubCommands("bal");
            if (bashDistCommands.get(0).equals("dist")) {
                subCommands = getSubCommands(bashDistCommands.get(0));
                printSuggestionList(subCommands, outStream);
            } else if (subCommands.contains(bashDistCommands.get(0))) {
                printSuggestionList(getDirContents(this.userDir), outStream);
                return;
            }
            subCommands = subCommands.stream().filter(i -> i.startsWith(bashDistCommands.get(0))).
                    collect(Collectors.toList());
            printSuggestionList(subCommands, outStream);
        } else {
            subCommands = getSubCommands(bashDistCommands.get(0));
            int index = bashDistCommands.size() - 1;
            if (bashDistCommands.get(0).equals("dist")) {
                subCommands = subCommands.stream().filter(i -> i.startsWith(bashDistCommands.get(index))).
                        collect(Collectors.toList());
                printSuggestionList(subCommands, outStream);
            } else if (subCommands.contains(bashDistCommands.get(index))) {
                printSuggestionList(getDirContents(this.userDir), outStream);
            } else if (bashDistCommands.get(index).startsWith("-") || bashDistCommands.get(index).startsWith("--")) {
                subCommands = subCommands.stream().filter(i -> i.startsWith(bashDistCommands.get(index))).
                        collect(Collectors.toList());
                printSuggestionList(subCommands, outStream);
            } else {
                dirContents = getDirContents(this.userDir);
                dirContents = dirContents.stream().filter(i -> i.startsWith(bashDistCommands.get(index))).
                        collect(Collectors.toList());
                printSuggestionList(dirContents, outStream);
            }
        }
    }

    @Override
    public String getName() {
        return COMPGEN_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * Retrieve command completion info.
     *
     * @return command completion info
     */
    private static String getCommandInfo() {
        String filePath = "bash-completion/command_completion.json";
        try {
            return BCompileUtil.readFileAsString(filePath);
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("completion file not found: " + filePath);
        }
    }

    /**
     * Retrieve sub commands for given command.
     *
     * @return sub commands for given command
     */
    private static List<String> getSubCommands(String commandName) {
        List<String> subCommands = new ArrayList<>();
        subCommands.add("-h");
        subCommands.add("--help");
        Gson gson = new Gson();
        List<CommandCompletions> commands = gson.fromJson(getCommandInfo(),
                new TypeToken<List<CommandCompletions>>() { }.getType());
        for (CommandCompletions command : commands) {
            if (!command.getCommand().equals(commandName)) {
                continue;
            }
            subCommands.addAll(command.getSubCommands());
        }
        return subCommands;
    }

    /**
     * Retrieve contents of the current directory.
     *
     * @return contents of the current directory
     */
    private static List<String> getDirContents(Path path) {
        List<String> dirContents = new ArrayList<>();
        File userDir = path.toFile();
        File[] filesList = userDir.listFiles();
        if (filesList != null) {
            for (File file: filesList) {
                if (file.isDirectory()) {
                    dirContents.add(file.getName() + "/");
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".bal")) {
                        dirContents.add(file.getName());
                    }
                }
            }
        }
        return dirContents;
    }

    /**
     * Print suggestion list.
     */
    private static void printSuggestionList(List<String> list, PrintStream outStream) {
        list.forEach(outStream::println);
    }
}
