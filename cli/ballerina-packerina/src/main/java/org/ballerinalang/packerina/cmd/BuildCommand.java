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
import org.ballerinalang.packerina.BuilderUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.BUILD_COMMAND;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "build the Ballerina source")
public class BuildCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;

    @CommandLine.Option(names = {"-c"}, description = "build a compiled package")
    private boolean buildCompiledPkg;

    @CommandLine.Option(names = {"-o"}, description = "write output to the given file")
    private String outputFileName;

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = {"--lockEnabled"})
    private boolean lockEnabled;

    @CommandLine.Option(names = {"--skiptests"})
    private boolean skiptests;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--java.debug", hidden = true)
    private String debugPort;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createUsageException("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));
        if (argList == null || argList.size() == 0) {
            // ballerina build
            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, offline, lockEnabled, skiptests);
        } else {
            // ballerina build pkgName [-o outputFileName]
            String targetFileName;
            Path sourcePath = Paths.get(argList.get(0));
            if (outputFileName != null && !outputFileName.isEmpty()) {
                targetFileName = outputFileName;
            } else {
                targetFileName = sourcePath.toString();
            }

            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, sourcePath.toString(), targetFileName,
                                                  buildCompiledPkg, offline, lockEnabled, skiptests);
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
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
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }
}
