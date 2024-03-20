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

import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CodeGenerator;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CompilerLifecycleListener;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.completion.CompletionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the {@code CompilerPluginContext}.
 *
 * @since 2.0.0
 */
class CompilerPluginContextIml implements CompilerPluginContext {

    private final CompilerPluginInfo compilerPluginInfo;
    private final List<CodeAnalyzerManager.CodeAnalyzerInfo> codeAnalyzers = new ArrayList<>();
    private final List<CodeGeneratorManager.CodeGeneratorInfo> codeGenerators = new ArrayList<>();
    private final List<CodeModifierManager.CodeModifierInfo> codeModifiers = new ArrayList<>();
    private final List<CompilerLifecycleManager.LifecycleListenerInfo> lifecycleListeners = new ArrayList<>();
    private final List<CodeAction> codeActions = new ArrayList<>();
    private Map<String, Object> compilerPluginUserData = new HashMap();
    private final List<CompletionProvider> completionProviders = new ArrayList<>();

    CompilerPluginContextIml(CompilerPluginInfo compilerPluginInfo) {
        this.compilerPluginInfo = compilerPluginInfo;
    }

    CompilerPluginContextIml(CompilerPluginInfo compilerPluginInfo, Map<String, Object> userData) {
        this.compilerPluginInfo = compilerPluginInfo;
        this.compilerPluginUserData = userData;
    }

    public void addCodeAnalyzer(CodeAnalyzer codeAnalyzer) {
        codeAnalyzers.add(new CodeAnalyzerManager.CodeAnalyzerInfo(codeAnalyzer, compilerPluginInfo));
    }

    public void addCodeGenerator(CodeGenerator codeGenerator) {
        codeGenerators.add(new CodeGeneratorManager.CodeGeneratorInfo(codeGenerator, compilerPluginInfo));
    }

    public void addCodeModifier(CodeModifier codeModifier) {
        codeModifiers.add(new CodeModifierManager.CodeModifierInfo(codeModifier, compilerPluginInfo));
    }

    @Override
    public void addCompilerLifecycleListener(CompilerLifecycleListener lifecycleListener) {
        lifecycleListeners.add(
                new CompilerLifecycleManager.LifecycleListenerInfo(lifecycleListener, compilerPluginInfo));
    }

    @Override
    public void addCodeAction(CodeAction codeAction) {
        codeActions.add(codeAction);
    }

    @Override
    public void addCompletionProvider(CompletionProvider completionProvider) {
        completionProviders.add(completionProvider);
    }

    List<CodeAnalyzerManager.CodeAnalyzerInfo> codeAnalyzers() {
        return codeAnalyzers;
    }

    List<CodeGeneratorManager.CodeGeneratorInfo> codeGenerators() {
        return codeGenerators;
    }

    List<CodeModifierManager.CodeModifierInfo> codeModifiers() {
        return codeModifiers;
    }

    public List<CompilerLifecycleManager.LifecycleListenerInfo> getLifecycleListeners() {
        return lifecycleListeners;
    }

    public List<CodeAction> codeActions() {
        return codeActions;
    }

    public List<CompletionProvider> completionProviders() {
        return completionProviders;
    }

    public CompilerPluginInfo compilerPluginInfo() {
        return compilerPluginInfo;
    }

    public Map<String, Object> userData() {
        return this.compilerPluginUserData;
    }
}
