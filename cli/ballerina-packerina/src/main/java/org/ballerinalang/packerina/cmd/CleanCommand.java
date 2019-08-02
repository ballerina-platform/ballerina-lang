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
import org.wso2.ballerinalang.util.RepoUtils;
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
@CommandLine.Command(name = CLEAN_COMMAND, description = "clean build artifact target path")
public class CleanCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;
    
    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CLEAN_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }
    
        // TODO: Set correct source root path.
        Path sourceRoot = Paths.get("");
        if (RepoUtils.isBallerinaProject(sourceRoot)) {
            TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                    .addTask(new CleanTargetDirTask())
                    .build();
    
            BuildContext buildContext = new BuildContext(sourceRoot);
            taskExecutor.executeTasks(buildContext);
            
            Runtime.getRuntime().exit(0);
        } else {
            outStream.println("'clean' command can only be executed from a ballerina project");
            Runtime.getRuntime().exit(1);
        }
    }
    
    @Override
    public String getName() {
        return CLEAN_COMMAND;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("clean out all build artifacts \n");
    }
    
    @Override
    public void printUsage(StringBuilder out) {
        out.append(" ballerina clean \n");
    }
    
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
