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
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.INIT_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.guessPkgName;


/**
 * Init command for creating a ballerina project.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = INIT_COMMAND, description = "Create a Init Ballerina project")
public class InitCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private boolean exitWhenFinish;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters
    private List<String> argList;

    public InitCommand() {
        this.userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.errStream = System.err;
        this.exitWhenFinish = true;
        CommandUtil.initJarFs();
    }

    public InitCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        CommandUtil.initJarFs();
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(INIT_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        // If the current directory is a ballerina project ignore.
        if (ProjectUtils.isBallerinaProject(this.userDir)) {
            CommandUtil.printError(errStream,
                    "directory is already a Ballerina project.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if one argument is given and not more than one argument.
        if (argList != null && !(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments",
                    "bal init <project-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if there is a ballerina project in sub level.

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectUtils.findProjectRoot(this.userDir);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
                    "directory is already within a Ballerina project :" +
                            projectRoot.resolve(ProjectConstants.BALLERINA_TOML).toString(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String packageName = Optional.of(this.userDir.getFileName()).get().toString();
        if (argList != null && !argList.isEmpty()) {
            packageName = argList.get(0);
            if (!ProjectUtils.validatePackageName(packageName)) {
                CommandUtil.printError(errStream,
                        "invalid package name : '" + packageName + "' :\n" +
                                "Package name can only contain alphanumerics and underscores.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        if (!ProjectUtils.validateNameLength(packageName)) {
            CommandUtil.printError(errStream,
                                   "invalid package name : '" + packageName + "' :\n" +
                                           "Maximum length of package name is 256 characters.",
                                   null,
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!ProjectUtils.validatePackageName(packageName)) {
            errStream.println("unallowed characters in the project name were replaced by " +
                    "underscores when deriving the package name. Edit the Ballerina.toml to change it.");
            errStream.println();
        }

        try {
            CommandUtil.initPackage(userDir);
        } catch (AccessDeniedException e) {
            CommandUtil.printError(errStream,
                    "error occurred while initializing project : " + " Access Denied : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "error occurred while initializing project : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        errStream.println("Created new Ballerina package '" + guessPkgName(packageName) + "'.");
    }

    @Override
    public String getName() {
        return INIT_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Initialize a Ballerina project in current directory");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal init \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
