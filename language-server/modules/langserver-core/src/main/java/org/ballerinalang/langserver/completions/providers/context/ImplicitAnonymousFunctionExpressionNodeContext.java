/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode} context.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImplicitAnonymousFunctionExpressionNodeContext
        extends NodeWithRHSInitializerProvider<ImplicitAnonymousFunctionExpressionNode> {

    public ImplicitAnonymousFunctionExpressionNodeContext() {
        super(ImplicitAnonymousFunctionExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ImplicitAnonymousFunctionExpressionNode node)
            throws LSCompletionException {
        if (withinExpression(node, context)) {
            List<LSCompletionItem> completionItems = this.initializerContextCompletions(context, node.expression());
            this.sort(context, node, completionItems);
            return completionItems;
        }
        return Collections.emptyList();
    }

    private boolean withinExpression(ImplicitAnonymousFunctionExpressionNode node, BallerinaCompletionContext context) {
        int cursorPosition = context.getCursorPositionInTree();
        return PositionUtil.isWithInRange(node.expression(), cursorPosition) ||
                node.rightDoubleArrow().textRange().endOffset() < cursorPosition;
    }
}
