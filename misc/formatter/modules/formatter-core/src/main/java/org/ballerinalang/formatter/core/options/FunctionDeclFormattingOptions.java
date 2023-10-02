/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

/**
 * A model for formatting of function declarations by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class FunctionDeclFormattingOptions {

    private final WrappingMethod parametersWrap;
    private final boolean alignMultilineParameters;
    private final boolean newLineAfterLeftParen;
    private final boolean rightParenOnNewLine;

    public FunctionDeclFormattingOptions(WrappingMethod parametersWrap, boolean alignMultilineParameters,
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

        private WrappingMethod parametersWrap = WrappingMethod.WRAP;
        private boolean alignMultilineParameters = false;
        private boolean newLineAfterLeftParen = false;
        private boolean rightParenOnNewLine = false;

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
                    case "parametersWrap":
                        setParametersWrap(WrappingMethod.fromString((String) methodDeclarationEntry.getValue()));
                        break;
                    case "alignMultilineParameters":
                        setAlignMultilineParameters((Boolean) methodDeclarationEntry.getValue());
                        break;
                    case "newLineAfterLeftParen":
                        setNewLineAfterLeftParen((Boolean) methodDeclarationEntry.getValue());
                        break;
                    case "rightParenOnNewLine":
                        setRightParenOnNewLine((Boolean) methodDeclarationEntry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
