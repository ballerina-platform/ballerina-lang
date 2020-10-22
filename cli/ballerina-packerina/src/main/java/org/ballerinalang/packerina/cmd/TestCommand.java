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

import io.ballerina.runtime.launch.LaunchUtils;
import io.ballerina.runtime.util.BLangConstants;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.packerina.JarResolverImpl;
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyResourcesTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.ListTestGroupsTask;
import org.ballerinalang.packerina.task.RunTestsTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.DUMP_BIR;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
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

    @CommandLine.Option(names = "--debug", description = "start in remote debugging mode")
    private String debugPort;

    @CommandLine.Option(names = "--list-groups", description = "list the groups available in the tests")
    private boolean listGroups;

    @CommandLine.Option(names = "--groups", split = ",", description = "test groups to be executed")
    private List<String> groupList;

    @CommandLine.Option(names = "--disable-groups", split = ",", description = "test groups to be disabled")
    private List<String> disableGroupList;

    @CommandLine.Option(names = "--test-report", description = "enable test report generation")
    private boolean testReport;

    @CommandLine.Option(names = "--code-coverage", description = "enable code coverage")
    private boolean coverage;

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable.")
    private boolean observabilityIncluded;

    @CommandLine.Option(names = "--tests", split = ",", description = "Test functions to be executed")
    private List<String> testList;

    @CommandLine.Option(names = "--old-parser", description = "Enable old parser.", hidden = true)
    private boolean useOldParser;

    @CommandLine.Option(names = "--rerun-failed", description = "Rerun failed tests.")
    private boolean rerunTests;

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(TEST_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }
        String[] args;
        if (this.argList == null) {
            args = new String[0];
        } else if (this.buildAll) {
            args = this.argList.toArray(new String[0]);
        } else {
            args = argList.subList(1, argList.size()).toArray(new String[0]);
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running the
        // executable jar in a separate JVM process.
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
        }

        String[] userArgs = LaunchUtils.getUserArgs(args, new HashMap<>());
        // check if there are too many arguments.
        if (userArgs.length > 0) {
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

        if (this.rerunTests) {
            // Cannot rerun failed tests using -a | -all flags
            if (this.buildAll) {
                CommandUtil.printError(this.errStream,
                                       "Cannot specify --rerun-failed and -a | --all flags at the same time",
                                       "ballerina test --rerun-failed <module-name>",
                                       true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            // Cannot rerun failed tests for single bal files
            if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                CommandUtil.printError(this.errStream,
                                       "--rerun-failed not supported for single bal files",
                                       "ballerina test --rerun-failed <module-name>",
                                       true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        // validation and decide source root and source full path
        this.sourceRootPath = null != this.sourceRoot ?
                Paths.get(this.sourceRoot).toAbsolutePath() : this.sourceRootPath;
        Path sourcePath = null;
        Path targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);

        if (groupList != null && disableGroupList != null) {
            CommandUtil.printError(this.errStream,
                    "Cannot specify both --groups and --disable-groups flags at the same time",
                    "ballerina test --groups <group1, ...> <module-name> | -a | --all",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        } else if ((groupList != null || disableGroupList != null) && testList != null) {
            CommandUtil.printError(this.errStream,
                    "Cannot specify --tests flag along with --groups/--disable-groups flags at the same time",
                    "ballerina test --tests <testFunction1, ...> <module-name> | -a | --all",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        if ((listGroups && disableGroupList != null) || (listGroups && groupList != null) ||
                (listGroups && testList != null)) {

            CommandUtil.printError(this.errStream,
                    "Cannot specify both --list-groups and --disable-groups/--groups/--tests flags at the " +
                            "same time",
                    "ballerina test --list-groups <module-name> | -a | --all",
                    true);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

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
            // TODO: remove this once code cov is implemented to support single bal file
            if (coverage) {
                coverage = false;
                this.outStream.println("Code coverage is not yet supported with single bal files. Ignoring the flag " +
                        "and continuing the test run...");
            }
            // when a single bal file is provided
            // Check if path given is an absolute path. Update the root accordingly
            sourcePath = (Paths.get(this.argList.get(0)).isAbsolute()) ?
                    Paths.get(this.argList.get(0)) : sourceRootPath.resolve(this.argList.get(0));
            sourceRootPath = sourcePath.getParent();

            // Check if the command is executed from a ballerina project
            // If function cannot find project root, then its likely not a ballerina project
            if (ProjectDirs.findProjectRoot(this.sourceRootPath) != null) {
                CommandUtil.printError(this.errStream,
                        "you are trying to test a single file within a ballerina project." +
                                " To run tests within a project, the module name must be specified.",
                        "ballerina test <module-name>",
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            // Check if the given file exists
            if (Files.notExists(sourcePath)) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' Ballerina file does not exist",
                        null,
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            // Check if the given file is a regular file and not a symlink
            if (!Files.isRegularFile(sourcePath)) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' is not a Ballerina file. check if it is a symlink or shortcut.",
                        null,
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            // Create a temp directory for the target path
            try {
                targetPath = Files.createTempDirectory("ballerina-test-" + System.nanoTime());
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("error occured when creating executable.");
            }
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
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(DUMP_BIR, Boolean.toString(dumpBIR));
        options.put(LOCK_ENABLED, Boolean.toString(!this.skipLock));
        options.put(TEST_ENABLED, "true");
        options.put(SKIP_TESTS, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));

        // create builder context
        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
        JarResolver jarResolver = JarResolverImpl.getInstance(buildContext, skipCopyLibsFromDist,
                observabilityIncluded);
        buildContext.put(BuildContextField.JAR_RESOLVER, jarResolver);
        buildContext.setOut(outStream);
        buildContext.setErr(errStream);

        boolean isSingleFileBuild = buildContext.getSourceType().equals(SINGLE_BAL_FILE);
        // output path is the current directory if -o flag is not given.

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory.
                //.addTask(new ResolveMavenDependenciesTask()) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask()) // compile the modules
                .addTask(new CreateBirTask(), listGroups)   // create the bir
                .addTask(new CreateBaloTask(), isSingleFileBuild || listGroups) // create the balos for modules
                // (projects only)
                .addTask(new CreateJarTask(), listGroups)  // create the jar
                .addTask(new CopyResourcesTask(), isSingleFileBuild || listGroups)
                // tasks to list groups or execute tests. the 'listGroups' boolean is used to decide whether to
                // skip the task or to execute
                .addTask(new ListTestGroupsTask(), !listGroups) // list the available test groups
                // run tests
                .addTask(new RunTestsTask(testReport, coverage, rerunTests, args, groupList, disableGroupList,
                                testList), listGroups)
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
