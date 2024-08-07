/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.List;

/**
 * Utilities for the command related operations.
 */
public final class CommandUtil {

    private CommandUtil() {
    }

    /**
     * Sends a message to the language server client.
     *
     * @param client      Language Server client
     * @param messageType message type
     * @param message     message
     */
    public static void notifyClient(LanguageClient client, MessageType messageType, String message) {
        client.showMessage(new MessageParams(messageType, message));
    }

    /**
     * Apply a given single text edit.
     *
     * @param editText   Edit text to be inserted
     * @param range      Line Range to be processed
     * @param identifier Document identifier
     * @param client     Language Client
     * @return {@link ApplyWorkspaceEditParams}     Workspace edit params
     */
    public static ApplyWorkspaceEditParams applySingleTextEdit(String editText, Range range,
                                                               VersionedTextDocumentIdentifier identifier,
                                                               LanguageClient client) {

        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
        TextEdit textEdit = new TextEdit(range, editText);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(identifier,
                Collections.singletonList(textEdit));
        Either<TextDocumentEdit, ResourceOperation> documentChange = Either.forLeft(textDocumentEdit);
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(documentChange));
        applyWorkspaceEditParams.setEdit(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    /**
     * Apply a workspace edit for the current instance.
     *
     * @param documentChanges List of either document edits or set of resource changes for current session
     * @param client          Language Client
     * @return {@link Object}   workspace edit parameters
     */
    public static Object applyWorkspaceEdit(List<Either<TextDocumentEdit, ResourceOperation>> documentChanges,
                                            LanguageClient client) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(documentChanges);
        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }
}
