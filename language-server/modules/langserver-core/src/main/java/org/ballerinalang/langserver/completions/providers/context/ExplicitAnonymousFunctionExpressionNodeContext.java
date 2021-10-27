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

import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link ExplicitAnonymousFunctionExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ExplicitAnonymousFunctionExpressionNodeContext
        extends AbstractCompletionProvider<ExplicitAnonymousFunctionExpressionNode> {

    public ExplicitAnonymousFunctionExpressionNodeContext() {
        super(ExplicitAnonymousFunctionExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ExplicitAnonymousFunctionExpressionNode node)
            throws LSCompletionException {
        if (this.onSuggestionsAfterQualifiers(context, node)) {
            // Currently we consider the isolated qualifier only
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        }
        if (this.onSuggestionsWithinSignature(context, node)) {
            return CompletionUtil.route(context, node.functionSignature());
        }

        return new ArrayList<>();
    }

    private boolean onSuggestionsAfterQualifiers(BallerinaCompletionContext context,
                                                 ExplicitAnonymousFunctionExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        NodeList<Token> qualifiers = node.qualifierList();
        Token functionKeyword = node.functionKeyword();

        if (qualifiers.isEmpty()) {
            return false;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        return cursor > lastQualifier.textRange().endOffset()
                && (functionKeyword.isMissing() || cursor < functionKeyword.textRange().startOffset());
    }

    private boolean onSuggestionsWithinSignature(BallerinaCompletionContext context,
                                                 ExplicitAnonymousFunctionExpressionNode node) {
        Token functionKeyword = node.functionKeyword();
        if (functionKeyword.isMissing()) {
            return false;
        }
        Token openParenToken = node.functionSignature().openParenToken();
        if (openParenToken.isMissing()) {
            return false;
        }
        int cursor = context.getCursorPositionInTree();
        
        return openParenToken.textRange().endOffset() <= cursor;
    }
}
