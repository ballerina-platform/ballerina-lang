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

import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Launcher Terminator Implementation for Mac.
 */
public class TerminatorMac extends TerminatorUnix {

    /**
     * Get find process command.
     *
     * @param script absolute path of ballerina file running
     * @return find process command
     */
    private String[] getFindProcessCommand(String script) {
        String[] cmd = {
                "/bin/sh", "-c", "ps ax | grep " + script + " | grep -v 'grep' | awk '{print $1}'"
        };
        return cmd;
    }

    public void terminate() {
        terminate(LS_PROCESS_ID);
        terminate(DEBUG_PROCESS_ID);
    }

    /**
     * Terminates a given ballerina program.
     */
    private void terminate(String processName) {
        int processID;
        String[] findProcessCommand = getFindProcessCommand(processName);
        BufferedReader reader = null;
        try {
            Process findProcess = Runtime.getRuntime().exec(findProcessCommand);
            findProcess.waitFor();
            reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream(), Charset.defaultCharset()));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processID = Integer.parseInt(line);
                    killChildProcesses(processID);
                    kill(processID);
                } catch (Throwable e) {
                    LOGGER.error("Launcher was unable to kill process " + line + ".");
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Launcher was unable to find the process ID for " + processName + ".");
        } finally {
            if (reader != null) {
                IOUtils.closeQuietly(reader);
            }
        }
    }
}
