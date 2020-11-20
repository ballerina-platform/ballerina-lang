/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This class contains various static methods that operate on {@code Diagnostic} instances.
 *
 * @since 2.0.0
 */
public class Diagnostics {

    public static Collection<Diagnostic> filterErrors(Collection<Diagnostic> diagnostics) {
        return filterDiagnostics(diagnostics, DiagnosticSeverity.ERROR);
    }

    public static boolean hasErrors(Collection<Diagnostic> diagnostics) {
        return hasDiagnosticsWithSeverity(diagnostics, DiagnosticSeverity.ERROR);
    }

    public static Collection<Diagnostic> filterWarnings(Collection<Diagnostic> diagnostics) {
        return filterDiagnostics(diagnostics, DiagnosticSeverity.WARNING);
    }

    public static boolean hasWarnings(Collection<Diagnostic> diagnostics) {
        return hasDiagnosticsWithSeverity(diagnostics, DiagnosticSeverity.WARNING);
    }

    private static Collection<Diagnostic> filterDiagnostics(Collection<Diagnostic> diagnostics,
                                                            DiagnosticSeverity severity) {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.diagnosticInfo().severity() == severity)
                .collect(Collectors.toList());
    }

    private static boolean hasDiagnosticsWithSeverity(Collection<Diagnostic> diagnostics,
                                                      DiagnosticSeverity severity) {
        return diagnostics.stream()
                .anyMatch(diagnostic -> diagnostic.diagnosticInfo().severity() == severity);
    }
}
