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

package io.ballerina.shell.cli.jline.parser;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * State machine implementation for ballerina parser.
 * Specifically used by {@link io.ballerina.shell.cli.jline.JlineBallerinaParser}.
 * <p>
 * In this stage, the "counting parentheses" approach is extended
 * so that it is consistent with Ballerina syntax, and correct in the sense that if it
 * says the input is incomplete, then in Ballerina it will definitely be incomplete.
 * <p>
 * There are several cases where it isn't at the moment e.g. {@code "\"(", // (}
 *
 * @since 2.0.0
 */
public class ParserStateMachine {
    private static final char BACKTICK = '`';
    private static final char OPEN_CURLY = '{';
    private static final char CLOSE_CURLY = '}';
    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';
    private static final char OPEN_SQ_BR = '[';
    private static final char CLOSE_SQ_BR = ']';
    private static final char BACKWARD_SLASH = '\\';
    private static final char FORWARD_SLASH = '/';
    private static final char HASH = '#';
    private static final char DOLLAR = '$';
    private static final char DOUBLE_QUOTES = '\"';
    private static final char NEW_LINE = '\n';
    private static final char CARRIAGE_RETURN = '\r';
    private static final char SPACE = ' ';
    private static final char TAB = '\t';
    // Continuing operator characters are characters than cannot
    // end a syntactically correct snippet: specifically :@*%+-<=&|^!,
    // Note that you cannot include,
    // . because floating point literals can end with a .
    // > because x is map<int> is a valid expression (also XML filter expressions can end with >)
    // '?' because x is int? is a valid expression
    // '/' will be handled by AFTER_FORWARD_SLASH so it cannot be included here
    private static final Set<Character> CONTINUING_OPERATORS =
            Set.of(':', '@', '*', '%', '+', '-', '<', '=', '&', '|', '^', '!', ',');
    private static final Map<Character, Character> OPEN_BRACKETS =
            Map.of(CLOSE_CURLY, OPEN_CURLY,
                    CLOSE_PAREN, OPEN_PAREN,
                    CLOSE_SQ_BR, OPEN_SQ_BR);

    private final Stack<Character> stack;
    private ParserState state;

    public ParserStateMachine() {
        this.state = ParserState.NORMAL;
        this.stack = new Stack<>();
    }

    public void feed(char character) {
        if (character == CARRIAGE_RETURN) {
            // Ignore carriage return
            return;
        }

        switch (state) {
            case NORMAL:
            case AFTER_OPERATOR:
                normalStateOrAfterOperator(character);
                return;
            case AFTER_BACKWARD_SLASH:
                afterBackwardSlashState();
                return;
            case IN_DOUBLE_QUOTES:
                inDoubleQuotesState(character);
                return;
            case IN_DOUBLE_QUOTES_AFTER_BACKWARD_SLASH:
                inDoubleQuotesAfterBackwardSlashState(character);
                return;
            case IN_TEMPLATE:
                inTemplateState(character);
                return;
            case IN_TEMPLATE_AFTER_DOLLAR:
                inTemplateAfterDollarState(character);
                return;
            case AFTER_FORWARD_SLASH:
                afterForwardSlashState(character);
                return;
            case IN_COMMENT:
                inCommentState(character);
                return;
            case ERROR:
                return;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Handles normal state input.
     * This will also handle normal state just after an operator.
     * If brackets: (, [, { are pushed, they are added to stack.
     * Inversely, if ), ], } are pushed, other is removed from stack.
     * Will transit to other states on \, ", `, /, # characters.
     *
     * @param character Next character to process.
     */
    private void normalStateOrAfterOperator(char character) {
        assert state == ParserState.NORMAL || state == ParserState.AFTER_OPERATOR;
        switch (character) {
            case NEW_LINE:
            case SPACE:
            case TAB:
                // The whitespace characters are ignored.
                // They simply act as NO-OP characters.
                return;
            case OPEN_CURLY:
            case OPEN_PAREN:
            case OPEN_SQ_BR:
                stack.push(character);
                break;
            case CLOSE_CURLY:
            case CLOSE_PAREN:
            case CLOSE_SQ_BR:
                if (!stack.empty() && OPEN_BRACKETS.get(character).equals(stack.peek())) {
                    stack.pop();
                    if (!stack.empty() && stack.peek() == BACKTICK) {
                        state = ParserState.IN_TEMPLATE;
                    }
                    break;
                }
                state = ParserState.ERROR;
                break;
            case BACKWARD_SLASH:
                state = ParserState.AFTER_BACKWARD_SLASH;
                break;
            case DOUBLE_QUOTES:
                state = ParserState.IN_DOUBLE_QUOTES;
                break;
            case BACKTICK:
                stack.push(BACKTICK);
                state = ParserState.IN_TEMPLATE;
                break;
            case FORWARD_SLASH:
                state = ParserState.AFTER_FORWARD_SLASH;
                break;
            case HASH:
                state = ParserState.IN_COMMENT;
                break;
            default:
        }
        // Change the transitioning state according to the current character.
        // If current character is an operator, the next state would be AFTER_OPERATOR.
        if (state == ParserState.NORMAL || state == ParserState.AFTER_OPERATOR) {
            if (CONTINUING_OPERATORS.contains(character)) {
                state = ParserState.AFTER_OPERATOR;
            } else {
                state = ParserState.NORMAL;
            }
        }
    }

    /**
     * State just after backward slash.
     * Simply moves to the normal state whatever the character was.
     */
    private void afterBackwardSlashState() {
        assert state == ParserState.AFTER_BACKWARD_SLASH;
        state = ParserState.NORMAL;
    }

    /**
     * Handles in double quotes state.
     * If \ is entered transits.
     * If " is entered, will move back to normal mode.
     * \n is unacceptable in this state.
     *
     * @param character Next character to process.
     */
    private void inDoubleQuotesState(char character) {
        assert state == ParserState.IN_DOUBLE_QUOTES;
        switch (character) {
            case BACKWARD_SLASH:
                state = ParserState.IN_DOUBLE_QUOTES_AFTER_BACKWARD_SLASH;
                break;
            case DOUBLE_QUOTES:
                state = ParserState.NORMAL;
                break;
            case NEW_LINE:
                state = ParserState.ERROR;
                break;
            default:
        }
    }

    /**
     * State just after backward slash while in a double quote.
     * Simply moves to the double quotes state whatever the character was.
     * \n is unacceptable in this state.
     *
     * @param character Next character to process.
     */
    private void inDoubleQuotesAfterBackwardSlashState(char character) {
        assert state == ParserState.IN_DOUBLE_QUOTES_AFTER_BACKWARD_SLASH;
        if (character == NEW_LINE) {
            state = ParserState.ERROR;
            return;
        }
        state = ParserState.IN_DOUBLE_QUOTES;
    }

    /**
     * Handles the state while in a template.
     * Transits on $ or `.
     *
     * @param character Next character to process.
     */
    private void inTemplateState(char character) {
        assert state == ParserState.IN_TEMPLATE;
        switch (character) {
            case DOLLAR:
                state = ParserState.IN_TEMPLATE_AFTER_DOLLAR;
                break;
            case BACKTICK:
                if (!stack.empty() && stack.peek() == BACKTICK) {
                    state = ParserState.NORMAL;
                    stack.pop();
                    break;
                }
                state = ParserState.ERROR;
                break;
            default:
        }
    }

    /**
     * Handles the state while in a template and a dollar is entered.
     * On { this will push { and move to normal state.
     * Otherwise move back to template state.
     *
     * @param character Next character to process.
     */
    private void inTemplateAfterDollarState(char character) {
        assert state == ParserState.IN_TEMPLATE_AFTER_DOLLAR;
        if (character == OPEN_CURLY) {
            stack.push(OPEN_CURLY);
            state = ParserState.NORMAL;
        } else {
            state = ParserState.IN_TEMPLATE;
        }
    }

    /**
     * Handles state after a /.
     * If another / is detected, comment starts.
     * Otherwise move back.
     *
     * @param character Next character to process.
     */
    private void afterForwardSlashState(char character) {
        assert state == ParserState.AFTER_FORWARD_SLASH;
        if (character == FORWARD_SLASH) {
            state = ParserState.IN_COMMENT;
        } else {
            state = ParserState.NORMAL;
        }
    }

    /**
     * Handles state inside a comment.
     * Skip all until new line.
     *
     * @param character Next character to process.
     */
    private void inCommentState(char character) {
        assert state == ParserState.IN_COMMENT;
        if (character == NEW_LINE) {
            state = ParserState.NORMAL;
        }
    }

    public ParserState getState() {
        return state;
    }

    public boolean isIncomplete() {
        if (state == ParserState.ERROR) {
            // ERROR state is always considered to be complete
            return false;
        }
        if (state == ParserState.AFTER_FORWARD_SLASH
                || state == ParserState.AFTER_OPERATOR) {
            // Just after operators is considered to be incomplete
            return true;
        }
        // Otherwise, all brackets/backticks are closed is completion
        return !stack.empty();
    }
}
