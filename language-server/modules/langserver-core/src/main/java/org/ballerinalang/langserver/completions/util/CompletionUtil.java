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

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.CompletionProvider;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.ProviderFactory;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionUtil.class);

    /**
     * Resolve the visible symbols from the given BLang Package and the current context.
     *
     * @param completionContext Completion Service Context
     */
    public static void resolveSymbols(LSContext completionContext) {
        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(completionContext);
        BLangPackage bLangPackage = completionContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        bLangPackage.accept(treeVisitor);
    }

    /**
     * Get the completion Items for the context.
     *
     * @param ctx Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem> getCompletionItems(LSContext ctx)
            throws WorkspaceDocumentException, LSCompletionException {
        fillTokenInfoAtCursor(ctx);
        NonTerminalNode nodeAtCursor = ctx.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        List<LSCompletionItem> items = route(ctx, nodeAtCursor);
        return getPreparedCompletionItems(ctx, items);
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
    public static List<LSCompletionItem> route(LSContext ctx, Node node)
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
                    && !ctx.get(CompletionKeys.RESOLVER_CHAIN).contains(provider.getClass())) {
                break;
            }
            reference = reference.parent();
        }

        if (provider == null) {
            return completionItems;
        }
        ctx.get(CompletionKeys.RESOLVER_CHAIN).add(provider.getClass());
        
        return provider.getCompletions(ctx, reference);
    }

    private static List<CompletionItem> getPreparedCompletionItems(LSContext context, List<LSCompletionItem> items) {
        List<CompletionItem> completionItems = new ArrayList<>();
        boolean isSnippetSupported = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                .getSnippetSupport();
        List<CompletionItem> sortedItems = ItemSorters.get(context.get(CompletionKeys.SCOPE_NODE_KEY).getClass())
                .sortItems(context, items);

        // TODO: Remove this
        for (CompletionItem item : sortedItems) {
            if (!isSnippetSupported) {
                item.setInsertText(CommonUtil.getPlainTextSnippet(item.getInsertText()));
                item.setInsertTextFormat(InsertTextFormat.PlainText);
            } else {
                item.setInsertTextFormat(InsertTextFormat.Snippet);
            }
            completionItems.add(item);
        }

        return completionItems;
    }

    /**
     * Find the token at cursor.
     *
     * @throws WorkspaceDocumentException while retrieving the syntax tree from the document manager
     */
    public static void fillTokenInfoAtCursor(LSContext context) throws WorkspaceDocumentException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (!filePath.isPresent()) {
            return;
        }
        SyntaxTree syntaxTree = docManager.getTree(filePath.get());
        TextDocument textDocument = syntaxTree.textDocument();

        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.put(CompletionKeys.TEXT_POSITION_IN_TREE, txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(range);

        while (true) {
            if (!withinTextRange(txtPos, nonTerminalNode)) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        context.put(CompletionKeys.NODE_AT_CURSOR_KEY, nonTerminalNode);
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }
}
