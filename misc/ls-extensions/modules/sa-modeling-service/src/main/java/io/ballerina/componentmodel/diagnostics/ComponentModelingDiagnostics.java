/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel.diagnostics;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Diagnostic class for the ComponentModelingService.
 */
public class ComponentModelingDiagnostics extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final List<DiagnosticProperty<?>> properties;
    private final String message;
    private final String severity;

    public ComponentModelingDiagnostics(String code, String message, DiagnosticSeverity severity,
                                        Location location, Object[] args) {
        this.diagnosticInfo = new DiagnosticInfo(code, message, severity);
        this.location = location;
        this.properties = Collections.emptyList();
        this.message = MessageFormat.format(message, args);
        this.severity = severity.name();
    }

    @Override
    public Location location() {
        return this.location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return this.diagnosticInfo;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return this.properties;
    }

    public String getSeverity() {
        return this.severity;
    }

    @Override
    public  String toString() {
        String severity = this.diagnosticInfo().severity().toString();
        return "[" + severity + "] " + this.message();
    }
}
