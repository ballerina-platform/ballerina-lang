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

import io.ballerina.projects.plugins.CompilerLifecycleContext;
import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleListener;
import io.ballerina.projects.plugins.CompilerLifecycleTask;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages compiler lifecycle listener tasks.
 *
 * @since 2.0.0
 */
class CompilerLifecycleManager {
    private final Package currentPackage;
    private final PackageCompilation compilation;
    private final LifecycleTasks lifecycleTasks;

    public CompilerLifecycleManager(PackageCompilation compilation, LifecycleTasks lifecycleTasks) {
        this.currentPackage = compilation.packageContext().project().currentPackage();
        this.compilation = compilation;
        this.lifecycleTasks = lifecycleTasks;
    }

    static CompilerLifecycleManager from(PackageCompilation compilation,
                                         List<CompilerPluginContextIml> compilerPluginContexts) {
        LifecycleTasks lifecycleTasks = initLifecycleListeners(compilerPluginContexts);
        return new CompilerLifecycleManager(compilation, lifecycleTasks);
    }

    private static LifecycleTasks initLifecycleListeners(List<CompilerPluginContextIml> compilerPluginContexts) {
        var lifecycleTasks = new LifecycleTasks();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            for (var lifecycleListenerInfo : compilerPluginContext.getLifecycleListeners()) {
                var lifecycleListenerContext = new LifecycleContextImpl(lifecycleListenerInfo, lifecycleTasks);
                lifecycleListenerInfo.getLifecycleListener().init(lifecycleListenerContext);
            }
        }
        return lifecycleTasks;
    }

    public List<Diagnostic> runCodeGeneratedTasks(Path binaryPath) {
        CompilerLifecycleEventContextImpl lifecycleEventContext =
                new CompilerLifecycleEventContextImpl(this.currentPackage, this.compilation);
        lifecycleEventContext.setBinaryPath(binaryPath);

        for (List<CodeGenerationCompletedTask> taskList : lifecycleTasks.codeGenerationCompletedTasks.values()) {
            for (CodeGenerationCompletedTask codeGenerationCompletedTask : taskList) {
                codeGenerationCompletedTask.lifecycleTask.perform(lifecycleEventContext);
            }
        }

        List<Diagnostic> reportedDiagnostics = new ArrayList<>(lifecycleEventContext.reportedDiagnostics());
        return reportedDiagnostics;
    }

    static class LifecycleContextImpl implements CompilerLifecycleContext {

        private final LifecycleListenerInfo lifecycleListenerInfo;
        private final LifecycleTasks lifecycleTasks;

        public LifecycleContextImpl(LifecycleListenerInfo lifecycleListenerInfo,
                                    LifecycleTasks lifecycleTasks) {
            this.lifecycleListenerInfo = lifecycleListenerInfo;
            this.lifecycleTasks = lifecycleTasks;
        }

        @Override
        public void addCodeGenerationCompletedTask(
                CompilerLifecycleTask<CompilerLifecycleEventContext> lifecycleTask) {
            lifecycleTasks.addCodeGenerationCompletedTask(lifecycleListenerInfo,
                    new CodeGenerationCompletedTask(lifecycleTask, lifecycleListenerInfo));
        }
    }

    /**
     * This class holds a {@code CompilerLifecycleListener} instance with additional details such as the
     * containing compiler plugin's {@code CompilerPluginInfo} instance.
     *
     * @since 2.0.0
     */
    static class LifecycleListenerInfo {
        private final CompilerLifecycleListener lifecycleListener;
        private final CompilerPluginInfo compilerPluginInfo;

        public LifecycleListenerInfo(CompilerLifecycleListener lifecycleListener,
                                     CompilerPluginInfo compilerPluginInfo) {
            this.lifecycleListener = lifecycleListener;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        public CompilerLifecycleListener getLifecycleListener() {
            return lifecycleListener;
        }

        public CompilerPluginInfo getCompilerPluginInfo() {
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

            LifecycleListenerInfo that = (LifecycleListenerInfo) o;
            return Objects.equals(lifecycleListener, that.lifecycleListener)
                    && Objects.equals(compilerPluginInfo, that.compilerPluginInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lifecycleListener, compilerPluginInfo);
        }
    }

    static class LifecycleTasks {
        private final Map<LifecycleListenerInfo, List<CodeGenerationCompletedTask>> codeGenerationCompletedTasks =
                new HashMap<>();

        public void addCodeGenerationCompletedTask(LifecycleListenerInfo lifecycleListenerInfo,
                                                   CodeGenerationCompletedTask codeGenerationCompletedTask) {
            List<CodeGenerationCompletedTask> tasks =
                    codeGenerationCompletedTasks.computeIfAbsent(lifecycleListenerInfo, key -> new ArrayList<>());
            tasks.add(codeGenerationCompletedTask);
        }
    }

    static class CodeGenerationCompletedTask {

        private CompilerLifecycleTask<CompilerLifecycleEventContext> lifecycleTask;
        private LifecycleListenerInfo lifecycleListenerInfo;

        public CodeGenerationCompletedTask(CompilerLifecycleTask<CompilerLifecycleEventContext> lifecycleTask,
                                           LifecycleListenerInfo lifecycleListenerInfo) {

            this.lifecycleTask = lifecycleTask;
            this.lifecycleListenerInfo = lifecycleListenerInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o != null && getClass() == o.getClass()) {
                CodeGenerationCompletedTask that = (CodeGenerationCompletedTask) o;
                return Objects.equals(lifecycleTask, that.lifecycleTask)
                        && Objects.equals(lifecycleListenerInfo, that.lifecycleListenerInfo);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lifecycleTask, lifecycleListenerInfo);
        }
    }

    private static class CompilerLifecycleEventContextImpl implements CompilerLifecycleEventContext {
        private final Package currentPackage;
        private final PackageCompilation compilation;

        private Path binaryPath;
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        public CompilerLifecycleEventContextImpl(Package currentPackage, PackageCompilation compilation) {
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

        void setBinaryPath(Path binaryPath) {
            this.binaryPath = binaryPath;
        }

        @Override
        public Optional<Path> getGeneratedArtifactPath() {
            return Optional.ofNullable(binaryPath);
        }

        List<Diagnostic> reportedDiagnostics() {
            return diagnostics;
        }
    }
}
