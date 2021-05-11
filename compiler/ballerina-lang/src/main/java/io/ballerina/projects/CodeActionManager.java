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
import io.ballerina.projects.plugins.codeaction.CodeActionProvider;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Manages code actions.
 *
 * @since 2.0.0
 */
public class CodeActionManager {

    private List<CompilerPluginContextIml> compilerPluginContexts;

    private CodeActionManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        this.compilerPluginContexts = compilerPluginContexts;
    }

    public List<CodeAction> codeActions(ToolingCodeActionContext context, Diagnostic diagnostic) {
        List<CodeAction> codeActions = new LinkedList<>();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            compilerPluginContext.codeActionProviders()
                    .forEach(provider -> provider.getCodeActions(context, diagnostic).stream()
                            .peek(codeAction -> {
                                // We change the provider name with package prefix
                                codeAction.setProviderName(getPackagePrefixedProviderName(
                                        compilerPluginContext.compilerPluginInfo(), provider.name()));
                                // Then, add code action ID as an argument
                                List<CodeActionArgument> arguments = new ArrayList<>(codeAction.getArguments());
                                codeAction.setArguments(arguments);
                            })
                            .forEach(codeActions::add));
        }

        return codeActions;
    }

    public List<DocumentEdit> executeCodeAction(String providerName, ToolingCodeActionContext context,
                                                List<CodeActionArgument> arguments) {
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            String prefix = getPackagePrefix(compilerPluginContext.compilerPluginInfo());
            if (!providerName.startsWith(prefix)) {
                continue;
            }

            if (providerName.length() == prefix.length()) {
                continue;
            }

            // Get command name. Substring excluding the "_"
            providerName = providerName.substring(prefix.length() + 1);
            if (providerName.isEmpty()) {
                continue;
            }

            String finalProviderName = providerName;
            Optional<CodeActionProvider> codeActionProvider = compilerPluginContext.codeActionProviders().stream()
                    .filter(provider -> finalProviderName.equals(provider.name()))
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
