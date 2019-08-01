/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyExecutableTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateDocsTask;
import org.ballerinalang.packerina.task.CreateExecutableTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateLockFileTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.PrintExecutablePathTask;
import org.ballerinalang.packerina.task.RunCompilerPluginTask;
import org.ballerinalang.packerina.task.RunTestsTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.util.BLangConstants;
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
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.cmd.Constants.BUILD_COMMAND;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "build the Ballerina source")
public class BuildCommand implements BLauncherCmd {
    
    private Path userDir;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private boolean exitWhenFinish;

    public BuildCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        outStream = System.out;
        errStream = System.err;
        exitWhenFinish = true;
    }

    public BuildCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @CommandLine.Option(names = {"-o"}, description = "write output to the given file")
    private String outputFileName;

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = {"--lockEnabled"})
    private boolean lockEnabled;

    @CommandLine.Option(names = {"--skip-tests"})
    private boolean skipTests;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--native"}, hidden = true,
            description = "compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = {"--config"}, description = "path to the configuration file")
    private String configFilePath;

    @CommandLine.Option(names = "--siddhi-runtime", description = "enable siddhi runtime for stream processing")
    private boolean siddhiRuntimeFlag;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina compile [<module-name>]",
                    true);
        }

        // Get source root path.
        Path sourceRootPath = userDir;
    
        if (nativeBinary) {
            genNativeBinary(sourceRootPath, argList);
        } else if (argList == null || argList.size() == 0) {
            // to build all modules of a project
            if (!ProjectDirs.isProject(sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(sourceRootPath);
                if (null == findRoot) {
                    CommandUtil.printError(errStream,
                            "Please provide a Ballerina file as a " +
                                    "input or run build command inside a project",
                            "ballerina build [<filename.bal>]",
                            false);
                    return;
                }
                sourceRootPath = findRoot;
            }
    
            CompilerContext context = new CompilerContext();
            CompilerOptions options = CompilerOptions.getInstance(context);
            options.put(PROJECT_DIR, sourceRootPath.toString());
            options.put(OFFLINE, Boolean.toString(offline));
            options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
            options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
            options.put(SKIP_TESTS, Boolean.toString(skipTests));
            options.put(TEST_ENABLED, "true");
            options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(experimentalFlag));
            options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeFlag));
            
            BuildContext buildContext = new BuildContext(sourceRootPath);
            buildContext.setOut(outStream);
            buildContext.setOut(errStream);
            buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
            
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new CleanTargetDirTask())
                    .addTask(new CreateTargetDirTask())
                    .addTask(new CompileTask())
                    .addTask(new CreateBaloTask())
                    .addTask(new CreateBirTask())
                    .addTask(new CopyNativeLibTask())
                    .addTask(new CreateJarTask())
                    .addTask(new CopyModuleJarTask())
                    .addTask(new RunTestsTask(), this.skipTests)
                    .addTask(new CreateExecutableTask())
                    .addTask(new PrintExecutablePathTask())
                    .addTask(new CreateLockFileTask())
                    .addTask(new CreateDocsTask())
                    .addTask(new RunCompilerPluginTask())
                    .build();
    
            taskExecutor.executeTasks(buildContext);
        } else {
            CompilerContext context = new CompilerContext();
            CompilerOptions options = CompilerOptions.getInstance(context);
            options.put(PROJECT_DIR, sourceRootPath.toString());
            options.put(OFFLINE, Boolean.toString(offline));
            options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
            options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
            options.put(SKIP_TESTS, Boolean.toString(skipTests));
            options.put(TEST_ENABLED, "true");
            options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(experimentalFlag));
            options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeFlag));
    
            // remove the hyphen of the module folder if it exists
            String pkgOrSourceFileNameAsString = argList.get(0);
            if (pkgOrSourceFileNameAsString.endsWith("/")) {
                pkgOrSourceFileNameAsString = pkgOrSourceFileNameAsString.substring(0,
                        pkgOrSourceFileNameAsString.length() - 1);
            }
            
            // normalize the source path to remove './' or '.\' characters that can appear before the name
            Path pkgOrSourceFileName = Paths.get(pkgOrSourceFileNameAsString).normalize();
            
            // get the absolute path for the source. source can be a module or a bal file.
            Path sourceFullPath = RepoUtils.isBallerinaProject(sourceRootPath) ?
                                  sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                          .resolve(pkgOrSourceFileName).toAbsolutePath() :
                                  sourceRootPath.resolve(pkgOrSourceFileName).toAbsolutePath();
            
            // check if source exists or not
            if (Files.notExists(sourceFullPath)) {
                throw LauncherUtils.createLauncherException("the given module or source file does not exist.");
            }
            
            if (Files.isRegularFile(sourceFullPath) &&
                pkgOrSourceFileName.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                !RepoUtils.isBallerinaProject(sourceRootPath)) {
                
                // if its a single bal file
                Path executableFilePath = sourceRootPath;
                if (outputFileName != null && !outputFileName.isEmpty()) {
                    if (Files.isDirectory(Paths.get(outputFileName)) &&
                        outputFileName.endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                        throw LauncherUtils.createLauncherException("invalid output path, it should be a file with" +
                                                                    " a \'" + BLangConstants.BLANG_SRC_FILE_SUFFIX +
                                                                    "\' extension.");
                    }
    
                    if (!executableFilePath.isAbsolute()) {
                        // this 'if' is to avoid spot bugs
                        Path executableFileName = executableFilePath.getFileName();
                        if (null != executableFileName) {
                            executableFilePath = sourceRootPath.resolve(executableFileName.toString());
                        }
                    }
                }
    
                try {
                    // TODO: use a files system
                    Path tempTarget = Files.createTempDirectory(pkgOrSourceFileNameAsString);
                    BuildContext buildContext = new BuildContext(sourceRootPath, tempTarget, sourceFullPath);
                    buildContext.setOut(outStream);
                    buildContext.setOut(errStream);
                    buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
        
                    TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                            .addTask(new CreateTargetDirTask())
                            .addTask(new CompileTask())
                            .addTask(new CreateBirTask())
                            .addTask(new CreateJarTask())
                            .addTask(new CopyModuleJarTask())
                            .addTask(new RunTestsTask(), this.skipTests)
                            .addTask(new CreateExecutableTask())
                            .addTask(new CopyExecutableTask(executableFilePath))
                            .addTask(new PrintExecutablePathTask())
                            .addTask(new RunCompilerPluginTask())
                            .build();
        
                    taskExecutor.executeTasks(buildContext);
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException("error occurred when creating build artifacts.");
                }
            } else if (Files.isDirectory(sourceFullPath)) {
                // if its a module
                // Checks if the source is a module and if its inside a project (with a Ballerina.toml folder)
                if (!RepoUtils.isBallerinaProject(sourceRootPath)) {
                    throw LauncherUtils.createLauncherException("you are trying to build a module that is not inside " +
                                                                "a project. Run `ballerina new` from " +
                                                                sourceRootPath + " to initialize it as a " +
                                                                "project and then build the module.");
                }
                
                BuildContext buildContext = new BuildContext(sourceRootPath, pkgOrSourceFileName);
                buildContext.setOut(outStream);
                buildContext.setOut(errStream);
                buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
    
                TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                        .addTask(new CleanTargetDirTask())
                        .addTask(new CreateTargetDirTask())
                        .addTask(new CompileTask())
                        .addTask(new CreateBaloTask())
                        .addTask(new CreateBirTask())
                        .addTask(new CopyNativeLibTask())
                        .addTask(new CreateJarTask())
                        .addTask(new CopyModuleJarTask())
                        .addTask(new RunTestsTask(), this.skipTests)
                        .addTask(new CreateExecutableTask())
                        .addTask(new PrintExecutablePathTask())
                        .addTask(new CreateLockFileTask())
                        .addTask(new CreateDocsTask())
                        .addTask(new RunCompilerPluginTask())
                        .build();
    
                taskExecutor.executeTasks(buildContext);
            } else {
                // Invalid source file provided
                throw LauncherUtils.createLauncherException("invalid ballerina source path, it should either be a " +
                                                            "directory or a file  with a \'"
                                                            + BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension");
            }
        }
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina sources and writes the output to a file. \n");
        out.append("\n");
        out.append("By default, output filename is the last part of module name \n");
        out.append("or the filename (minus the extension) with the extension \".balx\". \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to that file. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build <balfile | module-name> [-o output] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void genNativeBinary(Path projectDirPath, List<String> argList) {
        throw LauncherUtils.createLauncherException("llvm native generation is not supported");
    }
}
