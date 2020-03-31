/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.cmd;

import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import picocli.CommandLine;

import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Class to implement "langserver start" command for ballerina. Ex: ballerina start-langserver [--debug debugPort]
 * [--help|-h]
 *
 * @since 1.1.0
 */
@CommandLine.Command(name = "start-language-server", description = "start Ballerina language server")
public class LangServerStartCmd implements BLauncherCmd {
    private static final String CMD_NAME = "start-language-server";
    private static final String BALLERINA_HOME;

    static {
        BALLERINA_HOME = System.getProperty("ballerina.home");
    }

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        try {
            // Set flags
            System.setProperty("ballerina.home", BALLERINA_HOME);

            // Start Language Server
            LogManager.getLogManager().reset();
            Logger globalLogger = Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
            globalLogger.setLevel(java.util.logging.Level.OFF);
            org.ballerinalang.langserver.launchers.stdio.Main.startServer(System.in, System.out);
        } catch (Throwable e) {
            throw LauncherUtils.createLauncherException("Could not start language server");
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }
}
