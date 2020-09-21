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
package org.ballerinalang.langserver.codeaction.builder.impl;

import org.ballerinalang.langserver.codeaction.builder.DiagBasedCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for ignore variable assignment.
 *
 * @since 2.0.0
 */
public class IgnoreReturnCodeAction implements DiagBasedCodeAction {
    private SymbolReferencesModel.Reference refAtCursor;

    public IgnoreReturnCodeAction(SymbolReferencesModel.Reference refAtCursor) {
        this.refAtCursor = refAtCursor;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position position = diagnostic.getRange().getStart();

        BSymbol symbolAtCursor = refAtCursor.getSymbol();

        boolean isInvocation = symbolAtCursor instanceof BInvokableSymbol;

        boolean hasDefaultInitFunction = false;
        boolean hasCustomInitFunction = false;
        if (refAtCursor.getbLangNode() instanceof BLangInvocation) {
            hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
            hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
                    symbolAtCursor.name.value.endsWith("init");
        }
        boolean isInitInvocation = hasDefaultInitFunction || hasCustomInitFunction;
        String commandTitle;

        if (isInvocation || isInitInvocation) {
            BType returnType;
            if (hasDefaultInitFunction) {
                returnType = symbolAtCursor.type;
            } else if (hasCustomInitFunction) {
                returnType = symbolAtCursor.owner.type;
            } else {
                returnType = ((BInvokableSymbol) symbolAtCursor).retType;
            }
            boolean hasError = false;
            if (returnType instanceof BErrorType) {
                hasError = true;
            } else if (returnType instanceof BUnionType) {
                BUnionType unionType = (BUnionType) returnType;
                hasError = unionType.getMemberTypes().stream().anyMatch(s -> s instanceof BErrorType);
            }
            // Add ignore return value code action
            if (!hasError) {
                commandTitle = CommandConstants.IGNORE_RETURN_TITLE;
                return Collections.singletonList(
                        createQuickFixCodeAction(commandTitle, getIgnoreCodeActionEdits(position), uri));
            }
        }
        return new ArrayList<>();
    }

    private static List<TextEdit> getIgnoreCodeActionEdits(Position position) {
        String editText = "_ = ";
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }
}
