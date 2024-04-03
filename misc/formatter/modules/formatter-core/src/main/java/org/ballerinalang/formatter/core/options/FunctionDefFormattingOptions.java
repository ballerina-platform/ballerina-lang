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
 * A model for formatting of function declarations by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class FunctionDefFormattingOptions {

    private final WrappingMethod parametersWrap;
    private final boolean alignMultilineParameters;
    private final boolean newLineAfterLeftParen;
    private final boolean rightParenOnNewLine;

    private FunctionDefFormattingOptions(WrappingMethod parametersWrap, boolean alignMultilineParameters,
                                         boolean newLineAfterLeftParen, boolean rightParenOnNewLine) {
        this.parametersWrap = parametersWrap;
        this.alignMultilineParameters = alignMultilineParameters;
        this.newLineAfterLeftParen = newLineAfterLeftParen;
        this.rightParenOnNewLine = rightParenOnNewLine;
    }

    public WrappingMethod parametersWrap() {
        return parametersWrap;
    }

    public boolean alignMultilineParameters() {
        return alignMultilineParameters;
    }

    public boolean newLineAfterLeftParen() {
        return newLineAfterLeftParen;
    }

    public boolean rightParenOnNewLine() {
        return rightParenOnNewLine;
    }

    public static FunctionDefFormattingOptionsBuilder builder() {
        return new FunctionDefFormattingOptionsBuilder();
    }

    public static class FunctionDefFormattingOptionsBuilder {

        private static final String PARAMETERS_WRAP = "parametersWrap";
        private static final String ALIGN_MULTILINE_PARAMETERS = "alignMultilineParameters";
        private static final String NEWLINE_AFTER_LEFT_PAREN = "newLineAfterLeftParen";
        private static final String RIGHT_PAREN_ON_NEWLINE = "rightParenOnNewLine";
        private WrappingMethod parametersWrap =
                WrappingMethod.valueOf(getDefaultString(FormatSection.FUNCTION_DEFINITION, PARAMETERS_WRAP));
        private boolean alignMultilineParameters =
                getDefaultBoolean(FormatSection.FUNCTION_DEFINITION, ALIGN_MULTILINE_PARAMETERS);
        private boolean newLineAfterLeftParen =
                getDefaultBoolean(FormatSection.FUNCTION_DEFINITION, NEWLINE_AFTER_LEFT_PAREN);
        private boolean rightParenOnNewLine =
                getDefaultBoolean(FormatSection.FUNCTION_DEFINITION, RIGHT_PAREN_ON_NEWLINE);

        public FunctionDefFormattingOptionsBuilder setParametersWrap(WrappingMethod parametersWrap) {
            this.parametersWrap = parametersWrap;
            return this;
        }

        public FunctionDefFormattingOptionsBuilder setAlignMultilineParameters(boolean alignMultilineParameters) {
            this.alignMultilineParameters = alignMultilineParameters;
            return this;
        }

        public FunctionDefFormattingOptionsBuilder setNewLineAfterLeftParen(boolean newLineAfterLeftParen) {
            this.newLineAfterLeftParen = newLineAfterLeftParen;
            return this;
        }

        public FunctionDefFormattingOptionsBuilder setRightParenOnNewLine(boolean rightParenOnNewLine) {
            this.rightParenOnNewLine = rightParenOnNewLine;
            return this;
        }

        public FunctionDefFormattingOptions build() {
            return new FunctionDefFormattingOptions(parametersWrap, alignMultilineParameters,
                    newLineAfterLeftParen, rightParenOnNewLine);
        }

        public FunctionDefFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> functionDefinitionEntry : configs.entrySet()) {
                String functionDefinitionKey = functionDefinitionEntry.getKey();
                switch (functionDefinitionKey) {
                    case PARAMETERS_WRAP ->
                            setParametersWrap(WrappingMethod.fromString((String) functionDefinitionEntry.getValue()));
                    case ALIGN_MULTILINE_PARAMETERS ->
                            setAlignMultilineParameters((Boolean) functionDefinitionEntry.getValue());
                    case NEWLINE_AFTER_LEFT_PAREN ->
                            setNewLineAfterLeftParen((Boolean) functionDefinitionEntry.getValue());
                    case RIGHT_PAREN_ON_NEWLINE ->
                            setRightParenOnNewLine((Boolean) functionDefinitionEntry.getValue());
                    default -> throw new FormatterException(
                            "invalid function definition formatting option: " + functionDefinitionKey);
                }
            }
            return build();
        }
    }
}
