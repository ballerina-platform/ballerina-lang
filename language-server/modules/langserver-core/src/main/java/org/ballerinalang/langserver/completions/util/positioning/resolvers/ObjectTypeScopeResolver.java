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
import org.ballerinalang.langserver.completions.util.CompletionVisitorUtil;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;

/**
 * Cursor position resolver for the object type.
 */
public class ObjectTypeScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node, BSymbol bSymbol) {
        if (!(treeVisitor.getBlockOwnerStack().peek() instanceof BLangObjectTypeNode)) {
            return false;
        }
        Node blockOwner = treeVisitor.getBlockOwnerStack().peek();
        if (blockOwner == null) {
            return false;
        }
        BLangObjectTypeNode ownerObject = (BLangObjectTypeNode) treeVisitor.getBlockOwnerStack().peek();
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(nodePosition);
        DiagnosticPos blockOwnerPos = CommonUtil.toZeroBasedPosition(
                ((BLangObjectTypeNode) blockOwner).parent.getPosition());
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        BLangNode lastItem = CommonUtil.getLastItem(CompletionVisitorUtil.getObjectItemsOrdered(ownerObject));
        boolean isLastItem = (lastItem == node);

        if (line < zeroBasedPos.getStartLine()
                || (line == zeroBasedPos.getStartLine() && col < zeroBasedPos.getStartColumn())
                || (isLastItem && ((blockOwnerPos.getEndLine() > line && zeroBasedPos.getEndLine() < line)
                || (blockOwnerPos.getEndLine() == line && blockOwnerPos.getEndColumn() > col)))) {
            
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, treeVisitor.getSymbolEnv());
            treeVisitor.forceTerminateVisitor();
            treeVisitor.setNextNode(bSymbol, node);
            return true;
        }
        
        return false;
    }
}
