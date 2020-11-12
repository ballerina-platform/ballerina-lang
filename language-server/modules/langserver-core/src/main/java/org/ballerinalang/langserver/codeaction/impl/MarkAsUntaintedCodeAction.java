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

import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Code Action for marking as untainted.
 *
 * @since 2.0.0
 */
public class MarkAsUntaintedCodeAction implements DiagBasedCodeAction {
    @Override
    public List<CodeAction> get(Diagnostic diagnostic, CodeActionContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        String uri = context.fileUri();
//        try {
//            Matcher matcher = CommandConstants.TAINTED_PARAM_PATTERN.matcher(diagnosticMessage);
//            if (matcher.find() && matcher.groupCount() > 0) {
//                String param = matcher.group(1);
//                String commandTitle = String.format(CommandConstants.MARK_UNTAINTED_TITLE, param);
//                // Extract specific content range
//                Range range = diagnostic.getRange();
//                WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
//                String content = CommandUtil.getContentOfRange(documentManager, uri, range);
//                // Add `untaint` keyword
//                matcher = CommandConstants.NO_CONCAT_PATTERN.matcher(content);
//                String editText = matcher.find() ? "<@untainted>  " + content : "<@untainted> (" + content + ")";
//                // Create text-edit
//                List<TextEdit> edits = new ArrayList<>();
//                edits.add(new TextEdit(range, editText));
//                return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
//            }
//        } catch (WorkspaceDocumentException | IOException e) {
//            //do nothing
//        }
        return new ArrayList<>();
    }
}
