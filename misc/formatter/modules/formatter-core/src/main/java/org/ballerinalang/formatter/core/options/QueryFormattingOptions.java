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
 * A model for formatting of the queries by the API user, that could be passed onto the formatter.
 *
 * @since 2201.9.0
 */
public class QueryFormattingOptions {

    private final boolean alignMultiLineQueries;

    private QueryFormattingOptions(boolean alignMultiLineQueries) {
        this.alignMultiLineQueries = alignMultiLineQueries;
    }

    public boolean alignMultiLineQueries() {
        return alignMultiLineQueries;
    }

    public static QueryFormattingOptions.QueryFormattingOptionsBuilder builder() {
        return new QueryFormattingOptionsBuilder();
    }

    public static class QueryFormattingOptionsBuilder {

        private static final String ALIGN_MULTILINE_QUERIES = "alignMultiLineQueries";
        private boolean alignMultiLineQueries = getDefaultBoolean(FormatSection.QUERY, ALIGN_MULTILINE_QUERIES);

        public QueryFormattingOptionsBuilder setAlignMultiLineQueries(boolean alignMultiLineQueries) {
            this.alignMultiLineQueries = alignMultiLineQueries;
            return this;
        }

        public QueryFormattingOptions build() {
            return new QueryFormattingOptions(alignMultiLineQueries);
        }

        public QueryFormattingOptions build(Map<String, Object> configs) throws FormatterException {
            for (Map.Entry<String, Object> queryStatementEntry : configs.entrySet()) {
                String queryStatementKey = queryStatementEntry.getKey();
                if (queryStatementKey.equals(ALIGN_MULTILINE_QUERIES)) {
                    setAlignMultiLineQueries((Boolean) queryStatementEntry.getValue());
                } else {
                    throw new FormatterException("invalid query formatting option: " + queryStatementKey);
                }
            }
            return build();
        }
    }
}
