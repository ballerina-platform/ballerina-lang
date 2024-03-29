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
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

import static io.ballerina.cli.cmd.Constants.ADD_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.guessModuleName;

/**
 * This class represents the "bal add" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = ADD_COMMAND, description = "Add a new Ballerina module to the current package")
public class AddCommand implements BLauncherCmd {

    private final Path userDir;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;
    private static final String TEST_FILE_SUFFIX = "_test" + ProjectConstants.BLANG_SOURCE_EXT;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "lib";

    public AddCommand() {
        this.userDir = Paths.get(System.getProperty("user.dir"));
        this.errStream = System.err;
        this.exitWhenFinish = true;
        CommandUtil.initJarFs();
    }

    public AddCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        CommandUtil.initJarFs();
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(ADD_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }
        // Check if inside a project repo
        Path projectPath = ProjectUtils.findProjectRoot(userDir);
        if (null == projectPath) {
            CommandUtil.printError(errStream,
                    "not a Ballerina project (or any parent up to mount point)\n" +
                            "You should run this command inside a Ballerina project.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if an argument is provided
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "module name is not provided.",
                    "bal add <module-name> [-t|--template <template-name>]",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if more than one argument is provided
        if (1 != argList.size()) {
            CommandUtil.printError(errStream,
                    "too many arguments",
                    "bal add <module-name>",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = ProjectUtils.validateModuleName(moduleName);
        if (!matches) {
            CommandUtil.printError(errStream,
                    "invalid module name : '" + moduleName + "' :\n" +
                            "Module name can only contain alphanumerics, underscores and periods.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!ProjectUtils.validateNameLength(moduleName)) {
            CommandUtil.printError(errStream,
                                   "invalid module name : '" + moduleName + "' :\n" +
                                           "Maximum length of module name is 256 characters.",
                                   null,
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!ProjectUtils.validateUnderscoresOfName(moduleName)) {
            CommandUtil.printError(errStream,
                                   "invalid module name : '" + moduleName + "' :\n" +
                                           ProjectUtils.getValidateUnderscoreError(moduleName, "Module"),
                                   null,
                                   false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!ProjectUtils.validateInitialNumericsOfName(moduleName)) {
            CommandUtil.printError(errStream,
                    "invalid module name : '" + moduleName + "' :\n" +
                            "Module name cannot have initial numeric characters.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the module already exists
        if (ProjectUtils.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "a module already exists with the given name : '" + moduleName + "' :\n" +
                            "Existing module path "
                            + projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check if the template exists
        if (!(template.equalsIgnoreCase("service") || template.equalsIgnoreCase("lib"))) {
            CommandUtil.printError(errStream,
                    "unsupported template provided. run 'bal add --help' to see available templates.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        try {
            Path moduleDirPath = projectPath.resolve(ProjectConstants.MODULES_ROOT);
            if (!Files.exists(moduleDirPath)) {
                Files.createDirectory(moduleDirPath);
            }
            createModule(projectPath, moduleName, template);
        } catch (AccessDeniedException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating module : " + "Insufficient Permission" + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } catch (IOException | URISyntaxException e) {
            CommandUtil.printError(errStream,
                    "error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        errStream.println("Added new Ballerina module at " + userDir.relativize(projectPath
                .resolve(ProjectConstants.MODULES_ROOT)
                .resolve(moduleName)));
    }

    @Override
    public String getName() {
        return ADD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(ADD_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal add <module-name> [-t|--template <template-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void createModule(Path projectPath, String moduleName, String template)
            throws IOException, URISyntaxException {
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
        Files.createDirectories(modulePath);
        // We will be creating following in the module directory
        // - modules/
        // -- mymodule/
        // --- main.bal       <- Contains default main method.
        CommandUtil.applyTemplate(modulePath, template, false);
        modifyTestFileName(projectPath, moduleName, template);
    }

    /**
     * Modify the file names to have the module name in them.
     *
     * @param projectPath project path
     * @param moduleName  module name
     * @param template   template
     * @throws IOException if an error occurs
     */
    private void modifyTestFileName(Path projectPath, String moduleName, String template) throws IOException {
        String validModuleName = guessModuleName(moduleName);
        String templateLowerCase = template.toLowerCase(Locale.getDefault());
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
        Path source = modulePath.resolve(templateLowerCase + ProjectConstants.BLANG_SOURCE_EXT);
        Files.move(source,
                source.resolveSibling(validModuleName + ProjectConstants.BLANG_SOURCE_EXT),
                StandardCopyOption.REPLACE_EXISTING);
        Path testSource = modulePath.resolve(ProjectConstants.TEST_DIR_NAME)
                .resolve(templateLowerCase + TEST_FILE_SUFFIX);
        Files.move(testSource,
                testSource.resolveSibling(validModuleName + TEST_FILE_SUFFIX),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
