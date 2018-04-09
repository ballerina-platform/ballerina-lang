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

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;

/**
 * Service scope position resolver.
 */
public class ServiceScopeResolver extends CursorPositionResolver {
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, BLangNode node, TreeVisitor treeVisitor,
                                      LSServiceOperationContext completionContext) {
        Position position = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        DiagnosticPos zeroBasedPo = CommonUtil.toZeroBasedPosition(nodePosition);
        int line = position.getLine();
        int col = position.getCharacter();
        int nodeSLine = zeroBasedPo.sLine;
        int nodeSCol = zeroBasedPo.sCol;

        if (line < nodeSLine
                || (line == nodeSLine && col < nodeSCol)
                || this.isWithinScopeAfterLastChildNode(node, treeVisitor, line, col)) {
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
     * @param node          Current Node to evaluate
     * @param treeVisitor   Operation Tree Visitor
     * @param curLine       line of the cursor                     
     * @param curCol        column of the cursor                     
     * @return              {@link Boolean} whether the last child node or not
     */
    protected boolean isWithinScopeAfterLastChildNode(Node node, TreeVisitor treeVisitor, int curLine, int curCol) {
        BLangService bLangService = (BLangService) treeVisitor.getBlockOwnerStack().peek();
        List<BLangResource> resources = bLangService.resources;
        List<BLangVariableDef> variableDefs = bLangService.vars;
        int serviceEndLine = bLangService.pos.getEndLine();
        int serviceEndCol = bLangService.pos.getEndColumn();
        int nodeEndLine = node.getPosition().getEndLine();
        int nodeEndCol = node.getPosition().getEndColumn();
        boolean isLastChildNode;

        if (resources.isEmpty()) {
            isLastChildNode = variableDefs.indexOf(node) == (variableDefs.size() - 1);
        } else {
            isLastChildNode = resources.indexOf(node) == (resources.size() - 1);
        }

        boolean isWithinScope =  (isLastChildNode
                && (curLine < serviceEndLine || (curLine == serviceEndLine && curCol < serviceEndCol))
                && (nodeEndLine < curLine || (nodeEndLine == curLine && nodeEndCol < curCol)));
        
        if (isWithinScope) {
            treeVisitor.setPreviousNode((BLangNode) node);
        }
        
        return isWithinScope;
    }
}
