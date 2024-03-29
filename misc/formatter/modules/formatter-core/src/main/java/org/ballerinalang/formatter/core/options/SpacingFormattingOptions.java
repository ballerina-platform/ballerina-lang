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
 * A model for formatting of spacing by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class SpacingFormattingOptions {

    private final boolean afterTypeCast;
    private final boolean aroundRecordBraces;
    private final boolean alignConsecutiveDefinitions;

    private SpacingFormattingOptions(boolean afterTypeCast, boolean aroundRecordBraces,
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

        private static final String AFTER_TYPE_CAST = "afterTypeCast";
        private static final String AROUND_RECORD_BRACES = "aroundRecordBraces";
        private static final String ALIGN_CONSECUTIVE_DEFINITIONS = "alignConsecutiveDefinitions";
        private boolean afterTypeCast = getDefaultBoolean(FormatSection.SPACING, AFTER_TYPE_CAST);
        private boolean aroundRecordBraces = getDefaultBoolean(FormatSection.SPACING, AROUND_RECORD_BRACES);
        private boolean alignConsecutiveDefinitions =
                getDefaultBoolean(FormatSection.SPACING, ALIGN_CONSECUTIVE_DEFINITIONS);

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

        public SpacingFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> spacingEntry : configs.entrySet()) {
                String spacingKey = spacingEntry.getKey();
                switch (spacingKey) {
                    case AFTER_TYPE_CAST -> setAfterTypeCast((Boolean) spacingEntry.getValue());
                    case AROUND_RECORD_BRACES -> setAroundRecordBraces((Boolean) spacingEntry.getValue());
                    case ALIGN_CONSECUTIVE_DEFINITIONS ->
                            setAlignConsecutiveDefinitions((Boolean) spacingEntry.getValue());
                    default -> throw new FormatterException("invalid spacing formatting option: " + spacingKey);
                }
            }
            return build();
        }
    }
}
