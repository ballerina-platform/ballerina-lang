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
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.packerina.BuilderUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 0.90
 */
@Parameters(commandNames = "build", commandDescription = "compile Ballerina program")
public class BuildCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;

    private JCommander parentCmdParser;

    @Parameter(names = {"-c"}, description = "build a compiled package")
    private boolean buildCompiledPkg;

    @Parameter(names = {"-o"}, description = "write output to the given file")
    private String outputFileName;

    @Parameter(names = {"--offline"})
    private boolean offline;

    @Parameter(arity = 1)
    private List<String> argList;

    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Parameter(names = "--java.debug", hidden = true)
    private String debugPort;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "build");
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList == null || argList.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina program given");
        }

        if (argList.size() > 1) {
            throw LauncherUtils.createUsageException("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));
        Path packagePath = Paths.get(argList.get(0));

        Path targetPath = null;
        if (outputFileName != null && !outputFileName.isEmpty()) {
            targetPath = Paths.get(outputFileName);
        }

        BuilderUtils.compileAndWrite(sourceRootPath, packagePath, targetPath, buildCompiledPkg, offline);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return "build";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina sources and writes the output to a file. \n");
        out.append("\n");
        out.append("By default, output filename is the last part of packagename \n");
        out.append("or the filename (minus the extension) with the extension \".balx\". \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to that file. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build <balfile | packagename> [-o output] \n");
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    }
}
