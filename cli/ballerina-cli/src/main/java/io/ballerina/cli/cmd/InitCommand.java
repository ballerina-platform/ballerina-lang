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

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--template", "-t"}, description = "Acceptable values: [main, service, lib]")
    private String template = "";

    public InitCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        errStream = System.err;
        CommandUtil.initJarFs();
    }

    public InitCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
        this.errStream = errStream;
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
                    "Directory is already a Ballerina project",
                    null,
                    false);
            return;
        }

        // Check if one argument is given and not more than one argument.
        if (argList != null && !(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "bal init <project-name>",
                    true);
            return;
        }

        // Check if there is a ballerina project in sub level.

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectUtils.findProjectRoot(this.userDir);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
                    "Directory is already within a Ballerina project :" +
                            projectRoot.resolve(ProjectConstants.BALLERINA_TOML).toString(),
                    null,
                    false);
            return;
        }

        // Check if the template exists
        if (!template.equals("") && !CommandUtil.getTemplates().contains(template)) {
            CommandUtil.printError(errStream,
                    "Template not found, use `bal init --help` to view available templates.",
                    null,
                    false);
            return;
        }

        String packageName = Optional.of(this.userDir.getFileName()).get().toString();
        if (argList != null && argList.size() > 0) {
            packageName = argList.get(0);
            if (!ProjectUtils.validatePackageName(packageName)) {
                CommandUtil.printError(errStream,
                        "Invalid package name : '" + packageName + "' :\n" +
                                "Package name can only contain alphanumerics and underscores" +
                                "and the maximum length is 256 characters",
                        null,
                        false);
                return;
            }
        }

        if (!ProjectUtils.validatePackageName(packageName)) {
            errStream.println("Unallowed characters in the project name were replaced by " +
                    "underscores when deriving the package name. Edit the Ballerina.toml to change it.");
            errStream.println();
        }

        try {
            if (template.equals("")) {
                CommandUtil.initPackage(userDir);
            } else {
                CommandUtil.initPackageByTemplate(userDir, packageName, template);
            }
        } catch (AccessDeniedException e) {
            errStream.println("error: Error occurred while initializing project : " + " Access Denied : " +
                    e.getMessage());
            return;
        } catch (IOException | URISyntaxException e) {
            errStream.println("error: Error occurred while initializing project : " + e.getMessage());
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
        out.append("initialize a Ballerina project in current directory");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal init \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
