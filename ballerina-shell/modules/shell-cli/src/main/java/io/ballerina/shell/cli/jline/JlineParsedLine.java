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

import org.jline.reader.CompletingParsedLine;
import org.jline.reader.ParsedLine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The result of a delimited buffer in {@link JlineBallerinaParser}.
 *
 * @since 2.0.0
 */
public class JlineParsedLine implements ParsedLine, CompletingParsedLine {
    private final String line;
    private final List<String> words;
    private final int wordIndex;
    private final int wordCursor;
    private final int cursor;

    /**
     * @param line       the command line being edited
     * @param words      the list of words
     * @param wordIndex  the index of the current word in the list of words
     * @param wordCursor the cursor position within the current word
     * @param cursor     the cursor position within the line
     */
    public JlineParsedLine(String line, List<String> words,
                           int wordIndex, int wordCursor, int cursor) {
        this.line = line;
        this.words = Collections.unmodifiableList(Objects.requireNonNull(words));
        this.wordIndex = wordIndex;
        this.wordCursor = wordCursor;
        this.cursor = cursor;
    }

    @Override
    public int wordIndex() {
        return wordIndex;
    }

    @Override
    public String word() {
        if (0 <= wordIndex && wordIndex < words.size()) {
            return words.get(wordIndex);
        }
        return "";
    }

    @Override
    public int wordCursor() {
        return this.wordCursor;
    }

    @Override
    public List<String> words() {
        return this.words;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public String line() {
        return line;
    }

    @Override
    public CharSequence escape(CharSequence candidate, boolean complete) {
        return candidate;
    }

    @Override
    public int rawWordCursor() {
        return wordCursor;
    }

    @Override
    public int rawWordLength() {
        return word().length();
    }
}
