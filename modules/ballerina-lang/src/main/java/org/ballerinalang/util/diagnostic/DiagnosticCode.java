/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This class contains a list of diagnostic codes.
 *
 * @since 0.94
 */
public enum DiagnosticCode {

    INCOMPATIBLE_TYPES("incompatible.types"),
    UNKNOWN_TYPE("unknown.type"),
    REDECLARED_SYMBOL("redeclared.symbol"),
    UNDEFINED_SYMBOL("undefined.symbol"),
    BINARY_OP_INCOMPATIBLE_TYPES("binary.op.incompatible.types"),
    UNARY_OP_INCOMPATIBLE_TYPES("unary.op.incompatible.types"),
    SELF_REFERENCE_VAR("self.reference.var"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch"),

    // Cast and conversion related codes
    INCOMPATIBLE_TYPES_CAST("incompatible.types.cast"),
    UNSAFE_CAST_ATTEMPT("unsafe.cast.attempt"),
    UNSAFE_CONVERSION_ATTEMPT("unsafe.conversion.attempt")
    ;

    private String value;

    DiagnosticCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
