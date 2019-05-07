/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.spi;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.LSCompletionProviderFactory;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Interface for completion item providers.
 *
 * @since 0.995.0
 */
public abstract class LSCompletionProvider {

    protected List<Class> attachmentPoints = new ArrayList<>();

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Language Server Context
     * @return {@link List}     List of calculated Completion Items
     */
    public abstract List<CompletionItem> getCompletions(LSContext context);

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    public List<Class> getAttachmentPoints() {
        return this.attachmentPoints;
    }

    /**
     * Get the Context Provider.
     * Ex: When a given scope is resolved then the context can be resolved by parsing a sub rule or token analyzing
     *
     * @param ctx Language Server Context
     * @return {@link Optional} Context Completion provider
     */
    public Optional<LSCompletionProvider> getContextProvider(LSContext ctx) {
        return Optional.empty();
    }

    /**
     * Populate the completion item list by considering the.
     *
     * @param symbolInfoList list of symbol information
     * @param context        Language server operation context
     * @return {@link List}     list of completion items
     */
    protected List<CompletionItem> getCompletionItemList(List<SymbolInfo> symbolInfoList, LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        symbolInfoList.removeIf(CommonUtil.invalidSymbolsPredicate());
        symbolInfoList.forEach(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.isCustomOperation() ? null : symbolInfo.getScopeEntry().symbol;
            if (CommonUtil.isValidInvokableSymbol(bSymbol) || symbolInfo.isCustomOperation()) {
                completionItems.add(populateBallerinaFunctionCompletionItem(symbolInfo));
            } else if (!(bSymbol instanceof BInvokableSymbol) && bSymbol instanceof BVarSymbol) {
                String typeName = symbolInfo.getScopeEntry().symbol.type.toString();
                completionItems.add(
                        BVariableCompletionItemBuilder.build((BVarSymbol) bSymbol, symbolInfo.getSymbolName(), typeName)
                );
            } else if (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol)) {
                // Here skip all the package symbols since the package is added separately
                completionItems.add(
                        BTypeCompletionItemBuilder.build((BTypeSymbol) bSymbol, symbolInfo.getSymbolName()));
            } else if (bSymbol instanceof BConstantSymbol) {
                completionItems.add(this.getBallerinaConstantCompletionItem(symbolInfo, context));
            }
        });
        return completionItems;
    }

    /**
     * Populate the completion item list by either list.
     *
     * @param list    Either List of completion items or symbol info
     * @param context LS Operation Context
     * @return {@link List}     Completion Items List
     */
    protected List<CompletionItem> getCompletionItemList(Either<List<CompletionItem>, List<SymbolInfo>> list,
                                                         LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        if (list.isLeft()) {
            completionItems.addAll(list.getLeft());
        } else {
            completionItems.addAll(this.getCompletionItemList(list.getRight(), context));
        }

        return completionItems;
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     *
     * @param context Completion operation context
     * @return {@link Boolean}      Whether invocation or Field Access
     */
    protected boolean isInvocationOrInteractionOrFieldAccess(LSContext context) {
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        if (lhsTokens == null) {
            return false;
        }
        List<CommonToken> lhsDefaultTokens = lhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        return !lhsDefaultTokens.isEmpty()
                && ((CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.COLON)
                || (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.DOT)
                || (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.RARROW)
                || (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.LARROW)
                || (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.NOT)
                || (lhsDefaultTokens.size() >= 2
                && (lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.COLON
                || lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.DOT
                || lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.RARROW
                || lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.LARROW
                || lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.NOT)));
    }

    /**
     * Get the basic types.
     *
     * @param visibleSymbols List of visible symbols
     * @return {@link List}     List of completion items
     */
    protected List<CompletionItem> getBasicTypes(List<SymbolInfo> visibleSymbols) {
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        List<CompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            if (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol)) {
                completionItems.add(
                        BTypeCompletionItemBuilder.build((BTypeSymbol) bSymbol, symbolInfo.getSymbolName()));
            }
        });

        return completionItems;
    }

    /**
     * Get variable definition context related completion items. This will extract the completion items analyzing the
     * variable definition context properties.
     *
     * @param context Completion context
     * @return {@link List}     List of resolved completion items
     */
    protected List<CompletionItem> getVarDefCompletionItems(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        filteredList.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || ((bSymbol instanceof BTypeSymbol)
                    && !(bSymbol instanceof BPackageSymbol))
                    || (bSymbol instanceof BInvokableSymbol
                    && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
        });
        completionItems.addAll(getCompletionItemList(filteredList, context));
        // Add the packages completion items.
        completionItems.addAll(getPackagesCompletionItems(context));
        // Add the check keyword
        CompletionItem checkKeyword = Snippet.KW_CHECK.get().build(context);
        completionItems.add(checkKeyword);
        // Add the wait keyword
        CompletionItem waitKeyword = Snippet.KW_CHECK.get().build(context);
        completionItems.add(waitKeyword);
        // Add But keyword item
        CompletionItem butKeyword = Snippet.EXPR_MATCH.get().build(context);
        completionItems.add(butKeyword);
        // Add the trap expression keyword
        CompletionItem trapExpression = Snippet.STMT_TRAP.get().build(context);
        completionItems.add(trapExpression);

        return completionItems;
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param context LS Context
     * @return {@link List}     List of populated completion items
     */
    protected List<CompletionItem> addTopLevelItems(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getStaticItem(context, Snippet.KW_IMPORT));
        completionItems.add(getStaticItem(context, Snippet.DEF_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_MAIN_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSOCKET));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSUB));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_GRPC));
        completionItems.add(getStaticItem(context, Snippet.DEF_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.STMT_NAMESPACE_DECLARATION));
        completionItems.add(getStaticItem(context, Snippet.DEF_OBJECT_SNIPPET));
        completionItems.add(getStaticItem(context, Snippet.DEF_RECORD));
        completionItems.add(getStaticItem(context, Snippet.KW_TYPE));
        completionItems.add(getStaticItem(context, Snippet.KW_PUBLIC));
        completionItems.add(getStaticItem(context, Snippet.KW_FINAL));
        completionItems.add(getStaticItem(context, Snippet.KW_CONST));
        completionItems.add(getStaticItem(context, Snippet.DEF_ERROR));
        completionItems.add(getStaticItem(context, Snippet.KW_LISTENER));
        return completionItems;
    }

    /**
     * Get a static completion Item for the given snippet.
     *
     * @param ctx     Language Server Context
     * @param snippet Snippet to generate the static completion item
     * @return {@link CompletionItem} Generated static completion Item
     */
    protected CompletionItem getStaticItem(LSContext ctx, Snippet snippet) {
        return snippet.get().build(ctx);
    }

    /**
     * Check whether the given token is an access modifier token.
     *
     * @param token Token values
     * @return {@link Boolean} Whether the token is an access modifier or not
     */
    protected boolean isAccessModifierToken(String token) {
        return token.equals(ItemResolverConstants.PUBLIC_KEYWORD)
                || token.equals(ItemResolverConstants.CONST_KEYWORD)
                || token.equals(ItemResolverConstants.FINAL_KEYWORD);
    }

    /**
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<CompletionItem> getPackagesCompletionItems(LSContext ctx) {
        // First we include the packages from the imported list.
        List<String> populatedList = new ArrayList<>();
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage pkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, pkg);
        List<CompletionItem> completionItems = CommonUtil.getCurrentFileImports(srcOwnerPkg, ctx).stream()
                .map(bLangImportPackage -> {
                    String orgName = bLangImportPackage.orgName.toString();
                    String pkgName = bLangImportPackage.pkgNameComps.stream()
                            .map(id -> id.value)
                            .collect(Collectors.joining("."));
                    CompletionItem item = new CompletionItem();
                    item.setLabel(orgName + "/" + pkgName);
                    item.setInsertText(CommonUtil.getLastItem(bLangImportPackage.getPackageName()).value);
                    item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return item;
                }).collect(Collectors.toList());
        List<BallerinaPackage> packages = LSPackageLoader.getSdkPackages();
        packages.addAll(LSPackageLoader.getHomeRepoPackages());

        packages.forEach(ballerinaPackage -> {
            String name = ballerinaPackage.getPackageName();
            String orgName = ballerinaPackage.getOrgName();
            if (!populatedList.contains(orgName + "/" + name)) {
                CompletionItem item = new CompletionItem();
                item.setLabel(ballerinaPackage.getFullPackageNameAlias());
                item.setInsertText(name);
                item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                item.setKind(CompletionItemKind.Module);
                item.setAdditionalTextEdits(CommonUtil.getAutoImportTextEdits(ctx, orgName, name));
                completionItems.add(item);
            }
        });

        return completionItems;
    }

    /**
     * Get the completion items based on the delimiter token, as an example . and : .
     *
     * @param context Language Server Service Operation Context
     * @return {@link List} Completion Item List
     */
    protected List<CompletionItem> getDelimiterBasedCompletionItems(LSContext context) {
        Either<List<CompletionItem>, List<SymbolInfo>> itemList = SymbolFilters.get(DelimiterBasedContentFilter.class)
                .filterItems(context);
        return this.getCompletionItemList(itemList, context);
    }

    /**
     * Predicate to filter the attached of Self, symbol.
     *
     * @return {@link Predicate} Symbol filter predicate
     */
    protected Predicate<SymbolInfo> attachedOrSelfKeywordFilter() {
        return symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED))
                    || (UtilSymbolKeys.SELF_KEYWORD_KEY.equals(bSymbol.getName().getValue())
                    && (bSymbol.owner.flags & Flags.RESOURCE) == Flags.RESOURCE);
        };
    }

    protected Optional<String> getSubrule(List<CommonToken> tokenList) {
        if (tokenList == null || tokenList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tokenList.stream().map(CommonToken::getText).collect(Collectors.joining("")));
    }

    /**
     * Get the provider for the given key.
     *
     * @param providerKey key to get the provider
     * @return {@link LSCompletionProvider} Completion Provider
     */
    protected LSCompletionProvider getProvider(Class providerKey) {
        return LSCompletionProviderFactory.getInstance().getProvider(providerKey);
    }

    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo) {
        if (symbolInfo.isCustomOperation()) {
            SymbolInfo.CustomOperationSignature signature =
                    symbolInfo.getCustomOperationSignature();
            return BFunctionCompletionItemBuilder.build(null, signature.getLabel(), signature.getInsertText());
        }
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        if (!(bSymbol instanceof BInvokableSymbol)) {
            return null;
        }
        return BFunctionCompletionItemBuilder.build((BInvokableSymbol) bSymbol);
    }

    /**
     * Get the Ballerina Constant Completion Item.
     *
     * @param symbolInfo symbol information
     * @param context    Language Server Operation Context
     * @return {@link CompletionItem}   completion item
     */
    private CompletionItem getBallerinaConstantCompletionItem(SymbolInfo symbolInfo, LSContext context) {
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        if (!(bSymbol instanceof BConstantSymbol)) {
            return null;
        }

        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(bSymbol.getName().getValue());
        completionItem.setInsertText(bSymbol.getName().getValue());
        completionItem.setDetail(CommonUtil.getBTypeName(((BConstantSymbol) bSymbol).literalValueType, context));
        completionItem.setDocumentation(ItemResolverConstants.CONSTANT_TYPE);
        completionItem.setKind(CompletionItemKind.Variable);

        return completionItem;
    }
}
