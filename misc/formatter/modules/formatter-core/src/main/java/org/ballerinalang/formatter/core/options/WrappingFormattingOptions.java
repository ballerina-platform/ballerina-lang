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

import java.util.Map;

/**
 * A model for formatting of wrapping settings by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class WrappingFormattingOptions {

    private final int maxLineLength;
    private final boolean lineWrap;
    private final boolean simpleBlocksInOneLine;
    private final boolean simpleMethodsInOneLine;

    private WrappingFormattingOptions(int maxLineLength, boolean lineWrap, boolean simpleBlocksInOneLine,
                                      boolean simpleMethodsInOneLine) {
        this.maxLineLength = maxLineLength;
        this.lineWrap = lineWrap;
        this.simpleBlocksInOneLine = simpleBlocksInOneLine;
        this.simpleMethodsInOneLine = simpleMethodsInOneLine;
    }

    public int maxLineLength() {
        return maxLineLength;
    }

    public boolean isSimpleBlocksInOneLine() {
        return simpleBlocksInOneLine;
    }

    public boolean isSimpleMethodsInOneLine() {
        return simpleMethodsInOneLine;
    }

    public boolean lineWrap() {
        return lineWrap;
    }

    public static WrappingFormattingOptions.WrappingFormattingOptionsBuilder builder() {
        return new WrappingFormattingOptionsBuilder();
    }

    public static class WrappingFormattingOptionsBuilder {

        private int maxLineLength = 120;
        private boolean simpleBlocksInOneLine = false;
        private boolean simpleMethodsInOneLine = false;
        private boolean lineWrap = false;

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

        public WrappingFormattingOptionsBuilder setSimpleMethodsInOneLine(boolean simpleMethodsInOneLine) {
            this.simpleMethodsInOneLine = simpleMethodsInOneLine;
            return this;
        }

        public WrappingFormattingOptions build() {
            return new WrappingFormattingOptions(maxLineLength, lineWrap, simpleBlocksInOneLine,
                    simpleMethodsInOneLine);
        }

        public WrappingFormattingOptions build(Map<String, Object> configs) {
            for (Map.Entry<String, Object> wrappingEntry : configs.entrySet()) {
                String wrappingKey = wrappingEntry.getKey();
                switch (wrappingKey) {
                    case "maxLineLength":
                        setMaxLineLength(((Number) wrappingEntry.getValue()).intValue());
                        setLineWrap(true);
                        break;
                    case "simpleBlocksInOneLine":
                        setSimpleBlocksInOneLine((Boolean) wrappingEntry.getValue());
                        break;
                    case "simpleMethodsInOneLine":
                        setSimpleMethodsInOneLine((Boolean) wrappingEntry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
