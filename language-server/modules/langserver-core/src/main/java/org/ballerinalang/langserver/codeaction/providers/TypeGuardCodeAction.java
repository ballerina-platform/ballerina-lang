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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for type guard variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TypeGuardCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Type Guard";

    public List<SyntaxKind> getSyntaxKinds() {
        return Arrays.asList(SyntaxKind.LOCAL_VAR_DECL, SyntaxKind.ASSIGNMENT_STATEMENT);
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedNode = posDetails.matchedStatementNode();
        if (!isInValidContext(matchedNode, context)) {
            return Collections.emptyList();
        }

        // Get LHS union type-symbol and type-desc-node of the variable
        Optional<Pair<UnionTypeSymbol, TypeDescriptorNode>> varTypeSymbolAndTypeDescNodePair =
                getVarTypeSymbolAndTypeNode(matchedNode, posDetails, context);
        if (varTypeSymbolAndTypeDescNodePair.isEmpty()) {
            return Collections.emptyList();
        }
        UnionTypeSymbol varTypeSymbol = varTypeSymbolAndTypeDescNodePair.get().getLeft();
        boolean hasCompilationError = varTypeSymbol.memberTypeDescriptors().stream()
                .anyMatch(s -> s.typeKind() == TypeDescKind.COMPILATION_ERROR);
        if (hasCompilationError) {
            return Collections.emptyList();
        }

        // Get var name
        Optional<String> varName = getVariableName(matchedNode);
        if (varName.isEmpty()) {
            return Collections.emptyList();
        }

        // Add type guard code action
        String commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, varName.get());
        Range range = PositionUtil.toRange(matchedNode.lineRange());
        List<TextEdit> edits = CodeActionUtil.getTypeGuardCodeActionEdits(varName.get(), range, varTypeSymbol, context);
        if (edits.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.Source));
    }

    @Override
    public String getName() {
        return NAME;
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

    protected Optional<Pair<UnionTypeSymbol, TypeDescriptorNode>> getVarTypeSymbolAndTypeNode(
            Node matchedNode, RangeBasedPositionDetails posDetails,
            CodeActionContext context) {
        TypeSymbol varTypeSymbol;
        TypeDescriptorNode varTypeNode;
        switch (matchedNode.kind()) {
            case LOCAL_VAR_DECL:
                varTypeSymbol = posDetails.matchedTopLevelTypeSymbol();
                varTypeNode = ((VariableDeclarationNode) matchedNode).typedBindingPattern().typeDescriptor();
                break;
            case ASSIGNMENT_STATEMENT:
                Optional<VariableSymbol> optVariableSymbol = getVariableSymbol(context, matchedNode);
                if (optVariableSymbol.isEmpty()) {
                    return Optional.empty();
                }
                varTypeSymbol = optVariableSymbol.get().typeDescriptor();
                SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
                Optional<NonTerminalNode> node = CommonUtil.findNode(optVariableSymbol.get(), syntaxTree);
                if (node.isPresent() && node.get().kind() == SyntaxKind.TYPED_BINDING_PATTERN) {
                    varTypeNode = ((TypedBindingPatternNode) node.get()).typeDescriptor();
                } else {
                    return Optional.empty();
                }
                break;
            default:
                return Optional.empty();
        }
        if (varTypeSymbol == null || varTypeSymbol.typeKind() != TypeDescKind.UNION) {
            return Optional.empty();
        }
        return Optional.of(new ImmutablePair<>((UnionTypeSymbol) varTypeSymbol, varTypeNode));
    }

    private Optional<VariableSymbol> getVariableSymbol(CodeActionContext context, Node matchedNode) {
        AssignmentStatementNode assignmentStmtNode = (AssignmentStatementNode) matchedNode;
        SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
        Document srcFile = context.currentDocument().orElseThrow();
        Optional<Symbol> symbol = semanticModel.symbol(srcFile,
                assignmentStmtNode.varRef().lineRange().startLine());
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        return Optional.of((VariableSymbol) symbol.get());
    }

    private boolean isInValidContext(NonTerminalNode matchedNode, CodeActionContext context) {

        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return false;
        }
        Node node = null;
        if (matchedNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            node = ((AssignmentStatementNode) matchedNode).varRef();
        } else if (matchedNode.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            node = ((VariableDeclarationNode) matchedNode).typedBindingPattern().bindingPattern();
        }
        return node != null && PositionUtil.isWithInRange(node, context.cursorPositionInTree());
    }

}
