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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;
import org.ballerinalang.langserver.completions.FunctionPointerCompletionItem;
import org.ballerinalang.langserver.completions.ObjectFieldCompletionItem;
import org.ballerinalang.langserver.completions.RecordFieldCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.ConstantCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.FieldCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.FunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.ParameterCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.StreamTypeInitCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.TypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.VariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.WorkerCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.XMLNSCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SnippetBlock;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.PATH_PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RESOURCE_METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE_DEFINITION;
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
            if (symbol.kind() == FUNCTION || symbol.kind() == METHOD || symbol.kind() == RESOURCE_METHOD) {
                FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
                if (functionSymbol.getName().isPresent() && !functionSymbol.getName().get().contains("$")) {
                    completionItems.addAll(populateBallerinaFunctionCompletionItems((FunctionSymbol) symbol, ctx));
                }
            } else if (symbol.kind() == SymbolKind.CONSTANT || symbol.kind() == SymbolKind.ENUM_MEMBER) {
                CompletionItem constantCItem = ConstantCompletionItemBuilder.build((ConstantSymbol) symbol, ctx);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, constantCItem));
            } else if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol varSymbol = (VariableSymbol) symbol;
                if (varSymbol.qualifiers().contains(Qualifier.ISOLATED) && !CommonUtil.withinLockStatementNode(ctx)) {
                    return;
                }
                TypeSymbol typeDesc = varSymbol.typeDescriptor();
                /*
                Null check added for safety.
                Eg: public listener test = <cursor>
                 */
                String typeName = (typeDesc == null || typeDesc.typeKind() == null) ? "" :
                        NameUtil.getModifiedTypeName(ctx, typeDesc);
                CompletionItem variableCItem = VariableCompletionItemBuilder.build(varSymbol,
                        varSymbol.getName().orElse(""), typeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, variableCItem));

                if (typeDesc != null) {
                    /*
                    Null check added for safety.
                    Eg: public listener test = <cursor>
                     */
                    TypeSymbol rawType = CommonUtil.getRawType(typeDesc);
                    completionItems.addAll(populateSelfClassSymbolCompletionItems(symbol, ctx, rawType));
                }
            } else if (symbol.kind() == PARAMETER) {
                ParameterSymbol paramSymbol = (ParameterSymbol) symbol;
                TypeSymbol typeDesc = paramSymbol.typeDescriptor();
                String typeName = NameUtil.getModifiedTypeName(ctx, typeDesc);
                CompletionItem variableCItem = ParameterCompletionItemBuilder.build(paramSymbol.getName().get(),
                        typeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, variableCItem));
            } else if (symbol.kind() == PATH_PARAMETER) {
                PathParameterSymbol paramSymbol = (PathParameterSymbol) symbol;
                TypeSymbol typeDesc = paramSymbol.typeDescriptor();
                String typeName = NameUtil.getModifiedTypeName(ctx, typeDesc);
                CompletionItem variableCItem = ParameterCompletionItemBuilder.build(paramSymbol.getName().get(),
                        typeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, variableCItem));
            } else if (symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS
                    || symbol.kind() == ENUM) {
                // Here skip all the package symbols since the package is added separately
                CompletionItem typeCItem = TypeCompletionItemBuilder.build(symbol, symbol.getName().get());
                completionItems.add(new SymbolCompletionItem(ctx, symbol, typeCItem));
            } else if (symbol.kind() == SymbolKind.WORKER) {
                CompletionItem workerItem = WorkerCompletionItemBuilder.build((WorkerSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, workerItem));
            } else if (symbol.kind() == XMLNS) {
                CompletionItem xmlItem = XMLNSCompletionItemBuilder.build((XMLNamespaceSymbol) symbol);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, xmlItem));
            } else if (symbol.kind() == RECORD_FIELD) {
                RecordFieldSymbol recordFieldSymbol = (RecordFieldSymbol) symbol;
                CompletionItem recFieldItem = FieldCompletionItemBuilder.build(recordFieldSymbol, ctx);
                completionItems.add(new RecordFieldCompletionItem(ctx, recordFieldSymbol, recFieldItem));
            } else if (symbol.kind() == OBJECT_FIELD || symbol.kind() == CLASS_FIELD) {
                ObjectFieldSymbol objectFieldSymbol = (ObjectFieldSymbol) symbol;
                CompletionItem objFieldItem = FieldCompletionItemBuilder.build(objectFieldSymbol, false);
                completionItems.add(new ObjectFieldCompletionItem(ctx, objectFieldSymbol, objFieldItem));
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
    private List<LSCompletionItem> getTypeItems(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.stream()
                .filter(CommonUtil.typesFilter())
                .forEach(symbol -> {
                    CompletionItem cItem = TypeCompletionItemBuilder.build(symbol, symbol.getName().get());
                    completionItems.add(new SymbolCompletionItem(context, symbol, cItem));
                });

        completionItems.addAll(this.getBasicAndOtherTypeCompletions(context));
        completionItems.addAll(Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()),
                new SnippetCompletionItem(context, Snippet.KW_RECORD.get()),
                new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()),
                new SnippetCompletionItem(context, Snippet.DEF_RECORD_TYPE_DESC.get()),
                new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get()),
                new SnippetCompletionItem(context, Snippet.KW_DISTINCT.get()),
                new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()),
                new SnippetCompletionItem(context, Snippet.KW_TRUE.get()),
                new SnippetCompletionItem(context, Snippet.KW_FALSE.get()),
                new SnippetCompletionItem(context, Snippet.KW_NIL.get())
        ));

        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (nodeAtCursor.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return completionItems;
        }
        String prefix = ((SimpleNameReferenceNode) nodeAtCursor).name().text().toLowerCase((Locale.ENGLISH));
        context.currentDocImportsMap().forEach((importDeclarationNode, moduleSymbol) -> {
            List<Symbol> symbols = moduleSymbol.allSymbols();
            CodeActionModuleId moduleId = CodeActionModuleId.from(importDeclarationNode);
            List<Symbol> filteredList = SymbolUtil.filterSymbolsByPrefix(symbols, context, prefix, moduleId);
            List<LSCompletionItem> completionItemList = getCompletionItemList(filteredList, context);

            for (LSCompletionItem lsCompletionItem : completionItemList) {
                CompletionItem completionItem = lsCompletionItem.getCompletionItem();
                String insertText = completionItem.getInsertText();
                String label = completionItem.getLabel();
                String moduleName = importDeclarationNode.prefix().isEmpty()
                        ? importDeclarationNode.moduleName().get(importDeclarationNode.moduleName().size() - 1).text()
                        : importDeclarationNode.prefix().get().prefix().text();
                completionItem.setInsertText(moduleName + ":" + insertText);
                completionItem.setLabel(moduleName + ":" + label);
                completionItems.add(lsCompletionItem);
            }
        });
        return completionItems;
    }

    /**
     * Get the type descriptor context completion items.
     * For this context, we usually get the types as well as the modules since we need to suggest the types in modules
     *
     * @param context Completion context
     * @return {@link List} of completion items
     */
    protected List<LSCompletionItem> getTypeDescContextItems(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));

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
        List<String> processedList = new ArrayList<>();
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = ctx.currentDocImportsMap();
        // Generate completion items for the import statements in the current document
        List<LSCompletionItem> completionItems = new ArrayList<>();
        currentDocImports.forEach((importNode, moduleSymbol) -> {
            String orgName = importNode.orgName().isEmpty() ? "" : importNode.orgName().get().orgName().text();
            String pkgName = importNode.moduleName().stream()
                    .map(Token::text)
                    .collect(Collectors.joining("."));

            if (CommonUtil.PRE_DECLARED_LANG_LIBS.contains(pkgName.replace("'", ""))) {
                // skip the predeclared langlib imports
                return;
            }
            String processedModuleHash = orgName.isEmpty() ? pkgName : orgName + CommonKeys.SLASH_KEYWORD_KEY + pkgName;
            String prefix;
            if (importNode.prefix().isEmpty()) {
                prefix = importNode.moduleName().get(importNode.moduleName().size() - 1).text();
            } else {
                prefix = importNode.prefix().get().prefix().text();
            }
            String label = prefix;
            String insertText = CommonUtil.escapeReservedKeyword(prefix);
            CompletionItem item = this.getModuleCompletionItem(label, insertText, new ArrayList<>(), prefix);
            processedList.add(processedModuleHash);
            completionItems.add(new SymbolCompletionItem(ctx, moduleSymbol, item));
        });

        // Generate completion items for the distribution repo packages excluding the pre-declared lang-libs
        List<LSPackageLoader.ModuleInfo> modules =
                LSPackageLoader.getInstance(ctx.languageServercontext()).getAllVisiblePackages(ctx);
        modules.forEach(pkg -> {
            String name = pkg.packageName().value();
            String orgName = ModuleUtil.escapeModuleName(pkg.packageOrg().value());
            if (ModuleUtil.matchingImportedModule(ctx, pkg).isEmpty()
                    && !processedList.contains(orgName + CommonKeys.SLASH_KEYWORD_KEY + name)
                    && !CommonUtil.PRE_DECLARED_LANG_LIBS.contains(name)) {
                List<String> pkgNameComps = Arrays.stream(name.split("\\."))
                        .map(ModuleUtil::escapeModuleName)
                        .map(CommonUtil::escapeReservedKeyword)
                        .collect(Collectors.toList());
                String label = pkg.packageOrg().value().isEmpty() ? String.join(".", pkgNameComps)
                        : CommonUtil.getPackageLabel(pkg);
                String aliasComponent = pkgNameComps.get(pkgNameComps.size() - 1);
                // TODO: 2021-04-23 This has to be revamped with completion/resolve request for faster responses 
                String insertText = CommonUtil.escapeReservedKeyword(NameUtil.getValidatedSymbolName(ctx,
                        aliasComponent));
                String alias = !insertText.equals(aliasComponent) ? insertText : "";
                List<TextEdit> txtEdits = CommonUtil.getAutoImportTextEdits("", label, alias, ctx);
                CompletionItem item = getModuleCompletionItem(label, insertText, txtEdits,
                        aliasComponent);
                completionItems.add(new StaticCompletionItem(ctx, item, StaticCompletionItem.Kind.MODULE));
            }
        });
        completionItems.addAll(this.getPredeclaredLangLibCompletions(ctx));

        return completionItems;
    }

    /**
     * Check whether the cursor is within a qualified name reference.
     *
     * @param context completion context
     * @param node    node to evaluate upon
     * @return {@link Boolean}
     * @deprecated Use {@link QNameRefCompletionUtil#onQualifiedNameIdentifier(PositionedOperationContext, Node)}
     * instead
     */
    @Deprecated(forRemoval = true)
    protected boolean onQualifiedNameIdentifier(CompletionContext context, Node node) {
        if (node.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        int colonPos = ((QualifiedNameReferenceNode) node).colon().textRange().startOffset();
        int cursor = context.getCursorPositionInTree();

        return colonPos < cursor;
    }

    protected Optional<LSCompletionItem> getExplicitNewCompletionItem(Symbol symbol,
                                                                      BallerinaCompletionContext context) {
        if (SymbolUtil.isClassDefinition(symbol)) {
            ClassSymbol classSymbol = (ClassSymbol) symbol;
            CompletionItem cItem = FunctionCompletionItemBuilder.build(classSymbol,
                    FunctionCompletionItemBuilder.InitializerBuildMode.EXPLICIT, context);
            MethodSymbol initMethod = classSymbol.initMethod().isPresent() ? classSymbol.initMethod().get() : null;

            return Optional.of(new SymbolCompletionItem(context, initMethod, cItem));
        } else if (SymbolUtil.isOfType(symbol, TypeDescKind.STREAM)) {
            TypeDefinitionSymbol typeSymbol = (TypeDefinitionSymbol) symbol;
            CompletionItem cItem = StreamTypeInitCompletionItemBuilder.build(typeSymbol, context);

            return Optional.of(new SymbolCompletionItem(context, typeSymbol.typeDescriptor(), cItem));
        }

        return Optional.empty();
    }

    /**
     * Get the implicit new expression completion item.
     *
     * @param symbol  object type symbol
     * @param context Language server operation context
     * @return {@link LSCompletionItem} generated
     */
    protected LSCompletionItem getImplicitNewCItemForStreamType(TypeSymbol symbol,
                                                                BallerinaCompletionContext context) {
        CompletionItem cItem = StreamTypeInitCompletionItemBuilder.build();
        return new SymbolCompletionItem(context, symbol, cItem);
    }

    /**
     * Get the implicit new expression completion item.
     *
     * @param symbol  object type symbol
     * @param context Language server operation context
     * @return {@link LSCompletionItem} generated
     */
    protected LSCompletionItem getImplicitNewCItemForClass(ClassSymbol symbol, BallerinaCompletionContext context) {
        CompletionItem cItem = FunctionCompletionItemBuilder.build(symbol,
                FunctionCompletionItemBuilder.InitializerBuildMode.IMPLICIT, context);
        MethodSymbol initMethod = symbol.initMethod().isPresent() ? symbol.initMethod().get() : null;

        return new SymbolCompletionItem(context, initMethod, cItem);
    }

    // Private Methods

    /**
     * Populate the Ballerina Function Completion Items.
     *
     * @param symbol symbol Entry
     * @return completion item
     */
    private List<LSCompletionItem> populateBallerinaFunctionCompletionItems(FunctionSymbol symbol,
                                                                            BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isPresent() && contextType.get().typeKind() == TypeDescKind.FUNCTION
                && symbol.kind() != RESOURCE_METHOD) {
            CompletionItem pointerCompletionItem =
                    FunctionCompletionItemBuilder.buildFunctionPointer(symbol, context);
            completionItems.add(new FunctionPointerCompletionItem(context, symbol, pointerCompletionItem));
        }
        CompletionItem completionItem = FunctionCompletionItemBuilder.build(symbol, context);
        completionItems.add(new SymbolCompletionItem(context, symbol, completionItem));
        return completionItems;
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
                new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get())
        );
    }

    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        /*
        check and check panic expression starts with check and check panic keywords, Which has been added with actions.
        query pipeline starts with from keyword and also being added with the actions
         */
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        // Here we do not add the error and object keywords since it will be added via the module completion items
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRUE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FALSE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NIL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_OBJECT_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE16_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE64_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REG_EXP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_STRING.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_XML.get()));
        
        Predicate<Symbol> symbolFilter = getExpressionContextSymbolFilter();
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbolFilter)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        completionItems.addAll(this.getBasicAndOtherTypeCompletions(context));
        this.getAnonFunctionDefSnippet(context).ifPresent(completionItems::add);
        return completionItems;
    }

    protected Predicate<Symbol> getExpressionContextSymbolFilter() {
        Predicate<Symbol> symbolFilter = CommonUtil.getVariableFilterPredicate();
        // Avoid the error symbol suggestion since it is covered by the lang.error lang-lib
        symbolFilter = symbolFilter.or(symbol -> (symbol.kind() == FUNCTION
                || symbol.kind() == TYPE_DEFINITION || symbol.kind() == CLASS)
                && !symbol.getName().orElse("").equals(Names.ERROR.getValue()));

        return symbolFilter;
    }

    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context, T node) {
        return this.expressionCompletions(context);
    }

    protected List<LSCompletionItem> getTypeQualifierItems(BallerinaCompletionContext context) {
        // Note: here we do not add the service type qualifier since it is being added via getTypeItems call.
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()),
                new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()),
                new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
    }

    private List<LSCompletionItem> getBasicAndOtherTypeCompletions(BallerinaCompletionContext context) {
        // Types in the predeclared langlibs are handled and extracted via #getPredeclaredLangLibCompletions
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return Collections.emptyList();
        }
        Types types = semanticModel.get().types();

        //"readonly", "handle", "never", "json", "anydata", "any", "byte");
        List<TypeSymbol> typeSymbols = new ArrayList<>();
        typeSymbols.add(types.READONLY);
        typeSymbols.add(types.HANDLE);
        typeSymbols.add(types.NEVER);
        typeSymbols.add(types.JSON);
        typeSymbols.add(types.ANYDATA);
        typeSymbols.add(types.ANY);
        typeSymbols.add(types.BYTE);

        List<LSCompletionItem> completionItems = new ArrayList<>();
        typeSymbols.forEach(type -> {
            CompletionItem cItem = TypeCompletionItemBuilder.build(type, type.signature());
            completionItems.add(new SymbolCompletionItem(context, type, cItem));
        });

        return completionItems;
    }

    /**
     * Get the predeclared langlib completions.
     *
     * @param context completion context
     * @return {@link List}
     */
    protected List<LSCompletionItem> getPredeclaredLangLibCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<Types> types = context.currentSemanticModel().map(SemanticModel::types);
        visibleSymbols.stream()
                .filter(symbol -> symbol.getName().isPresent() && symbol.kind() == MODULE)
                .map(symbol -> (ModuleSymbol) symbol)
                .filter(moduleSymbol -> CommonUtil.PRE_DECLARED_LANG_LIBS.contains(moduleSymbol.id().moduleName()))
                .forEach(moduleSymbol -> {
                    //Some lang libs are essentially types. So,type symbols are specifically created for them.
                    TypeSymbol typeSymbol = null;
                    if (types.isPresent()) {
                        switch (moduleSymbol.id().moduleName()) {
                            case "lang.boolean":
                                typeSymbol = types.get().BOOLEAN;
                                break;
                            case "lang.int":
                                typeSymbol = types.get().INT;
                                break;
                            case "lang.error":
                                typeSymbol = types.get().ERROR;
                                break;
                            case "lang.decimal":
                                typeSymbol = types.get().DECIMAL;
                                break;
                            case "lang.float":
                                typeSymbol = types.get().FLOAT;
                                break;
                            case "lang.function":
                                typeSymbol = types.get().FUNCTION;
                                break;
                            case "lang.future":
                                typeSymbol = types.get().FUTURE;
                                break;
                            case "lang.typedesc":
                                typeSymbol = types.get().TYPEDESC;
                                break;
                            case "lang.string":
                                typeSymbol = types.get().STRING;
                                break;
                            case "lang.xml":
                                typeSymbol = types.get().XML;
                                break;
                            default:
                                //ignore
                        }
                    }
                    CompletionItem cItem = TypeCompletionItemBuilder.build(
                            typeSymbol, moduleSymbol.id().modulePrefix());
                    completionItems.add(new SymbolCompletionItem(context, 
                            Objects.requireNonNullElse(typeSymbol, moduleSymbol), cItem));
                });
        return completionItems;
    }

    protected List<LSCompletionItem> getBuiltInTypes(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.stream()
                .filter(symbol -> symbol.getName().isPresent() && symbol.kind() == MODULE)
                .map(symbol -> (ModuleSymbol) symbol)
                .filter(moduleSymbol -> CommonUtil.isLangLib(moduleSymbol.id()))
                .forEach(moduleSymbol -> {
                    // LangLib modules need to be interpreted as a type in vscode. Therefore, we pass a null to bSymbol.
                    CompletionItem cItem = TypeCompletionItemBuilder.build(
                            null, moduleSymbol.id().modulePrefix());
                    completionItems.add(new SymbolCompletionItem(context, moduleSymbol, cItem));
                });
        return completionItems;
    }

    private CompletionItem getModuleCompletionItem(String label, String insertText, @Nonnull List<TextEdit> txtEdits,
                                                   String prefix) {
        CompletionItem moduleCompletionItem = new CompletionItem();
        moduleCompletionItem.setLabel(label);
        if (prefix != null) {
            moduleCompletionItem.setFilterText(prefix);
        }
        moduleCompletionItem.setInsertText(insertText);
        moduleCompletionItem.setDetail(ItemResolverConstants.MODULE_TYPE);
        moduleCompletionItem.setKind(CompletionItemKind.Module);
        if (!txtEdits.isEmpty()) {
            moduleCompletionItem.setAdditionalTextEdits(txtEdits);
        }

        return moduleCompletionItem;
    }

    /**
     * Populate Completion Items of Self Class Symbol.
     *
     * @param ctx     completion context
     * @param rawType type descriptor
     * @return completion item
     */
    private List<LSCompletionItem> populateSelfClassSymbolCompletionItems(Symbol symbol, BallerinaCompletionContext ctx,
                                                                          TypeSymbol rawType) {
        Optional<ModuleMemberDeclarationNode> moduleMember = ctx.enclosedModuleMember();
        if (moduleMember.isEmpty() || (!SymbolUtil.isSelfClassSymbol(symbol, ctx, moduleMember.get())
                && !SymbolUtil.isSelfObjectSymbol(symbol, ctx.getNodeAtCursor()))) {
            return Collections.emptyList();
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ObjectTypeSymbol objectTypeDesc = (ObjectTypeSymbol) rawType;

        objectTypeDesc.fieldDescriptors().values().stream()
                .map(classFieldSymbol -> {
                    CompletionItem completionItem = FieldCompletionItemBuilder.build(classFieldSymbol, true);
                    return new ObjectFieldCompletionItem(ctx, classFieldSymbol, completionItem);
                })
                .forEach(completionItems::add);

        objectTypeDesc.methods().values().stream()
                .map(methodSymbol -> {
                    CompletionItem completionItem = FunctionCompletionItemBuilder.buildMethod(methodSymbol, ctx);
                    return new SymbolCompletionItem(ctx, methodSymbol, completionItem);
                }).forEach(completionItems::add);

        return completionItems;
    }

    /**
     * Returns the completion items based on qualifiers of a given node.
     * <p>
     * Currently, isolated, transactional, client, service are considered qualifiers.
     *
     * @param node    node of which the qualifiers are checked.
     * @param context completion context.
     * @return completion items
     */
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        Set<SyntaxKind> qualKinds = qualifiers.stream().map(Node::kind).collect(Collectors.toSet());
        switch (lastQualifier.kind()) {
            case ISOLATED_KEYWORD:
                if (qualKinds.contains(SyntaxKind.TRANSACTIONAL_KEYWORD)) {
                    break;
                }
                //Objected type desc is added with type completion items in the following case.
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()));
                if (qualKinds.contains(SyntaxKind.CLIENT_KEYWORD) ||
                        qualKinds.contains(SyntaxKind.SERVICE_KEYWORD)) {
                    break;
                }
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
                break;
            case TRANSACTIONAL_KEYWORD:
                if (!qualKinds.contains(SyntaxKind.ISOLATED_KEYWORD)) {
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
                }
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
                break;
            case CLIENT_KEYWORD:
            case SERVICE_KEYWORD:
                if (!qualKinds.contains(SyntaxKind.ISOLATED_KEYWORD)) {
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
                }
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
                break;
            default:
        }
        return completionItems;
    }

    /**
     * Check if the cursor is positioned immediately after a qualifier.
     *
     * @param context completion context.
     * @param node    node.
     * @return {@link Boolean}
     */
    protected boolean onSuggestionsAfterQualifiers(BallerinaCompletionContext context, Node node) {
        int cursor = context.getCursorPositionInTree();
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return false;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        return lastQualifier.textRange().endOffset() < cursor;
    }

    /**
     * Get anonymous function definition snippet.
     *
     * @param context completion context.
     * @return snippet completion item
     */
    protected Optional<SnippetCompletionItem> getAnonFunctionDefSnippet(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<TypeSymbol> typeSymbolAtCursor = context.getContextType();

        if (typeSymbolAtCursor.isEmpty() || typeSymbolAtCursor.get().typeKind() != TypeDescKind.FUNCTION) {
            return Optional.empty();
        }

        TypeSymbol symbol = typeSymbolAtCursor.get();
        FunctionTypeSymbol functionTypeSymbol = ((FunctionTypeSymbol) symbol);
        Optional<TypeSymbol> returnTypeSymbol = functionTypeSymbol.returnTypeDescriptor();

        if (returnTypeSymbol.isEmpty()) {
            return Optional.empty();
        }

        List<String> args = new ArrayList<>();
        int argIndex = 1;
        Set<String> visibleSymbolNames = visibleSymbols
                .stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        if (functionTypeSymbol.params().isPresent()) {
            // variable names
            for (ParameterSymbol parameterSymbol : functionTypeSymbol.params().get()) {
                String varName = "";
                TypeSymbol parameterTypeSymbol = parameterSymbol.typeDescriptor();
                varName = NameUtil.generateParameterName(varName, argIndex, parameterTypeSymbol,
                        visibleSymbolNames);
                args.add(FunctionGenerator.getParameterTypeAsString(context, parameterTypeSymbol) + " " + varName);
                visibleSymbolNames.add(varName);
                argIndex++;
            }
        }

        String functionName = "";
        String snippet = FunctionGenerator.generateFunction(context, false, functionName, args,
                returnTypeSymbol.get());
        SnippetBlock snippetBlock = new SnippetBlock(ItemResolverConstants.ANON_FUNCTION,
                ItemResolverConstants.FUNCTION, snippet, ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET);
        snippetBlock.setId(ItemResolverConstants.ANON_FUNCTION);

        return Optional.of(new SnippetCompletionItem(context, snippetBlock));
    }

    /**
     * Sets the sort text of the provided completion item based on provided rank and context type.
     *
     * @param context        Completion context
     * @param completionItem Completion item
     * @param rank           A secondary rank to be considered other than assignability
     */
    protected void sortByAssignability(BallerinaCompletionContext context, LSCompletionItem completionItem, int rank) {
        Optional<TypeSymbol> contextType = context.getContextType();
        String sortText = "";
        // First we sort the assignable items above others, and then sort by the rank
        if (contextType.isPresent() && SortingUtil.isCompletionItemAssignable(completionItem, contextType.get())) {
            // Rank directly assignable ones first
            sortText += SortingUtil.genSortText(1);
        } else if (contextType.isPresent() &&
                SortingUtil.isCompletionItemAssignableWithCheck(completionItem, contextType.get())) {
            // Then the items which can be made assignable using a check expression
            sortText += SortingUtil.genSortText(2);
        } else {
            sortText += SortingUtil.genSortText(3);
        }
        sortText += SortingUtil.genSortText(rank);

        completionItem.getCompletionItem().setSortText(sortText);
    }
}
