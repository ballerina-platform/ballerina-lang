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
import org.ballerinalang.packerina.task.CompileTask;
import org.ballerinalang.packerina.task.CreateDocsTask;
import org.ballerinalang.packerina.task.CreateTargetDirTask;
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
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.cmd.Constants.DOC_COMMAND;

/**
 * This class represents the "ballerina doc" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = DOC_COMMAND, description = "Ballerina doc - Generates API Documentation")
public class DocCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path sourceRootPath;

    public DocCommand() {
        this.sourceRootPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
    }

    @CommandLine.Option(names = {"--sourceroot"},
                        description = "Path to the directory containing source files and modules.")
    private String sourceRoot;

    @CommandLine.Option(names = {"--all", "-a"}, description = "Generate docs for all the modules of the project.")
    private boolean buildAll;

    @CommandLine.Option(names = {"--offline"}, description = "Compiles offline without downloading " +
                                                              "dependencies.")
    private boolean offline;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--exclude", "-e"}, description = "List of modules to be excluded.")
    private String[] excludes;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private boolean experimentalFlag;

    public void execute() {
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(DOC_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }
        
        // check if there are too many arguments.
        if (this.argList != null && this.argList.size() > 1) {
            CommandUtil.printError(this.errStream,
                    "too many arguments.",
                    "ballerina doc [--sourceroot] [--offline]\n" +
                           "                     {<module-name> | -a | --all}",
                    false);
            CommandUtil.exitError(true);
            return;
        }
    
        // if -a or --all flag is not given, then it is mandatory to give a module name.
        if (!this.buildAll && (this.argList == null || this.argList.size() == 0)) {
            CommandUtil.printError(this.errStream,
                    "'doc' command requires a module name. use '-a' or " +
                    "'--all' flag to generate api documentation for all the modules of the project.",
                    "ballerina doc {<module-name> | -a | --all}",
                    false);
            CommandUtil.exitError(true);
            return;
        }
        
        // validation and decide source root and source full path
        this.sourceRootPath = null != this.sourceRoot ?
                Paths.get(this.sourceRoot).toAbsolutePath() : this.sourceRootPath;
        Path sourcePath = null;
        Path targetPath;
        
        // when -a or --all flag is provided. check if the command is executed within a ballerina project. update source
        // root path if command executed inside a project.
        if (this.buildAll) {
            //// validate and set source root path
            if (!ProjectDirs.isProject(this.sourceRootPath)) {
                Path findRoot = ProjectDirs.findProjectRoot(this.sourceRootPath);
                if (null == findRoot) {
                    CommandUtil.printError(this.errStream,
                            "you are not in a Ballerina project.",
                            null,
                            false);
                    CommandUtil.exitError(true);
                    return;
                }
                
                this.sourceRootPath = findRoot;
            }
        
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else if (this.argList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            CommandUtil.printError(this.errStream,
                    "generating API Documentation is not supported for a single Ballerina file.",
                    null,
                    false);
            CommandUtil.exitError(true);
            return;
        } else if (Files.exists(
                this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0))) &&
                   Files.isDirectory(
               this.sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(this.argList.get(0)))) {

            //// check if command executed from project root.
            if (!RepoUtils.isBallerinaProject(this.sourceRootPath)) {
                CommandUtil.printError(this.errStream,
                        "you are trying to generate docs for a module that is not inside a project.",
                        null,
                        false);
                CommandUtil.exitError(true);
                return;
            }
            
            //// check if module name given is not absolute.
            if (Paths.get(argList.get(0)).isAbsolute()) {
                CommandUtil.printError(this.errStream,
                        "you are trying to generate docs for a module by giving the absolute path. " +
                                "you only need give the name of the module.",
                        "ballerina doc <module-name>",
                        true);
                CommandUtil.exitError(true);
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
                        "ballerina doc <module-name>",
                        true);
                CommandUtil.exitError(true);
                return;
            }
    
            targetPath = this.sourceRootPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        } else {
            CommandUtil.printError(this.errStream,
                    "invalid Ballerina source path. It should be a name of a module in a Ballerina project." +
                    " Use the -a or --all " +
                    "flag to generate docs for all modules.",
                    "ballerina doc {<module-name> | -a | --all}",
                    true);
            CommandUtil.exitError(true);
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
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        options.put(SKIP_TESTS, Boolean.toString(true));
        options.put(TEST_ENABLED, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        // create builder context
        BuildContext buildContext = new BuildContext(this.sourceRootPath, targetPath, sourcePath, compilerContext);
        buildContext.setOut(outStream);
        buildContext.setErr(errStream);
        
        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CreateTargetDirTask()) // create target directory.
                .addTask(new CompileTask()) // compile the modules
                .addTask(new CreateDocsTask()) // creates API documentation
                .build();
        
        taskExecutor.executeTasks(buildContext);

        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return DOC_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates API Documentation for Ballerina module(s)/file. \n");
        out.append("\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina doc {<ballerina-file | module-name> | -a | --all} \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
