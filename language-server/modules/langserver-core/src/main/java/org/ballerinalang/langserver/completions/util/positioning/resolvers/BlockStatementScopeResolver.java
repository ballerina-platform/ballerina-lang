/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Block statement scope position resolver.
 */
public class BlockStatementScopeResolver extends CursorPositionResolver {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node, BSymbol bSymbol) {
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(nodePosition);
        int nodeSLine = zeroBasedPos.sLine;
        int nodeSCol = zeroBasedPos.sCol;
        // node endLine for the BLangIf node has to calculate by considering the else node. End line of the BLangIf
        // node is the endLine of the else node.
        int nodeELine = node instanceof BLangIf ? getIfElseNodeEndLine((BLangIf) node) : zeroBasedPos.eLine;
        int nodeECol = zeroBasedPos.eCol;

        BLangBlockStmt bLangBlockStmt = treeVisitor.getBlockStmtStack().peek();
        Node blockOwner = treeVisitor.getBlockOwnerStack().peek();

        boolean isLastStatement = this.isNodeLastStatement(bLangBlockStmt, blockOwner, node);
        boolean isWithinScopeAfterLastChild = this.isWithinScopeAfterLastChildNode(treeVisitor, isLastStatement,
                nodeELine, nodeECol, line, col, node);

        if (line < nodeSLine || (line == nodeSLine && col <= nodeSCol) || isWithinScopeAfterLastChild) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, treeVisitor.getSymbolEnv());
            treeVisitor.forceTerminateVisitor();
            treeVisitor.setNextNode(bSymbol);
            return true;
        }

        return false;
    }

    private boolean isWithinScopeAfterLastChildNode(TreeVisitor treeVisitor, boolean lastChild,
                                                    int nodeELine, int nodeECol, int line, int col, Node node) {
        if (!lastChild) {
            return false;
        } else {
            BLangBlockStmt bLangBlockStmt = treeVisitor.getBlockStmtStack().peek();
            Node blockOwner = treeVisitor.getBlockOwnerStack().peek();
            int blockOwnerELine = this.getBlockOwnerELine(blockOwner, bLangBlockStmt);
            int blockOwnerECol = this.getBlockOwnerECol(blockOwner, bLangBlockStmt);
            boolean isWithinScope = (line < blockOwnerELine || (line == blockOwnerELine && col <= blockOwnerECol)) &&
                    (line > nodeELine || (line == nodeELine && col > nodeECol));

            if (isWithinScope) {
                treeVisitor.setPreviousNode((BLangNode) node);
            }

            return isWithinScope;
        }
    }

    private boolean isNodeLastStatement(BLangBlockStmt bLangBlockStmt, Node blockOwner, Node node) {
        Node parentNode = this.getParentNode(node);
        if (bLangBlockStmt != null) {
            List<BLangStatement> statements = bLangBlockStmt.stmts.stream()
                    .filter(bLangStatement -> !CommonUtil.isWorkerDereivative(bLangStatement))
                    .collect(Collectors.toList());
            statements.sort(new CommonUtil.BLangNodeComparator());
            return (statements.indexOf(parentNode) == (statements.size() - 1));
        } else if (blockOwner instanceof BLangTypeDefinition
                && ((BLangTypeDefinition) blockOwner).typeNode instanceof BLangObjectTypeNode) {
            List<BLangSimpleVariable> structFields = (List<BLangSimpleVariable>)
                    ((BLangObjectTypeNode) ((BLangTypeDefinition) blockOwner).typeNode).getFields();
            return (structFields.indexOf(parentNode) == structFields.size() - 1);
        } else {
            return false;
        }
    }

    /**
     * When given a statement/ node then find the parent.
     * As an example for an else-if statement, the root is the parent if statement when consider the positioning
     * 
     * @param node              Node to find the parent
     * @return {@link Node}     Parent of the node
     */
    private Node getParentNode(Node node) {
        Node tempNode = node;
        while (tempNode != null && !(((BLangNode) tempNode).parent instanceof BLangBlockStmt)) {
            tempNode = ((BLangNode) tempNode).parent;
        }

        return tempNode;
    }

    private int getBlockOwnerELine(Node blockOwner, BLangBlockStmt bLangBlockStmt) {
        if (blockOwner == null) {
            // When the else node is evaluating, block owner is null and the block statement only present
            // This is because, else node is represented with a blocks statement only
            return CommonUtil.toZeroBasedPosition(bLangBlockStmt.getPosition()).getEndLine();
        } else if (blockOwner instanceof BLangTransaction) {
            return this.getTransactionBlockComponentEndLine((BLangTransaction) blockOwner, bLangBlockStmt);
        } else {
            return CommonUtil.toZeroBasedPosition((DiagnosticPos) blockOwner.getPosition()).getEndLine();
        }
    }

    private int getBlockOwnerECol(Node blockOwner, BLangBlockStmt bLangBlockStmt) {
        if (blockOwner == null) {
            // When the else node is evaluating, block owner is null and the block statement only present
            // This is because, else node is represented with a blocks statement only
            return CommonUtil.toZeroBasedPosition(bLangBlockStmt.getPosition()).getEndColumn();
        } else {
            return CommonUtil.toZeroBasedPosition((DiagnosticPos) blockOwner.getPosition()).getEndColumn();
        }
    }

    private int getTransactionBlockComponentEndLine(BLangTransaction bLangTransaction, BLangBlockStmt bLangBlockStmt) {
        BLangBlockStmt transactionBody = bLangTransaction.transactionBody;
        BLangBlockStmt failedBody = bLangTransaction.onRetryBody;

        List<BLangBlockStmt> components = new ArrayList<>();
        components.add(transactionBody);
        components.add(failedBody);

        components.sort(Comparator.comparing(component -> {
            if (component != null) {
                return CommonUtil.toZeroBasedPosition(component.getPosition()).getEndLine();
            } else {
                return -1;
            }
        }));

        int blockStmtIndex = components.indexOf(bLangBlockStmt);
        if (blockStmtIndex == components.size() - 1) {
            return CommonUtil.toZeroBasedPosition(bLangTransaction.getPosition()).eLine;
        } else if (components.get(blockStmtIndex + 1) != null) {
            return CommonUtil.toZeroBasedPosition(components.get(blockStmtIndex + 1).getPosition()).sLine;
        } else {
            // Ideally should not invoke this
            return -1;
        }
    }

    /**
     * Calculate the end line of the BLangIf node.
     *
     * @param bLangIf {@link BLangIf}
     * @return end line of the if node
     */
    private int getIfElseNodeEndLine(BLangIf bLangIf) {
        BLangIf ifNode = bLangIf;
        while (true) {
            if (ifNode.elseStmt == null) {
                return CommonUtil.toZeroBasedPosition(ifNode.getPosition()).eLine;
            } else if (ifNode.elseStmt instanceof BLangIf) {
                ifNode = (BLangIf) ifNode.elseStmt;
            } else {
                return CommonUtil.toZeroBasedPosition(ifNode.elseStmt.getPosition()).getEndLine();
            }
        }
    }
}
