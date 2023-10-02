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
 * A model for formatting of spacing by the API user, that could be passed onto the formatter.
 *
 * @since 2201.8.0
 */
public class SpacingFormattingOptions {

    private final boolean afterTypeCast;
    private final boolean aroundRecordBraces;
    private final boolean alignConsecutiveDefinitions;

    public SpacingFormattingOptions(boolean afterTypeCast, boolean aroundRecordBraces,
                                    boolean alignConsecutiveDefinitions) {
        this.afterTypeCast = afterTypeCast;
        this.aroundRecordBraces = aroundRecordBraces;
        this.alignConsecutiveDefinitions = alignConsecutiveDefinitions;
    }

    public boolean afterTypeCast() {
        return afterTypeCast;
    }

    public boolean aroundRecordBraces() {
        return aroundRecordBraces;
    }

    public boolean alignConsecutiveDefinitions() {
        return alignConsecutiveDefinitions;
    }

    public static SpacingFormattingOptions.SpacingFormattingOptionsBuilder builder() {
        return new SpacingFormattingOptionsBuilder();
    }

    public static class SpacingFormattingOptionsBuilder {

        private boolean afterTypeCast = false;
        private boolean aroundRecordBraces = false;
        private boolean alignConsecutiveDefinitions = false;

        public SpacingFormattingOptionsBuilder setAfterTypeCast(boolean afterTypeCast) {
            this.afterTypeCast = afterTypeCast;
            return this;
        }

        public SpacingFormattingOptionsBuilder setAroundRecordBraces(boolean aroundRecordBraces) {
            this.aroundRecordBraces = aroundRecordBraces;
            return this;
        }

        public SpacingFormattingOptionsBuilder setAlignConsecutiveDefinitions(boolean alignConsecutiveDefinitions) {
            this.alignConsecutiveDefinitions = alignConsecutiveDefinitions;
            return this;
        }

        public SpacingFormattingOptions build() {
            return new SpacingFormattingOptions(afterTypeCast, aroundRecordBraces, alignConsecutiveDefinitions);
        }

        public SpacingFormattingOptions build(Map<String, Object> configs) {
            for (Map.Entry<String, Object> spacingEntry : configs.entrySet()) {
                String spacingKey = spacingEntry.getKey();
                switch (spacingKey) {
                    case "afterTypeCast":
                        setAfterTypeCast((Boolean) spacingEntry.getValue());
                        break;
                    case "aroundRecordBraces":
                        setAroundRecordBraces((Boolean) spacingEntry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
