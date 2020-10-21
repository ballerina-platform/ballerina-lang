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

import org.ballerinalang.langserver.command.executors.CreateTestExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.impl.NodeBasedCodeAction.isTopLevelNode;

/**
 * Code Action for generating test case for service.
 *
 * @since 1.2.0
 */
public class CreateServiceTestCodeAction implements NodeBasedCodeAction {
    @Override
    public List<CodeAction> get(CodeActionNodeType nodeType, List<Diagnostic> allDiagnostics, LSContext context) {
        try {
            String docUri = context.get(DocumentServiceKeys.FILE_URI_KEY);
            List<CodeAction> actions = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri));
            Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + position.getLine()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + position.getCharacter()));

            WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            if (!isTopLevelNode(docUri, documentManager, context, position)) {
                return actions;
            }

            CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_SERVICE_TITLE);
            action.setCommand(new Command(CommandConstants.CREATE_TEST_SERVICE_TITLE,
                                          CreateTestExecutor.COMMAND, args));
            actions.add(action);
            return actions;

        } catch (WorkspaceDocumentException | CompilationFailedException | TokenOrSymbolNotFoundException e) {
            // ignore
        }
        return new ArrayList<>();
    }
}
