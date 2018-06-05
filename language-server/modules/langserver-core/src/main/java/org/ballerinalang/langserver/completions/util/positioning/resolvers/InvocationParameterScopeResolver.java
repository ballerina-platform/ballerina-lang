/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;

/**
 * Invocation parameter scope position resolver.
 */
public class InvocationParameterScopeResolver extends CursorPositionResolver {

    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, BLangNode node, TreeVisitor treeVisitor,
                                      LSServiceOperationContext completionContext) {
        Position position = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int line = position.getLine();
        int col = position.getCharacter();
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(node.getPosition());
        int nodeEndLine = zeroBasedPos.eLine;
        int nodeEndCol = zeroBasedPos.eCol;

        if (this.isWithinScopeAfterLastParameterNode(node, treeVisitor, line, col, nodeEndLine, nodeEndCol)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, null);
            treeVisitor.setNextNode(node);
            treeVisitor.setTerminateVisitor(true);
            return true;
        }

        return false;
    }

    /**
     * Check whether the given node is within the scope and located after the last child node.
     *
     * @param node        Current Node to evaluate
     * @param treeVisitor Operation Tree Visitor
     * @param curLine     line of the cursor
     * @param curCol      column of the cursor
     * @param nodeEndLine
     * @param nodeEndCol  @return {@link Boolean} whether the last child node or not
     */
    private boolean isWithinScopeAfterLastParameterNode(Node node, TreeVisitor treeVisitor, int curLine, int curCol,
                                                        int nodeEndLine, int nodeEndCol) {
        BLangInvocation bLangInvocation = (BLangInvocation) treeVisitor.getBlockOwnerStack().peek();
        List<? extends ExpressionNode> argumentExpressions = bLangInvocation.getArgumentExpressions();
        int invocationEndLine = bLangInvocation.pos.getEndLine();
        int invocationEndCol = bLangInvocation.pos.getEndColumn();

        boolean isLastChildNode = argumentExpressions.indexOf(node) == (argumentExpressions.size() - 1);

        boolean isWithinScope = (isLastChildNode &&
                (curLine < invocationEndLine || (curLine == invocationEndLine && curCol <= invocationEndCol)) &&
                (curLine > nodeEndLine || (curLine == nodeEndLine && curCol > nodeEndCol)));

        if (isWithinScope) {
            treeVisitor.setPreviousNode((BLangNode) node);
        }

        return isWithinScope;
    }
}
