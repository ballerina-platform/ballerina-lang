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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.projects.Module;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.builder.TypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link TypeTestExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypeTestExpressionNodeContext extends AbstractCompletionProvider<TypeTestExpressionNode> {

    public TypeTestExpressionNodeContext() {
        super(TypeTestExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypeTestExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onExpressionContext(context, node)) {
            completionItems.addAll(this.expressionCompletions(context, node));
        } else if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.addAll(getModuleTypeDescCompletionsForExpression(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private List<LSCompletionItem> getModuleTypeDescCompletionsForExpression(BallerinaCompletionContext context,
                                                                             TypeTestExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> typeSymbol =
                context.currentSemanticModel().flatMap(semanticModel -> semanticModel.typeOf(node.expression()));
        Optional<Module> currentModule = context.currentModule();
        if (typeSymbol.isEmpty() || currentModule.isEmpty()) {
            return completionItems;
        }
        List<TypeReferenceTypeSymbol> typeReferences;
        if (typeSymbol.get().typeKind() == TypeDescKind.UNION) {
            typeReferences = ((UnionTypeSymbol) typeSymbol.get()).memberTypeDescriptors().stream()
                    .filter(type -> type.typeKind() == TypeDescKind.TYPE_REFERENCE)
                    .map(type -> (TypeReferenceTypeSymbol) type).collect(Collectors.toList());
        } else if (typeSymbol.get().typeKind() == TypeDescKind.TYPE_REFERENCE) {
            typeReferences = List.of((TypeReferenceTypeSymbol) typeSymbol.get());
        } else {
            //check for intersection as well.
            return completionItems;
        }
        typeReferences.stream()
                .filter(typeRef -> isQualifiedTypeReference(context, typeRef))
                .forEach(typeRef -> {
                    String typeName = CommonUtil.getModifiedTypeName(context, typeRef);
                    if (!typeName.isEmpty()) {
                        TypeSymbol rawType = CommonUtil.getRawType(typeRef);
                        completionItems.add(new SymbolCompletionItem(context, rawType,
                                TypeCompletionItemBuilder.build(rawType, typeName)));
                    }
                });
        return completionItems;
    }

    private boolean isQualifiedTypeReference(BallerinaCompletionContext context,
                                             TypeSymbol typeSymbol) {

        Optional<ModuleSymbol> module = typeSymbol.getModule();
        Optional<Module> currentModule = context.currentModule();
        Optional<String> name = typeSymbol.getName();

        if (typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE
                && module.isEmpty() || name.isEmpty()
                || currentModule.isEmpty()) {
            return false;
        }
        ModuleID moduleID = module.get().id();
        Optional<String> moduleName = module.get().getName();
        String orgName = moduleID.orgName();

        String currentModuleName = currentModule.get().isDefaultModule() ?
                currentModule.get().moduleName().packageName().value()
                : currentModule.get().moduleName().moduleNamePart();
        String currentOrg = context.currentModule().get().packageInstance().packageOrg().value();
        if (moduleName.isEmpty() || (orgName.equals(currentOrg) && moduleName.get().equals(currentModuleName))) {
            return false;
        }
        return true;
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, TypeTestExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token isKeyword = node.isKeyword();

        return cursor < isKeyword.textRange().startOffset();
    }

    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context,
                                                           TypeTestExpressionNode node) {
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> typesInModule = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            return this.getCompletionItemList(typesInModule, context);
        }

        return this.expressionCompletions(context);
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, TypeTestExpressionNode node) {
        return !node.isKeyword().isMissing();
    }

    @Override
    public void sort(BallerinaCompletionContext context, TypeTestExpressionNode node,
                     List<LSCompletionItem> completionItems) {

        Optional<TypeSymbol> typeSymbol =
                context.currentSemanticModel().flatMap(semanticModel -> semanticModel.typeOf(node.expression()));
        if (typeSymbol.isEmpty()) {
            super.sort(context, node, completionItems);
        }

        completionItems.forEach(lsCItem -> {
            int rank = 2;
            if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
                Optional<TypeSymbol> tSymbol = ((SymbolCompletionItem) lsCItem).getSymbol()
                        .filter(symbol -> CommonUtil.typesFilter().test(symbol) || symbol.kind() == SymbolKind.TYPE)
                        .flatMap(SymbolUtil::getTypeDescriptor);
                if (tSymbol.isPresent() && tSymbol.get().subtypeOf(typeSymbol.get())) {
                    rank = 1;
                }
            }
            if (lsCItem.getType() == LSCompletionItem.CompletionItemType.TYPE) {
                Optional<TypeSymbol> tSymbol = ((TypeCompletionItem) lsCItem).getTypeSymbol();
                if (tSymbol.isPresent() && tSymbol.get().subtypeOf(typeSymbol.get())) {
                    rank = 1;
                }
            }
            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank) +
                    SortingUtil.genSortTextForTypeDescContext(context, lsCItem));
        });
    }
}
