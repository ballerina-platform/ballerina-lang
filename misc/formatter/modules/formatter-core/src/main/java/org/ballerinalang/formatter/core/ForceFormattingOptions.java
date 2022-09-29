/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 */
public class ForceFormattingOptions {
    boolean forceFormatRecordTypeDesc;

    ForceFormattingOptions(boolean forceFormatRecordTypeDesc) {
        this.forceFormatRecordTypeDesc = forceFormatRecordTypeDesc;
    }

    public boolean getForceFormatRecordTypeDesc() {
        return forceFormatRecordTypeDesc;
    }

    public static ForceFormattingOptionsBuilder builder() {
        return new ForceFormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code ForceFormattingOptions}.
     *
     * @since 2.0.0
     */
    public static class ForceFormattingOptionsBuilder {
        private boolean forceFormatRecordTypeDesc = false;

        public ForceFormattingOptionsBuilder setForceFormatRecordTypeDesc(boolean value) {
            forceFormatRecordTypeDesc = value;
            return this;
        }

        public ForceFormattingOptions build() {
            return new ForceFormattingOptions(forceFormatRecordTypeDesc);
        }
    }
}
