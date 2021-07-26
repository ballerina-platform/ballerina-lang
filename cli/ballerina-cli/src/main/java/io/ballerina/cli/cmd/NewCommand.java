/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.NEW_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.guessPkgName;

/**
 * New command for creating a ballerina project.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = NEW_COMMAND, description = "Create a new Ballerina project")
public class NewCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"}, description = "Acceptable values: [main, service, lib] " +
            "default: main")
    private String template = "main";

    public NewCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        errStream = System.err;
        CommandUtil.initJarFs();
    }

    public NewCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
        this.errStream = errStream;
        CommandUtil.initJarFs();
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
                    "bal new <project-name>",
                    true);
            return;
        }
        // Check if one argument is given and not more than one argument.
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "bal new <project-name>",
                    true);
            return;
        }

        // If the current directory is a ballerina project, fail the command.
        if (ProjectUtils.isBallerinaProject(this.userDir)) {
            CommandUtil.printError(errStream,
                    "Directory is already a Ballerina project",
                    null,
                    false);
            return;
        }

        String packageName = argList.get(0);
        Path path = userDir.resolve(packageName);
        // Check if the directory or file exists with the given project name
        if (Files.exists(path)) {
            CommandUtil.printError(errStream,
                    "destination '" + path.toString() + "' already exists",
                    "bal new <project-name>",
                    true);
            return;
        }

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectUtils.findProjectRoot(path);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
                    "Directory is already within a Ballerina project :" +
                            projectRoot.resolve(ProjectConstants.BALLERINA_TOML).toString(),
                    null,
                    false);
            return;
        }

        if (!ProjectUtils.validatePackageName(packageName)) {
            errStream.println("Unallowed characters in the project name were replaced by " +
                    "underscores when deriving the package name. Edit the Ballerina.toml to change it.");
            errStream.println();
        }

        // Check if the template exists
        if (!CommandUtil.getTemplates().contains(template)) {
            CommandUtil.printError(errStream,
                    "Template not found, use `bal new --help` to view available templates.",
                    null,
                    false);
            return;
        }

        try {
            Files.createDirectories(path);
            CommandUtil.initPackageByTemplate(path, packageName, template);
        } catch (AccessDeniedException e) {
            errStream.println("error: Error occurred while creating project : " + "Insufficient Permission : " +
                    e.getMessage());
            return;
        } catch (IOException | URISyntaxException e) {
            errStream.println("error: Error occurred while creating project : " + e.getMessage());
            return;
        }
        errStream.println("Created new Ballerina package '" + guessPkgName(packageName)
                + "' at " + userDir.relativize(path) + ".");
    }

    @Override
    public String getName() {
        return NEW_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("create a new Ballerina project");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal new <project-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
