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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.getAddCheckTextEdits;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddCheckCodeAction extends TypeGuardCodeAction {
    public AddCheckCodeAction() {
        super();
        this.codeActionNodeTypes = Arrays.asList(CodeActionNodeType.LOCAL_VARIABLE,
                                                 CodeActionNodeType.ASSIGNMENT);
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        NonTerminalNode matchedNode = posDetails.matchedStatementNode();
        boolean isAssignment = matchedNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT;
        boolean isVarDeclr = matchedNode.kind() == SyntaxKind.LOCAL_VAR_DECL;
        // Skip, if not a var declaration or assignment
        if (!isVarDeclr && !isAssignment) {
            return Collections.emptyList();
        }

        // Get LHS union type-symbol and type-desc-node of the variable
        Optional<Pair<UnionTypeSymbol, TypeDescriptorNode>> varTypeSymbolAndTypeDescNodePair =
                getVarTypeSymbolAndTypeNode(matchedNode, posDetails, context);
        if (varTypeSymbolAndTypeDescNodePair.isEmpty()) {
            return Collections.emptyList();
        }
        UnionTypeSymbol varTypeSymbol = varTypeSymbolAndTypeDescNodePair.get().getLeft();
        TypeDescriptorNode varTypeDescNode = varTypeSymbolAndTypeDescNodePair.get().getRight();

        // Get RHS expression
        Optional<ExpressionNode> rhsExpression = getRHSExpression(matchedNode);
        if (rhsExpression.isEmpty()) {
            return Collections.emptyList();
        }

        // Skip, if RHS doesn't contains error union member type
        if (!containsErrorMemberType(context, rhsExpression.get())) {
            return Collections.emptyList();
        }

        // Generate RHS `check` text-edit
        Position pos = CommonUtil.toPosition(rhsExpression.get().lineRange().startLine());
        List<TextEdit> edits = new ArrayList<>(getAddCheckTextEdits(pos, matchedNode, context));

        // Generate `error` member type removal text-edit
        edits.addAll(getErrorTypeRemovalTextEdits(varTypeDescNode, varTypeSymbol, context));

        CodeAction codeAction = AbstractCodeActionProvider.createQuickFixCodeAction(CommandConstants.ADD_CHECK_TITLE,
                                                                                    edits, context.fileUri());
        return Collections.singletonList(codeAction);
    }

    private List<TextEdit> getErrorTypeRemovalTextEdits(TypeDescriptorNode varTypeDescNode,
                                                        UnionTypeSymbol varTypeSymbol,
                                                        CodeActionContext context) {
        List<TextEdit> edits = new ArrayList<>();
        Range range = CommonUtil.toRange(varTypeDescNode.lineRange());
        String typeWithoutError = varTypeSymbol.memberTypeDescriptors().stream()
                .filter(member -> member.typeKind() != TypeDescKind.ERROR)
                .map(typeDesc -> CodeActionUtil.getPossibleType(typeDesc, edits, context).orElseThrow())
                .collect(Collectors.joining("|"));
        edits.add(new TextEdit(range, typeWithoutError));
        return edits;
    }

    private Optional<ExpressionNode> getRHSExpression(Node matchedNode) {
        switch (matchedNode.kind()) {
            case ASSIGNMENT_STATEMENT:
                AssignmentStatementNode assignmentStmtNode = (AssignmentStatementNode) matchedNode;
                ExpressionNode expression = assignmentStmtNode.expression();
                return Optional.of(expression);
            case LOCAL_VAR_DECL:
                // Skip, if initializer is not found
                VariableDeclarationNode varDeclrNode = (VariableDeclarationNode) matchedNode;
                if (varDeclrNode.initializer().isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(varDeclrNode.initializer().get());
            default:
                return Optional.empty();
        }
    }

    private boolean containsErrorMemberType(CodeActionContext context, ExpressionNode expressionNode) {
        SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
        Optional<TypeSymbol> typeSymbol = semanticModel.type(expressionNode.lineRange());
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() != TypeDescKind.UNION) {
            return false;
        }
        UnionTypeSymbol unionTypeDesc = (UnionTypeSymbol) typeSymbol.get();
        return unionTypeDesc.memberTypeDescriptors().stream()
                .anyMatch(member -> member.typeKind() == TypeDescKind.ERROR);
    }
}
