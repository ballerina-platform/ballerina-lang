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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNode;

/**
 * Command executor for adding all documentation for top level items.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class AddAllDocumentationExecutor implements LSCommandExecutor {

    public static final String COMMAND = "ADD_ALL_DOC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((JsonObject) arg).get(ARG_KEY).getAsString().equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = ((JsonObject) arg).get(ARG_VALUE).getAsString();
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            }
        }

        try {
            Optional<Path> filePath = CommonUtil.getPathFromURI(documentUri);
            if (!filePath.isPresent()) {
                return new ArrayList<>();
            }
            WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            SyntaxTree syntaxTree = documentManager.getTree(filePath.get());

            List<TextEdit> textEdits = new ArrayList<>();
            ((ModulePartNode) syntaxTree.rootNode()).members().forEach(member -> {
                getDocumentationEditForNode(member, true).ifPresent(docs -> textEdits.add(getTextEdit(docs)));
            });
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, textEdits);
            LanguageClient languageClient = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
            return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), languageClient);
        } catch (WorkspaceDocumentException e) {
            throw  new LSCommandExecutorException("Error when executing the 'add all documentation' code action", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * Get TextEdit from doc attachment info.
     *
     * @param attachmentInfo Doc attachment info
     * @return {@link TextEdit}     Text edit for attachment info
     */
    private static TextEdit getTextEdit(DocAttachmentInfo attachmentInfo) {
        Range range = new Range(attachmentInfo.getDocStartPos(), attachmentInfo.getDocStartPos());
        return new TextEdit(range, attachmentInfo.getDocAttachment());
    }
}
