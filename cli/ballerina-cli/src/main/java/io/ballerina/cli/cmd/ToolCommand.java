/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import picocli.CommandLine;

import java.io.PrintStream;

import static io.ballerina.cli.cmd.Constants.TOOL_COMMAND;

/**
 * This class represents the "bal tool" command.
 *
 * @since 2201.6.0
 */
@CommandLine.Command(name = TOOL_COMMAND, description = "Manage ballerina tool commands",
subcommands = {
        ToolPullCommand.class,
        ToolUseCommand.class,
        ToolListCommand.class,
        ToolSearchCommand.class,
        ToolRemoveCommand.class,
        ToolUpdateCommand.class,
        ToolLocationCommand.class
})
public class ToolCommand implements BLauncherCmd {
    private static final String TOOL_USAGE_TEXT = "bal tool <sub-command> [args]";
    private final PrintStream outStream;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public ToolCommand() {
        this.outStream = System.out;
    }

    public ToolCommand(PrintStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public String getName() {
        return TOOL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append(TOOL_USAGE_TEXT);
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    @Override
    public void execute() {
        outStream.println(BLauncherCmd.getCommandUsageInfo(TOOL_COMMAND));
    }
}
