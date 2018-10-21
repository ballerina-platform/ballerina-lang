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

import org.ballerinalang.compiler.backend.llvm.NativeGen;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.util.BLangConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.BUILD_COMMAND;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;

/**
 * This class represents the "ballerina build" command.
 *
 * @since 0.90
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "build the Ballerina source")
public class BuildCommand implements BLauncherCmd {
    private static final String USER_DIR = "user.dir";
    private static PrintStream outStream = System.err;

    @CommandLine.Option(names = {"-c"}, description = "build a compiled module")
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

    @CommandLine.Option(names = {"--native"}, hidden = true,
                        description = "compile Ballerina program to a native binary")
    private boolean nativeBinary;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-llvm-ir", hidden = true)
    private boolean dumpLLVMIR;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("too many arguments");
        }

        // Get source root path.
        Path sourceRootPath = Paths.get(System.getProperty(USER_DIR));
        if (nativeBinary) {
            genNativeBinary(sourceRootPath, argList);
        } else if (argList == null || argList.size() == 0) {
            // ballerina build
            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, offline, lockEnabled, skiptests);
        } else {
            // ballerina build pkgName [-o outputFileName]
            String targetFileName;
            String pkgName = argList.get(0);
            if (pkgName.endsWith("/")) {
                pkgName = pkgName.substring(0, pkgName.length() - 1);
            }

            Path sourcePath = Paths.get(pkgName);

            // Normalize the source path to remove './' or '.\' characters that can appear before the name
            pkgName = sourcePath.normalize().toString();

            if (outputFileName != null && !outputFileName.isEmpty()) {
                targetFileName = outputFileName;
            } else {
                targetFileName = pkgName;
            }

            Path resolvedFullPath = sourceRootPath.resolve(sourcePath);
            // If the source is a single bal file which is not inside a project
            if (Files.isRegularFile(resolvedFullPath) &&
                    sourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                    !RepoUtils.hasProjectRepo(sourceRootPath)) {
                // If there is no output file name provided, then the executable (balx) should be created in the user's
                // current directory with the same source file name with a balx extension. So if there's no output file
                // name provided (with flag -o) and the target file name contains the full path (along with its parent
                // path) and not only the source file name, then we extract the name of the source file to be the target
                // file name
                targetFileName = getTargetFileName(Paths.get(targetFileName));

                // The source root path should be the parent of the source file
                Path parent = resolvedFullPath.getParent();
                sourceRootPath = parent != null ? parent : sourceRootPath;

                // The module name/source should be the source file name
                Path resolvedFileName = resolvedFullPath.getFileName();
                pkgName = resolvedFileName != null ? resolvedFileName.toString() : pkgName;

            } else if (Files.isDirectory(sourceRootPath)) { // If the source is a module from a project
                // Checks if the source is a module and if its inside a project (with a .ballerina folder)
                if (Files.isDirectory(resolvedFullPath) && !RepoUtils.hasProjectRepo(sourceRootPath)) {
                    throw LauncherUtils.createLauncherException("did you mean to build the module ? If so build " +
                                                                        "from the project folder");
                }
                if (Files.isRegularFile(resolvedFullPath) && !sourcePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                    throw LauncherUtils.createLauncherException("only modules and " + BLANG_SRC_FILE_SUFFIX + " " +
                                                                "files can be used with the 'ballerina build' " +
                                                                        "command.");
                }
                // If we are trying to run a bal file inside a module from a project directory an error is thrown.
                // To differentiate between top level bals and bals inside modules we need to check if the parent of
                // the sourcePath given is null. If it is null then its a top level bal else its a bal inside a module
                Path parentPath = sourcePath.getParent();
                if (Files.isRegularFile(resolvedFullPath) && sourcePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX) &&
                        parentPath != null) {
                    throw LauncherUtils.createLauncherException("you are trying to build a ballerina file inside a " +
                                                                        "module within a project. Try running " +
                                                                        "'ballerina build <module-name>'");
                }
            } else {
                // Invalid source file provided
                throw LauncherUtils.createLauncherException("invalid ballerina source path, it should either be a " +
                                                                    "directory or a file  with a \'"
                                                            + BLangConstants.BLANG_SRC_FILE_SUFFIX + "\' extension");
            }
            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, pkgName, targetFileName, buildCompiledPkg,
                                                  offline, lockEnabled, skiptests);
        }
        Runtime.getRuntime().exit(0);
    }

    /**
     * Get the target file path for a single bal file.
     *
     * @param targetPath target path given
     * @return actual target path
     */
    private String getTargetFileName(Path targetPath) {
        if (outputFileName == null && targetPath.getParent() != null) {
            Path targetFileName = targetPath.getFileName();
            if (targetFileName != null) {
                return targetFileName.toString();
            }
        }
        return targetPath.toString();
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina sources and writes the output to a file. \n");
        out.append("\n");
        out.append("By default, output filename is the last part of module name \n");
        out.append("or the filename (minus the extension) with the extension \".balx\". \n");
        out.append("\n");
        out.append("If the output file is specified with the -o flag, the output \n");
        out.append("will be written to that file. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina build <balfile | module-name> [-o output] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(CommandLine selfCmdParser) {
    }

    private void genNativeBinary(Path projectDirPath, List<String> argList) {
        if (argList == null || argList.size() != 1) {
            throw LauncherUtils.createUsageExceptionWithHelp("no Ballerina program given");
        }
        String programName = argList.get(0);

        // TODO Check whether we need to remove last slash from program name.
        NativeGen.genBinaryExecutable(projectDirPath, programName, outputFileName,
                offline, lockEnabled, dumpBIR, dumpLLVMIR);
    }
}
