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
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Visit each non-terminal node in the tree to check for syntax kinds to which analyzer task are attached.
 *
 * @since 2.0.0
 */
class SyntaxTreeVisitor extends NodeVisitor {

    private final Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap;
    private final Package currentPackage;
    private final PackageCompilation compilation;
    private final ModuleId moduleId;
    private final DocumentId documentId;
    private final SyntaxTree syntaxTree;
    private final SemanticModel semanticModel;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    SyntaxTreeVisitor(Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap,
                      Package currentPackage,
                      PackageCompilation compilation,
                      ModuleId moduleId,
                      DocumentId documentId,
                      SyntaxTree syntaxTree,
                      SemanticModel semanticModel) {
        this.syntaxNodeAnalysisTaskMap = syntaxNodeAnalysisTaskMap;
        this.currentPackage = currentPackage;
        this.compilation = compilation;
        this.moduleId = moduleId;
        this.documentId = documentId;
        this.syntaxTree = syntaxTree;
        this.semanticModel = semanticModel;
    }

    List<Diagnostic> runAnalysisTasks() {
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        this.visit(modulePartNode);
        return diagnostics;
    }

    protected void visitSyntaxNode(Node node) {
        // We don't support syntax kinds related to Tokens
        if (node instanceof Token) {
            return;
        }

        SyntaxKind syntaxKind = node.kind();
        if (syntaxNodeAnalysisTaskMap.containsKey(syntaxKind)) {
            runAnalysisTasks(node, syntaxNodeAnalysisTaskMap.get(syntaxKind));
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }

    private void runAnalysisTasks(Node node, List<SyntaxNodeAnalysisTask> syntaxNodeAnalysisTasks) {
        for (SyntaxNodeAnalysisTask syntaxNodeAnalysisTask : syntaxNodeAnalysisTasks) {
            SyntaxNodeAnalysisContextImpl analysisContext = new SyntaxNodeAnalysisContextImpl(node, moduleId,
                    documentId, syntaxTree, semanticModel, currentPackage, compilation);
            syntaxNodeAnalysisTask.perform(analysisContext);
            diagnostics.addAll(analysisContext.reportedDiagnostics());
        }
    }
}
