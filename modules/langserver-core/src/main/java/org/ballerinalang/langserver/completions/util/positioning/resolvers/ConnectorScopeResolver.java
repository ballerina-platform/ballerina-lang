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

import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;

import java.util.List;

/**
 * Connector definition scope position resolver.
 */
public class ConnectorScopeResolver extends ServiceScopeResolver {

    /**
     * Check whether the given node is within the scope and located after the last child node.
     * @param node          Current Node to evaluate
     * @param treeVisitor   Operation Tree Visitor
     * @param curLine       line of the cursor                     
     * @param curCol        column of the cursor                     
     * @return              {@link Boolean} whether the last child node or not
     */
    protected boolean isWithinScopeAfterLastChildNode(Node node, TreeVisitor treeVisitor, int curLine, int curCol) {
        BLangConnector bLangConnector = (BLangConnector) treeVisitor.getBlockOwnerStack().peek();
        List<BLangAction> actions = bLangConnector.actions;
        List<BLangVariableDef> variableDefs = bLangConnector.varDefs;
        int connectorEndLine = bLangConnector.pos.getEndLine();
        int connectorEndCol = bLangConnector.pos.getEndColumn();
        int nodeEndLine = node.getPosition().getEndLine();
        int nodeEndCol = node.getPosition().getEndColumn();
        boolean isLastChildNode;

        if (actions.isEmpty()) {
            isLastChildNode = variableDefs.indexOf(node) == (variableDefs.size() - 1);
        } else {
            isLastChildNode = actions.indexOf(node) == (actions.size() - 1);
        }

        return (isLastChildNode
                && (curLine < connectorEndLine || (curLine == connectorEndLine && curCol < connectorEndCol))
                && (nodeEndLine < curLine || (nodeEndLine == curLine && nodeEndCol < curCol)));
    }
}
