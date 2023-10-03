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
 * A model for formatting of if statement by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class FunctionCallFormattingOptions {

    private final WrappingMethod parametersWrap;
    private final boolean alignMultilineParameters;
    private final boolean newLineAfterLeftParen;
    private final boolean rightParenOnNewLine;

    private FunctionCallFormattingOptions(WrappingMethod parametersWrap, boolean alignMultilineParameters,
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

    public static FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder builder() {
        return new FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FunctionFormattingOptions}.
     */
    public static class FunctionCallFormattingOptionsBuilder {

        private WrappingMethod parametersWrap = WrappingMethod.Wrap;
        private boolean alignMultilineParameters = false;
        private boolean newLineAfterLeftParen = false;
        private boolean rightParenOnNewLine = false;

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setParametersWrap(
                WrappingMethod parametersWrap) {
            this.parametersWrap = parametersWrap;
            return this;
        }

        public FunctionCallFormattingOptions.FunctionCallFormattingOptionsBuilder setAlignMultilineParameters(
                boolean alignMultilineParameters) {
            this.alignMultilineParameters = alignMultilineParameters;
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
            return new FunctionCallFormattingOptions(parametersWrap, alignMultilineParameters, newLineAfterLeftParen,
                    rightParenOnNewLine);
        }

        public FunctionCallFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> methodCallEntry : configs.entrySet()) {
                String methodCallKey = methodCallEntry.getKey();
                switch (methodCallKey) {
                    case "parametersWrap":
                        setParametersWrap(WrappingMethod.fromString((String) methodCallEntry.getValue()));
                        break;
                    case "alignMultilineParameters":
                        setAlignMultilineParameters((Boolean) methodCallEntry.getValue());
                        break;
                    case "newLineAfterLeftParen":
                        setNewLineAfterLeftParen((Boolean) methodCallEntry.getValue());
                        break;
                    case "rightParenOnNewLine":
                        setRightParenOnNewLine((Boolean) methodCallEntry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
