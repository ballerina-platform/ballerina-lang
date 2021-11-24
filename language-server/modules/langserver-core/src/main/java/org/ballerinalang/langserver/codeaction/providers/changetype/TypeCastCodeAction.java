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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ExpressionNodeResolver;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action for incompatible types.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TypeCastCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Type Cast";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068");

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        //Check if there is a type cast expression already present.
        ExpressionNodeResolver expressionResolver = new ExpressionNodeResolver();
        Optional<ExpressionNode> expressionNode = positionDetails.matchedNode().apply(expressionResolver);
        if (expressionNode.isEmpty() || expressionNode.get().kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> rhsTypeSymbol;
        if ("BCE2068".equals(diagnostic.diagnosticInfo().code())) {
            rhsTypeSymbol = positionDetails.diagnosticProperty(CodeActionUtil
                    .getDiagPropertyFilterFunction(DiagBasedPositionDetails
                            .DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else {
            rhsTypeSymbol = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        }

        Optional<TypeSymbol> lhsTypeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED_SYMBOL_INDEX);
        if (lhsTypeSymbol.isEmpty() || rhsTypeSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        if (rhsTypeSymbol.get().typeKind() == TypeDescKind.UNION) {
            // If RHS is a union and has error member type; skip code-action
            if (CodeActionUtil.hasErrorMemberType((UnionTypeSymbol) rhsTypeSymbol.get())) {
                return Collections.emptyList();
            }
        }

        List<TextEdit> edits = new ArrayList<>();
        //numeric types can be casted between each other.
        if (!lhsTypeSymbol.get().subtypeOf(rhsTypeSymbol.get()) && (!isNumeric(lhsTypeSymbol.get())
                || !isNumeric(rhsTypeSymbol.get()))) {
            return Collections.emptyList();
        }
        String typeName = lhsTypeSymbol.get().signature();
        if (typeName.isEmpty()) {
            return Collections.emptyList();
        }

        edits.addAll(getTextEdits(positionDetails, typeName));
        String commandTitle = CommandConstants.ADD_TYPE_CAST_TITLE;
        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
    }

    @Override
    public String getName() {
        return NAME;
    }

    protected Optional<VariableSymbol> getVariableSymbol(CodeActionContext context, Node matchedNode) {
        AssignmentStatementNode assignmentStmtNode = (AssignmentStatementNode) matchedNode;
        Optional<Symbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(assignmentStmtNode.varRef()));

        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        return Optional.of((VariableSymbol) symbol.get());
    }

    /**
     * Get text edits for the provided type name to cast the node in focus to match the left hand side of
     * the assignment/var declaration, etc. This considers if additional parentheses requires to be added around
     * the RHS expression.
     *
     * @param positionDetails  Expression Node of the diagnostic position
     * @param expectedTypeName Expected type name as a string
     * @return Text edits to perform the cast
     */
    private List<TextEdit> getTextEdits(DiagBasedPositionDetails positionDetails, String expectedTypeName) {

        NonTerminalNode matchedNode = positionDetails.matchedNode();
        Position startPosition = CommonUtil.toPosition(matchedNode.lineRange().startLine());
        Position endPosition = CommonUtil.toPosition(matchedNode.lineRange().endLine());

        String editText = "<" + expectedTypeName + "> ";

        // If the expression is a binary expression, need to add parentheses around the expression
        if (matchedNode.kind() == SyntaxKind.BINARY_EXPRESSION) {
            editText = editText + CommonKeys.OPEN_PARENTHESES_KEY;
            TextEdit castWithParentheses = new TextEdit(new Range(startPosition, startPosition), editText);
            TextEdit closeParentheses = new TextEdit(new Range(endPosition, endPosition),
                    CommonKeys.CLOSE_PARENTHESES_KEY);

            return List.of(castWithParentheses, closeParentheses);
        }

        return List.of(new TextEdit(new Range(startPosition, startPosition), editText));
    }

    private boolean isNumeric(TypeSymbol typeSymbol) {
        return (typeSymbol.typeKind().isIntegerType()
                || typeSymbol.typeKind() == TypeDescKind.FLOAT
                || typeSymbol.typeKind() == TypeDescKind.DECIMAL);
    }
}
