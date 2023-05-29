/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.diagnostics;

import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.List;

/**
 * A diagnostic represents a error, a warning or a message related to runtime .
 *
 * @since 2.0.0
 */
public class RuntimeDiagnostic extends Diagnostic {

    private final DiagnosticInfo diagnosticInfo;

    private Object[] args;

    private RuntimeDiagnosticLocation location;

    public RuntimeDiagnostic(DiagnosticInfo diagnosticInfo, String location, Object[] args) {
        this.diagnosticInfo = diagnosticInfo;
        this.args = args;
        this.location = new RuntimeDiagnosticLocation(location);
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
        return ErrorHelper.getErrorMessage(diagnosticInfo.messageFormat(), args).getValue();
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }

    @Override
    public String toString() {
        String prefix = "error";
        if (diagnosticInfo.severity().equals(DiagnosticSeverity.WARNING)) {
            prefix = "warning";
        }
        if (location.getLocation() == null) {
            return String.format("%s: %s", prefix, message());
        }
        return String.format("%s: %s\n\tat %s", prefix, message(), location.getLocation());
    }
}
