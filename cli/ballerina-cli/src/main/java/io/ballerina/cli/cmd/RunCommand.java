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
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.task.RunExecutableTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.isProjectUpdated;
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


    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "when run is used with a source file or a module.")
    private Boolean observabilityIncluded;

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

    @CommandLine.Option(names = "--enable-cache", description = "enable caches for the compilation", hidden = true)
    private Boolean enableCache;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time", hidden = true)
    private Boolean dumpBuildTime;

    private static final String runCmd =
            """
                    bal run [--debug <port>] <executable-jar>\s
                        bal run [--offline]
                                      [<ballerina-file | package-path>] [-- program-args...]
                    \s""";

    public RunCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
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

    RunCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish, Path targetDir) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
        this.targetDir = targetDir;
        this.offline = true;
    }

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
                this.projectPath = Paths.get(argList.get(0));
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

        if (sticky == null) {
            sticky = false;
        }

        // load project
        Project project;
        BuildOptions buildOptions = constructBuildOptions();

        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = SingleFileProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = BuildProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // If project is empty
        if (ProjectUtils.isProjectEmpty(project)) {
            CommandUtil.printError(this.errStream, "package is empty. Please add at least one .bal file.", null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // Check package files are modified after last build
        boolean isPackageModified = isProjectUpdated(project);
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // clean target dir for projects
                .addTask(new CleanTargetDirTask(isPackageModified, buildOptions.enableCache()), isSingleFileBuild)
                // Run build tools
                .addTask(new RunBuildToolsTask(outStream), isSingleFileBuild)
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, false,
                        isPackageModified, buildOptions.enableCache()))
//                .addTask(new CopyResourcesTask(), isSingleFileBuild)
                .addTask(new RunExecutableTask(args, outStream, errStream))
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .build();
        taskExecutor.executeTasks(project);
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

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        buildOptionsBuilder
                .setCodeCoverage(false)
                .setOffline(offline)
                .setSkipTests(true)
                .setTestReport(false)
                .setObservabilityIncluded(observabilityIncluded)
                .setSticky(sticky)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setConfigSchemaGen(configSchemaGen)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setDumpBuildTime(dumpBuildTime);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.build();
    }
}
