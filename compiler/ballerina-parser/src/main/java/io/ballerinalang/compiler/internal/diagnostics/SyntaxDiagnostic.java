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
package io.ballerinalang.compiler.internal.diagnostics;

import io.ballerinalang.compiler.diagnostics.Diagnostic;
import io.ballerinalang.compiler.diagnostics.DiagnosticInfo;
import io.ballerinalang.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerinalang.compiler.syntax.tree.NodeLocation;

/**
 * Represents a {@code Diagnostic} related to syntax analysis.
 *
 * @since 2.0.0
 */
public class SyntaxDiagnostic extends Diagnostic {
    private final STNodeDiagnostic nodeDiagnostic;
    private final NodeLocation location;
    private DiagnosticInfo diagnosticInfo;

    private SyntaxDiagnostic(STNodeDiagnostic nodeDiagnostic, NodeLocation location) {
        this.nodeDiagnostic = nodeDiagnostic;
        this.location = location;
    }

    // TODO We need a generic location here.
    // TODO Node location is associated with a node.
    public static SyntaxDiagnostic from(STNodeDiagnostic nodeDiagnostic, NodeLocation location) {
        return new SyntaxDiagnostic(nodeDiagnostic, location);
    }

    @Override
    public NodeLocation location() {
        return location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        if (diagnosticInfo != null) {
            return diagnosticInfo;
        }

        DiagnosticCode diagnosticCode = nodeDiagnostic.diagnosticCode();
        // TODO Fix the following conversion
        diagnosticInfo = new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.toString(),
                diagnosticCode.severity());
        // TODO
        // TODO Severity comes from the error Code
        return diagnosticInfo;
    }

    @Override
    public String message() {
        // TODO Fix this
        return nodeDiagnostic.diagnosticCode().toString();
    }
}
