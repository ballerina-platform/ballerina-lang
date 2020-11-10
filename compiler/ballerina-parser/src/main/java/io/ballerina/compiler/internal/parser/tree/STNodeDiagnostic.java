/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.internal.diagnostics.DiagnosticCode;
import io.ballerina.compiler.internal.diagnostics.IRDiagnostic;

/**
 * Internal representation of diagnostic that is related to an internal syntax node.
 *
 * @since 2.0.0
 */
public class STNodeDiagnostic extends IRDiagnostic {
    private DiagnosticCode diagnosticCode;
    private Object[] args;

    STNodeDiagnostic(DiagnosticCode diagnosticCode, Object... args) {
        this.diagnosticCode = diagnosticCode;
        this.args = args;
    }

    public static STNodeDiagnostic from(DiagnosticCode diagnosticCode, Object... args) {
        return new STNodeDiagnostic(diagnosticCode, args);
    }

    public DiagnosticCode diagnosticCode() {
        return diagnosticCode;
    }

    public Object[] args() {
        return args;
    }
}
