/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link TemplateExpressionNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TemplateExpressionNodeContext extends AbstractCompletionProvider<TemplateExpressionNode> {

    public TemplateExpressionNodeContext() {
        super(TemplateExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TemplateExpressionNode node)
            throws LSCompletionException {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        List<LSCompletionItem> completionItems = new ArrayList<>();

        Optional<InterpolationNode> interpolationNode = findInterpolationNode(nodeAtCursor, node);
        if (interpolationNode.isPresent()) {
            // If the node at cursor is an interpolation, show expression suggestions
            if (this.isWithinInterpolation(context, node)) {
                completionItems.addAll(this.expressionCompletions(context));
            }
        }

        this.sort(context, node, completionItems);

        return completionItems;
    }

    /**
     * Finds an {@link InterpolationNode} which is/is a parent of the cursor node.
     *
     * @param cursorNode             Node at cursor
     * @param node Template expression node
     * @return Optional interpolation node
     */
    private Optional<InterpolationNode> findInterpolationNode(NonTerminalNode cursorNode, TemplateExpressionNode node) {
        // We know that the template expression node is definitely a parent of the node at the cursor
        while (cursorNode.kind() != node.kind()) {
            if (cursorNode.kind() == SyntaxKind.INTERPOLATION) {
                return Optional.of((InterpolationNode) cursorNode);
            }

            cursorNode = cursorNode.parent();
        }

        return Optional.empty();
    }
    
    private boolean isWithinInterpolation(BallerinaCompletionContext context, TemplateExpressionNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Optional<InterpolationNode> interpolationNode = this.findInterpolationNode(nodeAtCursor, node);
        int cursor = context.getCursorPositionInTree();
        // Check if cursor is within the interpolation start and end tokens. Ex: 
        // 1. `some text ${..<cursor>..} other text`
        if (interpolationNode.isEmpty()) {
            return false;
        }
        Token startToken = interpolationNode.get().interpolationStartToken();
        Token endToken = interpolationNode.get().interpolationEndToken();
        return !startToken.isMissing() && startToken.textRange().endOffset() <= cursor
                && (endToken.isMissing() || cursor <= endToken.textRange().startOffset());
    }
}
