/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.debugadapter.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.LauncherUtils;
import org.ballerinalang.debugadapter.launcher.DebugAdapterLauncher;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to implement `start-debugger-adapter` command for Ballerina.
 * Ex: `bal start-debugger-adapter [--help|-h] <debugAdapterPort> `
 *
 * @since 1.1.0
 */
@CommandLine.Command(name = "start-debugger-adapter", description = "start Ballerina Debug adapter")
public class DebugAdapterStartCmd implements BLauncherCmd {

    private static final String CMD_NAME = "start-debugger-adapter";

    @SuppressWarnings("unused")
    @CommandLine.Parameters
    private List<String> argList;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
        try {
            List<String> debugLauncherArgs = new ArrayList<>();
            if (argList != null && !argList.isEmpty()) {
                int debugServerPort = Integer.parseInt(argList.get(0));
                debugLauncherArgs.add(String.valueOf(debugServerPort));
            }
            // Lunches the debug server
            DebugAdapterLauncher.main(debugLauncherArgs.toArray(new String[0]));
        } catch (NumberFormatException e) {
            throw LauncherUtils.createLauncherException("Failed to start debug adaptor due to the invalid port " +
                    "specified: '" + argList.get(0) + "'");
        } catch (Throwable e) {
            throw LauncherUtils.createLauncherException("Failed to start debug adaptor due to: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {

    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }
}
