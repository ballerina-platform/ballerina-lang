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
 * A model for formatting of indent settings by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class IfStatementFormattingOptions {

    private final boolean elseOnNewLine;

    private IfStatementFormattingOptions(boolean elseOnNewLine) {
        this.elseOnNewLine = elseOnNewLine;
    }

    public boolean elseOnNewLine() {
        return elseOnNewLine;
    }

    public static IfStatementFormattingOptions.IfStatementFormattingOptionsBuilder builder() {
        return new IfStatementFormattingOptionsBuilder();
    }

    public static class IfStatementFormattingOptionsBuilder {

        private static final String ELSE_ON_NEW_LINE = "elseOnNewLine";
        private boolean elseOnNewLine = getDefaultBoolean(FormatSection.IF_STATEMENT, ELSE_ON_NEW_LINE);

        public IfStatementFormattingOptionsBuilder setElseOnNewLine(boolean elseOnNewLine) {
            this.elseOnNewLine = elseOnNewLine;
            return this;
        }

        public IfStatementFormattingOptions build() {
            return new IfStatementFormattingOptions(elseOnNewLine);
        }

        public IfStatementFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> ifStatementEntry : configs.entrySet()) {
                String ifStatementKey = ifStatementEntry.getKey();
                if (ifStatementKey.equals(ELSE_ON_NEW_LINE)) {
                    setElseOnNewLine((Boolean) ifStatementEntry.getValue());
                } else {
                    throw new FormatterException("invalid if statement formatting option: " + ifStatementKey);
                }
            }
            return build();
        }
    }
}
