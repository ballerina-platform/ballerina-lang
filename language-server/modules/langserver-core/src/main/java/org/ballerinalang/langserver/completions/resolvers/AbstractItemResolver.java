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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.CompletionKeys;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Interface for completion item resolvers.
 */
public abstract class AbstractItemResolver {

    public abstract List<CompletionItem> resolveItems(LSServiceOperationContext completionContext);

    /**
     * Populate the completion item list by considering the.
     *
     * @param symbolInfoList    list of symbol information
     * @param context           Language server operation context
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
     * @param list              Either List of completion items or symbol info
     * @param context           LS Operation Context
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
     * @param context               Completion operation context
     * @return {@link Boolean}      Whether invocation or Field Access
     */
    protected boolean isInvocationOrInteractionOrFieldAccess(LSServiceOperationContext context) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(context), 2);
        return poppedTokens.contains(UtilSymbolKeys.DOT_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)
                || poppedTokens.contains(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.LEFT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.BANG_SYMBOL_KEY);
    }

    /**
     * Check whether the token stream contains an annotation start (@).
     *
     * @param ctx                   Completion operation context
     * @return {@link Boolean}      Whether annotation context start or not
     */
    boolean isAnnotationStart(LSServiceOperationContext ctx) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(ctx), 4);
        return poppedTokens.contains(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY);
    }

    /**
     * Get the basic types.
     *
     * @param visibleSymbols    List of visible symbols
     * @return {@link List}     List of completion items
     */
    List<CompletionItem> getBasicTypes(List<SymbolInfo> visibleSymbols) {
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
     * @param context           Completion context
     * @return {@link List}     List of resolved completion items
     */
    protected List<CompletionItem> getVarDefCompletionItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        boolean snippetCapability = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY)
                .getCompletionItem().getSnippetSupport();
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
        CompletionItem checkKeyword = Snippet.KW_CHECK.get().build(snippetCapability);
        completionItems.add(checkKeyword);
        // Add the wait keyword
        CompletionItem waitKeyword = Snippet.KW_CHECK.get().build(snippetCapability);
        completionItems.add(waitKeyword);
        // Add But keyword item
        CompletionItem butKeyword = Snippet.EXPR_MATCH.get().build(snippetCapability);
        completionItems.add(butKeyword);
        // Add the trap expression keyword
        CompletionItem trapExpression = Snippet.STMT_TRAP.get().build(snippetCapability);
        completionItems.add(trapExpression);

        return completionItems;
    }

    /**
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     * 
     * @param ctx               LS Operation context
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
     * @param context       Language Server Service Operation Context
     * @return {@link List} Completion Item List
     */
    protected List<CompletionItem> getDelimiterBasedCompletionItems(LSServiceOperationContext context) {
        Either<List<CompletionItem>, List<SymbolInfo>> itemList = SymbolFilters.get(DelimiterBasedContentFilter.class)
                .filterItems(context);
        return this.getCompletionItemList(itemList, context);
    }

    protected Predicate<SymbolInfo> attachedOrSelfKeywordFilter() {
        return symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED)
                    || (UtilSymbolKeys.SELF_KEYWORD_KEY.equals(bSymbol.getName().getValue())
                    && (bSymbol.owner.flags & Flags.RESOURCE) == Flags.RESOURCE);
        };
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
     * @param symbolInfo                symbol information
     * @param context                   Language Server Operation Context
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
