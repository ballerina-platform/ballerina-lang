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

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a collection of diagnostics generated typically by the compiler.
 *
 * @since 2.0.0
 */
public abstract class DiagnosticResult {
    protected final Collection<Diagnostic> allDiagnostics;
    protected Collection<Diagnostic> errors;
    protected Collection<Diagnostic> warnings;

    protected DiagnosticResult(Collection<Diagnostic> allDiagnostics) {
        this.allDiagnostics = Collections.unmodifiableCollection(allDiagnostics);
    }

    public Collection<Diagnostic> diagnostics() {
        return allDiagnostics;
    }

    public Collection<Diagnostic> errors() {
        return getErrors(allDiagnostics);
    }

    public Collection<Diagnostic> warnings() {
        return getWarnings(allDiagnostics);
    }

    public int diagnosticCount() {
        return allDiagnostics.size();
    }

    public int errorCount() {
        return getErrors(allDiagnostics).size();
    }

    public int warningCount() {
        return getWarnings(allDiagnostics).size();
    }

    public boolean hasErrors() {
        return !getErrors(allDiagnostics).isEmpty();
    }

    public boolean hasWarnings() {
        return !getWarnings(allDiagnostics).isEmpty();
    }

    private Collection<Diagnostic> getErrors(Collection<Diagnostic> diagnostics) {
        if (errors != null) {
            return errors;
        }

        errors = Diagnostics.filterErrors(diagnostics);
        return errors;
    }

    private Collection<Diagnostic> getWarnings(Collection<Diagnostic> diagnostics) {
        if (warnings != null) {
            return warnings;
        }

        warnings = Diagnostics.filterWarnings(diagnostics);
        return warnings;
    }
}
