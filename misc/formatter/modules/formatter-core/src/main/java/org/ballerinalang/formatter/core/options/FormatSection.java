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
 * Represents the sections in the format file.
 *
 * @since 2201.9.0
 */
public enum FormatSection {
    BRACES("braces"),
    FUNCTION_CALL("functionCall"),
    FUNCTION_DEFINITION("functionDefinition"),
    IF_STATEMENT("ifStatement"),
    IMPORT("import"),
    INDENT("indent"),
    QUERY("query"),
    SPACING("spacing"),
    WRAPPING("wrapping");

    private final String val;

    FormatSection(String val) {
        this.val = val;
    }

    public String getStringValue() {
        return val;
    }

    public static FormatSection fromString(String value) throws FormatterException {
        for (FormatSection section : FormatSection.values()) {
            if (section.val.equals(value)) {
                return section;
            }
        }
        throw new FormatterException("invalid format section: " + value);
    }
}
