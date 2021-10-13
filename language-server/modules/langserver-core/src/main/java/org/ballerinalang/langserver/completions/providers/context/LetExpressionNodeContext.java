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

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Collections;
import java.util.List;

/**
 * Handles the completions for {@link AssignmentStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class LetExpressionNodeContext extends AbstractCompletionProvider<LetExpressionNode> {

    public LetExpressionNodeContext() {
        super(LetExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, LetExpressionNode node) 
            throws LSCompletionException {
        if (CompletionUtil.isInKWMissing(node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        }
        return CompletionUtil.route(context, node.parent());
    }
    
    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        if (node.parent().kind() == SyntaxKind.LET_VAR_DECL) {
            LetVariableDeclarationNode letVariableDeclarationNode = (LetVariableDeclarationNode) node.parent();
            return !letVariableDeclarationNode.equalsToken().isMissing()
                    && letVariableDeclarationNode.equalsToken().textRange().startOffset() < cursor;
        }
        return true;
    }
}
