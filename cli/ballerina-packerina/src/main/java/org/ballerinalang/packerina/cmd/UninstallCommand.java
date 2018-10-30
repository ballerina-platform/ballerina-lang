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

import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.packerina.UninstallUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.UNINSTALL_COMMAND;

/**
 * This class represents the "ballerina uninstall" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = UNINSTALL_COMMAND, description = "Uninstalls modules from the user repository")
public class UninstallCommand implements BLauncherCmd {

    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(UNINSTALL_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageExceptionWithHelp("no module given");
        }

        if (argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }
        String packageStr = argList.get(0);
        UninstallUtils.uninstallPackage(packageStr);
    }

    @Override
    public String getName() {
        return UNINSTALL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("uninstall modules from the user repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina uninstall <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
