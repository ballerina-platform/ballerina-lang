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

package io.ballerina.syntaxapicallsgen.formatter;

import io.ballerina.syntaxapicallsgen.segment.Segment;

/**
 * A simple formatter similar to dart language formatter.
 * For each ( character, indent increases and ) decreases indent.
 * Each parameter is given in a new line.
 *
 * @since 2.0.0
 */
public class DefaultFormatter extends SegmentFormatter {
    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';
    private static final char COMMA = ',';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String TAB_CHAR = "    ";

    /**
     * Check if there is a comma inside the current parenthesis.
     * Any strings with nested parenthesis inside the closest parenthesis is also taken as true.
     *
     * @param input    String to check.
     * @param position Current parenthesis position.
     * @return Whether of not there is a comma or a open paren.
     */
    private boolean commaInsideParenPresent(StringBuilder input, int position) {
        for (int i = position + 1; i < input.length(); i++) {
            if (input.charAt(i) == CLOSE_PAREN) {
                return false;
            }
            if (input.charAt(i) == COMMA || input.charAt(i) == OPEN_PAREN) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String format(Segment segment) {
        StringBuilder output = new StringBuilder();
        StringBuilder input = segment.stringBuilder();

        boolean isCurrentlyInStringLiteral = false;
        boolean foundCommaInCurrentParen = false;
        int depth = 0;

        for (int position = 0; position < input.length(); position++) {
            char character = input.charAt(position);

            if (character == QUOTE && input.charAt(position - 1) != ESCAPE) {
                // Unescaped quotation mark
                isCurrentlyInStringLiteral = !isCurrentlyInStringLiteral;
                output.append(character);
            } else if (isCurrentlyInStringLiteral) {
                // Currently inside a string literal
                output.append(character);
            } else if (character == OPEN_PAREN) {
                // Opening parenthesis
                output.append(character);
                foundCommaInCurrentParen = commaInsideParenPresent(input, position);
                if (foundCommaInCurrentParen) {
                    output.append(System.lineSeparator()).append(tab(++depth));
                }
            } else if (character == CLOSE_PAREN) {
                // Closing parentheses
                if (input.charAt(position - 1) != OPEN_PAREN && foundCommaInCurrentParen) {
                    output.append(System.lineSeparator()).append(tab(--depth));
                }
                foundCommaInCurrentParen = true;
                output.append(character);
            } else if (character == COMMA) {
                // Comma
                output.append(character).append(System.lineSeparator()).append(tab(depth));
            } else {
                // Other
                output.append(character);
            }
        }
        return output.toString();
    }

    /**
     * Repeats a tab character over given number of times.
     */
    private String tab(int depth) {
        return TAB_CHAR.repeat(depth);
    }
}
