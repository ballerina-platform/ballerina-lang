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
import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.CLEAN_COMMAND;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * This class represents the "bal clean" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = CLEAN_COMMAND, description = "Clean the artifacts generated during the build")
public class CleanCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private final Path projectPath;
    private final boolean exitWhenFinish;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--dependency-cache", description = "clean the caches of project dependencies")
    private boolean depCache;

    public CleanCommand(Path projectPath, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = System.out;
        this.exitWhenFinish = exitWhenFinish;
    }

    public CleanCommand() {
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.exitWhenFinish = true;
    }

    public CleanCommand(Path projectPath, PrintStream printStream, boolean exitWhenFinish, Path targetDir) {
        this.projectPath = projectPath;
        this.outStream =  printStream;
        this.exitWhenFinish = exitWhenFinish;
        this.targetDir = targetDir;
    }

    public CleanCommand(Path projectPath, PrintStream printStream, boolean depCache, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream =  printStream;
        this.exitWhenFinish = exitWhenFinish;
        this.depCache = depCache;
    }
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND);
            this.outStream.println(commandUsageInfo);
            return;
        }

        if (this.targetDir != null) {
            if (Files.notExists(this.targetDir)) {
                CommandUtil.printError(this.outStream,
                        "provided target directory '" + this.targetDir + "' does not exist.",
                        null, false);
            } else if (!Files.isDirectory(this.targetDir)) {
                CommandUtil.printError(this.outStream,
                        "provided target path '" + this.targetDir + "' is not a directory.",
                        null, false);
            } else {
                Path cacheDir = this.targetDir.resolve("cache");
                boolean isValidTarget = Files.exists(cacheDir)
                        && (Files.exists(this.targetDir.resolve(ProjectConstants.BALA_DIR_NAME))
                        || Files.exists(this.targetDir.resolve(ProjectConstants.BIN_DIR_NAME))
                        || Files.exists(this.targetDir.resolve(ProjectConstants.TARGET_API_DOC_DIRECTORY))
                        || Files.exists(cacheDir.resolve(ProjectConstants.TESTS_CACHE_DIR_NAME)));

                if (!isValidTarget) {
                    CommandUtil.printError(this.outStream,
                            "provided target directory '" + this.targetDir +
                                    "' is not a valid target directory.",
                            null, false);
                    return;
                }
                ProjectUtils.deleteDirectory(this.targetDir);
                this.outStream.println("Successfully deleted '" + this.targetDir);
            }
            return;
        }

        ProjectLoadResult loadResult = ProjectLoader.load(this.projectPath);
        Project project = loadResult.project();
        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            CommandUtil.printError(this.outStream,
                    "clean command is not supported for single file projects.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (project.kind().equals(ProjectKind.WORKSPACE_PROJECT)) {
            WorkspaceProject workspaceProject = (WorkspaceProject) project;
            List<BuildProject> projectList;

            if (depCache) {
                ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true).build();
                projectList = workspaceProject.getResolution(resolutionOptions).dependencyGraph()
                        .toTopologicallySortedList();
            } else {
                projectList = workspaceProject.projects();
            }
            for (BuildProject buildProject : projectList) {
                if (depCache) {
                    cleanDependencyCaches(project);
                }
                cleanProject(buildProject);
            }
            cleanProject(project);
        } else {
            if (depCache) {
                cleanDependencyCaches(project);
            }
            cleanProject(project);
        }
    }

    private void cleanDependencyCaches(Project project) {
        ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true)
                .setSticky(project.buildOptions().sticky()).build();
        PackageResolution packageResolution = project.currentPackage().getResolution(resolutionOptions);
        for (ResolvedPackageDependency packageDependency : packageResolution.allDependencies()) {
            Project dependency = packageDependency.packageInstance().project();
            if (dependency.kind().equals(ProjectKind.BALA_PROJECT)) {
                if (ProjectUtils.isBuiltInPackage(packageDependency.packageInstance().packageOrg(),
                        packageDependency.packageInstance().packageName().toString())) {
                    continue;
                }
                if (Files.notExists(dependency.sourceRoot())) {
                    continue;
                }
                // if the cache is in the distribution, do not delete
                if (dependency.sourceRoot().startsWith(ProjectUtils.getBallerinaHomePath()
                        .resolve(DIST_CACHE_DIRECTORY))) {
                    continue;
                }
                ProjectUtils.deleteDirectory(dependency.sourceRoot());
                this.outStream.println("Successfully deleted " + dependency.sourceRoot());
                Path birPath = ((FileSystemCache) dependency.projectEnvironmentContext()
                        .getService(CompilationCache.class)).cachePath();
                if (Files.notExists(birPath)) {
                    continue;
                }
                ProjectUtils.deleteDirectory(birPath);
                this.outStream.println("Successfully deleted " + birPath);
            }
        }
    }

    private void cleanProject(Project project) {
        Path generatedDir;
        try {
            // delete the generated directory
            generatedDir = project.sourceRoot().resolve(ProjectConstants.GENERATED_MODULES_ROOT);
        } catch (ProjectException e) {
            CommandUtil.printError(this.outStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (Files.exists(project.targetDir())) {
            ProjectUtils.deleteDirectory(project.targetDir());
            this.outStream.println("Successfully deleted " + project.targetDir());
        }
        if (Files.exists(generatedDir)) {
            ProjectUtils.deleteDirectory(generatedDir);
            this.outStream.println("Successfully deleted " + generatedDir);
        }
    }

    @Override
    public String getName() {
        return CLEAN_COMMAND;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND));
    }
    
    @Override
    public void printUsage(StringBuilder out) {
        out.append(" bal clean \n");
    }
    
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
