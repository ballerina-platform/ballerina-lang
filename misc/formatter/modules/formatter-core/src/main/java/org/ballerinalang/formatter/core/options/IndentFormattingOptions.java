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
 * A model for formatting of indent settings by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class IndentFormattingOptions {
    private final int indentSize;
    private final int continuationIndentSize;
    private final String wsCharacter;


    public IndentFormattingOptions(int indentSize, int continuationIndentSize, String wsCharacter) {
        this.indentSize = indentSize;
        this.continuationIndentSize = continuationIndentSize;
        this.wsCharacter = wsCharacter;
    }

    public int indentSize() {
        return indentSize;
    }

    public String wsCharacter() {
        return wsCharacter;
    }

    public int continuationIndentSize() {
        return continuationIndentSize;
    }

    public static IndentFormattingOptions.IndentFormattingOptionsBuilder builder() {
        return new IndentFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code IndentFormattingOptions}.
     */
    public static class IndentFormattingOptionsBuilder {
        private int indentSize = 4;
        private int continuationIndentSize = 2;
        private String wsCharacter = " ";

        public IndentFormattingOptionsBuilder setIndentSize(int indentSize) {
            this.indentSize = indentSize;
            return this;
        }

        public IndentFormattingOptionsBuilder setContinuationIndentSize(int continuationIndentSize) {
            this.continuationIndentSize = continuationIndentSize;
            return this;
        }

        public IndentFormattingOptions build() {
            return new IndentFormattingOptions(indentSize, continuationIndentSize, wsCharacter);
        }

        public IndentFormattingOptions build(Map<String, Object> configs) {
            for (Map.Entry<String, Object> indentEntry : configs.entrySet()) {
                String indentKey = indentEntry.getKey();
                switch (indentKey) {
                    case "indentSize":
                        setIndentSize(((Number) indentEntry.getValue()).intValue());
                        break;
                    case "continuationIndentSize":
                        setContinuationIndentSize(((Number) indentEntry.getValue()).intValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
