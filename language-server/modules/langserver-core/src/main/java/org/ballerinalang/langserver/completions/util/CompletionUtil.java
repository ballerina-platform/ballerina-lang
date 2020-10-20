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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.CommonKeys;
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
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
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
     * Resolve the visible symbols from the given BLang Package and the current context.
     *
     * @param completionContext Completion Service Context
     */
    public static void resolveSymbols(LSContext completionContext) {
        // Visit the package to resolve the symbols
        BLangPackage bLangPackage = completionContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage,
                completionContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
        Position position = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        String filePath = completionContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        completionContext.put(CommonKeys.VISIBLE_SYMBOLS_KEY, semanticModel
                .visibleSymbols(filePath, LinePosition.from(position.getLine(), position.getCharacter())));
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

    /**
     * Find the token at cursor.
     *
     * @throws WorkspaceDocumentException while retrieving the syntax tree from the document manager
     */
    public static void fillTokenInfoAtCursor(LSContext context) throws WorkspaceDocumentException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (filePath.isEmpty()) {
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
