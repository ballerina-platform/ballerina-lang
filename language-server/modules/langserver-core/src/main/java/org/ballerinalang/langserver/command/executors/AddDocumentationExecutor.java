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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
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
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNode;

/**
 * Command executor for adding single documentation.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class AddDocumentationExecutor implements LSCommandExecutor {

    public static final String COMMAND = "ADD_DOC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext ctx) throws LSCommandExecutorException {
        String documentUri = "";
        int line = 0;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        for (Object arg : ctx.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    ctx.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    line = Integer.parseInt(argVal);
                    break;
                default:
                    break;
            }
        }

        try {
            Optional<Path> filePath = CommonUtil.getPathFromURI(documentUri);
            if (!filePath.isPresent()) {
                return Collections.emptyList();
            }
            WorkspaceDocumentManager documentManager = ctx.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            SyntaxTree syntaxTree = documentManager.getTree(filePath.get());
            TextDocument textDocument = syntaxTree.textDocument();
            int txtPos = textDocument.textPositionFrom(LinePosition.from(line, 1));
            NonTerminalNode node = ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(txtPos, 0));
            Optional<DocAttachmentInfo> docAttachmentInfo = getDocumentationEditForNode(getDocumentableNode(node),
                                                                                        false);
            if (docAttachmentInfo.isPresent()) {
                DocAttachmentInfo docs = docAttachmentInfo.get();
                Range range = new Range(docs.getDocStartPos(), docs.getDocStartPos());
                LanguageClient languageClient = ctx.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
                return applySingleTextEdit(docs.getDocAttachment(), range, textDocumentIdentifier, languageClient);
            }
            return Collections.emptyList();
        } catch (WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Error when executing the 'add documentation' code action", e);
        }
    }

    private NonTerminalNode getDocumentableNode(NonTerminalNode node) {
        while (node.parent().kind() != SyntaxKind.MODULE_PART &&
                node.parent().kind() != SyntaxKind.SERVICE_BODY &&
                node.parent().kind() != SyntaxKind.OBJECT_TYPE_DESC &&
                node.parent().kind() != SyntaxKind.CLASS_DEFINITION) {
            node = node.parent();
        }
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
