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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ObjectTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ObjectTypeDescriptorNodeContext
        extends AbstractCompletionProvider<ObjectTypeDescriptorNode> {

    public ObjectTypeDescriptorNodeContext() {
        super(ObjectTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ObjectTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (onSuggestionsAfterQualifiers(context, node)) {
            completionItems.addAll(this.getCompletionItemsOnQualifiers(node, context));
        } else if (this.onSuggestionsWithinObjectBody(context, node)) {
            completionItems.addAll(this.getObjectBodyCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    protected boolean onSuggestionsAfterQualifiers(BallerinaCompletionContext context, Node node) {
        if (node.kind() != SyntaxKind.OBJECT_TYPE_DESC) {
            return false;
        }
        boolean isAfterQualifier = super.onSuggestionsAfterQualifiers(context, node);
        int cursor = context.getCursorPositionInTree();
        Token objectKeyword = ((ObjectTypeDescriptorNode) node).objectKeyword();
        return isAfterQualifier
                && (objectKeyword.isMissing() || cursor < objectKeyword.textRange().startOffset());
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        if (lastQualifier.kind() == SyntaxKind.ISOLATED_KEYWORD) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
        }
        return completionItems;
    }

    private boolean onSuggestionsWithinObjectBody(BallerinaCompletionContext context, ObjectTypeDescriptorNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();

        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }

        return cursor > openBrace.textRange().startOffset() && cursor < closeBrace.textRange().endOffset();
    }

    private List<LSCompletionItem> getObjectBodyCompletions(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        // Here we do not add the function keyword as type descriptor completion items add it.
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_METHOD_DECL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION_SIGNATURE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.addAll(this.getTypeDescContextItems(context));

        return completionItems;
    }
}
