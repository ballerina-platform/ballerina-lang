/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.toml.semantic.diagnostics;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Objects;

/**
 * Represents New Toml Diagnostics.
 *
 * @since 2.0.0
 */
public class TomlDiagnostic extends Diagnostic {

    private final TomlNodeLocation location;
    private final DiagnosticInfo diagnosticInfo;
    private final String message;

    public TomlDiagnostic(TomlNodeLocation location, DiagnosticInfo diagnosticInfo, String message) {
        this.location = location;
        this.diagnosticInfo = diagnosticInfo;
        this.message = message;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TomlDiagnostic that = (TomlDiagnostic) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(diagnosticInfo, that.diagnosticInfo) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, diagnosticInfo, message);
    }

    @Override
    public String toString() {
        String filePath = this.location().lineRange().filePath();

        LineRange lineRange = this.location().lineRange();
        LineRange oneBasedLineRange = LineRange.from(
                filePath,
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));

        return this.diagnosticInfo().severity().toString() + " ["
                + filePath + ":" + oneBasedLineRange + "] " + message();
    }
}
