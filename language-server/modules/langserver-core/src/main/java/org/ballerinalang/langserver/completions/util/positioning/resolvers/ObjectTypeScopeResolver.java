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

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.Map;

/**
 * Cursor position resolver for the object type.
 */
public class ObjectTypeScopeResolver extends CursorPositionResolver {
    /**
     * Check whether the cursor is positioned before the given node start.
     *
     * @param nodePosition      Position of the node
     * @param node              Node
     * @param treeVisitor       {@link TreeVisitor} current tree visitor instance
     * @param completionContext Completion operation context
     * @return {@link Boolean}      Whether the cursor is before the node start or not
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, BLangNode node, TreeVisitor treeVisitor,
                                      LSServiceOperationContext completionContext) {
        if (!(treeVisitor.getBlockOwnerStack().peek() instanceof BLangObject)) {
            return false;
        }
        BLangObject ownerObject = (BLangObject) treeVisitor.getBlockOwnerStack().peek();
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(nodePosition);
        DiagnosticPos blockOwnerPos = CommonUtil
                .toZeroBasedPosition((DiagnosticPos) treeVisitor.getBlockOwnerStack().peek().getPosition());
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        boolean isLastField = false;
        
        
        if ((!ownerObject.fields.isEmpty() && node instanceof BLangVariable
                && ownerObject.fields.indexOf(node) == ownerObject.fields.size() - 1
                && ownerObject.functions.isEmpty())
                || (!ownerObject.functions.isEmpty() && node instanceof BLangFunction 
                && ownerObject.functions.indexOf(node) == ownerObject.functions.size() - 1)) {
            isLastField = true;
        }
        
        if ((line < zeroBasedPos.getStartLine()
                || (line == zeroBasedPos.getStartLine() && col < zeroBasedPos.getStartColumn()))
                || (isLastField && ((blockOwnerPos.getEndLine() > line && zeroBasedPos.getEndLine() < line)
                || (blockOwnerPos.getEndLine() == line && blockOwnerPos.getEndColumn() > col)))) {
            
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, null);
            treeVisitor.setTerminateVisitor(true);
            treeVisitor.setNextNode(node);
            return true;
        }
        
        return false;
    }
}
