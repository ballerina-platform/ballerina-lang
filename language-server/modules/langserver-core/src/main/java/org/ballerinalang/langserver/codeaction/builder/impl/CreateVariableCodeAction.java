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

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.codeaction.builder.DiagBasedCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.builder.DiagBasedCodeAction.getPossibleTypesAndNames;
import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
public class CreateVariableCodeAction implements DiagBasedCodeAction {
    private SymbolReferencesModel.Reference refAtCursor;

    public CreateVariableCodeAction(SymbolReferencesModel.Reference refAtCursor) {
        this.refAtCursor = refAtCursor;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position position = diagnostic.getRange().getStart();
        BSymbol symbolAtCursor = refAtCursor.getSymbol();
        boolean hasDefaultInitFunction = false;
        boolean hasCustomInitFunction = false;
        boolean isAsync = false;
        if (refAtCursor.getbLangNode() instanceof BLangInvocation) {
            hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
            hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
                    symbolAtCursor.name.value.endsWith("init");
            isAsync = ((BLangInvocation) refAtCursor.getbLangNode()).isAsync();
        }

        // Find enclosing function node
        BLangNode bLangNode = refAtCursor.getbLangNode();
        BLangFunction enclosedFunc = null;
        while (!(bLangNode instanceof BLangPackage)) {
            if (bLangNode instanceof BLangFunction) {
                enclosedFunc = (BLangFunction) bLangNode;
                break;
            }
            bLangNode = bLangNode.parent;
        }

        return getCreateVariableCodeActions(context, uri, enclosedFunc, position, refAtCursor,
                                            hasDefaultInitFunction, hasCustomInitFunction, isAsync);
    }

    private static List<CodeAction> getCreateVariableCodeActions(LSContext context, String uri,
                                                                 BLangFunction enclosedFunc, Position position,
                                                                 Reference referenceAtCursor,
                                                                 boolean hasDefaultInitFunction,
                                                                 boolean hasCustomInitFunction, boolean isAsync) {
        List<CodeAction> actions = new ArrayList<>();


        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);


        List<TextEdit> importEdits = new ArrayList<>();
        Pair<List<String>, List<String>> typesAndNames = getPossibleTypesAndNames(context, referenceAtCursor,
                                                                                  hasDefaultInitFunction,
                                                                                  hasCustomInitFunction, isAsync,
                                                                                  bLangNode,
                                                                                  importEdits, compilerContext);

        List<String> types = typesAndNames.getLeft();
        List<String> names = typesAndNames.getRight();

        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            String name = names.get(i);
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            if (types.size() > 1) {
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }
            Position insertPos = new Position(position.getLine(), position.getCharacter());
            String edit = type + " " + name + " = ";
            List<TextEdit> edits = new ArrayList<>();
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
            edits.addAll(importEdits);
            actions.add(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return actions;
    }

}
