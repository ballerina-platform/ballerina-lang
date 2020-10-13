/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.packerina.PushUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static org.ballerinalang.packerina.cmd.Constants.PUSH_COMMAND;

/**
 * This class represents the "ballerina push" command.
 *
 * @since 0.964
 */
@CommandLine.Command(name = PUSH_COMMAND, description = "push modules and binaries available locally to "
        + "Ballerina Central")
public class PushCommand implements BLauncherCmd {
    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = "--repository", hidden = true)
    private String repositoryHome;

    @CommandLine.Option(names = {"--skip-source-check"}, description = "skip checking if source has changed")
    private boolean skipSourceCheck;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;
    
    @CommandLine.Option(names = {"--all", "-a"}, description = "Push all the modules of the project.")
    private boolean pushAll;
    
    private Path userDir;
    private PrintStream errStream;
    
    public PushCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
    }
    
    public PushCommand(Path userDir, PrintStream errStream) {
        this.userDir = userDir;
        this.errStream = errStream;
    }
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PUSH_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }
    
        // Get source root path.
        Path sourceRootPath = userDir;
    
        // Push command only works inside a project
        if (!ProjectDirs.isProject(sourceRootPath)) {
            Path findRoot = ProjectDirs.findProjectRoot(sourceRootPath);
            if (null == findRoot) {
                CommandUtil.printError(errStream,
                        "Push command can be only run inside a Ballerina project",
                        null,
                        false);
                return;
            }
            sourceRootPath = findRoot;
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        if (this.pushAll) {
            // push all modules
            PushUtils.pushAllModules(sourceRootPath);
        } else if (argList != null && argList.size() == 1) {
            String moduleName = argList.get(0);
            PushUtils.pushModules(Collections.singletonList(moduleName), sourceRootPath);
        } else if (argList == null || argList.size() == 0) {
            CommandUtil.printError(errStream,
                    "Push command requires the name of the module. To push all modules use '-a' or '--all' flag.",
                    "ballerina push {<module-name> | -a | --all}",
                    false);
            Runtime.getRuntime().exit(1);
            return;
        } else {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }
        // Exit status, zero for OK, non-zero for error
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return PUSH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("push modules to Ballerina Central");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina push <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
