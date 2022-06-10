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
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Response format for get Result from BalShell endpoint.
 *
 * @since 2201.1.1
 */
public class BalShellGetResultResponse {
    private ShellValue shellValue;
    private final ArrayList<String> errors;
    private final ArrayList<String> diagnostics;
    private MetaInfo metaInfo;
    private String consoleOut;

    public BalShellGetResultResponse() {
        this.shellValue = null;
        this.errors = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
        this.metaInfo = null;
        this.consoleOut = "";
    }

    /**
     * Set return value for response.
     *
     * @param value evaluated value
     */
    public void setValue(Object value) {
        if (value == null) {
            return;
        }

        Type type = TypeUtils.getType(value);
        String stringValue = StringUtils.getJsonString(value);
        if (Objects.equals(stringValue, "[]")) {
            stringValue = StringUtils.getExpressionStringValue(value, null);
        }
        this.shellValue = new ShellValue(stringValue, type.toString(), type.getTag());
    }

    /**
     * Add collected diagnostics for response.
     *
     * @param diagnostics collected diagnostics
     */
    public void addOutputDiagnostics(Collection<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            DiagnosticKind diagnosticKind = diagnostic.getKind();
            if (diagnosticKind != DiagnosticKind.DEBUG) {
                this.diagnostics.add(diagnostic.toString());
            }
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

    /**
     * Add meta info to the shell result output.
     *
     * @param definedVars new defined variable list
     * @param moduleDclns new module declarations list
     */
    public void setMetaInfo(List<String> definedVars, List<String> moduleDclns) {
        this.metaInfo = new MetaInfo(definedVars, moduleDclns);
    }

    /**
     * Set console output for response.
     *
     * @param consoleOut collected strings from system.out
     */
    public void setConsoleOut(List<String> consoleOut) {
        this.consoleOut = String.join("\n", consoleOut);
    }

    public ShellValue getShellValue() {
        return shellValue;
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public String getConsoleOut() {
        return consoleOut;
    }
}
