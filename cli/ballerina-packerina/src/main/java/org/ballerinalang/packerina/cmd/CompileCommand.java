/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.util.BLangConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.packerina.cmd.Constants.COMPILE_COMMAND;
import static org.ballerinalang.util.BLangConstants.BALLERINA_TARGET;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.JVM_TARGET;

/**
 * Compile Ballerina modules in to balo.
 *
 * @since 0.992.0
 */
@CommandLine.Command(name = COMPILE_COMMAND, description = "Compile Ballerina modules")
public class CompileCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private boolean exitWhenFinish;


    public static final boolean GEN_EXECUTABLES = false;

    public CompileCommand() {
        userDir = Paths.get(System.getProperty("user.dir"));
        errStream = System.err;
        exitWhenFinish = true;
    }

    public CompileCommand(Path userDir, PrintStream errStream, boolean exitWhenFinish) {
        this.userDir = userDir;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
    }


    @CommandLine.Option(names = {"-c"}, description = "build a compiled module")
    private boolean buildCompiledPkg;

    @CommandLine.Option(names = {"-o"}, description = "write output to the given file")
    private String outputFileName;

    @CommandLine.Option(names = {"--offline"})
    private boolean offline;

    @CommandLine.Option(names = {"--lockEnabled"})
    private boolean lockEnabled;

    @CommandLine.Option(names = {"--skip-tests"})
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

    @CommandLine.Option(names = {"--jvmTarget"}, hidden = true,
            description = "compile Ballerina program to a jvm class")
    private boolean jvmTarget;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "enable experimental language features")
    private boolean experimentalFlag;

    @CommandLine.Option(names = {"--config"}, description = "path to the configuration file")
    private String configFilePath;

    @CommandLine.Option(names = "--siddhi-runtime", description = "enable siddhi runtime for stream processing")
    private boolean siddhiRuntimeFlag;

    public void execute() {
        // ToDo: We will temporarily disable old code gen and tests
        jvmTarget = true;
        skiptests = true;

        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMPILE_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina compile [<module-name>]",
                    true);
        }

        // Get source root path.
        Path sourceRootPath = userDir;

        // Compile command only works inside a project
        if (!ProjectDirs.isProject(sourceRootPath)) {
            Path findRoot = ProjectDirs.findProjectRoot(sourceRootPath);
            if (null == findRoot) {
                CommandUtil.printError(errStream,
                        "Compile command can be only run inside a Ballerina project",
                        null,
                        false);
                return;
            }
            sourceRootPath = findRoot;
        }

        if (nativeBinary) {
            genNativeBinary(sourceRootPath, argList);
        } else if (argList == null || argList.size() == 0) {
            // ballerina build
            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, offline, lockEnabled, skiptests, experimentalFlag,
                    siddhiRuntimeFlag, jvmTarget, dumpBIR, GEN_EXECUTABLES);
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

            Path resolvedFullPath = sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                    .resolve(sourcePath);
            // If the source is a single bal file which is not inside a project
            if (Files.isRegularFile(resolvedFullPath) &&
                    sourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
                    !RepoUtils.isBallerinaProject(sourceRootPath)) {
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
                if (Files.isDirectory(resolvedFullPath) && !RepoUtils.isBallerinaProject(sourceRootPath)) {
                    throw LauncherUtils.createLauncherException("you are trying to build a module that is not inside " +
                            "a project. Run `ballerina new` from " + sourceRootPath + " to initialize it as a " +
                            "project and then build the module.");
                }
                if (Files.isRegularFile(resolvedFullPath) && !sourcePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX)) {
                    throw LauncherUtils.createLauncherException("only modules and " + BLANG_SRC_FILE_SUFFIX + " " +
                            "files can be used with the 'ballerina build' " +
                            "command.");
                }

                if (Files.exists(resolvedFullPath)) {
                    if (Files.isRegularFile(resolvedFullPath) && !sourcePath.toString()
                            .endsWith(BLANG_SRC_FILE_SUFFIX)) {
                        throw LauncherUtils.createLauncherException("only modules and " + BLANG_SRC_FILE_SUFFIX + " " +
                                "files can be used with the 'ballerina build' " +
                                "command.");
                    }
                } else {
                    throw LauncherUtils.createLauncherException("ballerina source does not exist '" + sourcePath + "'");
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

            // Load the configuration file. If no config file is given then the default config file i.e.
            // "ballerina.conf" in the source root path is taken.
            LauncherUtils.loadConfigurations(sourceRootPath, configFilePath);

            BuilderUtils.compileWithTestsAndWrite(sourceRootPath, pkgName, targetFileName, buildCompiledPkg,
                    offline, lockEnabled, skiptests, experimentalFlag, siddhiRuntimeFlag,
                    jvmTarget || JVM_TARGET.equals(System.getProperty(BALLERINA_TARGET)),
                    dumpBIR, GEN_EXECUTABLES);
        }
    
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
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
        return COMPILE_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Compiles Ballerina modules and create balo files. \n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina compile [<module-name>] \n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void genNativeBinary(Path projectDirPath, List<String> argList) {
        throw LauncherUtils.createLauncherException("llvm native generation is not supported");
    }
}
