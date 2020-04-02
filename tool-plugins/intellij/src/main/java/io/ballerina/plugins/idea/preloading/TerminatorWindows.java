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

/**
 * Launcher Terminator Implementation for Windows. ( Xp professional SP2++).
 */
public class TerminatorWindows extends Terminator {

    /**
     * @return file process command.
     */
    private String getFindProcessCommand(String processName) {
        // Escapes forward slashes.
        return "cmd /c wmic.exe Process where \"Commandline like '%" + processName + "%'\" CALL TERMINATE";
    }

    public void terminate() {
        terminate(LS_SCRIPT_PROCESS_ID);
        terminate(LS_CMD_PROCESS_ID);
        terminate(DEBUG_SCRIPT_PROCESS_ID);
    }

    /**
     * Terminates a given ballerina process.
     */
    private void terminate(String processName) {
        String findProcessCommand = getFindProcessCommand(processName);
        try {
            Process findProcess = Runtime.getRuntime().exec(findProcessCommand);
            findProcess.waitFor();
        } catch (Throwable e) {
            LOGGER.error("Launcher was unable to find the process ID for " + processName + ".");
        }
    }
}
