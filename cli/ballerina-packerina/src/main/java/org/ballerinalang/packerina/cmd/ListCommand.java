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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.packerina.ListUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class represents the "ballerina list" command.
 *
 * @since 0.970
 */
@Parameters(commandNames = "list", commandDescription = "lists dependencies of packages")
public class ListCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;

    private JCommander parentCmdParser;

    @Parameter(arity = 1)
    private List<String> argList;

    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Parameter(names = "--java.debug", hidden = true)
    private String debugPort;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "list");
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageException("no package given");
        }

        if (argList.size() > 1) {
            throw LauncherUtils.createUsageException("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));

        if (Files.exists(sourceRootPath.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME))) {
            Path packagePath = Paths.get(argList.get(0));
            ListUtils.list(sourceRootPath, packagePath);
        } else {
            throw new BLangCompilerException("Current directory is not a project");
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("lists dependencies of packages \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina list <balfile | packagename> \n");
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    }
}
