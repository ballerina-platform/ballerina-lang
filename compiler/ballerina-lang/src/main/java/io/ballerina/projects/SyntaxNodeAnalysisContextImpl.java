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
package io.ballerina.projects;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of the {@code SyntaxNodeAnalysisContext}.
 *
 * @since 2.0.0
 */
class SyntaxNodeAnalysisContextImpl implements SyntaxNodeAnalysisContext {

    private final Node node;
    private final ModuleId moduleId;
    private final DocumentId documentId;
    private final SyntaxTree syntaxTree;
    private final SemanticModel semanticModel;
    private final Package currentPackage;
    private final PackageCompilation compilation;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public SyntaxNodeAnalysisContextImpl(Node node,
                                         ModuleId moduleId,
                                         DocumentId documentId,
                                         SyntaxTree syntaxTree,
                                         SemanticModel semanticModel,
                                         Package currentPackage,
                                         PackageCompilation compilation) {
        this.node = node;
        this.moduleId = moduleId;
        this.documentId = documentId;
        this.syntaxTree = syntaxTree;
        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
        this.compilation = compilation;
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public ModuleId moduleId() {
        return moduleId;
    }

    @Override
    public DocumentId documentId() {
        return documentId;
    }

    @Override
    public SyntaxTree syntaxTree() {
        return syntaxTree;
    }

    @Override
    public SemanticModel semanticModel() {
        return semanticModel;
    }

    @Override
    public Package currentPackage() {
        return currentPackage;
    }

    @Override
    public PackageCompilation compilation() {
        return compilation;
    }

    @Override
    public void reportDiagnostic(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    List<Diagnostic> reportedDiagnostics() {
        return diagnostics;
    }
}
