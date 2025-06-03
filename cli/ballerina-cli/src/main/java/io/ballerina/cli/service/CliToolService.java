/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.cli.service;

import io.ballerina.cli.service.types.CommandResponse;
import io.ballerina.cli.service.types.Context;
import io.ballerina.cli.service.types.SubCommand;

import java.util.List;

/**
 * Java SPI for executing CLI commands and retrieving available commands.
 *
 * @since 2201.13.0
 */
public interface CliToolService {

    /**
     * Executes a command with the given arguments.
     *
     * @param command The command to execute.
     * @param args    The arguments for the command.
     * @return The response from executing the command.
     */
    CommandResponse executeCommand(String command, String[] args, Context context);

    /**
     * Retrieves the list of available commands.
     *
     * @return A list of available subcommands.
     */
    List<SubCommand> getAvailableCommands();

    /**
     * Checks if a command is available in the CLI tool.
     *
     * @param command The command to check for availability.
     * @return true if the command is available, false otherwise.
     */
    boolean isCommandAvailable(String command);
}
