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
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.cli.cmd.CommandUtil.DEFAULT_TEMPLATE;
import static io.ballerina.cli.cmd.CommandUtil.balFilesExists;
import static io.ballerina.cli.cmd.CommandUtil.checkPackageFilesExists;
import static io.ballerina.cli.cmd.CommandUtil.initPackageFromCentral;
import static io.ballerina.cli.cmd.Constants.NEW_COMMAND;

/**
 * New command for creating a ballerina project.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = NEW_COMMAND, description = "Create a new Ballerina package")
public class NewCommand implements BLauncherCmd {

    private PrintStream errStream;
    private boolean exitWhenFinish;
    Path homeCache = RepoUtils.createAndGetHomeReposPath();

    @CommandLine.Parameters
    public List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"}, description = "Acceptable values: [main, service, lib] " +
            "default: default")
    public String template = "default";

    public NewCommand() {
        this.errStream = System.err;
        this.exitWhenFinish = true;
        CommandUtil.initJarFs();
    }

    public NewCommand(PrintStream errStream, boolean exitWhenFinish) {
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        CommandUtil.initJarFs();
    }

    public NewCommand(PrintStream errStream, boolean exitWhenFinish, Path customHomeCache) {
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        CommandUtil.initJarFs();
        this.homeCache = customHomeCache;
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(NEW_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        // Check if the project path is given
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "project path is not provided.",
                    "bal new <project-path>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        // Check if one argument is given and not more than one argument.
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments",
                    "bal new <project-path>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        Path packagePath = Paths.get(argList.get(0));
        Path currentDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        if (!packagePath.isAbsolute()) {
            packagePath = Paths.get(currentDir.toString(), packagePath.toString()).normalize();
        }
        List<Path> filesInDir = new ArrayList<>();

        CommandUtil.setPrintStream(errStream);
        Path packageDirectory;
        String packageName;
        Optional<Path> optionalPackageName = Optional.ofNullable(packagePath.getFileName());
        if (optionalPackageName.isEmpty()) {
            CommandUtil.printError(errStream,
                    "package name could not be derived",
                    "bal new <project-path>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        packageName = optionalPackageName.get().toString();
        boolean balFilesExist = false;

        // Check if the given path is a valid path
        if (Files.exists((packagePath))) {
            // If the given path is a ballerina project, fail the command.
            if (ProjectUtils.isBallerinaProject(packagePath)) {
                CommandUtil.printError(errStream,
                        "directory is already a Ballerina project.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            if (FileUtils.checkBallerinaTomlInExistingDir(packagePath)) {
                CommandUtil.printError(errStream,
                        "directory already contains a Ballerina project",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            // Check if package contains files/directories other than .bal files exist.
            String packageFiles = checkPackageFilesExists(packagePath);
            if (!packageFiles.equals("") && !template.equals(DEFAULT_TEMPLATE)) {
                CommandUtil.printError(errStream,
                        "existing " + packageFiles.substring(0, packageFiles.length() - 2) +
                                " file/directory(s) were found. " +
                                "Please use a different directory or remove existing files.",
                        null,
                        false);
                CommandUtil.exitError(exitWhenFinish);
                return;
            }

            try {
                balFilesExist = balFilesExists(packagePath);
                if (balFilesExist && !template.equals(DEFAULT_TEMPLATE)) {
                    CommandUtil.printError(errStream,
                            "existing .bal files found. " +
                                    "Please use a different directory or remove existing files.",
                            null,
                            false);
                    CommandUtil.exitError(exitWhenFinish);
                    return;
                }
            } catch (IOException e) {
                CommandUtil.printError(errStream,
                        "error occurred while looking for existing package files: " + e.getMessage(),
                        null,
                        false);
                CommandUtil.exitError(exitWhenFinish);
                return;
            }

            packageDirectory = packagePath;
            filesInDir = FileUtils.getFilesInDirectory(packageDirectory);
        } else {
            Path parent = packagePath.getParent();
            if (parent == null) {
                CommandUtil.printError(errStream,
                        "destination '" + packagePath + "' does not exist.",
                        "bal new <project-path>",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            // Check if the parent directory path is a valid path
            if (!Files.exists(parent)) {
                CommandUtil.printError(errStream,
                        "destination '" + parent + "' does not exist.",
                        "bal new <project-path>",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            // If the parent directory is a ballerina project, fail the command.
            if (ProjectUtils.isBallerinaProject(parent)) {
                CommandUtil.printError(errStream,
                        "directory is already within the Ballerina project '" +
                                parent + "'",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Check if the command is executed inside a ballerina project
        Path projectRoot = ProjectUtils.findProjectRoot(packagePath);
        if (projectRoot != null) {
            CommandUtil.printError(errStream,
                    "directory is already within the Ballerina project '" +
                            projectRoot + "'",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
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
            packageName = ProjectUtils.guessPkgName(packageName, template);
            errStream.println("Package name is derived as '" + packageName
                    + "'. Edit the Ballerina.toml to change it.");
            errStream.println();
        }

        try {
            // check if the template matches with one of the inbuilt template types
            if (CommandUtil.getTemplates().contains(template)) {
                // create package with inbuilt template
                if (Files.exists((packagePath))) {
                    String existingFiles = CommandUtil.checkTemplateFilesExists(template, packagePath);
                    if (!existingFiles.equals("")) {
                        CommandUtil.printError(errStream,
                                "existing " + existingFiles.substring(0, existingFiles.length() - 2) +
                                        " file/directory(s) were found. " +
                                        "Please use a different directory or remove existing files.",
                                null,
                                false);
                        CommandUtil.exitError(exitWhenFinish);
                        return;
                    }
                }
                CommandUtil.initPackageByTemplate(packagePath, packageName, template, balFilesExist);
            } else {
                Path balaCache = homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME);
                initPackageFromCentral(balaCache, packagePath, packageName, template, filesInDir);
            }
        } catch (AccessDeniedException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating project : " + "Insufficient Permission : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
        } catch (BLauncherException e) {
            if (Files.exists(packagePath)) {
                try {
                    Files.delete(packagePath);
                } catch (IOException ignored) {
                }
            }
            CommandUtil.printError(errStream, e.getDetailedMessages().get(0),
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
        if (Files.exists(packagePath)) {
            // If the provided path argument is relative, print it as it is
            if (!Paths.get(argList.get(0)).isAbsolute()) {
                packagePath = Paths.get(argList.get(0));
            }
            errStream.println("Created new package '" + packageName + "' at " + packagePath + ".");
        }
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return NEW_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(NEW_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal new <project-path> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

}
