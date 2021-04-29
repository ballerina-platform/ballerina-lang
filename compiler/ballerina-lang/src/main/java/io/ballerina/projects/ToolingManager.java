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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutor;
import io.ballerina.projects.plugins.codeaction.ToolingCodeActionContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Tooling manager.
 */
public class ToolingManager {

    private List<CompilerPluginContextIml> compilerPluginContexts;

    private ToolingManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        this.compilerPluginContexts = compilerPluginContexts;
    }

    public List<CodeAction> nodeBasedCodeActions(ToolingCodeActionContext context, Node node) {
        List<CodeAction> codeActions = new LinkedList<>();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            compilerPluginContext.codeActionProviders().stream()
                    .map(provider -> provider.getNodeBasedCodeActions(context, node))
                    .flatMap(Collection::stream)
                    .peek(codeAction -> {
                        codeAction.setCommand(getPackagePrefixedCommand(compilerPluginContext.compilerPluginInfo(),
                                codeAction.getCommand()));
                    })
                    .forEach(codeActions::add);
        }

        return codeActions;
    }

    public List<CodeAction> diagnosticBasedCodeActions(ToolingCodeActionContext context, Diagnostic diagnostic) {
        List<CodeAction> codeActions = new LinkedList<>();
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            compilerPluginContext.codeActionProviders().stream()
                    .map(provider -> provider.getDiagnosticBasedCodeAction(context, diagnostic))
                    .flatMap(Collection::stream)
                    .peek(codeAction -> {
                        codeAction.setCommand(getPackagePrefixedCommand(compilerPluginContext.compilerPluginInfo(),
                                codeAction.getCommand()));
                    })
                    .forEach(codeActions::add);
        }

        return codeActions;
    }

    public Optional<CodeActionExecutor> getCodeActionExecutor(String command) {
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            String prefix = getPackagePrefix(compilerPluginContext.compilerPluginInfo());
            if (!command.startsWith(prefix)) {
                continue;
            }

            if (command.length() == prefix.length()) {
                continue;
            }

            // Get command name. Substring excluding the "_"
            command = command.substring(prefix.length() + 1);
            if (command.isEmpty()) {
                continue;
            }
            
            String finalCommand = command;
            Optional<CodeActionExecutor> codeActionExecutor = compilerPluginContext.codeActionExecutors().stream()
                    .filter(executor -> finalCommand.equals(executor.getCommand()))
                    .findAny();

            if (codeActionExecutor.isPresent()) {
                return codeActionExecutor;
            }
        }

        return Optional.empty();
    }

    static ToolingManager from(List<CompilerPluginContextIml> compilerPluginContexts) {
        return new ToolingManager(compilerPluginContexts);
    }

    private static String getPackagePrefix(CompilerPluginInfo compilerPluginInfo) {
        PackageDescriptor packageDescriptor = compilerPluginInfo.packageDesc();
        return packageDescriptor.org().value().replaceAll("\\.", "_") + "_" +
                packageDescriptor.name().value().replaceAll("\\.", "_");
    }

    private static String getPackagePrefixedCommand(CompilerPluginInfo compilerPluginInfo, String command) {
        return getPackagePrefix(compilerPluginInfo) + "_" + command;
    }
}
