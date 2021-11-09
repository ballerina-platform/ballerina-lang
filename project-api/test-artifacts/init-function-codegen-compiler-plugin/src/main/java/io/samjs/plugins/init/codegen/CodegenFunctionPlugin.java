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
package io.samjs.plugins.init.codegen;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.samjs.jarlibrary.diagnosticutils.DiagnosticUtils;

/**
 * A sample {@code CompilerPlugin} that generated files for each function definition.
 *
 * @since 2.0.0
 */
public class CodegenFunctionPlugin extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new FunctionNodeAnalyzer());
        pluginContext.addCodeGenerator(new InitFunctionCodeGenerator());
    }

    /**
     * A sample {@code CodeAnalyzer} that report function info diagnostics.
     *
     * @since 2.0.0
     */
    public static class FunctionNodeAnalyzer extends CodeAnalyzer {

        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
                FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) syntaxNodeAnalysisContext.node();
                // Report a test diagnostic
                Diagnostic diagnostic = DiagnosticUtils.createDiagnostic("CODEGEN_PLUGIN_FUNCTION",
                        funcDefNode.functionName().text(),
                        funcDefNode.functionKeyword().location(), DiagnosticSeverity.INFO);
                syntaxNodeAnalysisContext.reportDiagnostic(diagnostic);
            }, SyntaxKind.FUNCTION_DEFINITION);
        }
    }
}
