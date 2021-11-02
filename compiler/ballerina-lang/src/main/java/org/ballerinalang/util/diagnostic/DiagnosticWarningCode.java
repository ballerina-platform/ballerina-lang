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
package org.ballerinalang.util.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This class contains a list of diagnostic warning codes.
 *
 * @since Swan Lake
 */
public enum DiagnosticWarningCode implements DiagnosticCode {

    NON_MODULE_QUALIFIED_ERROR_REASON("BCE20000", "non.module.qualified.error.reason"),

    UNDOCUMENTED_PARAMETER("BCE20001", "undocumented.parameter"),
    NO_SUCH_DOCUMENTABLE_PARAMETER("BCE20002", "no.such.documentable.parameter"),
    PARAMETER_ALREADY_DOCUMENTED("BCE20003", "parameter.already.documented"),
    UNDOCUMENTED_FIELD("BCE20004", "undocumented.field"),
    NO_SUCH_DOCUMENTABLE_FIELD("BCE20005", "no.such.documentable.field"),
    FIELD_ALREADY_DOCUMENTED("BCE20006", "field.already.documented"),
    UNDOCUMENTED_VARIABLE("BCE20007", "undocumented.variable"),
    NO_SUCH_DOCUMENTABLE_VARIABLE("BCE20008", "no.such.documentable.variable"),
    VARIABLE_ALREADY_DOCUMENTED("BCE20009", "variable.already.documented"),
    UNDOCUMENTED_RETURN_PARAMETER("BCE20010", "undocumented.return.parameter"),
    NO_DOCUMENTABLE_RETURN_PARAMETER("BCE20011", "no.documentable.return.parameter"),
    INVALID_DOCUMENTATION_REFERENCE("BCE20012", "invalid.documentation.reference"),
    INVALID_USAGE_OF_PARAMETER_REFERENCE("BCE20013", "invalid.use.of.parameter.reference"),

    NO_SUCH_DOCUMENTABLE_ATTRIBUTE("BCE20014", "no.such.documentable.attribute"),
    INVALID_USE_OF_ENDPOINT_DOCUMENTATION_ATTRIBUTE("BCE20015", "invalid.use.of.endpoint.documentation.attribute"),
    DUPLICATE_DOCUMENTED_ATTRIBUTE("BCE20016", "duplicate.documented.attribute"),
    UNDEFINED_DOCUMENTATION_PUBLIC_FUNCTION("BCE20017", "undefined.documentation.public.function"),
    USAGE_OF_DEPRECATED_CONSTRUCT("BCE20018", "usage.of.deprecated.construct"),

    // Parser diagnostic codes
    SYNTAX_WARNING("BCE20200", "syntax.warning"),

    MATCH_STMT_PATTERN_UNREACHABLE("BCE20250", "match.stmt.unreachable.pattern.available"),
    MATCH_STMT_UNMATCHED_PATTERN("BCE20251", "match.stmt.unmatched.pattern"),

    COMPILER_PLUGIN_ERROR("BCE20300", "compiler.plugin.crashed"),

    FUNCTION_SHOULD_EXPLICITLY_RETURN_A_VALUE("BCE20350", "function.should.explicitly.return.a.value"),

    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD_IN_NON_ISOLATED_SERVICE("BCE20400",
            "concurrent.calls.will.not.be.made.to.non.isolated.method.in.non.isolated.service"),
    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_SERVICE("BCE20401",
            "concurrent.calls.will.not.be.made.to.non.isolated.service"),
    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD("BCE20402",
            "concurrent.calls.will.not.be.made.to.non.isolated.method"),

    UNUSED_LOCAL_VARIABLE("BCE20403", "unused.local.variable")
    ;

    private String diagnosticId;
    private String messageKey;

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
