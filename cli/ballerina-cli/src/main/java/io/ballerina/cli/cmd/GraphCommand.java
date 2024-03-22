/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.cli.task.CreateDependencyGraphTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.cmd.Constants.GRAPH_COMMAND;

/**
 * This class represents the "bal graph" command.
 *
 * @since 2201.2.0
 */
@CommandLine.Command(name = GRAPH_COMMAND, description = "Print the dependency graph in the console")
public class GraphCommand implements BLauncherCmd {
    private Project project;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;

    @CommandLine.Parameters(arity = "0..1")
    private final Path projectPath;

    @CommandLine.Option(names = "--dump-raw-graphs", description = "Print all intermediate graphs created in the " +
            "dependency resolution process.", defaultValue = "false")
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true, defaultValue = "false")
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, description = "Print the dependency graph offline without downloading " +
            "dependencies.", defaultValue = "false")
    private boolean offline;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)",
            defaultValue = "false")
    private boolean sticky;

    public GraphCommand() {
        this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    GraphCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = true;
    }

    public void execute() {
        if (this.helpFlag) {
            printHelpCommandInfo();
            return;
        }

        try {
            loadProject();
        } catch (ProjectException e) {
            printErrorAndExit(e.getMessage());
            return;
        }

        if (ProjectUtils.isProjectEmpty(this.project)) {
            printErrorAndExit("package is empty. Please add at least one .bal file.");
            return;
        }

        validateSettingsToml();


        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(true, false), isSingleFileProject())
                .addTask(new RunBuildToolsTask(outStream), isSingleFileProject())
                .addTask(new ResolveMavenDependenciesTask(outStream))
                .addTask(new CreateDependencyGraphTask(outStream, errStream))
                .build();
        taskExecutor.executeTasks(this.project);

        exitIfRequired();
    }

    private void printHelpCommandInfo() {
        String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(GRAPH_COMMAND);
        this.outStream.println(commandUsageInfo);
    }

    private void loadProject() {
        BuildOptions buildOptions = constructBuildOptions();

        if (isSingleFileProject()) {
            this.project = SingleFileProject.load(this.projectPath, buildOptions);
        } else {
            this.project = BuildProject.load(this.projectPath, buildOptions);
        }
    }

    private void printErrorAndExit(String errorMessage) {
        CommandUtil.printError(this.errStream, errorMessage, null, false);
        CommandUtil.exitError(this.exitWhenFinish);
    }

    private void validateSettingsToml() {
        try {
            RepoUtils.readSettings();
        } catch (SettingsTomlException e) {
            this.outStream.println("warning: " + e.getMessage());
        }
    }

    private void exitIfRequired() {
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private BuildOptions constructBuildOptions() {

        // if all dependency graphs are printed it includes the final graph.
        // Therefore, final graph is not needed to print separately.
        boolean dumpGraph = !dumpRawGraphs;
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        buildOptionsBuilder
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(this.dumpRawGraphs)
                .setOffline(this.offline)
                .setSticky(this.sticky);

        return buildOptionsBuilder.build();
    }

    private boolean isSingleFileProject() {
        return FileUtils.hasExtension(this.projectPath);
    }

    @Override
    public String getName() {
        return GRAPH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(GRAPH_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal graph [--dump-raw-graph] [--offline] [--sticky] \\n\" +\n" +
                "            \"                    [<ballerina-file | package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
