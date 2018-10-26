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

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.packerina.PushUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.PUSH_COMMAND;
import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

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

    @CommandLine.Option(names = {"--sourceroot"},
            description = "path to the directory containing source files and modules")
    private String sourceRoot;

    @CommandLine.Option(names = {"--no-build"}, description = "skip building before pushing")
    private boolean noBuild;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(PUSH_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }

        if (argList == null || argList.size() == 0) {
            PushUtils.pushAllPackages(sourceRoot, repositoryHome, noBuild);
        } else if (argList.size() == 1) {
            String packageName = argList.get(0);
            PushUtils.pushPackages(packageName, sourceRoot, repositoryHome, noBuild);
        } else {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return PUSH_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("push modules to the ballerina central repository");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina push <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
