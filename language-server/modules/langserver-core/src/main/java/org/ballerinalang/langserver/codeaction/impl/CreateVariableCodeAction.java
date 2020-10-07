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
package org.ballerinalang.langserver.codeaction.impl;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction.getPossibleTypesAndNames;
import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
public class CreateVariableCodeAction implements DiagBasedCodeAction {
    private SemanticModel semanticModel;

    public CreateVariableCodeAction(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Optional<Path> path = CommonUtil.getPathFromURI(uri);
        if (path.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Position position = diagnostic.getRange().getStart();
            NonTerminalNode node = CommonUtil.findNode(context, position, path.get());
            Optional<NonTerminalNode> scopedNode = getBroadScopedNode(node);
            if (scopedNode.isEmpty()) {
                return new ArrayList<>();
            }

            LinePosition scopedNodePos = LinePosition.from(scopedNode.get().lineRange().startLine().line() + 1,
                                                           scopedNode.get().lineRange().startLine().offset() + 2);
            String filePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            Optional<Symbol> scopedSymbol = semanticModel.symbol(filePath, scopedNodePos);
            if (scopedSymbol.isEmpty()) {
                return new ArrayList<>();
            }
            Optional<BallerinaTypeDescriptor> type = getTypeDescriptor(scopedSymbol.get());
            if (type.isEmpty()) {
                return new ArrayList<>();
            }
            boolean hasDefaultInitFunction = false;
            boolean hasCustomInitFunction = false;
            boolean isAsync = false;
            //TODO: Fix this
//        if (semanticModel.getbLangNode() instanceof BLangInvocation) {
//            hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
//            hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
//                    symbolAtCursor.name.value.endsWith("init");
//            isAsync = ((BLangInvocation) semanticModel.getbLangNode()).isAsync();
//        }
//
//        // Find enclosing function node
//        BLangNode bLangNode = semanticModel.getbLangNode();
//        BLangFunction enclosedFunc = null;
//        while (!(bLangNode instanceof BLangPackage)) {
//            if (bLangNode instanceof BLangFunction) {
//                enclosedFunc = (BLangFunction) bLangNode;
//                break;
//            }
//            bLangNode = bLangNode.parent;
//        }
//
            return getCreateVariableCodeActions(context, uri, position, scopedSymbol.get(),
                                                hasDefaultInitFunction, hasCustomInitFunction, isAsync);
        } catch (WorkspaceDocumentException e) {
            //ignore
            return new ArrayList<>();
        }
    }

    private Optional<BallerinaTypeDescriptor> getTypeDescriptor(Symbol scopedSymbol) {
        if (scopedSymbol.kind() == SymbolKind.FUNCTION) {
            FunctionSymbol functionSymbol = (FunctionSymbol) scopedSymbol;
            return functionSymbol.typeDescriptor();
//            if (typeDescriptor.isPresent()) {
//                FunctionTypeDescriptor funTypeDesc = (FunctionTypeDescriptor) typeDescriptor.get();
//                Optional<BallerinaTypeDescriptor> returnType = funTypeDesc.getReturnType();
//                if (returnType.isPresent()) {
//                    BallerinaTypeDescriptor typeDescriptor1 = returnType.get();
//                }
//            }
        }
        return Optional.empty();
    }

    private static List<CodeAction> getCreateVariableCodeActions(LSContext context, String uri, Position position,
                                                                 Symbol symbol,
                                                                 boolean hasDefaultInitFunction,
                                                                 boolean hasCustomInitFunction, boolean isAsync) {
        List<CodeAction> actions = new ArrayList<>();


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

    private static Optional<NonTerminalNode> getBroadScopedNode(NonTerminalNode node) {
        while (node != null && (node.kind() != SyntaxKind.MODULE_PART &&
                node.kind() != SyntaxKind.FUNCTION_CALL &&
                node.kind() != SyntaxKind.METHOD_CALL &&
                node.kind() != SyntaxKind.REMOTE_METHOD_CALL_ACTION &&
                node.kind() != SyntaxKind.IMPLICIT_NEW_EXPRESSION &&
                node.kind() != SyntaxKind.EXPLICIT_NEW_EXPRESSION)) {
            node = node.parent();
        }
        return Optional.ofNullable(node);
    }
}
