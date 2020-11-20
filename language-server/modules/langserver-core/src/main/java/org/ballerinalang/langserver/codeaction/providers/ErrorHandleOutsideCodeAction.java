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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorHandleOutsideCodeAction extends CreateVariableCodeAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 998;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    CodeActionContext context) {
        String uri = context.fileUri();
        String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
        if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }
        TypeSymbol typeSymbol = context.positionDetails().matchedExprType();
        if (typeSymbol == null || typeSymbol.typeKind() != TypeDescKind.UNION) {
            return Collections.emptyList();
        }
        UnionTypeSymbol unionTypeDesc = (UnionTypeSymbol) typeSymbol;

        boolean hasErrorMemberType = unionTypeDesc.memberTypeDescriptors().stream()
                .anyMatch(member -> member.typeKind() == TypeDescKind.ERROR);
        if (!hasErrorMemberType) {
            return Collections.emptyList();
        }

        Optional<FunctionDefinitionNode> optParentFunction = getParentFunction(context.positionDetails().matchedNode());
        if (optParentFunction.isEmpty()) {
            return Collections.emptyList();
        }

        String filePath = context.filePath().getFileName().toString();
        Optional<SemanticModel> semanticModel = context.workspace().semanticModel(context.filePath());
        if (semanticModel.isEmpty()) {
            return Collections.emptyList();
        }
        FunctionDefinitionNode parentFunction = optParentFunction.get();
        Optional<Symbol> optParentFuncSymbol = semanticModel.get()
                .symbol(filePath, parentFunction.functionName().lineRange().startLine());
        String returnText = "";
        Range returnRange = null;
        if (optParentFuncSymbol.isPresent() && optParentFuncSymbol.get().kind() == SymbolKind.FUNCTION) {
            FunctionSymbol parentFuncSymbol = (FunctionSymbol) optParentFuncSymbol.get();
            if (parentFunction.functionSignature().returnTypeDesc().isPresent() &&
                    parentFuncSymbol.typeDescriptor().returnTypeDescriptor().isPresent()) {
                // Parent function already has a return-type
                TypeSymbol parentRetTypeDesc =
                        parentFuncSymbol.typeDescriptor().returnTypeDescriptor().get();
                ReturnTypeDescriptorNode parentRetTypeDescNode =
                        parentFunction.functionSignature().returnTypeDesc().get();
                if (parentRetTypeDesc.typeKind() == TypeDescKind.UNION) {
                    // Parent function already has a union return-type
                    UnionTypeSymbol parentUnionRetTypeDesc = (UnionTypeSymbol) parentRetTypeDesc;
                    boolean hasErrorMember = parentUnionRetTypeDesc.memberTypeDescriptors().stream()
                            .anyMatch(m -> m.typeKind() == TypeDescKind.ERROR);
                    if (!hasErrorMember) {
                        // Union has no error member-type
                        returnText = "returns " + parentUnionRetTypeDesc.signature() + "|error";
                        returnRange = CommonUtil.toRange(parentRetTypeDescNode.lineRange());
                    }
                } else {
                    // Parent function already has a other return-type
                    returnText = "returns " + parentRetTypeDesc.signature() + "|error";
                    returnRange = CommonUtil.toRange(parentRetTypeDescNode.lineRange());
                }
            } else {
                // Parent function has no return
                returnText = " returns error?";
                Position pos = CommonUtil.toPosition(
                        parentFunction.functionSignature().closeParenToken().lineRange().endLine());
                returnRange = new Range(pos, pos);
            }
        }

        List<TextEdit> edits = new ArrayList<>();
        // Add create variable edits
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(diagnostic, context);

        // Change and add type text edit
        String typeWithError = createVarTextEdits.types.get(0);
        String typeWithoutError = unionTypeDesc.memberTypeDescriptors().stream()
                .filter(member -> member.typeKind() != TypeDescKind.ERROR)
                .map(typeDesc -> CodeActionUtil.getPossibleTypes(typeDesc, edits, context).get(0))
                .collect(Collectors.joining("|"));

        TextEdit textEdit = createVarTextEdits.edits.get(0);
        textEdit.setNewText(typeWithoutError + textEdit.getNewText().substring(typeWithError.length()));
        edits.add(textEdit);
        edits.addAll(createVarTextEdits.imports);

        // Add `check` expression text edit
        Position pos = diagnostic.getRange().getStart();
        Position insertPos = new Position(pos.getLine(), pos.getCharacter());
        edits.add(new TextEdit(new Range(insertPos, insertPos), "check "));

        // Add parent function return change text edits
        if (!returnText.isEmpty()) {
            edits.add(new TextEdit(returnRange, returnText));
        }

        String commandTitle = String.format(CommandConstants.ADD_CHECK_TITLE,
                                            context.positionDetails().matchedSymbol().name());
        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
    }

    private Optional<FunctionDefinitionNode> getParentFunction(NonTerminalNode matchedNode) {
        FunctionDefinitionNode functionDefNode = null;
        NonTerminalNode parentNode = matchedNode;
        while (parentNode.kind() != SyntaxKind.FUNCTION_DEFINITION || parentNode.kind() != SyntaxKind.MODULE_PART) {
            parentNode = parentNode.parent();
            if (parentNode == null) {
                break;
            }
            if (parentNode.kind() == SyntaxKind.FUNCTION_DEFINITION &&
                    parentNode.parent() != null && parentNode.parent().kind() == SyntaxKind.MODULE_PART) {
                functionDefNode = (FunctionDefinitionNode) parentNode;
                break;
            }
        }
        return Optional.ofNullable(functionDefNode);
    }
}
