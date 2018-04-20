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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Cursor position resolver for the record scope.
 */
public class RecordScopeResolver extends CursorPositionResolver {
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
        Node recordNode = treeVisitor.getBlockOwnerStack().peek();
        if (!recordNode.getKind().equals(NodeKind.RECORD)) {
            return false;
        }
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition((DiagnosticPos) node.getPosition());
        DiagnosticPos ownerPos = CommonUtil.toZeroBasedPosition((DiagnosticPos) recordNode.getPosition());
        int ownerEndLine = ownerPos.getEndLine();
        int ownerEndCol = ownerPos.getEndColumn();
        int nodeStartLine = nodePos.getStartLine();
        int nodeStartCol = nodePos.getStartColumn();
        BLangRecord bLangRecord = (BLangRecord) recordNode;
        List<BLangVariable> fields = bLangRecord.fields;
        boolean isLastField = fields.indexOf(node) == fields.size() - 1;
        boolean isCursorBefore = ((nodeStartLine > line) || (nodeStartLine == line && nodeStartCol > col)) ||
                (isLastField && ((line < ownerEndLine)
                        || (line == ownerEndLine && col < ownerEndCol)));
        
        if (isCursorBefore) {
            treeVisitor.setTerminateVisitor(true);
            SymbolEnv recordEnv = createRecordEnv((BLangRecord) recordNode, treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(recordEnv), recordEnv);
        }
        
        return isCursorBefore;
    }

    private static SymbolEnv createRecordEnv(BLangRecord record, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(record, env.scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }
}
