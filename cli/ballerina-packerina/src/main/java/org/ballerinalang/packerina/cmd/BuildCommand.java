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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.nio.file.Files;
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

    @Parameter(names = {"--sourceroot"}, description = "path to the directory containing source files and packages")
    private String sourceRoot;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "build");
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createUsageException("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        System.setProperty("ballerina.source.root", sourceRootPath.toString());
        if (argList == null || argList.size() == 0) {
            // ballerina build
            BuilderUtils.compileAndWrite(sourceRootPath, offline);
        } else {
            // ballerina build pkgName [-o outputFileName]
            String targetFileName;
            String pkgName = argList.get(0);
            if (pkgName.endsWith("/")) {
                pkgName = pkgName.substring(0, pkgName.length() - 1);
            }
            if (outputFileName != null && !outputFileName.isEmpty()) {
                targetFileName = outputFileName;
            } else {
                targetFileName = pkgName;
            }

            Path sourcePath = Paths.get(pkgName);
            String srcPathStr = sourcePath.toString();
            Path fullPath = sourceRootPath.resolve(sourcePath);
            if (Files.isRegularFile(fullPath) &&
                    srcPathStr.endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                    !RepoUtils.hasProjectRepo(sourceRootPath)) {
                BuilderUtils.compileAndWrite(fullPath.getParent(), fullPath.getFileName().toString(), targetFileName,
                                             buildCompiledPkg, offline);
            } else if (Files.isDirectory(sourceRootPath)) {
                if (Files.isDirectory(fullPath) && !RepoUtils.hasProjectRepo(sourceRootPath)) {
                    throw new BallerinaException("Do you mean to build the ballerina package as a project? If so run" +
                                                 " ballerina init to make it a project with a .ballerina directory");
                }
                BuilderUtils.compileAndWrite(sourceRootPath, pkgName, targetFileName, buildCompiledPkg, offline);
            } else {
                throw new BallerinaException("Invalid Ballerina source path, it should either be a directory or a" +
                                             "file  with a \'" + BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension");
            }
        }
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
