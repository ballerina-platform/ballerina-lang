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
package io.ballerina.shell.service;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticKind;
import io.ballerina.shell.service.util.TypeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Response format for get Result from BalShell endpoint.
 *
 * @since 2.0.0
 */
public class BalShellResponse {

    private class ShellValue{
        private final String value;
        private final String mimeType;
        private final String type;

        public ShellValue(String value, String type, int tag) {
            this.value = value;
            this.type = type;
            this.mimeType = TypeUtils.getMimeTypeFromName(tag);
        }
    }

    private ShellValue shellValue;
    private ArrayList<String> errors;
    private ArrayList<String> diagnostics;

    public BalShellResponse() {
        this.shellValue = null;
        this.errors = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
    }

    /**
     * Set return value for response.
     *
     * @param value evaluated value
     * @param consoleOut collected strings from system.out
     */
    public void setValue(Object value, List<String> consoleOut) {
        if (value == null) {
            if (consoleOut.isEmpty()) {
                return;
            }
            this.shellValue = new ShellValue(String.join("\n", consoleOut), "String", 5);
            return;
        }

        Type type = io.ballerina.runtime.api.utils.TypeUtils.getType(value);
        String stringValue = StringUtils.getJsonString(value);
        consoleOut.add(stringValue);
        this.shellValue = new ShellValue(String.join("\n", consoleOut), type.toString(), type.getTag());
    }

    /**
     * Add collected diagnostics for response.
     *
     * @param diagnostics collected diagnostics
     * @param isDebug whether the debug mode of shell is on
     */
    public void addOutputDiagnostics(Collection<Diagnostic> diagnostics, boolean isDebug) {
        for (Diagnostic diagnostic : diagnostics) {
            DiagnosticKind diagnosticKind = diagnostic.getKind();
            if (diagnosticKind == DiagnosticKind.DEBUG) {
                if (isDebug) {
                    this.diagnostics.add(diagnostic.toString());
                }
                continue;
            }

            this.diagnostics.add(diagnostic.toString());
        }
    }

    /**
     * Add generated errors to the response.
     *
     * @param message error message
     */
    public void addError(String message) {
        this.errors.add(message);
    }
}
