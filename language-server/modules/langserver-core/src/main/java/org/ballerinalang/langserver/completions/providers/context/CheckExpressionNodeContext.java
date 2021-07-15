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

import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link CheckExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class CheckExpressionNodeContext extends AbstractCompletionProvider<CheckExpressionNode> {
    public CheckExpressionNodeContext() {
        super(CheckExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, CheckExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT
                || node.parent().kind() == SyntaxKind.LOCAL_VAR_DECL
                || node.parent().kind() == SyntaxKind.MODULE_VAR_DECL
                || node.parent().kind() == SyntaxKind.OBJECT_FIELD) {
            completionItems.addAll(CompletionUtil.route(ctx, node.parent()));
        } else {
            /*
            We add the action keywords in order to support the check action context completions
             */
            completionItems.addAll(this.actionKWCompletions(ctx));
            completionItems.addAll(this.expressionCompletions(ctx));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.STMT_COMMIT.get()));
        }
        this.sort(ctx, node, completionItems);

        return completionItems;
    }
}
