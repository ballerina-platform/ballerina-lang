/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.cli.handlers;

import io.ballerina.shell.cli.ShellExitException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler to contain all commands and handle them.
 *
 * @since 2.0.0
 */
public class CommandHandler {
    private static final String COMMAND_PREFIX = "/";

    private final Map<String, AbstractCommand> commands;

    public CommandHandler() {
        this.commands = new HashMap<>();
    }

    /**
     * Attaches a command handler to a prefix.
     *
     * @param prefix  Prefix for the command.
     * @param command Command to use.
     */
    public void attach(String prefix, AbstractCommand command) {
        assert prefix.startsWith(COMMAND_PREFIX);
        commands.put(prefix, command);
    }

    /**
     * Handles a given source by internal commands.
     *
     * @param source The source to handle.
     * @return Whether it was handled.
     * @throws ShellExitException when user wants to exit.
     */
    public boolean handle(String source) throws ShellExitException {
        if (source.isBlank()) {
            // Empty strings are handled
            return true;
        }

        if (source.startsWith(COMMAND_PREFIX)) {
            String[] args = source.split(" ");
            if (this.commands.containsKey(args[0])) {
                String[] rest = Arrays.copyOfRange(args, 1, args.length);
                this.commands.get(args[0]).run(rest);
                return true;
            }
        }
        return false;
    }
}
