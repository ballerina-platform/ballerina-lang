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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Package;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.CompletionProvider;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.builder.ConstantCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.FunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.TypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.VariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.WorkerCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;

/**
 * Interface for completion item providers.
 *
 * @param <T> Provider's node type
 * @since 0.995.0
 */
public abstract class AbstractCompletionProvider<T extends Node> implements CompletionProvider<T> {

    private final List<Class<T>> attachmentPoints;

    protected Precedence precedence = Precedence.LOW;

    public AbstractCompletionProvider(List<Class<T>> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }

    public AbstractCompletionProvider(Class<T> attachmentPoint) {
        this.attachmentPoints = Collections.singletonList(attachmentPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<T>> getAttachmentPoints() {
        return this.attachmentPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Precedence getPrecedence() {
        return precedence;
    }

    @Override
    public boolean onPreValidation(CompletionContext context, T node) {
        return true;
    }

    @Override
    public void sort(CompletionContext context, T node, List<LSCompletionItem> completionItems) {
        for (LSCompletionItem item : completionItems) {
            CompletionItem cItem = item.getCompletionItem();
            int rank;
            if (item instanceof SnippetCompletionItem) {
                rank = 1;
            } else if (item instanceof SymbolCompletionItem) {
                rank = 2;
            } else {
                rank = 3;
            }
            cItem.setSortText(SortingUtil.genSortText(rank));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(CompletionContext context, T node, List<LSCompletionItem> completionItems, Object... metaData) {
        this.sort(context, node, completionItems);
    }

    /**
     * Populate the completion item list by considering the.
     *
     * @param scopeEntries list of symbol information
     * @param ctx          Language server operation context
     * @return {@link List}     list of completion items
     */
    protected List<LSCompletionItem> getCompletionItemList(List<? extends Symbol> scopeEntries, CompletionContext ctx) {
        List<Symbol> processedSymbols = new ArrayList<>();
        List<LSCompletionItem> completionItems = new ArrayList<>();
        scopeEntries.forEach(symbol -> {
            if (processedSymbols.contains(symbol)) {
                return;
            }
            if (symbol.kind() == FUNCTION || symbol.kind() == METHOD) {
                completionItems.add(populateBallerinaFunctionCompletionItem(symbol, ctx));
            } else if (symbol.kind() == SymbolKind.CONSTANT) {
                CompletionItem constantCItem = ConstantCompletionItemBuilder.build((ConstantSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, constantCItem));
            } else if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol varSymbol = (VariableSymbol) symbol;
                TypeSymbol typeDesc = (varSymbol).typeDescriptor();
                String typeName = typeDesc.signature();
                CompletionItem variableCItem = VariableCompletionItemBuilder.build(varSymbol, symbol.name(), typeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, variableCItem));
            } else if (symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS) {
                // Here skip all the package symbols since the package is added separately
                CompletionItem typeCItem = TypeCompletionItemBuilder.build(symbol, symbol.name());
                completionItems.add(new SymbolCompletionItem(ctx, symbol, typeCItem));
            } else if (symbol.kind() == SymbolKind.WORKER) {
                CompletionItem workerItem = WorkerCompletionItemBuilder.build((WorkerSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, workerItem));
            }
            processedSymbols.add(symbol);
        });
        return completionItems;
    }

    /**
     * Get the type completion Items.
     *
     * @param context LS Operation Context
     * @return {@link List}     List of completion items
     */
    protected List<LSCompletionItem> getTypeItems(CompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(bSymbol -> {
            if (bSymbol.kind() == SymbolKind.TYPE_DEFINITION || bSymbol.kind() == SymbolKind.CLASS) {
                CompletionItem cItem = TypeCompletionItemBuilder.build(bSymbol, bSymbol.name());
                completionItems.add(new SymbolCompletionItem(context, bSymbol, cItem));
            }
        });

        completionItems.addAll(this.getBasicAndOtherTypeCompletions(context));
        completionItems.add(new TypeCompletionItem(context, null, Snippet.TYPE_MAP.get().build(context)));

        completionItems.add(CommonUtil.getErrorTypeCompletionItem(context));
        completionItems.addAll(Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_RECORD.get()),
                new SnippetCompletionItem(context, Snippet.DEF_RECORD_TYPE_DESC.get()),
                new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get()),
                new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()),
                new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get())
        ));

        return completionItems;
    }

    /**
     * Filter all the types in the Module.
     *
     * @param moduleSymbol package symbol
     * @return {@link List} list of filtered type entries
     */
    @Deprecated
    protected List<Symbol> filterTypesInModule(ModuleSymbol moduleSymbol) {
        List<Symbol> typeDefs = new ArrayList<>();
        typeDefs.addAll(moduleSymbol.typeDefinitions());
        typeDefs.addAll(moduleSymbol.classes());
        return typeDefs;
    }

    /**
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<LSCompletionItem> getModuleCompletionItems(CompletionContext ctx) {
        // First we include the packages from the imported list.
        List<String> populatedList = new ArrayList<>();
        List<ImportDeclarationNode> currentModuleImports = ctx.currentDocImports();
        List<LSCompletionItem> completionItems = currentModuleImports.stream()
                .map(importNode -> {
                    String orgName = importNode.orgName().isEmpty()
                            ? "" : importNode.orgName().get().orgName().text();
                    String pkgName = importNode.moduleName().stream()
                            .map(Token::text)
                            .collect(Collectors.joining("."));
                    String prefix;
                    if (importNode.prefix().isEmpty()) {
                        prefix = importNode.moduleName().get(importNode.moduleName().size() - 1).text();
                    } else {
                        prefix = importNode.prefix().get().prefix().text();
                    }
                    String label = prefix;
                    String insertText = prefix;
                    // If the import is a langlib module and there isn't a user defined alias we add ' before
                    if ("ballerina".equals(orgName) && importNode.moduleName().get(0).text().equals("lang")
                            && pkgName.endsWith("." + importNode.prefix().get().prefix().text())
                            && this.appendSingleQuoteForPackageInsertText(ctx)) {
                        insertText = "'" + insertText;
                    }
                    CompletionItem item = new CompletionItem();
                    item.setLabel(label);
                    item.setInsertText(insertText);
                    item.setDetail(ItemResolverConstants.MODULE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return new SymbolCompletionItem(ctx, null, item);
                }).collect(Collectors.toList());

        List<Package> packages = LSPackageLoader.getDistributionRepoPackages();
        // TODO: Refactor to match the latest project structure
//        packages.addAll(LSPackageLoader.getCurrentProjectModules(currentPkg, ctx));
        packages.forEach(pkg -> {
            String name = pkg.packageName().value();
            String orgName = pkg.packageOrg().value();
            boolean pkgAlreadyImported = currentModuleImports.stream()
                    .anyMatch(importPkg -> importPkg.orgName().isPresent()
                            && importPkg.orgName().get().orgName().text().equals(orgName)
                            && CommonUtil.getPackageNameComponentsCombined(importPkg).equals(name));
            if (!pkgAlreadyImported && !populatedList.contains(orgName + "/" + name)
                    && !this.isPreDeclaredLangLib(pkg)) {
                CompletionItem item = new CompletionItem();
                item.setLabel(CommonUtil.getPackageLabel(pkg));
                String[] pkgNameComps = name.split("\\.");
                String insertText = pkgNameComps[pkgNameComps.length - 1];
                // Check for the lang lib module insert text
                if ("ballerina".equals(orgName) && name.startsWith("lang.")) {
                    String[] pkgNameComponents = name.split("\\.");
                    insertText = "'" + pkgNameComponents[pkgNameComponents.length - 1];
                }
                item.setInsertText(insertText);
                item.setDetail(ItemResolverConstants.MODULE_TYPE);
                item.setKind(CompletionItemKind.Module);
                item.setAdditionalTextEdits(CommonUtil.getAutoImportTextEdits(orgName, name, ctx));
                StaticCompletionItem.Kind kind;
                if (orgName.equals("ballerina") && name.startsWith("lang.")) {
                    kind = StaticCompletionItem.Kind.LANG_LIB_MODULE;
                } else {
                    kind = StaticCompletionItem.Kind.MODULE;
                }
                completionItems.add(new StaticCompletionItem(ctx, item, kind));
            }
        });

        List<LSCompletionItem> langlibModules = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                .filter(symbol -> {
                    ModuleID moduleID = symbol.moduleID();
                    return symbol.kind() == MODULE && moduleID.orgName().equals("ballerina")
                            && moduleID.moduleName().startsWith("lang.")
                            && CommonUtil.PRE_DECLARED_LANG_LIBS.contains(moduleID.moduleName());
                })
                .map(symbol -> this.getLangLibCompletionItem(symbol.moduleID(), ctx))
                .collect(Collectors.toList());
        completionItems.addAll(langlibModules);

        return completionItems;
    }

    /**
     * Get the Resource Snippets.
     *
     * @param ctx Language Server Context
     * @return {@link Optional} Completion List
     */
    protected List<LSCompletionItem> getResourceSnippets(CompletionContext ctx) {
        // TODO: Fix after the service refactor
        BLangNode symbolEnvNode = null;
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

    protected boolean onQualifiedNameIdentifier(CompletionContext context, Node node) {
        if (node.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        int colonPos = ((QualifiedNameReferenceNode) node).colon().textRange().startOffset();
        int cursor = context.getCursorPositionInTree();

        return colonPos < cursor;
    }

    private void addAllWSClientResources(CompletionContext ctx, List<LSCompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CS_CLOSE.get(), service, items, ctx);
    }

    private void addAllWSResources(CompletionContext ctx, List<LSCompletionItem> items, BLangService service) {
        addIfNotExists(Snippet.DEF_RESOURCE_WS_OPEN.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_TEXT.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_BINARY.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PING.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_PONG.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_IDLE.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_ERROR.get(), service, items, ctx);
        addIfNotExists(Snippet.DEF_RESOURCE_WS_CLOSE.get(), service, items, ctx);
    }

    /**
     * Get the implicit new expression completion item.
     *
     * @param objectType object type symbol
     * @param context    Language server operation context
     * @return {@link LSCompletionItem} generated
     */
    protected LSCompletionItem getImplicitNewCompletionItem(ObjectTypeSymbol objectType, CompletionContext context) {
        CompletionItem cItem = FunctionCompletionItemBuilder.build(objectType,
                FunctionCompletionItemBuilder.InitializerBuildMode.IMPLICIT, context);

        MethodSymbol initMethod = null;
        if (objectType.kind() == SymbolKind.CLASS) {
            ClassSymbol classSymbol = (ClassSymbol) objectType;
            initMethod = classSymbol.initMethod().isPresent() ? classSymbol.initMethod().get() : null;
        }

        return new SymbolCompletionItem(context, initMethod, cItem);
    }

    protected LSCompletionItem getExplicitNewCompletionItem(ObjectTypeSymbol objectType, CompletionContext context) {
        CompletionItem cItem = FunctionCompletionItemBuilder.build(objectType,
                FunctionCompletionItemBuilder.InitializerBuildMode.EXPLICIT, context);

        MethodSymbol initMethod = null;
        if (objectType.kind() == SymbolKind.CLASS) {
            ClassSymbol classSymbol = (ClassSymbol) objectType;
            initMethod = classSymbol.initMethod().isPresent() ? classSymbol.initMethod().get() : null;
        }

        return new SymbolCompletionItem(context, initMethod, cItem);
    }

    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param symbol symbol Entry
     * @return completion item
     */
    private LSCompletionItem populateBallerinaFunctionCompletionItem(Symbol symbol, CompletionContext context) {
        if (symbol.kind() != SymbolKind.FUNCTION && symbol.kind() != SymbolKind.METHOD) {
            return null;
        }
        CompletionItem completionItem = FunctionCompletionItemBuilder.build((FunctionSymbol) symbol, context);
        return new SymbolCompletionItem(context, symbol, completionItem);
    }

    /**
     * Whether the cursor is located at the qualified name reference context.
     * This is added in order to support both of the following cases,
     * Ex:
     * (1) ... http:<cursor>
     * (2) ... http:a<cursor>
     *
     * @param tokenAtCursor Token at cursor
     * @param nodeAtCursor  Node at cursor
     * @return {@link Boolean} status
     */
    @Deprecated
    protected boolean qualifiedNameReferenceContext(Token tokenAtCursor, Node nodeAtCursor) {
        return nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE
                || tokenAtCursor.text().equals(SyntaxKind.COLON_TOKEN.stringValue());
    }

    protected List<LSCompletionItem> actionKWCompletions(CompletionContext context) {
        /*
        Add the start keywords of the following actions.
        start, wait, flush, check, check panic, trap, query action (query pipeline starts with from keyword)
         */
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_START.get()),
                new SnippetCompletionItem(context, Snippet.KW_WAIT.get()),
                new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()),
                new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                new SnippetCompletionItem(context, Snippet.KW_CHECK.get()),
                new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get())
        );
    }

    protected List<LSCompletionItem> expressionCompletions(CompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        /*
        check and check panic expression starts with check and check panic keywords, Which has been added with actions.
        query pipeline starts with from keyword and also being added with the actions
         */
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TABLE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
        // to support start of string template expression
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_STRING.get()));
        // to support start of xml template expression
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XML.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ERROR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_OBJECT_CONSTRUCTOR.get()));

        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol instanceof VariableSymbol || symbol.kind() == FUNCTION)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        // TODO: anon function expressions, 
        return completionItems;
    }

    protected boolean isPreDeclaredLangLib(Package pkg) {
        return "ballerina".equals(pkg.packageOrg().value())
                && CommonUtil.PRE_DECLARED_LANG_LIBS.contains(pkg.packageName().value());
    }

    private List<LSCompletionItem> getBasicAndOtherTypeCompletions(CompletionContext context) {
        List<String> types = Arrays.asList("float", "xml", "readonly", "handle", "never", "decimal", "string", "stream",
                "json", "table", "anydata", "any", "int", "boolean", "future", "service", "typedesc", "byte");
        List<LSCompletionItem> completionItems = new ArrayList<>();
        types.forEach(type -> {

            CompletionItem cItem = TypeCompletionItemBuilder.build(null, type);
            completionItems.add(new SymbolCompletionItem(context, null, cItem));
        });

        return completionItems;
    }

    /**
     * Returns a websocket service or not.
     * <p>
     * Currently, both 'websocket' and 'http' services are attached into a common http:Listener.
     * Thus, need to distinguish both.
     *
     * @param service service
     * @return Optional boolean, empty when service has zero resources(ambiguous).
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

    private boolean appendSingleQuoteForPackageInsertText(CompletionContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        return !(nodeAtCursor != null && nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                ((SimpleNameReferenceNode) nodeAtCursor).name().text().startsWith("'"));
    }

    private void addIfNotExists(SnippetBlock snippet, BLangService service, List<LSCompletionItem> items,
                                CompletionContext ctx) {
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

    private LSCompletionItem getLangLibCompletionItem(ModuleID moduleID, CompletionContext context) {
        CompletionItem item = new CompletionItem();
        item.setLabel(moduleID.orgName() + "/" + moduleID.moduleName());
        String insertText = "'" + moduleID.modulePrefix();
        item.setInsertText(insertText);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);
        item.setKind(CompletionItemKind.Module);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.LANG_LIB_MODULE);
    }
}
