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

import org.ballerinalang.langserver.common.constants.DocumentServiceKeys;
import org.ballerinalang.langserver.common.context.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionTreeVisitor;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import static org.ballerinalang.langserver.common.utils.CommonUtil.toZeroBasedPosition;

/**
 * Position resolver for the top level nodes.
 */
public class TopLevelNodeScopeResolver extends CursorPositionResolver {
    /**
     * Check whether the cursor is positioned before the given node start.
     *
     * @param nodePosition      Position of the node
     * @param node              Node
     * @param completionTreeVisitor       {@link CompletionTreeVisitor} current tree visitor instance
     * @param completionContext Completion operation context
     * @return {@link Boolean}      Whether the cursor is before the node start or not
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos nodePosition, Node node,
                                      CompletionTreeVisitor completionTreeVisitor,
                                      TextDocumentServiceContext completionContext) {
        int line = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int col = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        DiagnosticPos zeroBasedPos = toZeroBasedPosition(nodePosition);
        int nodeSLine = zeroBasedPos.sLine;
        int nodeSCol = zeroBasedPos.sCol;

        if (line < nodeSLine || (line == nodeSLine && col <= nodeSCol)) {
            completionTreeVisitor.setTerminateVisitor(true);
            return true;
        }

        return false;
    }
}
