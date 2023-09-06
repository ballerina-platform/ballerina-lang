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
package org.ballerinalang.formatter.core;

/**
 * A model for formatting of functions by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class FunctionFormattingOptions {

    private boolean paranAlign;
    private boolean oneArgPerLine;

    private FunctionFormattingOptions(boolean paranAlign, boolean oneArgPerLine) {
        this.paranAlign = paranAlign;
        this.oneArgPerLine = oneArgPerLine;
    }

    public boolean paranAlign() {
        return paranAlign;
    }

    public boolean oneArgPerLine() {
        return oneArgPerLine;
    }

    public static FunctionFormattingOptions.FunctionFormattingOptionsBuilder builder() {
        return new FunctionFormattingOptions.FunctionFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FunctionFormattingOptions}.
     */
    public static class FunctionFormattingOptionsBuilder {

        private boolean paranAlign = false;
        private boolean oneArgPerLine = false;

        public FunctionFormattingOptions.FunctionFormattingOptionsBuilder setParanAlign(boolean paranAlign) {
            this.paranAlign = paranAlign;
            return this;
        }

        public FunctionFormattingOptions.FunctionFormattingOptionsBuilder setOneArgPerLine(boolean oneArgPerLine) {
            this.oneArgPerLine = oneArgPerLine;
            return this;
        }

        public FunctionFormattingOptions build() {
            return new FunctionFormattingOptions(oneArgPerLine, paranAlign);
        }
    }
}
