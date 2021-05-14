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
import io.ballerina.projects.plugins.CompilerLifecycleListener;
import io.ballerina.projects.plugins.CompilerPluginContext;
import io.ballerina.projects.plugins.codeaction.CodeAction;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of the {@code CompilerPluginContext}.
 *
 * @since 2.0.0
 */
class CompilerPluginContextIml implements CompilerPluginContext {

    private final CompilerPluginInfo compilerPluginInfo;
    private final List<CodeAnalyzerManager.CodeAnalyzerInfo> codeAnalyzers = new ArrayList<>();
    private final List<CompilerLifecycleManager.LifecycleListenerInfo> lifecycleListeners = new ArrayList<>();
    private final List<CodeAction> codeActions = new ArrayList<>();

    CompilerPluginContextIml(CompilerPluginInfo compilerPluginInfo) {
        this.compilerPluginInfo = compilerPluginInfo;
    }

    public void addCodeAnalyzer(CodeAnalyzer codeAnalyzer) {
        codeAnalyzers.add(new CodeAnalyzerManager.CodeAnalyzerInfo(codeAnalyzer, compilerPluginInfo));
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

    List<CodeAnalyzerManager.CodeAnalyzerInfo> codeAnalyzers() {
        return codeAnalyzers;
    }

    public List<CompilerLifecycleManager.LifecycleListenerInfo> getLifecycleListeners() {
        return lifecycleListeners;
    }

    public List<CodeAction> codeActions() {
        return codeActions;
    }

    public CompilerPluginInfo compilerPluginInfo() {
        return compilerPluginInfo;
    }
}
