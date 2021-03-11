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

import io.ballerina.shell.cli.BallerinaShell;

/**
 * Reads source from a file.
 *
 * @since 2.0.0
 */
public class FileCommand extends AbstractCommand {
    public FileCommand(BallerinaShell ballerinaShell) {
        super(ballerinaShell);
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            ballerinaShell.outputError("No arguments provided. " +
                    "You have to provide the name of the file to load.");
        } else if (args.length == 1) {
            ballerinaShell.runFile(args[0]);
        } else {
            ballerinaShell.outputError("Multiple arguments provided. " +
                    "You have to provide the name of the file as the only argument.");
        }
    }
}
