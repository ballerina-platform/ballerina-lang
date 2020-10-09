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

import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Completion provider for {@link ObjectTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ObjectTypeDescriptorNodeContext
        extends AbstractCompletionProvider<ObjectTypeDescriptorNode> {

    public ObjectTypeDescriptorNodeContext() {
        super(ObjectTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ObjectTypeDescriptorNode node) {
        if (this.onSuggestionsAfterQualifier(context, node)) {
            return Arrays.asList(
                    new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()),
                    new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get())
            );
        }
        if (this.onSuggestionsWithinObjectBody(context, node)) {
            return this.getObjectBodyCompletions(context);
        }
        
        return new ArrayList<>();
    }

    private boolean onSuggestionsAfterQualifier(LSContext context, ObjectTypeDescriptorNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        NodeList<Token> qualifiers = node.objectTypeQualifiers();

        if (qualifiers.isEmpty()) {
            return false;
        }

        Token objectKeyword = node.objectKeyword();
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);

        return cursor > lastQualifier.textRange().endOffset()
                && (objectKeyword.isMissing() || cursor < objectKeyword.textRange().startOffset());
    }

    private boolean onSuggestionsWithinObjectBody(LSContext context, ObjectTypeDescriptorNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();
        
        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }
        
        return cursor > openBrace.textRange().startOffset() && cursor < closeBrace.textRange().endOffset();
    }

    private List<LSCompletionItem> getObjectBodyCompletions(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PRIVATE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }
}
