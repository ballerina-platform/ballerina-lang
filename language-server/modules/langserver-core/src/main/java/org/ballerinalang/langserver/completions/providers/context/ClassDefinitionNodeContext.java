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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link AnnotationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ClassDefinitionNodeContext extends AbstractCompletionProvider<ClassDefinitionNode> {

    public ClassDefinitionNodeContext() {
        super(ClassDefinitionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ClassDefinitionNode node) {
        if (this.withinBody(context, node)) {
            return this.getClassBodyCompletions(context);
        }

        if (this.onTypeReferenceContext(context)) {
            return new ArrayList<>();
        }

        return this.getClassTypeCompletions(context, node);
    }

    @Override
    public boolean onPreValidation(LSContext context, ClassDefinitionNode node) {
        return !node.classKeyword().isMissing();
    }

    private List<LSCompletionItem> getClassTypeCompletions(LSContext context, ClassDefinitionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        SnippetCompletionItem kwDistinct = new SnippetCompletionItem(context, Snippet.KW_DISTINCT.get());
        SnippetCompletionItem kwClient = new SnippetCompletionItem(context, Snippet.KW_CLIENT.get());
        SnippetCompletionItem kwReadonly = new SnippetCompletionItem(context, Snippet.KW_READONLY.get());
        completionItems.add(kwDistinct);
        completionItems.add(kwClient);
        completionItems.add(kwReadonly);
        node.classTypeQualifiers().forEach(token -> {
            switch (token.kind()) {
                case DISTINCT_KEYWORD:
                    completionItems.remove(kwDistinct);
                    break;
                case CLIENT_KEYWORD:
                    completionItems.remove(kwClient);
                    break;
                case READONLY_KEYWORD:
                    completionItems.remove(kwReadonly);
                    break;
                default:
                    break;
            }
        });

        return completionItems;
    }

    private boolean withinBody(LSContext context, ClassDefinitionNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();

        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }

        return cursor >= openBrace.textRange().endOffset() && cursor <= closeBrace.textRange().startOffset();
    }

    private List<LSCompletionItem> getClassBodyCompletions(LSContext context) {
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

    private boolean onTypeReferenceContext(LSContext context) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        while (nodeAtCursor.kind() != SyntaxKind.CLASS_DEFINITION) {
            if (nodeAtCursor.kind() == SyntaxKind.TYPE_REFERENCE) {
                return true;
            }
            nodeAtCursor = nodeAtCursor.parent();
        }

        return false;
    }
}
