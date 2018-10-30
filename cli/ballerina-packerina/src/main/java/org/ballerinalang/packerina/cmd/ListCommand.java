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
import org.ballerinalang.packerina.ListUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.launcher.LauncherUtils.createLauncherException;
import static org.ballerinalang.packerina.cmd.Constants.LIST_COMMAND;

/**
 * This class represents the "ballerina list" command.
 *
 * @since 0.970
 */
@CommandLine.Command(name = LIST_COMMAND, description = "list dependencies of modules")
public class ListCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(LIST_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));

        if (argList == null || argList.size() == 0) {
            // ballerina list
            ListUtils.list(sourceRootPath);
        } else {
            if (Files.exists(sourceRootPath.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME))) {
                String pkgName = argList.get(0);
                ListUtils.list(sourceRootPath, pkgName);
            } else {
                throw createLauncherException("Current directory is not a project");
            }
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return LIST_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("lists dependencies of modules \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina list <module-name> \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
