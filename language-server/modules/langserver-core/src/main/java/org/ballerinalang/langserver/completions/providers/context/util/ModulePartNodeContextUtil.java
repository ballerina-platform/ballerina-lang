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
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.FunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SnippetBlock;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

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
        List<Snippet> snippets = new ArrayList<>(Arrays.asList(Snippet.KW_TYPE, Snippet.KW_PUBLIC, Snippet.KW_ISOLATED,
                Snippet.KW_FINAL, Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_CLIENT, Snippet.KW_VAR,
                Snippet.KW_ENUM, Snippet.KW_XMLNS, Snippet.KW_CLASS, Snippet.KW_TRANSACTIONAL,
                Snippet.DEF_FUNCTION, Snippet.DEF_EXPRESSION_BODIED_FUNCTION, Snippet.KW_CONFIGURABLE,
                Snippet.DEF_ANNOTATION, Snippet.DEF_RECORD, Snippet.STMT_NAMESPACE_DECLARATION,
                Snippet.DEF_OBJECT_SNIPPET, Snippet.DEF_CLASS, Snippet.DEF_ENUM, Snippet.DEF_CLOSED_RECORD,
                Snippet.DEF_ERROR_TYPE, Snippet.DEF_TABLE_TYPE_DESC, Snippet.DEF_TABLE_WITH_KEY_TYPE_DESC,
                Snippet.DEF_STREAM, Snippet.DEF_SERVICE_COMMON));
        //Add import keyword conditionally
        if (isInImportStatementsContext(context)) {
            snippets.add(Snippet.KW_IMPORT);
        }
        snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));

        if (isMainFunctionUnavailable(context)) {
            LSCompletionItem mainCompletionItem = FunctionCompletionItemBuilder.buildMainFunction(context);
            completionItems.add(mainCompletionItem);
        }

        return completionItems;
    }

    public static boolean isMainFunctionUnavailable(BallerinaCompletionContext context) {
        Optional<ModulePartNode> modulePartNode = context.currentSyntaxTree().map(SyntaxTree::rootNode);
        return modulePartNode.filter(partNode ->
                context.visibleSymbols(PositionUtil.toPosition(partNode.lineRange().startLine()))
                        .stream().anyMatch(symbol -> symbol.kind() == SymbolKind.FUNCTION
                                && symbol.nameEquals("main"))).isEmpty();
    }

    private static boolean isInImportStatementsContext(BallerinaCompletionContext context) {
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        Optional<ModulePartNode> modulePartNode = syntaxTree.map(SyntaxTree::rootNode);
        if (syntaxTree.isEmpty() || modulePartNode.isEmpty()) {
            return false;
        }
        int cursor = context.getCursorPositionInTree();
        return modulePartNode.get().members().stream().noneMatch(
                moduleMemberDeclarationNode ->
                        moduleMemberDeclarationNode.kind() != SyntaxKind.IMPORT_DECLARATION &&
                                PositionUtil.getPositionOffset(
                                        PositionUtil.toPosition(moduleMemberDeclarationNode.lineRange().endLine()),
                                        syntaxTree.get()) < cursor);
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
                SnippetCompletionItem snippetCompletionItem = (SnippetCompletionItem) item;
                if (snippetCompletionItem.id().equals(Snippet.DEF_SERVICE_COMMON.name())) {
                    cItem.setSortText(genSortText(1) + genSortText(2));
                    continue;
                }
                if (snippetCompletionItem.id().equals(Snippet.DEF_FUNCTION.name())) {
                    cItem.setSortText(genSortText(1) + genSortText(5));
                    continue;
                }
                if (snippetCompletionItem.id().equals(Snippet.DEF_CLOSED_RECORD.name())) {
                    cItem.setSortText(genSortText(1) + genSortText(6));
                    continue;
                }
                if (snippetCompletionItem.id().equals(Snippet.DEF_RECORD.name())) {
                    cItem.setSortText(genSortText(1) + genSortText(7));
                    continue;
                }
                cItem.setSortText(genSortText(2));
                continue;
            }
            if (isServiceTemplate(item)) {
                cItem.setSortText(genSortText(1) + genSortText(3));
                continue;
            }
            if (SortingUtil.isTypeCompletionItem(item)) {
                if (SortingUtil.isLangLibModuleCompletionItem(item)) {
                    cItem.setSortText(genSortText(3) + genSortText(2));
                    continue;
                }
                cItem.setSortText(genSortText(3) + genSortText(1));
                continue;
            }
            if (isKeyword(item)) {
                SnippetCompletionItem snippet = (SnippetCompletionItem) item;
                if (Snippet.KW_SERVICE.name().equals(snippet.id())
                        || Snippet.KW_FUNCTION.name().equals(snippet.id())) {
                    cItem.setSortText(genSortText(1) + genSortText(4));
                    continue;
                }
                cItem.setSortText(genSortText(4));
                continue;
            }
            if (SortingUtil.isLangLibModuleCompletionItem(item)) {
                cItem.setSortText(genSortText(5));
                continue;
            }
            if (SortingUtil.isModuleCompletionItem(item)) {
                cItem.setSortText(genSortText(6));
                continue;
            }
            if (item instanceof StaticCompletionItem &&
                    (((StaticCompletionItem) item).kind() == StaticCompletionItem.Kind.MAIN_FUNCTION)) {
                cItem.setSortText(genSortText(1) + genSortText(1));
                continue;
            }
            cItem.setSortText(genSortText(7));
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

    private static boolean isServiceTemplate(LSCompletionItem completionItem) {
        return completionItem.getType() == LSCompletionItem.CompletionItemType.STATIC
                && ((StaticCompletionItem) completionItem).kind() == StaticCompletionItem.Kind.SERVICE_TEMPLATE;
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

    /**
     * Get the last qualifier of the node, but in the same line as the cursor. We have to consider the cursor's
     * line here due to parser's behavior at the module context.
     *
     * @param context Completion context
     * @param node    Node
     * @return Optional last qualifier's token
     */
    public static Optional<Token> getLastQualifier(BallerinaCompletionContext context, Node node) {
        Position cursorPos = context.getCursorPosition();
        // Get the qualifiers in the same line as the cursor
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node)
                .stream()
                .filter(qualifier -> qualifier.lineRange().endLine().line() == cursorPos.getLine())
                .collect(Collectors.toList());
        if (qualifiers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(qualifiers.get(qualifiers.size() - 1));
    }
}
