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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction.getPossibleTypesAndNames;
import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
public class ErrorTypeCodeAction implements DiagBasedCodeAction {
    private SemanticModel semanticModel;

    public ErrorTypeCodeAction(SemanticModel semanticModel) {
        this.semanticModel = null;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        String filePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        Position position = diagnostic.getRange().getStart();

        LinePosition linePosition = LinePosition.from(position.getLine() + 1, position.getCharacter() + 2);
        Optional<Symbol> symbolAtCursor = semanticModel.symbol(filePath, linePosition);
        if (symbolAtCursor.isEmpty()) {
            return new ArrayList<>();
        }
        Symbol symbol = symbolAtCursor.get();

        boolean hasDefaultInitFunction = false;
        boolean hasCustomInitFunction = false;
        boolean isAsync = false;
        if (symbol instanceof BLangInvocation) {
            hasDefaultInitFunction = symbol instanceof BObjectTypeSymbol;
            hasCustomInitFunction = symbol instanceof BInvokableSymbol &&
                    symbol.name().endsWith("init");
            //TODO Fix this, always false
            isAsync = false;
        }

        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        List<TextEdit> importEdits = new ArrayList<>();
        Pair<List<String>, List<String>> typesAndNames = getPossibleTypesAndNames(context, symbol,
                                                                                  hasDefaultInitFunction,
                                                                                  hasCustomInitFunction, isAsync,
                                                                                  importEdits, compilerContext);

        List<String> types = typesAndNames.getLeft();
        List<String> names = typesAndNames.getRight();

        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            String name = names.get(i);
            if (type.endsWith("|error")) {
                return addErrorTypeBasedCodeActions(uri, symbol, position, importEdits, type, name);
            }
        }
        return new ArrayList<>();
    }


    private static List<CodeAction> addErrorTypeBasedCodeActions(String uri, Symbol symbol,
                                                                 Position position, List<TextEdit> importEdits,
                                                                 String type,
                                                                 String name) {
        List<CodeAction> actions = new ArrayList<>();
        // add code action for `check`
        if (symbol != null) {
            boolean hasError = false;
            //TODO: Fix this
            BType returnType = null;
            if (returnType instanceof BErrorType) {
                hasError = true;
            } else if (returnType instanceof BUnionType) {
                BUnionType unionType = (BUnionType) returnType;
                hasError = unionType.getMemberTypes().stream().anyMatch(s -> s instanceof BErrorType);
            }
            if (hasError) {
                String panicCmd = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", "Check");
                String edit = type.replace("|error", "") + " " + name + " = check ";
                List<TextEdit> edits = new ArrayList<>();
                Position insertPos = new Position(position.getLine(), position.getCharacter());
                edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
                edits.addAll(importEdits);
                actions.add(createQuickFixCodeAction(panicCmd, edits, uri));
            }
        }
        // add code action for `checkpanic`
        String panicCmd = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", "CheckPanic");
        String edit = type.replace("|error", "") + " " + name + " = checkpanic ";
        List<TextEdit> edits = new ArrayList<>();
        Position insertPos = new Position(position.getLine(), position.getCharacter());
        edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
        edits.addAll(importEdits);
        actions.add(createQuickFixCodeAction(panicCmd, edits, uri));
        return actions;
    }

}
