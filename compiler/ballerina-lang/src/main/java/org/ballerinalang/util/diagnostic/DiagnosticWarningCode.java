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

public enum DiagnosticWarningCode {

    NON_MODULE_QUALIFIED_ERROR_REASON("non.module.qualified.error.reason"),

    UNDOCUMENTED_PARAMETER("undocumented.parameter"),
    NO_SUCH_DOCUMENTABLE_PARAMETER("no.such.documentable.parameter"),
    PARAMETER_ALREADY_DOCUMENTED("parameter.already.documented"),
    UNDOCUMENTED_FIELD("undocumented.field"),
    NO_SUCH_DOCUMENTABLE_FIELD("no.such.documentable.field"),
    FIELD_ALREADY_DOCUMENTED("field.already.documented"),
    UNDOCUMENTED_VARIABLE("undocumented.variable"),
    NO_SUCH_DOCUMENTABLE_VARIABLE("no.such.documentable.variable"),
    VARIABLE_ALREADY_DOCUMENTED("variable.already.documented"),
    UNDOCUMENTED_RETURN_PARAMETER("undocumented.return.parameter"),
    NO_DOCUMENTABLE_RETURN_PARAMETER("no.documentable.return.parameter"),
    INVALID_DOCUMENTATION_REFERENCE("invalid.documentation.reference"),
    INVALID_USAGE_OF_PARAMETER_REFERENCE("invalid.use.of.parameter.reference"),

    NO_SUCH_DOCUMENTABLE_ATTRIBUTE("no.such.documentable.attribute"),
    INVALID_USE_OF_ENDPOINT_DOCUMENTATION_ATTRIBUTE("invalid.use.of.endpoint.documentation.attribute"),
    DUPLICATE_DOCUMENTED_ATTRIBUTE("duplicate.documented.attribute"),
    UNDEFINED_DOCUMENTATION_PUBLIC_FUNCTION("undefined.documentation.public.function"),
    USAGE_OF_DEPRECATED_CONSTRUCT("usage.of.deprecated.construct"),

    // Parser diagnostic codes
    SYNTAX_WARNING("syntax.warning"),

    WARNING_INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT("warning.invalid.mutable.access.as.record.default"),

    WARNING_INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT(
            "warning.invalid.non.isolated.invocation.as.record.default"),

    WARNING_INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT(
            "warning.invalid.non.isolated.init.expression.as.record.default"),

    FUNCTION_CAN_BE_MARKED_ISOLATED("function.can.be.marked.isolated"),

    COMPILER_PLUGIN_ERROR("compiler.plugin.crashed"),
    ;

    private String diagnosticId;
    private String messageKey;

    DiagnosticWarningCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    DiagnosticWarningCode(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getValue() {
        return messageKey;
    }
}
