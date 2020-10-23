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

import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for marking as untainted.
 *
 * @since 2.0.0
 */
public class MarkAsUntaintedCodeAction implements DiagBasedCodeAction {
    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException {
        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        try {
            Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
            if (matcher.find() && matcher.groupCount() > 0) {
                String param = matcher.group(1);
                String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
                // Extract specific content range
                Range range = diagnostic.getRange();
                WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
                String content = CommandUtil.getContentOfRange(documentManager, uri, range);
                // Add `untaint` keyword
                matcher = CommandConstants.NO_CONCAT_PATTERN.matcher(content);
                String editText = matcher.find() ? "<@untainted>  " + content : "<@untainted> (" + content + ")";
                // Create text-edit
                List<TextEdit> edits = new ArrayList<>();
                edits.add(new TextEdit(range, editText));
                return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
            }
        } catch (WorkspaceDocumentException | IOException e) {
            //do nothing
        }
        return new ArrayList<>();
    }
}
