/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.internal.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticPropertyKind;

import java.util.Arrays;

/**
 * Represents a string diagnostic property.
 *
 * @since 2201.9.0
 */
public class StringDiagnosticProperty implements DiagnosticProperty<String> {

    private final DiagnosticPropertyKind kind;
    private final String value;

    public StringDiagnosticProperty(String value) {
        this.kind = DiagnosticPropertyKind.STRING;
        this.value = value;
    }

    @Override
    public DiagnosticPropertyKind kind() {
        return kind;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{kind.hashCode(), value.hashCode()});
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringDiagnosticProperty prop) {
            return this.value.equals(prop.value) && this.kind.equals(prop.kind);
        }
        return false;
    }
}
