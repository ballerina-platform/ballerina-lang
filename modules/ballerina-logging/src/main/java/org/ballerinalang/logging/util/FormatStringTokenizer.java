/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class takes a log format string and tokenizes it based on the Ballerina log format. These tokens can then be
 * consumed by the FormatStringParser.
 *
 * @since 0.95.0
 */
public class FormatStringTokenizer {

    private static final String logFormatRegex =
            "\\{\\{timestamp\\}\\}(\\[[a-zA-Z0-9_+\\-.\\ \\t:,!@#$%^&*();\\\\/|<>\"']*\\])?|\\{\\{\\w+\\}\\}";
    private static final Pattern logPattern = Pattern.compile(logFormatRegex);

    private String logFormatString;
    private List<Token> tokens;
    private Iterator<Token> iterator;

    public FormatStringTokenizer(String logFormatString) {
        this.logFormatString = logFormatString;
        tokens = tokenize(this.logFormatString);
        iterator = tokens.iterator();
    }

    public Token nextToken() {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    private List<Token> tokenize(String formatString) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = logPattern.matcher(formatString);

        int i = 0;
        while (matcher.find()) {
            if (matcher.start() != i) {
                tokens.add(new Token(TokenType.TEXT, formatString.substring(i, matcher.start())));
            }

            Token t;
            String tokenText = formatString.substring(matcher.start(), matcher.end());
            String fieldName = tokenText.split("\\[")[0];
            switch (fieldName) {
                case TokenTypes.FMT_CLASS:
                    t = new Token(TokenType.CLASS, tokenText);
                    break;
                case TokenTypes.FMT_ERROR:
                    t = new Token(TokenType.ERROR, tokenText);
                    break;
                case TokenTypes.FMT_FILE:
                    t = new Token(TokenType.FILE, tokenText);
                    break;
                case TokenTypes.FMT_LEVEL:
                    t = new Token(TokenType.LEVEL, tokenText);
                    break;
                case TokenTypes.FMT_LINE:
                    t = new Token(TokenType.LINE, tokenText);
                    break;
                case TokenTypes.FMT_LOGGER:
                    t = new Token(TokenType.LOGGER, tokenText);
                    break;
                case TokenTypes.FMT_MESSAGE:
                    t = new Token(TokenType.MESSAGE, tokenText);
                    break;
                case TokenTypes.FMT_PACKAGE:
                    t = new Token(TokenType.PACKAGE, tokenText);
                    break;
                case TokenTypes.FMT_TIMESTAMP:
                    t = new Token(TokenType.TIMESTAMP, tokenText);
                    break;
                case TokenTypes.FMT_UNIT:
                    t = new Token(TokenType.UNIT, tokenText);
                    break;
                case TokenTypes.FMT_WORKER:
                    t = new Token(TokenType.WORKER, tokenText);
                    break;
                default:
                    throw new RuntimeException("Invalid log field: " + tokenText);
            }

            tokens.add(t);
            i = matcher.end();
        }
        tokens.add(new Token(TokenType.TEXT, formatString.substring(i)));

        return tokens;
    }
}
