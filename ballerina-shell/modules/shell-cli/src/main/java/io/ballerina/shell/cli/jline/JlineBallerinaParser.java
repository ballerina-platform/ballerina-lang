/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.jline;

import io.ballerina.shell.cli.jline.parser.ParserStateMachine;
import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;

import java.util.LinkedList;
import java.util.List;

/**
 * A parser for Ballerina Shell to detect incomplete lines.
 *
 * @since 2.0.0
 */
public class JlineBallerinaParser implements Parser {
    private static final char SPACE = ' ';


    @Override
    public ParsedLine parse(String line, int cursor, ParseContext context) throws SyntaxError {
        assert line != null;

        List<String> words = new LinkedList<>();
        StringBuilder currentWord = new StringBuilder();

        int wordCursor = -1;
        int wordIndex = -1;
        ParserStateMachine stateMachine = new ParserStateMachine();
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            // once we reach the cursor, set selected index.
            // position of the selected index is current word length.
            if (i == cursor) {
                wordIndex = words.size();
                wordCursor = currentWord.length();
            }
            currentWord.append(character);
            if (character == SPACE) {
                words.add(currentWord.toString());
                currentWord.setLength(0);
            }
            stateMachine.feed(character);
        }

        // Add left-over from last word
        if (currentWord.length() > 0 || cursor == line.length()) {
            words.add(currentWord.toString());
        }
        // Update indices and cursors
        if (cursor == line.length()) {
            wordIndex = words.size() - 1;
            wordCursor = words.get(words.size() - 1).length();
        }

        if (context != ParseContext.COMPLETE && stateMachine.isIncomplete()) {
            throw new EOFError(-1, -1, "incomplete line");
        }
        return new JlineParsedLine(line, words, wordIndex, wordCursor, cursor);
    }

    @Override
    public boolean isEscapeChar(char ch) {
        // No escape characters, everything will be
        // passed as it is provided
        return false;
    }
}
