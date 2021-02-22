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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;

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
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class PullModuleCodeAction extends AbstractCodeActionProvider {
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    @Override
    public boolean isEnabled(LanguageServerContext serverContext) {
        //TODO: disabled since offline is yet to be supported through project api
        return false;
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().startsWith(UNRESOLVED_MODULE))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.message();
        String uri = context.fileUri();
        CommandArgument uriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                diagnosticMessage.toLowerCase(Locale.ROOT)
        );
        if (matcher.find() && matcher.groupCount() > 0) {
            List<Object> args = new ArrayList<>();
            String pkgName = matcher.group(1).trim();
            String version = getVersion(context, pkgName, matcher);
            args.add(CommandArgument.from(CommandConstants.ARG_KEY_MODULE_NAME, pkgName + version));
            args.add(uriArg);
            String commandTitle = CommandConstants.PULL_MOD_TITLE;

            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, PullModuleExecutor.COMMAND, args));
            action.setDiagnostics(CodeActionUtil.toDiagnostics(diagnostics));
            return Collections.singletonList(action);
        }
        return Collections.emptyList();
    }

    private static String getVersion(CodeActionContext context, String pkgName, Matcher matcher) {
        // TODO: Fix when version info is available through project api
//        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
//        String version = matcher.groupCount() > 1 && matcher.group(2) != null ? ":" + matcher.group(2) : "";
//        int aliasIndex = version.indexOf(" as ");
//        if (aliasIndex > 0) {
//            version = version.substring(0, aliasIndex);
//        }
//        if (compilerContext != null && version.isEmpty()) {
//            // If no version in source, try reading Ballerina.toml dependencies
//            ManifestProcessor manifestProcessor = ManifestProcessor.getInstance(compilerContext);
//            Manifest manifest = manifestProcessor.getManifest();
//            List<Dependency> dependencies = manifest.getDependencies();
//            version = dependencies.stream()
//                    .filter(d -> d.getModuleID().equals(pkgName))
//                    .findAny().map(d -> ":" + d.getMetadata().getVersion()).orElse(version);
//        }
//        return version;

        return "";
    }
}
