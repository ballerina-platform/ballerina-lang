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
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNode;

/**
 * Command executor for adding all documentation for top level items.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class AddAllDocumentationExecutor implements LSCommandExecutor {

    public static final String COMMAND = "ADD_ALL_DOC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((JsonObject) arg).get(ARG_KEY).getAsString().equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = ((JsonObject) arg).get(ARG_VALUE).getAsString();
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            }
        }

        BLangPackage bLangPackage;
        try {
            WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager, LSCustomErrorStrategy.class, false,
                    false);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Couldn't compile the source", e);
        }

        String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);

        List<TextEdit> textEdits = new ArrayList<>();
        for (TopLevelNode topLevelNode : CommonUtil.getCurrentFileTopLevelNodes(srcOwnerPkg, context)) {
            if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).flagSet.contains(Flag.SERVICE)) {
                continue;
            }
            DocAttachmentInfo docAttachmentInfo = getDocumentationEditForNode(topLevelNode);
            if (docAttachmentInfo != null) {
                textEdits.add(getTextEdit(docAttachmentInfo));
            }
            if (topLevelNode instanceof BLangService) {
                BLangService service = (BLangService) topLevelNode;
                ((BLangObjectTypeNode) service.serviceTypeDefinition.getTypeNode()).getFunctions()
                        .forEach(bLangResource -> {
                            DocAttachmentInfo resourceInfo = getDocumentationEditForNode(bLangResource);
                            if (resourceInfo != null) {
                                textEdits.add(getTextEdit(resourceInfo));
                            }
                        });
            }
            if (topLevelNode instanceof BLangTypeDefinition
                    && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                ((BLangObjectTypeNode) ((BLangTypeDefinition) topLevelNode).typeNode).functions.forEach(function -> {
                    DocAttachmentInfo resourceInfo = getDocumentationEditForNode(function);
                    if (resourceInfo != null) {
                        textEdits.add(getTextEdit(resourceInfo));
                    }
                });
            }
        }

        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, textEdits);
        LanguageClient languageClient = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
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
        return new TextEdit(range, attachmentInfo.getDocAttachment());
    }
}
