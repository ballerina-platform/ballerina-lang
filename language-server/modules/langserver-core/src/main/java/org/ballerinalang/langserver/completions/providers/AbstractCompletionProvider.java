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
package org.ballerinalang.langserver.completions.providers;

import org.antlr.v4.runtime.CommonToken;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.completions.LSCompletionProviderHolder;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.BConstantCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
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
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
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
public abstract class AbstractCompletionProvider implements LSCompletionProvider {

    protected List<Class> attachmentPoints = new ArrayList<>();

    protected Precedence precedence = Precedence.LOW;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<LSCompletionItem> getCompletions(LSContext context) throws LSCompletionException;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class> getAttachmentPoints() {
        return this.attachmentPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Precedence getPrecedence() {
        return precedence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LSCompletionProvider> getContextProvider(LSContext ctx) {
        return Optional.empty();
    }

    /**
     * Populate the completion item list by considering the.
     *
     * @param scopeEntries list of symbol information
     * @param context        Language server operation context
     * @return {@link List}     list of completion items
     */
    protected List<LSCompletionItem> getCompletionItemList(List<Scope.ScopeEntry> scopeEntries, LSContext context) {
        List<BSymbol> processedSymbols = new ArrayList<>();
        List<LSCompletionItem> completionItems = new ArrayList<>();
        scopeEntries.removeIf(CommonUtil.invalidSymbolsPredicate());
        scopeEntries.forEach(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            if (processedSymbols.contains(symbol)) {
                return;
            }
            Optional<BSymbol> bTypeSymbol;
            if (CommonUtil.isValidInvokableSymbol(symbol)) {
                completionItems.add(populateBallerinaFunctionCompletionItem(scopeEntry, context));
            } else if (symbol instanceof BConstantSymbol) {
                CompletionItem constantCItem = BConstantCompletionItemBuilder.build((BConstantSymbol) symbol, context);
                completionItems.add(new SymbolCompletionItem(context, symbol, constantCItem));
            } else if (!(symbol instanceof BInvokableSymbol) && symbol instanceof BVarSymbol) {
                String typeName = CommonUtil.getBTypeName(symbol.type, context, false);
                CompletionItem variableCItem = BVariableCompletionItemBuilder
                        .build((BVarSymbol) symbol, scopeEntry.symbol.name.value, typeName);
                completionItems.add(new SymbolCompletionItem(context, symbol, variableCItem));
            } else if ((bTypeSymbol = FilterUtils.getBTypeEntry(scopeEntry)).isPresent()) {
                // Here skip all the package symbols since the package is added separately
                CompletionItem typeCItem = BTypeCompletionItemBuilder.build(bTypeSymbol.get(),
                        scopeEntry.symbol.name.value);
                completionItems.add(new SymbolCompletionItem(context, bTypeSymbol.get(), typeCItem));
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
    protected List<LSCompletionItem> getCompletionItemList(Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> list,
                                                         LSContext context) {
        return list.isLeft() ? list.getLeft() : this.getCompletionItemList(list.getRight(), context);
    }

    /**
     * Get the basic types.
     *
     * @param context LS Operation Context
     * @param visibleSymbols List of visible symbols
     * @return {@link List}     List of completion items
     */
    protected List<LSCompletionItem> getBasicTypesItems(LSContext context, List<Scope.ScopeEntry> visibleSymbols) {
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(scopeEntry -> {
            BSymbol bSymbol = scopeEntry.symbol;
            if (((bSymbol instanceof BConstructorSymbol && Names.ERROR.equals(bSymbol.name)))
                    || (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol))) {
                BSymbol symbol = bSymbol;
                if (bSymbol instanceof BConstructorSymbol) {
                    symbol = ((BConstructorSymbol) bSymbol).type.tsymbol;
                }
                CompletionItem cItem = BTypeCompletionItemBuilder.build(symbol, scopeEntry.symbol.name.getValue());
                completionItems.add(new SymbolCompletionItem(context, symbol, cItem));
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
    protected List<LSCompletionItem> getTypeItemsInPackage(List<Scope.ScopeEntry> visibleSymbols, String pkgName,
                                                         LSContext ctx) {
        List<Scope.ScopeEntry> filteredList = new ArrayList<>();
        Optional<Scope.ScopeEntry> pkgSymbolInfo = visibleSymbols.stream()
                .filter(scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return symbol instanceof BPackageSymbol && scopeEntry.symbol.name.getValue().equals(pkgName);
                })
                .findAny();
        pkgSymbolInfo.ifPresent(pkgEntry -> {
            BSymbol pkgSymbol = pkgEntry.symbol;
            pkgSymbol.scope.entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BTypeSymbol || (scopeEntry.symbol instanceof BConstructorSymbol
                        && Names.ERROR.equals(scopeEntry.symbol.name))) {
                    filteredList.add(scopeEntry);
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
    protected List<LSCompletionItem> addTopLevelItems(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IMPORT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_MAIN_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WEBSOCKET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WS_CLIENT.get()));
//        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WEBSUB));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_GRPC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ANNOTATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_SNIPPET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CONST.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LISTENER.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        return completionItems;
    }

    /**
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<LSCompletionItem> getPackagesCompletionItems(LSContext ctx) {
        // First we include the packages from the imported list.
        List<String> populatedList = new ArrayList<>();
        BLangPackage currentPkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        List<BLangImportPackage> currentModuleImports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        List<LSCompletionItem> completionItems = currentModuleImports.stream()
                .map(pkg -> {
                    String orgName = pkg.orgName.value;
                    String pkgName = pkg.pkgNameComps.stream()
                            .map(id -> id.value)
                            .collect(Collectors.joining("."));
                    String label = pkg.alias.value;
                    String insertText = pkg.alias.value;
                    // If the import is a langlib module and there isn't a user defined alias we add ' before
                    if ("ballerina".equals(orgName) && pkg.pkgNameComps.get(0).getValue().equals("lang")
                            && pkgName.endsWith("." + pkg.alias.value)
                            && this.appendSingleQuoteForPackageInsertText(ctx)) {
                        insertText = "'" + insertText;
                    }
                    CompletionItem item = new CompletionItem();
                    item.setLabel(label);
                    item.setInsertText(insertText);
                    item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return new SymbolCompletionItem(ctx, pkg.symbol, item);
                }).collect(Collectors.toList());

        List<BallerinaPackage> packages = LSPackageLoader.getSdkPackages();
        packages.addAll(LSPackageLoader.getHomeRepoPackages());
        packages.addAll(LSPackageLoader.getCurrentProjectModules(currentPkg, ctx));
        packages.forEach(pkg -> {
            String name = pkg.getPackageName();
            String orgName = pkg.getOrgName();
            boolean pkgAlreadyImported = currentModuleImports.stream()
                    .anyMatch(importPkg -> importPkg.orgName.value.equals(orgName)
                            && importPkg.alias.value.equals(name));
            if (!pkgAlreadyImported && !populatedList.contains(orgName + "/" + name)) {
                CompletionItem item = new CompletionItem();
                item.setLabel(pkg.getFullPackageNameAlias());
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
                completionItems.add(new StaticCompletionItem(ctx, item));
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
    protected List<LSCompletionItem> getDelimiterBasedCompletionItems(LSContext context) {
        Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> itemList =
                SymbolFilters.get(DelimiterBasedContentFilter.class)
                .filterItems(context);
        return this.getCompletionItemList(itemList, context);
    }

    /**
     * Predicate to filter the attached of Self, symbol.
     *
     * @return {@link Predicate} Symbol filter predicate
     */
    protected Predicate<Scope.ScopeEntry> attachedSymbolFilter() {
        return scopeEntry -> {
            BSymbol bSymbol = scopeEntry.symbol;
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
     * @return {@link AbstractCompletionProvider} Completion Provider
     */
    protected LSCompletionProvider getProvider(Class providerKey) {
        return LSCompletionProviderHolder.getInstance().getProvider(providerKey);
    }

    protected List<LSCompletionItem> getCompletionItemsAfterOnKeyword(LSContext ctx) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Scope.ScopeEntry> filtered = this.filterListenerVariables(visibleSymbols);
        List<LSCompletionItem> completionItems =
                new ArrayList<>(this.getCompletionItemList(new ArrayList<>(filtered), ctx));
        completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_NEW.get()));

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
    protected List<LSCompletionItem> getVarDefExpressionCompletions(LSContext context, boolean onGlobal) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Integer invocationType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        try {
            Optional<BLangType> assignmentType = getAssignmentType(context, onGlobal);
            
            if (assignmentType.isPresent() && !(assignmentType.get() instanceof BLangUserDefinedType)
                    && invocationType > -1) {
                Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
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
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_VAR.get()));
            } else if (assignmentType.isPresent() && assignmentType.get() instanceof BLangUserDefinedType) {
                completionItems.addAll(
                        getUserDefinedTypeCompletions(context, (BLangUserDefinedType) assignmentType.get()));
            } else if (isTypeDesc) {
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
                completionItems.addAll(getVarDefCompletions(context));
            } else if (invocationType > -1) {
                Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
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

    protected List<LSCompletionItem> getUserDefinedTypeCompletions(LSContext context, BLangUserDefinedType type) {
        List<Integer> defaultTokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        Integer invocationType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        List<LSCompletionItem> completionItems = new ArrayList<>();
        int newTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.NEW);
        int lastColonIndex = defaultTokenTypes.lastIndexOf(BallerinaParser.COLON);
        int firstColonIndex = defaultTokenTypes.indexOf(BallerinaParser.COLON);
        String typeName = type.typeName.value;
        String pkgAlias = type.pkgAlias.value;
        BSymbol bSymbol;

        Optional<Scope.ScopeEntry> pkgSymbol = this.getPackageSymbolFromAlias(context, pkgAlias);
        if (pkgSymbol.isPresent()) {
            Optional<Scope.ScopeEntry> entry = pkgSymbol.get().symbol.scope.entries.values().stream()
                    .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(typeName)).findAny();
            
            if (!entry.isPresent()) {
                return completionItems;
            }
            bSymbol = entry.get().symbol;
        } else {
            List<Scope.ScopeEntry> typeSymbol = getSymbolByName(typeName, context);
            if (typeSymbol.isEmpty()) {
                return completionItems;
            }
            bSymbol = typeSymbol.get(0).symbol;
        }
        
        if (bSymbol == null) {
            return completionItems;
        }
        
        if (bSymbol instanceof BRecordTypeSymbol) {
            if (invocationType > -1) {
                Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                return this.getCompletionItemList(filteredList, context);
            }
            return getVarDefCompletions(context);
        }
        if (!(bSymbol instanceof BObjectTypeSymbol)) {
            if (invocationType > -1) {
                Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
                return this.getCompletionItemList(filteredList, context);
            }
            return getVarDefCompletions(context);
        }
        
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bSymbol;
        BAttachedFunction initFunction = objectTypeSymbol.initializerFunc;
        BInvokableSymbol initFuncSymbol = initFunction == null ? null : initFunction.symbol;
        
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
            Pair<String, String> newWithTypeSign = getFunctionInvocationSignature(initFuncSymbol, typeName,
                    context);
            CompletionItem cItem = BFunctionCompletionItemBuilder.build(initFuncSymbol, newWithTypeSign.getRight(),
                    newWithTypeSign.getLeft(), context);
            completionItems.add(new SymbolCompletionItem(context, initFuncSymbol, cItem));
            return completionItems;
        } else if (invocationType > -1) {
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
            return this.getCompletionItemList(filteredList, context);
        }
        
        CompletionItem newCItem;
        CompletionItem typeCItem;
        if (initFunction == null) {
            newCItem = BFunctionCompletionItemBuilder.build(null, "new()", "new();", context);
            typeCItem = BFunctionCompletionItemBuilder.build(null, typeName + "()",
                                                             typeName + "();", context);
            completionItems.add(new SymbolCompletionItem(context, null, newCItem));
            completionItems.add(new SymbolCompletionItem(context, null, typeCItem));
        } else {
            Pair<String, String> newSign = CommonUtil.getFunctionInvocationSignature(initFunction.symbol,
                                                                                     CommonKeys.NEW_KEYWORD_KEY,
                                                                                     context);
            Pair<String, String> newWithTypeSign = CommonUtil.getFunctionInvocationSignature(initFunction.symbol,
                                                                                             typeName,
                                                                                             context);
            newCItem = BFunctionCompletionItemBuilder.build(initFunction.symbol,
                                                            newSign.getRight(),
                                                            newSign.getLeft(), context);
            typeCItem = BFunctionCompletionItemBuilder.build(initFunction.symbol,
                                                             newWithTypeSign.getRight(),
                                                             newWithTypeSign.getLeft(), context);
            completionItems.add(new SymbolCompletionItem(context, initFunction.symbol, newCItem));
            completionItems.add(new SymbolCompletionItem(context, initFunction.symbol, typeCItem));
        }
        completionItems.addAll(getVarDefCompletions(context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
    
        return completionItems;
    }

    /**
     * Get the Resource Snippets.
     * 
     * @param ctx Language Server Context
     * @return {@link Optional} Completion List
     */
    protected List<LSCompletionItem> getResourceSnippets(LSContext ctx) {
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);
        List<LSCompletionItem> items = new ArrayList<>();
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
                items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_HTTP.get()));
                items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_COMMON.get()));
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
                            items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_HTTP.get()));
                        }
                    } else {
                        // Is ambiguous, suggest both 'ws' and 'http'
                        addAllWSResources(ctx, items, service);
                        items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_HTTP.get()));
                    }
                    break;
                }
                return items;
            case "grpc":
                items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_GRPC.get()));
                break;
//            case "websub":
//                addAllWebsubResources(ctx, items, service);
//                break;
            default:
                items.add(new SnippetCompletionItem(ctx, Snippet.DEF_RESOURCE_COMMON.get()));
                return items;
        }
        return items;
    }

//    private void addAllWebsubResources(LSContext ctx, List<CompletionItem> items, BLangService service) {
//        addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_INTENT.get(), service, items, ctx);
//        addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_NOTIFY.get(), service, items, ctx);
//    }

    private void addAllWSClientResources(LSContext ctx, List<LSCompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_CLOSE.get(), service, items, ctx);
    }

    private void addAllWSResources(LSContext ctx, List<LSCompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_OPEN.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CLOSE.get(), service, items, ctx);
    }


    protected Optional<Scope.ScopeEntry> getPackageSymbolFromAlias(LSContext context, String alias) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<BLangImportPackage> pkgForAlias = context.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
                .filter(pkg -> pkg.alias.value.equals(alias))
                .findAny();
        if (alias.isEmpty() || !pkgForAlias.isPresent()) {
            return Optional.empty();
        }
        return visibleSymbols.stream()
                .filter(scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return symbol == pkgForAlias.get().symbol;
                })
                .findAny();
    }

    protected List<Scope.ScopeEntry> getSymbolByName(String name, LSContext context) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        return visibleSymbols.parallelStream()
                .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(name))
                .collect(Collectors.toList());
    }

    protected boolean inFunctionReturnParameterContext(LSContext context) {
        if (context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY) == null) {
            return false;
        }
        List<Integer> defaultTokens = new ArrayList<>(context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY));
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
        List<Integer> defaultTokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        int annotationAccessIndex = defaultTokenTypes.indexOf(BallerinaParser.ANNOTATION_ACCESS);

        return annotationAccessIndex > -1;
    }
    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param scopeEntry - symbol Entry
     * @return completion item
     */
    private LSCompletionItem populateBallerinaFunctionCompletionItem(Scope.ScopeEntry scopeEntry, LSContext context) {
        BSymbol bSymbol = scopeEntry.symbol;
        if (!(bSymbol instanceof BInvokableSymbol)) {
            return null;
        }
        CompletionItem completionItem = BFunctionCompletionItemBuilder.build((BInvokableSymbol) bSymbol, context);
        return new SymbolCompletionItem(context, bSymbol, completionItem);
    }

    private List<Scope.ScopeEntry> filterListenerVariables(List<Scope.ScopeEntry> scopeEntries) {
        return scopeEntries.stream()
                .filter(scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return symbol instanceof BVarSymbol && CommonUtil.isListenerObject(symbol.type.tsymbol);
                })
                .collect(Collectors.toList());
    }

    public static Optional<BLangType> getAssignmentType(LSContext context, boolean onGlobal)
            throws LSCompletionException {

        List<CommonToken> lhsTokens = context.get(SourcePruneKeys.LHS_TOKENS_KEY);
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
            throw new LSCompletionException("Error while parsing the sub-rule", e);
        }

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        BLangType typeNode;
        
        if (onGlobal) {
            typeNode = bLangPackage.get().globalVars.get(0).getTypeNode();
        } else {
            typeNode = ((BLangSimpleVariableDef) ((BLangBlockFunctionBody) bLangPackage.get().getFunctions().get(0)
                    .funcBody).stmts.get(0)).getVariable().typeNode;
        }

        return Optional.ofNullable(typeNode);
    }

    private void fillFunctionWithBodySnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                             List<LSCompletionItem> completionItems)
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
        completionItems.add(new SnippetCompletionItem(context, snippetBlock));
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
                                           List<LSCompletionItem> completionItems) throws LSCompletionException {
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
        completionItems.add(new SnippetCompletionItem(context, snippetBlock));
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
    public List<LSCompletionItem> getVarDefCompletions(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        visibleSymbols.removeIf(scopeEntry -> {
            BSymbol bSymbol = scopeEntry.symbol;
            return (bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || (FilterUtils.isBTypeEntry(scopeEntry))
                    || (bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
        });
        completionItems.addAll(getCompletionItemList(visibleSymbols, context));
        // Add the packages completion items.
        completionItems.addAll(getPackagesCompletionItems(context));
        // Add the check keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        // Add the checkpanic keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        // Add the wait keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        // Add the start keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        // Add the flush keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        // Add the untaint keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_UNTAINT.get()));
        // Add But keyword item
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_MATCH.get()));
        // Add the trap expression keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRAP.get()));

        return completionItems;
    }

    /**
     * Check whether the current context is annotation context.
     *
     * @param context Language server context
     * @return {@link Boolean} whether the cursor is in the annotation context
     */
    protected boolean isAnnotationAttachmentContext(LSContext context) {
        List<Integer> lhsDefaultTokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
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
    
    private boolean appendSingleQuoteForPackageInsertText(LSContext context) {
        List<CommonToken> defaultTokens = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
        if (defaultTokens == null || defaultTokens.isEmpty()) {
            return false;
        }
        CommonToken lastToken = defaultTokens.get(defaultTokens.size() - 1);
        
        return !lastToken.getText().startsWith("'");
    }

    private void addIfNotExists(SnippetBlock snippet, BLangService service, List<LSCompletionItem> items,
                                LSContext ctx) {
        boolean found = false;
        for (BLangFunction resource : service.getResources()) {
            if (snippet.getLabel().endsWith(resource.name.value + " " + ItemResolverConstants.RESOURCE)) {
                found = true;
            }
        }
        if (!found) {
            items.add(new SnippetCompletionItem(ctx, snippet));
        }
    }

}
