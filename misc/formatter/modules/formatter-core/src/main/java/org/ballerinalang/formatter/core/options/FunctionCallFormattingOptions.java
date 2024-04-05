/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core.options;

import org.ballerinalang.formatter.core.FormatterException;

import java.util.Map;

import static org.ballerinalang.formatter.core.FormatterUtils.getDefaultBoolean;
import static org.ballerinalang.formatter.core.FormatterUtils.getDefaultString;

/**
 * A model for formatting of if statement by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */

public class FunctionCallFormattingOptions {

    private final WrappingMethod argumentsWrap;
    private final boolean alignMultilineArguments;
    private final boolean newLineAfterLeftParen;
    private final boolean rightParenOnNewLine;

    private FunctionCallFormattingOptions(WrappingMethod argumentsWrap, boolean alignMultilineArguments,
                                          boolean newLineAfterLeftParen, boolean rightParenOnNewLine) {
        this.argumentsWrap = argumentsWrap;
        this.alignMultilineArguments = alignMultilineArguments;
        this.newLineAfterLeftParen = newLineAfterLeftParen;
        this.rightParenOnNewLine = rightParenOnNewLine;
    }

    public WrappingMethod argumentsWrap() {
        return argumentsWrap;
    }

    public boolean alignMultilineArguments() {
        return alignMultilineArguments;
    }

    public boolean newLineAfterLeftParen() {
        return newLineAfterLeftParen;
    }

    public boolean rightParenOnNewLine() {
        return rightParenOnNewLine;
    }

    public static FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder builder() {
        return new FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FunctionCallFormattingOptions}.
     */
    public static class FunctionCallFormattingOptionsBuilder {

        private static final String ARGUMENTS_WRAP = "argumentsWrap";
        private static final String ALIGN_MULTILINE_ARGUMENTS = "alignMultilineArguments";
        private static final String NEWLINE_AFTER_LEFT_PAREN = "newLineAfterLeftParen";
        private static final String RIGHT_PAREN_ON_NEWLINE = "rightParenOnNewLine";
        private WrappingMethod argumentsWrap =
                WrappingMethod.valueOf(getDefaultString(FormatSection.FUNCTION_CALL, ARGUMENTS_WRAP));
        private boolean alignMultilineArguments =
                getDefaultBoolean(FormatSection.FUNCTION_CALL, ALIGN_MULTILINE_ARGUMENTS);
        private boolean newLineAfterLeftParen =
                getDefaultBoolean(FormatSection.FUNCTION_CALL, NEWLINE_AFTER_LEFT_PAREN);
        private boolean rightParenOnNewLine = getDefaultBoolean(FormatSection.FUNCTION_CALL, RIGHT_PAREN_ON_NEWLINE);

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setArgumentsWrap(
                WrappingMethod argumentsWrap) {
            this.argumentsWrap = argumentsWrap;
            return this;
        }

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setAlignMultilineArguments(
                boolean alignMultilineArguments) {
            this.alignMultilineArguments = alignMultilineArguments;
            return this;
        }

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setNewLineAfterLeftParen(
                boolean newLineAfterLeftParen) {
            this.newLineAfterLeftParen = newLineAfterLeftParen;
            return this;
        }

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setRightParenOnNewLine(
                boolean rightParenOnNewLine) {
            this.rightParenOnNewLine = rightParenOnNewLine;
            return this;
        }

        public FunctionCallFormattingOptions build() {
            return new FunctionCallFormattingOptions(argumentsWrap, alignMultilineArguments, newLineAfterLeftParen,
                    rightParenOnNewLine);
        }

        public FunctionCallFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> funcCallEntry : configs.entrySet()) {
                String funcCallKey = funcCallEntry.getKey();
                switch (funcCallKey) {
                    case ARGUMENTS_WRAP ->
                            setArgumentsWrap(WrappingMethod.fromString((String) funcCallEntry.getValue()));
                    case ALIGN_MULTILINE_ARGUMENTS -> setAlignMultilineArguments((Boolean) funcCallEntry.getValue());
                    case NEWLINE_AFTER_LEFT_PAREN -> setNewLineAfterLeftParen((Boolean) funcCallEntry.getValue());
                    case RIGHT_PAREN_ON_NEWLINE -> setRightParenOnNewLine((Boolean) funcCallEntry.getValue());
                    default -> throw new FormatterException("invalid function call formatting option: " + funcCallKey);
                }
            }
            return build();
        }
    }
}
