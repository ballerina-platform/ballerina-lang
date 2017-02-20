/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import java.util.Arrays;
import java.util.List;

/**
 * {@code BLauncherCmd} represents a Ballerina launcher command.
 *
 * @see Main.RunCmd
 * @see Main.BuildCmd
 * @since 0.8.0
 */
public interface BLauncherCmd {

    void execute();

    String getName();

    void printUsage(StringBuilder out);

    void setParentCmdParser(JCommander parentCmdParser);

    void setSelfCmdParser(JCommander selfCmdParser);

    static String getCommandUsageInfo(JCommander cmdParser, String commandName) {
        StringBuilder out = new StringBuilder();
        JCommander jCommander = cmdParser.getCommands().get(commandName);
        BLauncherCmd bLauncherCmd = (BLauncherCmd) jCommander.getObjects().get(0);

        out.append(cmdParser.getCommandDescription(commandName)).append("\n");
        out.append("\n");
        out.append("Usage:\n");
        bLauncherCmd.printUsage(out);
        out.append("\n");

        if (jCommander.getCommands().values().size() != 0) {
            out.append("Available Commands:\n");
            printCommandList(jCommander, out);
            out.append("\n");
        }

        printFlags(jCommander.getParameters(), out);
        return out.toString();
    }

    static void printCommandList(JCommander cmdParser, StringBuilder out) {
        int longestNameLen = 0;
        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("default-cmd") || cmd.getName().equals("help")) {
                continue;
            }

            int length = cmd.getName().length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
        }

        for (JCommander commander : cmdParser.getCommands().values()) {
            BLauncherCmd cmd = (BLauncherCmd) commander.getObjects().get(0);
            if (cmd.getName().equals("default-cmd") || cmd.getName().equals("help")) {
                continue;
            }

            String cmdName = cmd.getName();
            String cmdDesc = cmdParser.getCommandDescription(cmdName);

            int noOfSpaces = longestNameLen - (cmd.getName().length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(cmdName).append(new String(charArray)).append(cmdDesc).append("\n");
        }
    }

    static void printFlags(List<ParameterDescription> paramDescs, StringBuilder out) {
        int longestNameLen = 0;
        int count = 0;
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }

            String names = parameterDesc.getNames();
            int length = names.length() + 2;
            if (length > longestNameLen) {
                longestNameLen = length;
            }
            count++;
        }

        if (count == 0) {
            return;
        }
        out.append("Flags:\n");
        for (ParameterDescription parameterDesc : paramDescs) {
            if (parameterDesc.getParameter().hidden()) {
                continue;
            }
            String names = parameterDesc.getNames();
            String desc = parameterDesc.getDescription();
            int noOfSpaces = longestNameLen - (names.length() + 2);
            char[] charArray = new char[noOfSpaces + 4];
            Arrays.fill(charArray, ' ');
            out.append("  ").append(names).append(new String(charArray)).append(desc).append("\n");
        }
    }
}
