/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.FunctionPointerCompletionItem;
import org.ballerinalang.langserver.completions.ObjectFieldCompletionItem;
import org.ballerinalang.langserver.completions.RecordFieldCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.commons.completion.LSCompletionItem.CompletionItemType.SNIPPET;

/**
 * Enclose a set of utilities for sorting and ranking of completion items.
 *
 * @since 2.0.0
 */
public class SortingUtil {

    private static final int RANK_UPPER_BOUNDARY = 64;

    private static final int RANK_LOWER_BOUNDARY = 90;

    private static final int RANK_RANGE = 25;

    private static final String BALLERINA_ORG = "ballerina";

    private static final String LANG_LIB_PKG_PREFIX = "lang.";

    private static final String LANG_LIB_LABEL_PREFIX = "ballerina/lang.";

    private static final String BAL_LIB_LABEL_PREFIX = "ballerina/";

    private SortingUtil() {
    }

    /**
     * Check whether the item is an associated module completion item.
     *
     * @param item {@link LSCompletionItem} to evaluate
     * @return {@link Boolean} whether module completion or not
     */
    public static boolean isModuleCompletionItem(LSCompletionItem item) {
        return (item instanceof SymbolCompletionItem
                && ((SymbolCompletionItem) item).getSymbol().orElse(null) instanceof ModuleSymbol)
                || (item instanceof StaticCompletionItem
                && (((StaticCompletionItem) item).kind() == StaticCompletionItem.Kind.MODULE
                || ((StaticCompletionItem) item).kind() == StaticCompletionItem.Kind.LANG_LIB_MODULE));
    }

    /**
     * Check whether the item is an associated type completion item.
     *
     * @param item {@link LSCompletionItem} to evaluate
     * @return {@link Boolean} whether type completion or not
     */
    public static boolean isTypeCompletionItem(LSCompletionItem item) {
        return (item.getType() == LSCompletionItem.CompletionItemType.SYMBOL
                && (((SymbolCompletionItem) item).getSymbol().orElse(null) instanceof TypeSymbol
                || ((SymbolCompletionItem) item).getSymbol().orElse(null) instanceof TypeDefinitionSymbol))
                || (item instanceof StaticCompletionItem
                && ((StaticCompletionItem) item).kind() == StaticCompletionItem.Kind.TYPE);
    }

    /**
     * Get the sort text for a given module completion item.
     *
     * @param context language server completion context
     * @param item    completion item to evaluate
     * @return {@link String} rank assigned to the completion item
     */
    public static String genSortTextForModule(BallerinaCompletionContext context, LSCompletionItem item) {
        /*
        Sorting order is defined as follows,
        (1) Current project's modules
        (2) Imported modules (there is an import statement added)
        (3) Langlib modules
        (4) Standard libraries
        (5) Other modules 
         */
        Optional<Project> currentProject = context.workspace().project(context.filePath());
        String currentOrg = currentProject.get().currentPackage().packageOrg().value();
        String currentPkgName = currentProject.get().currentPackage().packageName().value();
        String label = item.getCompletionItem().getLabel();
        int rank = -1;
        if (item instanceof SymbolCompletionItem && ((SymbolCompletionItem) item).getSymbol().isPresent() &&
                ((SymbolCompletionItem) item).getSymbol().get().kind() == SymbolKind.MODULE) {
            // Already imported module
            ModuleSymbol moduleSymbol = (ModuleSymbol) ((SymbolCompletionItem) item).getSymbol().get();
            String orgName = moduleSymbol.id().orgName();
            String moduleName = moduleSymbol.id().moduleName();

            if (currentOrg.equals(orgName) && moduleName.startsWith(currentPkgName + ".")) {
                // Module in the current project
                rank = 1;
            } else if (BALLERINA_ORG.equals(orgName) && !moduleName.startsWith(LANG_LIB_PKG_PREFIX)) {
                // langLib module
                rank = 2;
            } else if (BALLERINA_ORG.equals(orgName)) {
                // ballerina module
                rank = 3;
            } else {
                // any other imported module
                rank = 4;
            }
        } else if (label.startsWith(LANG_LIB_LABEL_PREFIX)) {
            // Langlib modules
            rank = 5;
        } else if (label.startsWith(BAL_LIB_LABEL_PREFIX)) {
            // Standard libraries modules
            rank = 6;
        }
        rank = rank < 0 ? 7 : rank;

        return genSortText(rank);
    }

    /**
     * Get the sort text for a given completion item within a context where the type descriptors are allowed.
     * This assumes that the user gets the particular completion item by calling
     * AbstractCompletionProvider#getTypeDescContextItems. Hence, here we honour the module completion items as well
     * <p>
     * Sorting order is defined as follows
     * 1. Types defined in the same module
     * 2. Types visible from other modules (StrandData, Thread in lang.value)
     * 3. Visible Modules
     * 4. Constant
     * 5. Enums
     * 6. Enum Member
     * 7. Basic Types (boolean, int, string, etc)
     * 8. Type Descriptor snippets (record snippet, object snippet)
     * 8+. keywords (true, false, record, object)
     *
     * @param context language server completion context
     * @param item    completion item to evaluate
     * @return {@link String} rank assigned to the completion item
     */
    public static String genSortTextForTypeDescContext(BallerinaCompletionContext context, LSCompletionItem item) {
        if (item.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
            Optional<Symbol> symbol = ((SymbolCompletionItem) item).getSymbol();
            // Case 6
            if (symbol.isEmpty()) {
                // Basic types such as int, boolean, string, and etc get a lower priority
                return genSortText(7);
            }
            Optional<Project> currentProject = context.workspace().project(context.filePath());
            String currentOrg = currentProject.get().currentPackage().packageOrg().value();
            String currentPkgName = currentProject.get().currentPackage().packageName().value();

            Optional<ModuleSymbol> symbolModule = symbol.get().getModule();
            String orgName = symbolModule.isPresent() ? symbolModule.get().id().orgName() : "";
            String moduleName = symbolModule.isPresent() ? symbolModule.get().id().moduleName() : "";

            // Case 4
            if (symbol.get().kind() == SymbolKind.CONSTANT) {
                return genSortText(4);
            }
            if (symbol.get().kind() == SymbolKind.ENUM) {
                return genSortText(5);
            }
            // Case 5
            if (symbol.get().kind() == SymbolKind.ENUM_MEMBER) {
                return genSortText(5);
            }
            /*
            Case 1 and 2
            Types in the same module get the priority.
            Types coming from lang.value (StrandData, Thread) get the second highest priority
            
            Note: At this point there shouldn't be any symbol kind other than TYPE
             */
            String sortPrefix = currentOrg.equals(orgName) && currentPkgName.equals(moduleName)
                    ? genSortText(1) : genSortText(2);
            return sortPrefix + genSortText(toRank(context, item));
        }

        // Case 3
        if (isModuleCompletionItem(item)) {
            return genSortText(3) + genSortTextForModule(context, item);
        }

        // Case 7
        if (item.getType() == SNIPPET && ((SnippetCompletionItem) item).kind() == SnippetBlock.Kind.TYPE) {
            return genSortText(8);
        }

        // Case 7+ 
        return genSortText(SortingUtil.toRank(context, item, 8));
    }

    /**
     * Generate the sort text based on the assignability to particular type symbol.
     * <p>
     * Sorting order is defined as follows
     * 1.Variables assignable to the given type symbol
     * 2.Functions with return type descriptor assignable to given type symbol
     * 3.Snippets assignable to given type symbol
     * 4.Other symbols follow the default sorting order
     *
     * @param context        Ballerina completion context
     * @param completionItem Completion item.
     * @param typeSymbol     type symbol
     * @return {@link String} Sort text.
     */
    public static String genSortTextByAssignability(BallerinaCompletionContext context,
                                                    LSCompletionItem completionItem,
                                                    TypeSymbol typeSymbol) {
        if (isCompletionItemAssignable(completionItem, typeSymbol)) {
            return genSortText(1) + genSortText(toRank(context, completionItem));
        } else {
            return genSortText(toRank(context, completionItem, 2));
        }
    }

    /**
     * Check if the provided completion item is assignable to the provided type.
     *
     * @param completionItem Completion item
     * @param typeSymbol     Type
     * @return True if assignable
     */
    public static boolean isCompletionItemAssignable(LSCompletionItem completionItem, TypeSymbol typeSymbol) {
        Optional<TypeSymbol> optionalTypeSymbol = Optional.empty();
        switch (completionItem.getType()) {
            case SYMBOL:
                optionalTypeSymbol = ((SymbolCompletionItem) completionItem).getSymbol()
                        .flatMap(symbol ->
                                SymbolUtil.getTypeDescriptor(symbol)
                                        .flatMap(typeDesc -> {
                                            if (symbol instanceof FunctionSymbol) {
                                                return ((FunctionTypeSymbol) typeDesc).returnTypeDescriptor();
                                            }
                                            return Optional.of(typeDesc);
                                        }));
                break;
            case OBJECT_FIELD: {
                ObjectFieldSymbol fieldSymbol = ((ObjectFieldCompletionItem) completionItem).getFieldSymbol();
                optionalTypeSymbol = SymbolUtil.getTypeDescriptor(fieldSymbol);
                break;
            }
            case RECORD_FIELD: {
                RecordFieldSymbol fieldSymbol = ((RecordFieldCompletionItem) completionItem).getFieldSymbol();
                optionalTypeSymbol = SymbolUtil.getTypeDescriptor(fieldSymbol);
                break;
            }
            case FUNCTION_POINTER:
                optionalTypeSymbol = ((FunctionPointerCompletionItem) completionItem).getSymbol()
                        .flatMap(SymbolUtil::getTypeDescriptor);
                break;
            case SNIPPET:
                if (typeSymbol.typeKind() == TypeDescKind.FUNCTION
                        && ((SnippetCompletionItem) completionItem).id().equals(ItemResolverConstants.ANON_FUNCTION)) {
                        return true;
                }
        }

        return optionalTypeSymbol.isPresent() && optionalTypeSymbol.get().assignableTo(typeSymbol);
    }

    /**
     * Generate the sort text when given the rank.
     * Sort Text is generated by providing an All Caps String which includes only the english alphabetical
     * characters within 65-90 ASCII range.
     *
     * @param rank rank to be assigned. Rank should be a non zero integer
     * @return {@link String} generated sort text
     */
    public static String genSortText(int rank) {
        if (rank < 1) {
            throw new IllegalArgumentException("Rank should be greater than zero");
        }

        int suffixValue = rank % RANK_RANGE;
        String suffix = suffixValue == 0 ? "" : String.valueOf((char) (RANK_UPPER_BOUNDARY + suffixValue));
        String prefix = String.join("", Collections.nCopies((rank - suffixValue) / RANK_RANGE,
                (char) RANK_LOWER_BOUNDARY + ""));

        return prefix + suffix;
    }

    /**
     * Get the assignable type for the node. Assignable type is only addressed for the following at the moment.
     * Assignable type is derived by analyzing the LHS of the particular node and the particular binding pattern
     * 1. Listener Declaration
     * 2. Local variable declaration
     * 3. Module level variable declaration
     *
     * @param context Completion context
     * @param owner   Owner node to extract the assignable type
     * @return {@link Optional} assignable type
     */
    public static Optional<TypeSymbol> getAssignableType(BallerinaCompletionContext context, Node owner) {
        Optional<Node> typeDesc;
        switch (owner.kind()) {
            case LISTENER_DECLARATION:
                typeDesc = Optional.ofNullable(((ListenerDeclarationNode) owner).typeDescriptor().orElse(null));
                break;
            case LOCAL_VAR_DECL:
                typeDesc = Optional.ofNullable(((VariableDeclarationNode) owner).typedBindingPattern()
                        .typeDescriptor());
                break;
            case MODULE_VAR_DECL:
                typeDesc = Optional.ofNullable(((ModuleVariableDeclarationNode) owner).typedBindingPattern()
                        .typeDescriptor());
                break;
            default:
                return Optional.empty();
        }

        if (typeDesc.isEmpty()) {
            return Optional.empty();
        }

        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        if (typeDesc.get().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) typeDesc.get();
            String alias = QNameReferenceUtil.getAlias(qNameRef);
            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context, alias);

            if (moduleSymbol.isEmpty()) {
                return Optional.empty();
            }
            String identifier = qNameRef.identifier().text();
            return CommonUtil.getTypeFromModule(context, alias, identifier);
        }
        if (typeDesc.get().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String nameRef = ((SimpleNameReferenceNode) typeDesc.get()).name().text();
            for (Symbol symbol : visibleSymbols) {
                if (symbol.kind() == SymbolKind.TYPE_DEFINITION && symbol.getName().get().equals(nameRef)) {
                    TypeDefinitionSymbol typeDefinitionSymbol = (TypeDefinitionSymbol) symbol;
                    return Optional.of(typeDefinitionSymbol.typeDescriptor());
                }
            }
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Sets the sorting text to provided completion items using the default sorting.
     *
     * @param context         Completion context
     * @param completionItems Completion items to be set sorting texts
     */
    public static void toDefaultSorting(BallerinaCompletionContext context, List<LSCompletionItem> completionItems) {
        for (LSCompletionItem item : completionItems) {
            int rank = SortingUtil.toRank(context, item);
            item.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        }
    }

    /**
     * Calculates the rank of a given completion item with rank offset 0.
     *
     * @param context        completion context
     * @param completionItem Completion item
     * @return rank
     * @see #toRank(BallerinaCompletionContext, LSCompletionItem, int)
     */
    public static int toRank(BallerinaCompletionContext context, LSCompletionItem completionItem) {
        return toRank(context, completionItem, 0);
    }

    /**
     * Calculates the rank of a given completion item.
     *
     * @param completionItem Completion item
     * @param rankOffset     Number to offset the rank by
     * @return Rank
     */
    public static int toRank(BallerinaCompletionContext context, LSCompletionItem completionItem, int rankOffset) {
        boolean onQnameRef = QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor());
        int rank = -1;
        CompletionItemKind completionItemKind = completionItem.getCompletionItem().getKind();
        switch (completionItem.getType()) {
            case FUNCTION_POINTER:
            case SYMBOL:
                if (completionItemKind != null) {
                    switch (completionItemKind) {
                        case Constant:
                            rank = onQnameRef ? 2 : 1;
                            break;
                        case Variable:
                            rank = onQnameRef ? 3 : 2;
                            break;
                        case Function:
                            rank = onQnameRef ? 1 : 3;
                            break;
                        case Method:
                            rank = 4;
                            break;
                        case Constructor:
                            rank = 5;
                            break;
                        case EnumMember:
                            rank = 8;
                            break;
                        case Enum:
                            rank = 9;
                            break;
                        case Class:
                            rank = 10;
                            break;
                        case Interface:
                            rank = 11;
                            break;
                        case Event:
                            rank = 12;
                            break;
                        case Struct:
                            rank = 13;
                            break;
                        case TypeParameter:
                            rank = 14;
                            break;
                        case Module:
                            rank = 15;
                            break;
                        default:
                            break;
                    }
                }
                break;
            case SNIPPET:
                if (completionItemKind != null) {
                    switch (completionItemKind) {
                        case TypeParameter:
                            rank = 14;
                            break;
                        case Snippet:
                            rank = 16;
                            break;
                        case Keyword:
                            rank = 17;
                            break;
                    }
                }
                break;
            case OBJECT_FIELD:
                rank = 6;
                break;
            case RECORD_FIELD:
                rank = 7;
                break;
        }

        if (rank == -1) {
            rank = 18;
        } else {
            rank = rankOffset + rank;
        }

        return rank;
    }
}
