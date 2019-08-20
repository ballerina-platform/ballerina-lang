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
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateDocsTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateLockFileTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.RunTestsTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
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
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.cmd.Constants.COMPILE_COMMAND;

/**
 * Compile Ballerina modules in to balo.
 *
 * @since 0.992.0
 */
@CommandLine.Command(name = COMPILE_COMMAND, description = "Ballerina compile - Compiles Ballerina module(s) and " +
                                                           "generates a .balo file(s).")
public class CompileCommand implements BLauncherCmd {
    
    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path sourceRootPath;
    private boolean exitWhenFinish;

    public CompileCommand() {
        this.sourceRootPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public CompileCommand(Path userDir, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.sourceRootPath = userDir;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @CommandLine.Option(names = {"--off-line"}, description = "Compiles offline without downloading dependencies.")
    private boolean offline;

    @CommandLine.Option(names = {"--skip-lock"}, description = "Skip using the lock file to resolve dependencies")
    private boolean skipLock;

    @CommandLine.Option(names = {"--skip-tests"}, description = "Skips test compilation and execution.")
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

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = {"--config"}, description = "Path to the configuration file when running tests." +
                                                            " A configuration file cannot be set if " +
                                                            "'--skip-tests' flag is passed.")
    private String configFilePath;

    public void execute() {

        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMPILE_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (this.argList != null && this.argList.size() > 1) {
            CommandUtil.printError(this.errStream,
                    "too many arguments.",
                    "ballerina compile [<module-name>]",
                    true);
        }
    
    
        if (!this.skipTests && null != this.configFilePath) {
            throw LauncherUtils.createLauncherException("you cannot use a config file for tests when tests are set " +
                                                        "to skip with '--skip-tests'.");
        }
    
        if (this.nativeBinary) {
            throw LauncherUtils.createLauncherException("llvm native generation is not supported");
        }
    
        // validation and decide source root and source full path
        Path sourcePath = null;
        Path targetPath;
    
        // when no bal file or module is given, it is assumed to compile all modules of the project. check if the
        // command is executed within a ballerina project. update source root path if command executed inside a project.
        if (this.argList == null || this.argList.size() == 0) {
            // when compiling all the modules
        
            //// validate and set source root path
            if (!ProjectDirs.isProject(this.sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(this.sourceRootPath);
                if (null == findRoot) {
                    throw LauncherUtils.createLauncherException("you are trying to compile a ballerina project but " +
                                                                "there is no Ballerina.toml file. Run " +
                                                                "'ballerina new' from '" + this.sourceRootPath +
                                                                "' to initialize it as a project.");
                }
            
                this.sourceRootPath = findRoot;
            }
        
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            // when a single bal file is provided.
            throw LauncherUtils.createLauncherException("'compile' command cannot be used on ballerina files. it can" +
                                                        " only be used with ballerina projects.");
        } else if (Files.exists(
                this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
                   Files.isDirectory(
                       this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0)))) {
        
            // when compiling a ballerina module
        
            //// check if command executed from project root.
            if (!RepoUtils.isBallerinaProject(this.sourceRootPath)) {
                throw LauncherUtils.createLauncherException("you are trying to compile a module that is not inside " +
                                                            "a project. Run 'ballerina new' from " +
                                                            this.sourceRootPath + " to initialize it as a " +
                                                            "project and then compile the module.");
            }
        
            //// check if module name given is not absolute.
            if (Paths.get(argList.get(0)).isAbsolute()) {
                throw LauncherUtils.createLauncherException("you are trying to compile a module by giving the " +
                                                            "absolute path. you only need give the name of the " +
                                                            "module.");
            }
        
            String moduleName = argList.get(0);
        
            //// remove end forward slash
            if (moduleName.endsWith("/")) {
                moduleName = moduleName.substring(0, moduleName.length() - 1);
            }
        
            sourcePath = Paths.get(moduleName);
        
            //// check if module exists.
            if (Files.notExists(this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(sourcePath))) {
                throw LauncherUtils.createLauncherException("'" + sourcePath + "' module does not exist.");
            }
        
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else {
            throw LauncherUtils.createLauncherException("invalid ballerina source path, it should either be a module " +
                                                        "name in a ballerina project or a file with a \'" +
                                                        BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension.");
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

        // create builder context
        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
        buildContext.setOut(outStream);
        buildContext.setErr(errStream);
    
        Path configFilePath = null == this.configFilePath ? null : Paths.get(this.configFilePath);
    
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CleanTargetDirTask())  // clean the target directory
                .addTask(new CreateTargetDirTask()) //  create target directory.
                .addTask(new CompileTask()) // compile the modules
                .addTask(new CreateBaloTask())  // create the balos for modules
                .addTask(new CreateBirTask())   // create the bir
                .addTask(new CopyNativeLibTask())   // copy the native libs
                .addTask(new CreateJarTask(this.dumpBIR))   // create the jar
                .addTask(new CopyModuleJarTask())
                .addTask(new RunTestsTask(configFilePath), this.skipTests)  // run tests
                .addTask(new CreateLockFileTask())  // create a lock file
                .addTask(new CreateDocsTask())  // generate API docs
                .build();
    
        taskExecutor.executeTasks(buildContext);
    
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return COMPILE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina module(s) and generates shareable .balo file(s). \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina compile [<module-name>] [--off-line] [--skip-tests] [--skip-lock] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
