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
import org.ballerinalang.packerina.PushUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.INSTALL_COMMAND;

/**
 * This class represents the "ballerina install" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = INSTALL_COMMAND, description = "install packages to the home repository")
public class InstallCommand implements BLauncherCmd {

    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--java.debug", hidden = true)
    private String debugPort;

    @CommandLine.Option(names = {"--sourceroot"},
            description = "path to the directory containing source files and packages")
    private String sourceRoot;

    @Parameter(names = {"--y"}, description = " Assume yes to all queries and do not prompt confirmation when " +
            "installing the package to the home repository")
    private boolean installToHome;

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(INSTALL_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            PushUtils.pushAllPackages(sourceRoot, "home", installToHome);
        } else if (argList.size() == 1) {
            String packageStr = argList.get(0);
            PushUtils.pushPackages(packageStr, sourceRoot, "home", installToHome);
        } else {
            throw LauncherUtils.createUsageException("too many arguments");
        }
    }

    @Override
    public String getName() {
        return INSTALL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("install packages to the home repository \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina install <package-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
