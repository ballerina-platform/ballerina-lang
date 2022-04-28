/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;
import io.ballerina.projects.plugins.ModifierTask;
import io.ballerina.projects.plugins.SourceModifierContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class CodeModifierManager {
    private final Package currentPackage;
    private final PackageCompilation compilation;
    private final CodeModifierTasks codeModifierTasks;

    private CodeModifierManager(PackageCompilation compilation, CodeModifierTasks codeModifierTasks) {
        // This is not the best way to get the current package, you may get a different version of the package tree
        this.currentPackage = compilation.packageContext().project().currentPackage();
        this.compilation = compilation;
        this.codeModifierTasks = codeModifierTasks;
    }

    static CodeModifierManager from(PackageCompilation compilation,
                                    List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeModifierTasks codeModifierTasks = initCodeModifiers(compilerPluginContexts);
        return new CodeModifierManager(compilation, codeModifierTasks);
    }

    CodeModifierResult runCodeModifiers(Package currentPackage) {
        CodeModifyTaskResult codeModifyTaskResult = getCodeModifyTaskResult();
        if (codeModifyTaskResult.containsSourceFile()
                || codeModifyTaskResult.containsTestSourceFile()
                || codeModifyTaskResult.containsResourceFile()
                || codeModifyTaskResult.containsTestResourceFile()) {
            Package packageWithGenSources = new PackageModifier().modifyPackage(currentPackage, codeModifyTaskResult);
            return new CodeModifierResult(packageWithGenSources, codeModifyTaskResult.reportedDiagnostics());
        } else {
            return new CodeModifierResult(null, codeModifyTaskResult.reportedDiagnostics());
        }
    }

    private static CodeModifierTasks initCodeModifiers(List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeModifierTasks codeModifierTasks = new CodeModifierTasks();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            for (CodeModifierInfo codeModifierInfo : compilerPluginContext.codeModifiers()) {
                CodeModifierContextImpl codeGeneratorContext = new CodeModifierContextImpl(
                        codeModifierInfo, codeModifierTasks);
                codeModifierInfo.codeModifier().init(codeGeneratorContext);
            }
        }
        return codeModifierTasks;
    }

    private CodeModifyTaskResult getCodeModifyTaskResult() {
        List<Diagnostic> reportedDiagnostics = runSyntaxNodeAnalysisTasks();
        CodeModifierTaskResultBuilder resultBuilder = new CodeModifierTaskResultBuilder();
        resultBuilder.addDiagnostics(reportedDiagnostics);
        runSourceModifierTasks(resultBuilder);
        return resultBuilder.build();
    }

    private void runSourceModifierTasks(CodeModifierTaskResultBuilder resultBuilder) {
        for (Map.Entry<CodeModifierInfo, List<SourceModifierTask>> codeModifierListEntry :
                codeModifierTasks.sourceModTaskMap.entrySet()) {
            runSourceModifierTask(codeModifierListEntry.getValue(), resultBuilder);
        }
    }

    private void runSourceModifierTask(List<SourceModifierTask> sourceModifierTasks,
                                       CodeModifierTaskResultBuilder resultBuilder) {
        for (SourceModifierTask sourceModifierTask : sourceModifierTasks) {
            SourceModifierContextImpl sourceModifyContext = new SourceModifierContextImpl(currentPackage, compilation);
            sourceModifierTask.perform(sourceModifyContext);

            resultBuilder.addDiagnostics(sourceModifyContext.reportedDiagnostics());
            resultBuilder.addSourceFiles(sourceModifyContext.modifiedSourceFiles());
            resultBuilder.addTestSourceFiles(sourceModifyContext.modifiedTestSourceFiles());
            resultBuilder.addResourceFiles(sourceModifyContext.modifiedResourceFiles());
            resultBuilder.addTestResourceFiles(sourceModifyContext.modifiedTestResourceFiles());
        }
    }

    private List<Diagnostic> runSyntaxNodeAnalysisTasks() {
        List<Diagnostic> reportedDiagnostics = new ArrayList<>();
        Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap = populateSyntaxNodeTaskMap();
        if (syntaxNodeAnalysisTaskMap.isEmpty()) {
            // There are no syntax node analyzers to run
            return reportedDiagnostics;
        }

        SyntaxNodeAnalysisTaskRunner taskRunner = new SyntaxNodeAnalysisTaskRunner(syntaxNodeAnalysisTaskMap,
                currentPackage, compilation);
        reportedDiagnostics.addAll(taskRunner.runTasks());
        return reportedDiagnostics;
    }

    private Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> populateSyntaxNodeTaskMap() {
        Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap = new HashMap<>();
        for (List<SyntaxNodeAnalysisTask> syntaxNodeAnalysisTasks :
                codeModifierTasks.syntaxNodeAnalysisTaskMap.values()) {
            populateSyntaxNodeTaskMap(syntaxNodeAnalysisTaskMap, syntaxNodeAnalysisTasks);
        }

        return syntaxNodeAnalysisTaskMap;
    }

    private void populateSyntaxNodeTaskMap(Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap,
                                           List<SyntaxNodeAnalysisTask> syntaxNodeAnalysisTasks) {
        for (SyntaxNodeAnalysisTask syntaxNodeTask : syntaxNodeAnalysisTasks) {
            populateSyntaxNodeTaskMap(syntaxNodeAnalysisTaskMap, syntaxNodeTask);
        }
    }

    private void populateSyntaxNodeTaskMap(Map<SyntaxKind, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap,
                                           SyntaxNodeAnalysisTask syntaxNodeAnalysisTask) {
        for (SyntaxKind syntaxKind : syntaxNodeAnalysisTask.syntaxKinds()) {
            List<SyntaxNodeAnalysisTask> syntaxNodeAnalysisTasks =
                    syntaxNodeAnalysisTaskMap.computeIfAbsent(syntaxKind, syntaxKind1 -> new ArrayList<>());
            syntaxNodeAnalysisTasks.add(syntaxNodeAnalysisTask);
        }
    }

    /**
     * A wrapper class for the source modifier task.
     *
     * @since 2201.0.3
     */
    private static class SourceModifierTask {
        private final ModifierTask<SourceModifierContext> modifierTask;
        private final CompilerPluginInfo compilerPluginInfo;

        public SourceModifierTask(ModifierTask<SourceModifierContext> modifierTask,
                                  CompilerPluginInfo compilerPluginInfo) {
            this.modifierTask = modifierTask;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        void perform(SourceModifierContext sourceModifierContext) {
            try {
                modifierTask.modify(sourceModifierContext);
            } catch (Throwable e) {
                // Used Throwable here catch any sort of error produced by the third-party compiler plugin code
                String message;
                if (compilerPluginInfo.kind().equals(CompilerPluginKind.PACKAGE_PROVIDED)) {
                    PackageProvidedCompilerPluginInfo pkgProvidedCompilerPluginInfo =
                            (PackageProvidedCompilerPluginInfo) compilerPluginInfo;
                    PackageDescriptor pkgDesc = pkgProvidedCompilerPluginInfo.packageDesc();
                    message = "The compiler extension in package '" +
                            pkgDesc.org() +
                            ":" + pkgDesc.name() +
                            ":" + pkgDesc.version() + "' failed to complete. ";
                } else {
                    message = "The compiler extension '" + compilerPluginInfo.compilerPlugin().getClass().getName()
                            + "' failed to complete. ";
                }
                throw new ProjectException(message + e.getMessage(), e);
            }
        }
    }

    /**
     * The default implementation of the {@code CodeModifierContextImpl}.
     *
     * @since 2201.0.3
     */
    private static class CodeModifierContextImpl implements CodeModifierContext {

        private final CodeModifierTasks codeModifierTasks;
        private final CodeModifierInfo codeModifierInfo;

        CodeModifierContextImpl(CodeModifierInfo codeModifierInfo,
                                CodeModifierTasks codeModifierTasks) {
            this.codeModifierInfo = codeModifierInfo;
            this.codeModifierTasks = codeModifierTasks;
        }

        @Override
        public void addSourceModifierTask(ModifierTask<SourceModifierContext> modifierTask) {
            codeModifierTasks.addSourceModifierTask(codeModifierInfo,
                    new SourceModifierTask(modifierTask, codeModifierInfo.compilerPluginInfo));
        }

        @Override
        public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                              SyntaxKind syntaxKind) {
            addSyntaxNodeAnalysisTask(analysisTask, Collections.singletonList(syntaxKind));
        }

        @Override
        public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                              Collection<SyntaxKind> syntaxKinds) {
            codeModifierTasks.addSyntaxNodeAnalysisTask(codeModifierInfo,
                    new SyntaxNodeAnalysisTask(analysisTask, syntaxKinds, codeModifierInfo.compilerPluginInfo));
        }
    }


    /**
     * A container that maintain various code modifier tasks against the {@code CodeModifier} instance.
     *
     * @since 2201.1.0
     */
    private static class CodeModifierTasks {
        private final Map<CodeModifierInfo, List<SourceModifierTask>> sourceModTaskMap = new HashMap<>();
        private final Map<CodeModifierInfo, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap = new HashMap<>();

        void addSourceModifierTask(CodeModifierInfo codeModifierInfo, SourceModifierTask modifierTask) {
            addTask(codeModifierInfo, sourceModTaskMap, modifierTask);

        }

        void addSyntaxNodeAnalysisTask(CodeModifierInfo codeModifierInfo, SyntaxNodeAnalysisTask analysisTask) {
            addTask(codeModifierInfo, syntaxNodeAnalysisTaskMap, analysisTask);
        }

        <T> void addTask(CodeModifierInfo codeModifierInfo, Map<CodeModifierInfo, List<T>> map, T task) {
            List<T> tasks = map.computeIfAbsent(codeModifierInfo, key -> new ArrayList<>());
            tasks.add(task);
        }
    }

    private static class SourceModifierContextImpl implements SourceModifierContext {
        private final Package currentPackage;
        private final PackageCompilation compilation;
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private final List<ModifiedSourceFile> sourceFiles = new ArrayList<>();
        private final List<ModifiedTestSourceFile> testSourceFiles = new ArrayList<>();
        private final List<ModifiedResourceFile> resourceFiles = new ArrayList<>();
        private final List<ModifiedTestResourceFile> testResourceFiles = new ArrayList<>();

        public SourceModifierContextImpl(Package currentPackage, PackageCompilation compilation) {
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
        public void modifySourceFile(TextDocument textDocument, DocumentId documentId) {
            for (ModuleId moduleId : currentPackage().moduleIds()) {
                Module module = currentPackage.module(moduleId);
                if (module.documentIds().contains(documentId)) {
                    sourceFiles.add(new ModifiedSourceFile(textDocument, documentId));
                    return;
                }
            }
            throw new IllegalArgumentException("There is no such document in the current package " +
                    "with the given identifier: " + documentId);
        }

        @Override
        public void modifyTestSourceFile(TextDocument textDocument, DocumentId testDocumentId) {
            for (ModuleId moduleId : currentPackage().moduleIds()) {
                Module module = currentPackage.module(moduleId);
                if (module.testDocumentIds().contains(testDocumentId)) {
                    testSourceFiles.add(new ModifiedTestSourceFile(textDocument, testDocumentId));
                    return;
                }
            }
            throw new IllegalArgumentException("There is no such test document in the current package " +
                    "with the given identifier: " + testDocumentId);
        }

        @Override
        public void modifyResourceFile(byte[] content, DocumentId documentId) {
            for (ModuleId moduleId : currentPackage().moduleIds()) {
                Module module = currentPackage.module(moduleId);
                if (module.resourceIds().contains(documentId)) {
                    resourceFiles.add(new ModifiedResourceFile(content, documentId));
                    return;
                }
            }
            throw new IllegalArgumentException("There is no such document in the current package " +
                    "with the given identifier: " + documentId);
        }

        @Override
        public void modifyTestResourceFile(byte[] content, DocumentId testDocumentId) {
            for (ModuleId moduleId : currentPackage().moduleIds()) {
                Module module = currentPackage.module(moduleId);
                if (module.testResourceIds().contains(testDocumentId)) {
                    testResourceFiles.add(new ModifiedTestResourceFile(content, testDocumentId));
                    return;
                }
            }
            throw new IllegalArgumentException("There is no such test document in the current package " +
                    "with the given identifier: " + testDocumentId);
        }

        @Override
        public void reportDiagnostic(Diagnostic diagnostic) {
            diagnostics.add(diagnostic);
        }

        Collection<Diagnostic> reportedDiagnostics() {
            return diagnostics;
        }

        Collection<ModifiedSourceFile> modifiedSourceFiles() {
            return sourceFiles;
        }

        Collection<ModifiedTestSourceFile> modifiedTestSourceFiles() {
            return testSourceFiles;
        }

        public List<ModifiedResourceFile> modifiedResourceFiles() {
            return resourceFiles;
        }

        public List<ModifiedTestResourceFile> modifiedTestResourceFiles() {
            return testResourceFiles;
        }
    }

    private static class ModifiedSourceFile {
        private final TextDocument textDocument;
        private final DocumentId documentId;

        public ModifiedSourceFile(TextDocument textDocument, DocumentId documentId) {
            this.textDocument = textDocument;
            this.documentId = documentId;
        }

        public TextDocument textDocument() {
            return textDocument;
        }

        public DocumentId documentId() {
            return documentId;
        }
    }

    private static class ModifiedTestSourceFile {
        private final TextDocument textDocument;
        private final DocumentId testDocumentId;

        public ModifiedTestSourceFile(TextDocument textDocument, DocumentId testDocumentId) {
            this.textDocument = textDocument;
            this.testDocumentId = testDocumentId;
        }

        public TextDocument textDocument() {
            return textDocument;
        }

        public DocumentId testDocumentId() {
            return testDocumentId;
        }
    }

    private static class ModifiedResourceFile {
        private final byte[] content;
        private final DocumentId documentId;

        public ModifiedResourceFile(byte[] content, DocumentId documentId) {
            this.content = content;
            this.documentId = documentId;
        }

        public byte[] content() {
            return content;
        }

        public DocumentId documentId() {
            return documentId;
        }
    }

    private static class ModifiedTestResourceFile {
        private final byte[] content;
        private final DocumentId testDocumentId;

        public ModifiedTestResourceFile(byte[] content, DocumentId testDocumentId) {
            this.content = content;
            this.testDocumentId = testDocumentId;
        }

        public byte[] content() {
            return content;
        }

        public DocumentId testDocumentId() {
            return testDocumentId;
        }
    }

    /**
     * This class holds a {@code CodeModifier} instance with additional details such the
     * containing compiler plugin's {@code CompilerPluginInfo} instance.
     *
     * @since 2.0.0
     */
    static class CodeModifierInfo {
        private final CodeModifier codeModifier;
        private final CompilerPluginInfo compilerPluginInfo;

        CodeModifierInfo(CodeModifier codeModifier, CompilerPluginInfo compilerPluginInfo) {
            this.codeModifier = codeModifier;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        CodeModifier codeModifier() {
            return codeModifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CodeModifierManager.CodeModifierInfo that = (CodeModifierManager.CodeModifierInfo) o;
            return Objects.equals(codeModifier, that.codeModifier) &&
                    Objects.equals(compilerPluginInfo, that.compilerPluginInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(codeModifier, compilerPluginInfo);
        }
    }

    private static class CodeModifyTaskResult {
        private final List<Diagnostic> reportedDiagnostics;
        private final Map<DocumentId, ModifiedSourceFile> sourceFilesMap;
        private final Map<DocumentId, ModifiedTestSourceFile> testSourceFilesMap;
        private final Map<DocumentId, ModifiedResourceFile> resourceFilesMap;
        private final Map<DocumentId, ModifiedTestResourceFile> testResourceFilesMap;

        public CodeModifyTaskResult(List<Diagnostic> reportedDiagnostics,
                                    Map<DocumentId, ModifiedSourceFile> sourceFilesMap,
                                    Map<DocumentId, ModifiedTestSourceFile> testSourceFilesMap,
                                    Map<DocumentId, ModifiedResourceFile> resourceFilesMap,
                                    Map<DocumentId, ModifiedTestResourceFile> testResourceFilesMap) {
            this.reportedDiagnostics = reportedDiagnostics;
            this.sourceFilesMap = sourceFilesMap;
            this.testSourceFilesMap = testSourceFilesMap;
            this.resourceFilesMap = resourceFilesMap;
            this.testResourceFilesMap = testResourceFilesMap;
        }

        Collection<Diagnostic> reportedDiagnostics() {
            return reportedDiagnostics;
        }

        ModifiedSourceFile sourceFile(DocumentId documentId) {
            return sourceFilesMap.get(documentId);
        }

        Collection<DocumentId> documentIds() {
            return sourceFilesMap.keySet();
        }

        boolean containsSourceFile() {
            return !sourceFilesMap.isEmpty();
        }

        ModifiedTestSourceFile testSourceFile(DocumentId documentId) {
            return testSourceFilesMap.get(documentId);
        }

        Collection<DocumentId> testDocumentIds() {
            return testSourceFilesMap.keySet();
        }

        boolean containsTestSourceFile() {
            return !testSourceFilesMap.isEmpty();
        }

        ModifiedResourceFile resourceFile(DocumentId documentId) {
            return resourceFilesMap.get(documentId);
        }

        Collection<DocumentId> resourceDocumentIds() {
            return resourceFilesMap.keySet();
        }

        boolean containsResourceFile() {
            return !resourceFilesMap.isEmpty();
        }

        ModifiedTestResourceFile testResourceFile(DocumentId documentId) {
            return testResourceFilesMap.get(documentId);
        }

        Collection<DocumentId> testResourceDocumentIds() {
            return testResourceFilesMap.keySet();
        }

        boolean containsTestResourceFile() {
            return !testResourceFilesMap.isEmpty();
        }
    }

    private static class CodeModifierTaskResultBuilder {
        private final List<Diagnostic> reportedDiagnostics;
        private final List<ModifiedSourceFile> modifiedSourceFiles;
        private final List<ModifiedTestSourceFile> modifiedTestSourceFiles;
        private final List<ModifiedResourceFile> modifiedResourceFiles;
        private final List<ModifiedTestResourceFile> modifiedTestResourceFiles;

        CodeModifierTaskResultBuilder() {
            this.reportedDiagnostics = new ArrayList<>();
            this.modifiedSourceFiles = new ArrayList<>();
            this.modifiedTestSourceFiles = new ArrayList<>();
            this.modifiedResourceFiles = new ArrayList<>();
            this.modifiedTestResourceFiles = new ArrayList<>();
        }

        CodeModifierTaskResultBuilder addDiagnostics(Collection<Diagnostic> diagnostics) {
            reportedDiagnostics.addAll(diagnostics);
            return this;
        }

        CodeModifierTaskResultBuilder addSourceFiles(Collection<ModifiedSourceFile> sourceFiles) {
            modifiedSourceFiles.addAll(sourceFiles);
            return this;
        }

        CodeModifierTaskResultBuilder addTestSourceFiles(Collection<ModifiedTestSourceFile> testSourceFiles) {
            modifiedTestSourceFiles.addAll(testSourceFiles);
            return this;
        }

        CodeModifierTaskResultBuilder addResourceFiles(Collection<ModifiedResourceFile> resourceFiles) {
            modifiedResourceFiles.addAll(resourceFiles);
            return this;
        }

        CodeModifierTaskResultBuilder addTestResourceFiles(Collection<ModifiedTestResourceFile> testResourceFiles) {
            modifiedTestResourceFiles.addAll(testResourceFiles);
            return this;
        }

        CodeModifyTaskResult build() {
            Map<DocumentId, ModifiedSourceFile> sourceFilesMap = new HashMap<>();
            Map<DocumentId, ModifiedTestSourceFile> testSourceFilesMap = new HashMap<>();
            Map<DocumentId, ModifiedResourceFile> resourceFilesMap = new HashMap<>();
            Map<DocumentId, ModifiedTestResourceFile> testResourceFilesMap = new HashMap<>();
            for (ModifiedSourceFile sourceFile : modifiedSourceFiles) {
                sourceFilesMap.put(sourceFile.documentId(), sourceFile);
            }
            for (ModifiedTestSourceFile testSourceFile : modifiedTestSourceFiles) {
                testSourceFilesMap.put(testSourceFile.testDocumentId(), testSourceFile);
            }
            for (ModifiedResourceFile resourceFile : modifiedResourceFiles) {
                resourceFilesMap.put(resourceFile.documentId(), resourceFile);
            }
            for (ModifiedTestResourceFile testResourceFile : modifiedTestResourceFiles) {
                testResourceFilesMap.put(testResourceFile.testDocumentId(), testResourceFile);
            }
            return new CodeModifyTaskResult(reportedDiagnostics, sourceFilesMap, testSourceFilesMap,
                    resourceFilesMap, testResourceFilesMap);
        }
    }

    private static class PackageModifier {

        Package modifyPackage(Package currentPackage, CodeModifyTaskResult codeModifyTaskResult) {
            Package newPackage = currentPackage;
            for (DocumentId documentId : codeModifyTaskResult.documentIds()) {
                newPackage = modifyModule(documentId.moduleId(), newPackage, codeModifyTaskResult);
            }
            for (DocumentId testDocumentId : codeModifyTaskResult.testDocumentIds()) {
                newPackage = modifyModule(testDocumentId.moduleId(), newPackage, codeModifyTaskResult);
            }
            for (DocumentId resourceDocumentId : codeModifyTaskResult.resourceDocumentIds()) {
                newPackage = modifyModule(resourceDocumentId.moduleId(), newPackage, codeModifyTaskResult);
            }
            for (DocumentId testResourceDocumentId : codeModifyTaskResult.testResourceDocumentIds()) {
                newPackage = modifyModule(testResourceDocumentId.moduleId(), newPackage, codeModifyTaskResult);
            }
            return newPackage;
        }

        private Package modifyModule(ModuleId moduleId, Package pkg, CodeModifyTaskResult codeModifyTaskResult) {
            Module module = pkg.module(moduleId);
            Module.Modifier modifier = module.modify();

            for (DocumentId documentId : pkg.module(moduleId).documentIds()) {
                ModifiedSourceFile modifiedSourceFile = codeModifyTaskResult.sourceFile(documentId);
                if (modifiedSourceFile != null) {
                    Document document = module.document(documentId);
                    updateModifiedDocument(document.name(), modifiedSourceFile.textDocument(), modifier, documentId);
                }
            }

            for (DocumentId testDocumentId : pkg.module(moduleId).testDocumentIds()) {
                ModifiedTestSourceFile modifiedTestSourceFile = codeModifyTaskResult.testSourceFile(testDocumentId);
                if (modifiedTestSourceFile != null) {
                    Document document = module.document(testDocumentId);
                    updateModifiedDocument(document.name(), modifiedTestSourceFile.textDocument(),
                            modifier, testDocumentId);
                }
            }

            for (DocumentId resourceDocumentId : pkg.module(moduleId).resourceIds()) {
                ModifiedResourceFile modifiedResourceFile = codeModifyTaskResult.resourceFile(resourceDocumentId);
                if (modifiedResourceFile != null) {
                    Resource resource = module.resource(resourceDocumentId);
                    updateModifiedResource(resource.name(), modifiedResourceFile.content(),
                            modifier, resourceDocumentId);
                }
            }

            for (DocumentId testResourceDocumentId : pkg.module(moduleId).testResourceIds()) {
                ModifiedTestResourceFile modifiedTestResourceFile =
                        codeModifyTaskResult.testResourceFile(testResourceDocumentId);
                if (modifiedTestResourceFile != null) {
                    Resource testResource = module.resource(testResourceDocumentId);
                    updateModifiedResource(testResource.name(), modifiedTestResourceFile.content(),
                            modifier, testResourceDocumentId);
                }
            }

            return modifier.apply().packageInstance();
        }

        private void updateModifiedDocument(String newDocFilename, TextDocument textDocument,
                                            Module.Modifier modifier, DocumentId documentId) {
            DocumentConfig documentConfig = DocumentConfig.from(documentId,
                    textDocument.toString(), newDocFilename);
            DocumentContext documentContext = DocumentContext.from(documentConfig);
            modifier.updateDocument(documentContext);
        }

        private void updateModifiedResource(String newResourceFilename, byte[] content,
                                            Module.Modifier modifier, DocumentId documentId) {
            ResourceConfig resourceConfig = ResourceConfig.from(documentId, newResourceFilename, content);
            ResourceContext resourceContext = ResourceContext.from(resourceConfig);
            modifier.updateResource(resourceContext);
        }
    }
}
