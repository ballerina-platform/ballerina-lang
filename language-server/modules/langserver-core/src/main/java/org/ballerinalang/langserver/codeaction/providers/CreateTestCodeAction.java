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
import org.ballerinalang.langserver.command.executors.CreateTestExecutor;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.elements.Flag;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code Action provider for create variable command.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateTestCodeAction extends AbstractCodeActionProvider {
    public CreateTestCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION, CodeActionNodeType.OBJECT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext context,
                                           List<Diagnostic> diagnostics) {
        try {
            String docUri = context.get(DocumentServiceKeys.FILE_URI_KEY);
            List<CodeAction> actions = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri));
            Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + position.getLine()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + position.getCharacter()));

            boolean isService = CommonKeys.SERVICE_KEYWORD_KEY.equals(nodeType.name());
            boolean isFunction = CommonKeys.FUNCTION_KEYWORD_KEY.equals(nodeType.name());
            WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            if ((isService || isFunction) && !isTopLevelNode(docUri, documentManager, context, position)) {
                return actions;
            }

            if (isService) {
                CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_SERVICE_TITLE);
                action.setCommand(new Command(CommandConstants.CREATE_TEST_SERVICE_TITLE,
                                              CreateTestExecutor.COMMAND, args));
                actions.add(action);
            } else if (isFunction) {
                CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_FUNC_TITLE);
                action.setCommand(new Command(CommandConstants.CREATE_TEST_FUNC_TITLE,
                                              CreateTestExecutor.COMMAND, args));
                actions.add(action);
            }
            return actions;

        } catch (CompilationFailedException | WorkspaceDocumentException e) {
            // ignore
        }
        return null;
    }

    private static boolean isTopLevelNode(String uri, WorkspaceDocumentManager docManager, LSContext context,
                                          Position pos)
            throws CompilationFailedException, WorkspaceDocumentException {
        LSDocumentIdentifier lsDocument = docManager.getLSDocument(CommonUtil.getPathFromURI(uri).get());
        context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
        SymbolReferencesModel.Reference refAtCursor = ReferencesUtil.getReferenceAtCursor(context, lsDocument, pos);
        BLangNode bLangNode = refAtCursor.getbLangNode();

        // Only supported for 'public' functions
        if (bLangNode instanceof BLangFunction &&
                !((BLangFunction) bLangNode).getFlags().contains(Flag.PUBLIC)) {
            return false;
        }

        // Only supported for top-level nodes
        return (bLangNode != null && bLangNode.parent instanceof BLangPackage);
    }
}
