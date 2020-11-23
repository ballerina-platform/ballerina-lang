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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.CompletionProvider;
import org.ballerinalang.langserver.completions.ProviderFactory;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {

    /**
     * Get the completion Items for the context.
     *
     * @param ctx Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(CompletionContext ctx) throws LSCompletionException,
            TokenOrSymbolNotFoundException {
        fillTokenInfoAtCursor(ctx);
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
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
    public static List<LSCompletionItem> route(CompletionContext ctx, Node node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node == null) {
            return completionItems;
        }
        Map<Class<?>, CompletionProvider<Node>> providers = ProviderFactory.instance().getProviders();
        Node reference = node;
        CompletionProvider<Node> provider = null;

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
    public static void fillTokenInfoAtCursor(CompletionContext context) throws TokenOrSymbolNotFoundException {
        context.setTokenAtCursor(TokensUtil.findTokenAtPosition(context, context.getCursorPosition()));
        Optional<Document> document = context.workspace().document(context.filePath());
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        // TODO: Try to delegate the set option to the context
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        while (true) {
            if (!withinTextRange(txtPos, nonTerminalNode)) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        context.setNodeAtCursor(nonTerminalNode);
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }
}
