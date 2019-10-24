/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Cursor position resolver for the record literal scope.
 * 
 * @since 1.0
 */
public class RecordLiteralScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node) {
        Node recordNode = treeVisitor.getBlockOwnerStack().peek();
        if (!(recordNode instanceof BLangRecordLiteral)) {
            return false;
        }
        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) recordNode;
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(nodePosition);
        DiagnosticPos ownerPos = CommonUtil.toZeroBasedPosition(recordLiteral.getPosition());
        int ownerEndLine = ownerPos.getEndLine();
        int ownerEndCol = ownerPos.getEndColumn();
        int nodeStartLine = nodePos.getStartLine();
        int nodeStartCol = nodePos.getStartColumn();
        int nodeEndCol = nodePos.getEndColumn();
        int nodeEndLine = nodePos.getEndLine();
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValuePairs = recordLiteral.keyValuePairs;
        boolean isLastField = keyValuePairs.indexOf(node) == keyValuePairs.size() - 1;
        boolean isCursorBefore = ((nodeStartLine > line) || (nodeStartLine == line && col < nodeStartCol)) ||
                (isLastField && ((line < ownerEndLine && (line > nodeEndLine
                        || (line == nodeEndLine && col > nodeEndCol))) || (line == ownerEndLine && col < ownerEndCol)));
        
        if (isCursorBefore) {
            treeVisitor.forceTerminateVisitor();
            SymbolEnv recordEnv = createRecordLiteralEnv(recordLiteral, treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(treeVisitor.resolveAllVisibleSymbols(recordEnv), recordEnv);
        }
        
        return isCursorBefore;
    }

    private static SymbolEnv createRecordLiteralEnv(BLangRecordLiteral record, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(record, env.scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }
}
