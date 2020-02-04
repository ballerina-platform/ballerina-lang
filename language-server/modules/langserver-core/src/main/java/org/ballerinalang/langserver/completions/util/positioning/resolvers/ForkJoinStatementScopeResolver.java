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
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;

/**
 * Fork Join scope position resolver.
 * 
 * @since 1.0
 */
public class ForkJoinStatementScopeResolver extends CursorPositionResolver {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node, BSymbol bSymbol) {
        if (treeVisitor.getForkJoinStack().isEmpty()) {
            return false;
        }
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(nodePosition);
        int nodeSLine = zeroBasedPos.sLine;
        int nodeSCol = zeroBasedPos.sCol;
        int nodeELine = zeroBasedPos.eLine;
        int nodeECol = zeroBasedPos.eCol;
        BLangForkJoin parent = treeVisitor.getForkJoinStack().peek();

        boolean isLastStatement = this.isNodeLastStatement(parent, node);
        boolean isWithinScopeAfterLastChild = this.isWithinScopeAfterLastChildNode(parent, isLastStatement, nodeELine,
                nodeECol, line, col);
        if (line < nodeSLine || (line == nodeSLine && col <= nodeSCol) || isWithinScopeAfterLastChild) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, treeVisitor.getSymbolEnv());
            treeVisitor.forceTerminateVisitor();
            treeVisitor.setNextNode(bSymbol, node);
            return true;
        }

        return false;
    }

    private boolean isWithinScopeAfterLastChildNode(BLangForkJoin parent, boolean lastChild, int nodeELine,
                                                    int nodeECol, int line, int col) {
        if (!lastChild) {
            return false;
        } else {
            DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(parent.pos);
            int blockOwnerELine = diagnosticPos.eLine;
            int blockOwnerECol = diagnosticPos.eCol;

            return (line < blockOwnerELine || (line == blockOwnerELine && col <= blockOwnerECol)) &&
                    (line > nodeELine || (line == nodeELine && col > nodeECol));
        }
    }

    private boolean isNodeLastStatement(BLangForkJoin forkJoin, Node node) {
        return !forkJoin.workers.isEmpty() && forkJoin.workers.indexOf(node) == forkJoin.workers.size() - 1;
    }
}
