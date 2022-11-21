/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SnippetBlock;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;

/**
 * Utilities for the module part node context.
 *
 * @since 2.0.0
 */
public class ModulePartNodeContextUtil {

    private ModulePartNodeContextUtil() {
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param context LS Context
     * @return {@link List}     List of populated completion items
     */
    public static List<LSCompletionItem> getTopLevelItems(BallerinaCompletionContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        // Here we should add the function keyword as well, although it is added via #getTypeItems
        List<Snippet> snippets = Arrays.asList(
                Snippet.KW_IMPORT, Snippet.KW_TYPE, Snippet.KW_PUBLIC, Snippet.KW_ISOLATED,
                Snippet.KW_FINAL, Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_CLIENT, Snippet.KW_VAR,
                Snippet.KW_ENUM, Snippet.KW_XMLNS, Snippet.KW_CLASS, Snippet.KW_TRANSACTIONAL,
                Snippet.DEF_FUNCTION, Snippet.DEF_EXPRESSION_BODIED_FUNCTION, Snippet.DEF_MAIN_FUNCTION,
                Snippet.KW_CONFIGURABLE, Snippet.DEF_ANNOTATION, Snippet.DEF_RECORD, Snippet.STMT_NAMESPACE_DECLARATION,
                Snippet.DEF_OBJECT_SNIPPET, Snippet.DEF_CLASS, Snippet.DEF_ENUM, Snippet.DEF_CLOSED_RECORD,
                Snippet.DEF_ERROR_TYPE, Snippet.DEF_TABLE_TYPE_DESC, Snippet.DEF_TABLE_WITH_KEY_TYPE_DESC,
                Snippet.DEF_STREAM, Snippet.DEF_SERVICE_COMMON, Snippet.DEF_CLIENT_DECLARATION
        );

        snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));

        return completionItems;
    }

    /**
     * Sort the list of completion items.
     *
     * @param items {@link List} of completion items to be sorted
     */
    public static void sort(List<LSCompletionItem> items) {
        for (LSCompletionItem item : items) {
            CompletionItem cItem = item.getCompletionItem();
            if (isSnippetBlock(item)) {
                cItem.setSortText(genSortText(1));
                continue;
            }
            if (isKeyword(item)) {
                cItem.setSortText(genSortText(2));
                continue;
            }
            if (SortingUtil.isModuleCompletionItem(item) && !SortingUtil.isLangLibModuleCompletionItem(item)) {
                cItem.setSortText(genSortText(3));
                continue;
            }
            if (SortingUtil.isTypeCompletionItem(item)) {
                cItem.setSortText(genSortText(4));
                continue;
            }
            cItem.setSortText(genSortText(5));
        }
    }

    /**
     * Check whether the cursor at service type descriptor context.
     *
     * @param evalToken {@link Token}
     * @return {@link Boolean} whether the cursor at type desc context or not
     */
    public static boolean onServiceTypeDescContext(Token evalToken, BallerinaCompletionContext context) {
        Optional<Minutiae> tokenValueAtCursor = ModulePartNodeContextUtil.findTokenValueInMinutiae(evalToken);
        int cursor = context.getCursorPositionInTree();

        return ((evalToken.text().equals(SyntaxKind.SERVICE_KEYWORD.stringValue())
                && cursor > evalToken.textRange().endOffset())
                || (tokenValueAtCursor.isPresent()
                && tokenValueAtCursor.get().text().equals(SyntaxKind.SERVICE_KEYWORD.stringValue())
                && tokenValueAtCursor.get().textRange().endOffset() < cursor));
    }

    /**
     * Get the object type symbols for the service type descriptor context.
     *
     * @param context {@link BallerinaCompletionContext}
     * @return {@link List} of object symbols filtered
     */
    public static List<Symbol> serviceTypeDescContextSymbols(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        return visibleSymbols.stream().filter(serviceTypeDescPredicate()).collect(Collectors.toList());
    }

    /**
     * Get the predicate to filter the service type descriptor context symbols.
     *
     * @return {@link Predicate}
     */
    public static Predicate<Symbol> serviceTypeDescPredicate() {
        return symbol -> {
            if (symbol.kind() == SymbolKind.CLASS) {
                return true;
            }
            if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
                return false;
            }
            Optional<? extends TypeSymbol> objSymbol = SymbolUtil.getTypeDescriptor(symbol);
            return objSymbol.isPresent() && CommonUtil.getRawType(objSymbol.get()).typeKind() == TypeDescKind.OBJECT;
        };
    }

    private static boolean isSnippetBlock(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && (((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.SNIPPET
                || ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.STATEMENT);
    }

    private static boolean isKeyword(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.KEYWORD;
    }

    /**
     * Get the token value at the cursor. This identified token is not a whitespace or new line token.
     *
     * @param evalToken {@link Token}
     * @return {@link String} value of the token identified at the cursor
     */
    private static Optional<Minutiae> findTokenValueInMinutiae(Token evalToken) {
        List<Minutiae> tokensFromMinutiae = new ArrayList<>();
        evalToken.leadingMinutiae().forEach(minutiae -> {
            if (minutiae.kind() != SyntaxKind.WHITESPACE_MINUTIAE
                    && minutiae.kind() != SyntaxKind.END_OF_LINE_MINUTIAE) {
                tokensFromMinutiae.add(minutiae);
            }
        });

        return !tokensFromMinutiae.isEmpty() ? Optional.of(tokensFromMinutiae.get(tokensFromMinutiae.size() - 1))
                : Optional.empty();
    }
}
