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
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.Parameters;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.testerina.util.Utils;
import org.ballerinalang.util.VMOptions;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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


    private JCommander parentCmdParser;

    @Parameter(arity = 1, description = "ballerina package/s to be tested")
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

    public void execute() {
        if (helpFlag) {
            printCommandUsageInfo(parentCmdParser, "test");
            return;
        }

        if (sourceFileList == null || sourceFileList.isEmpty()) {
            Path userDir = Paths.get(System.getProperty("user.dir"));
            SourceDirectory srcDirectory = new FileSystemProjectDirectory(userDir);
            sourceFileList = srcDirectory.getSourcePackageNames();
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

        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        // Setting the source root so it can be accessed from anywhere
        System.setProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT, sourceRootPath.toString());
        try {
            ConfigRegistry.getInstance().initRegistry(runtimeParams, configFilePath, null);
            ((BLogManager) LogManager.getLogManager()).loadUserProvidedLogConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("failed to read the specified configuration file: " + configFilePath, e);
        }

        Path[] paths = sourceFileList.stream().map(Paths::get).toArray(Path[]::new);

        BTestRunner testRunner = new BTestRunner();
        if (listGroups) {
            testRunner.listGroups(sourceRootPath.toString(), paths);
            Runtime.getRuntime().exit(0);
        }
        if (disableGroupList != null) {
            testRunner.runTest(sourceRootPath.toString(), paths, disableGroupList, false);
        } else {
            testRunner.runTest(sourceRootPath.toString(), paths, groupList, true);
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
        out.append("Complies and Run Ballerina test sources (*_test.bal) and prints " + System.lineSeparator());
        out.append("a summary of test results" + System.lineSeparator() + System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("ballerina test <filename>" + System.lineSeparator());
        stringBuilder.append("ballerina test command will have -mock flag enabled by default" + System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    private static void printCommandUsageInfo(JCommander cmdParser, String commandName) {
        StringBuilder out = new StringBuilder();
        JCommander jCommander = cmdParser.getCommands().get(commandName);
        BLauncherCmd bLauncherCmd = (BLauncherCmd) jCommander.getObjects().get(0);

        out.append(cmdParser.getCommandDescription(commandName)).append("\n");
        out.append("\n");
        out.append("Usage:\n");
        bLauncherCmd.printUsage(out);
        out.append("\n");

        if (jCommander.getCommands().values().size() != 0) {
            out.append("Available Commands:\n");
            printCommandList(jCommander, out);
            out.append("\n");
        }

        printFlags(jCommander.getParameters(), out);
        errStream.println(out.toString());
    }

    private static void printCommandList(JCommander cmdParser, StringBuilder out) {
        int longestNameLen = 0;
        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("default-cmd") || cmd.getName().equals("help")) {
                continue;
            }

            int length = cmd.getName().length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
        }

        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("default-cmd") || cmd.getName().equals("help")) {
                continue;
            }

            String cmdName = cmd.getName();
            String cmdDesc = cmdParser.getCommandDescription(cmdName);

            int noOfSpaces = longestNameLen - (cmd.getName().length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(cmdName).append(new String(charArray)).append(cmdDesc).append("\n");
        }
    }

    private static void printFlags(List<ParameterDescription> paramDescs, StringBuilder out) {
        int longestNameLen = 0;
        int count = 0;
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }

            String names = parameterDesc.getNames();
            int length = names.length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
            count++;
        }

        if (count == 0) {
            return;
        }
        out.append("Flags:\n");
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }
            String names = parameterDesc.getNames();
            String desc = parameterDesc.getDescription();
            int noOfSpaces = longestNameLen - (names.length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(names).append(new String(charArray)).append(desc).append("\n");
        }
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
        // ignore

    }
}
