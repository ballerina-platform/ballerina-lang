/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package io.ballerina.plugins.idea.preloading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher Terminator Implementation for Windows. ( Xp professional SP2++).
 */
public class TerminatorWindows implements Terminator {
    private final String processIdentifier = "org.ballerinalang.langserver.launchers.stdio.Main";
    private static final Logger LOGGER = LoggerFactory.getLogger(TerminatorWindows.class);

    /**
     * @return file process command.
     */
    private String getFindProcessCommand() {
        // Escapes forward slashes.
        return "cmd /c wmic.exe Process where \"Commandline like '%" + processIdentifier + "%'\" CALL TERMINATE";
    }

    /**
     * Terminate running ballerina program.
     */
    public void terminate() {
        String findProcessCommand = getFindProcessCommand();
        try {
            Process findProcess = Runtime.getRuntime().exec(findProcessCommand);
            findProcess.waitFor();
        } catch (Throwable e) {
            LOGGER.error("Launcher was unable to find the process ID for " + processIdentifier + ".");
        }
    }
}
