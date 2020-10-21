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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.AttachPoint;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;
import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;

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
//        List<LSCompletionItem> completionItems = new ArrayList<>();
//        Optional<Symbol> expressionEntry = this.getExpressionEntry(context, node.expression());

        // Fixme
//        if (!expressionEntry.isPresent()) {
//            return completionItems;
//        }

//        BType typeOfSymbol = CommonUtil.getTypeOfSymbol(expressionEntry.get());
//
//        if (!(typeOfSymbol instanceof BTypedescType)) {
//            return completionItems;
//        }
//
//        return getAnnotationTags(context, (BTypedescType) typeOfSymbol);

        return new ArrayList<>();
    }

    public List<LSCompletionItem> getAnnotationTags(LSContext context, BTypedescType typedescType) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        AttachPoint.Point attachPoint = getAttachPointForType(typedescType);

        if (attachPoint == null) {
            return new ArrayList<>();
        }

        if (onQualifiedNameIdentifier(context, nodeAtCursor)) {
//            String alias = ((QualifiedNameReferenceNode) nodeAtCursor).modulePrefix().text();
//            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context, alias);

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
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == ANNOTATION && ((AnnotationSymbol) symbol).attachPoints()
                        .stream()
                        .anyMatch(aPoint -> aPoint.name().equals(attachPoint.getValue())))
                .forEach(annotation -> {
                    AnnotationSymbol symbol = (AnnotationSymbol) annotation;
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

    private LSCompletionItem getAnnotationsCompletionItem(LSContext ctx, AnnotationSymbol annotationSymbol) {
        Optional<BallerinaTypeDescriptor> attachedType = annotationSymbol.typeDescriptor();
        CompletionItem item = new CompletionItem();
        item.setInsertText(annotationSymbol.name());
        item.setLabel(annotationSymbol.name());
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        item.setKind(CompletionItemKind.Property);

        return new TypeCompletionItem(ctx, attachedType.get(), item);
    }

    /**
     * Get the expression entry, given the node.
     *
     * @param context        language server context
     * @param expressionNode expression node
     * @return {@link Optional} scope entry for the node
     */
    private Optional<Symbol> getExpressionEntry(LSContext context, Node expressionNode) {
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);

        switch (expressionNode.kind()) {
            case SIMPLE_NAME_REFERENCE:
                String nameRef = ((SimpleNameReferenceNode) expressionNode).name().text();
                for (Symbol symbol : visibleSymbols) {
                    if (symbol.name().equals(nameRef)) {
                        return Optional.of(symbol);
                    }
                }
                return Optional.empty();
            case FUNCTION_CALL:
                NameReferenceNode refName = ((FunctionCallExpressionNode) expressionNode).functionName();
                if (refName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    String alias = ((QualifiedNameReferenceNode) refName).modulePrefix().text();
                    String fName = ((QualifiedNameReferenceNode) refName).identifier().text();
                    Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context, alias);
                    if (moduleSymbol.isEmpty()) {
                        return Optional.empty();
                    }
                    for (FunctionSymbol functionSymbol : moduleSymbol.get().functions()) {
                        if (functionSymbol.name().equals(fName)) {
                            return Optional.of(functionSymbol);
                        }
                    }
                    return Optional.empty();
                } else if (refName.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    String funcName = ((SimpleNameReferenceNode) refName).name().text();
                    for (Symbol symbol : visibleSymbols) {
                        if (symbol.kind() == FUNCTION && symbol.name().equals(funcName)) {
                            return Optional.of(symbol);
                        }
                    }
                    return Optional.empty();
                }
                break;
            default:
                break;
        }

        return Optional.empty();
    }
}
