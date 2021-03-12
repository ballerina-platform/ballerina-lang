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

package io.ballerina.shell.preprocessor;

import io.ballerina.shell.exceptions.PreprocessorException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * Preprocessor to split the input into several statements
 * based on the semicolons and brackets. Outputs the split input.
 * If a comment starting is detected, this will ignore anything upto a new line.
 *
 * @since 2.0.0
 */
public class SeparatorPreprocessor extends Preprocessor {
    private static final char ESCAPE_CHAR = '\\';
    private static final char BACK_TICK = '`';
    private static final char DOUBLE_QUOTE = '\"';
    private static final char SEMICOLON = ';';
    private static final char PARENTHESIS_OPEN = '(';
    private static final char PARENTHESIS_CLOSE = ')';
    private static final char SQUARE_BR_OPEN = '[';
    private static final char SQUARE_BR_CLOSE = ']';
    private static final char CURLY_BR_OPEN = '{';
    private static final char CURLY_BR_CLOSE = '}';
    private static final char COMMENT_START = '/';
    private static final char NEW_LINE = '\n';

    @Override
    public Collection<String> process(String input) throws PreprocessorException {
        List<String> snippets = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        Stack<Character> brackets = new Stack<>();

        boolean isInBacktickLiteral = false;
        boolean isInQuoteLiteral = false;
        boolean isInComment = false;
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);

            if (isInComment && character == NEW_LINE) {
                isInComment = false;
            }

            if (!isInComment) {
                builder.append(character);
                // Switch in and out of string literal.
                if (character == DOUBLE_QUOTE && isNotEscaped(input, i)) {
                    isInQuoteLiteral = !isInQuoteLiteral;
                } else if (character == BACK_TICK && isNotEscaped(input, i)) {
                    isInBacktickLiteral = !isInBacktickLiteral;
                }

                // If not in a string literal, process brackets.
                if (!isInBacktickLiteral && !isInQuoteLiteral) {
                    if (isInComment = isCommentStart(input, i)) {
                        // First character of comment is already added
                        builder.deleteCharAt(builder.length() - 1);
                    } else if (character == SEMICOLON && brackets.isEmpty()) {
                        addToList(snippets, builder);
                        builder.setLength(0);
                    } else if (isOpeningBracket(character)) {
                        brackets.push(character);
                    } else if (!brackets.isEmpty() && isBracketPair(brackets.peek(), character)) {
                        brackets.pop();
                    } else if (isClosingBracket(character)) {
                        if (brackets.isEmpty()) {
                            addErrorDiagnostic("syntax error: found closing brackets but opening one not found.");
                            throw new PreprocessorException();
                        }
                    }
                }
            }
        }

        // Append remaining string to the statements.
        if (builder.length() > 0) {
            addToList(snippets, builder);
        }

        return snippets;
    }

    /**
     * Adds the builders string representation
     * to stack if string is not empty.
     * Adds a semicolon to end if not present.
     *
     * @param stack   Stack list.
     * @param builder Builder to add.
     */
    private void addToList(List<String> stack, StringBuilder builder) {
        String string = builder.toString().trim();
        // If empty string, pass
        if (string.isBlank()) {
            return;
        }
        // Add semicolon if there is none
        if (!string.endsWith(String.valueOf(SEMICOLON))) {
            string = string + SEMICOLON;
        }
        // If only semicolon present, pass
        if (string.length() == 1) {
            return;
        }
        stack.add(string);
    }

    /**
     * Whether a comment started.
     *
     * @param input    Whole input string.
     * @param position Position of character to check.
     * @return Whether a comment started.
     */
    private boolean isCommentStart(String input, int position) {
        return position < input.length() - 1
                && input.charAt(position) == COMMENT_START
                && input.charAt(position + 1) == COMMENT_START;
    }

    /**
     * Whether the character at the given position was not escaped or not.
     *
     * @param input    Whole input string.
     * @param position Position of character to check.
     * @return Whether the character was escaped.
     */
    private boolean isNotEscaped(String input, int position) {
        return position <= 0 || input.charAt(position - 1) != ESCAPE_CHAR;
    }

    /**
     * Whether the character is a opening bracket type.
     *
     * @param character Character to check.
     * @return Whether the input is a opening bracket.
     */
    private boolean isOpeningBracket(char character) {
        return character == PARENTHESIS_OPEN
                || character == SQUARE_BR_OPEN
                || character == CURLY_BR_OPEN;
    }

    /**
     * Whether the character is a closing bracket type.
     *
     * @param character Character to check.
     * @return Whether the input is a closing bracket.
     */
    private boolean isClosingBracket(char character) {
        return character == PARENTHESIS_CLOSE
                || character == SQUARE_BR_CLOSE
                || character == CURLY_BR_CLOSE;
    }

    /**
     * Whether the inputs resemble a pair of brackets.
     *
     * @param opening Opening bracket.
     * @param closing Closing bracket.
     * @return Whether the opening/closing brackets are matching brackets.
     */
    private boolean isBracketPair(char opening, char closing) {
        return (opening == PARENTHESIS_OPEN && closing == PARENTHESIS_CLOSE)
                || (opening == SQUARE_BR_OPEN && closing == SQUARE_BR_CLOSE)
                || (opening == CURLY_BR_OPEN && closing == CURLY_BR_CLOSE);
    }
}
