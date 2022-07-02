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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
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
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.hasDocs;

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
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) {
        String documentUri = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (CommandArgument arg : context.getArguments()) {
            if (CommandConstants.ARG_KEY_DOC_URI.equals(arg.key())) {
                documentUri = arg.valueAs(String.class);
                textDocumentIdentifier.setUri(documentUri);
            }
        }

        Optional<Path> filePath = PathUtil.getPathFromURI(documentUri);
        if (filePath.isEmpty()) {
            return Collections.emptyList();
        }
        SyntaxTree syntaxTree = context.workspace().syntaxTree(filePath.get()).orElseThrow();

        List<TextEdit> textEdits = new ArrayList<>();
        ((ModulePartNode) syntaxTree.rootNode()).members()
                .stream().filter(node -> !hasDocs(node))
                .forEach(member ->
                                 getDocumentationEditForNode(member, syntaxTree)
                                         .ifPresent(docs -> textEdits.add(getTextEdit(docs))));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, textEdits);
        LanguageClient languageClient = context.getLanguageClient();

        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), languageClient);
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
        return new TextEdit(range, attachmentInfo.getDocumentationString());
    }
}
