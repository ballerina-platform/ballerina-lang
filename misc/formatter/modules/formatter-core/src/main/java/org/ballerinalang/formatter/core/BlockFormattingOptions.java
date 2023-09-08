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
public class BlockFormattingOptions {

    private final boolean allowShortBlocksOnASingleLine;
    private final boolean allowShortMatchLabelsOnASingleLine;
    private final boolean allowShortIfStatementsOnASingleLine;
    private final boolean allowShortLoopsOnASingleLine;
    private final boolean elseBlockOnANewLine;

    public BlockFormattingOptions(boolean allowShortBlocksOnASingleLine, boolean allowShortMatchLabelsOnASingleLine,
                                  boolean allowShortIfStatementsOnASingleLine, boolean allowShortLoopsOnASingleLine,
                                  boolean elseBlockOnANewLine) {
        this.allowShortBlocksOnASingleLine = allowShortBlocksOnASingleLine;
        this.allowShortMatchLabelsOnASingleLine = allowShortMatchLabelsOnASingleLine;
        this.allowShortIfStatementsOnASingleLine = allowShortIfStatementsOnASingleLine;
        this.allowShortLoopsOnASingleLine = allowShortLoopsOnASingleLine;
        this.elseBlockOnANewLine = elseBlockOnANewLine;
    }

    public boolean allowShortBlocksOnASingleLine() {
        return allowShortBlocksOnASingleLine;
    }

    public boolean allowShortMatchLabelsOnASingleLine() {
        return allowShortMatchLabelsOnASingleLine;
    }

    public boolean allowShortIfStatementsOnASingleLine() {
        return allowShortIfStatementsOnASingleLine;
    }

    public boolean allowShortLoopsOnASingleLine() {
        return allowShortLoopsOnASingleLine;
    }

    public boolean elseBlockOnANewLine() {
        return elseBlockOnANewLine;
    }

    public static BlockFormattingOptions.BlockFormattingOptionsBuilder builder() {
        return new BlockFormattingOptions.BlockFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code BlockFormattingOptions}.
     */
    public static class BlockFormattingOptionsBuilder {

        private boolean allowShortBlocksOnASingleLine = false;
        private boolean allowShortMatchLabelsOnASingleLine = false;
        private boolean allowShortIfStatementsOnASingleLine = false;
        private boolean allowShortLoopsOnASingleLine = false;
        private boolean elseBlockOnANewLine = false;

        public BlockFormattingOptions.BlockFormattingOptionsBuilder setAllowShortBlocksOnASingleLine(
                boolean allowShortBlocksOnASingleLine) {
            this.allowShortBlocksOnASingleLine = allowShortBlocksOnASingleLine;
            return this;
        }

        public BlockFormattingOptions.BlockFormattingOptionsBuilder setAllowShortMatchLabelsOnASingleLine(
                boolean allowShortMatchLabelsOnASingleLine) {
            this.allowShortMatchLabelsOnASingleLine = allowShortMatchLabelsOnASingleLine;
            return this;
        }

        public BlockFormattingOptions.BlockFormattingOptionsBuilder setAllowShortIfStatementsOnASingleLine(
                boolean allowShortIfStatementsOnASingleLine) {
            this.allowShortIfStatementsOnASingleLine = allowShortIfStatementsOnASingleLine;
            return this;
        }

        public BlockFormattingOptions.BlockFormattingOptionsBuilder setAllowShortLoopsOnASingleLine(
                boolean allowShortLoopsOnASingleLine) {
            this.allowShortLoopsOnASingleLine = allowShortLoopsOnASingleLine;
            return this;
        }

        public BlockFormattingOptions.BlockFormattingOptionsBuilder setElseBlockOnANewLine(
                boolean elseBlockOnANewLine) {
            this.elseBlockOnANewLine = elseBlockOnANewLine;
            return this;
        }

        public BlockFormattingOptions build() {
            return new BlockFormattingOptions(allowShortBlocksOnASingleLine, allowShortMatchLabelsOnASingleLine,
                    allowShortIfStatementsOnASingleLine, allowShortLoopsOnASingleLine, elseBlockOnANewLine);
        }
    }
}
