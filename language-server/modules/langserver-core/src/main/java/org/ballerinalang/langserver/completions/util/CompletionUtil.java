/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;
import org.ballerinalang.langserver.completions.ProviderFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionTriggerKind;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {

    private CompletionUtil() {
    }

    /**
     * Get the completion Items for the context.
     *
     * @param ctx Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(BallerinaCompletionContext ctx) throws LSCompletionException {
        ctx.checkCancelled();
        fillTokenInfoAtCursor(ctx);
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        /*
        Here we skip auto completion for the cases where the token at cursor is `>`. `>` is added as a trigger 
        character, in order to trigger completions for `->`. This leads to completion trigger for un wanted cases.
         */
        boolean contextSupport = Boolean.TRUE.equals(ctx.getCapabilities().getContextSupport());
        String triggerCharacter = contextSupport ?
                ctx.getCompletionParams().getContext().getTriggerCharacter() : "";
        CompletionTriggerKind triggerKind = contextSupport ? 
                ctx.getCompletionParams().getContext().getTriggerKind() : null;
        if (triggerKind == CompletionTriggerKind.TriggerCharacter
                && (triggerCharacter.equals(SyntaxKind.GT_TOKEN.stringValue())
                && ctx.getTokenAtCursor().kind() != SyntaxKind.RIGHT_ARROW_TOKEN
                && ctx.getTokenAtCursor().kind() != SyntaxKind.SYNC_SEND_TOKEN
                || triggerCharacter.equals(SyntaxKind.BACK_SLASH_TOKEN.stringValue()) 
                && ctx.getNodeAtCursor().kind() != SyntaxKind.RE_LITERAL_CHAR_DOT_OR_ESCAPE
                || triggerCharacter.equals(SyntaxKind.QUESTION_MARK_TOKEN.stringValue()) 
                && ctx.getNodeAtCursor().kind() != SyntaxKind.RE_FLAG_EXPR)
                || isWithinComment(ctx)) {
            return Collections.emptyList();
        }

        List<LSCompletionItem> items = route(ctx, nodeAtCursor);

        return items.stream()
                .map(LSCompletionItem::getCompletionItem)
                .collect(Collectors.toList());
    }

    /**
     * Get the nearest matching provider for the context node.
     * Router can be called recursively. Therefore, if there is an already checked resolver in the resolver chain,
     * that means the particular resolver could not handle the completions request. Therefore skip the particular node
     * and traverse the parent ladder to find the nearest matching resolver. In order to handle this, the particular
     * resolver chain check has been added.
     *
     * @param node node to evaluate
     * @return {@link Optional} provider which resolved
     */
    public static List<LSCompletionItem> route(BallerinaCompletionContext ctx, Node node)
            throws LSCompletionException {
        ctx.checkCancelled();
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node == null) {
            return completionItems;
        }
        Map<Class<?>, BallerinaCompletionProvider<Node>> providers = ProviderFactory.instance().getProviders();
        Node reference = node;
        BallerinaCompletionProvider<Node> provider = null;

        while ((reference != null)) {
            provider = providers.get(reference.getClass());
            // Resolver chain check has been added to cover the use-case in the documentation of the method
            if (provider != null && provider.onPreValidation(ctx, reference)
                    && !ctx.getResolverChain().contains(reference)) {
                ctx.addResolver(reference);
                break;
            }
            ctx.addResolver(reference);
            reference = reference.parent();
        }

        if (provider == null) {
            return completionItems;
        }

        return provider.getCompletions(ctx, reference);
    }

    /**
     * Find the token at cursor.
     */
    public static void fillTokenInfoAtCursor(BallerinaCompletionContext context) {
        Optional<Token> tokenAtCursor = PositionUtil.findTokenAtPosition(context, context.getCursorPosition());
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty() || tokenAtCursor.isEmpty()) {
            throw new RuntimeException("Could not find a valid document/token");
        }
        context.setTokenAtCursor(tokenAtCursor.get());
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        // TODO: Try to delegate the set option to the context
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        context.setNodeAtCursor(nonTerminalNode);
    }

    /**
     * Check whether the cursor is within a comment.
     */
    private static boolean isWithinComment(BallerinaCompletionContext ctx) {
        Iterator<Minutiae> minutiaeIterator = Collections.emptyIterator();
        if (ctx.getCursorPositionInTree() <= ctx.getTokenAtCursor().textRange().startOffset()) {
            minutiaeIterator = ctx.getTokenAtCursor().leadingMinutiae().iterator();
        } else if (ctx.getTokenAtCursor().textRange().endOffset() <= ctx.getCursorPositionInTree()) {
            minutiaeIterator = ctx.getTokenAtCursor().trailingMinutiae().iterator();
        }

        while (minutiaeIterator.hasNext()) {
            Minutiae minutiae = minutiaeIterator.next();
            if (minutiae.kind() == SyntaxKind.COMMENT_MINUTIAE
                    && minutiae.textRange().startOffset() < ctx.getCursorPositionInTree()
                    && ctx.getCursorPositionInTree() <= minutiae.textRange().endOffset()) {
                        return true;
            }
        }
        return false;
    }
}
