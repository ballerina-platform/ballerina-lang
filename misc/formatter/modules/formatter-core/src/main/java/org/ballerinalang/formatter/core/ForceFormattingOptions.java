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
 * A model for formatting options that can be forced by the API user, that could be passed onto the formatter.
 *
 * @since 2201.3.0
 */
public class ForceFormattingOptions {
    private boolean forceFormatRecordFields;

    private ForceFormattingOptions(boolean forceFormatRecordFields) {
        this.forceFormatRecordFields = forceFormatRecordFields;
    }

    public boolean getForceFormatRecordFields() {
        return forceFormatRecordFields;
    }

    public static ForceFormattingOptionsBuilder builder() {
        return new ForceFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code ForceFormattingOptions}.
     */
    public static class ForceFormattingOptionsBuilder {
        private boolean forceFormatRecordFields = false;

        public ForceFormattingOptionsBuilder setForceFormatRecordFields(boolean forceFormatRecordFields) {
            this.forceFormatRecordFields = forceFormatRecordFields;
            return this;
        }

        public ForceFormattingOptions build() {
            return new ForceFormattingOptions(forceFormatRecordFields);
        }
    }
}
