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
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Code Action provider for tainted parameters.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TaintedParamCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.TAINTED_PARAM_PASSED)) {
                CodeAction codeAction = getTaintedParamCommand(diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    private static CodeAction getTaintedParamCommand(Diagnostic diagnostic, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        List<Diagnostic> diagnostics = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);

        try {
            Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 0) {
                String param = matcher.group(1);
                String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
                CodeAction action = new CodeAction(commandTitle);
                action.setKind(CodeActionKind.QuickFix);
                action.setDiagnostics(diagnostics);
                // Extract specific content range
                Range range = diagnostic.getRange();
                WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
                String content = CommandUtil.getContentOfRange(documentManager, uri, range);
                // Add `untaint` keyword
                matcher = CommandConstants.NO_CONCAT_PATTERN.matcher(content);
                String editText = matcher.find() ? "<@untained>  " + content : "<@untained> (" + content + ")";
                // Create text-edit
                List<TextEdit> edits = new ArrayList<>();
                edits.add(new TextEdit(range, editText));
                VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(uri, null);
                action.setEdit(new WorkspaceEdit(Collections.singletonList(
                        Either.forLeft(new TextDocumentEdit(identifier, edits)))));
                return action;
            }
        } catch (WorkspaceDocumentException | IOException e) {
            //do nothing
        }
        return null;
    }
}
