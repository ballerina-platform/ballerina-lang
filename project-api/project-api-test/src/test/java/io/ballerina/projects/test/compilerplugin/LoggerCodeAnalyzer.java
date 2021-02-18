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
package io.ballerina.projects.test.compilerplugin;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.Node;

import java.io.PrintStream;

/**
 * @since 2.0.0
 */
public class LoggerCodeAnalyzer extends CodeAnalyzer {
    private final PrintStream out = System.out;

    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addCompilationAnalysisTask(compilationAnalysisTask);
        analysisContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisTask);
    }

    private final AnalysisTask<CompilationAnalysisContext> compilationAnalysisTask =
            compilationAnalysisContext -> {

                PackageCompilation compilation = compilationAnalysisContext.compilation();
                DiagnosticResult diagnosticResult = compilation.diagnosticResult();
                for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
                    out.println(diagnostic);
                }

                out.println("Compilation is completed!!!");
            };

    private final AnalysisTask<SyntaxNodeAnalysisContext> syntaxNodeAnalysisTask =
            syntaxNodeAnalysisContext -> {
                Node node = syntaxNodeAnalysisContext.node();
                out.println(node.getPosition());
            };
}
