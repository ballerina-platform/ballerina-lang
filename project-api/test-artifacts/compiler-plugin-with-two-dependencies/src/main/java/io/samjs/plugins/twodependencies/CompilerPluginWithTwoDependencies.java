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
package io.samjs.plugins.twodependencies;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.samjs.jarlibrary.diagnosticutils.DiagnosticUtils;
import io.samjs.jarlibrary.stringutils.StringUtils;

/**
 * A sample {@code CompilerPlugin} that logs events.
 *
 * @since 2.0.0
 */
public class CompilerPluginWithTwoDependencies extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new LogCodeAnalyzer());
    }

    /**
     * A sample {@code CodeAnalyzer} that logs events.
     *
     * @since 2.0.0
     */
    public static class LogCodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
                // There is only one module and document in this project.
                SyntaxTree syntaxTree = getSyntaxTree(compAnalysisCtx.currentPackage());

                // There exits a main function in this tree.
                // Get the location of that node.
                ModulePartNode modulePartNode = syntaxTree.rootNode();
                FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

                // Report a test diagnostic
                Diagnostic diagnostic = DiagnosticUtils.createDiagnostic("COMP_PLUGIN_2_ERROR",
                        "Test error message", funcDefNode.location(), DiagnosticSeverity.ERROR);
                compAnalysisCtx.reportDiagnostic(diagnostic);
            });

            analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
                // There is only one module and document in this project.
                SyntaxTree syntaxTree = getSyntaxTree(compAnalysisCtx.currentPackage());

                // There exits a main function in this tree.
                // Get the location of the function token
                ModulePartNode modulePartNode = syntaxTree.rootNode();
                FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);

                // Report a test diagnostic
                Diagnostic diagnostic = DiagnosticUtils.createDiagnostic("COMP_PLUGIN_2_WARNING",
                        "Test warning message",
                        funcDefNode.functionKeyword().location(), DiagnosticSeverity.WARNING);
                compAnalysisCtx.reportDiagnostic(diagnostic);

                // Dummy usage of the StringUtils library
                String pkgName = compAnalysisCtx.currentPackage().manifest().name().value();
                if (StringUtils.isEmpty(pkgName)) {
                    // This diagnostic should not be reported
                    compAnalysisCtx.reportDiagnostic(
                            DiagnosticUtils.createDiagnostic("COMP_PLUGIN_2_SYNTAX_ERROR",
                                    "Empty package name", null, DiagnosticSeverity.ERROR));
                }
            });

            analysisContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext ->
                syntaxNodeAnalysisContext.reportDiagnostic(
                        DiagnosticUtils.createDiagnostic("COMP_PLUGIN_2_SYNTAX_WARNING",
                                "Local var decl test warning message",
                                syntaxNodeAnalysisContext.node().location(), DiagnosticSeverity.WARNING)),
                    SyntaxKind.LOCAL_VAR_DECL);
        }

        SyntaxTree getSyntaxTree(Package currentPkg) {
            Module module = currentPkg.modules().iterator().next();
            DocumentId docId = module.documentIds().iterator().next();
            Document doc = module.document(docId);
            return doc.syntaxTree();
        }
    }
}
