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
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.cli.cmd.CommandUtil.DEFAULT_TEMPLATE;
import static io.ballerina.cli.cmd.CommandUtil.balFilesExists;
import static io.ballerina.cli.cmd.CommandUtil.checkPackageFilesExists;
import static io.ballerina.cli.cmd.CommandUtil.initPackageFromCentral;
import static io.ballerina.cli.cmd.Constants.NEW_COMMAND;
import static io.ballerina.projects.util.TomlUtil.getStringArrayFromTableNode;

/**
 * New command for creating a ballerina project.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = NEW_COMMAND, description = "Create a new Ballerina package")
public class NewCommand implements BLauncherCmd {

    private final PrintStream errStream;
    private final boolean exitWhenFinish;
    Path homeCache = RepoUtils.createAndGetHomeReposPath();

    @CommandLine.Parameters
    public List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"}, description = "Acceptable values: [main, service, lib] " +
            "default: default")
    public String template = "default";

    @CommandLine.Option(names = {"--workspace"})
    private boolean workspace;

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

        Path packagePath = Path.of(argList.get(0));
        Path currentDir = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        if (!packagePath.isAbsolute()) {
            packagePath = Path.of(currentDir.toString(), packagePath.toString()).normalize();
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
            if (!packageFiles.isEmpty() && !template.equals(DEFAULT_TEMPLATE)) {
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
        }

        if (this.workspace) {
            String tplPackageName = "hello";
            try {
                Files.createDirectories(packagePath);
                if (DEFAULT_TEMPLATE.equals(template) || "main".equals(template)) {
                    tplPackageName += "-app";
                } else if ("service".equals(template)) {
                    tplPackageName += "-service";
                } else if ("lib".equals(template)) {
                    tplPackageName += "-lib";
                }
                Files.writeString(packagePath.resolve(ProjectConstants.BALLERINA_TOML),
                        "[workspace]\n");
                packagePath = packagePath.resolve(tplPackageName);
                packageName = tplPackageName.replace("_", "-");
            } catch (IOException e) {
                CommandUtil.printError(errStream,
                        "error while creating the workspace directory '" + packagePath + "'",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        } else {
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
        }
        Optional<Path> workspaceRoot = ProjectPaths.workspaceRoot(packagePath);

        try {
            // check if the template matches with one of the inbuilt template types
            if (CommandUtil.getTemplates().contains(template)) {
                // create package with inbuilt template
                if (Files.exists((packagePath))) {
                    String existingFiles = CommandUtil.checkTemplateFilesExists(template, packagePath);
                    if (!existingFiles.isEmpty()) {
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
                String orgName;
                if (workspaceRoot.isEmpty()) {
                    orgName = ProjectUtils.guessOrgName();
                } else {
                    orgName = getOrgName(workspaceRoot.get());
                }
                CommandUtil.initPackageByTemplate(packagePath, orgName, packageName, template, balFilesExist);
            } else {
                Path balaCache = homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME);
                initPackageFromCentral(balaCache, packagePath, packageName, template, filesInDir);
            }
            workspaceRoot = ProjectPaths.workspaceRoot(packagePath);
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
            if (!Path.of(argList.get(0)).isAbsolute()) {
                packagePath = Path.of(argList.get(0));
            }
            errStream.println("Created new package '" + packageName + "' at " + packagePath + ".");
        }
        if (workspaceRoot.isPresent()) {
            Path wpBallerinaToml = workspaceRoot.get().resolve(ProjectConstants.BALLERINA_TOML);
            try {
                TomlDocument tomlDocument = TomlDocument.from(ProjectConstants.BALLERINA_TOML,
                        Files.readString(wpBallerinaToml));
                String relativePackagePath;
                if (workspace) {
                    relativePackagePath = workspaceRoot.get().relativize(workspaceRoot.get().resolve(packageName))
                            .toString();
                } else {
                    if (packagePath.isAbsolute()) {
                        relativePackagePath = workspaceRoot.get().relativize(packagePath).toString();
                    } else {
                        relativePackagePath = workspaceRoot.get().relativize(packagePath.toAbsolutePath()).toString();
                    }
                }
                replacePackagesInWpBalToml(tomlDocument.toml(), wpBallerinaToml, relativePackagePath);
            } catch (IOException e) {
                CommandUtil.printError(errStream, e.getMessage(),
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
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

    private void replacePackagesInWpBalToml(Toml toml, Path balTomlPath, String packagePath) throws IOException {
        StringBuilder replacementStr = new StringBuilder();
        TomlTableNode tomlAstNode = toml.rootNode();
        TopLevelNode topLevelPkgNode = tomlAstNode.entries().get("workspace");
        if (topLevelPkgNode == null || topLevelPkgNode.kind() != TomlType.TABLE) {
            replacementStr.append("[workspace]\n");
        }
        TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;
        if (pkgNode == null) {
            CommandUtil.printError(errStream, "error while parsing the 'workspace' table in " + balTomlPath,
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        List<String> packages = getStringArrayFromTableNode(pkgNode, "packages");
        replacementStr.append("packages = [");
        for (String pkg : packages) {
            replacementStr.append("\"").append(pkg).append("\", ");
        }
        replacementStr.append("\"").append(packagePath).append("\"").append("]");

        String content = Files.readString(balTomlPath);
        Pattern pattern = Pattern.compile("packages\\s*=\\s*\\[\\s*(?:\"[^\"]*\"\\s*,\\s*)*\"[^\"]*\"\\s*]",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        String modifiedContent;
        if (matcher.find()) {
            String existingStr = matcher.group();
            modifiedContent = content.replace(existingStr, replacementStr);
        } else {
            modifiedContent = content + replacementStr;
        }
        Files.writeString(balTomlPath, modifiedContent, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String getOrgName(Path workspaceRoot) throws IOException {
        TomlDocument tomlDocument = TomlDocument.from(ProjectConstants.BALLERINA_TOML,
                Files.readString(workspaceRoot.resolve(ProjectConstants.BALLERINA_TOML)));
        TomlTableNode tomlAstNode = tomlDocument.toml().rootNode();
        TopLevelNode topLevelPkgNode = tomlAstNode.entries().get("workspace");
        if (topLevelPkgNode != null && topLevelPkgNode.kind() == TomlType.TABLE) {
            TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;
            List<String> packages = getStringArrayFromTableNode(pkgNode, "packages");
            if (!packages.isEmpty()) {
                String packageName = packages.get(0);
                Path pkgRoot = workspaceRoot.resolve(packageName);
                try {
                    ProjectLoadResult projectLoadResult = ProjectLoader.load(pkgRoot);
                    PackageManifest manifest = projectLoadResult.project().currentPackage().manifest();
                    return manifest.org().toString();
                } catch (ProjectException e) {
                    // ignore the exception and return the guessed org name
                }
            }
        }
        return ProjectUtils.guessOrgName();
    }
}
