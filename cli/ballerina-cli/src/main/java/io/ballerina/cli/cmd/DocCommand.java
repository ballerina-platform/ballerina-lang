/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.cmd;

import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateDocsTask;
import io.ballerina.cli.task.CreateTargetDirTask;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.cli.cmd.Constants.DOC_COMMAND;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * This class represents the "ballerina doc" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = DOC_COMMAND, description = "Ballerina doc - Generates API Documentation")
public class DocCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private Path projectPath;
    private Path outputPath;
    private boolean exitWhenFinish;

    public DocCommand() {
        this.projectPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    public DocCommand(PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.projectPath = Paths.get(System.getProperty("user.dir"));
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @CommandLine.Option(names = {"--o", "-o"}, description = "Location to save API Docs.")
    private String outputLoc;

    @CommandLine.Option(names = {"--excludeIndex", "-excludeIndex"}, description = "Prevents project index from " +
            "being generated.")
    private boolean excludeIndex;

    @CommandLine.Option(names = {"--combine", "-combine"}, description = "Creates index using modules.")
    private boolean combine;

    @CommandLine.Option(names = {"--offline"}, description = "Compiles offline without downloading " +
            "dependencies.")
    private boolean offline;

    @CommandLine.Option(names = "--old-parser", description = "Enable old parser.", hidden = true)
    private boolean useOldParser;

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
        if (argList == null) {
            this.projectPath = Paths.get(System.getProperty("user.dir"));
        } else {
            this.projectPath = Paths.get(argList.get(0));
        }
        // combine docs
        if (this.combine) {
            outStream.println("Combining Docs");
            BallerinaDocGenerator.mergeApiDocs(this.projectPath.toString());
            if (this.exitWhenFinish) {
                Runtime.getRuntime().exit(0);
            }
        }

        if (this.argList != null && this.argList.get(0).endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
            CommandUtil.printError(this.errStream,
                    "generating API Documentation is not supported for a single Ballerina file.",
                    null,
                    false);
            CommandUtil.exitError(true);
            return;
        }

        // check if there are too many arguments.
        if (this.argList != null && this.argList.size() > 1) {
            CommandUtil.printError(this.errStream,
                    "too many arguments.",
                    "ballerina doc <project_path> [--offline]\n",
                    false);
            CommandUtil.exitError(true);
            return;
        }

        // load project
        Project project;
        try {
            project = BuildProject.load(this.projectPath);
        } catch (RuntimeException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }

        // normalize paths
        this.projectPath = this.projectPath.normalize();
        this.outputPath = this.outputLoc != null ? Paths.get(this.outputLoc).toAbsolutePath() : null;

        // create compiler context
        CompilerContext compilerContext = project.projectEnvironmentContext().getService(CompilerContext.class);
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(OFFLINE, Boolean.toString(this.offline));
        options.put(LOCK_ENABLED, "true");
        options.put(SKIP_TESTS, "true");
        options.put(TEST_ENABLED, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(this.experimentalFlag));
        options.put(PRESERVE_WHITESPACE, "true");

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                .addTask(new CreateTargetDirTask()) // create target directory.
                //.addTask(new ResolveMavenDependenciesTask()) // resolve maven dependencies in Ballerina.toml
                .addTask(new CompileTask(outStream, errStream)) // compile the modules
                .addTask(new CreateDocsTask(excludeIndex, outStream, outputPath)) // creates API documentation
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    @Override
    public String getName() {
        return DOC_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates API Documentation for Ballerina projects. \n");
        out.append("\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina doc \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
