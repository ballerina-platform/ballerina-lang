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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
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
import org.ballerinalang.model.elements.AttachPoint;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
//
        if (!(typeSymbol.typeKind() instanceof TypeDescKind)) {
            return completionItems;
        }

        return getAnnotationTags(context, typeSymbol);
    }

    public List<LSCompletionItem> getAnnotationTags(BallerinaCompletionContext context, TypeSymbol typeSymbol) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        AttachPoint.Point attachPoint = getAttachPointForType(typeSymbol);

        if (attachPoint == null) {
            return new ArrayList<>();
        }

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            String alias = ((QualifiedNameReferenceNode) nodeAtCursor).modulePrefix().text();
            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context, alias);

            // Fixme
//            return scopeEntry.map(value -> value.symbol.scope.entries.values().stream()
//                    .filter(entry -> {
//                        BSymbol symbol = entry.symbol;
//                        return symbol instanceof BAnnotationSymbol && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC
//                                && ((BAnnotationSymbol) symbol).points.stream()
//                                .anyMatch(aPoint -> aPoint.point.getValue().equals(attachPoint.getValue()));
//                    })
//                    .map(entry -> {
//                        BAnnotationSymbol symbol = (BAnnotationSymbol) entry.symbol;
//                        return getAnnotationsCompletionItem(context, symbol);
//                    })
//                    .collect(Collectors.toList())).orElseGet(ArrayList::new);
            return new ArrayList<>();
        }

        List<LSCompletionItem> completionItems = this.getModuleCompletionItems(context);
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == ANNOTATION && ((AnnotationSymbol) symbol).attachPoints()
                        .stream()
                        .anyMatch(aPoint -> aPoint.name().equals(attachPoint.name())))
                .forEach(annotation -> {
                    AnnotationSymbol symbol = (AnnotationSymbol) annotation;
                    completionItems.add(getAnnotationsCompletionItem(context, symbol));
                });


        return completionItems;
    }

    private AttachPoint.Point getAttachPointForType(TypeSymbol typeSymbol) {
        TypeDescKind typeDescKind = ((TypeDescTypeSymbol) typeSymbol).typeParameter().get().typeKind();
        if (typeDescKind == TypeDescKind.SERVICE) {
            return AttachPoint.Point.SERVICE;
        } else if (typeDescKind == TypeDescKind.FUNCTION) {
            return AttachPoint.Point.FUNCTION;
        } else {
            return null;
        }
    }

    private LSCompletionItem getAnnotationsCompletionItem(BallerinaCompletionContext ctx,
                                                          AnnotationSymbol annotationSymbol) {
        Optional<TypeSymbol> attachedType = annotationSymbol.typeDescriptor();
        CompletionItem item = new CompletionItem();
        item.setInsertText(annotationSymbol.getName().get());
        item.setLabel(annotationSymbol.getName().get());
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        item.setKind(CompletionItemKind.Property);

        return new TypeCompletionItem(ctx, null, item);
    }
}
