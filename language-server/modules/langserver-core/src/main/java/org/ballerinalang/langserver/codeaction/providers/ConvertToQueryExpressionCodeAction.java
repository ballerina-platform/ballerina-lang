/*
 *  Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.annotation.JavaSPIService;
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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToQueryExpressionCodeAction implements RangeBasedCodeActionProvider {

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

        Optional<LhsRhsSymbolInfo> symbolInfo = getLhsAndRhsSymbolInfo(matchedNode, context);
        if (symbolInfo.isEmpty()) {
            return Collections.emptyList();
        }

        Symbol lhsSymbol = symbolInfo.get().lhsSymbol;
        Symbol rhsSymbol = symbolInfo.get().rhsSymbol;
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

        // If rhs member type is a subtype, then solution is straight forward
        if (rhsMemberType.subtypeOf(lhsMemberType)) {
            // lhs = from var item in lhs select item;
            String query = String.format("from var item in %s select item", rhsSymbol.getName().get());
            List<TextEdit> edits = new ArrayList<>();
            Range range = PositionUtil.toRange(symbolInfo.get().rhsNode.lineRange());
            edits.add(new TextEdit(range, query));
            CodeAction codeAction = CodeActionUtil.createCodeAction("Convert to query expression",
                    edits, context.fileUri(), CodeActionKind.QuickFix);
            return List.of(codeAction);
        }

        // LHS and RHS are different.
        // If LHS is a record, we have to generate the default value for that
        // Else, we have to just generate the query expression
        if (lhsMemberType.typeKind() == TypeDescKind.RECORD) {
            Optional<String> defaultVal = DefaultValueGenerationUtil.getDefaultValueForType(lhsMemberType);
            if (defaultVal.isPresent()) {
                String query = String.format("from var item in %s select %s", rhsSymbol.getName().get(), defaultVal.get());
                List<TextEdit> edits = new ArrayList<>();
                Range range = PositionUtil.toRange(symbolInfo.get().rhsNode.lineRange());
                edits.add(new TextEdit(range, query));
                CodeAction codeAction = CodeActionUtil.createCodeAction("Convert to query expression",
                        edits, context.fileUri(), CodeActionKind.QuickFix);
                return List.of(codeAction);
            }
        }

        String selectExpr = "item";
        String query = String.format("from var item in %s select %s", rhsSymbol.getName().get(), selectExpr);
        List<TextEdit> edits = new ArrayList<>();
        Range range = PositionUtil.toRange(symbolInfo.get().rhsNode.lineRange());
        edits.add(new TextEdit(range, query));
        CodeAction codeAction = CodeActionUtil.createCodeAction("Convert to query expression",
                edits, context.fileUri(), CodeActionKind.QuickFix);
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
        Node lhsNode = null;
        Node rhsNode = null;
        SemanticModel semanticModel = context.currentSemanticModel().get();
        if (matchedNode.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode node = (VariableDeclarationNode) matchedNode;
            rhsSymbol = semanticModel.symbol(node.initializer().get());
            rhsNode = node.initializer().get();
            lhsSymbol = semanticModel.symbol(node.typedBindingPattern())
                    .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE);
            lhsNode = node.typedBindingPattern();
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
            lhsSymbol = semanticModel.symbol(node.fieldName())
                    .filter(symbol -> symbol.kind() == SymbolKind.RECORD_FIELD);
            lhsNode = node.fieldName();
            rhsSymbol = semanticModel.symbol(node.valueExpr().get());
            rhsNode = node.valueExpr().get();
        }

        if (rhsSymbol.isEmpty() || lhsSymbol.isEmpty()
                || lhsNode == null || rhsNode == null
                || rhsSymbol.get().kind() != SymbolKind.VARIABLE && rhsSymbol.get().kind() != SymbolKind.PARAMETER) {
            return Optional.empty();
        }
        LhsRhsSymbolInfo nodeInfo = new LhsRhsSymbolInfo(lhsSymbol.get(), rhsSymbol.get(), lhsNode, rhsNode);
        return Optional.of(nodeInfo);
    }

    static class LhsRhsSymbolInfo {

        private Symbol lhsSymbol;
        private Symbol rhsSymbol;
        private Node lhsNode;
        private Node rhsNode;

        public LhsRhsSymbolInfo(Symbol lhsSymbol, Symbol rhsSymbol, Node lhsNode, Node rhsNode) {
            this.lhsSymbol = lhsSymbol;
            this.rhsSymbol = rhsSymbol;
            this.lhsNode = lhsNode;
            this.rhsNode = rhsNode;
        }
    }
}
