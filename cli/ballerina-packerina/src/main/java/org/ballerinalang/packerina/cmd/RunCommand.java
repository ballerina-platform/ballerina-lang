/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CopyResourcesTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.PrintExecutablePathTask;
import org.ballerinalang.packerina.task.PrintRunningExecutableTask;
import org.ballerinalang.packerina.task.ResolveMavenDependenciesTask;
import org.ballerinalang.packerina.task.RunExecutableTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.BallerinaCliCommands;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.DUMP_BIR;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.NEW_PARSER_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * This class represents the "run" command and it holds arguments and flags specified by the user.
 *
 * @since 1.0.0
 */
@CommandLine.Command(name = "run", description = "Build and execute a Ballerina program.")
public class RunCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;

    @CommandLine.Parameters(description = "Program arguments")
    private List<String> argList;

    @CommandLine.Option(names = {"--sourceroot"},
            description = "Path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"}, description = "Builds offline without downloading dependencies and " +
            "then run.")
    private boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = {"--native"}, hidden = true,
            description = "Compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = "--no-optimize-llvm", hidden = true)
    private boolean noOptimizeLLVM;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--new-parser", description = "Enable new parser.", hidden = true)
    private boolean newParserEnabled;

    public RunCommand() {
        this.outStream = System.err;
        this.errStream = System.err;
    }

    public RunCommand(PrintStream outStream, PrintStream errStream) {
        this.outStream = outStream;
        this.errStream = errStream;
    }

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(Constants.RUN_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        if (this.argList == null || this.argList.size() == 0) {
            CommandUtil.printError(this.errStream,
                    "no ballerina program given.",
                    "ballerina run {<bal-file> | <module-name> | <executable-jar>}",
                    true);

            Runtime.getRuntime().exit(1);
            return;
        }

        // Sets the debug port as a system property, which will be used when setting up debug args before running the
        // executable jar in a separate JVM process.
        if (this.debugPort != null) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, this.debugPort);
        }

        // get program args
        String[] programArgs = this.getProgramArgs(this.argList);

        // validation and decide source root and source full path
        Path sourceRootPath = this.sourceRoot == null ? Paths.get(System.getProperty("user.dir")) :
                Paths.get(this.sourceRoot);
        Path sourcePath;
        Path targetPath;

        if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            // when a single bal file is provided.
            //// check if path given is an absolute path. update source root accordingly.
            if (Paths.get(this.argList.get(0)).isAbsolute()) {
                sourcePath = Paths.get(this.argList.get(0));
            } else {
                sourcePath = sourceRootPath.resolve(this.argList.get(0));
            }
            sourceRootPath = sourcePath.getParent();

            //// check if the given file exists.
            if (Files.notExists(sourcePath)) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' Ballerina file does not exist.",
                        null,
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            //// check if the given file is a regular file and not a symlink.
            if (!Files.isRegularFile(sourcePath)) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' is not a Ballerina file. check if it is a symlink or a shortcut.",
                        null,
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            try {
                targetPath = Files.createTempDirectory("ballerina-run-" + System.nanoTime());
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException("error occurred when creating executable.");
            }
        } else if (Files.exists(
                sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
                Files.isDirectory(
                        sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0)))) {

            // when building a ballerina module

            //// check if command executed from project root.
            if (!RepoUtils.isBallerinaProject(sourceRootPath)) {
                CommandUtil.printError(this.errStream,
                        "you are trying to run a module that is not inside a project.",
                        null,
                        false);
                Runtime.getRuntime().exit(1);
                return;
            }

            //// check if module name given is not absolute.
            if (Paths.get(argList.get(0)).isAbsolute()) {
                CommandUtil.printError(this.errStream,
                        "you are trying to run a module by giving the absolute path. you only need give " +
                                "the name of the module.",
                        "ballerina run <module-name>",
                        true);
                Runtime.getRuntime().exit(1);
                return;
            }

            String moduleName = argList.get(0);

            //// remove end forward slash
            if (moduleName.endsWith("/")) {
                moduleName = moduleName.substring(0, moduleName.length() - 1);
            }

            sourcePath = Paths.get(moduleName);

            //// check if module exists.
            if (Files.notExists(sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(sourcePath))) {
                CommandUtil.printError(this.errStream,
                        "'" + sourcePath + "' module does not exist.",
                        "ballerina run <module-name>",
                        true);
                Runtime.getRuntime().exit(1);
                return;
            }

            targetPath = sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else {
            CommandUtil.printError(this.errStream, "invalid Ballerina source path. It should either be a name" +
                            " of a module in a Ballerina project, a file with a '" +
                            BLangConstants.BLANG_SRC_FILE_SUFFIX + "' extension, or an executable '" +
                            BLANG_COMPILED_JAR_EXT + "' file.",
                    "ballerina run {<bal-file> | <module-name> | <executable-jar>}", true);
            Runtime.getRuntime().exit(1);
            return;
        }

        // normalize paths
        sourceRootPath = sourceRootPath.normalize();
        sourcePath = sourcePath == null ? null : sourcePath.normalize();
        targetPath = targetPath.normalize();

        // create compiler context
        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(DUMP_BIR, Boolean.toString(dumpBIR));
        options.put(LOCK_ENABLED, Boolean.toString(true));
        options.put(SKIP_TESTS, Boolean.toString(true));
        options.put(TEST_ENABLED, Boolean.toString(false));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        options.put(NEW_PARSER_ENABLED, Boolean.toString(this.newParserEnabled));

        // create builder context
        BuildContext buildContext = new BuildContext(sourceRootPath, targetPath, sourcePath, compilerContext);
        buildContext.setOut(this.outStream);
        buildContext.setErr(this.errStream);

        boolean isSingleFileBuild = buildContext.getSourceType().equals(SINGLE_BAL_FILE);

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory.
                .addTask(new CompileTask()) // compile the modules
                .addTask(new ResolveMavenDependenciesTask())
                .addTask(new CreateBaloTask(), isSingleFileBuild)   // create the balos for modules(projects only)
                .addTask(new CreateBirTask())   // create the bir
                .addTask(new CopyNativeLibTask())    // copy the native libs(projects only)
                .addTask(new CreateJarTask())   // create the jar
                .addTask(new CopyResourcesTask(), isSingleFileBuild)
                .addTask(new CopyModuleJarTask(false, true))
                .addTask(new PrintExecutablePathTask(), isSingleFileBuild)   // print the location of the executable
                .addTask(new PrintRunningExecutableTask(!isSingleFileBuild))   // print running executables
                .addTask(new RunExecutableTask(programArgs))
                .build();

        taskExecutor.executeTasks(buildContext);
    }

    /**
     * Get the program args from the passed argument list.
     *
     * @param argList The argument list.
     * @return An array of program args.
     */
    private String[] getProgramArgs(List<String> argList) {
        String[] argsArray = argList.toArray(new String[0]);
        return Arrays.copyOfRange(argsArray, 1, argsArray.length);
    }

    @Override
    public String getName() {
        return BallerinaCliCommands.RUN;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Run command runs a compiled Ballerina program. \n");
        out.append("\n");
        out.append("If a Ballerina source file or a module is given, \n");
        out.append("run command compiles and runs it. \n");
        out.append("\n");
        out.append("By default, 'ballerina run' executes the main function. \n");
        out.append("If the main function is not there, it executes services. \n");
        out.append("\n");
        out.append("If the -s flag is given, 'ballerina run' executes\n");
        out.append("services instead of the main function.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina run [--offline]\n" +
                "                [--sourceroot]\n" +
                "                {<balfile> | module-name | executable-jar} [(--key=value)...] "
                + "[--] [args...] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
