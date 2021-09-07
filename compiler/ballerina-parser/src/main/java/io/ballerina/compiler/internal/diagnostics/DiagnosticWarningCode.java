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

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Represents a diagnostic warning code.
 *
 * @since 2.0.0
 */
public enum DiagnosticWarningCode implements DiagnosticCode {

    // The member represents a generic syntax warning
    // We should use this only when we can't figure out the exact warning
    WARNING_SYNTAX_WARNING("BCE10000", "warning.syntax.warning"),

    // Missing tokens in documentation
    WARNING_MISSING_HASH_TOKEN("BCE10001", "warning.missing.hash.token"),
    WARNING_MISSING_SINGLE_BACKTICK_TOKEN("BCE10002", "warning.missing.single.backtick.token"),
    WARNING_MISSING_DOUBLE_BACKTICK_TOKEN("BCE10003", "warning.missing.double.backtick.token"),
    WARNING_MISSING_TRIPLE_BACKTICK_TOKEN("BCE10004", "warning.missing.triple.backtick.token"),
    WARNING_MISSING_IDENTIFIER_TOKEN("BCE10005", "warning.missing.identifier.token"),
    WARNING_MISSING_OPEN_PAREN_TOKEN("BCE10006", "warning.missing.open.paren.token"),
    WARNING_MISSING_CLOSE_PAREN_TOKEN("BCE10007", "warning.missing.close.paren.token"),
    WARNING_MISSING_HYPHEN_TOKEN("BCE10008", "warning.missing.hyphen.token"),
    WARNING_MISSING_PARAMETER_NAME("BCE10009", "warning.missing.parameter.name"),
    WARNING_MISSING_CODE_REFERENCE("BCE10010", "warning.missing.code.reference"),

    // Invalid nodes in documentation
    WARNING_INVALID_BALLERINA_NAME_REFERENCE("BCE10100", "warning.invalid.ballerina.name.reference"),
    WARNING_CANNOT_HAVE_DOCUMENTATION_INLINE_WITH_A_CODE_REFERENCE_BLOCK("BCE10101",
            "warning.cannot.have.documentation.inline.with.a.code.reference.block"),
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

    public boolean equals(DiagnosticCode code) {
        return this.messageKey.equals(code.messageKey());
    }
}
