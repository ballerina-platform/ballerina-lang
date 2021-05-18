/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.CodeActionPluginContext;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages interaction with code actions via compiler plugins.
 *
 * @since 2.0.0
 */
public class CodeActionManager {

    private final List<CompilerPluginContextIml> compilerPluginContexts;
    private final Map<String, List<CodeActionDescriptor>> codeActionsMap;

    private CodeActionManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        this.compilerPluginContexts = compilerPluginContexts;
        this.codeActionsMap = new HashMap<>();
        compilerPluginContexts.forEach(compilerPluginContext -> {
            // Find code actions of this context
            compilerPluginContext.codeActions().forEach(codeAction -> {
                // Get supported diagnostic codes of that code action and create a mapping
                codeAction.supportedDiagnosticCodes().forEach(diagnosticCode -> {
                    List<CodeActionDescriptor> codeActions =
                            codeActionsMap.computeIfAbsent(diagnosticCode, k -> new ArrayList<>());
                    codeActions.add(new CodeActionDescriptor(codeAction, compilerPluginContext.compilerPluginInfo()));
                });
            });
        });
    }

    /**
     * Get all available code actions for the provided diagnostic from all available compiler plugins.
     *
     * @param context    Code action context
     * @param diagnostic Diagnostic for which code actions are to be shown
     * @return List of available code actions
     */
    public List<CodeActionInfo> codeActions(CodeActionPluginContext context, Diagnostic diagnostic) {
        List<CodeActionInfo> commands = new LinkedList<>();
        codeActionsMap.getOrDefault(diagnostic.diagnosticInfo().code(), Collections.emptyList())
                .forEach(codeActionDescriptor ->
                        codeActionDescriptor.codeAction().codeActionInfo(context, diagnostic).stream()
                                .peek(codeActionInfo -> {
                                    // We change the provider name with package prefix
                                    codeActionInfo.setProviderName(getPackagePrefixedProviderName(
                                            codeActionDescriptor.compilerPluginInfo(),
                                            codeActionDescriptor.codeAction().name()));
                                    codeActionInfo.setArguments(codeActionInfo.getArguments());
                                })
                                .forEach(commands::add));

        return commands;
    }

    /**
     * Executes the code action provided by name.
     *
     * @param codeActionName Name of the code action. Will be prefixed with the package name
     * @param context        Code action context
     * @param arguments      Arguments for the code action to execute
     * @return List of edits to be applied
     */
    public List<DocumentEdit> executeCodeAction(String codeActionName, CodeActionPluginContext context,
                                                List<CodeActionArgument> arguments) {
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            String prefix = getPackagePrefix(compilerPluginContext.compilerPluginInfo());
            if (!codeActionName.startsWith(prefix)) {
                continue;
            }

            if (codeActionName.length() == prefix.length()) {
                continue;
            }

            // Get command name. Substring excluding the "_"
            codeActionName = codeActionName.substring(prefix.length() + 1);
            if (codeActionName.isEmpty()) {
                continue;
            }

            String finalCodeActionName = codeActionName;
            Optional<CodeAction> codeActionProvider = compilerPluginContext.codeActions().stream()
                    .filter(provider -> finalCodeActionName.equals(provider.name()))
                    .findFirst();

            if (codeActionProvider.isPresent()) {
                return codeActionProvider.get().execute(context, arguments);
            }
        }

        return Collections.emptyList();
    }

    static CodeActionManager from(List<CompilerPluginContextIml> compilerPluginContexts) {
        return new CodeActionManager(compilerPluginContexts);
    }

    private static String getPackagePrefix(CompilerPluginInfo compilerPluginInfo) {
        PackageDescriptor packageDescriptor = compilerPluginInfo.packageDesc();
        return packageDescriptor.org().value().replaceAll("\\.", "_") + "_" +
                packageDescriptor.name().value().replaceAll("\\.", "_");
    }

    private static String getPackagePrefixedProviderName(CompilerPluginInfo compilerPluginInfo, String providerName) {
        return getPackagePrefix(compilerPluginInfo) + "_" + providerName;
    }

    /**
     * A wrapper class to keep track of information of a code action.
     */
    static class CodeActionDescriptor {

        private final CodeAction codeAction;
        private final CompilerPluginInfo compilerPluginInfo;

        public CodeActionDescriptor(CodeAction codeAction, CompilerPluginInfo compilerPluginInfo) {
            this.codeAction = codeAction;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        public CodeAction codeAction() {
            return codeAction;
        }

        public CompilerPluginInfo compilerPluginInfo() {
            return compilerPluginInfo;
        }
    }
}
