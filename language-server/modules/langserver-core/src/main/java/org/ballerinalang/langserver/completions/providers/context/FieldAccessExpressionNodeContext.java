/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.List;

/**
 * Completion Provider for {@link FieldAccessExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FieldAccessExpressionNodeContext 
        extends AbstractFieldAccessExpressionNodeContext<FieldAccessExpressionNode> {
    public FieldAccessExpressionNodeContext() {
        super(FieldAccessExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FieldAccessExpressionNode node)
            throws LSCompletionException {
        ExpressionNode expression = node.expression();
        List<LSCompletionItem> completionItems = getEntries(context, expression);
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FieldAccessExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token dotToken = node.dotToken();
        /*
        Added the cursor check against the dot token in order to validate the following and move to the enclosing 
        block.
        This check will skip the field access and accordingly navigate to MethodCallExpressionNodeContext
        and the particular onPreValidation will route to the it's own parent (ex: BlockNode)
        (1) s<cursor>abc.def.testMethod()
         */
        return cursor <= node.textRange().endOffset() && !dotToken.isMissing() &&
                cursor > dotToken.textRange().startOffset();
    }
}
