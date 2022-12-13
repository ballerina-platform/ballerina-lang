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
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CodeGenerator;
import io.ballerina.projects.plugins.CodeGeneratorContext;
import io.ballerina.projects.plugins.GeneratorTask;
import io.ballerina.projects.plugins.SourceGeneratorContext;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class CodeGeneratorManager {
    private final Package currentPackage;
    private final PackageCompilation compilation;
    private final CodeGeneratorTasks codeGeneratorTasks;

    private CodeGeneratorManager(PackageCompilation compilation, CodeGeneratorTasks codeGeneratorTasks) {
        // This is not the best way to get the current package, you may get a different version of the package tree
        this.currentPackage = compilation.packageContext().project().currentPackage();
        this.compilation = compilation;
        this.codeGeneratorTasks = codeGeneratorTasks;
    }

    static CodeGeneratorManager from(PackageCompilation compilation,
                                     List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeGeneratorTasks codeGeneratorTasks = initCodeGenerators(compilerPluginContexts);
        return new CodeGeneratorManager(compilation, codeGeneratorTasks);
    }

    CodeGeneratorResult runCodeGenerators(Package currentPackage) {
        CodeGenTaskResult codeGenTaskResult = getCodeGenTaskResult();
        if (codeGenTaskResult.containsSourceFile()
                || codeGenTaskResult.containsTestSourceFile()
                || codeGenTaskResult.containsResourceFile()
                || codeGenTaskResult.containsTestResourceFile()) {
            Package packageWithGenSources = new PackageModifier().modifyPackage(currentPackage, codeGenTaskResult);
            return new CodeGeneratorResult(packageWithGenSources, codeGenTaskResult.reportedDiagnostics());
        } else {
            return new CodeGeneratorResult(null, codeGenTaskResult.reportedDiagnostics());
        }
    }

    private static CodeGeneratorTasks initCodeGenerators(List<CompilerPluginContextIml> compilerPluginContexts) {
        CodeGeneratorTasks codeGeneratorTasks = new CodeGeneratorTasks();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            for (CodeGeneratorInfo codeGeneratorInfo : compilerPluginContext.codeGenerators()) {
                CodeGeneratorContextImpl codeGeneratorContext = new CodeGeneratorContextImpl(
                        codeGeneratorInfo, codeGeneratorTasks);
                codeGeneratorInfo.codeGenerator().init(codeGeneratorContext);
            }
        }
        return codeGeneratorTasks;
    }

    private CodeGenTaskResult getCodeGenTaskResult() {
        List<Diagnostic> reportedDiagnostics = runSyntaxNodeAnalysisTasks();
        CodeGeneratorTaskResultBuilder resultBuilder = new CodeGeneratorTaskResultBuilder();
        resultBuilder.addDiagnostics(reportedDiagnostics);
        runSourceGeneratorTasks(resultBuilder);
        return resultBuilder.build();
    }

    private void runSourceGeneratorTasks(CodeGeneratorTaskResultBuilder resultBuilder) {
        for (Map.Entry<CodeGeneratorInfo, List<SourceGeneratorTask>> codeGeneratorListEntry :
                codeGeneratorTasks.sourceGenTaskMap.entrySet()) {
            runSourceGeneratorTask(codeGeneratorListEntry.getValue(), resultBuilder);
        }
    }

    private void runSourceGeneratorTask(List<SourceGeneratorTask> sourceGeneratorTasks,
                                        CodeGeneratorTaskResultBuilder resultBuilder) {
        for (SourceGeneratorTask sourceGeneratorTask : sourceGeneratorTasks) {
            SourceGeneratorContextImpl sourceGenContext = new SourceGeneratorContextImpl(currentPackage, compilation);
            sourceGeneratorTask.perform(sourceGenContext);

            resultBuilder.addDiagnostics(sourceGenContext.reportedDiagnostics());
            resultBuilder.addSourceFiles(sourceGenContext.generatedSourceFiles());
            resultBuilder.addTestSourceFiles(sourceGenContext.generatedTestSourceFiles());
            resultBuilder.addResourceFiles(sourceGenContext.generatedResourceFiles());
            resultBuilder.addTestResourceFiles(sourceGenContext.generatedTestResourceFiles());
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
                codeGeneratorTasks.syntaxNodeAnalysisTaskMap.values()) {
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
     * A wrapper class for the compilation analysis task.
     *
     * @since 2.0.0
     */
    private static class SourceGeneratorTask {
        private final GeneratorTask<SourceGeneratorContext> generatorTask;
        private final CompilerPluginInfo compilerPluginInfo;

        SourceGeneratorTask(GeneratorTask<SourceGeneratorContext> generatorTask,
                            CompilerPluginInfo compilerPluginInfo) {
            this.generatorTask = generatorTask;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        void perform(SourceGeneratorContext sourceGeneratorContext) {
            try {
                generatorTask.generate(sourceGeneratorContext);
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
     * The default implementation of the {@code CodeGeneratorContext}.
     *
     * @since 2.0.0
     */
    private static class CodeGeneratorContextImpl implements CodeGeneratorContext {
        private final CodeGeneratorTasks codeGeneratorTasks;
        private final CodeGeneratorInfo codeGeneratorInfo;

        CodeGeneratorContextImpl(CodeGeneratorInfo codeGeneratorInfo,
                                 CodeGeneratorTasks codeGeneratorTasks) {
            this.codeGeneratorInfo = codeGeneratorInfo;
            this.codeGeneratorTasks = codeGeneratorTasks;
        }

        @Override
        public void addSourceGeneratorTask(GeneratorTask<SourceGeneratorContext> generatorTask) {
            codeGeneratorTasks.addSourceGeneratorTask(codeGeneratorInfo,
                    new SourceGeneratorTask(generatorTask, codeGeneratorInfo.compilerPluginInfo));
        }

        @Override
        public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                              SyntaxKind syntaxKind) {
            addSyntaxNodeAnalysisTask(analysisTask, Collections.singletonList(syntaxKind));
        }

        @Override
        public void addSyntaxNodeAnalysisTask(AnalysisTask<SyntaxNodeAnalysisContext> analysisTask,
                                              Collection<SyntaxKind> syntaxKinds) {
            codeGeneratorTasks.addSyntaxNodeAnalysisTask(codeGeneratorInfo,
                    new SyntaxNodeAnalysisTask(analysisTask, syntaxKinds, codeGeneratorInfo.compilerPluginInfo));
        }
    }


    /**
     * A container that maintain various code analyzer tasks against the {@code CodeGenerator} instance.
     *
     * @since 2.0.0
     */
    private static class CodeGeneratorTasks {
        private final Map<CodeGeneratorInfo, List<SourceGeneratorTask>> sourceGenTaskMap = new HashMap<>();
        private final Map<CodeGeneratorInfo, List<SyntaxNodeAnalysisTask>> syntaxNodeAnalysisTaskMap = new HashMap<>();

        void addSourceGeneratorTask(CodeGeneratorInfo codeGeneratorInfo, SourceGeneratorTask generatorTask) {
            addTask(codeGeneratorInfo, sourceGenTaskMap, generatorTask);

        }

        void addSyntaxNodeAnalysisTask(CodeGeneratorInfo codeGeneratorInfo, SyntaxNodeAnalysisTask analysisTask) {
            addTask(codeGeneratorInfo, syntaxNodeAnalysisTaskMap, analysisTask);
        }

        <T> void addTask(CodeGeneratorInfo codeGeneratorInfo, Map<CodeGeneratorInfo, List<T>> map, T task) {
            List<T> tasks = map.computeIfAbsent(codeGeneratorInfo, key -> new ArrayList<>());
            tasks.add(task);
        }
    }

    private static class SourceGeneratorContextImpl implements SourceGeneratorContext {
        private final Package currentPackage;
        private final PackageCompilation compilation;
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private final List<GeneratedSourceFile> sourceFiles = new ArrayList<>();
        private final List<GeneratedTestFile> testSourceFiles = new ArrayList<>();
        private final List<GeneratedResourceFile> resourceFiles = new ArrayList<>();
        private final List<GeneratedTestResourceFile> testResourceFiles = new ArrayList<>();

        private final ModuleId defaultModuleId;

        public SourceGeneratorContextImpl(Package currentPackage, PackageCompilation compilation) {
            this.currentPackage = currentPackage;
            this.compilation = compilation;
            this.defaultModuleId = currentPackage.getDefaultModule().moduleId();
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
        public void addSourceFile(TextDocument textDocument, String filenamePrefix, ModuleId moduleId) {
            if (this.currentPackage.project().kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.UNSUPPORTED_COMPILER_PLUGIN_TYPE.diagnosticId(),
                        "Skipped adding the generated source file with prefix \"" + filenamePrefix +
                                "\". Source file generation is not supported with standalone bal files",
                        DiagnosticSeverity.WARNING);
                reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
                return;
            }
            if (currentPackage.moduleIds().contains(moduleId)) {
                sourceFiles.add(new GeneratedSourceFile(textDocument, filenamePrefix, moduleId));
            } else {
                throw new IllegalArgumentException("There is no such module in the current package " +
                        "with the given identifier: " + moduleId);
            }
        }

        @Override
        public void addSourceFile(TextDocument textDocument, String filenamePrefix) {
            addSourceFile(textDocument, filenamePrefix, defaultModuleId);
        }

        @Override
        public void addTestSourceFile(TextDocument textDocument, String filenamePrefix, ModuleId moduleId) {
            if (this.currentPackage.project().kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.UNSUPPORTED_COMPILER_PLUGIN_TYPE.diagnosticId(),
                        "Skipped adding the generated test source file with prefix \"" + filenamePrefix +
                                "\". Test source file generation is not supported with standalone bal files",
                        DiagnosticSeverity.WARNING);
                reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
                return;
            }
            if (!filenamePrefix.startsWith(ProjectConstants.TEST_DIR_NAME + "/")) {
                filenamePrefix = ProjectConstants.TEST_DIR_NAME + "/" + filenamePrefix;
            }
            if (currentPackage.moduleIds().contains(moduleId)) {
                testSourceFiles.add(new GeneratedTestFile(textDocument, filenamePrefix, moduleId));
            } else {
                throw new IllegalArgumentException("There is no such module in the current package " +
                        "with the given identifier: " + moduleId);
            }
        }

        @Override
        public void addTestSourceFile(TextDocument textDocument, String filenamePrefix) {
            addTestSourceFile(textDocument, filenamePrefix, defaultModuleId);
        }

        @Override
        public void addResourceFile(byte[] content, String fileName, ModuleId moduleId) {
            if (currentPackage.moduleIds().contains(moduleId)) {
                resourceFiles.add(new GeneratedResourceFile(content, fileName, moduleId));
            } else {
                throw new IllegalArgumentException("There is no such module in the current package " +
                        "with the given identifier: " + moduleId);
            }
        }

        @Override
        public void addResourceFile(byte[] content, String fileName) {
            addResourceFile(content, fileName, defaultModuleId);
        }

        @Override
        public void addTestResourceFile(byte[] content, String fileName, ModuleId moduleId) {
            if (currentPackage.moduleIds().contains(moduleId)) {
                testResourceFiles.add(new GeneratedTestResourceFile(content, fileName, moduleId));
            } else {
                throw new IllegalArgumentException("There is no such module in the current package " +
                        "with the given identifier: " + moduleId);
            }
        }

        @Override
        public void addTestResourceFile(byte[] content, String fileName) {
            addTestResourceFile(content, fileName, defaultModuleId);
        }

        @Override
        public void reportDiagnostic(Diagnostic diagnostic) {
            diagnostics.add(diagnostic);
        }

        Collection<Diagnostic> reportedDiagnostics() {
            return diagnostics;
        }

        Collection<GeneratedSourceFile> generatedSourceFiles() {
            return sourceFiles;
        }

        Collection<GeneratedTestFile> generatedTestSourceFiles() {
            return testSourceFiles;
        }

        Collection<GeneratedResourceFile> generatedResourceFiles() {
            return resourceFiles;
        }

        Collection<GeneratedTestResourceFile> generatedTestResourceFiles() {
            return testResourceFiles;
        }
    }

    /**
     * Represents the null location. This can used to create diagnostics
     * which cannot be related to a location in the document.
     *
     * @since 2.0.0
     */
    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }

    private static class GeneratedSourceFile {
        private final TextDocument textDocument;
        private final String filenamePrefix;
        private final ModuleId moduleId;

        public GeneratedSourceFile(TextDocument textDocument, String filenamePrefix, ModuleId moduleId) {
            this.textDocument = textDocument;
            this.filenamePrefix = filenamePrefix;
            this.moduleId = moduleId;
        }

        public TextDocument textDocument() {
            return textDocument;
        }

        public String filenamePrefix() {
            return filenamePrefix;
        }

        public ModuleId moduleId() {
            return moduleId;
        }
    }

    private static class GeneratedTestFile {
        private final TextDocument textDocument;
        private final String filenamePrefix;
        private final ModuleId moduleId;

        public GeneratedTestFile(TextDocument textDocument, String filenamePrefix, ModuleId moduleId) {
            this.textDocument = textDocument;
            this.filenamePrefix = filenamePrefix;
            this.moduleId = moduleId;
        }

        public TextDocument textDocument() {
            return textDocument;
        }

        public String filenamePrefix() {
            return filenamePrefix;
        }

        public ModuleId moduleId() {
            return moduleId;
        }
    }

    private static class GeneratedResourceFile {
        private final byte[] content;
        private final String filename;
        private final ModuleId moduleId;

        public GeneratedResourceFile(byte[] content, String filename, ModuleId moduleId) {
            this.content = content;
            this.filename = filename;
            this.moduleId = moduleId;
        }

        public byte[] content() {
            return content;
        }

        public String filename() {
            return filename;
        }

        public ModuleId moduleId() {
            return moduleId;
        }
    }

    private static class GeneratedTestResourceFile {
        private final byte[] content;
        private final String filename;
        private final ModuleId moduleId;

        public GeneratedTestResourceFile(byte[] content, String filename, ModuleId moduleId) {
            this.content = content;
            this.filename = filename;
            this.moduleId = moduleId;
        }

        public byte[] content() {
            return content;
        }

        public String filename() {
            return filename;
        }

        public ModuleId moduleId() {
            return moduleId;
        }
    }

    /**
     * This class holds a {@code CodeGenerator} instance with additional details such the
     * containing compiler plugin's {@code CompilerPluginInfo} instance.
     *
     * @since 2.0.0
     */
    static class CodeGeneratorInfo {
        private final CodeGenerator codeGenerator;
        private final CompilerPluginInfo compilerPluginInfo;

        CodeGeneratorInfo(CodeGenerator codeGenerator, CompilerPluginInfo compilerPluginInfo) {
            this.codeGenerator = codeGenerator;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        CodeGenerator codeGenerator() {
            return codeGenerator;
        }

        CompilerPluginInfo compilerPluginInfo() {
            return compilerPluginInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CodeGeneratorManager.CodeGeneratorInfo that = (CodeGeneratorManager.CodeGeneratorInfo) o;
            return Objects.equals(codeGenerator, that.codeGenerator) &&
                    Objects.equals(compilerPluginInfo, that.compilerPluginInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(codeGenerator, compilerPluginInfo);
        }
    }

    private static class CodeGenTaskResult {
        private final List<Diagnostic> reportedDiagnostics;
        private final Map<ModuleId, List<GeneratedSourceFile>> sourceFilesMap;
        private final Map<ModuleId, List<GeneratedTestFile>> testSourceFilesMap;
        private final Map<ModuleId, List<GeneratedResourceFile>> resourceFilesMap;
        private final Map<ModuleId, List<GeneratedTestResourceFile>> testResourceFilesMap;

        public CodeGenTaskResult(List<Diagnostic> reportedDiagnostics,
                                 Map<ModuleId, List<GeneratedSourceFile>> sourceFilesMap,
                                 Map<ModuleId, List<GeneratedTestFile>> testSourceFilesMap,
                                 Map<ModuleId, List<GeneratedResourceFile>> resourceFilesMap,
                                 Map<ModuleId, List<GeneratedTestResourceFile>> testResourceFilesMap) {
            this.reportedDiagnostics = reportedDiagnostics;
            this.sourceFilesMap = sourceFilesMap;
            this.testSourceFilesMap = testSourceFilesMap;
            this.resourceFilesMap = resourceFilesMap;
            this.testResourceFilesMap = testResourceFilesMap;
        }

        Collection<Diagnostic> reportedDiagnostics() {
            return reportedDiagnostics;
        }

        Collection<GeneratedSourceFile> sourceFiles(ModuleId moduleId) {
            if (sourceFilesMap.get(moduleId) == null) {
                return Collections.emptyList();
            }
            return sourceFilesMap.get(moduleId);
        }

        Collection<GeneratedTestFile> testSourceFiles(ModuleId moduleId) {
            if (testSourceFilesMap.get(moduleId) == null) {
                return Collections.emptyList();
            }
            return testSourceFilesMap.get(moduleId);
        }

        Collection<GeneratedResourceFile> resourceFiles(ModuleId moduleId) {
            if (resourceFilesMap.get(moduleId) == null) {
                return Collections.emptyList();
            }
            return resourceFilesMap.get(moduleId);
        }

        Collection<GeneratedTestResourceFile> testResourceFiles(ModuleId moduleId) {
            if (testResourceFilesMap.get(moduleId) == null) {
                return Collections.emptyList();
            }
            return testResourceFilesMap.get(moduleId);
        }

        boolean containsSourceFile() {
            return !sourceFilesMap.isEmpty();
        }

        boolean containsTestSourceFile() {
            return !testSourceFilesMap.isEmpty();
        }

        boolean containsResourceFile() {
            return !resourceFilesMap.isEmpty();
        }

        boolean containsTestResourceFile() {
            return !testResourceFilesMap.isEmpty();
        }
    }

    private static class CodeGeneratorTaskResultBuilder {
        private final List<Diagnostic> reportedDiagnostics;
        private final List<GeneratedSourceFile> generatedSourceFiles;
        private final List<GeneratedTestFile> generatedTestSourceFiles;
        private final List<GeneratedResourceFile> generatedResourceFiles;
        private final List<GeneratedTestResourceFile> generatedTestResourceFiles;

        CodeGeneratorTaskResultBuilder() {
            this.reportedDiagnostics = new ArrayList<>();
            this.generatedSourceFiles = new ArrayList<>();
            this.generatedTestSourceFiles = new ArrayList<>();
            this.generatedResourceFiles = new ArrayList<>();
            this.generatedTestResourceFiles = new ArrayList<>();
        }

        CodeGeneratorTaskResultBuilder addDiagnostics(Collection<Diagnostic> diagnostics) {
            reportedDiagnostics.addAll(diagnostics);
            return this;
        }

        CodeGeneratorTaskResultBuilder addSourceFiles(Collection<GeneratedSourceFile> sourceFiles) {
            generatedSourceFiles.addAll(sourceFiles);
            return this;
        }

        CodeGeneratorTaskResultBuilder addTestSourceFiles(Collection<GeneratedTestFile> testSourceFiles) {
            generatedTestSourceFiles.addAll(testSourceFiles);
            return this;
        }

        CodeGeneratorTaskResultBuilder addResourceFiles(Collection<GeneratedResourceFile> resourceFiles) {
            generatedResourceFiles.addAll(resourceFiles);
            return this;
        }

        CodeGeneratorTaskResultBuilder addTestResourceFiles(Collection<GeneratedTestResourceFile> testResourceFiles) {
            generatedTestResourceFiles.addAll(testResourceFiles);
            return this;
        }

        CodeGenTaskResult build() {
            Map<ModuleId, List<GeneratedSourceFile>> sourceFilesMap = new HashMap<>();
            for (GeneratedSourceFile sourceFile : generatedSourceFiles) {
                sourceFilesMap.computeIfAbsent(sourceFile.moduleId(), key -> new ArrayList<>()).add(sourceFile);
            }
            Map<ModuleId, List<GeneratedTestFile>> testSourceFilesMap = new HashMap<>();
            for (GeneratedTestFile testFile : generatedTestSourceFiles) {
                testSourceFilesMap.computeIfAbsent(testFile.moduleId(), key -> new ArrayList<>()).add(testFile);
            }
            Map<ModuleId, List<GeneratedResourceFile>> resourceFilesMap = new HashMap<>();
            for (GeneratedResourceFile resourceFile : generatedResourceFiles) {
                resourceFilesMap.computeIfAbsent(resourceFile.moduleId(), key -> new ArrayList<>()).add(resourceFile);
            }
            Map<ModuleId, List<GeneratedTestResourceFile>> testResourceFilesMap = new HashMap<>();
            for (GeneratedTestResourceFile testResourceFile : generatedTestResourceFiles) {
                testResourceFilesMap.computeIfAbsent(testResourceFile.moduleId(), key -> new ArrayList<>())
                        .add(testResourceFile);
            }
            return new CodeGenTaskResult(reportedDiagnostics, sourceFilesMap, testSourceFilesMap,
                    resourceFilesMap, testResourceFilesMap);
        }
    }

    private static class GeneratedFilename {
        private static final String GENERATED_INDICATOR = "generated";
        private static final String EXTENSION = ".bal";
        private final String prefix;
        private int uniqueCounter;

        GeneratedFilename(String prefix, int uniqueCounter) {
            String filenamePrefix = prefix;
            if (filenamePrefix == null || filenamePrefix.isEmpty()) {
                filenamePrefix = "0";
            }
            if (filenamePrefix.endsWith(".bal")) {
                // Remove .bal extension
                filenamePrefix = filenamePrefix.substring(0, filenamePrefix.length() - 4);
            }
            this.prefix = filenamePrefix;
            this.uniqueCounter = uniqueCounter;
        }

        void updateUniqueCounter(int value) {
            uniqueCounter = value;
        }

        public String toString() {
            return prefix + "-" + GENERATED_INDICATOR + "_" + uniqueCounter + EXTENSION;
        }
    }

    private static class PackageModifier {
        private int filenameCounter = 0;

        Package modifyPackage(Package currentPackage, CodeGenTaskResult codeGenTaskResult) {
            Collection<ModuleId> moduleIds = currentPackage.moduleIds();
            Package newPackage = currentPackage;
            for (ModuleId moduleId : moduleIds) {
                newPackage = modifyModule(moduleId, newPackage, codeGenTaskResult);
            }
            return newPackage;
        }

        private Package modifyModule(ModuleId moduleId, Package pkg, CodeGenTaskResult codeGenTaskResult) {
            Module module = pkg.module(moduleId);
            List<String> docNames = getDocNamesInModule(module);

            Module.Modifier modifier = module.modify();
            for (GeneratedSourceFile sourceFile : codeGenTaskResult.sourceFiles(moduleId)) {
                String uniqueFilename = getUniqueFilename(sourceFile, docNames);
                docNames.add(uniqueFilename);
                addGeneratedDocument(uniqueFilename, sourceFile.textDocument, modifier, moduleId);
            }

            for (GeneratedTestFile testSourceFile : codeGenTaskResult.testSourceFiles(moduleId)) {
                String uniqueFilename = getUniqueFilename(testSourceFile, docNames);
                docNames.add(uniqueFilename);
                addGeneratedTestDocument(uniqueFilename, testSourceFile.textDocument, modifier, moduleId);
            }

            for (GeneratedResourceFile resourceFile : codeGenTaskResult.resourceFiles(moduleId)) {
                docNames.add(resourceFile.filename());
                addGeneratedResource(resourceFile.filename(), resourceFile.content, modifier, moduleId);
            }

            for (GeneratedTestResourceFile testResourceFile : codeGenTaskResult.testResourceFiles(moduleId)) {
                docNames.add(testResourceFile.filename());
                addGeneratedTestResource(testResourceFile.filename(), testResourceFile.content, modifier, moduleId);
            }

            return modifier.apply().packageInstance();
        }

        private void addGeneratedResource(String newResourceFilename,
                                          byte[] content,
                                          Module.Modifier modifier,
                                          ModuleId moduleId) {
            DocumentId documentId = DocumentId.create(newResourceFilename, moduleId);
            ResourceConfig resourceConfig = ResourceConfig.from(documentId, newResourceFilename, content);
            modifier.addResource(resourceConfig);
        }

        private void addGeneratedTestResource(String newTestResourceFilename,
                                              byte[] content,
                                              Module.Modifier modifier,
                                              ModuleId moduleId) {
            DocumentId documentId = DocumentId.create(newTestResourceFilename, moduleId);
            ResourceConfig resourceConfig = ResourceConfig.from(documentId, newTestResourceFilename, content);
            modifier.addTestResource(resourceConfig);
        }

        private void addGeneratedDocument(String newDocFilename,
                                          TextDocument textDocument,
                                          Module.Modifier modifier,
                                          ModuleId moduleId) {
            DocumentId documentId = DocumentId.create(newDocFilename, moduleId);
            DocumentConfig documentConfig = DocumentConfig.from(documentId,
                    textDocument.toString(), newDocFilename);
            modifier.addDocument(documentConfig);
        }

        private void addGeneratedTestDocument(String newDocFilename,
                                              TextDocument textDocument,
                                              Module.Modifier modifier,
                                              ModuleId moduleId) {
            DocumentId documentId = DocumentId.create(newDocFilename, moduleId);
            DocumentConfig documentConfig = DocumentConfig.from(documentId,
                    textDocument.toString(), newDocFilename);
            modifier.addTestDocument(documentConfig);
        }

        private String getUniqueFilename(GeneratedSourceFile sourceFile, List<String> docNames) {
            GeneratedFilename genFileName = new GeneratedFilename(
                    sourceFile.filenamePrefix(), getNextFilenameCounter());
            // Making sure that we have a unique filename
            while (!uniqueFileName(genFileName.toString(), docNames)) {
                genFileName.updateUniqueCounter(getNextFilenameCounter());
            }

            return genFileName.toString();
        }

        private String getUniqueFilename(GeneratedTestFile testSourceFile, List<String> docNames) {
            GeneratedFilename genFileName = new GeneratedFilename(
                    testSourceFile.filenamePrefix(), getNextFilenameCounter());
            // Making sure that we have a unique filename
            while (!uniqueFileName(genFileName.toString(), docNames)) {
                genFileName.updateUniqueCounter(getNextFilenameCounter());
            }

            return genFileName.toString();
        }

        private List<String> getDocNamesInModule(Module module) {
            List<String> docNames = new ArrayList<>();
            for (DocumentId documentId : module.documentIds()) {
                docNames.add(module.document(documentId).name());
            }
            return docNames;
        }

        private int getNextFilenameCounter() {
            return ++filenameCounter;
        }

        private boolean uniqueFileName(String filename, List<String> existingFilenames) {
            return !existingFilenames.contains(filename);
        }
    }
}
