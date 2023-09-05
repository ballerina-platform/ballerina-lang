/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import java.util.Map;

/**
 * A model for formatting options that could be passed onto the formatter.
 */
public class FormattingOptions {

    // Size of a tab in spaces.
    private int tabSize;

    // Prefer spaces over tabs.
    private String wsCharacter;

    private int columnLimit;

    private boolean lineWrapping;

    private int continuationIndent;

    private boolean paranAlignedIndentation;

    private boolean oneArgPerLine;

    private boolean recordBraceSpacing;

    private boolean allowShortBlocksOnASingleLine;

    private boolean allowShortMatchLabelsOnASingleLine;

    private boolean allowShortIfStatementsOnASingleLine;

    private boolean allowShortLoopsOnASingleLine;

    private boolean elseBlockOnANewLine;

    private boolean sortImports;

    private boolean alignConsecutiveDefinitions;

    private ForceFormattingOptions forceFormattingOptions;

    private ImportFormattingOptions importFormattingOptions;

    private FormattingOptions(int tabSize, String wsCharacter, int columnLimit, boolean lineWrapping,
                              int continuationIndent, ForceFormattingOptions forceFormattingOptions,
                              ImportFormattingOptions importFormattingOptions) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
        this.continuationIndent = continuationIndent;
        this.forceFormattingOptions = forceFormattingOptions;
        this.importFormattingOptions = importFormattingOptions;
    }

    FormattingOptions(int tabSize, String wsCharacter, int columnLimit, boolean lineWrapping,
                      ForceFormattingOptions forceFormattingOptions, boolean paranAlignedIndentation,
                      boolean oneArgPerLine, boolean allowShortBlocksOnASingleLine, boolean recordBraceSpacing,
                      boolean elseBlockOnANewLine, boolean sortImports, boolean alignConsecutiveDefinitions,
                      boolean allowShortIfStatementsOnASingleLine, boolean allowShortLoopsOnASingleLine,
                      boolean allowShortMatchLabelsOnASingleLine) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
        this.forceFormattingOptions = forceFormattingOptions;
        this.paranAlignedIndentation = paranAlignedIndentation;
        this.oneArgPerLine = oneArgPerLine;
        this.recordBraceSpacing = recordBraceSpacing;
        this.elseBlockOnANewLine = elseBlockOnANewLine;
        this.sortImports = sortImports;
        this.alignConsecutiveDefinitions = alignConsecutiveDefinitions;
        this.allowShortBlocksOnASingleLine = allowShortBlocksOnASingleLine;
        this.allowShortIfStatementsOnASingleLine = allowShortIfStatementsOnASingleLine;
        this.allowShortLoopsOnASingleLine = allowShortLoopsOnASingleLine;
        this.allowShortMatchLabelsOnASingleLine = allowShortMatchLabelsOnASingleLine;
    }

    /**
     * @deprecated
     * This constructor is no longer acceptable to instantiate Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public FormattingOptions(int tabSize, String wsCharacter) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = 120;
        this.lineWrapping = false;
    }

    /**
     * @deprecated
     * This constructor is no longer acceptable to instantiate Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public FormattingOptions(boolean lineWrapping, int columnLimit) {
        this.tabSize = 4;
        this.wsCharacter = " ";
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
    }

    /**
     * @deprecated
     * This constructor is no longer acceptable to instantiate Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public FormattingOptions(int tabSize, String wsCharacter, boolean lineWrapping, int columnLimit) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
    }

    public int getTabSize() {
        return tabSize;
    }

    /**
     * @deprecated
     * This setter method is no longer acceptable to set Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public String getWSCharacter() {
        return wsCharacter;
    }

    /**
     * @deprecated
     * This setter method is no longer acceptable to set Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public void setWSCharacter(String wsCharacter) {
        this.wsCharacter = wsCharacter;
    }

    public int getColumnLimit() {
        return this.columnLimit;
    }

    /**
     * @deprecated
     * This setter method is no longer acceptable to set Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public void setColumnLimit(int columnLimit) {
        this.columnLimit = columnLimit;
    }

    public boolean getLineWrapping() {
        return lineWrapping;
    }

    /**
     * @deprecated
     * This setter method is no longer acceptable to set Formatting Options.
     * <p> Use {@link FormattingOptions#builder()} instead.
     */
    @Deprecated
    public void setLineWrapping(boolean lineWrapping) {
        this.lineWrapping = lineWrapping;
    }

    public int getContinuationIndent() {
        return continuationIndent;
    }

    public ForceFormattingOptions getForceFormattingOptions() {
        return forceFormattingOptions;
    }

    public ImportFormattingOptions getImportFormattingOptions() {
        return importFormattingOptions;
    }

    public boolean isElseBlockOnANewLine() {
        return elseBlockOnANewLine;
    }

    public boolean isParanAlignedIndentation() {
        return paranAlignedIndentation;
    }

    public boolean isOneArgPerLine() {
        return oneArgPerLine;
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

    public boolean isRecordBraceSpacing() {
        return recordBraceSpacing;
    }

    public boolean sortImports() {
        return sortImports;
    }

    public boolean alignConsecutiveDefinitions() {
        return alignConsecutiveDefinitions;
    }

    public static FormattingOptionsBuilder builder() {
        return new FormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FormattingOptions}.
     *
     * @since 2201.3.0
     */
    public static class FormattingOptionsBuilder {
        private int tabSize = 4;
        private String wsCharacter = " ";
        private int columnLimit = 120;
        private boolean lineWrapping = false;
        private int continuationIndent = 2;
        private boolean paranAlignedIndentation = false;
        private boolean oneArgPerLine = false;
        private boolean recordBraceSpacing = false;
        private boolean elseBlockOnANewLine = false;
        private Boolean sortImports = false;
        private Boolean alignConsecutiveDefinitions = false;
        private boolean allowShortBlocksOnASingleLine = false;
        private Boolean allowShortMatchLabelsOnASingleLine = false;
        private Boolean allowShortLoopsOnASingleLine = false;
        private Boolean allowShortIfStatementsOnASingleLine = false;
        private ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder().build();
        private ImportFormattingOptions importFormattingOptions = ImportFormattingOptions.builder().build();

        public FormattingOptions.FormattingOptionsBuilder setTabSize(int tabSize) {
            this.tabSize = tabSize;
            return this;
        }

        public FormattingOptions.FormattingOptionsBuilder setWSCharacter(String wsCharacter) {
            this.wsCharacter = wsCharacter;
            return this;
        }

        public FormattingOptions.FormattingOptionsBuilder setColumnLimit(int columnLimit) {
            this.columnLimit = columnLimit;
            return this;
        }

        public FormattingOptions.FormattingOptionsBuilder setLineWrapping(boolean lineWrapping) {
            this.lineWrapping = lineWrapping;
            return this;
        }

        public FormattingOptions.FormattingOptionsBuilder setForceFormattingOptions(
                ForceFormattingOptions forceFormattingOptions) {
            this.forceFormattingOptions = forceFormattingOptions;
            return this;
        }

        public FormattingOptions.FormattingOptionsBuilder setImportFormattingOptions(
                ImportFormattingOptions importFormattingOptions) {
            this.importFormattingOptions = importFormattingOptions;
            return this;
        }

        public FormattingOptions build() {
            return new FormattingOptions(tabSize, wsCharacter, columnLimit, lineWrapping, continuationIndent,
                    forceFormattingOptions, importFormattingOptions);
        }

        public FormattingOptions build(Map<String, Object> configurations) {
            for (Map.Entry<String, Object> entry : configurations.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                switch (key) {
                    case "columnLimit":
                        this.columnLimit = ((Number) value).intValue();
                        this.lineWrapping = true;
                        break;
                    case "tabWidth":
                        this.tabSize = ((Number) value).intValue();
                        break;
                    case "sortImports":
                        this.sortImports = ((Boolean) value);
                        break;
                    case "alignConsecutiveDefinitions":
                        this.alignConsecutiveDefinitions = ((Boolean) value);
                        break;
                    case "allowShortMatchLabelsOnASingleLine":
                        this.allowShortMatchLabelsOnASingleLine = ((Boolean) value);
                        break;
                    case "allowShortIfStatementsOnASingleLine":
                        this.allowShortIfStatementsOnASingleLine = ((Boolean) value);
                        break;
                    case "allowShortLoopsOnASingleLine":
                        this.allowShortLoopsOnASingleLine = ((Boolean) value);
                        break;
                    case "allowShortBlocksOnASingleLine":
                        this.allowShortBlocksOnASingleLine = ((Boolean) value);
                        break;
                    case "recordBraceSpacing":
                        this.recordBraceSpacing = ((Boolean) value);
                        break;
                    case "elseBlockOnANewLine":
                        this.elseBlockOnANewLine = ((Boolean) value);
                        break;
                    case "function":
                        Map<String, Object> funcConfigurations = (Map<String, Object>) value;
                        for (Map.Entry<String, Object> funcEntry : funcConfigurations.entrySet()) {
                            String funcKey = funcEntry.getKey();
                            Object funcValue = funcEntry.getValue();
                            switch (funcKey) {
                                case "paranAlign":
                                    this.paranAlignedIndentation = ((Boolean) funcValue);
                                    break;
                                case "oneArgPerLine":
                                    this.oneArgPerLine = ((Boolean) funcValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            return new FormattingOptions(tabSize, wsCharacter, columnLimit, lineWrapping, forceFormattingOptions,
                    paranAlignedIndentation, oneArgPerLine, allowShortBlocksOnASingleLine, recordBraceSpacing,
                    elseBlockOnANewLine, sortImports, alignConsecutiveDefinitions, allowShortIfStatementsOnASingleLine,
                    allowShortLoopsOnASingleLine, allowShortMatchLabelsOnASingleLine);
        }
    }
}
