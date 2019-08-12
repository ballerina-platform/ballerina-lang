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
import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.packerina.cmd.Constants.BUILD_COMMAND;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "Ballerina build - Builds Ballerina module(s) and generates " +
                                                         "executable outputs.")
public class BuildCommand implements BLauncherCmd {
    
    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path sourceRootPath;
    private boolean exitWhenFinish;

    public BuildCommand() {
        this.sourceRootPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public BuildCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.sourceRootPath = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }
    
    @CommandLine.Option(names = {"--sourceroot"},
                        description = "Path to the directory containing source files and modules")
    private String sourceRoot;
    
    @CommandLine.Option(names = {"--compile", "-c"}, description = "Compile the source without generating " +
                                                                   "executable(s).")
    private boolean compile;
    
    @CommandLine.Option(names = {"--output", "-o"}, description = "Writes output to the given file. The provided " +
                                                                  "output filename may or may not contain the '.jar' " +
                                                                  "extension.")
    private String output;

    @CommandLine.Option(names = {"--off-line"}, description = "Builds/Compiles offline without downloading " +
                                                              "dependencies.")
    private boolean offline;

    @CommandLine.Option(names = {"--skip-lock"}, description = "Skip using the lock file to resolve dependencies.")
    private boolean skipLock;

    @CommandLine.Option(names = {"--skip-tests"}, description = "Skips test compilation and execution.")
    private boolean skipTests;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--native"}, hidden = true,
                        description = "Compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    @CommandLine.Option(names = {"--test-config"}, description = "Path to the configuration file when running tests.")
    private String configFilePath;

    @CommandLine.Option(names = "--siddhi-runtime", description = "Enable siddhi runtime for stream processing.")
    private boolean siddhiRuntimeFlag;

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }
        
        // check if there are too many arguments.
        if (this.argList != null && this.argList.size() > 1) {
            CommandUtil.printError(this.errStream,
                    "too many arguments.",
                    "ballerina build [<bal-file> | <module-name>]",
                    false);
            
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        
        if (this.nativeBinary) {
            CommandUtil.printError(this.errStream,
                    "llvm native generation is not supported.",
                    null,
                    false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
    
        // validation and decide source root and source full path
        this.sourceRootPath = null != this.sourceRoot ? Paths.get(this.sourceRoot) : this.sourceRootPath;
        Path sourcePath = null;
        Path targetPath;
        
        // when no bal file or module is given, it is assumed to build all modules of the project. check if the command
        // is executed within a ballerina project. update source root path if command executed inside a project.
        if (this.argList == null || this.argList.size() == 0) {
            // when building all the modules
            //// check if output flag is set
            if (null != this.output) {
                CommandUtil.printError(this.errStream,
                        "'-o' and '--output' flag is only supported for building a single ballerina file.",
                        "ballerina build <bal-file> -o foo.jar",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        
            //// validate and set source root path
            if (!ProjectDirs.isProject(this.sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(this.sourceRootPath);
                if (null == findRoot) {
                    CommandUtil.printError(this.errStream,
                            "you are trying to build/compile a ballerina project but there is no Ballerina.toml file.",
                            null,
                            false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                
                this.sourceRootPath = findRoot;
            }
        
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            // when a single bal file is provided.
            if (this.compile) {
                CommandUtil.printError(this.errStream,
                        "'-c' or '--compile' flag cannot be used on ballerina files. the flag can only be used with " +
                        "ballerina projects.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            } else {
                //// check if path given is an absolute path. update source root accordingly.
                if (Paths.get(this.argList.get(0)).isAbsolute()) {
                    sourcePath = Paths.get(this.argList.get(0));
                    this.sourceRootPath = sourcePath.getParent();
                } else {
                    sourcePath = this.sourceRootPath.resolve(this.argList.get(0));
                }
                
                //// check if the given file exists.
                if (Files.notExists(sourcePath)) {
                    CommandUtil.printError(this.errStream,
                            "'" + sourcePath + "' ballerina file does not exist.",
                            null,
                            false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                
                //// check if the given file is a regular file and not a symlink.
                if (!Files.isRegularFile(sourcePath)) {
                    CommandUtil.printError(this.errStream,
                            "'" + sourcePath + "' is not ballerina file. check if it is a symlink or shortcut.",
                            null,
                            false);
                    CommandUtil.exitError(this.exitWhenFinish);
                    return;
                }
                
                try {
                    targetPath = Files.createTempDirectory("ballerina-build-" + System.nanoTime());
                } catch (IOException e) {
                    throw LauncherUtils.createLauncherException("error occurred when creating executable.");
                }
            }
        } else if (Files.exists(
                this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
                   Files.isDirectory(
               this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0)))) {
            
            // when building a ballerina module
            //// output flag cannot be set for projects
            if (null != this.output) {
                CommandUtil.printError(this.errStream,
                        "'-o' and '--output' flag is only supported for building a single ballerina file.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            
            //// check if command executed from project root.
            if (!RepoUtils.isBallerinaProject(this.sourceRootPath)) {
                CommandUtil.printError(this.errStream,
                        "you are trying to build/compile a module that is not inside a project.",
                        null,
                        false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            
            //// check if module name given is not absolute.
            if (Paths.get(argList.get(0)).isAbsolute()) {
                CommandUtil.printError(this.errStream,
                        "you are trying to build/compile a module by giving the absolute path. you only need give " +
                        "the name of the module.",
                        "ballerina build [-c] <module-name>",
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
                        "ballerina build [-c] <module-name>",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
    
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else {
            CommandUtil.printError(this.errStream,
                    "invalid ballerina source path, it should either be a module name in a ballerina project or a " +
                    "file with a \'" + BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension.",
                    "ballerina build [<bal-file> | <module-name>]",
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
        options.put(SKIP_TESTS, Boolean.toString(this.skipTests));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(this.siddhiRuntimeFlag));
    
        // create builder context
        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
        buildContext.setOut(outStream);
        buildContext.setErr(errStream);
    
        boolean isSingleFileBuild = buildContext.getSourceType().equals(SINGLE_BAL_FILE);
        Path outputPath = null == this.output ? this.sourceRootPath : Paths.get(this.output);
        Path configFilePath = null == this.configFilePath ? null : Paths.get(this.configFilePath);
        
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask(), isSingleFileBuild)   // clean the target directory(projects only)
                .addTask(new CreateTargetDirTask()) // create target directory.
                .addTask(new CompileTask()) // compile the modules
                .addTask(new CreateBaloTask(), isSingleFileBuild)   // create the balos for modules(projects only)
                .addTask(new CreateBirTask())   // create the bir
                .addTask(new CopyNativeLibTask(), isSingleFileBuild)    // copy the native libs(projects only)
                .addTask(new CreateJarTask(this.dumpBIR))    // create the jar
                .addTask(new CopyModuleJarTask())
                .addTask(new RunTestsTask(configFilePath), this.skipTests || isSingleFileBuild) // run tests
                                                                                                // (projects only)
                .addTask(new CreateExecutableTask(), this.compile)  // create the executable .jar file
                .addTask(new CopyExecutableTask(outputPath), !isSingleFileBuild)    // copy executable
                .addTask(new PrintExecutablePathTask(), this.compile)   // print the location of the executable
                .addTask(new CreateLockFileTask(), isSingleFileBuild)   // create a lock file(projects only)
                .addTask(new CreateDocsTask(), isSingleFileBuild)   // generate API docs(projects only)
                .addTask(new RunCompilerPluginTask(), this.compile) // run compiler plugins
                .addTask(new CleanTargetDirTask(), !isSingleFileBuild)  // clean the target dir(single bals only)
                .build();
        
        taskExecutor.executeTasks(buildContext);
        
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Builds Ballerina module(s)/file and produces an executable jar file(s). \n");
        out.append("\n");
        out.append("Building a Ballerina project or a specific module in a project the \n");
        out.append("executable \".jar\" files will be created in <project-root>/target/bin directory. \n");
        out.append("\n");
        out.append("Building a single Ballerina file will create an executable .jar file in the \n");
        out.append("current directory. The name of the executable file will be. \n");
        out.append("<bal-file-name>-executable.jar. \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to the given output file name. The -o flag will only \n");
        out.append("work for single files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build [-o <output>] [--off-line] [--skip-tests] [--skip-lock] " +
                   "[<bal-file | module-name>] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
