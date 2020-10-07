/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.impl;

import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Code Action for pulling a package from central.
 *
 * @since 1.1.1
 */
public class PullModuleCodeAction implements DiagBasedCodeAction {
    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException {
        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                diagnosticMessage.toLowerCase(Locale.ROOT)
        );
        if (matcher.find() && matcher.groupCount() > 0) {
            List<Object> args = new ArrayList<>();
            String pkgName = matcher.group(1).trim();
            String version = getVersion(context, pkgName, matcher);
            args.add(new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME, pkgName + version));
            args.add(uriArg);
            String commandTitle = CommandConstants.PULL_MOD_TITLE;

            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, PullModuleExecutor.COMMAND, args));
            action.setDiagnostics(diagnostics);
            return Collections.singletonList(action);
        }
        return new ArrayList<>();
    }

    private static String getVersion(LSContext context, String pkgName, Matcher matcher) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        String version = matcher.groupCount() > 1 && matcher.group(2) != null ? ":" + matcher.group(2) : "";
        int aliasIndex = version.indexOf(" as ");
        if (aliasIndex > 0) {
            version = version.substring(0, aliasIndex);
        }
        if (compilerContext != null && version.isEmpty()) {
            // If no version in source, try reading Ballerina.toml dependencies
            ManifestProcessor manifestProcessor = ManifestProcessor.getInstance(compilerContext);
            Manifest manifest = manifestProcessor.getManifest();
            List<Dependency> dependencies = manifest.getDependencies();
            version = dependencies.stream()
                    .filter(d -> d.getModuleID().equals(pkgName))
                    .findAny().map(d -> ":" + d.getMetadata().getVersion()).orElse(version);
        }
        return version;
    }
}
