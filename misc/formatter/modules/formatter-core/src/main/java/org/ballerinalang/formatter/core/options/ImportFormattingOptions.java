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

import static org.ballerinalang.formatter.core.FormatterUtils.getDefaultBoolean;

/**
 * A model for formatting and optimizing imports by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
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

    public boolean groupImports() {
        return groupImports;
    }

    public boolean sortImports() {
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

        private static final String GROUP_IMPORTS = "groupImports";
        private static final  String SORT_IMPORTS = "sortImports";
        private static final String REMOVE_UNUSED_IMPORTS = "removeUnusedImports";
        private boolean groupImports = getDefaultBoolean(FormatSection.IMPORT, GROUP_IMPORTS);
        private boolean sortImports = getDefaultBoolean(FormatSection.IMPORT, SORT_IMPORTS);
        private boolean removeUnusedImports = getDefaultBoolean(FormatSection.IMPORT, REMOVE_UNUSED_IMPORTS);

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setGroupImports(boolean groupImports) {
            this.groupImports = groupImports;
            return this;
        }

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setSortImports(boolean sortImports) {
            this.sortImports = sortImports;
            return this;
        }

        public ImportFormattingOptions.ImportFormattingOptionsBuilder setRemoveUnusedImports(
                boolean removeUnusedImports) {
            this.removeUnusedImports = removeUnusedImports;
            return this;
        }

        public ImportFormattingOptions build() {
            return new ImportFormattingOptions(groupImports, sortImports, removeUnusedImports);
        }

        public ImportFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> importEntry : configs.entrySet()) {
                String importKey = importEntry.getKey();
                switch (importKey) {
                    case SORT_IMPORTS -> setSortImports((Boolean) importEntry.getValue());
                    case GROUP_IMPORTS -> setGroupImports((Boolean) importEntry.getValue());
                    default -> throw new FormatterException("invalid import formatting option: " + importKey);
                }
            }
            return build();
        }
    }
}
