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
package io.ballerina.projects.plugins;

import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.completion.CompletionProvider;

import java.util.Map;

/**
 * This class can be used to add various compiler plugin tasks to the current compilation.
 *
 * @since 2.0.0
 */
public interface CompilerPluginContext {

    /**
     * Add a {@code CodeAnalyzer} instance to the current compilation.
     *
     * @param codeAnalyzer the {@code CodeAnalyzer} instance
     */
    void addCodeAnalyzer(CodeAnalyzer codeAnalyzer);

    /**
     * Add a {@code CodeGenerator} instance to the current compilation.
     *
     * @param codeGenerator the {@code CodeGenerator} instance
     */
    void addCodeGenerator(CodeGenerator codeGenerator);

    /**
     * Add a {@code CodeModifier} instance to the current compilation.
     *
     * @param codeModifier the {@code CodeModifier} instance
     */
    void addCodeModifier(CodeModifier codeModifier);

    /**
     * Add a {@code CompilerLifecycleListener} instance to the current compilation.
     *
     * @param lifecycleListener the {@code CompilerLifecycleListener} instance
     */
    void addCompilerLifecycleListener(CompilerLifecycleListener lifecycleListener);

    /**
     * Add a {@link CodeAction} to the current compilation.
     *
     * @param codeAction the {@link CodeAction} instance
     */
    void addCodeAction(CodeAction codeAction);
    
    /**
     * Add a {@link CompletionProvider} to the current compilation.
     *
     * @param completionProvider the {@link CompletionProvider} instance
     */
    void addCompletionProvider(CompletionProvider completionProvider);

    /**
     * Returns user data for the compiler plugin.
     *
     * @return Map of user data as Map<String, Object>
     */
    Map<String, Object> userData();
}
