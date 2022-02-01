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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Responsible for running {@code SyntaxNodeAnalysisTask} tasks.
 *
 * @since 2.0.0
 */
class SyntaxNodeAnalysisTaskRunner {
    private final Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap;
    private final Package currentPackage;
    private final PackageCompilation compilation;

    public SyntaxNodeAnalysisTaskRunner(Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap,
                                        Package currentPackage,
                                        PackageCompilation compilation) {
        this.syntaxNodeAnalysisTaskMap = syntaxNodeAnalysisTaskMap;
        this.currentPackage = currentPackage;
        this.compilation = compilation;
    }

    List<Diagnostic> runTasks() {
        // Here we are iterating through all the non-test documents in the current package.
        List<Diagnostic> reportedDiagnostics = new ArrayList<>();
        PackageContext packageContext = compilation.packageContext();
        for (ModuleId moduleId : packageContext.moduleIds()) {
            runTasks(packageContext.moduleContext(moduleId), reportedDiagnostics);
        }
        return reportedDiagnostics;
    }

    private void runTasks(ModuleContext moduleContext, List<Diagnostic> reportedDiagnostics) {
        for (DocumentId srcDocumentId : moduleContext.srcDocumentIds()) {
            DocumentContext documentContext = moduleContext.documentContext(srcDocumentId);
            runTasks(documentContext.syntaxTree(), moduleContext.moduleId(),
                    srcDocumentId, reportedDiagnostics);
        }
    }

    private void runTasks(SyntaxTree syntaxTree,
                          ModuleId moduleId,
                          DocumentId documentId,
                          List<Diagnostic> reportedDiagnostics) {
        SyntaxTreeVisitor syntaxTreeVisitor = new SyntaxTreeVisitor(syntaxNodeAnalysisTaskMap, currentPackage,
                compilation, moduleId, documentId, syntaxTree, compilation.getSemanticModel(moduleId));
        reportedDiagnostics.addAll(syntaxTreeVisitor.runAnalysisTasks());
    }
}
