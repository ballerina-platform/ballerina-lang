/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.task.RunProfilerTask;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.PROFILE_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.isProjectUpdated;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_PROFILE_DEBUG;

/**
 * This class represents the "profile" command, and it holds arguments and flags specified by the user.
 *
 * @since 2201.8.0
 */
@CommandLine.Command(name = PROFILE_COMMAND, description = "Compile and profile the current package")
public class ProfileCommand implements BLauncherCmd {
    private static final String ENV_OPTION_BAL_PROFILE_DEBUG = "BAL_PROFILE_JAVA_DEBUG";
    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private boolean exitWhenFinish;

    @CommandLine.Parameters(description = "Program arguments")
    private final List<String> argList = new ArrayList<>();

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, description = "Builds offline without downloading dependencies and " +
            "then run.")
    private Boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--generate-config-schema", hidden = true)
    private Boolean configSchemaGen;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--enable-cache", description = "enable caches for the compilation", hidden = true)
    private Boolean enableCache;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    private static final String PROFILE_CMD = "bal profile [--debug <port>] [<ballerina-file | package-path>]\n ";

    public ProfileCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.err;
        this.errStream = System.err;
    }

    ProfileCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
        this.offline = true;
    }

    public void execute() {
        if (this.helpFlag) {
            printCommandUsageInfo();
            return;
        }
        setupDebugPort();
        setupProfileDebugPort();
        String[] args = getArgumentsFromArgList();
        BuildOptions buildOptions = constructBuildOptions();
        Project project = loadProject(buildOptions);
        if (project == null) {
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        if (ProjectUtils.isProjectEmpty(project)) {
            CommandUtil.printError(this.errStream, "package is empty. Please add at least one .bal file.",
                    null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        boolean isPackageModified = isProjectUpdated(project);
        TaskExecutor taskExecutor = createTaskExecutor(isPackageModified, args, buildOptions,
                project.kind() == ProjectKind.SINGLE_FILE_PROJECT);
        taskExecutor.executeTasks(project);
    }

    private void setupProfileDebugPort() {
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_PROFILE_DEBUG, this.debugPort);
        }
    }

    private void printCommandUsageInfo() {
        String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PROFILE_COMMAND);
        this.errStream.println(commandUsageInfo);
    }

    private void setupDebugPort() {
        String port = System.getenv(ENV_OPTION_BAL_PROFILE_DEBUG);
        if (port != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, port);
        }
    }

    private String[] getArgumentsFromArgList() {
        String[] args = new String[0];
        if (!argList.isEmpty()) {
            if (!argList.get(0).equals("--")) { // project path provided
                this.projectPath = Paths.get(argList.get(0));
                if (argList.size() > 2 && argList.get(1).equals("--")) { // args to main provided
                    args = argList.subList(2, argList.size()).toArray(new String[0]);
                }
            } else { // current directory is the project path
                if (argList.size() > 1 && argList.get(0).equals("--")) { // args to main provided
                    args = argList.subList(1, argList.size()).toArray(new String[0]);
                }
            }
        }
        return args;
    }

    private Project loadProject(BuildOptions buildOptions) {
        if (FileUtils.hasExtension(this.projectPath)) {
            return loadSingleFileProject(buildOptions);
        } else {
            return loadBuildProject(buildOptions);
        }
    }

    private Project loadSingleFileProject(BuildOptions buildOptions) {
        try {
            return SingleFileProject.load(this.projectPath, buildOptions);
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), PROFILE_CMD, false);
            return null;
        }
    }

    private Project loadBuildProject(BuildOptions buildOptions) {
        try {
            return BuildProject.load(this.projectPath, buildOptions);
        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), PROFILE_CMD, false);
            return null;
        }
    }

    private TaskExecutor createTaskExecutor(boolean isPackageModified, String[] args, BuildOptions buildOptions,
                                            boolean isSingleFileBuild) {
        return new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(isPackageModified, buildOptions.enableCache()), isSingleFileBuild)
                .addTask(new RunBuildToolsTask(outStream), isSingleFileBuild)
                .addTask(new ResolveMavenDependenciesTask(outStream))
                .addTask(new CompileTask(outStream, errStream, false, false, isPackageModified,
                        buildOptions.enableCache()))
                .addTask(new CreateExecutableTask(outStream, null), false)
                .addTask(new DumpBuildTimeTask(outStream), false)
                .addTask(new RunProfilerTask(errStream), false).build();
    }

    @Override
    public String getName() {
        return PROFILE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(PROFILE_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal profile [--debug <port>] [<balfile> | <project-path>]\n");
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
                .setConfigSchemaGen(configSchemaGen)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.build();
    }
}
