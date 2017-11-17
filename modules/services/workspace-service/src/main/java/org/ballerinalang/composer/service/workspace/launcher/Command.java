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
import org.ballerinalang.composer.service.workspace.rest.datamodel.BallerinaFile;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.tree.TopLevelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private String packageDir = null;
    private String packagePath = null;
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
        List<String> commandList = new ArrayList<>();
        String scriptLocation = getScriptLocation();

        // path to ballerina
        String ballerinaExecute = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator +
                                  "ballerina";

        if (LaunchUtils.isWindows()) {
            ballerinaExecute += ".bat";
        }
        commandList.add(ballerinaExecute);
        commandList.add("run");

        BallerinaFile ballerinaFile = WorkspaceUtils.getBallerinaFile(filePath, fileName);
        // assuming there will be only one compilation unit in the list, I'm getting the first element from the list
        BLangCompilationUnit currentBLangCompilationUnit = ballerinaFile.getBLangPackage().compUnits.get(0);
        List<TopLevelNode> topLevelNodes = currentBLangCompilationUnit.getTopLevelNodes();
        // filter out the BLangPackageDeclaration from top level nodes list
        List<TopLevelNode> bLangPackageDeclarations = topLevelNodes.stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangPackageDeclaration).collect(Collectors.toList());
        if (!bLangPackageDeclarations.isEmpty()) {
            BLangPackageDeclaration bLangPackageDeclaration = (BLangPackageDeclaration) bLangPackageDeclarations.get(0);
            if (bLangPackageDeclaration != null) {
                List<String> pkgNameCompsInString = bLangPackageDeclaration.pkgNameComps.stream()
                        .map(WorkspaceUtils.B_LANG_IDENTIFIER_TO_STRING).collect(Collectors.<String>toList());
                if (!(pkgNameCompsInString.size() == 1 && ".".equals(pkgNameCompsInString.get(0)))) {
                    packagePath = String.join(File.separator, pkgNameCompsInString);
                    packageDir = Utils.getProgramDirectory(
                            pkgNameCompsInString.size(), Paths.get(scriptLocation)
                    ).toString();
                }
            }
        }

        if (packagePath == null) {
            commandList.add(scriptLocation);
        } else {
            commandList.add(packagePath);
        }

        if (debug) {
            commandList.add("--ballerina.debug");
            commandList.add(String.valueOf(this.port));
        }

        if (this.commandArgs != null) {
            commandList.addAll(Arrays.asList(this.commandArgs));
        }

        return commandList.toArray(new String[0]);
    }

    public String getPackageDir() {
        return this.packageDir;
    }

    public String getCommandIdentifier() {
        if (this.packagePath == null) {
            return this.getScriptLocation();
        } else {
            return this.packagePath;
        }
    }

    public String getScriptLocation() {
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
