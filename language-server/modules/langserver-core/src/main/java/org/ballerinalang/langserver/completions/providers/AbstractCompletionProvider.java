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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Package;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.builder.ConstantCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.FunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.TypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.VariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.WorkerCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.XMLNSCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.XMLNS;

/**
 * Interface for completion item providers.
 *
 * @param <T> Provider's node type
 * @since 0.995.0
 */
public abstract class AbstractCompletionProvider<T extends Node> implements BallerinaCompletionProvider<T> {

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
    public boolean onPreValidation(BallerinaCompletionContext context, T node) {
        return true;
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        SortingUtil.toDefaultSorting(context, completionItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems,
                     Object... metaData) {
        this.sort(context, node, completionItems);
    }

    /**
     * Populate the completion item list by considering the.
     *
     * @param scopeEntries list of symbol information
     * @param ctx          Language server operation context
     * @return {@link List}     list of completion items
     */
    protected List<LSCompletionItem> getCompletionItemList(List<? extends Symbol> scopeEntries,
                                                           BallerinaCompletionContext ctx) {
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
                String typeName = typeDesc == null ? "" : typeDesc.signature();
                CompletionItem variableCItem = VariableCompletionItemBuilder.build(varSymbol, symbol.name(), typeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, variableCItem));
            } else if (symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS) {
                // Here skip all the package symbols since the package is added separately
                CompletionItem typeCItem = TypeCompletionItemBuilder.build(symbol, symbol.name());
                completionItems.add(new SymbolCompletionItem(ctx, symbol, typeCItem));
            } else if (symbol.kind() == SymbolKind.WORKER) {
                CompletionItem workerItem = WorkerCompletionItemBuilder.build((WorkerSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, workerItem));
            } else if (symbol.kind() == XMLNS) {
                CompletionItem xmlItem = XMLNSCompletionItemBuilder.build((XMLNamespaceSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, xmlItem));
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
    protected List<LSCompletionItem> getTypeItems(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(bSymbol -> {
            if (bSymbol.kind() == SymbolKind.TYPE_DEFINITION || bSymbol.kind() == SymbolKind.CLASS
                    || bSymbol.kind() == ENUM) {
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
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<LSCompletionItem> getModuleCompletionItems(BallerinaCompletionContext ctx) {
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
                    CompletionItem item = new CompletionItem();
                    item.setLabel(label);
                    item.setInsertText(insertText);
                    item.setDetail(ItemResolverConstants.MODULE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return new SymbolCompletionItem(ctx, null, item);
                }).collect(Collectors.toList());

        List<Package> packages = LSPackageLoader.getInstance(ctx.languageServercontext()).getDistributionRepoPackages();
        // TODO: Refactor to match the latest project structure
//        packages.addAll(LSPackageLoader.getCurrentProjectModules(currentPkg, ctx));
        packages.forEach(pkg -> {
            String name = pkg.packageName().value();
            String orgName = pkg.packageOrg().value();
            if (!CommonUtil.matchingImportedModule(ctx, pkg) && !populatedList.contains(orgName + "/" + name)
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

    protected boolean onQualifiedNameIdentifier(BallerinaCompletionContext context, Node node) {
        if (node.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        int colonPos = ((QualifiedNameReferenceNode) node).colon().textRange().startOffset();
        int cursor = context.getCursorPositionInTree();

        return colonPos < cursor;
    }

    protected LSCompletionItem getExplicitNewCompletionItem(ClassSymbol clsSymbol, BallerinaCompletionContext context) {
        CompletionItem cItem = FunctionCompletionItemBuilder.build(clsSymbol,
                FunctionCompletionItemBuilder.InitializerBuildMode.EXPLICIT, context);
        MethodSymbol initMethod = clsSymbol.initMethod().isPresent() ? clsSymbol.initMethod().get() : null;

        return new SymbolCompletionItem(context, initMethod, cItem);
    }

    /**
     * Get the implicit new expression completion item.
     *
     * @param classSymbol object type symbol
     * @param context     Language server operation context
     * @return {@link LSCompletionItem} generated
     */
    protected LSCompletionItem getImplicitNewCompletionItem(ClassSymbol classSymbol,
                                                            BallerinaCompletionContext context) {
        CompletionItem cItem = FunctionCompletionItemBuilder.build(classSymbol,
                FunctionCompletionItemBuilder.InitializerBuildMode.IMPLICIT, context);
        MethodSymbol initMethod = classSymbol.initMethod().isPresent() ? classSymbol.initMethod().get() : null;

        return new SymbolCompletionItem(context, initMethod, cItem);
    }

    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param symbol symbol Entry
     * @return completion item
     */
    private LSCompletionItem populateBallerinaFunctionCompletionItem(Symbol symbol,
                                                                     BallerinaCompletionContext context) {
        if (symbol.kind() != SymbolKind.FUNCTION && symbol.kind() != SymbolKind.METHOD) {
            return null;
        }
        CompletionItem completionItem = FunctionCompletionItemBuilder.build((FunctionSymbol) symbol, context);
        return new SymbolCompletionItem(context, symbol, completionItem);
    }

    protected List<LSCompletionItem> actionKWCompletions(BallerinaCompletionContext context) {
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

    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context) {
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
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ERROR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_OBJECT_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE16_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE64_LITERAL.get()));

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

    private List<LSCompletionItem> getBasicAndOtherTypeCompletions(BallerinaCompletionContext context) {
        List<String> types = Arrays.asList("float", "xml", "readonly", "handle", "never", "decimal", "string", "stream",
                "json", "table", "anydata", "any", "int", "boolean", "future", "service", "typedesc", "byte");
        List<LSCompletionItem> completionItems = new ArrayList<>();
        types.forEach(type -> {

            CompletionItem cItem = TypeCompletionItemBuilder.build(null, type);
            completionItems.add(new SymbolCompletionItem(context, null, cItem));
        });

        return completionItems;
    }

    private LSCompletionItem getLangLibCompletionItem(ModuleID moduleID, BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        item.setLabel(moduleID.orgName() + "/" + moduleID.moduleName());
        String insertText = "'" + moduleID.modulePrefix();
        item.setInsertText(insertText);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);
        item.setKind(CompletionItemKind.Module);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.LANG_LIB_MODULE);
    }
}
