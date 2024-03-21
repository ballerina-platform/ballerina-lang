/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class reports diagnostics.
 * Encapsulates the diagnostic list and add APIs to add/view diagnostics.
 * A diagnostic represents error, a warning or a message.
 *
 * @since 2.0.0
 */
public abstract class DiagnosticReporter {
    private final List<Diagnostic> diagnostics;
    private boolean hasErrors;

    protected DiagnosticReporter() {
        this.hasErrors = false;
        this.diagnostics = new ArrayList<>();
    }

    /**
     * Adds a diagnostic to the object.
     *
     * @param diagnostic Diagnostic to add.
     */
    public void addDiagnostic(Diagnostic diagnostic) {
        if (diagnostic.getKind() == DiagnosticKind.ERROR) {
            hasErrors = true;
        }
        this.diagnostics.add(diagnostic);
    }

    /**
     * Adds an error diagnostic.
     */
    protected void addErrorDiagnostic(String message) {
        addDiagnostic(Diagnostic.error(message));
    }

    /**
     * Adds an error diagnostic.
     */
    protected void addDebugDiagnostic(String message) {
        addDiagnostic(Diagnostic.debug(message));
    }

    /**
     * Adds a warning diagnostic.
     */
    protected void addWarnDiagnostic(String message) {
        addDiagnostic(Diagnostic.warn(message));
    }

    /**
     * Adds a list of diagnostics to the object.
     * Helpful to combine diagnostics from several objects.
     *
     * @param diagnostics Diagnostics to add.
     */
    public void addAllDiagnostics(Collection<Diagnostic> diagnostics) {
        diagnostics.forEach(this::addDiagnostic);
    }

    /**
     * Returns all the diagnostics that were collected in this object.
     *
     * @return All the collected diagnostics.
     */
    public List<Diagnostic> diagnostics() {
        return this.diagnostics;
    }

    /**
     * Clears diagnostics.
     * Can be used to reuse the same object but with different sessions.
     */
    public void resetDiagnostics() {
        this.diagnostics.clear();
        this.hasErrors = false;
    }

    /**
     * If any diagnostic indicated an error, the reporter will contain errors.
     *
     * @return Whether reporter contains errors.
     */
    public boolean hasErrors() {
        return hasErrors;
    }
}
