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

import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 2.0.0
 */
class CodeAnalyzerManager {
    private final PackageCompilation compilation;
    private final CodeAnalyzerTasks codeAnalyzerTasks;

    public CodeAnalyzerManager(PackageCompilation compilation, CodeAnalyzerTasks codeAnalyzerTasks) {
        this.compilation = compilation;
        this.codeAnalyzerTasks = codeAnalyzerTasks;
    }

    static CodeAnalyzerManager from(PackageCompilation compilation,
                                    List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeAnalyzerTasks codeAnalyzerContexts = initCodeAnalyzers(compilerPluginContexts);
        return new CodeAnalyzerManager(compilation, codeAnalyzerContexts);
    }

    List<Diagnostic> runCodeAnalyzerTasks() {
        // TODO Implement CodeAnalyzerRunner

        // TODO I am running on the compilation analysis events for now.
        List<Diagnostic> reportedDiagnostics = new ArrayList<>();
        for (Map.Entry<CodeAnalyzer, List<CompilationAnalysisTask>> codeAnalyzerListEntry :
                codeAnalyzerTasks.compilationAnalysisTaskMap.entrySet()) {
            runCompilationAnalysisTask(codeAnalyzerListEntry.getValue(), reportedDiagnostics);
        }

        // Returning the reported the diagnostics for now.
        // We need to return AnalyzerTaskResult later
        return reportedDiagnostics;
    }

    private void runCompilationAnalysisTask(List<CompilationAnalysisTask> compilationAnalysisTasks,
                                            List<Diagnostic> reportedDiagnostics) {
        // This is not the best way to get the current package, you may get a different version of the package tree
        Package currentPackage = compilation.packageContext().project().currentPackage();
        for (CompilationAnalysisTask compilationAnalysisTask : compilationAnalysisTasks) {
            CompilationAnalysisContextIml analysisContext = new CompilationAnalysisContextIml(
                    currentPackage, compilation);
            compilationAnalysisTask.perform(analysisContext);
            reportedDiagnostics.addAll(analysisContext.reportedDiagnostics());
        }
    }

    private static CodeAnalyzerTasks initCodeAnalyzers(List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeAnalyzerTasks codeAnalyzerTasks = new CodeAnalyzerTasks();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            for (CodeAnalyzer codeAnalyzer : compilerPluginContext.codeAnalyzers()) {
                CodeAnalysisContextImpl codeAnalysisContext = new CodeAnalysisContextImpl(
                        codeAnalyzer, codeAnalyzerTasks);
                codeAnalyzer.init(codeAnalysisContext);
            }
        }
        return codeAnalyzerTasks;
    }

    static class CodeAnalysisContextImpl implements CodeAnalysisContext {
        private final CodeAnalyzerTasks codeAnalyzerTasks;
        private final CodeAnalyzer codeAnalyzer;

        CodeAnalysisContextImpl(CodeAnalyzer codeAnalyzer,
                                CodeAnalyzerTasks codeAnalyzerTasks) {
            this.codeAnalyzer = codeAnalyzer;
            this.codeAnalyzerTasks = codeAnalyzerTasks;
        }

        @Override
        public void addCompilationAnalysisTask(AnalysisTask<CompilationAnalysisContext> analysisTask) {
            codeAnalyzerTasks.addCompilationAnalysisTask(codeAnalyzer, new CompilationAnalysisTask(analysisTask));
        }

        @Override
        public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                              SyntaxKind... syntaxKinds) {
            codeAnalyzerTasks.addSyntaxNodeAnalysisTask(codeAnalyzer,
                    new SyntaxNodeAnalysisTask(analysisTask, syntaxKinds));
        }
    }

    static class CodeAnalyzerTasks {
        private final Map<CodeAnalyzer, List<CompilationAnalysisTask>> compilationAnalysisTaskMap = new HashMap<>();
        private final Map<CodeAnalyzer, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap = new HashMap<>();

        void addCompilationAnalysisTask(CodeAnalyzer codeAnalyzer, CompilationAnalysisTask analysisTask) {
            addTask(codeAnalyzer, compilationAnalysisTaskMap, analysisTask);

        }

        void addSyntaxNodeAnalysisTask(CodeAnalyzer codeAnalyzer, SyntaxNodeAnalysisTask analysisTask) {
            addTask(codeAnalyzer, syntaxNodeAnalysisTaskMap, analysisTask);
        }

        <T> void addTask(CodeAnalyzer codeAnalyzer, Map<CodeAnalyzer, List<T>> map, T task) {
            List<T> tasks = map.computeIfAbsent(codeAnalyzer, key -> new ArrayList<>());
            tasks.add(task);
        }

        public Map<CodeAnalyzer, List<CompilationAnalysisTask>> compilationAnalysisTaskMap() {
            return compilationAnalysisTaskMap;
        }

        public Map<CodeAnalyzer, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap() {
            return syntaxNodeAnalysisTaskMap;
        }
    }

    static class SyntaxNodeAnalysisTask {
        private final AnalysisTask<SyntaxNodeAnalysisContext> analysisTask;
        private final SyntaxKind[] syntaxKinds;

        SyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                               SyntaxKind[] syntaxKinds) {
            this.analysisTask = analysisTask;
            this.syntaxKinds = syntaxKinds;
        }

        public AnalysisTask<SyntaxNodeAnalysisContext> analysisTask() {
            return analysisTask;
        }

        public SyntaxKind[] syntaxKinds() {
            return syntaxKinds;
        }
    }

    static class CompilationAnalysisTask {
        private final AnalysisTask<CompilationAnalysisContext> analysisTask;

        CompilationAnalysisTask(AnalysisTask<CompilationAnalysisContext> analysisTask) {
            this.analysisTask = analysisTask;
        }

        void perform(CompilationAnalysisContext compilationAnalysisContext) {
            analysisTask.perform(compilationAnalysisContext);
        }
    }

    static class CompilationAnalysisContextIml extends CompilationAnalysisContext {
        private final Package currentPackage;
        private final PackageCompilation compilation;
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        public CompilationAnalysisContextIml(Package currentPackage, PackageCompilation compilation) {
            this.currentPackage = currentPackage;
            this.compilation = compilation;
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
}
