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

package io.ballerina.shell.cli;


import io.ballerina.shell.cli.jline.DumbJlineTerminalAdapter;
import io.ballerina.shell.cli.jline.JlineBallerinaParser;
import io.ballerina.shell.cli.jline.JlineSimpleCompleter;
import io.ballerina.shell.cli.jline.JlineTerminalAdapter;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import static io.ballerina.shell.cli.PropertiesLoader.APP_NAME;

/**
 * Main entry point for REPL shell application.
 *
 * @since 2.0.0
 */
public class ReplShellApplication {
    /**
     * Executes the repl shell.
     *
     * @param configuration Configuration to use.
     * @throws Exception If the execution failed with an unexpected error.
     */
    public static void execute(BShellConfiguration configuration) throws Exception {
        TerminalAdapter terminalAdapter;
        Terminal terminal;

        if (configuration.isDumb()) {
            terminal = TerminalBuilder.builder()
                    .streams(configuration.getInputStream(), configuration.getOutputStream())
                    .jna(false).jansi(false).dumb(true).build();
        } else {
            terminal = TerminalBuilder.terminal();
            configuration.setDumb(terminal.getType().equals(Terminal.TYPE_DUMB));
        }

        if (configuration.isDumb()) {
            LineReader lineReader = LineReaderBuilder.builder()
                    .appName(PropertiesLoader.getProperty(APP_NAME))
                    .terminal(terminal)
                    .build();
            terminalAdapter = new DumbJlineTerminalAdapter(lineReader);
        } else {
            Completer completer = new JlineSimpleCompleter();
            DefaultHighlighter highlighter = new DefaultHighlighter();

            Parser parser = new JlineBallerinaParser();
            LineReader lineReader = LineReaderBuilder.builder()
                    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%P > ")
                    .appName(PropertiesLoader.getProperty(APP_NAME))
                    .highlighter(highlighter)
                    .completer(completer)
                    .terminal(terminal)
                    .parser(parser)
                    .build();
            terminalAdapter = new JlineTerminalAdapter(lineReader);
        }

        BallerinaShell shell = new BallerinaShell(configuration, terminalAdapter);
        shell.run();
    }

    public static void main(String... args) throws Exception {
        BShellConfiguration configuration = new BShellConfiguration.Builder().build();
        ReplShellApplication.execute(configuration);
    }
}
