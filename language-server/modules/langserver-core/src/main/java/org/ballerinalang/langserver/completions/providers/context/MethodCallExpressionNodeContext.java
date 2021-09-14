/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.List;

/**
 * Completion provider for {@link MethodCallExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MethodCallExpressionNodeContext extends FieldAccessContext<MethodCallExpressionNode> {

    public MethodCallExpressionNodeContext() {
        super(MethodCallExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, MethodCallExpressionNode node)
            throws LSCompletionException {
        ExpressionNode expression = node.expression();
        List<LSCompletionItem> completionItems = getEntries(context, expression);
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MethodCallExpressionNode node) {
        /*
        Supports the following only
        eg:
        (1) abc.def.test<cursor>Method()
        With this check, the following example also will come to the methodCall navigating through the parent 
        hierarchy and skip properly
        eg:
        (2) s<cursor>abc.def.testMethod()
        (3) self.<cursor>Method()
         */
        int cursor = context.getCursorPositionInTree();
        NameReferenceNode nameRef = node.methodName();
        Token dotToken = node.dotToken();

        return ((cursor >= nameRef.textRange().startOffset() && cursor <= nameRef.textRange().endOffset())
                || (!dotToken.isMissing() && cursor > dotToken.textRange().startOffset()));
    }
}
