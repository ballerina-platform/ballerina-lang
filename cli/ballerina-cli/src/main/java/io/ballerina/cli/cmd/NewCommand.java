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
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.cli.cmd.CommandUtil.applyBalaTemplate;
import static io.ballerina.cli.cmd.CommandUtil.applyBalaTemplateForCentralPackages;
import static io.ballerina.cli.cmd.CommandUtil.findBalaTemplate;
import static io.ballerina.cli.cmd.CommandUtil.pullPackageFromCentral;
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
    private boolean exitWhenFinish;
    private static Path homeCache;

    @CommandLine.Parameters
    public List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"}, description = "Acceptable values: [main, service, lib] " +
            "default: default")
    private String template = "default";

    public NewCommand() {
        this.userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.errStream = System.err;
        this.exitWhenFinish = true;
        CommandUtil.initJarFs();
    }

    public NewCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
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
                    "project name is not provided.",
                    "bal new <project-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        // Check if one argument is given and not more than one argument.
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments",
                    "bal new <project-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // If the current directory is a ballerina project, fail the command.
        if (ProjectUtils.isBallerinaProject(this.userDir)) {
            CommandUtil.printError(errStream,
                    "directory is already a Ballerina project.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        String packageName = argList.get(0);
        Path path = userDir.resolve(packageName);
        // Check if the directory or file exists with the given project name
        if (Files.exists(path)) {
            CommandUtil.printError(errStream,
                    "destination '" + path.toString() + "' already exists.",
                    "bal new <project-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectUtils.findProjectRoot(path);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
                    "directory is already within a Ballerina project :" +
                            projectRoot.resolve(ProjectConstants.BALLERINA_TOML).toString(),
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

        // Apply suitable template
        try {
            homeCache = RepoUtils.createAndGetHomeReposPath();
            Path balaCache = homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                    .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                    .resolve(ProjectConstants.BALA_DIR_NAME);
            Path balaCachePkgPath = (Path) findBalaTemplate(template);
            Files.createDirectories(path);
            // check if the template matches with one of the inbuilt template types
            if (!CommandUtil.getTemplates().contains(template)) {
                // Check if the package is available in local bala cache
                if (balaCachePkgPath != null) {
                    // Pkg is available in the local cache
                    applyBalaTemplate(path, balaCache, template);
                } else {
                    // Pull pkg from central
                    pullPackageFromCentral(balaCache, path, template);
                    applyBalaTemplateForCentralPackages(path, balaCache, template);
                }
            } else {
                // create package with inbuilt template
                CommandUtil.initPackageByTemplate(path, packageName, template);
            }
        } catch (AccessDeniedException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating project : " + "Insufficient Permission : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (CentralClientException e) {
            CommandUtil.printError(errStream,
                    "error occurred while pulling the package : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (IOException | URISyntaxException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating project : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
        }
        errStream.println("Created new Ballerina package '" + guessPkgName(packageName)
                + "' at " + userDir.relativize(path) + ".");
        return;
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
