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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.packerina.cmd.Constants.CREATE_COMMAND;

/**
 * New command for creating a ballerina project.
 */
@CommandLine.Command(name = CREATE_COMMAND, description = "Create a new Ballerina project")
public class CreateCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private static FileSystem jarFs;
    private static Map<String, String> env;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "main";

    @CommandLine.Option(names = {"--list", "-l"})
    private boolean list = false;

    public CreateCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
        initJarFs();
    }

    public CreateCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
        this.errStream = errStream;
        initJarFs();
    }

    private void initJarFs() {
        URI uri = null;
        try {
            uri = CreateCommand.class.getClassLoader().getResource("create_cmd_templates").toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                if (null == jarFs) {
                    env = new HashMap<>();
                    jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CREATE_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (list) {
            errStream.println("Available templates:");
            for (String template: getTemplates()) {
                errStream.println("    - " + template);
            }
            return;
        }

        // Check if inside a project repo
        Path projectPath = ProjectDirs.findProjectRoot(userDir);
        if (null == projectPath) {
            CommandUtil.printError(errStream,
                    "not a ballerina project (or any parent up to mount point)\n" +
                            "You should run this command inside a ballerina project", null, false);
            return;
        }

        // Check if an argument is provided
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "The following required arguments were not provided:\n" +
                            "    <module-name>",
                    "ballerina create <module-name> [-t|--template <template-name>]",
                    true);
            return;
        }

        // Check if more then one argument is provided
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina create <project-name>",
                    true);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = RepoUtils.validatePkg(moduleName);
        if (!matches) {
            CommandUtil.printError(errStream,
                    "Invalid module name : '" + moduleName + "' :\n" +
                         "Module name can only contain alphanumerics, underscores and periods " +
                         "and the maximum length is 256 characters",
                    null,
                    false);
            return;
        }

        // Check if the module already exists
        if (ProjectDirs.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "A module already exists with the given name : '" + moduleName + "' :\n" +
                         "Existing module path "
                         + projectPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleName),
                    null,
                    false);
            return;
        }

        // Check if the template exists
        if (!getTemplates().contains(template)) {
            CommandUtil.printError(errStream,
                    "Template not found, use `ballerina create --list` to view available templates.",
                    null,
                    false);
            return;
        }

        try {
            createModule(projectPath, moduleName, template);
        } catch (ModuleCreateException e) {
            CommandUtil.printError(errStream,
                    "Error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            return;
        }

        errStream.println("Created new ballerina module at '" + userDir.relativize(projectPath
                .resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                .resolve(moduleName)) + "'");
    }


    @Override
    public String getName() {
        return CREATE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("create a new ballerina module");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina create <module-name> [-t|--template <template-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void createModule(Path projectPath, String moduleName, String template)
            throws ModuleCreateException {
        Path modulePath = projectPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleName);
        try {
            Files.createDirectories(modulePath);

            // We will be creating following in the module directory
            // - src/
            // -- mymodule/
            // --- Module.md      <- module level documentation
            // --- main.bal       <- Contains default main method.
            // --- resources/     <- resources for the module (available at runtime)
            // --- tests/         <- tests for this module (e.g. unit tests)
            // ---- main_test.bal  <- test file for main
            // ---- resources/    <- resources for these tests

            applyTemplate(modulePath, template);

        } catch (AccessDeniedException e) {
            throw new ModuleCreateException("Insufficient Permission");
        } catch (IOException | TemplateException e) {
            throw new ModuleCreateException(e.getMessage());
        }
    }

    private List<String> getTemplates() {
        try {
            Path templateDir = getTemplatePath();
            Stream<Path> walk = Files.walk(templateDir, 1);

            List<String> templates = walk.filter(Files::isDirectory)
                    .filter(directory -> !templateDir.equals(directory))
                    .filter(directory -> directory.getFileName() != null)
                    .map(directory -> directory.getFileName())
                    .map(fileName -> fileName.toString())
                    .collect(Collectors.toList());

            if (null != CreateCommand.jarFs) {
                return templates.stream().map(t -> t
                        .replace(CreateCommand.jarFs.getSeparator(), ""))
                        .collect(Collectors.toList());
            } else {
                return templates;
            }

        } catch (IOException | TemplateException e) {
            // we will return an empty list if error.
            return new ArrayList<String>();
        }
    }

    private Path getTemplatePath() throws TemplateException {
        try {
            URI uri = getClass().getClassLoader().getResource("create_cmd_templates").toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                return jarFs.getPath(array[1]);
            } else {
                return Paths.get(uri);
            }
        } catch (URISyntaxException e) {
            throw new TemplateException(e.getMessage());
        }
    }

    private void applyTemplate(Path modulePath, String template) throws TemplateException {
        Path templateDir = getTemplatePath().resolve(template);

        try {
            Files.walkFileTree(templateDir, new Copy(templateDir, modulePath));
        } catch (IOException e) {
            throw new TemplateException(e.getMessage());
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


        public Copy (Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy (Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile (Path file, BasicFileAttributes attrs)
                throws IOException {

            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

    static class TemplateException extends Exception {
        public TemplateException(String message) {
            super(message);
        }
    }

    static class ModuleCreateException extends Exception {
        public ModuleCreateException(String message) {
            super(message);
        }
    }
}
