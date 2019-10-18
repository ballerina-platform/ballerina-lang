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
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.LSCompletionException;
import org.ballerinalang.langserver.completions.LSCompletionProviderFactory;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BConstantCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getFunctionInvocationSignature;

/**
 * Interface for completion item providers.
 *
 * @since 0.995.0
 */
public abstract class LSCompletionProvider {

    protected List<Class> attachmentPoints = new ArrayList<>();
    
    private Precedence precedence = Precedence.LOW;

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
     * Get the precedence of the provider.
     * 
     * @return {@link Precedence} precedence of the provider
     */
    public Precedence getPrecedence() {
        return precedence;
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
        List<BSymbol> processedSymbols = new ArrayList<>();
        List<CompletionItem> completionItems = new ArrayList<>();
        symbolInfoList.removeIf(CommonUtil.invalidSymbolsPredicate());
        symbolInfoList.forEach(symbolInfo -> {
            BSymbol symbol = symbolInfo.getScopeEntry().symbol;
            if (processedSymbols.contains(symbol)) {
                return;
            }
            Optional<BSymbol> bTypeSymbol;
            BSymbol bSymbol = symbolInfo.isCustomOperation() ? null : symbol;
            if (CommonUtil.isValidInvokableSymbol(bSymbol) || symbolInfo.isCustomOperation()) {
                completionItems.add(populateBallerinaFunctionCompletionItem(symbolInfo, context));
            } else if (bSymbol instanceof BConstantSymbol) {
                completionItems.add(BConstantCompletionItemBuilder.build((BConstantSymbol) bSymbol, context));
            } else if (!(bSymbol instanceof BInvokableSymbol) && bSymbol instanceof BVarSymbol) {
                String typeName = CommonUtil.getBTypeName(symbol.type, context, false);
                completionItems.add(
                        BVariableCompletionItemBuilder.build((BVarSymbol) bSymbol, symbolInfo.getSymbolName(), typeName)
                );
            } else if ((bTypeSymbol = FilterUtils.getBTypeEntry(symbolInfo.getScopeEntry())).isPresent()) {
                // Here skip all the package symbols since the package is added separately
                completionItems.add(BTypeCompletionItemBuilder.build(bTypeSymbol.get(), symbolInfo.getSymbolName()));
            }
            processedSymbols.add(symbol);
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
        return list.isLeft() ? list.getLeft() : this.getCompletionItemList(list.getRight(), context);
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
            if (((bSymbol instanceof BConstructorSymbol && Names.ERROR.equals(bSymbol.name)))
                    || (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol))) {
                BSymbol symbol = bSymbol;
                if (bSymbol instanceof BConstructorSymbol) {
                    symbol = ((BConstructorSymbol) bSymbol).type.tsymbol;
                }
                completionItems.add(BTypeCompletionItemBuilder.build(symbol, symbolInfo.getSymbolName()));
            }
        });

        return completionItems;
    }

    /**
     * Get all the types in the Package with given name.
     *
     * @param visibleSymbols Visible Symbols
     * @param pkgName package name
     * @param ctx language server context
     * @return {@link List} list of Type completion items
     */
    protected List<CompletionItem> getTypesInPackage(List<SymbolInfo> visibleSymbols, String pkgName, LSContext ctx) {
        List<SymbolInfo> filteredList = new ArrayList<>();
        Optional<SymbolInfo> pkgSymbolInfo = visibleSymbols.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BPackageSymbol && symbolInfo.getSymbolName().equals(pkgName); 
                })
                .findAny();
        pkgSymbolInfo.ifPresent(symbolInfo -> {
            BSymbol pkgSymbol = symbolInfo.getScopeEntry().symbol;
            pkgSymbol.scope.entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BTypeSymbol) {
                    filteredList.add(new SymbolInfo(name.getValue(), scopeEntry));
                } else if (scopeEntry.symbol instanceof BConstructorSymbol && Names.ERROR.equals(
                        scopeEntry.symbol.name)) {
                    filteredList.add(new SymbolInfo(name.getValue(), scopeEntry));
                }
            });
        });
        
        return this.getCompletionItemList(filteredList, ctx);
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
        completionItems.add(getStaticItem(context, Snippet.KW_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_MAIN_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSOCKET));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WS_CLIENT));
//        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSUB));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_GRPC));
        completionItems.add(getStaticItem(context, Snippet.DEF_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.STMT_NAMESPACE_DECLARATION));
        completionItems.add(getStaticItem(context, Snippet.DEF_OBJECT_SNIPPET));
        completionItems.add(getStaticItem(context, Snippet.DEF_RECORD));
        completionItems.add(getStaticItem(context, Snippet.DEF_CLOSED_RECORD));
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
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<CompletionItem> getPackagesCompletionItems(LSContext ctx) {
        // First we include the packages from the imported list.
        List<String> populatedList = new ArrayList<>();
        BLangPackage currentPkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<BLangImportPackage> currentModuleImports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        List<CompletionItem> completionItems = currentModuleImports.stream()
                .map(pkg -> {
                    String orgName = pkg.orgName.value;
                    String pkgName = pkg.pkgNameComps.stream()
                            .map(id -> id.value)
                            .collect(Collectors.joining("."));
                    String label = pkg.alias.value;
                    String insertText = pkg.alias.value;
                    // If the import is a langlib module and there isn't a user defined alias we add ' before
                    if ("ballerina".equals(orgName) && pkg.pkgNameComps.get(0).getValue().equals("lang")
                            && pkgName.endsWith("." + pkg.alias.value)) {
                        insertText = "'" + insertText;
                    }
                    CompletionItem item = new CompletionItem();
                    item.setLabel(label);
                    item.setInsertText(insertText);
                    item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return item;
                }).collect(Collectors.toList());

        List<BallerinaPackage> packages = LSPackageLoader.getSdkPackages();
        packages.addAll(LSPackageLoader.getHomeRepoPackages());
        packages.addAll(LSPackageLoader.getCurrentProjectModules(currentPkg, ctx));
        packages.forEach(ballerinaPackage -> {
            String name = ballerinaPackage.getPackageName();
            String orgName = ballerinaPackage.getOrgName();
            boolean pkgAlreadyImported = currentModuleImports.stream()
                    .anyMatch(importPkg -> importPkg.orgName.value.equals(orgName)
                            && importPkg.alias.value.equals(name));
            if (!pkgAlreadyImported && !populatedList.contains(orgName + "/" + name)) {
                CompletionItem item = new CompletionItem();
                item.setLabel(ballerinaPackage.getFullPackageNameAlias());
                String[] pkgNameComps = name.split("\\.");
                String insertText = pkgNameComps[pkgNameComps.length - 1];
                // Check for the lang lib module insert text
                if ("ballerina".equals(orgName) && name.startsWith("lang.")) {
                    String[] pkgNameComponents = name.split("\\.");
                    insertText = "'" + pkgNameComponents[pkgNameComponents.length - 1];
                }
                item.setInsertText(insertText);
                item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                item.setKind(CompletionItemKind.Module);
                item.setAdditionalTextEdits(CommonUtil.getAutoImportTextEdits(orgName, name, ctx));
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
    protected Predicate<SymbolInfo> attachedSymbolFilter() {
        return symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED);
        };
    }

    protected Optional<String> getSubRule(List<CommonToken> tokenList) {
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

    protected List<CompletionItem> getCompletionItemsAfterOnKeyword(LSContext ctx) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<SymbolInfo> filtered = this.filterListenerVariables(visibleSymbols);
        List<CompletionItem> completionItems = new ArrayList<>(this.getCompletionItemList(filtered, ctx));
        completionItems.add(Snippet.KW_NEW.get().build(ctx));

        return completionItems;
    }

    /**
     * Get the completions for the expression context in a variable definition.
     * Eg: Completion after the EQUAL sign of the variable definition
     * 
     * @param context Language Server context
     * @param onGlobal whether global variable definition
     * @return {@link List} List of completion Items
     */
    protected List<CompletionItem> getVarDefExpressionCompletions(LSContext context, boolean onGlobal) {
        List<CompletionItem> completionItems = new ArrayList<>();
        Integer invocationType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        try {
            Optional<BLangType> assignmentType = getAssignmentType(context, onGlobal);
            
            if (assignmentType.isPresent() && !(assignmentType.get() instanceof BLangUserDefinedType)
                    && invocationType > -1) {
                Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                completionItems.addAll(this.getCompletionItemList(filteredList, context));
                return completionItems;
            }

            boolean isTypeDesc = assignmentType.isPresent() && assignmentType.get() instanceof BLangConstrainedType
                    && ((BLangConstrainedType) assignmentType.get()).type instanceof BLangBuiltInRefTypeNode
                    && ((BLangBuiltInRefTypeNode) ((BLangConstrainedType) assignmentType.get()).type).typeKind ==
                    TypeKind.TYPEDESC;
            if (assignmentType.isPresent() && assignmentType.get() instanceof BLangFunctionTypeNode) {
                // Function Type Suggestion
                completionItems.addAll(this.getVarDefCompletions(context));
                fillFunctionWithBodySnippet((BLangFunctionTypeNode) assignmentType.get(), context, completionItems);
                fillArrowFunctionSnippet((BLangFunctionTypeNode) assignmentType.get(), context, completionItems);
            } else if (assignmentType.isPresent() && assignmentType.get().type instanceof BServiceType) {
                completionItems.addAll(this.getVarDefCompletions(context));
                completionItems.add(Snippet.DEF_SERVICE_VAR.get().build(context));
            } else if (assignmentType.isPresent() && assignmentType.get() instanceof BLangUserDefinedType) {
                completionItems.addAll(
                        getUserDefinedTypeCompletions(context, (BLangUserDefinedType) assignmentType.get()));
            } else if (isTypeDesc) {
                completionItems.add(Snippet.KW_TYPEOF.get().build(context));
                completionItems.addAll(getVarDefCompletions(context));
            } else if (invocationType > -1) {
                Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                return this.getCompletionItemList(filteredList, context);
            } else {
                completionItems.addAll(getVarDefCompletions(context));
            }
        } catch (LSCompletionException ex) {
            // do nothing
        }

        return completionItems;
    }

    protected List<CompletionItem> getUserDefinedTypeCompletions(LSContext context, BLangUserDefinedType type) {
        List<Integer> defaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        Integer invocationType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        List<CompletionItem> completionItems = new ArrayList<>();
        int newTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.NEW);
        int lastColonIndex = defaultTokenTypes.lastIndexOf(BallerinaParser.COLON);
        int firstColonIndex = defaultTokenTypes.indexOf(BallerinaParser.COLON);
        String typeName = type.typeName.value;
        String pkgAlias = type.pkgAlias.value;
        BSymbol bSymbol;

        Optional<SymbolInfo> pkgSymbol = this.getPackageSymbolFromAlias(context, pkgAlias);
        if (pkgSymbol.isPresent()) {
            Optional<Scope.ScopeEntry> entry = pkgSymbol.get().getScopeEntry().symbol.scope.entries.values().stream()
                    .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(typeName)).findAny();
            
            if (!entry.isPresent()) {
                return completionItems;
            }
            bSymbol = entry.get().symbol;
        } else {
            List<SymbolInfo> typeSymbol = getSymbolByName(typeName, context);
            if (typeSymbol.isEmpty()) {
                return completionItems;
            }
            bSymbol = typeSymbol.get(0).getScopeEntry().symbol;
        }
        
        if (bSymbol == null) {
            return completionItems;
        }
        
        if (bSymbol instanceof BRecordTypeSymbol) {
            if (invocationType > -1) {
                Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                return this.getCompletionItemList(filteredList, context);
            }
            return getVarDefCompletions(context);
        }
        if (!(bSymbol instanceof BObjectTypeSymbol)) {
            if (invocationType > -1) {
                Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                return this.getCompletionItemList(filteredList, context);
            }
            return getVarDefCompletions(context);
        }
        
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bSymbol;
        BAttachedFunction initFunction = objectTypeSymbol.initializerFunc;
        
        if (newTokenIndex > -1 && firstColonIndex > -1 && lastColonIndex == firstColonIndex) {
            /*
            ex: modName:ObjectName = new <cursor>
            suggest modules
             */
            return getPackagesCompletionItems(context);
        } else if (newTokenIndex > -1) {
            /*
            ex: modName:ObjectName = new modName:
                ObjectName = new 
             */
            Pair<String, String> newWithTypeSign = getFunctionInvocationSignature(initFunction.symbol, typeName,
                    context);
            completionItems.add(BFunctionCompletionItemBuilder.build(initFunction.symbol, newWithTypeSign.getRight(),
                    newWithTypeSign.getLeft(), context));
            return completionItems;
        } else if (invocationType > -1) {
            Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
            return this.getCompletionItemList(filteredList, context);
        }
        
        CompletionItem newCItem;
        CompletionItem typeCItem;
        if (initFunction == null) {
            newCItem = BFunctionCompletionItemBuilder.build(null, "new()", "new();", context);
            typeCItem = BFunctionCompletionItemBuilder.build(null, typeName + "()",
                                                             typeName + "();", context);
        } else {
            Pair<String, String> newSign = getFunctionInvocationSignature(initFunction.symbol,
                                                                          CommonKeys.NEW_KEYWORD_KEY, context);
            Pair<String, String> newWithTypeSign = getFunctionInvocationSignature(initFunction.symbol, typeName,
                                                                                  context);
            newCItem = BFunctionCompletionItemBuilder.build(initFunction.symbol,
                                                            newSign.getRight(),
                                                            newSign.getLeft(), context);
            typeCItem = BFunctionCompletionItemBuilder.build(initFunction.symbol,
                                                             newWithTypeSign.getRight(),
                                                             newWithTypeSign.getLeft(), context);
        }
        completionItems.addAll(getVarDefCompletions(context));
        completionItems.add(newCItem);
        completionItems.add(typeCItem);
    
        return completionItems;
    }

    /**
     * Get the Resource Snippets.
     * 
     * @param ctx Language Server Context
     * @return {@link Optional} Completion List
     */
    protected List<CompletionItem> getResourceSnippets(LSContext ctx) {
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);
        List<CompletionItem> items = new ArrayList<>();
        if (!(symbolEnvNode instanceof BLangService)) {
            return items;
        }
        BLangService service = (BLangService) symbolEnvNode;

        if (service.listenerType == null) {
            Optional<Boolean> webSocketClientService = isWebSocketClientService(service);
            if (webSocketClientService.isPresent()) {
                if (webSocketClientService.get()) {
                    // Is a 'ws' client service
                    addAllWSClientResources(ctx, items, service);
                } else {
                    // Is a 'ws' service
                    addAllWSResources(ctx, items, service);
                }
            } else {
                // Is ambiguous, suggest for all 'ws', 'ws-client' and 'http' services
                items.add(Snippet.DEF_RESOURCE_HTTP.get().build(ctx));
                items.add(Snippet.DEF_RESOURCE_COMMON.get().build(ctx));
                addAllWSClientResources(ctx, items, service);
                addAllWSResources(ctx, items, service);
            }
            return items;
        }

        String owner = service.listenerType.tsymbol.owner.name.value;
        String serviceTypeName = service.listenerType.tsymbol.name.value;

        // Only http, grpc have generic resource templates, others will have generic resource snippet
        switch (owner) {
            case "http":
                if ("Listener".equals(serviceTypeName)) {
                    Optional<Boolean> webSocketService = isWebSocketService(service);
                    if (webSocketService.isPresent()) {
                        if (webSocketService.get()) {
                            // Is a 'ws' service
                            addAllWSResources(ctx, items, service);
                        } else {
                            // Is a 'http' service
                            items.add(Snippet.DEF_RESOURCE_HTTP.get().build(ctx));
                        }
                    } else {
                        // Is ambiguous, suggest both 'ws' and 'http'
                        addAllWSResources(ctx, items, service);
                        items.add(Snippet.DEF_RESOURCE_HTTP.get().build(ctx));
                    }
                    break;
                }
                return items;
            case "grpc":
                items.add(Snippet.DEF_RESOURCE_GRPC.get().build(ctx));
                break;
//            case "websub":
//                addAllWebsubResources(ctx, items, service);
//                break;
            default:
                items.add(Snippet.DEF_RESOURCE_COMMON.get().build(ctx));
                return items;
        }
        return items;
    }

//    private void addAllWebsubResources(LSContext ctx, List<CompletionItem> items, BLangService service) {
//        addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_INTENT.get(), service, items, ctx);
//        addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_NOTIFY.get(), service, items, ctx);
//    }

    private void addAllWSClientResources(LSContext ctx, List<CompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_CLOSE.get(), service, items, ctx);
    }

    private void addAllWSResources(LSContext ctx, List<CompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_OPEN.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CLOSE.get(), service, items, ctx);
    }


    protected Optional<SymbolInfo> getPackageSymbolFromAlias(LSContext context, String alias) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (alias.isEmpty()) {
            return Optional.empty();
        }
        return visibleSymbols.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BPackageSymbol && alias.equals(symbolInfo.getSymbolName());
                })
                .findAny();
    }
    
    protected List<SymbolInfo> getSymbolByName(String name, LSContext context) {
        List<SymbolInfo> symbolInfos = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        return symbolInfos.parallelStream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.getName().getValue().equals(name))
                .collect(Collectors.toList());
    }

    protected boolean inFunctionReturnParameterContext(LSContext context) {
        if (context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY) == null) {
            return false;
        }
        List<Integer> defaultTokens = new ArrayList<>(context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY));
        if (defaultTokens.isEmpty()) {
            return false;
        }
        int tokenSize = defaultTokens.size();
        int assignTokenIndex = defaultTokens.indexOf(BallerinaParser.ASSIGN);
        if (assignTokenIndex > 0 && assignTokenIndex >= tokenSize - 3 && assignTokenIndex < tokenSize) {
            // check added to avoid the external function definition
            // function xyz() returns int = external;
            return false;
        }
        if (defaultTokens.contains(BallerinaParser.RETURNS)
                && ((defaultTokens.size() - 1) - defaultTokens.indexOf(BallerinaParser.RETURNS) <= 3)) {
            /*
            Check for the following cases
            Eg: function xyz() returns <cursor> {}
                function xyz() returns in<cursor> {}
                function xyz() returns in:<cursor> {}
                function xyz() returns mod1:a<cursor> {}
             */
            return true;
        }

        /*
         Catches the following case
         Eg: function xyz() re {}
         */
        return tokenSize > 2 && defaultTokens.contains(BallerinaParser.FUNCTION) &&
                (defaultTokens.get(tokenSize - 1) == BallerinaParser.RIGHT_PARENTHESIS
                || defaultTokens.get(tokenSize - 2) == BallerinaParser.RIGHT_PARENTHESIS);
    }

    protected boolean isAnnotationAccessExpression(LSContext context) {
        List<Integer> defaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        int annotationAccessIndex = defaultTokenTypes.indexOf(BallerinaParser.ANNOTATION_ACCESS);

        return annotationAccessIndex > -1;
    }
    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo, LSContext context) {
        if (symbolInfo.isCustomOperation()) {
            SymbolInfo.CustomOperationSignature signature =
                    symbolInfo.getCustomOperationSignature();
            return BFunctionCompletionItemBuilder.build(null, signature.getLabel(), signature.getInsertText(), context);
        }
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        if (!(bSymbol instanceof BInvokableSymbol)) {
            return null;
        }
        return BFunctionCompletionItemBuilder.build((BInvokableSymbol) bSymbol, context);
    }

    private List<SymbolInfo> filterListenerVariables(List<SymbolInfo> symbolInfos) {
        return symbolInfos.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BVarSymbol && CommonUtil.isListenerObject(symbol.type.tsymbol);
                })
                .collect(Collectors.toList());
    }

    public static Optional<BLangType> getAssignmentType(LSContext context, boolean onGlobal)
            throws LSCompletionException {

        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        int counter = 0;
        StringBuilder subRule = new StringBuilder();
        if (!onGlobal) {
            subRule.append("function testFunction () {").append(CommonUtil.LINE_SEPARATOR).append("\t");
        }
        while (counter < lhsTokens.size()) {
            if (lhsTokens.get(counter).getType() == BallerinaParser.PUBLIC
                    || lhsTokens.get(counter).getType() == BallerinaParser.PRIVATE) {
                // Evaluated when the object field definition completion is routed to var def completion context
                counter++;
                continue;
            }
            subRule.append(lhsTokens.get(counter).getText());
            if (lhsTokens.get(counter).getType() == BallerinaParser.ASSIGN) {
                subRule.append("0;");
                break;
            }
            counter++;
        }
        if (!onGlobal) {
            subRule.append(CommonUtil.LINE_SEPARATOR).append("}");
        }
        
        Optional<BLangPackage> bLangPackage;
        try {
            bLangPackage = ExtendedLSCompiler.compileContent(subRule.toString(), CompilerPhase.CODE_ANALYZE)
                    .getBLangPackage();
        } catch (CompilationFailedException e) {
            throw new LSCompletionException("Error while parsing the sub-rule");
        }

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        BLangType typeNode;
        
        if (onGlobal) {
            typeNode = bLangPackage.get().globalVars.get(0).getTypeNode();
        } else {
            typeNode = ((BLangSimpleVariableDef) bLangPackage.get().getFunctions().get(0).getBody().stmts.get(0))
                    .getVariable().typeNode;
        }

        return Optional.ofNullable(typeNode);
    }

    private void fillFunctionWithBodySnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                             List<CompletionItem> completionItems)
            throws LSCompletionException {

        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        String functionSignature = this.getFunctionSignature(params, returnBLangType, context);
        String body = this.getAnonFunctionSnippetBody(returnBLangType, params.size());
        String snippet = functionSignature + body;
        String label = this.convertToLabel(functionSignature);
        SnippetBlock snippetBlock = new SnippetBlock(label, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(context));
    }

    private String getFunctionSignature(List<BLangVariable> paramTypes, BLangType returnType, LSContext context)
            throws LSCompletionException {
        StringBuilder signature = new StringBuilder("function ");

        signature.append(this.getDynamicParamsSnippet(paramTypes, true, context));
        if (!(returnType.type instanceof BNilType)) {
            signature.append("returns (")
                    .append(CommonUtil.getBTypeName(returnType.type, context, false))
                    .append(") ");
        }

        return signature.toString();
    }

    private void fillArrowFunctionSnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                           List<CompletionItem> completionItems) throws LSCompletionException {
        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        String paramSignature = this.getDynamicParamsSnippet(params, false, context);
        StringBuilder signature = new StringBuilder(paramSignature);

        signature.append(" => ")
                .append("${");
        if (!(returnBLangType.type instanceof BNilType)) {
            signature.append(params.size() + 1)
                    .append(":")
                    .append(CommonUtil.getDefaultValueForType(returnBLangType.type));
        } else {
            signature.append(params.size() + 1);
        }
        signature.append("};");

        String label = "arrow function  " + this.convertToLabel(paramSignature);

        SnippetBlock snippetBlock = new SnippetBlock(label, signature.toString(), ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(context));
    }
    
    private String getAnonFunctionSnippetBody(BLangType returnType, int numberOfParams) {
        StringBuilder body = new StringBuilder();
        if (!(returnType.type instanceof BNilType)) {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t")
                    .append("return ")
                    .append(CommonUtil.getDefaultValueForType(returnType.type))
                    .append(";")
                    .append(CommonUtil.LINE_SEPARATOR);
        } else {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t${")
                    .append(numberOfParams + 1)
                    .append("}")
                    .append(CommonUtil.LINE_SEPARATOR);
        }

        body.append("};");

        return body.toString();
    }

    private String convertToLabel(String signature) {
        return signature
                .replaceAll("(\\$\\{\\d:)([a-zA-Z\\d]*:*[a-zA-Z\\d]*)(\\})", "$2")
                .replaceAll("(\\$\\{\\d\\})", "");
    }

    /**
     * Get the function parameter signature generated dynamically, with the given list of parameter types.
     * Parameter names will not be included for the arrow function snippets and the parameter names are generated 
     * dynamically
     * 
     * @param paramTypes List of Parameter Types
     * @param withType Whether tha parameters included with the types. In case of arrow functions this value is false
     * @param context Language server operation context
     *                 
     * @return {@link String} Generated function parameter snippet
     * @throws LSCompletionException Completion exception
     */
    private String getDynamicParamsSnippet(List<BLangVariable> paramTypes, boolean withType, LSContext context)
            throws LSCompletionException {
        String paramName = "param";
        StringBuilder signature = new StringBuilder("(");
        List<String> params = IntStream.range(0, paramTypes.size())
                .mapToObj(index -> {
                    int paramIndex = index + 1;
                    String paramPlaceHolder = "${" + paramIndex + ":" + paramName + paramIndex + "}";
                    if (withType) {
                        BType paramType = paramTypes.get(index).getTypeNode().type;
                        paramPlaceHolder = CommonUtil.getBTypeName(paramType, context, true) + " " + paramPlaceHolder;
                    }
                    return paramPlaceHolder;
                })
                .collect(Collectors.toList());

        if (params.contains("")) {
            throw new LSCompletionException("Contains invalid parameter type");
        }

        signature.append(String.join(", ", params))
                .append(") ");

        return signature.toString();
    }

    /**
     * Get variable definition context related completion items. This will extract the completion items analyzing the
     * variable definition context properties.
     *
     * @param context Completion context
     * @return {@link List}     List of resolved completion items
     */
    public List<CompletionItem> getVarDefCompletions(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredList = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        filteredList.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || (FilterUtils.isBTypeEntry(symbolInfo.getScopeEntry()))
                    || (bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
        });
        completionItems.addAll(getCompletionItemList(filteredList, context));
        // Add the packages completion items.
        completionItems.addAll(getPackagesCompletionItems(context));
        // Add the check keyword
        CompletionItem checkKeyword = Snippet.KW_CHECK.get().build(context);
        completionItems.add(checkKeyword);
        // Add the checkpanic keyword
        CompletionItem checkPanicKeyword = Snippet.KW_CHECK_PANIC.get().build(context);
        completionItems.add(checkPanicKeyword);
        // Add the wait keyword
        CompletionItem waitKeyword = Snippet.KW_WAIT.get().build(context);
        completionItems.add(waitKeyword);
        // Add the start keyword
        CompletionItem startKeyword = Snippet.KW_START.get().build(context);
        completionItems.add(startKeyword);
        // Add the flush keyword
        CompletionItem flushKeyword = Snippet.KW_FLUSH.get().build(context);
        completionItems.add(flushKeyword);
        // Add the untaint keyword
        CompletionItem untaintKeyword = Snippet.KW_UNTAINT.get().build(context);
        completionItems.add(untaintKeyword);
        // Add But keyword item
        CompletionItem butKeyword = Snippet.EXPR_MATCH.get().build(context);
        completionItems.add(butKeyword);
        // Add the trap expression keyword
        CompletionItem trapExpression = Snippet.STMT_TRAP.get().build(context);
        completionItems.add(trapExpression);

        return completionItems;
    }

    /**
     * Check whether the current context is annotation context.
     *
     * @param context Language server context
     * @return {@link Boolean} whether the cursor is in the annotation context
     */
    protected boolean isAnnotationAttachmentContext(LSContext context) {
        List<Integer> lhsDefaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        /*
        Max token bactrack count is set to 4 in order to support the following
        @moduleName:Rec
         */
        int maxTokenVisitCount = 4;
        int visitCount = 0;
        int counter = lhsDefaultTokenTypes.size() - 1;
        while (counter >= 0 && visitCount < maxTokenVisitCount) {
            Integer token = lhsDefaultTokenTypes.get(counter);
            if (token == BallerinaParser.AT) {
                return true;
            }
            counter--;
            visitCount++;
        }
        
        return false;
    }

    /**
     * Returns a websocket service or not.
     *
     * Currently, both 'websocket' and 'http' services are attached into a common http:Listener.
     * Thus, need to distinguish both.
     *
     * @param service   service
     * @return  Optional boolean, empty when service has zero resources(ambiguous).
     */
    private Optional<Boolean> isWebSocketService(BLangService service) {
        List<BLangFunction> resources = service.getResources();
        if (resources.isEmpty()) {
            return Optional.empty();
        }
        BLangFunction resource = resources.get(0);
        if (!resource.requiredParams.isEmpty()) {
            BLangType typeNode = resource.requiredParams.get(0).typeNode;
            if (typeNode instanceof BLangUserDefinedType) {
                BLangUserDefinedType node = (BLangUserDefinedType) typeNode;
                if ("WebSocketCaller".equals(node.typeName.value)) {
                    return Optional.of(true);
                } else if ("Caller".equals(node.typeName.value)) {
                    return Optional.of(false);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Boolean> isWebSocketClientService(BLangService service) {
        List<BLangFunction> resources = service.getResources();
        if (resources.isEmpty()) {
            return Optional.empty();
        }
        BLangFunction resource = resources.get(0);
        if (!resource.requiredParams.isEmpty()) {
            BLangType typeNode = resource.requiredParams.get(0).typeNode;
            if (typeNode instanceof BLangUserDefinedType) {
                BLangUserDefinedType node = (BLangUserDefinedType) typeNode;
                if ("WebSocketClient".equals(node.typeName.value)) {
                    return Optional.of(true);
                } else if ("WebSocketCaller".equals(node.typeName.value)) {
                    return Optional.of(false);
                }
            }
        }
        return Optional.empty();
    }

    private void addIfNotExists(SnippetBlock snippet, BLangService service, List<CompletionItem> items, LSContext ctx) {
        boolean found = false;
        for (BLangFunction resource : service.getResources()) {
            if (snippet.getLabel().endsWith(resource.name.value + " " + ItemResolverConstants.RESOURCE)) {
                found = true;
            }
        }
        if (!found) {
            items.add(snippet.build(ctx));
        }
    }

    /**
     * Precedence for a given provider.
     * 
     * @since 1.0
     */
    public enum Precedence {
        LOW,
        HIGH
    }
}
