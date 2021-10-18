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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for change variable type.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ChangeVariableTypeCodeAction extends TypeCastCodeAction {

    public static final String NAME = "Change Variable Type";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().contains(CommandConstants.INCOMPATIBLE_TYPES))) {
            return Collections.emptyList();
        }


        Optional<TypeSymbol> typeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        if (typeSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        // Skip, non-local var declarations
        Optional<NonTerminalNode> variableNode = getVariableNode(positionDetails.matchedNode());
        if (variableNode.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<ExpressionNode> typeNode = getTypeNode(variableNode.get(), context);
        Optional<String> variableName = getVariableName(variableNode.get());
        if (typeNode.isEmpty() || variableName.isEmpty()) {
            return Collections.emptyList();
        }

        // Derive possible types
        Optional<String> typeNodeStr = getTypeNodeStr(typeNode.get());
        List<CodeAction> actions = new ArrayList<>();
        List<TextEdit> importEdits = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeSymbol.get(), importEdits, context);
        for (String type : types) {
            if (typeNodeStr.isPresent() && typeNodeStr.get().equals(type)) {
                // Skip suggesting same type
                continue;
            }
            List<TextEdit> edits = new ArrayList<>();
            edits.add(new TextEdit(CommonUtil.toRange(typeNode.get().lineRange()), type));
            String commandTitle = String.format(CommandConstants.CHANGE_VAR_TYPE_TITLE, variableName.get(), type);
            actions.add(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
        }
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Optional<NonTerminalNode> getVariableNode(NonTerminalNode sNode) {
        // Find var node
        while (sNode != null &&
                sNode.kind() != SyntaxKind.LOCAL_VAR_DECL &&
                sNode.kind() != SyntaxKind.MODULE_VAR_DECL &&
                sNode.kind() != SyntaxKind.ASSIGNMENT_STATEMENT) {
            // The cursor can be within a positional/named arg. If so, don't show this code action
            if (sNode.kind() == SyntaxKind.POSITIONAL_ARG || sNode.kind() == SyntaxKind.NAMED_ARG) {
                return Optional.empty();
            }
            sNode = sNode.parent();
        }

        return Optional.ofNullable(sNode);
    }

    private Optional<String> getTypeNodeStr(ExpressionNode expressionNode) {
        if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode sRefNode = (SimpleNameReferenceNode) expressionNode;
            return Optional.of(sRefNode.name().text());
        } else if (expressionNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qnRefNode = (QualifiedNameReferenceNode) expressionNode;
            return Optional.of(qnRefNode.modulePrefix().text() + ":" + qnRefNode.identifier().text());
        }
        return Optional.empty();
    }

    private Optional<ExpressionNode> getTypeNode(Node matchedNode, CodeActionContext context) {
        switch (matchedNode.kind()) {
            case LOCAL_VAR_DECL:
                return Optional.of(
                        ((VariableDeclarationNode) matchedNode).typedBindingPattern().typeDescriptor());
            case ASSIGNMENT_STATEMENT:
                Optional<VariableSymbol> optVariableSymbol = getVariableSymbol(context, matchedNode);
                if (optVariableSymbol.isEmpty()) {
                    return Optional.empty();
                }
                SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
                Optional<NonTerminalNode> node = CommonUtil.findNode(optVariableSymbol.get(), syntaxTree);
                if (node.isPresent() && node.get().kind() == SyntaxKind.TYPED_BINDING_PATTERN) {
                    return Optional.of(((TypedBindingPatternNode) node.get()).typeDescriptor());
                } else {
                    return Optional.empty();
                }
            default:
                return Optional.empty();
        }
    }

    private Optional<String> getVariableName(Node matchedNode) {
        switch (matchedNode.kind()) {
            case LOCAL_VAR_DECL:
                VariableDeclarationNode variableDeclrNode = (VariableDeclarationNode) matchedNode;
                BindingPatternNode bindingPatternNode = variableDeclrNode.typedBindingPattern().bindingPattern();
                if (bindingPatternNode.kind() != SyntaxKind.CAPTURE_BINDING_PATTERN) {
                    return Optional.empty();
                }
                CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) bindingPatternNode;
                return Optional.of(captureBindingPatternNode.variableName().text());
            case ASSIGNMENT_STATEMENT:
                AssignmentStatementNode assignmentStmtNode = (AssignmentStatementNode) matchedNode;
                Node varRef = assignmentStmtNode.varRef();
                if (varRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    return Optional.of(((SimpleNameReferenceNode) varRef).name().text());
                } else if (varRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    return Optional.of(((QualifiedNameReferenceNode) varRef).identifier().text());
                }
                return Optional.empty();
            default:
                return Optional.empty();
        }
    }
}
