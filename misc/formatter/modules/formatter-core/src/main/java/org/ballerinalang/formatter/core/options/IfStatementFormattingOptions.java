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
public class IfStatementFormattingOptions {

    private final boolean elseOnNewLine;

    public IfStatementFormattingOptions(boolean elseOnNewLine) {
        this.elseOnNewLine = elseOnNewLine;
    }

    public boolean elseOnNewLine() {
        return elseOnNewLine;
    }

    public static IfStatementFormattingOptions.IfStatementFormattingOptionsBuilder builder() {
        return new IfStatementFormattingOptionsBuilder();
    }

    public static class IfStatementFormattingOptionsBuilder {

        private boolean elseOnNewLine = false;

        public IfStatementFormattingOptionsBuilder setElseOnNewLine(boolean elseOnNewLine) {
            this.elseOnNewLine = elseOnNewLine;
            return this;
        }

        public IfStatementFormattingOptions build() {
            return new IfStatementFormattingOptions(elseOnNewLine);
        }

        public IfStatementFormattingOptions build(Map<String, Object> configs) {
            for (Map.Entry<String, Object> ifStatementEntry : configs.entrySet()) {
                String ifStatementKey = ifStatementEntry.getKey();
                switch (ifStatementKey) {
                    case "elseOnNewLine":
                        setElseOnNewLine((Boolean) ifStatementEntry.getValue());
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }
}
