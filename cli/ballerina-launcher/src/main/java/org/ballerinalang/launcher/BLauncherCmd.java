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

import org.ballerinalang.launcher.util.BCompileUtil;
import picocli.CommandLine;

import java.io.IOException;

/**
 * {@code BLauncherCmd} represents a Ballerina launcher command.
 *
 * @since 0.8.0
 */
public interface BLauncherCmd {

    void execute();

    String getName();

    void printLongDesc(StringBuilder out);

    void printUsage(StringBuilder out);

    void setParentCmdParser(CommandLine parentCmdParser);

    void setSelfCmdParser(CommandLine selfCmdParser);

    static String getCommandUsageInfo(String commandName) {
        if (commandName == null) {
            throw LauncherUtils.createUsageException("invalid command");
        }

        String fileName = "cli-help/ballerina-" + commandName + ".help";
        try {
            return BCompileUtil.readFileAsString(fileName);
        } catch (IOException e) {
            throw LauncherUtils.createUsageException("usage info not available for command: " + commandName);
        }
    }
}
