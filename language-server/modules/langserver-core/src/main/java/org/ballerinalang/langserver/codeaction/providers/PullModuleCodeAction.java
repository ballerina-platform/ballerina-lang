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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Code Action provider for pulling a package from central.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class PullModuleCodeAction extends AbstractCodeActionProvider {
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {

        List<CodeAction> actions = new ArrayList<>();

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().startsWith(UNRESOLVED_MODULE)) {
                actions.add(getUnresolvedPackageCommand(diagnostic, lsContext));
            }
        }
        return actions;
    }

    private static CodeAction getUnresolvedPackageCommand(Diagnostic diagnostic, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.get(CodeActionKeys.FILE_URI_KEY);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                diagnosticMessage.toLowerCase(Locale.ROOT)
        );
        if (matcher.find() && matcher.groupCount() > 0) {
            List<Object> args = new ArrayList<>();
            String pkgName = matcher.group(1).trim();
            String version = matcher.groupCount() > 1 && matcher.group(2) != null ? ":" + matcher.group(2) : "";
            args.add(new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME, pkgName + version));
            args.add(uriArg);
            String commandTitle = CommandConstants.PULL_MOD_TITLE;

            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, PullModuleExecutor.COMMAND, args));
            action.setDiagnostics(diagnostics);
            return action;
        }
        return null;
    }
}
