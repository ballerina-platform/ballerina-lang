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
package io.ballerina.compiler.internal.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Represents a diagnostic warning code.
 *
 * @since 2.0.0
 */
public enum DiagnosticWarningCode implements DiagnosticCode {
    WARNING_INVALID_DOCUMENTATION_IDENTIFIER("BCE10000", "warning.invalid.documentation.identifier"),
    WARNING_INVALID_DOCUMENTATION_EXPRESSION("BCE10001", "warning.invalid.documentation.expression"),
    ;

    String diagnosticId;
    String messageKey;

    DiagnosticWarningCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    @Override
    public DiagnosticSeverity severity() {
        return DiagnosticSeverity.WARNING;
    }

    @Override
    public String diagnosticId() {
        return diagnosticId;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }
}
