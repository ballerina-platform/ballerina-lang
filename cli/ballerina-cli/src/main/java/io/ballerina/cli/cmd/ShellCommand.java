/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.cli.ReplShellApplication;
import picocli.CommandLine;

import java.io.PrintStream;

import static io.ballerina.cli.cmd.Constants.SHELL_COMMAND;

/**
 * This class represents the "ballerina add" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = SHELL_COMMAND, description = "Run ballerina interactive REPL")
public class ShellCommand implements BLauncherCmd {
    private PrintStream errStream;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "Whether to enable debug mode from start.")
    private boolean isDebug = false;

    @CommandLine.Option(names = {"-f", "--force-dumb"}, description = "Whether to force use of dumb terminal.")
    private boolean forceDumb = false;

    @CommandLine.Option(names = {"-t", "--time-out"}, description = "Timeout to use for tree parsing.")
    private long timeOut = 100;

    public ShellCommand() {
        errStream = System.err;
    }

    public ShellCommand(PrintStream errStream, boolean forceDumb, long timeOut) {
        this.errStream = errStream;
        this.forceDumb = forceDumb;
        this.timeOut = timeOut;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(SHELL_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }
        try {
            BShellConfiguration configuration = new BShellConfiguration.Builder()
                    .setDebug(isDebug).setDumb(forceDumb).setTreeParsingTimeoutMs(timeOut).build();
            ReplShellApplication.execute(configuration);
        } catch (Exception e) {
            errStream.println("Something went wrong while executing REPL: " + e.toString());
        }
    }

    @Override
    public String getName() {
        return SHELL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("run ballerina interactive REPL");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal shell [-d|--debug] [-f|--force-dumb] [-t|--time-out  <time-out-ms>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
