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

import org.ballerinalang.packerina.TaskExecutor;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.task.CleanTargetDirTask;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.packerina.cmd.Constants.CLEAN_COMMAND;

/**
 * This class represents the "ballerina clean" command.
 *
 * @since 1.0.0
 */
@CommandLine.Command(name = CLEAN_COMMAND, description = "Ballerina clean - Cleans out the target directory of a " +
                                                         "project.")
public class CleanCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private boolean exitWhenFinish;
    
    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;
    
    @CommandLine.Option(names = {"--sourceroot"},
                        description = "Path to the directory containing the ballerina project")
    private String sourceRootPath;

    public CleanCommand(Path sourceRoot, boolean exitWhenFinish) {
        this.sourceRootPath = sourceRoot.toAbsolutePath().toString();
        this.outStream = System.out;
        this.exitWhenFinish = exitWhenFinish;
    }

    public CleanCommand() {
        this.sourceRootPath = Paths.get(System.getProperty("user.dir")).toString();
        this.outStream = System.out;
        this.exitWhenFinish = true;
    }
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND);
            this.outStream.println(commandUsageInfo);
            return;
        }
    
        if (ProjectDirs.isProject(Paths.get(this.sourceRootPath))) {
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new CleanTargetDirTask())
                    .build();
    
            BuildContext buildContext = new BuildContext(Paths.get(this.sourceRootPath));
            taskExecutor.executeTasks(buildContext);
    
            if (this.exitWhenFinish) {
                Runtime.getRuntime().exit(0);
            }
        } else {
            this.outStream.println("'clean' command can only be executed for a Ballerina project.");
            CommandUtil.exitError(this.exitWhenFinish);
        }
    }
    
    @Override
    public String getName() {
        return CLEAN_COMMAND;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Cleans the \"target\" directory of a Ballerina project. \n");
    }
    
    @Override
    public void printUsage(StringBuilder out) {
        out.append(" ballerina clean \n");
    }
    
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
