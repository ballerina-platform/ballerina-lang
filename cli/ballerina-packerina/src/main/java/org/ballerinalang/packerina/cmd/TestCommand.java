/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.packerina.cmd;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.jvm.launch.LaunchUtils;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.RunTestsTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.packerina.cmd.Constants.TEST_COMMAND;

/**
 * Compile Ballerina modules in to balo.
 *
 * @since 0.992.0
 */
@CommandLine.Command(name = TEST_COMMAND, description = "Test Ballerina modules")
public class TestCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path sourceRootPath;
    private boolean exitWhenFinish;
    private boolean skipCopyLibsFromDist;

    public TestCommand() {
        this.sourceRootPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
        this.skipCopyLibsFromDist = false;
    }

    public TestCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
                       boolean skipCopyLibsFromDist) {
        this.sourceRootPath = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    @CommandLine.Option(names = {"--sourceroot"},
                        description = "Path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--all", "-a"}, description = "Build or compile all the modules of the project.")
    private boolean buildAll;

    @CommandLine.Option(names = {"--offline"}, description = "Builds/Compiles offline without downloading " +
            "dependencies.")
    private boolean offline;

    @CommandLine.Option(names = {"--skip-lock"}, description = "Skip using the lock file to resolve dependencies.")
    private boolean skipLock;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--native"}, hidden = true,
            description = "Compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = "--no-optimize-llvm", hidden = true)
    private boolean noOptimizeLLVM;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    // --debug flag is handled by ballerina.sh/ballerina.bat. It will launch ballerina with java debug options.
    @CommandLine.Option(names = "--debug", description = "start Ballerina in remote debugging mode")
    private String debugPort;

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TEST_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        String[] args = LaunchUtils
                .initConfigurations(this.argList == null ? new String[0] : this.argList.toArray(new String[0]));

        // check if there are too many arguments.
        if (args.length > 1) {
            CommandUtil.printError(this.errStream,
                    "too many arguments.",
                    "ballerina test [--offline] [--sourceroot <path>] [--experimental] [--skip-lock]\n" +
                           "                      [<module-name> | -a | --all]  [--] [(--key=value)...]",
                    false);

            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // if -a or --all flag is not given, then it is mandatory to give a module name or ballerina file as arg.
        if (!this.buildAll && (this.argList == null || this.argList.size() == 0)) {
            CommandUtil.printError(this.errStream,
                    "'test' command requires a module name or '-a | --all' flag " +
                            "to test all the modules of the project.",
                    "ballerina test <module-name> | -a | --all",
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // validation and decide source root and source full path
        this.sourceRootPath = null != this.sourceRoot ?
                Paths.get(this.sourceRoot).toAbsolutePath() : this.sourceRootPath;
        Path sourcePath = null;
        Path targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);

        // when -a or --all flag is provided. check if the command is executed within a ballerina project. update source
        // root path if command executed inside a project.
        if (this.buildAll) {
            //// validate and set source root path
            if (!ProjectDirs.isProject(this.sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(this.sourceRootPath);
                if (null == findRoot) {
                    CommandUtil.printError(this.errStream,
                            "you are trying to test a Ballerina project but there is no Ballerina.toml file.",
                            null,
                            false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }

                this.sourceRootPath = findRoot;
            }
        } else if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            // when a single bal file is provided.
            CommandUtil.printError(this.errStream,
                    "test command is only supported inside a Ballerina project",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
        } else if (Files.exists(
                this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
                Files.isDirectory(
                        this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                .resolve(this.argList.get(0)))) {

            //// check if command executed from project root.
            if (!RepoUtils.isBallerinaProject(this.sourceRootPath)) {
                CommandUtil.printError(this.errStream,
                        "you are trying to test a module that is not inside a project.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            //// check if module name given is not absolute.
            if (Paths.get(argList.get(0)).isAbsolute()) {
                CommandUtil.printError(this.errStream,
                        "you are trying to test a module by giving the absolute path. you only need give " +
                                "the name of the module.",
                        "ballerina test <module-name>",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            String moduleName = argList.get(0);

            //// remove end forward slash
            if (moduleName.endsWith("/")) {
                moduleName = moduleName.substring(0, moduleName.length() - 1);
            }

            sourcePath = Paths.get(moduleName);

            //// check if module exists.
            if (Files.notExists(this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(sourcePath))) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' module does not exist.",
                        "ballerina test <module-name>",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

        } else {
            CommandUtil.printError(this.errStream,
                    "invalid Ballerina project",
                    "ballerina test  <module-name> | -a | --all",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // normalize paths
        this.sourceRootPath = this.sourceRootPath.normalize();
        sourcePath = sourcePath == null ? null : sourcePath.normalize();
        targetPath = targetPath.normalize();

        // create compiler context
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(PROJECT_DIR, this.sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(LOCK_ENABLED, Boolean.toString(!this.skipLock));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        // create builder context
        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
        buildContext.setOut(outStream);
        buildContext.setErr(errStream);

        boolean isSingleFileBuild = buildContext.getSourceType().equals(SINGLE_BAL_FILE);
        // output path is the current directory if -o flag is not given.

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory.
                .addTask(new CompileTask()) // compile the modules
                .addTask(new CreateBaloTask(), isSingleFileBuild)   // create the balos for modules(projects only)
                .addTask(new CreateBirTask())   // create the bir
                .addTask(new CopyNativeLibTask(skipCopyLibsFromDist))    // copy the native libs(projects only)
                // create the jar.
                .addTask(new CreateJarTask(this.dumpBIR, this.skipCopyLibsFromDist, this.nativeBinary, this.dumpLLVMIR,
                        this.noOptimizeLLVM))
                .addTask(new CopyModuleJarTask(skipCopyLibsFromDist))
                .addTask(new RunTestsTask()) // run tests
                .build();

        taskExecutor.executeTasks(buildContext);

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return TEST_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina modules and create balo files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(" ballerina test [--offline] [--sourceroot <path>] [--experimental] [--skip-lock]\n" +
                           "[<module-name> | -a | --all] [--] [(--key=value)...]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
