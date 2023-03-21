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

import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;

import java.util.List;

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
        return DiagnosticMessageHelper.getDiagnosticMessage(nodeDiagnostic.diagnosticCode(), nodeDiagnostic.args());
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }

    @Override
    public String toString() {
        return diagnosticInfo().severity().toString() + " [" +
                location().lineRange().fileName() + ":" + location().lineRange() + "] " + message();
    }
}
