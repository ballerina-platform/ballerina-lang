/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.CreateFingerprintTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.ResolveWorkspaceDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * This class represents the "bal build" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "Compile the current package")
public class BuildCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;

    public BuildCommand() {
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                 Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        boolean dumpBuildTime) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.dumpBuildTime = dumpBuildTime;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        String output) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.output = output;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                        Path targetDir) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.targetDir = targetDir;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                 boolean dumpBuildTime, boolean nativeImage, String graalVMBuildOptions) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.dumpBuildTime = dumpBuildTime;
        this.offline = true;
        this.nativeImage = nativeImage;
        this.graalVMBuildOptions = graalVMBuildOptions;
    }

    @CommandLine.Option(names = {"--output", "-o"}, description = "Write the output to the given file. The provided " +
                                                                  "output file name may or may not contain the " +
                                                                  "'.jar' extension.")
    private String output;

    @CommandLine.Option(names = {"--offline"}, description = "Build/Compile offline without downloading " +
                                                              "dependencies.")
    private Boolean offline;

    @CommandLine.Parameters (arity = "0..1")
    private final Path projectPath;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-bir-file", hidden = true)
    private Boolean dumpBIRFile;

    @CommandLine.Option(names = "--dump-graph", description = "Print the dependency graph.", hidden = true)
    private boolean dumpGraph;

    @CommandLine.Option(names = "--dump-raw-graphs", description = "Print all intermediate graphs created in the " +
            "dependency resolution process.", hidden = true)
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private Boolean experimentalFlag;

    @CommandLine.Option(names = "--generate-config-schema", hidden = true)
    private Boolean configSchemaGen;

    private static final String buildCmd = "bal build [-o <output>] [--offline] [--taint-check]\n" +
            "                    [<ballerina-file | package-path>]";

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "JAR file(s).")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--cloud", description = "Enable cloud artifact generation")
    private String cloud;

    @CommandLine.Option(names = "--show-dependency-diagnostics", description = "Show the diagnostics " +
            "generated by the dependencies")
    private Boolean showDependencyDiagnostics;

    @CommandLine.Option(names = "--remote-management", description = "enable service management tools in " +
            "the executable JAR file(s).")
    private Boolean remoteManagement;

    @CommandLine.Option(names = "--list-conflicted-classes",
            description = "list conflicted classes when generating executable")
    private Boolean listConflictedClasses;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time", hidden = true)
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--export-openapi", description = "generate openAPI contract files for all" +
            " the services in the current package")
    private Boolean exportOpenAPI;

    @CommandLine.Option(names = "--export-component-model", description = "generate a model to represent " +
            "interactions between the package components (i.e. service/type definitions) and, export it in JSON format",
            hidden = true)
    private Boolean exportComponentModel;

    @CommandLine.Option(names = "--graalvm", description = "enable native image generation")
    private Boolean nativeImage;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--graalvm-build-options", description = "additional build options for native image " +
            "generation")
    private String graalVMBuildOptions;

    @CommandLine.Option(names = "--optimize-dependency-compilation", hidden = true,
            description = "experimental memory optimization for large projects")
    private Boolean optimizeDependencyCompilation;

    @CommandLine.Option(names = "--locking-mode", hidden = true,
            description = "allow passing the package locking mode.", converter = PackageLockingModeConverter.class)
    private PackageLockingMode lockingMode;

    @Override
    public void execute() {
        long start = 0;
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        // load project
        Project project;
        BuildOptions buildOptions = constructBuildOptions();
        Path absProjectPath = this.projectPath.toAbsolutePath().normalize();
        try {
            if (buildOptions.dumpBuildTime()) {
                start = System.currentTimeMillis();
                BuildTime.getInstance().timestamp = start;
            }
            if (!ProjectPaths.isBuildProjectRoot(projectPath)
                    && !ProjectPaths.isStandaloneBalFile(projectPath)
                    && !ProjectPaths.isWorkspaceProjectRoot(projectPath)) {
                throw new ProjectException("invalid source provided: " + absProjectPath +
                        ". Please provide a valid Ballerina package, workspace or a standalone file.");
            }

            project = ProjectLoader.load(projectPath, buildOptions).project();
            if (buildOptions.dumpBuildTime()) {
                BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
            }
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if (!project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            if (this.output != null) {
                CommandUtil.printError(this.errStream,
                        "'-o' and '--output' are only supported when building a single Ballerina " +
                                "file.",
                        "bal build -o <output-file> <ballerina-file> ",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // Validate Settings.toml file
        RepoUtils.readSettings();

        if (!project.buildOptions().nativeImage() && !project.buildOptions().graalVMBuildOptions().isEmpty()) {
            this.outStream.println("WARNING: Additional GraalVM build options are ignored since graalvm " +
                    "flag is not set");
        }

        if (project.kind() == ProjectKind.WORKSPACE_PROJECT) {
            WorkspaceProject workspaceProject = (WorkspaceProject) project;
            DependencyGraph<BuildProject> projectDependencyGraph = resolveWorkspaceDependencies(workspaceProject);
            List<BuildProject> topologicallySortedList = new ArrayList<>(
                    projectDependencyGraph.toTopologicallySortedList());
            if (!workspaceProject.sourceRoot().equals(absProjectPath)) {
                // If the project path is not the workspace root, filter the topologically sorted list to include only
                // the projects that are dependencies of the project at the specified path.
                Optional<BuildProject> buildProjectOptional = projectDependencyGraph.getNodes().stream()
                        .filter(node -> node.sourceRoot().equals(absProjectPath)).findFirst();
                Collection<BuildProject> projectDependencies = projectDependencyGraph.getAllDependencies(
                        buildProjectOptional.orElseThrow());
                // remove projects that are not dependencies of the project at the specified path
                topologicallySortedList.removeIf(prj -> !projectDependencies.contains(prj)
                        && prj != buildProjectOptional.get());
            }
            for (BuildProject buildProject : topologicallySortedList) {
                boolean skipExecutable = false;
                if (workspaceProject.sourceRoot().equals(absProjectPath)) {
                    if (hasDependents(buildProject, projectDependencyGraph)) {
                        // If the project is a dependency of another project, skip creating an executable JAR.
                        skipExecutable = true;
                    }
                } else if (!buildProject.sourceRoot().equals(absProjectPath)) {
                    if (hasDependents(buildProject, projectDependencyGraph)) {
                        // If the project is a dependency of another project, skip creating an executable JAR.
                        skipExecutable = true;
                    }
                }
                executeTasks(false, buildProject, skipExecutable);
            }
        } else {
            executeTasks(project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT), project, false);
        }
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private boolean hasDependents(BuildProject buildProject, DependencyGraph<BuildProject> projectDependencyGraph) {
        return !projectDependencyGraph.getDirectDependents(buildProject).isEmpty();
    }

    private DependencyGraph<BuildProject> resolveWorkspaceDependencies(WorkspaceProject workspaceProject) {
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new ResolveWorkspaceDependenciesTask(outStream))
                .build();
        taskExecutor.executeTasks(workspaceProject);
        return workspaceProject.getResolution().dependencyGraph();
    }

    private void executeTasks(boolean isSingleFile, Project project, boolean skipExecutable) {
        BuildOptions buildOptions = project.buildOptions();
        boolean rebuildStatus = isRebuildNeeded(project, skipExecutable);
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // clean the target directory(projects only)
                .addTask(new CleanTargetDirTask(),  isSingleFile)
                .addTask(new RestoreCachedArtifactsTask(), rebuildStatus)
                // Run build tools
                .addTask(new RunBuildToolsTask(outStream, !rebuildStatus), isSingleFile)
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream, !rebuildStatus))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, true, !rebuildStatus))
                .addTask(new CreateExecutableTask(outStream, this.output, null, false,
                         !rebuildStatus, skipExecutable))
                .addTask(new DumpBuildTimeTask(outStream), !buildOptions.dumpBuildTime())
                .addTask(new CacheArtifactsTask(BUILD_COMMAND, skipExecutable), !rebuildStatus || isSingleFile)
                .addTask(new CreateFingerprintTask(false, skipExecutable), !rebuildStatus || isSingleFile)
                .build();

        taskExecutor.executeTasks(project);
    }

    private boolean isRebuildNeeded(Project project, boolean skipExecutable) {
        Path buildFilePath = project.targetDir().resolve(BUILD_FILE);
        try {
            BuildJson buildJson = readBuildJson(buildFilePath);
            if (!Objects.equals(buildJson.distributionVersion(), RepoUtils.getBallerinaVersion())) {
                return true;
            }
            if (buildJson.isExpiredLastUpdateTime()) {
                return true;
            }
            if (CommandUtil.isFilesModifiedSinceLastBuild(buildJson, project, false, skipExecutable)) {
                return true;
            }
            if (isRebuildForCurrCmd()) {
                return true;
            }
            return CommandUtil.isPrevCurrCmdCompatible(project.buildOptions(), buildJson.getBuildOptions());
        } catch (IOException e) {
            // ignore
        }
        return true;
    }

    private boolean isRebuildForCurrCmd() {
        return dumpBIR
                || Boolean.TRUE.equals(dumpBIRFile) || dumpGraph || dumpRawGraphs
                || Boolean.TRUE.equals(configSchemaGen) || Boolean.TRUE.equals(showDependencyDiagnostics)
                || Boolean.TRUE.equals(listConflictedClasses) || Boolean.TRUE.equals(dumpBuildTime)
                || targetDir != null || Boolean.TRUE.equals(exportOpenAPI) || Boolean.TRUE.equals(exportComponentModel)
                || Boolean.TRUE.equals(nativeImage)
                || cloud != null
                || Boolean.TRUE.equals(disableSyntaxTreeCaching) || graalVMBuildOptions != null;
    }

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setObservabilityIncluded(observabilityIncluded)
                .setCloud(cloud)
                .setRemoteManagement(remoteManagement)
                .setDumpBir(dumpBIR)
                .setDumpBirFile(dumpBIRFile)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setListConflictedClasses(listConflictedClasses)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(this.sticky)
                .setConfigSchemaGen(configSchemaGen)
                .setExportOpenAPI(exportOpenAPI)
                .setExportComponentModel(exportComponentModel)
                .setNativeImage(nativeImage)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setGraalVMBuildOptions(graalVMBuildOptions)
                .setShowDependencyDiagnostics(showDependencyDiagnostics)
                .setOptimizeDependencyCompilation(optimizeDependencyCompilation)
                .setLockingMode(lockingMode);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.setConfigSchemaGen(configSchemaGen)
                .build();
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal build [-o <output>] [--offline] \\n\" +\n" +
                "            \"                    [<ballerina-file | package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
