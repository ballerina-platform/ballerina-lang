/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Node visitor for the extract to function code action. This will determine,
 * 1. Symbols which are referenced as an assignment inside the selected range
 * 2. Symbols which are declared inside the selected range
 * 3. Whether the selected range of a particular code action context is extractable to a function
 * 4. Nodes which are inside the selected range that should be extracted(fully selected nodes)
 *
 * @since 2201.2.1
 */
public class ExtractToFunctionStatementAnalyzer extends NodeVisitor {
    private final List<Symbol> updatedSymbols = new ArrayList<>();
    private final List<Symbol> declaredVariableSymbols = new ArrayList<>();
    private final List<Node> selectedNodes = new ArrayList<>();
    private boolean isExtractable = true;
    private final Range selectedRange;
    private final SemanticModel semanticModel;

    // Set of syntax kinds of nodes which we do not allow to have inside the extracted function
    private final Set<SyntaxKind> notSupportedSyntaxKindsWithinRange = Set.of(SyntaxKind.BREAK_STATEMENT,
            SyntaxKind.CONTINUE_STATEMENT, SyntaxKind.PANIC_STATEMENT, SyntaxKind.NAMED_WORKER_DECLARATION,
            SyntaxKind.FORK_STATEMENT, SyntaxKind.TRANSACTION_STATEMENT, SyntaxKind.ROLLBACK_STATEMENT,
            SyntaxKind.RETRY_STATEMENT, SyntaxKind.XML_NAMESPACE_DECLARATION, SyntaxKind.REMOTE_METHOD_CALL_ACTION,
            SyntaxKind.BRACED_ACTION, SyntaxKind.CHECK_ACTION, SyntaxKind.START_ACTION, SyntaxKind.TRAP_ACTION,
            SyntaxKind.FLUSH_ACTION, SyntaxKind.ASYNC_SEND_ACTION, SyntaxKind.SYNC_SEND_ACTION,
            SyntaxKind.RECEIVE_ACTION, SyntaxKind.WAIT_ACTION, SyntaxKind.QUERY_ACTION, SyntaxKind.COMMIT_ACTION,
            SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION);

    public ExtractToFunctionStatementAnalyzer(Range selectedRange, SemanticModel semanticModel) {
        this.selectedRange = selectedRange;
        this.semanticModel = semanticModel;
    }

    public void analyze(NonTerminalNode node) {
        if (node.kind() == SyntaxKind.LIST) {
            node.children().forEach(child -> {
                /*
                 * When the matched code action node is a LIST node it contains all the child nodes which are not even
                 * selected(highlighted), this check is added to find and ignore such child nodes.
                 */
                if (PositionUtil.isRangeWithinRange(PositionUtil.toRange(child.lineRange()), selectedRange)) {
                    selectedNodes.add(child);
                    child.accept(this);
                }
            });
        } else {
            /*
             * Code action is provided only when the matched code action node is fully within the selected range.
             */
            if (!PositionUtil.isRangeWithinRange(PositionUtil.toRange(node.lineRange()), selectedRange)) {
                this.isExtractable = false;
                return;
            }
            selectedNodes.add(node);
            node.accept(this);
        }

        if (selectedNodes.isEmpty()) {
            this.isExtractable = false;
        }
    }

    /**
     * The list of symbols which are getting updated(assigned) inside the selected range.
     */
    public List<Symbol> getUpdatedSymbols() {
        return updatedSymbols;
    }

    /**
     * The list of symbols which are getting declared inside the range.
     */
    public List<Symbol> getDeclaredVariableSymbols() {
        return declaredVariableSymbols;
    }

    /**
     * The list of symbols which are fully selected by the user(partially selected nodes are not considered).
     */
    public List<Node> getSelectedNodes() {
        return selectedNodes;
    }

    /**
     * Checks whether the content of the selected range is syntactically correct to be extracted to a function.
     */
    public boolean isExtractable() {
        return isExtractable;
    }

    @Override
    public void visit(IfElseStatementNode node) {
        if (node.parent().kind() == SyntaxKind.ELSE_BLOCK) {
            // this is when selected if-else-stmt is inside another if-else-stmt
            this.isExtractable = false;
        }
        super.visit(node);
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        Optional<Symbol> symbol = semanticModel.symbol(node.varRef());
        if (symbol.isPresent() && !updatedSymbols.contains(symbol.get())) {
            this.updatedSymbols.add((symbol.get()));
        }
    }

    @Override
    public void visit(CompoundAssignmentStatementNode node) {
        Optional<Symbol> symbol = semanticModel.symbol(node.lhsExpression());
        if (symbol.isPresent() && !updatedSymbols.contains(symbol.get())) {
            this.updatedSymbols.add((symbol.get()));
        }
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        Optional<Symbol> symbol = semanticModel.symbol(node.typedBindingPattern().bindingPattern());
        symbol.ifPresent(declaredVariableSymbols::add);
        super.visit(node);
    }

    @Override
    public void visit(ReturnStatementNode node) {
        if (node.expression().isPresent() && node.parent().kind() == SyntaxKind.FUNCTION_BODY_BLOCK) {
            super.visit(node);
        } else {
            this.isExtractable = false;
        }
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (notSupportedSyntaxKindsWithinRange.contains(node.kind())) {
            this.isExtractable = false;
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }
}
