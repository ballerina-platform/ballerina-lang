/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.test.util.terminator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Terminator Implementation for Mac.
 */
public class TerminatorMac extends TerminatorUnix {

    public void terminate(String processIdentifier) {
        int processID;
        String[] processFindCmd = getProcessFindCmd(processIdentifier);
        BufferedReader reader = null;
        try {
            Process findProcess = Runtime.getRuntime().exec(processFindCmd);
            findProcess.waitFor();
            reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream(), Charset.defaultCharset()));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processID = Integer.parseInt(line);
                    killChildProcesses(processID);
                    kill(processID);
                } catch (Throwable e) {
                    LOGGER.error("error occurred when trying to to kill the process:" + processIdentifier + ".", e);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Launcher was unable to find the PID for " + processIdentifier + ".");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Returns all the processes related to the given process identifier.
     *
     * @param processId string identifier for inclusive filtering.
     * @return find process command
     */
    private String[] getProcessFindCmd(String processId) {
        return new String[]{"/bin/sh", "-c", "ps ax | grep " + processId + " | grep -v 'grep' | awk '{print $1}'"};
    }
}
