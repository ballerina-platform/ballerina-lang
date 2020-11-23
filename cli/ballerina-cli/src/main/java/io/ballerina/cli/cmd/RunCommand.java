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

import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateBaloTask;
import io.ballerina.cli.task.CreateTargetDirTask;
import io.ballerina.cli.task.RunExecutableTask;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.DUMP_BIR;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

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
    private boolean observabilityIncluded;

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
            args = new String[0];
            this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        } else if (this.argList.get(0).startsWith(RuntimeConstants.BALLERINA_ARGS_INIT_PREFIX)) {
            args = argList.toArray(new String[0]);
            this.projectPath = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        } else {
            args = argList.subList(1, argList.size()).toArray(new String[0]);
            this.projectPath = Paths.get(argList.get(0));
        }


        // create compiler context
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(DUMP_BIR, Boolean.toString(dumpBIR));
        options.put(LOCK_ENABLED, Boolean.toString(true));
        options.put(SKIP_TESTS, Boolean.toString(true));
        options.put(TEST_ENABLED, Boolean.toString(false));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));

        // load project
        Project project;
        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                project = SingleFileProject.load(this.projectPath);
            } catch (RuntimeException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            try {
                project = BuildProject.load(this.projectPath);
            } catch (RuntimeException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory.
                //.addTask(new ResolveMavenDependenciesTask()) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask(outStream, errStream)) // compile the modules
                .addTask(new CreateBaloTask(outStream), isSingleFileBuild) // create the BALO (build projects only)
//                .addTask(new CopyResourcesTask(), isSingleFileBuild)
//                .addTask(new CopyObservabilitySymbolsTask(), isSingleFileBuild)
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
        out.append("By default, 'ballerina run' executes the main function. \n");
        out.append("If the main function is not there, it executes services. \n");
        out.append("\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina run [--offline]\n" +
                "                {<balfile> | executable-jar} [(--key=value)...] "
                + "[--] [args...] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
