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
import org.ballerinalang.formatter.core.FormatterUtils;

import java.util.Map;

/**
 * A model for formatting of wrapping settings by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class WrappingFormattingOptions {

    private final int maxLineLength;
    private final boolean lineWrap;
    private final boolean simpleBlocksInOneLine;
    private final boolean simpleFunctionsInOneLine;

    private WrappingFormattingOptions(int maxLineLength, boolean lineWrap, boolean simpleBlocksInOneLine,
                                      boolean simpleFunctionsInOneLine) {
        this.maxLineLength = maxLineLength;
        this.lineWrap = lineWrap;
        this.simpleBlocksInOneLine = simpleBlocksInOneLine;
        this.simpleFunctionsInOneLine = simpleFunctionsInOneLine;
    }

    public int maxLineLength() {
        return maxLineLength;
    }

    public boolean isSimpleBlocksInOneLine() {
        return simpleBlocksInOneLine;
    }

    public boolean isSimpleFunctionsInOneLine() {
        return simpleFunctionsInOneLine;
    }

    public boolean lineWrap() {
        return lineWrap;
    }

    public static WrappingFormattingOptions.WrappingFormattingOptionsBuilder builder() {
        return new WrappingFormattingOptionsBuilder();
    }

    public static class WrappingFormattingOptionsBuilder {

        private static final String MAX_LINE_LENGTH = "maxLineLength";
        private static final String SIMPLE_BLOCKS_IN_ONE_LINE = "simpleBlocksInOneLine";
        private static final String SIMPLE_METHODS_IN_ONE_LINE = "simpleFunctionsInOneLine";
        private static final String LINE_WRAP = "lineWrap";
        private int maxLineLength = FormatterUtils.getDefaultInt(FormatSection.WRAPPING, MAX_LINE_LENGTH);
        private boolean simpleBlocksInOneLine =
                FormatterUtils.getDefaultBoolean(FormatSection.WRAPPING, SIMPLE_BLOCKS_IN_ONE_LINE);
        private boolean simpleFunctionsInOneLine =
                FormatterUtils.getDefaultBoolean(FormatSection.WRAPPING, SIMPLE_METHODS_IN_ONE_LINE);
        private boolean lineWrap = FormatterUtils.getDefaultBoolean(FormatSection.WRAPPING, LINE_WRAP);

        public WrappingFormattingOptionsBuilder setMaxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public WrappingFormattingOptionsBuilder setSimpleBlocksInOneLine(boolean simpleBlocksInOneLine) {
            this.simpleBlocksInOneLine = simpleBlocksInOneLine;
            return this;
        }

        public WrappingFormattingOptionsBuilder setLineWrap(boolean lineWrap) {
            this.lineWrap = lineWrap;
            return this;
        }

        public WrappingFormattingOptionsBuilder setSimpleFunctionsInOneLine(boolean simpleFunctionsInOneLine) {
            this.simpleFunctionsInOneLine = simpleFunctionsInOneLine;
            return this;
        }

        public WrappingFormattingOptions build() {
            return new WrappingFormattingOptions(maxLineLength, lineWrap, simpleBlocksInOneLine,
                    simpleFunctionsInOneLine);
        }

        public WrappingFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> wrappingEntry : configs.entrySet()) {
                String wrappingKey = wrappingEntry.getKey();
                switch (wrappingKey) {
                    case MAX_LINE_LENGTH -> {
                        setMaxLineLength(((Number) wrappingEntry.getValue()).intValue());
                        setLineWrap(true);
                    }
                    case SIMPLE_BLOCKS_IN_ONE_LINE -> setSimpleBlocksInOneLine((Boolean) wrappingEntry.getValue());
                    case SIMPLE_METHODS_IN_ONE_LINE -> setSimpleFunctionsInOneLine((Boolean) wrappingEntry.getValue());
                    default -> throw new FormatterException("invalid wrapping formatting option: " + wrappingKey);
                }
            }
            return build();
        }
    }
}
