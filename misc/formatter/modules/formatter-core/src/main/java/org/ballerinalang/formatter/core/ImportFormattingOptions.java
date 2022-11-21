/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
 * A model for formatting and optimizing imports by the API user, that could be passed onto the formatter.
 *
 * @since 2201.4.0
 */
public class ImportFormattingOptions {
    private boolean groupImports;
    private boolean sortImports;
    private boolean removeUnusedImports;

    private ImportFormattingOptions(boolean groupImports, boolean sortImports, boolean removeUnusedImports) {
        this.groupImports = groupImports;
        this.sortImports = sortImports;
        this.removeUnusedImports = removeUnusedImports;
    }

    public boolean getGroupImports() {
        return groupImports;
    }

    public boolean getSortImports() {
        return sortImports;
    }

    public boolean getRemoveUnusedImports() {
        return removeUnusedImports;
    }

    public static ImportFormattingOptions.ImportFormattingOptionsBuilder builder() {
        return new ImportFormattingOptions.ImportFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code ImportFormattingOptions}.
     */
    public static class ImportFormattingOptionsBuilder {
        private boolean groupImports = true;
        private boolean sortImports = true;
        private boolean removeUnusedImports = true;

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setGroupImports(boolean groupImports) {
            this.groupImports = groupImports;
            return this;
        }

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setSortImports(boolean sortImports) {
            this.sortImports = sortImports;
            return  this;
        }

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setRemoveUnusedImports(
                boolean removeUnusedImports) {
            this.removeUnusedImports = removeUnusedImports;
            return this;
        }

        public ImportFormattingOptions build() {
            return new ImportFormattingOptions(groupImports, sortImports, removeUnusedImports);
        }
    }
}
