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
import io.ballerina.shell.cli.PropertiesLoader;
import io.ballerina.shell.cli.handlers.help.HelpProvider;
import io.ballerina.shell.cli.handlers.help.HelpProviderException;
import io.ballerina.shell.cli.handlers.help.RemoteBbeHelpProvider;
import io.ballerina.shell.cli.utils.FileUtils;

import static io.ballerina.shell.cli.PropertiesLoader.HELP_FILE;

/**
 * Outputs help messages to the shell.
 *
 * @since 2.0.0
 */
public class HelpCommand extends AbstractCommand {
    private final HelpProvider helpProvider;

    public HelpCommand(BallerinaShell ballerinaShell) {
        super(ballerinaShell);
        helpProvider = new RemoteBbeHelpProvider();
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            String helpFile = PropertiesLoader.getProperty(HELP_FILE);
            String helpContent = FileUtils.readResource(helpFile);
            ballerinaShell.outputInfo(helpContent);
        } else {
            try {
                StringBuilder content = new StringBuilder();
                helpProvider.getTopic(args, content);
                ballerinaShell.outputInfo(content.toString());
            } catch (HelpProviderException e) {
                ballerinaShell.outputError(e.getMessage());
            }
        }
    }
}
