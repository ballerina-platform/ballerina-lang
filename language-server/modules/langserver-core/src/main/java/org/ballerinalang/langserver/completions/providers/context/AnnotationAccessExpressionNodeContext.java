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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.FieldAccessCompletionResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.ballerinalang.model.elements.AttachPoint;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;

/**
 * Completion provider for {@link AnnotAccessExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class AnnotationAccessExpressionNodeContext extends AbstractCompletionProvider<AnnotAccessExpressionNode> {

    public AnnotationAccessExpressionNodeContext() {
        super(AnnotAccessExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, AnnotAccessExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        FieldAccessCompletionResolver fieldAccessCompletionResolver = new FieldAccessCompletionResolver(context);
        Optional<TypeSymbol> type = node.expression().apply(fieldAccessCompletionResolver);

        if (type.isEmpty()) {
            return completionItems;
        }

        TypeSymbol typeSymbol = type.get();

        if (((TypeDescTypeSymbol) typeSymbol).typeParameter().isEmpty()) {
            return completionItems;
        }
        
        return getAnnotationTags(context, node, typeSymbol);
    }

    public List<LSCompletionItem> getAnnotationTags(BallerinaCompletionContext context, AnnotAccessExpressionNode node,
                                                    TypeSymbol typeSymbol) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        TypeDescKind typeDescKind = ((TypeDescTypeSymbol) typeSymbol).typeParameter().get().typeKind();
        AttachPoint.Point attachPoint = getAttachPointForType(typeSymbol);
        List<LSCompletionItem> completionItems = new ArrayList<>();
        boolean isTypeDescKindAnyOrAnyData = typeDescKind == TypeDescKind.ANY || typeDescKind == TypeDescKind.ANYDATA;

        if (attachPoint == null && !isTypeDescKindAnyOrAnyData) {
            return new ArrayList<>();
        }

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;

            List<Symbol> moduleSymbols = getAnnotationContextEntries(context, qNameRef);
            moduleSymbols.stream()
                    .filter(symbol -> isTypeDescKindAnyOrAnyData || ((AnnotationSymbol) symbol).attachPoints()
                            .stream()
                            .anyMatch(aPoint -> aPoint.name().equals(attachPoint.name())))
                    .forEach(annotation -> {
                        AnnotationSymbol symbol = (AnnotationSymbol) annotation;
                        completionItems.add(getAnnotationsCompletionItem(context, symbol));
                    });
        } else {
            completionItems.addAll(this.getModuleCompletionItems(context));
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == ANNOTATION
                            && (isTypeDescKindAnyOrAnyData || ((AnnotationSymbol) symbol).attachPoints()
                            .stream()
                            .anyMatch(aPoint -> aPoint.name().equals(attachPoint.name()))))
                    .forEach(annotation -> {
                        AnnotationSymbol symbol = (AnnotationSymbol) annotation;
                        completionItems.add(getAnnotationsCompletionItem(context, symbol));
                    });
        }
        
        this.sort(context, node, completionItems);
        return completionItems;
    }

    public static AttachPoint.Point getAttachPointForType(TypeSymbol typeSymbol) {
        if (((TypeDescTypeSymbol) typeSymbol).typeParameter().isEmpty()) {
            return null;
        }
        TypeSymbol symbol = ((TypeDescTypeSymbol) typeSymbol).typeParameter().get();
        switch (symbol.typeKind()) {
            case TYPEDESC:
                TypeDescTypeSymbol typeDescTypeSymbol = (TypeDescTypeSymbol) symbol;
                if (((TypeReferenceTypeSymbol) typeDescTypeSymbol.typeParameter().get()).typeDescriptor().kind() 
                        == SymbolKind.CLASS) {
                    return  AttachPoint.Point.CLASS;
                }
                return AttachPoint.Point.TYPE;
            case OBJECT:
                if (((ObjectTypeSymbol) symbol).qualifiers().contains(Qualifier.SERVICE)) {
                    return AttachPoint.Point.SERVICE;
                } else {
                    return AttachPoint.Point.OBJECT_FIELD;
                }
            case FUNCTION:
                return AttachPoint.Point.FUNCTION;
            case RECORD:
                return AttachPoint.Point.RECORD_FIELD;
            default:
                return null;
        }
    }
    
    private static List<Symbol> getAnnotationContextEntries(BallerinaCompletionContext ctx,
                                                            QualifiedNameReferenceNode qNameRef) {
        String moduleAlias = QNameReferenceUtil.getAlias(qNameRef);
        Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(ctx, moduleAlias);

        return moduleSymbol.map(value -> value.allSymbols().stream()
                .filter(symbol -> symbol.kind() == ANNOTATION)
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    private LSCompletionItem getAnnotationsCompletionItem(BallerinaCompletionContext ctx,
                                                          AnnotationSymbol annotationSymbol) {
        CompletionItem item = new CompletionItem();
        item.setInsertText(annotationSymbol.getName().get());
        item.setLabel(annotationSymbol.getName().get());
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        item.setKind(CompletionItemKind.Property);

        return new TypeCompletionItem(ctx, null, item);
    }

    @Override
    protected List<LSCompletionItem> getPredeclaredLangLibCompletions(BallerinaCompletionContext context) {
        return Collections.emptyList();
    }

    @Override
    public void sort(BallerinaCompletionContext context, AnnotAccessExpressionNode node, 
                     List<LSCompletionItem> lsCItems) {
        for (LSCompletionItem lsCItem : lsCItems) {
            CompletionItem completionItem = lsCItem.getCompletionItem();
            if (completionItem.getDetail().equals(ItemResolverConstants.ANNOTATION_TYPE)) {
                completionItem.setSortText(SortingUtil.genSortText(1) 
                        + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
            } else {
            completionItem.setSortText(SortingUtil.genSortText(2) 
                    + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
            }
        }
    }
}
