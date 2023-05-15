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

import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
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
public class TypeCastCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Type Cast";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        //Check if there is a type cast expression already present.
        MatchedExpressionNodeResolver expressionResolver =
                new MatchedExpressionNodeResolver(positionDetails.matchedNode());
        Optional<ExpressionNode> expressionNode = expressionResolver.findExpression(positionDetails.matchedNode());
        if (expressionNode.isEmpty() || expressionNode.get().kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> optionalActualTypeSymbol;
        if ("BCE2068".equals(diagnostic.diagnosticInfo().code())) {
            optionalActualTypeSymbol = positionDetails.diagnosticProperty(CodeActionUtil
                    .getDiagPropertyFilterFunction(DiagBasedPositionDetails
                            .DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else {
            optionalActualTypeSymbol = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        }

        Optional<TypeSymbol> optionalExpectedTypeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED_SYMBOL_INDEX);
        if (optionalExpectedTypeSymbol.isEmpty() || optionalActualTypeSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        TypeSymbol actualTypeSymbol = CommonUtil.getRawType(optionalActualTypeSymbol.get());
        TypeSymbol expectedTypeSymbol = optionalExpectedTypeSymbol.get();

        if (actualTypeSymbol.typeKind() == TypeDescKind.UNION) {
            // If RHS is a union and has error member type; skip code-action
            if (CodeActionUtil.hasErrorMemberType((UnionTypeSymbol) actualTypeSymbol)) {
                return Collections.emptyList();
            }
        }

        //Consider the type parameter of future type symbols within the wait action.
        if (actualTypeSymbol.typeKind() == TypeDescKind.FUTURE
                && expressionNode.get().kind() == SyntaxKind.WAIT_ACTION) {
            if (expectedTypeSymbol.typeKind() != TypeDescKind.FUTURE) {
                return Collections.emptyList();
            }
            optionalExpectedTypeSymbol = ((FutureTypeSymbol) expectedTypeSymbol).typeParameter();
            optionalActualTypeSymbol = ((FutureTypeSymbol) actualTypeSymbol).typeParameter();
            if (optionalActualTypeSymbol.isEmpty() || optionalExpectedTypeSymbol.isEmpty()) {
                return Collections.emptyList();
            }
            actualTypeSymbol = CommonUtil.getRawType(optionalActualTypeSymbol.get());
            expectedTypeSymbol = optionalExpectedTypeSymbol.get();
        }

        String typeName = "";
        if (expectedTypeSymbol.subtypeOf(actualTypeSymbol) && expectedTypeSymbol.typeKind() != TypeDescKind.SINGLETON) {
            typeName = NameUtil.getModifiedTypeName(context, expectedTypeSymbol);
        } else if (isNumeric(expectedTypeSymbol)) {
            Optional<TypeSymbol> numericExpected = findNumericType(expectedTypeSymbol);
            Optional<TypeSymbol> numericActual = findNumericType(actualTypeSymbol);
            //Numeric types can be cast between each other.
            if (numericActual.isPresent() && numericExpected.isPresent()) {
                typeName = NameUtil.getModifiedTypeName(context, numericExpected.get());
            }
        }

        if (typeName.isEmpty()) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = new ArrayList<>(getTextEdits(expressionNode.get(), typeName));
        String commandTitle = CommandConstants.ADD_TYPE_CAST_TITLE;
        return Collections.singletonList(CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.QuickFix));
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
     * @param expressionNode   Expression Node of the diagnostic position
     * @param expectedTypeName Expected type name as a string
     * @return Text edits to perform the cast
     */
    protected List<TextEdit> getTextEdits(Node expressionNode, String expectedTypeName) {

        Position startPosition = PositionUtil.toPosition(expressionNode.lineRange().startLine());
        Position endPosition = PositionUtil.toPosition(expressionNode.lineRange().endLine());

        String editText = "<" + expectedTypeName + ">";

        // If the expression is a binary expression, need to add parentheses around the expression
        if (expressionNode.kind() == SyntaxKind.BINARY_EXPRESSION) {
            editText = editText + CommonKeys.OPEN_PARENTHESES_KEY;
            TextEdit castWithParentheses = new TextEdit(new Range(startPosition, startPosition), editText);
            TextEdit closeParentheses = new TextEdit(new Range(endPosition, endPosition),
                    CommonKeys.CLOSE_PARENTHESES_KEY);

            return List.of(castWithParentheses, closeParentheses);
        }

        return List.of(new TextEdit(new Range(startPosition, startPosition), editText));
    }

    private boolean isNumeric(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors().stream()
                    .anyMatch(this::isNumeric);
        }
        return (typeSymbol.typeKind().isIntegerType()
                || typeSymbol.typeKind() == TypeDescKind.FLOAT
                || typeSymbol.typeKind() == TypeDescKind.DECIMAL);
    }

    private Optional<TypeSymbol> findNumericType(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors().stream().filter(this::isNumeric).findFirst();
        }

        if (isNumeric(typeSymbol)) {
            return Optional.of(typeSymbol);
        }
        
        return Optional.empty();
    }
}
