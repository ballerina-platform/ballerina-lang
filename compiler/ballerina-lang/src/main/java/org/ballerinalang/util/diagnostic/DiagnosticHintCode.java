/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * This class contains a list of diagnostic hint codes.
 *
 * @since 2.0.0
 */
public enum DiagnosticHintCode implements DiagnosticCode {

    UNNECESSARY_CONDITION("BCH2000", "unnecessary.condition"),
    EXPRESSION_ALWAYS_FALSE("BCH2001", "expression.always.false"),
    UNNECESSARY_CONDITION_FOR_VARIABLE_OF_TYPE_NEVER("BCH2002", "unnecessary.condition.for.variable.of.type.never"),

    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD_IN_NON_ISOLATED_SERVICE(
            "BCH2003", "concurrent.calls.will.not.be.made.to.non.isolated.method.in.non.isolated.service"),
    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_SERVICE(
            "BCH2004", "concurrent.calls.will.not.be.made.to.non.isolated.service"),
    CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD(
            "BCH2005", "concurrent.calls.will.not.be.made.to.non.isolated.method"),;

    private String diagnosticId;
    private String messageKey;

    DiagnosticHintCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    @Override
    public DiagnosticSeverity severity() {
        return DiagnosticSeverity.HINT;
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
