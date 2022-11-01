/*
 *  Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code action provider to convert RHS variable assignment to a query expression when both LHS and RHS types are
 * arrays. If the LHS is an array of records, we provide the default value for the record in the select clause.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToQueryExpressionCodeAction implements RangeBasedCodeActionProvider {

    private static final String TITLE = "Map with a query expression";

    @Override
    public String getName() {
        return "CONVERT_TO_QUERY";
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedNode = posDetails.matchedCodeActionNode();
        if (context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }

        Optional<LhsRhsSymbolInfo> optSymbolInfo = getLhsAndRhsSymbolInfo(matchedNode, context);
        if (optSymbolInfo.isEmpty()) {
            return Collections.emptyList();
        }

        LhsRhsSymbolInfo symbolInfo = optSymbolInfo.get();
        Symbol lhsSymbol = symbolInfo.lhsSymbol;
        Symbol rhsSymbol = symbolInfo.rhsSymbol;
        Optional<TypeSymbol> rhsType = SymbolUtil.getTypeDescriptor(rhsSymbol);
        Optional<TypeSymbol> lhsType = SymbolUtil.getTypeDescriptor(lhsSymbol);

        if (rhsType.isEmpty()
                || lhsType.isEmpty()
                || rhsType.get().typeKind() != TypeDescKind.ARRAY
                || lhsType.get().typeKind() != TypeDescKind.ARRAY) {
            return Collections.emptyList();
        }

        // Now we know both lhs and rhs are arrays.
        // Next we have to check if the member types are assignable
        TypeSymbol lhsMemberType = CommonUtil.getRawType(((ArrayTypeSymbol) lhsType.get()).memberTypeDescriptor());
        TypeSymbol rhsMemberType = CommonUtil.getRawType(((ArrayTypeSymbol) rhsType.get()).memberTypeDescriptor());

        String queryExpr = null;
        Range range = PositionUtil.toRange(symbolInfo.rhsNode.lineRange());

        // If rhs member type is a subtype, then solution is straight forward
        if (rhsMemberType.subtypeOf(lhsMemberType)) {
            // lhs = from var item in lhs select item;
            queryExpr = String.format("from var item in %s select item", rhsSymbol.getName().get());
        }

        // LHS and RHS are different.
        // If LHS is a record, we have to generate the default value for that
        if (queryExpr == null && lhsMemberType.typeKind() == TypeDescKind.RECORD) {
            Optional<String> defaultVal = DefaultValueGenerationUtil.getDefaultValueForType(lhsMemberType);
            if (defaultVal.isPresent()) {
                queryExpr = String.format("from var item in %s select %s", rhsSymbol.getName().get(), defaultVal.get());
            }
        }

        // Else, we have to just generate the query expression
        if (queryExpr == null) {
            String selectExpr = "item";
            queryExpr = String.format("from var item in %s select %s", rhsSymbol.getName().get(), selectExpr);
        }
        try {
            // Format and trim whitespaces
            queryExpr = Formatter.format(queryExpr).trim();
        } catch (FormatterException e) {
            // We log the error and move on
            LSClientLogger.getInstance(context.languageServercontext()).logError(LSContextOperation.TXT_CODE_ACTION,
                    "Failed to format query expression", e, null, (Position) null);
        }

        // The formatter will indent the 2nd line (with select clause) by a single tab since we formatted only the
        // query expression. So, we have to explicitly add more padding. Then rejoin by the line separator to get
        // the final output.
        int offset = symbolInfo.lhsNode.lineRange().startLine().offset();
        String[] lines = queryExpr.split(CommonUtil.LINE_SEPARATOR);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                lines[i] = StringUtils.repeat(' ', offset) + lines[i];
            }
        }
        queryExpr = String.join(CommonUtil.LINE_SEPARATOR, lines);

        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(range, queryExpr));
        CodeAction codeAction = CodeActionUtil.createCodeAction(TITLE, 
                edits, context.fileUri(), CodeActionKind.RefactorRewrite);
        return List.of(codeAction);
    }

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(
                SyntaxKind.LOCAL_VAR_DECL,
                SyntaxKind.SPECIFIC_FIELD,
                SyntaxKind.ASSIGNMENT_STATEMENT
        );
    }

    private Optional<LhsRhsSymbolInfo> getLhsAndRhsSymbolInfo(NonTerminalNode matchedNode, CodeActionContext context) {
        Optional<Symbol> rhsSymbol = Optional.empty();
        Optional<Symbol> lhsSymbol = Optional.empty();
        NonTerminalNode rhsNode = null;
        Node lhsNode = null;

        SemanticModel semanticModel = context.currentSemanticModel().get();
        if (matchedNode.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode node = (VariableDeclarationNode) matchedNode;
            if (node.initializer().isPresent()) {
                rhsNode = node.initializer().get();
                rhsSymbol = semanticModel.symbol(rhsNode);
                lhsNode = node.typedBindingPattern();
                lhsSymbol = semanticModel.symbol(lhsNode)
                        .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE);
            }
        } else if (matchedNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            // There can be 2 types here: Variable assignments and field access expressions.
            // 1. list = otherList;
            // 2. obj.list = otherList;
            AssignmentStatementNode node = (AssignmentStatementNode) matchedNode;
            if (node.varRef().kind() == SyntaxKind.FIELD_ACCESS) {
                FieldAccessExpressionNode exprNode = (FieldAccessExpressionNode) node.varRef();
                lhsNode = exprNode.fieldName();
                lhsSymbol = semanticModel.symbol(lhsNode)
                        .filter(symbol -> symbol.kind() == SymbolKind.CLASS_FIELD
                                || symbol.kind() == SymbolKind.RECORD_FIELD);
            } else {
                lhsNode = node.varRef();
                lhsSymbol = semanticModel.symbol(lhsNode).filter(symbol -> symbol.kind() == SymbolKind.VARIABLE);
            }
            rhsSymbol = semanticModel.symbol(node.expression());
            rhsNode = node.expression();
        } else if (matchedNode.kind() == SyntaxKind.SPECIFIC_FIELD) {
            SpecificFieldNode node = (SpecificFieldNode) matchedNode;
            lhsNode = node.fieldName();
            lhsSymbol = semanticModel.symbol(lhsNode)
                    .filter(symbol -> symbol.kind() == SymbolKind.RECORD_FIELD);
            rhsSymbol = semanticModel.symbol(node.valueExpr().get());
            rhsNode = node.valueExpr().get();
        }

        if (rhsSymbol.isEmpty() || lhsSymbol.isEmpty()
                || rhsNode == null
                || rhsSymbol.get().kind() != SymbolKind.VARIABLE && rhsSymbol.get().kind() != SymbolKind.PARAMETER) {
            return Optional.empty();
        }
        LhsRhsSymbolInfo nodeInfo = new LhsRhsSymbolInfo(lhsSymbol.get(), rhsSymbol.get(), rhsNode, lhsNode);
        return Optional.of(nodeInfo);
    }

    static class LhsRhsSymbolInfo {

        private final Symbol lhsSymbol;
        private final Symbol rhsSymbol;
        private final NonTerminalNode rhsNode;
        private final Node lhsNode;

        public LhsRhsSymbolInfo(Symbol lhsSymbol, Symbol rhsSymbol, NonTerminalNode rhsNode, Node lhsNode) {
            this.lhsSymbol = lhsSymbol;
            this.rhsSymbol = rhsSymbol;
            this.rhsNode = rhsNode;
            this.lhsNode = lhsNode;
        }
    }
}
