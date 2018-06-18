/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.testerina.util.Utils;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.VMOptions;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_DEBUG;

/**
 * Test command for ballerina launcher.
 */
@Parameters(commandNames = "test", commandDescription = "test Ballerina program")
public class TestCmd implements BLauncherCmd {

    private static final PrintStream errStream = System.err;
    private static final PrintStream outStream = System.out;

    @Parameter(arity = 1, description = "ballerina package/files to be tested")
    private List<String> sourceFileList;

    @Parameter(names = { "--help", "-h" }, hidden = true)
    private boolean helpFlag;

    @Parameter(names = {"--sourceroot"}, description = "path to the directory containing source files and packages")
    private String sourceRoot;

    @DynamicParameter(names = "-e", description = "Ballerina environment parameters")
    private Map<String, String> runtimeParams = new HashMap<>();

    @DynamicParameter(names = "-B", description = "Ballerina VM options")
    private Map<String, String> vmOptions = new HashMap<>();

    @Parameter(names = {"--config", "-c"}, description = "path to the testerina configuration file")
    private String configFilePath;

    @Parameter(names = "--debug", description = "remote debug testerina programs")
    private String debugPort;

    // Testerina Flags
    @Parameter(names = {"--list-groups", "-lg"}, description = "list the groups available in the tests")
    private boolean listGroups;

    @Parameter(names = "--groups", description = "test groups to be executed")
    private List<String> groupList;

    @Parameter(names = "--disable-groups", description = "test groups to be disabled")
    private List<String> disableGroupList;

    @Parameter(names = "--exclude-packages", description = "packages to be excluded")
    private List<String> excludedPackageList;

    public void execute() {
        if (helpFlag) {
            outStream.println(BLauncherCmd.getCommandUsageInfo("test"));
            return;
        }

        if (sourceFileList != null && sourceFileList.size() > 1) {
            throw LauncherUtils.createUsageException("Too many arguments. You can only provide a single package or a" +
                                                     " single file to test command");
        }

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        SourceDirectory srcDirectory = null;
        if (sourceFileList == null || sourceFileList.isEmpty()) {
            srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
            sourceFileList = srcDirectory.getSourcePackageNames();
        } else if (sourceFileList.get(0).endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            Path sourceFilePath = Paths.get(sourceFileList.get(0));
            if (sourceFilePath.isAbsolute()) {
                sourceRootPath = sourceFilePath.getParent();
            } else {
                sourceRootPath = sourceRootPath.resolve(sourceFilePath).getParent();
            }
            Path fileName = sourceFilePath.getFileName();
            if (fileName == null) {
                throw new BallerinaIOException("Provided ballerina file doesn't exist!");
            }
            sourceFileList.clear();
            sourceFileList.add(fileName.toString());
        } else {
            srcDirectory = new FileSystemProjectDirectory(sourceRootPath);
        }

        if (groupList != null && disableGroupList != null) {
            throw LauncherUtils
                    .createUsageException("Cannot specify both --groups and --disable-groups flags at the same time");
        }

        // Enable remote debugging
        if (null != debugPort) {
            System.setProperty(SYSTEM_PROP_BAL_DEBUG, debugPort);
        }
        // Setting the vm options
        VMOptions.getInstance().addOptions(vmOptions);

        // Setting the source root so it can be accessed from anywhere
        System.setProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT, sourceRootPath.toString());
        try {
            ConfigRegistry.getInstance().initRegistry(runtimeParams, configFilePath, null);
            ((BLogManager) LogManager.getLogManager()).loadUserProvidedLogConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("failed to read the specified configuration file: " + configFilePath, e);
        }

        Path[] paths = sourceFileList.stream()
                .filter(source -> excludedPackageList == null || !excludedPackageList.contains(source))
                .map(Paths::get)
                .sorted()
                .toArray(Path[]::new);

        if (srcDirectory != null) {
            Utils.setManifestConfigs();
        }
        BTestRunner testRunner = new BTestRunner();
        if (listGroups) {
            testRunner.listGroups(sourceRootPath.toString(), paths);
            Runtime.getRuntime().exit(0);
        }
        if (disableGroupList != null) {
            testRunner.runTest(sourceRootPath.toString(), paths, disableGroupList, false, false);
        } else {
            testRunner.runTest(sourceRootPath.toString(), paths, groupList, false);
        }
        if (testRunner.getTesterinaReport().isFailure()) {
            Utils.cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            Runtime.getRuntime().exit(1);
        }
        Utils.cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
        Runtime.getRuntime().exit(0);
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
        // ignore

    }
}
