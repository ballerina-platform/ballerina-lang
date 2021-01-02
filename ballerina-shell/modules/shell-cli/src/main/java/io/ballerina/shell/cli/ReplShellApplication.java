/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import io.ballerina.shell.cli.jline.JlineSimpleCompleter;
import io.ballerina.shell.cli.jline.JlineTerminalAdapter;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import static io.ballerina.shell.cli.PropertiesLoader.APP_NAME;

/**
 * Main entry point for REPL shell application.
 */
public class ReplShellApplication {
    /**
     * Executes the repl shell.
     *
     * @param configuration Configuration to use.
     * @throws Exception If the execution failed with an unexpected error.
     */
    public static void execute(Configuration configuration) throws Exception {
        Terminal terminal = TerminalBuilder.terminal();
        Completer completer = new JlineSimpleCompleter();
        DefaultHighlighter highlighter = new DefaultHighlighter();

        DefaultParser parser = new DefaultParser();
        parser.setEofOnUnclosedBracket(DefaultParser.Bracket.CURLY,
                DefaultParser.Bracket.ROUND, DefaultParser.Bracket.SQUARE);
        parser.setQuoteChars(new char[]{'"'});
        parser.setEscapeChars(new char[]{});

        LineReader lineReader = LineReaderBuilder.builder()
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%P > ")
                .appName(PropertiesLoader.getProperty(APP_NAME))
                .variable(LineReader.INDENTATION, 2)
                .highlighter(highlighter)
                .completer(completer)
                .terminal(terminal)
                .parser(parser)
                .build();

        TerminalAdapter adapter = new JlineTerminalAdapter(lineReader);
        BallerinaShell shell = new BallerinaShell(configuration, adapter);
        shell.run();
    }

    public static void main(String... args) throws Exception {
        Configuration configuration = new Configuration(false,
                Configuration.EvaluatorMode.DEFAULT);
        ReplShellApplication.execute(configuration);
    }
}
