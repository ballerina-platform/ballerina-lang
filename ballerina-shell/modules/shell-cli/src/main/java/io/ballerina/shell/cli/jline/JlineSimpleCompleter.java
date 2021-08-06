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

package io.ballerina.shell.cli.jline;

import io.ballerina.shell.cli.PropertiesLoader;
import io.ballerina.shell.cli.handlers.help.BbeTopicsProvider;
import io.ballerina.shell.cli.utils.FileUtils;
import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;

import static io.ballerina.shell.cli.PropertiesLoader.COMMANDS_FILE;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_FILE;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_HELP;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_PREFIX;
import static io.ballerina.shell.cli.PropertiesLoader.HELP_DESCRIPTION_POSTFIX;
import static io.ballerina.shell.cli.PropertiesLoader.HELP_EXAMPLE_POSTFIX;
import static io.ballerina.shell.cli.PropertiesLoader.KEYWORDS_FILE;

/**
 * A simple completer to give completions based on the input line.
 * If the input starts with /, built-in commands are given.
 * Otherwise keyword completion is given.
 *
 * @since 2.0.0
 */
public class JlineSimpleCompleter implements Completer {
    private final StringsCompleter topicsOptionCompleter;
    private final StringsCompleter commandsCompleter;
    private final StringsCompleter keywordsCompleter;
    private final Completers.FileNameCompleter fileNameCompleter;
    private final StringsCompleter topicsCompleter;

    public JlineSimpleCompleter() {
        BbeTopicsProvider bbeTopicsProvider = BbeTopicsProvider.getBbeTopicsProvider();
        List<String> topicsKeywords = bbeTopicsProvider.getTopicList();
        this.topicsCompleter = new StringsCompleter(topicsKeywords);
        List<String> commandsKeywords = FileUtils.readKeywords(PropertiesLoader.getProperty(COMMANDS_FILE));
        List<String> codeKeywords = FileUtils.readKeywords(PropertiesLoader.getProperty(KEYWORDS_FILE));
        this.commandsCompleter = new StringsCompleter(commandsKeywords);
        this.keywordsCompleter = new StringsCompleter(codeKeywords);
        String helpDescriptionPostfix = PropertiesLoader.getProperty(HELP_DESCRIPTION_POSTFIX);
        String helpExamplePostfix = PropertiesLoader.getProperty(HELP_EXAMPLE_POSTFIX);
        this.topicsOptionCompleter = new StringsCompleter(helpDescriptionPostfix, helpExamplePostfix);
        this.fileNameCompleter = new Completers.FileNameCompleter();
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (hasPropertyPrefix(line, COMMAND_HELP)) {
            if (line.wordIndex() == 1) {
                topicsCompleter.complete(reader, line, candidates);
            } else {
                topicsOptionCompleter.complete(reader, line, candidates);
            }
        } else if (hasPropertyPrefix(line, COMMAND_FILE)) {
            fileNameCompleter.complete(reader, line, candidates);
        } else if (hasPropertyPrefix(line, COMMAND_PREFIX)) {
            commandsCompleter.complete(reader, line, candidates);
        } else {
            keywordsCompleter.complete(reader, line, candidates);
        }
    }

    private boolean hasPropertyPrefix(ParsedLine line, String propertyName) {
        return line.line().trim().startsWith(PropertiesLoader.getProperty(propertyName));
    }
}
