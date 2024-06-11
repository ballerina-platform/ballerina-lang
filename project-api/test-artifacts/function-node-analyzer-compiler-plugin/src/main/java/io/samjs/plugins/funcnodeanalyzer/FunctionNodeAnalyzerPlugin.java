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
package io.samjs.plugins.funcnodeanalyzer;


import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.samjs.jarlibrary.diagnosticutils.DiagnosticUtils;

import java.util.List;

/**
 * A sample {@code CompilerPlugin} that logs events.
 *
 * @since 2.0.0
 */
public class FunctionNodeAnalyzerPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new SyntaxNodeNodeAnalyzer());
    }

    /**
     * A sample {@code CodeAnalyzer} that logs events.
     *
     * @since 2.0.0
     */
    public static class SyntaxNodeNodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addSyntaxNodeAnalysisTask(functionCallAnalysisTask, SyntaxKind.FUNCTION_CALL);

            analysisContext.addSyntaxNodeAnalysisTask(moduleLevelNodeAnalysisTask,
                    List.of(SyntaxKind.FUNCTION_DEFINITION, SyntaxKind.MODULE_VAR_DECL));

            analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
                // There is only one module and document in this project.
                SyntaxTree syntaxTree = getSyntaxTree(compAnalysisCtx.currentPackage());

                // There exits a main function in this tree.
                // Get the location of that node.
                ModulePartNode modulePartNode = syntaxTree.rootNode();
                FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

                // Report a test diagnostic
                Diagnostic diagnostic = DiagnosticUtils.createDiagnostic("FUNC_NODE_INVALID_NODE",
                        "Test error message", funcDefNode.location(), DiagnosticSeverity.ERROR);
                compAnalysisCtx.reportDiagnostic(diagnostic);
            });
        }

        private final AnalysisTask<SyntaxNodeAnalysisContext> functionCallAnalysisTask =
                syntaxNodeAnalysisContext ->
                    syntaxNodeAnalysisContext.reportDiagnostic(
                            DiagnosticUtils.createDiagnostic("FUNC_NODE_INVALID_FUNC_CALL",
                                    "Function call related error message",
                                    syntaxNodeAnalysisContext.node().location(), DiagnosticSeverity.ERROR));

        private final AnalysisTask<SyntaxNodeAnalysisContext> moduleLevelNodeAnalysisTask =
                syntaxNodeAnalysisContext -> {
                    String code;
                    if (syntaxNodeAnalysisContext.node().kind() == SyntaxKind.MODULE_VAR_DECL) {
                        code = "FUNC_NODE_INVALID_MODULE_LEVEL_VAR";
                    } else if (syntaxNodeAnalysisContext.node().kind() == SyntaxKind.FUNCTION_DEFINITION) {
                        code = "FUNC_NODE_INVALID_FUNCTION_DEF";
                    } else {
                        throw new IllegalStateException("This path cannot be executed");
                    }

                    syntaxNodeAnalysisContext.reportDiagnostic(
                            DiagnosticUtils.createDiagnostic(code,
                                    "Module level declaration: ''{0}'', test ''{1}'' message",
                                    syntaxNodeAnalysisContext.node().location(), DiagnosticSeverity.WARNING,
                                    syntaxNodeAnalysisContext.node().kind(), DiagnosticSeverity.WARNING));
                };

        SyntaxTree getSyntaxTree(Package currentPkg) {
            Module module = currentPkg.modules().iterator().next();
            DocumentId docId = module.documentIds().iterator().next();
            Document doc = module.document(docId);
            return doc.syntaxTree();
        }
    }
}
