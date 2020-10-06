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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ObjectFieldNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ObjectFieldNodeContext extends AbstractCompletionProvider<ObjectFieldNode> {

    public ObjectFieldNodeContext() {
        super(ObjectFieldNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ObjectFieldNode node) {
        if (this.onExpressionContext(context, node)) {
            return this.getExpressionContextCompletions(context);
        }
        if (this.onModuleTypeDescriptorsOnly(context, node)) {
            NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getTypesInModule(context, qNameRef), context);
        }

        return this.getClassBodyCompletions(context, node);
    }

    private List<LSCompletionItem> getClassBodyCompletions(LSContext context, ObjectFieldNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PRIVATE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
        if (node.parent().kind() == SyntaxKind.OBJECT_CONSTRUCTOR
                || node.parent().kind() == SyntaxKind.CLASS_DEFINITION) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        } else if (node.parent().kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION_SIGNATURE.get()));
        }
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    private List<LSCompletionItem> getExpressionContextCompletions(LSContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        return this.expressionCompletions(ctx);
    }

    private boolean onModuleTypeDescriptorsOnly(LSContext context, ObjectFieldNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        Optional<Token> qualifier = node.visibilityQualifier();

        return qualifier.isPresent() && qualifier.get().textRange().endOffset() < cursor
                && nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }

    private boolean onExpressionContext(LSContext context, ObjectFieldNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Optional<Token> equalsToken = node.equalsToken();

        return equalsToken.isPresent() && equalsToken.get().textRange().endOffset() <= cursor;
    }

    @Override
    public boolean onPreValidation(LSContext context, ObjectFieldNode node) {
        /*
        This validation is added in order to avoid identifying the following context as object field node context.
        This is happened due to the parser recovery strategy.
        Eg: type TestType client o<cursor>
         */
        NonTerminalNode parent = node.parent();
        return (parent.kind() == SyntaxKind.CLASS_DEFINITION
                && !((ClassDefinitionNode) parent).openBrace().isMissing()) ||
                (parent.kind() == SyntaxKind.OBJECT_TYPE_DESC
                        && !((ObjectTypeDescriptorNode) parent).openBrace().isMissing()) ||
                (parent.kind() == SyntaxKind.OBJECT_CONSTRUCTOR
                        && !((ObjectConstructorExpressionNode) parent).openBraceToken().isMissing());
    }
}
