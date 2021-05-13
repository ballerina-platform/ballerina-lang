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
import io.ballerina.projects.plugins.codeaction.CodeActionCommand;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages code actions.
 *
 * @since 2.0.0
 */
public class CodeActionManager {

    private final Map<CompilerPluginContextIml, Map<String, List<CodeAction>>> codeActionsMap;

    private CodeActionManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        this.codeActionsMap = new HashMap<>();
        compilerPluginContexts.forEach(compilerPluginContext -> {
            codeActionsMap.put(compilerPluginContext, new HashMap<>());
            // Find code actions of this context
            compilerPluginContext.codeActions().forEach(codeAction -> {
                // Get supported diagnostic codes of that code action and create a mapping
                codeAction.getSupportedDiagnosticCodes().forEach(diagnosticCode -> {
                    List<CodeAction> codeActions = codeActionsMap.get(compilerPluginContext)
                            .computeIfAbsent(diagnosticCode, k -> new ArrayList<>());
                    codeActions.add(codeAction);
                });
            });
        });
    }

    public List<CodeActionCommand> codeActions(ToolingCodeActionContext context, Diagnostic diagnostic) {
        List<CodeActionCommand> commands = new LinkedList<>();
        for (Map.Entry<CompilerPluginContextIml, Map<String, List<CodeAction>>> entry : codeActionsMap.entrySet()) {
            entry.getValue().getOrDefault(diagnostic.diagnosticInfo().code(), Collections.emptyList())
                    .forEach(codeAction ->
                            codeAction.getCodeAction(context, diagnostic).stream()
                                    .peek(codeActionDetails -> {
                                        // We change the provider name with package prefix
                                        codeActionDetails.setProviderName(getPackagePrefixedProviderName(
                                                entry.getKey().compilerPluginInfo(), codeAction.name()));
                                        codeActionDetails.setArguments(codeActionDetails.getArguments());
                                    })
                                    .forEach(commands::add));
        }

        return commands;
    }

    public List<DocumentEdit> executeCodeAction(String codeActionName, ToolingCodeActionContext context,
                                                List<CodeActionArgument> arguments) {
        for (CompilerPluginContextIml compilerPluginContext : codeActionsMap.keySet()) {
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
}
