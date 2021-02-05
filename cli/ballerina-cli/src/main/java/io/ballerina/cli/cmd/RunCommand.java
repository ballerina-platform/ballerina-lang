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
import io.ballerina.cli.task.CreateBaloTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunExecutableTask;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * This class represents the "run" command and it holds arguments and flags specified by the user.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = RUN_COMMAND, description = "Build and execute a Ballerina program.")
public class RunCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private boolean exitWhenFinish;

    @CommandLine.Parameters(description = "Program arguments")
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, description = "Builds offline without downloading dependencies and " +
            "then run.")
    private boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;


    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "when run is used with a source file or a module.")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--taint-check", description = "perform taint flow analysis")
    private Boolean taintCheck;

    private static final String runCmd = "bal run [--experimental] [--offline] [--taint-check]\n" +
            "                  <executable-jar | ballerina-file | . | package-path> [program-args] [(--key=value)...]";

    public RunCommand() {
        this.outStream = System.err;
        this.errStream = System.err;
    }

    public RunCommand(Path projectPath, PrintStream outStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.exitWhenFinish = exitWhenFinish;
        this.outStream = outStream;
        this.errStream = outStream;
    }

    public void execute() {
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

        String[] args;
        if (this.argList == null) {
            CommandUtil.printError(this.errStream, "no package path provided.", runCmd, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } else {
            args = argList.subList(1, argList.size()).toArray(new String[0]);
            this.projectPath = Paths.get(argList.get(0)).toAbsolutePath().normalize();
        }

        // load project
        Project project;
        BuildOptions buildOptions = constructBuildOptions();
        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                project = SingleFileProject.load(this.projectPath, buildOptions);
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            try {
                project = BuildProject.load(this.projectPath, buildOptions);
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), runCmd, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new ResolveMavenDependenciesTask(outStream)) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask(outStream, errStream)) // compile the modules
                .addTask(new CreateBaloTask(outStream), isSingleFileBuild) // create the BALO (build projects only)
//                .addTask(new CopyResourcesTask(), isSingleFileBuild)
                .addTask(new RunExecutableTask(args, outStream, errStream))
                .build();

        taskExecutor.executeTasks(project);
    }

    @Override
    public String getName() {
        return RUN_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Run command runs a compiled Ballerina program. \n");
        out.append("\n");
        out.append("If a Ballerina source file is given, \n");
        out.append("run command compiles and runs it. \n");
        out.append("\n");
        out.append("By default, 'bal run' executes the main function. \n");
        out.append("If the main function is not there, it executes services. \n");
        out.append("\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal run {<balfile> | <project-path> | executable-jar}[--offline]\n" +
                "                 [(--key=value)...] "
                + "[--] [args...] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private BuildOptions constructBuildOptions() {
        return new BuildOptionsBuilder()
                .codeCoverage(false)
                .experimental(experimentalFlag)
                .offline(offline)
                .skipTests(true)
                .testReport(false)
                .observabilityIncluded(observabilityIncluded)
                .taintCheck(taintCheck)
                .build();
    }
}
