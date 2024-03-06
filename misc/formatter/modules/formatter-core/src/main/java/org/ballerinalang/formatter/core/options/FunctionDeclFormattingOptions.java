/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
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
public class FunctionDeclFormattingOptions {

    private final WrappingMethod parametersWrap;
    private final boolean alignMultilineParameters;
    private final boolean newLineAfterLeftParen;
    private final boolean rightParenOnNewLine;

    private FunctionDeclFormattingOptions(WrappingMethod parametersWrap, boolean alignMultilineParameters,
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

    public static FunctionDeclFormattingOptionsBuilder builder() {
        return new FunctionDeclFormattingOptions.FunctionDeclFormattingOptionsBuilder();
    }

    public static class FunctionDeclFormattingOptionsBuilder {

        private static final String PARAMETERS_WRAP = "parametersWrap";
        private static final String ALIGN_MULTILINE_PARAMETERS = "alignMultilineParameters";
        private static final String NEWLINE_AFTER_LEFT_PAREN = "newLineAfterLeftParen";
        private static final String RIGHT_PAREN_ON_NEWLINE = "rightParenOnNewLine";
        private WrappingMethod parametersWrap =
                WrappingMethod.valueOf(getDefaultString(FormatSection.METHOD_DECLARATION, PARAMETERS_WRAP));
        private boolean alignMultilineParameters =
                getDefaultBoolean(FormatSection.METHOD_DECLARATION, ALIGN_MULTILINE_PARAMETERS);
        private boolean newLineAfterLeftParen =
                getDefaultBoolean(FormatSection.METHOD_DECLARATION, NEWLINE_AFTER_LEFT_PAREN);
        private boolean rightParenOnNewLine =
                getDefaultBoolean(FormatSection.METHOD_DECLARATION, RIGHT_PAREN_ON_NEWLINE);

        public FunctionDeclFormattingOptionsBuilder setParametersWrap(WrappingMethod parametersWrap) {
            this.parametersWrap = parametersWrap;
            return this;
        }

        public FunctionDeclFormattingOptionsBuilder setAlignMultilineParameters(boolean alignMultilineParameters) {
            this.alignMultilineParameters = alignMultilineParameters;
            return this;
        }

        public FunctionDeclFormattingOptionsBuilder setNewLineAfterLeftParen(boolean newLineAfterLeftParen) {
            this.newLineAfterLeftParen = newLineAfterLeftParen;
            return this;
        }

        public FunctionDeclFormattingOptionsBuilder setRightParenOnNewLine(boolean rightParenOnNewLine) {
            this.rightParenOnNewLine = rightParenOnNewLine;
            return this;
        }

        public FunctionDeclFormattingOptions build() {
            return new FunctionDeclFormattingOptions(parametersWrap, alignMultilineParameters,
                    newLineAfterLeftParen, rightParenOnNewLine);
        }

        public FunctionDeclFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> methodDeclarationEntry : configs.entrySet()) {
                String methodDeclarationKey = methodDeclarationEntry.getKey();
                switch (methodDeclarationKey) {
                    case PARAMETERS_WRAP ->
                            setParametersWrap(WrappingMethod.fromString((String) methodDeclarationEntry.getValue()));
                    case ALIGN_MULTILINE_PARAMETERS ->
                            setAlignMultilineParameters((Boolean) methodDeclarationEntry.getValue());
                    case NEWLINE_AFTER_LEFT_PAREN ->
                            setNewLineAfterLeftParen((Boolean) methodDeclarationEntry.getValue());
                    case RIGHT_PAREN_ON_NEWLINE -> setRightParenOnNewLine((Boolean) methodDeclarationEntry.getValue());
                    default -> {
                        assert false : "Include the function declaration formatting option " + methodDeclarationKey +
                                " in the validator";
                    }
                }
            }
            return build();
        }
    }
}
