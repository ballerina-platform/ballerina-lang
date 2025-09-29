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
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.CreateFingerprintTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.ResolveWorkspaceDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.task.RunExecutableTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.ProjectWatcher;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.WorkspaceResolution;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "run" command and it holds arguments and flags specified by the user.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = RUN_COMMAND, description = "Compile and run the current package")
public class RunCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private boolean exitWhenFinish;
    RunExecutableTask runExecutableTask;
    Project project;

    private static final PathMatcher JAR_EXTENSION_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.jar");

    @CommandLine.Parameters(description = "Program arguments")
    private final List<String> argList = new ArrayList<>();

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, description = "Builds offline without downloading dependencies and " +
            "then run.")
    private Boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--watch", description = "watch for file changes and automatically re-run the project")
    private boolean watch;
    private boolean initialWatch;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "when run is used with a source file or a module.")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--remote-management", description = "enable management service in the " +
            "executable when run is used with a source file or a module.")
    private Boolean remoteManagement;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--dump-graph", description = "Print the dependency graph.", hidden = true)
    private boolean dumpGraph;

    @CommandLine.Option(names = "--dump-raw-graphs", description = "Print all intermediate graphs created in the " +
            "dependency resolution process.", hidden = true)
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = "--generate-config-schema", hidden = true)
    private Boolean configSchemaGen;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time", hidden = true)
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--show-dependency-diagnostics", description = "Show the diagnostics " +
            "generated by the dependencies")
    private Boolean showDependencyDiagnostics;

    @CommandLine.Option(names = "--optimize-dependency-compilation", hidden = true,
            description = "experimental memory optimization for large projects")
    private Boolean optimizeDependencyCompilation;

    @CommandLine.Option(names = "--locking-mode", hidden = true,
            description = "allow passing the package locking mode.", converter = PackageLockingModeConverter.class)
    private PackageLockingMode lockingMode;

    private static final String runCmd =
            """
                    bal run [--debug <port>] <executable-jar>\s
                        bal run [--experimental] [--offline]
                                      [<ballerina-file | package-path>] [-- program-args...]
                    \s""";

    public RunCommand() {
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.err;
        this.errStream = System.err;
    }

    RunCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
        this.offline = true;
    }

    RunCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish, Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
        this.offline = true;
    }

    RunCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish, Path targetDir) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
        this.targetDir = targetDir;
        this.offline = true;
    }

    @Override
    public void execute() {
        long start = 0;
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(RUN_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running the
        // executable jar in a separate JVM process.
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
        }

        String[] args = new String[0];
        if (!argList.isEmpty()) {
            if (!argList.get(0).equals("--")) { // project path provided
                this.projectPath = Path.of(argList.get(0));
                if (RunCommand.JAR_EXTENSION_MATCHER.matches(this.projectPath)) {
                    CommandUtil.printError(this.errStream, "unsupported option(s) provided for jar execution",
                            runCmd, true);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                if (argList.size() > 1 && !argList.get(1).equals("--")) {
                    CommandUtil.printError(this.errStream,
                            "unmatched command argument found: " + argList.get(1), runCmd, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                if (argList.size() > 2 && argList.get(1).equals("--")) { // args to main provided
                    args = argList.subList(2, argList.size()).toArray(new String[0]);
                }
            } else { // current directory is the project path
                if (argList.size() > 1 && argList.get(0).equals("--")) { // args to main provided
                    args = argList.subList(1, argList.size()).toArray(new String[0]);
                }
            }
        }

        if (this.watch) {
            try {
                ProjectWatcher projectWatcher = new ProjectWatcher(
                        this, Path.of(this.projectPath.toString()), outStream);
                projectWatcher.watch();
            } catch (IOException e) {
                throw createLauncherException("unable to watch the project:" + e.getMessage());
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
            }
            return;
        }


        // load project
        BuildOptions buildOptions = constructBuildOptions();
        Path absProjectPath = this.projectPath.toAbsolutePath().normalize();
        try {
            if (buildOptions.dumpBuildTime()) {
                start = System.currentTimeMillis();
                BuildTime.getInstance().timestamp = start;
            }
            project = ProjectLoader.load(projectPath, buildOptions).project();
            if (project.kind().equals(ProjectKind.WORKSPACE_PROJECT)) {
                WorkspaceProject workspaceProject = (WorkspaceProject) project;
                WorkspaceResolution workspaceResolution = workspaceProject.getResolution(
                        ResolutionOptions.builder().setOffline(true).build());
                List<BuildProject> topologicallySortedList = workspaceResolution.dependencyGraph()
                        .toTopologicallySortedList();
                BuildProject buildProject = topologicallySortedList.get(topologicallySortedList.size() - 1);
                Path relativePath = Paths.get(System.getProperty("user.dir")).relativize(buildProject.sourceRoot());
                if (project.sourceRoot().equals(absProjectPath)) {
                    CommandUtil.printError(this.errStream, "'bal run' command is not supported for workspaces. " +
                            "Please specify a package to run. \nExample:\n\tbal run " + relativePath, runCmd, false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
            }
            if (buildOptions.dumpBuildTime()) {
                BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
            }
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        Target target;
        try {
            if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
                target = new Target(Files.createTempDirectory("ballerina-cache" + System.nanoTime()));
                target.setOutputPath(target.getBinPath());
            }
            if (project.kind() == ProjectKind.WORKSPACE_PROJECT) {
                WorkspaceProject workspaceProject = (WorkspaceProject) project;
                DependencyGraph<BuildProject> projectDependencyGraph = resolveWorkspaceDependencies(workspaceProject);
                // If the project path is not the workspace root, filter the topologically sorted list to include only
                // the projects that are dependencies of the project at the specified path.
                Optional<BuildProject> buildProjectOptional = projectDependencyGraph.getNodes().stream().filter(node ->
                        node.sourceRoot().equals(absProjectPath)).findFirst();
                if (buildProjectOptional.isEmpty()) {
                    throw createLauncherException("no package found at the specified path: " + absProjectPath);
                }
                target = new Target(buildProjectOptional.get().targetDir());
                executeTasks(false, target, args, buildProjectOptional.get());

            } else {
                target = new Target(project.targetDir());
                executeTasks(project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT), target, args, project);
            }
        } catch (IOException e) {
            throw createLauncherException("unable to resolve the target path:" + e.getMessage());
        } catch (ProjectException e) {
            throw createLauncherException("unable to create the executable:" + e.getMessage());
        }
    }

    private DependencyGraph<BuildProject> resolveWorkspaceDependencies(WorkspaceProject workspaceProject) {
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new ResolveWorkspaceDependenciesTask(outStream))
                .build();
        taskExecutor.executeTasks(workspaceProject);
        return workspaceProject.getResolution().dependencyGraph();
    }

    private void executeTasks(boolean isSingleFileBuild, Target target, String[] args, Project project) {
        boolean rebuildStatus = isRebuildNeeded(project, false);
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // clean target dir for projects
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)
                .addTask(new RestoreCachedArtifactsTask(), rebuildStatus)
                // Run build tools
                .addTask(new RunBuildToolsTask(outStream, !rebuildStatus), isSingleFileBuild)
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream, !rebuildStatus))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, false, !rebuildStatus))
                .addTask(new CreateExecutableTask(outStream, null, target, true, !rebuildStatus))
                .addTask(new CacheArtifactsTask(RUN_COMMAND), !rebuildStatus || isSingleFileBuild)
                .addTask(new CreateFingerprintTask(false, false), !rebuildStatus || isSingleFileBuild)
                .addTask(runExecutableTask = new RunExecutableTask(args, outStream, errStream, target))
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
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
        return this.debugPort != null
                || initialWatch
                || this.dumpBIR
                || this.dumpGraph
                || this.dumpRawGraphs
                || Boolean.TRUE.equals(this.configSchemaGen)
                || this.targetDir != null
                || Boolean.TRUE.equals(this.disableSyntaxTreeCaching)
                || Boolean.TRUE.equals(this.dumpBuildTime)
                || Boolean.TRUE.equals(this.showDependencyDiagnostics);
    }

    @Override
    public String getName() {
        return RUN_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(RUN_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal run [--debug <port>] <executable-jar>\n");
        out.append("""
                  bal run [--offline] [<balfile> | <project-path>]
                [--] [args...]\s
                """);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    public void unsetWatch() {
        this.watch = false;
    }

    public void setInitialWatch() {
        this.initialWatch = true;
    }

    public void killProcess() {
        if (runExecutableTask != null) {
            runExecutableTask.killProcess();
        }
    }

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder
                .setCodeCoverage(false)
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setSkipTests(true)
                .setTestReport(false)
                .setObservabilityIncluded(observabilityIncluded)
                .setCloud("") // Skip the cloud import for the run command
                .setRemoteManagement(remoteManagement)
                .setSticky(sticky)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setConfigSchemaGen(configSchemaGen)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setDumpBuildTime(dumpBuildTime)
                .setShowDependencyDiagnostics(showDependencyDiagnostics)
                .setOptimizeDependencyCompilation(optimizeDependencyCompilation)
                .setLockingMode(lockingMode);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.build();
    }

    public boolean containsService() {
        return project == null || ProjectUtils.containsDefaultModuleService(project.currentPackage());
    }
}
