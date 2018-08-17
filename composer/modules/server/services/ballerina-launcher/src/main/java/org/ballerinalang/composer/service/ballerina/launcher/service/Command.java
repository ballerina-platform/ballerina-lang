/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.ballerina.launcher.service;

import org.ballerinalang.composer.service.ballerina.launcher.service.util.LaunchUtils;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command class represent the launcher commands.
 */
public class Command {

    private String fileName;
    private String filePath;
    private boolean debug = false;
    private String[] commandArgs;
    private int port;
    private Process program;
    private boolean errorOutputEnabled = true;
    private String sourceRoot = null;
    private String packageName = null;
    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    public Command(String fileName, String filePath, boolean debug) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.debug = debug;

        if (debug) {
            this.port = LaunchUtils.getFreePort();
        }
    }

    public Command(String fileName, String filePath, String[] commandArgs, boolean debug) {
        this(fileName, filePath, debug);
        this.commandArgs = commandArgs;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(String[] commandArgs) {
        this.commandArgs = commandArgs;
    }

    /**
     * Construct the command array to be executed.
     * @return String[] command array
     */
    public String[] getCommandArray() {
        boolean isTestFile = false;
        List<String> commandList = new ArrayList<>();

        // path to ballerina
        String ballerinaExecute = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator +
                "ballerina";

        if (LaunchUtils.isWindows()) {
            ballerinaExecute += ".bat";
        }

        commandList.add(ballerinaExecute);
        sourceRoot = LSCompilerUtil.getSourceRoot(Paths.get(filePath + fileName));
        if (ProjectDirConstants.TEST_DIR_NAME.equals(Paths.get(filePath).toFile().getName())) {
            isTestFile = true;
        }

        if (!isTestFile) {
            commandList.add("run");
        } else {
            commandList.add("test");
        }

        if (filePath != null && !filePath.equals(sourceRoot + File.separator)) {
            packageName =
                    LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, filePath + fileName);
            commandList.add(packageName);
            commandList.add("--sourceroot");
            commandList.add(sourceRoot);
        } else {
            commandList.add(getScriptName());
        }

        if (debug) {
            commandList.add("--debug");
            commandList.add(String.valueOf(this.port));
        }

        if (!isTestFile) {
            commandList.add("-e");
            commandList.add("b7a.http.tracelog.host=localhost");

            commandList.add("-e");
            commandList.add("b7a.http.tracelog.port=5010");
        }

        if (this.commandArgs != null) {
            commandList.addAll(Arrays.asList(this.commandArgs));
        }

        return commandList.toArray(new String[0]);
    }

    public String getSourceRoot() {
        return this.sourceRoot;
    }

    public String getCommandIdentifier() {
        if (this.packageName == null) {
            return this.getScriptName();
        } else {
            return this.packageName;
        }
    }

    public String getScriptName() {
        return fileName;
    }

    public void setProgram(Process program) {
        this.program = program;
    }

    public Process getProgram() {
        return program;
    }

    public boolean isErrorOutputEnabled() {
        return errorOutputEnabled;
    }

    public void setErrorOutputEnabled(boolean errorOutputEnabled) {
        this.errorOutputEnabled = errorOutputEnabled;
    }
}
