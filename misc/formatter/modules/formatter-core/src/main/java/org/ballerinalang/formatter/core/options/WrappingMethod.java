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

/**
 * Represents the wrapping methods.
 *
 * @since 2201.9.0
 */
public enum WrappingMethod {
    ChopDown,
    Wrap,
    NoWrap;

    public static WrappingMethod fromString(String value) throws FormatterException {
        try {
            return WrappingMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new FormatterException("invalid wrapping method: " + value);
        }
    }

}
