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
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNodeByPosition;

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
        String nodeType = "";
        String documentUri;
        int line = 0;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        for (Object arg : ctx.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            switch (((JsonObject) arg).get(ARG_KEY).getAsString()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = ((JsonObject) arg).get(ARG_VALUE).getAsString();
                    textDocumentIdentifier.setUri(documentUri);
                    ctx.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_TYPE:
                    nodeType = ((JsonObject) arg).get(ARG_VALUE).getAsString();
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    line = Integer.parseInt(((JsonObject) arg).get(ARG_VALUE).getAsString());
                    break;
                default:
                    break;
            }
        }

        BLangPackage bLangPackage;
        try {
            WorkspaceDocumentManager documentManager = ctx.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            bLangPackage = LSModuleCompiler.getBLangPackage(ctx, documentManager, LSCustomErrorStrategy.class, false,
                    false);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Couldn't compile the source", e);
        }

        String relativeSourcePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);

        DocAttachmentInfo docAttachmentInfo = getDocumentationEditForNodeByPosition(nodeType, srcOwnerPkg, line, ctx);

        if (docAttachmentInfo == null) {
            return new Object();
        }

        Range range = new Range(docAttachmentInfo.getDocStartPos(), docAttachmentInfo.getDocStartPos());

        return applySingleTextEdit(docAttachmentInfo.getDocAttachment(), range, textDocumentIdentifier,
                                   ctx.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
