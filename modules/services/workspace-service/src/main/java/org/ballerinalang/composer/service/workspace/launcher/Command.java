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

package org.ballerinalang.composer.service.workspace.launcher;

import org.ballerinalang.composer.service.workspace.common.Utils;
import org.ballerinalang.composer.service.workspace.launcher.util.LaunchUtils;
import org.ballerinalang.model.BallerinaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Command class represent the launcher commands.
 */
public class Command {

    private String fileName;
    private String filePath;
    private boolean debug = false;
    private String commandArgs;
    private LauncherConstants.ProgramType type;
    private int port;
    private Process program;
    private boolean errorOutputEnabled = true;
    private String packageDir = null;
    private String packagePath = null;
    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    public Command(LauncherConstants.ProgramType type, String fileName, String filePath, boolean debug) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.debug = debug;
        this.type = type;

        if (debug) {
            this.port = LaunchUtils.getFreePort();
        }
    }

    public Command(LauncherConstants.ProgramType type, String fileName, String filePath, String commandArgs, boolean
            debug) {
        this(type, fileName, filePath, debug);
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

    public LauncherConstants.ProgramType getType() {
        return type;
    }

    public void setType(LauncherConstants.ProgramType type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(String commandArgs) {
        this.commandArgs = commandArgs;
    }

    @Override
    public String toString() {
        String ballerinaBin, ballerinaCommand, programType, scriptLocation, debugSwitch = "",
                commandArgs = "";
        int port = -1;

        // path to bin directory
        ballerinaBin = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator;

        if (LaunchUtils.isWindows()) {
            ballerinaCommand = "ballerina.bat run ";
        } else {
            ballerinaCommand = "ballerina run ";
        }

        if (type == LauncherConstants.ProgramType.RUN) {
            programType = "main ";
        } else {
            programType = "service ";
        }

        scriptLocation = getScript();

        if (debug) {
            debugSwitch = " --ballerina.debug " + this.port;
        }

        if (this.commandArgs != null) {
            commandArgs = " " + this.commandArgs;
        }

        try {
            BallerinaFile bFile = Utils.getBFile(scriptLocation);
            String packageName = bFile.getPackagePath();
            if (!".".equals(packageName)) {
                packagePath = bFile.getPackagePath().replace(".", File.separator);
                packageDir = Utils.getProgramDirectory(bFile, scriptLocation).toString();
            }
        } catch (IOException e) {
            logger.warn("could not find package name");
        }

        if (packagePath == null) {
            return ballerinaBin + ballerinaCommand + programType + scriptLocation + debugSwitch + commandArgs;
        } else {
            return ballerinaBin + ballerinaCommand + programType + " " + packagePath + debugSwitch
                    + commandArgs;
        }

    }

    public String getPackageDir() {
        return this.packageDir;
    }

    public String getCommandIdentifier() {
        String ballerinaCommand, programType;
        if (this.packagePath == null) {
            return this.getScript();
        } else {
            return this.packagePath;
        }
    }

    public String getScript() {
        return this.filePath + File.separator + fileName;
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
