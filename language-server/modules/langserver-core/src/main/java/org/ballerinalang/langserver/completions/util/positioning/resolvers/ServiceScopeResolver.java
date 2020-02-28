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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service scope position resolver.
 */
public class ServiceScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, TreeVisitor treeVisitor, LSContext completionContext,
                                      BLangNode node, BSymbol bSymbol) {
        Position position = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        DiagnosticPos zeroBasedPo = CommonUtil.toZeroBasedPosition(nodePosition);
        int line = position.getLine();
        int col = position.getCharacter();
        int nodeSLine = zeroBasedPo.sLine;
        int nodeSCol = zeroBasedPo.sCol;

        if (line < nodeSLine
                || (line == nodeSLine && col <= nodeSCol)
                || this.isWithinScopeAfterLastChildNode(node, treeVisitor, line, col)) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries =
                    treeVisitor.resolveAllVisibleSymbols(treeVisitor.getSymbolEnv());
            treeVisitor.populateSymbols(visibleSymbolEntries, treeVisitor.getSymbolEnv());
            treeVisitor.setNextNode(bSymbol, node);
            treeVisitor.forceTerminateVisitor();
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
    private boolean isWithinScopeAfterLastChildNode(Node node, TreeVisitor treeVisitor, int curLine, int curCol) {
        BLangObjectTypeNode bLangService = (BLangObjectTypeNode) treeVisitor.getBlockOwnerStack().peek();
        DiagnosticPos serviceNodePos = CommonUtil.toZeroBasedPosition(bLangService.getPosition());
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition((DiagnosticPos) node.getPosition());
        List<BLangFunction> serviceFunctions = bLangService.getFunctions();
        List<BLangSimpleVariable> serviceFields = bLangService.getFields().stream()
                .map(simpleVar -> (BLangSimpleVariable) simpleVar)
                .collect(Collectors.toList());
        List<BLangNode> serviceContent = new ArrayList<>(serviceFunctions);
        serviceContent.addAll(serviceFields);
        serviceContent.sort(new CommonUtil.BLangNodeComparator());
        
        int serviceEndLine = serviceNodePos.getEndLine();
        int serviceEndCol = serviceNodePos.getEndColumn();
        int nodeEndLine = nodePos.getEndLine();
        int nodeEndCol = nodePos.getEndColumn();
        boolean isLastChildNode;

        isLastChildNode = !serviceContent.isEmpty() && serviceContent.indexOf(node) == (serviceContent.size() - 1);

        return (isLastChildNode
                && (curLine < serviceEndLine || (curLine == serviceEndLine && curCol < serviceEndCol))
                && (nodeEndLine < curLine || (nodeEndLine == curLine && nodeEndCol < curCol)));
    }
}
