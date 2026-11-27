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
import io.ballerina.projects.internal.environment.DependencyCachePruner;
import io.ballerina.projects.internal.environment.FingerprintCompilationCache;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

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

    @CommandLine.Option(names = "--prune-dependency-cache", description = "remove the compiled dependencies that " +
            "no build used for a number of days, from the caches of all projects")
    private boolean pruneDependencyCache;

    @CommandLine.Option(names = "--unused-days", description = "number of days without use after which " +
            "--prune-dependency-cache removes a compiled dependency (default: 30)")
    private Integer unusedDays;

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
        this.outStream = printStream;
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

        if (this.unusedDays != null && !this.pruneDependencyCache) {
            CommandUtil.printError(this.outStream, "'--unused-days' can only be used with '--prune-dependency-cache'",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (this.pruneDependencyCache) {
            pruneDependencyCaches();
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

        ProjectLoadResult loadResult;
        try {
            loadResult = ProjectLoader.load(this.projectPath);
        } catch (ProjectException e) {
            CommandUtil.printError(this.outStream, "invalid project directory: " + e.getMessage(),
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

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
            if (depCache) {
                // Packages may share dependencies, so resolve all of them before any bala is deleted. A package
                // resolved after a shared bala is deleted would not find the dependency or its compiled variant.
                projectList.forEach(CleanCommand::resolveDependencies);
            }
            for (BuildProject buildProject : projectList) {
                if (depCache) {
                    cleanDependencyCaches(buildProject);
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

    /**
     * Removes compiled dependencies that no build used for the given number of days. Compiled dependencies are shared
     * by all projects, so this does not depend on the current project.
     */
    private void pruneDependencyCaches() {
        int days = this.unusedDays == null ? (int) DependencyCachePruner.DEFAULT_UNUSED_PERIOD.toDays() :
                this.unusedDays;
        if (days < 0) {
            CommandUtil.printError(this.outStream, "'--unused-days' must not be negative", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        DependencyCachePruner.Result result = DependencyCachePruner.prune(ProjectUtils.createAndGetHomeReposPath(),
                Duration.ofDays(days));
        if (result.removedVariants() == 0) {
            this.outStream.println("No compiled dependencies unused for " + days + " days to remove");
            return;
        }
        this.outStream.println(String.format(Locale.ROOT, "Removed %d compiled dependencies unused for %d days " +
                "(%.1f MB)", result.removedVariants(), days, result.freedBytes() / (1024.0 * 1024.0)));
    }

    /**
     * Resolves the dependencies of the given package offline, as the build that cached them did. The resolution is
     * kept by the package, so a later call returns the same one.
     */
    private static PackageResolution resolveDependencies(Project project) {
        ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true)
                .setSticky(project.buildOptions().sticky()).setPackageLockingMode(project.buildOptions().lockingMode())
                .build();
        return project.currentPackage().getResolution(resolutionOptions);
    }

    private void cleanDependencyCaches(Project project) {
        PackageResolution packageResolution = resolveDependencies(project);
        Path distributionRepository = ProjectUtils.getBallerinaHomePath().resolve(DIST_CACHE_DIRECTORY);
        Path localRepository = RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.LOCAL_REPOSITORY_NAME).resolve(ProjectConstants.BALA_DIR_NAME);
        for (ResolvedPackageDependency packageDependency : packageResolution.allDependencies()) {
            Project dependency = packageDependency.packageInstance().project();
            if (!dependency.kind().equals(ProjectKind.BALA_PROJECT)
                    || ProjectUtils.isBuiltInPackage(packageDependency.packageInstance().packageOrg(),
                    packageDependency.packageInstance().packageName().toString())) {
                continue;
            }
            Path bala = dependency.sourceRoot();
            boolean fromDistribution = bala.startsWith(distributionRepository);
            boolean fromLocalRepository = bala.startsWith(localRepository);
            // Balas of the distribution and the local repository cannot be pulled again, so they are kept.
            if (!fromDistribution && !fromLocalRepository && Files.exists(bala)) {
                ProjectUtils.deleteDirectory(bala);
                this.outStream.println("Successfully deleted " + bala);
            }
            // Delete the compiled dependency for the versions this project resolves, which is in the user home for
            // packages of all repositories. Other variants may be used by other projects.
            Path compiledDependency = compiledDependency(dependency, fromDistribution);
            if (compiledDependency != null && Files.exists(compiledDependency)) {
                ProjectUtils.deleteDirectory(compiledDependency);
                this.outStream.println("Successfully deleted " + compiledDependency);
            }
            if (fromLocalRepository) {
                this.outStream.println("Kept the local repository package " + bala);
            }
        }
    }

    /**
     * Returns the compiled dependency of the given package in the user home, or null if it has none. The cache the
     * distribution is shipped with is never returned.
     */
    private static Path compiledDependency(Project dependency, boolean fromDistribution) {
        CompilationCache compilationCache = dependency.projectEnvironmentContext().getService(CompilationCache.class);
        if (compilationCache instanceof FingerprintCompilationCache fingerprintCache) {
            return fingerprintCache.variantDir().orElse(null);
        }
        if (compilationCache instanceof FileSystemCache fileSystemCache && !fromDistribution) {
            return fileSystemCache.cachePath();
        }
        return null;
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
