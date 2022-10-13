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

    private ForceFormattingOptions forceFormattingOptions;

    FormattingOptions(int tabSize, String wsCharacter, int columnLimit, boolean lineWrapping,
                      ForceFormattingOptions forceFormattingOptions) {
        this.tabSize = tabSize;
        this.wsCharacter = wsCharacter;
        this.columnLimit = columnLimit;
        this.lineWrapping = lineWrapping;
        this.forceFormattingOptions = forceFormattingOptions;
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

    public static FormattingOptionsBuilder builder() {
        return new FormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FormattingOptions}.
     *
     * @since 2201.1.3
     */
    public static class FormattingOptionsBuilder {
        private int tabSize = 4;
        private String wsCharacter = " ";
        private int columnLimit = 120;
        private boolean lineWrapping = false;
        private ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder().build();

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

        public FormattingOptions build() {
            return new FormattingOptions(tabSize, wsCharacter, columnLimit, lineWrapping, forceFormattingOptions);
        }
    }
}
