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
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Cursor position resolver for the record scope.
 */
public class RecordScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node, BSymbol bSymbol) {
        Node recordNode = treeVisitor.getBlockOwnerStack().peek();
        if (!recordNode.getKind().equals(NodeKind.RECORD_TYPE)) {
            return false;
        }
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(node.getPosition());
        DiagnosticPos ownerPos = CommonUtil
                .toZeroBasedPosition(((BLangRecordTypeNode) recordNode).parent.getPosition());
        int ownerEndLine = ownerPos.getEndLine();
        int nodeStartLine = nodePos.getStartLine();
        int nodeEndLine = nodePos.getEndLine();
        int nodeStartCol = nodePos.getStartColumn();
        BLangRecordTypeNode bLangRecord = (BLangRecordTypeNode) recordNode;
        List<BLangSimpleVariable> fields = bLangRecord.fields;
        boolean isLastField = fields.indexOf(node) == fields.size() - 1;
        boolean isCursorBefore = ((nodeStartLine > line) || (nodeStartLine == line && nodeStartCol > col)) ||
                (isLastField && isCursorWithinScopeAfterLastChild(line, ownerEndLine, nodeEndLine));
        
        if (isCursorBefore) {
            treeVisitor.forceTerminateVisitor();
            SymbolEnv recordEnv = createRecordEnv((BLangRecordTypeNode) recordNode, treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(recordEnv), recordEnv);
        }
        
        return isCursorBefore;
    }

    private static SymbolEnv createRecordEnv(BLangRecordTypeNode record, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(record, env.scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }
    
    private boolean isCursorWithinScopeAfterLastChild(int cLine, int ownerELine, int nodeELine) {
        return cLine > nodeELine && cLine < ownerELine;
    }
}
