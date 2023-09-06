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

    private boolean recordBraceSpacing;

    private boolean alignConsecutiveDefinitions;

    private boolean alignMultiLineQueries;

    private ForceFormattingOptions forceFormattingOptions;

    private ImportFormattingOptions importFormattingOptions;

    private FunctionFormattingOptions functionFormattingOptions;

    private BlockFormattingOptions blockFormattingOptions;

    private FormattingOptions(int tabSize, String wsCharacter, int columnLimit, boolean lineWrapping,
                              ForceFormattingOptions forceFormattingOptions,
                              ImportFormattingOptions importFormattingOptions) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
        this.forceFormattingOptions = forceFormattingOptions;
        this.importFormattingOptions = importFormattingOptions;
    }

    FormattingOptions(int tabSize, String wsCharacter, int columnLimit, boolean lineWrapping,
                      ForceFormattingOptions forceFormattingOptions,
                      boolean recordBraceSpacing,
                      boolean alignConsecutiveDefinitions,
                      boolean alignMultiLineQueries,
                      ImportFormattingOptions importFormattingOptions,
                      FunctionFormattingOptions functionFormattingOptions,
                      BlockFormattingOptions blockFormattingOptions) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
        this.forceFormattingOptions = forceFormattingOptions;
        this.recordBraceSpacing = recordBraceSpacing;
        this.alignConsecutiveDefinitions = alignConsecutiveDefinitions;
        this.alignMultiLineQueries = alignMultiLineQueries;
        this.importFormattingOptions = importFormattingOptions;
        this.functionFormattingOptions = functionFormattingOptions;
        this.blockFormattingOptions = blockFormattingOptions;

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

    public ForceFormattingOptions getForceFormattingOptions() {
        return forceFormattingOptions;
    }

    public ImportFormattingOptions getImportFormattingOptions() {
        return importFormattingOptions;
    }

    public FunctionFormattingOptions getFunctionFormattingOptions() {
        return functionFormattingOptions;
    }

    public BlockFormattingOptions getBlockFormattingOptions() {
        return blockFormattingOptions;
    }

    public boolean isRecordBraceSpacing() {
        return recordBraceSpacing;
    }

    public boolean alignConsecutiveDefinitions() {
        return alignConsecutiveDefinitions;
    }

    public boolean alignMultiLineQueries() {
        return alignMultiLineQueries;
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
        private boolean recordBraceSpacing = false;
        private boolean alignConsecutiveDefinitions = false;
        private boolean alignMultiLineQueries = false;
        private ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder().build();
        private ImportFormattingOptions.ImportFormattingOptionsBuilder importBuilder =
                ImportFormattingOptions.builder();
        private FunctionFormattingOptions.FunctionFormattingOptionsBuilder functionBuilder =
                FunctionFormattingOptions.builder();
        private BlockFormattingOptions.BlockFormattingOptionsBuilder blockBuilder =
                BlockFormattingOptions.builder();
        private ImportFormattingOptions importFormattingOptions = importBuilder.build();

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
            return new FormattingOptions(tabSize, wsCharacter, columnLimit, lineWrapping, forceFormattingOptions,
                    importFormattingOptions);
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
                    case "alignConsecutiveDefinitions":
                        this.alignConsecutiveDefinitions = ((Boolean) value);
                        break;
                    case "recordBraceSpacing":
                        this.recordBraceSpacing = ((Boolean) value);
                        break;
                    case "alignMultiLineQueries":
                        this.alignMultiLineQueries = ((Boolean) value);
                        break;
                    case "block":
                        Map<String, Object> blockConfigurations = (Map<String, Object>) value;
                        for (Map.Entry<String, Object> funcEntry : blockConfigurations.entrySet()) {
                            String blockKey = funcEntry.getKey();
                            boolean blockValue = (Boolean) funcEntry.getValue();
                            switch (blockKey) {
                                case "elseBlockOnANewLine":
                                    this.blockBuilder.setElseBlockOnANewLine(blockValue);
                                    break;
                                case "allowShortMatchLabelsOnASingleLine":
                                    this.blockBuilder.setAllowShortMatchLabelsOnASingleLine(
                                            blockValue);
                                    break;
                                case "allowShortIfStatementsOnASingleLine":
                                    this.blockBuilder.setAllowShortIfStatementsOnASingleLine(
                                            blockValue);
                                    break;
                                case "allowShortLoopsOnASingleLine":
                                    this.blockBuilder.setAllowShortLoopsOnASingleLine(blockValue);
                                    break;
                                case "allowShortBlocksOnASingleLine":
                                    this.blockBuilder.setAllowShortBlocksOnASingleLine(blockValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "function":
                        Map<String, Object> funcConfigurations = (Map<String, Object>) value;
                        for (Map.Entry<String, Object> funcEntry : funcConfigurations.entrySet()) {
                            String funcKey = funcEntry.getKey();
                            boolean funcValue = (Boolean) funcEntry.getValue();
                            switch (funcKey) {
                                case "paranAlign":
                                    this.functionBuilder.setParanAlign(funcValue);
                                    break;
                                case "oneArgPerLine":
                                    this.functionBuilder.setOneArgPerLine(funcValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "import":
                        Map<String, Object> importConfigurations = (Map<String, Object>) value;
                        for (Map.Entry<String, Object> funcEntry : importConfigurations.entrySet()) {
                            String funcKey = funcEntry.getKey();
                            boolean importValue = (Boolean) funcEntry.getValue();
                            switch (funcKey) {
                                case "groupImports":
                                    this.importBuilder.setGroupImports(importValue);
                                    break;
                                case "sortImports":
                                    this.importBuilder.setSortImports(importValue);
                                    break;
                                case "removeUnusedImports":
                                    this.importBuilder.setRemoveUnusedImports(importValue);
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
                    recordBraceSpacing,
                    alignConsecutiveDefinitions, alignMultiLineQueries, importBuilder.build(),
                    functionBuilder.build(), blockBuilder.build());
        }
    }
}
