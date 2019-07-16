/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;


import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.NEW_COMMAND;

/**
 * New command for creating a ballerina project.
 *
 * @since 0.992.0
 */
@CommandLine.Command(name = NEW_COMMAND, description = "Create a new Ballerina project")
public class NewCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public NewCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
    }

    public NewCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
        this.errStream = errStream;
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(NEW_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }
        // Check if the project name is given
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "The following required arguments were not provided:\n" +
                         "    <project-name>",
                    "ballerina new <project-name>",
                    true);
            return;
        }
        // Check if one argument is given and not more than one argument.
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina new <project-name>",
                    true);
            return;
        }

        Path path = userDir.resolve(argList.get(0));
        // Check if the directory or file exists with the given project name
        if (Files.exists(path)) {
            CommandUtil.printError(errStream,
                    "destination '" + path.toString() + "' already exists",
                    "ballerina new <project-name>",
                    true);
            return;
        }

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectDirs.findProjectRoot(path);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
            "Directory is already within a ballerina project :" + projectRoot.toString(),
                    null,
                    false);
            return;
        }

        try {
            Files.createDirectories(path);
            CommandUtil.initProject(path);
        } catch (AccessDeniedException e) {
            errStream.println("error: Error occurred while creating project : " + "Insufficient Permission");
        } catch (IOException e) {
            errStream.println("error: Error occurred while creating project : " + e.getMessage());
            return;
        }
        errStream.println("Created new ballerina project at " + userDir.relativize(path));
        errStream.println();
        errStream.println("Next:");
        errStream.println("    Use `ballerina create` to create a ballerina module inside the project.");
    }

    @Override
    public String getName() {
        return NEW_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("create a new ballerina project");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina new <project-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
