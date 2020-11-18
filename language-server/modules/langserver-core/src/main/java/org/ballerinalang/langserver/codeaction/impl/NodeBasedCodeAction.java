/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.ballerinalang.model.elements.Flag;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;

/**
 * Interface for node type based code actions.
 *
 * @since 2.0.0
 */
public interface NodeBasedCodeAction {

    List<CodeAction> get(CodeActionNodeType nodeType, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException;

    static boolean isTopLevelNode(String uri, WorkspaceDocumentManager docManager, LSContext context,
                                  Position pos)
            throws CompilationFailedException, WorkspaceDocumentException, TokenOrSymbolNotFoundException {
        LSDocumentIdentifier lsDocument = docManager.getLSDocument(CommonUtil.getPathFromURI(uri).get());
        context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
        Reference refAtCursor = ReferencesUtil.getReferenceAtCursor(context, lsDocument, pos);
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
