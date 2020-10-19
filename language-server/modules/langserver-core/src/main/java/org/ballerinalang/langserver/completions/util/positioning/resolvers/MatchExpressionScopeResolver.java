/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;
import java.util.Map;

/**
 * Cursor position resolver for the match expression scope.
 */
public class MatchExpressionScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(BLangDiagnosticLocation nodePosition, TreeVisitor treeVisitor,
                                      LSContext completionContext, BLangNode node, BSymbol bSymbol) {
        if (!(treeVisitor.getBlockOwnerStack().peek() instanceof BLangMatchExpression)) {
            // In the ideal case, this will not get triggered
            return false;
        }
        BLangMatchExpression matchNode = (BLangMatchExpression) treeVisitor.getBlockOwnerStack().peek();
        BLangDiagnosticLocation matchNodePos = CommonUtil.toZeroBasedPosition(matchNode.getPosition());
        BLangDiagnosticLocation nodePos = CommonUtil.toZeroBasedPosition(node.getPosition());
        List<BLangMatchExpression.BLangMatchExprPatternClause> patternClauseList = matchNode.getPatternClauses();
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        int nodeLine = nodePos.lineRange().startLine().line();
        int nodeCol = nodePos.lineRange().startLine().offset();
        boolean isBeforeNode = false;
        
        if ((line < nodeLine) || (line == nodeLine && col < nodeCol)) {
            isBeforeNode = true;
        } else if (patternClauseList.indexOf(node) == patternClauseList.size() - 1) {
            isBeforeNode = (line < matchNodePos.lineRange().endLine().line())
                    || (line == matchNodePos.lineRange().endLine().line()
                    && col < matchNodePos.lineRange().endLine().offset());
        }
        
        if (isBeforeNode) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            SymbolEnv matchEnv = createMatchExpressionEnv(matchNode, treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, matchEnv);
            treeVisitor.forceTerminateVisitor();
            treeVisitor.setNextNode(bSymbol, node);
        }
        
        return isBeforeNode;
    }

    private static SymbolEnv createMatchExpressionEnv(BLangMatchExpression matchExpression, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(matchExpression, null);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }
}
