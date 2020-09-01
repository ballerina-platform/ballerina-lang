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

import io.ballerinalang.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.AttachPoint;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link AnnotAccessExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class AnnotationAccessExpressionNodeContext extends AbstractCompletionProvider<AnnotAccessExpressionNode> {

    public AnnotationAccessExpressionNodeContext() {
        super(AnnotAccessExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, AnnotAccessExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<Scope.ScopeEntry> expressionEntry = CommonUtil.getExpressionEntry(context, node.expression());

        if (!expressionEntry.isPresent()) {
            return completionItems;
        }

        BType typeOfSymbol = CommonUtil.getTypeOfSymbol(expressionEntry.get().symbol);

        if (!(typeOfSymbol instanceof BTypedescType)) {
            return completionItems;
        }

        return getAnnotationTags(context, (BTypedescType) typeOfSymbol);
    }

    private List<LSCompletionItem> getAnnotationTags(LSContext context, BTypedescType typedescType) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        AttachPoint.Point attachPoint = getAttachPointForType(typedescType);

        if (attachPoint == null) {
            return new ArrayList<>();
        }

        if (onQualifiedNameIdentifier(context, nodeAtCursor)) {
            String alias = ((QualifiedNameReferenceNode) nodeAtCursor).modulePrefix().text();
            Optional<Scope.ScopeEntry> scopeEntry = CommonUtil.packageSymbolFromAlias(context, alias);

            return scopeEntry.map(value -> value.symbol.scope.entries.values().stream()
                    .filter(entry -> {
                        BSymbol symbol = entry.symbol;
                        return symbol instanceof BAnnotationSymbol && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC
                                && ((BAnnotationSymbol) symbol).points.stream()
                                .anyMatch(aPoint -> aPoint.point.getValue().equals(attachPoint.getValue()));
                    })
                    .map(entry -> {
                        BAnnotationSymbol symbol = (BAnnotationSymbol) entry.symbol;
                        return getAnnotationsCompletionItem(context, symbol);
                    })
                    .collect(Collectors.toList())).orElseGet(ArrayList::new);

        }

        List<LSCompletionItem> completionItems = this.getPackagesCompletionItems(context);
        List<Scope.ScopeEntry> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        visibleSymbols.stream()
                .filter(scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return symbol instanceof BAnnotationSymbol && ((BAnnotationSymbol) symbol).points.stream()
                            .anyMatch(aPoint -> aPoint.point.getValue().equals(attachPoint.getValue()));
                })
                .forEach(entry -> {
                    BAnnotationSymbol symbol = (BAnnotationSymbol) entry.symbol;
                    completionItems.add(getAnnotationsCompletionItem(context, symbol));
                });

        return completionItems;
    }

    private AttachPoint.Point getAttachPointForType(BTypedescType typedescType) {
        BType type = typedescType.constraint.tsymbol.type;
        if (type instanceof BServiceType) {
            return AttachPoint.Point.SERVICE;
        } else if (type instanceof BInvokableType) {
            return AttachPoint.Point.FUNCTION;
        } else {
            return null;
        }
    }

    private LSCompletionItem getAnnotationsCompletionItem(LSContext ctx, BAnnotationSymbol annotationSymbol) {
        BTypeSymbol attachedType = annotationSymbol.attachedType;
        CompletionItem item = new CompletionItem();
        item.setInsertText(annotationSymbol.name.value);
        item.setLabel(annotationSymbol.name.value);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        item.setKind(CompletionItemKind.Property);

        return new SymbolCompletionItem(ctx, attachedType, item);
    }
}
