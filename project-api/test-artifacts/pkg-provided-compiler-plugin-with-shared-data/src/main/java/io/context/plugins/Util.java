/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.context.plugins;

import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * A class that holds utility functions for the compiler plugin using shared data.
 *
 * @since 2201.8.7
 */
public class Util {

    public static void reportDiagnostic(SyntaxNodeAnalysisContext context, CompilationDiagnostic diagnosticCode,
                                        Location location, Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticCode.getDiagnosticCode(),
                diagnosticCode.getDiagnostic(), diagnosticCode.getDiagnosticSeverity());
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location, args);
        context.reportDiagnostic(diagnostic);
    }
}

class NullLocation implements Location {
    @Override
    public LineRange lineRange() {
        LinePosition from = LinePosition.from(0, 0);
        return LineRange.from("", from, from);
    }

    @Override
    public TextRange textRange() {
        return TextRange.from(0, 0);
    }
}

// Holds Compilation diagnostics for the modifiers
enum CompilationDiagnostic {
    DIAG_1(DiagnosticMessage.WARNING_101,
            new DiagnosticCode() {
                @Override
                public DiagnosticSeverity severity() {
                    return DiagnosticSeverity.WARNING;
                }

                @Override
                public String diagnosticId() {
                    return "0001";
                }

                @Override
                public String messageKey() {
                    return "0001";
                }
            },
            DiagnosticSeverity.WARNING),

    DIAG_2(DiagnosticMessage.WARNING_102,
            new DiagnosticCode() {
                @Override
                public DiagnosticSeverity severity() {
                    return DiagnosticSeverity.WARNING;
                }

                @Override
                public String diagnosticId() {
                    return "0002";
                }

                @Override
                public String messageKey() {
                    return "0002";
                }
            },
            DiagnosticSeverity.WARNING);

    private final String diagnostic;
    private final String diagnosticCode;
    private final DiagnosticSeverity diagnosticSeverity;

    CompilationDiagnostic(DiagnosticMessage message, DiagnosticCode diagnosticCode,
                          DiagnosticSeverity diagnosticSeverity) {
        this.diagnostic = message.getMessage();
        this.diagnosticCode = diagnosticCode.diagnosticId();
        this.diagnosticSeverity = diagnosticSeverity;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public DiagnosticSeverity getDiagnosticSeverity() {
        return this.diagnosticSeverity;
    }
}

// Holds diagnostic messages for the code modifier
enum DiagnosticMessage {
    WARNING_101("warning 01 from modifier"),
    WARNING_102("warning 02 from modifier");

    private final String message;

    DiagnosticMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
