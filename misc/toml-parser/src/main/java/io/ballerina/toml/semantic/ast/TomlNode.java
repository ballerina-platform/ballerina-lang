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
package io.ballerina.toml.semantic.ast;

import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.diagnostics.DiagnosticComparator;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a TOML Node AST.
 *
 * @since 2.0.0
 */
public abstract class TomlNode {

    private final TomlType kind;
    private final TomlNodeLocation location;
    private final Node syntaxTreeNode;
    protected Set<Diagnostic> diagnostics;

    public TomlNode(Node syntaxTreeNode, TomlType kind, TomlNodeLocation location) {
        this.kind = kind;
        this.location = location;
        this.syntaxTreeNode = syntaxTreeNode;
        this.diagnostics = new TreeSet<>(new DiagnosticComparator());
        this.diagnostics.addAll(reportSyntaxDiagnostics(syntaxTreeNode.diagnostics()));
    }

    public abstract void accept(TomlNodeVisitor visitor);

    public Set<Diagnostic> diagnostics() {
        return diagnostics;
    }

    public void clearDiagnostics() {
        diagnostics.clear();
    }

    public void addDiagnostic(Diagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
    }

    public void addDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics.addAll(diagnostics);
    }

    public TomlNodeLocation location() {
        return location;
    }

    public Node externalTreeNode() {
        return syntaxTreeNode;
    }
    
    public TomlType kind() {
        return kind;
    }
    
    
    public boolean isMissingNode() {
        return syntaxTreeNode.isMissing();
    }

    private static Set<Diagnostic> reportSyntaxDiagnostics(Iterable<Diagnostic> syntaxDiagnostics) {
        Set<Diagnostic> diagnostics = new TreeSet<>(new DiagnosticComparator());
        for (Diagnostic syntaxDiagnostic : syntaxDiagnostics) {
            TomlNodeLocation tomlNodeLocation = new TomlNodeLocation(syntaxDiagnostic.location().lineRange(),
                    syntaxDiagnostic.location().textRange());
            TomlDiagnostic tomlDiagnostic =
                    new TomlDiagnostic(tomlNodeLocation, syntaxDiagnostic.diagnosticInfo(), syntaxDiagnostic.message());
            diagnostics.add(tomlDiagnostic);
        }
        return diagnostics;
    }
}
