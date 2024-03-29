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

import static org.ballerinalang.formatter.core.FormatterUtils.getDefaultString;

/**
 * A model for formatting of brace settings by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class BraceFormattingOptions {

    private final BraceStyle classBraceStyle;
    private final BraceStyle functionBraceStyle;

    private BraceFormattingOptions(BraceStyle classBraceStyle, BraceStyle functionBraceStyle) {
        this.classBraceStyle = classBraceStyle;
        this.functionBraceStyle = functionBraceStyle;
    }

    public BraceStyle classBraceStyle() {
        return classBraceStyle;
    }

    public BraceStyle functionBraceStyle() {
        return functionBraceStyle;
    }

    public static BraceFormattingOptions.BraceFormattingOptionsBuilder builder() {
        return new BraceFormattingOptionsBuilder();
    }

    public static class BraceFormattingOptionsBuilder {

        private static final String CLASS_BRACE_STYLE = "classBraceStyle";
        private static final String FUNCTION_BRACE_STYLE = "functionBraceStyle";
        private BraceStyle classBraceStyle =
                BraceStyle.valueOf(getDefaultString(FormatSection.BRACES, CLASS_BRACE_STYLE));
        private BraceStyle functionBraceStyle =
                BraceStyle.valueOf(getDefaultString(FormatSection.BRACES, FUNCTION_BRACE_STYLE));

        public BraceFormattingOptionsBuilder setClassBraceStyle(BraceStyle classBraceStyle) {
            this.classBraceStyle = classBraceStyle;
            return this;
        }

        public BraceFormattingOptionsBuilder setFunctionBraceStyle(BraceStyle functionBraceStyle) {
            this.functionBraceStyle = functionBraceStyle;
            return this;
        }

        public BraceFormattingOptions build() {
            return new BraceFormattingOptions(classBraceStyle, functionBraceStyle);
        }

        public BraceFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> bracesEntry : configs.entrySet()) {
                String bracesKey = bracesEntry.getKey();
                BraceStyle style = BraceStyle.fromString((String) bracesEntry.getValue());
                switch (bracesKey) {
                    case CLASS_BRACE_STYLE -> setClassBraceStyle(style);
                    case FUNCTION_BRACE_STYLE -> setFunctionBraceStyle(style);
                    default -> throw new FormatterException("invalid Brace Option: " + bracesKey);
                }
            }
            return build();
        }
    }
}
