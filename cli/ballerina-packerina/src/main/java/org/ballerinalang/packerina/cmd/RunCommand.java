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
import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CopyModuleJarTask;
import org.ballerinalang.packerina.task.CopyNativeLibTask;
import org.ballerinalang.packerina.task.CreateBaloTask;
import org.ballerinalang.packerina.task.CreateBirTask;
import org.ballerinalang.packerina.task.CreateExecutableTask;
import org.ballerinalang.packerina.task.CreateJarTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
import org.ballerinalang.packerina.task.RunExecutableTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.BallerinaCliCommands;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.VMOptions;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * This class represents the "run" command and it holds arguments and flags specified by the user.
 *
 * @since 1.0.0
 */
@CommandLine.Command(name = "run", description = "compile and run Ballerina programs")
public class RunCommand implements BLauncherCmd {
    
    private final PrintStream errStream;

    @CommandLine.Parameters(description = "arguments")
    private List<String> argList;

    @CommandLine.Option(names = {"--sourceroot"},
            description = "path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = {"--config", "-c"}, description = "path to the Ballerina configuration file")
    private String configFilePath;

    @CommandLine.Option(names = "--observe", description = "enable observability with default configs")
    private boolean observeFlag;

    @CommandLine.Option(names = "-e", description = "Ballerina environment parameters")
    private Map<String, String> runtimeParams = new HashMap<>();

    @CommandLine.Option(names = "-B", description = "Ballerina VM options")
    private Map<String, String> vmOptions = new HashMap<>();

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = "--siddhiruntime", description = "enable siddhi runtime for stream processing")
    private boolean siddhiRuntimeFlag;

    public RunCommand() {
        this.errStream = System.err;
    }

    public RunCommand(PrintStream errStream) {
        this.errStream = errStream;
    }

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(Constants.RUN_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageExceptionWithHelp("no ballerina program given");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        String programArg = argList.get(0);

        // remove the hyphen of the module folder if it exists
        if (programArg.endsWith("/")) {
            programArg = programArg.substring(0, programArg.length() - 1);
        }
        // Check if programArg is a path not a file if so we calculate the source root from that
        if (programArg.contains(File.separator)) {
            Path programArgPath = Paths.get(programArg);
            Path tempProgramFile = programArgPath.getFileName();
            if (null != tempProgramFile) {
                programArg = tempProgramFile.toString();
            }
            Path balFileRoot = programArgPath.getParent();
            if (null != balFileRoot) {
                sourceRoot = balFileRoot.toString();
            }
        }

        Path sourcePath = Paths.get(programArg).normalize();



        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        VMOptions.getInstance().addOptions(vmOptions);

        // Filter out the list of arguments given to the ballerina program.
        // TODO: 7/26/18 improve logic with positioned param
        String[] programArgs;
        if (argList.size() >= 2) {
            argList.remove(0);
            programArgs = argList.toArray(new String[0]);
        } else {
            programArgs = new String[0];
        }
    
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(LOCK_ENABLED, Boolean.toString(false));
        options.put(SKIP_TESTS, Boolean.toString(true));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(experimentalFlag));
        options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeFlag));
    
        if (sourcePath.normalize().toString().endsWith(BLANG_COMPILED_JAR_EXT)) {
            // jar file given to directly run
    
            BuildContext buildContext = new BuildContext(sourceRootPath);
            buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
            
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new RunExecutableTask(sourcePath.normalize(), programArgs, runtimeParams, configFilePath,
                            observeFlag))
                    .build();
    
            taskExecutor.executeTasks(buildContext);
        } else {
    
            // get the absolute path for the source. source can be a module or a bal file.
            Path sourceFullPath = RepoUtils.isBallerinaProject(sourceRootPath) ?
                                  sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                          .resolve(sourcePath).toAbsolutePath() :
                                  sourceRootPath.resolve(sourcePath).toAbsolutePath();
    
            // check if source exists or not
            if (Files.notExists(sourceFullPath)) {
                throw LauncherUtils.createLauncherException("the given module or source file does not exist.");
            }
    
            if (Files.isRegularFile(sourceFullPath) &&
                sourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                !RepoUtils.isBallerinaProject(sourceRootPath)) {
    
                try {
                    Path tempTarget = Files.createTempDirectory(sourcePath.toString());
                    BuildContext buildContext = new BuildContext(sourceRootPath, tempTarget, sourceFullPath);
                    buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
        
                    // if its a single bal file
                    TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                            .addTask(new CreateTargetDirTask())
                            .addTask(new CompileTask())
                            .addTask(new CreateBirTask())
                            .addTask(new CreateJarTask())
                            .addTask(new CopyModuleJarTask())
                            .addTask(new CreateExecutableTask())
                            .addTask(new RunExecutableTask(programArgs, runtimeParams, configFilePath, observeFlag))
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
    
                BuildContext buildContext = new BuildContext(sourceRootPath, sourceFullPath);
                buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
        
                TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                        .addTask(new CleanTargetDirTask())
                        .addTask(new CreateTargetDirTask())
                        .addTask(new CompileTask())
                        .addTask(new CreateBaloTask())
                        .addTask(new CopyNativeLibTask())
                        .addTask(new CreateBirTask())
                        .addTask(new CreateJarTask())
                        .addTask(new CopyModuleJarTask())
                        .addTask(new CreateExecutableTask())
                        .addTask(new RunExecutableTask(programArgs, runtimeParams, configFilePath, observeFlag))
                        .build();
        
                taskExecutor.executeTasks(buildContext);
            } else {
                // Invalid source file provided
                throw LauncherUtils.createLauncherException("invalid ballerina source path, it should either be a " +
                                                            "directory or a file  with a \'"
                                                            + BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension");
            }
        }
        
        // Normalize the source path to remove './' or '.\' characters that can appear before the name
        LauncherUtils.runProgram(sourceRootPath, sourcePath.normalize(), runtimeParams, configFilePath, programArgs,
                offline, observeFlag, siddhiRuntimeFlag, experimentalFlag);
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
        out.append("  ballerina run [flags] <balfile | module-name > [args...] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
