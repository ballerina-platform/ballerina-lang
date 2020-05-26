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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Abstract terminator implementation.
 */
public abstract class Terminator {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Terminator.class);

    /**
     * Terminates all the processes and sub-processes for a given process identifier.
     *
     * @param processIdentifier process identifier string.
     */
    public abstract void terminate(String processIdentifier);

    /**
     * Kills its process for a given PID.
     *
     * @param pid - process id
     */
    public void kill(int pid) {
        // Todo - need to put additional validation.
        if (pid < 0) {
            return;
        }
        String killCommand = String.format("kill -9 %d", pid);
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
        } catch (Throwable e) {
            LOGGER.error("error occurred when trying to to kill the process:" + pid + ".", e);
        }
    }

    /**
     * Terminates all the child processes for a given pid.
     *
     * @param pid - process id
     */
    protected void killChildProcesses(int pid) {
        BufferedReader reader = null;
        try {
            Process findChildProcess = Runtime.getRuntime().exec(String.format("pgrep -P %d", pid));
            findChildProcess.waitFor();
            reader = new BufferedReader(new InputStreamReader(findChildProcess.getInputStream(),
                    Charset.defaultCharset()));
            String line;
            int childProcessID;
            while ((line = reader.readLine()) != null) {
                childProcessID = Integer.parseInt(line);
                kill(childProcessID);
            }
        } catch (Throwable e) {
            LOGGER.error("Error occurred when trying to find child processes of the process:" + pid + ".");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
