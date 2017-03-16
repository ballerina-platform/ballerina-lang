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

import org.apache.commons.io.IOUtils;
import org.ballerinalang.composer.service.workspace.launcher.util.LaunchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Handle program termination
 */
public class Terminator {
    
    private static final Logger logger = LoggerFactory.getLogger(Terminator.class);
    
    private Command command;
    
    public Terminator(Command command) {
        this.command = command;
    }
    
    public void terminate() {
        int pid = findPID(command.getScript());
        kill(pid);
    }
    
    
    private int findPID(String script) {
        int processID = -1;
        String findProcessCommand = getFindProcessCommand();
        BufferedReader reader = null;
        try {
            Process kill = Runtime.getRuntime().exec(findProcessCommand);
            kill.waitFor();
            reader = new BufferedReader(new InputStreamReader(kill.getInputStream(), Charset.defaultCharset()));
            
            String line = "";
            String explode[];
            while ((line = reader.readLine()) != null) {
                if (line.contains(script)) {
                    explode = line.split(" ");
                    processID = Integer.parseInt(explode[0]);
                }
            }
        } catch (Throwable e) {
            logger.error("Launcher was unable to find the process ID for " + script + ".");
        } finally {
            if (reader != null) {
                IOUtils.closeQuietly(reader);
            }
        }
        return processID;
    }
    
    private String getFindProcessCommand() {
        String findProcessCommand = "";
        String jdkHome = System.getProperty("java.command");
        return jdkHome.replaceAll("java$", "jps -m");
    }
    
    private void kill(int pid) {
        //todo need to put aditional validation
        if (pid < 0) {
            return;
        }
        String killCommand = getKillCommand(pid);
        BufferedReader reader = null;
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
            reader = new BufferedReader(new InputStreamReader(kill.getInputStream(), Charset.defaultCharset()));
            
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
            }
        } catch (Throwable e) {
            logger.error("Launcher was unable to terminate process:" + pid + ".");
        } finally {
            if (reader != null) {
                IOUtils.closeQuietly(reader);
            }
        }
    }
    
    private String getKillCommand(int pid) {
        String killCommand = "";
        if (LaunchUtils.isWindows()) {
            killCommand = String.format("Taskkill /PID %d /F", pid);
        } else {
            killCommand = String.format("kill -9 %d", pid);
        }
        return killCommand;
    }
}
