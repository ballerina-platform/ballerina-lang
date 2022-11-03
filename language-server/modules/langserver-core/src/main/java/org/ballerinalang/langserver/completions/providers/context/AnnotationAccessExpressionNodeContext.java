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

import io.ballerina.compiler.api.symbols.AnnotationAttachPoint;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.FieldAccessCompletionResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

        if (type.isEmpty() || type.get().typeKind() != TypeDescKind.TYPEDESC
                || ((TypeDescTypeSymbol) type.get()).typeParameter().isEmpty()) {
            return completionItems;
        }

        return getAnnotationTags(context, node, type.get());
    }

    private List<LSCompletionItem> getAnnotationTags(BallerinaCompletionContext context,
                                                     AnnotAccessExpressionNode node,
                                                     TypeSymbol typeSymbol) {
        // Before calling this method we have explicitly check the existence of the param value
        TypeDescKind typeDescKind = ((TypeDescTypeSymbol) typeSymbol).typeParameter().get().typeKind();
        List<AnnotationAttachPoint> attachPointsForType = getAttachPointsForType(typeSymbol);
        List<LSCompletionItem> completionItems = new ArrayList<>();
        boolean isTypeDescKindAnyOrAnyData = typeDescKind == TypeDescKind.ANY || typeDescKind == TypeDescKind.ANYDATA;

        if (attachPointsForType.isEmpty() && !isTypeDescKindAnyOrAnyData) {
            return Collections.emptyList();
        }

        List<AnnotationSymbol> annotationSymbols = getAnnotationEntries(context);
        annotationSymbols.forEach(annotationSymbol -> {
            List<AnnotationAttachPoint> annotationAttachPoints = annotationSymbol.attachPoints();
            for (AnnotationAttachPoint ap : attachPointsForType) {
                if (annotationAttachPoints.isEmpty() || annotationAttachPoints.contains(ap)) {
                    completionItems.add(getAnnotationsCompletionItem(context, annotationSymbol));
                    break;
                }
            }
        });

        if (!QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            completionItems.addAll(this.getModuleCompletionItems(context));
        }

        this.sort(context, node, completionItems);
        return completionItems;
    }

    private List<AnnotationAttachPoint> getAttachPointsForType(TypeSymbol typeSymbol) {
        if (((TypeDescTypeSymbol) typeSymbol).typeParameter().isEmpty()) {
            return Collections.emptyList();
        }
        List<AnnotationAttachPoint> annotationAttachPoints = new ArrayList<>();
        TypeSymbol symbol = ((TypeDescTypeSymbol) typeSymbol).typeParameter().get();

        switch (symbol.typeKind()) {
            case TYPEDESC:
                TypeDescTypeSymbol typeDescTypeSymbol = (TypeDescTypeSymbol) symbol;
                if (((TypeReferenceTypeSymbol) typeDescTypeSymbol.typeParameter().get()).typeDescriptor().kind()
                        == SymbolKind.CLASS) {
                    annotationAttachPoints.add(AnnotationAttachPoint.CLASS);
                } else {
                    annotationAttachPoints.add(AnnotationAttachPoint.TYPE);
                }
                break;
            case OBJECT:
                if (((ObjectTypeSymbol) symbol).qualifiers().contains(Qualifier.SERVICE)) {
                    annotationAttachPoints.add(AnnotationAttachPoint.SERVICE);
                } else {
                    annotationAttachPoints.add(AnnotationAttachPoint.CLASS);
                }
                break;
            case FUNCTION:
                annotationAttachPoints.add(AnnotationAttachPoint.FUNCTION);
                annotationAttachPoints.add(AnnotationAttachPoint.TYPE);
                break;
            case RECORD:
                annotationAttachPoints.add(AnnotationAttachPoint.TYPE);
                break;
            case UNION:
                List<TypeDescKind> typeDescKinds = ((UnionTypeSymbol) symbol).memberTypeDescriptors().stream()
                        .map(TypeSymbol::typeKind)
                        .collect(Collectors.toList());
                if (typeDescKinds.contains(TypeDescKind.ANY) || !typeDescKinds.contains(TypeDescKind.ANYDATA)) {
                    annotationAttachPoints.addAll(Arrays.asList(
                            AnnotationAttachPoint.TYPE,
                            AnnotationAttachPoint.CLASS,
                            AnnotationAttachPoint.FUNCTION,
                            AnnotationAttachPoint.SERVICE
                    ));
                }
                break;
            case ANY:
            case ANYDATA:
                annotationAttachPoints.addAll(Arrays.asList(
                        AnnotationAttachPoint.TYPE,
                        AnnotationAttachPoint.CLASS,
                        AnnotationAttachPoint.FUNCTION,
                        AnnotationAttachPoint.SERVICE
                ));
                break;
            default:
                break;
        }

        return annotationAttachPoints;
    }

    private static List<AnnotationSymbol> getAnnotationEntries(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        Predicate<Symbol> predicate = symbol -> symbol.kind() == ANNOTATION;
        List<AnnotationSymbol> annotationSymbols;
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            annotationSymbols =
                    QNameRefCompletionUtil.getModuleContent(ctx, (QualifiedNameReferenceNode) nodeAtCursor, predicate)
                            .stream()
                            .map(symbol -> (AnnotationSymbol) symbol)
                            .collect(Collectors.toList());
        } else {
            annotationSymbols = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                    .filter(predicate)
                    .map(symbol -> (AnnotationSymbol) symbol)
                    .collect(Collectors.toList());
        }

        return annotationSymbols;
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
