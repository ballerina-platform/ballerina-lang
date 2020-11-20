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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.FieldCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic Completion provider for field access providers.
 * eg: Optional Field access and Field Access
 *
 * @param <T> Field access node type
 * @since 2.0.0
 */
public abstract class FieldAccessContext<T extends Node> extends AbstractCompletionProvider<T> {

    public FieldAccessContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    /**
     * Get the entries for the given field access expression.
     * This particular logic is written in order to capture the chain completion usage as well.
     *
     * @param ctx  language server operation context
     * @param expr expression node to evaluate
     * @return {@link List} of filtered scope entries
     */
    protected List<LSCompletionItem> getEntries(CompletionContext ctx, ExpressionNode expr) {
        Optional<? extends TypeSymbol> typeDesc = this.getTypeDesc(ctx, expr);
        if (typeDesc.isEmpty()) {
            return new ArrayList<>();
        }

        return this.getCompletionsForTypeDesc(ctx, typeDesc.get());
    }


    /**
     * Whether to remove the optional fields during the record fields filtering phase.
     *
     * @return {@link Boolean} optional field removal status
     */
    protected abstract boolean removeOptionalFields();

    private Optional<? extends TypeSymbol> getTypeDesc(CompletionContext ctx, ExpressionNode expr) {
        switch (expr.kind()) {
            case SIMPLE_NAME_REFERENCE:
                /*
                Captures the following
                (1) fieldName.<cursor>
                (2) fieldName.t<cursor>
                 */
                return this.getTypeDescForNameRef(ctx, (SimpleNameReferenceNode) expr);
            case FUNCTION_CALL:
                /*
                Captures the following
                (1) functionName().<cursor>
                (2) functionName().t<cursor>
                 */
                return this.getTypeDescForFunctionCall(ctx, (FunctionCallExpressionNode) expr);
            case METHOD_CALL: {
                /*
                Address the following
                (1) test.testMethod().<cursor>
                (2) test.testMethod().t<cursor>
                 */
                return this.getTypeDescForMethodCall(ctx, (MethodCallExpressionNode) expr);
            }
            case FIELD_ACCESS: {
                /*
                Address the following
                (1) test1.test2.<cursor>
                (2) test1.test2.t<cursor>
                 */
                return this.getTypeDescForFieldAccess(ctx, (FieldAccessExpressionNode) expr);
            }
            case INDEXED_EXPRESSION: {
                /*
                Address the following
                (1) test1[].<cursor>
                (2) test1[].t<cursor>
                 */
                return this.getTypeDescForIndexedExpr(ctx, (IndexedExpressionNode) expr);
            }

            default:
                return Optional.empty();
        }
    }

    private Optional<? extends TypeSymbol> getTypeDescForFieldAccess(CompletionContext context,
                                                                     FieldAccessExpressionNode node) {
        String fieldName = ((SimpleNameReferenceNode) node.fieldName()).name().text();
        ExpressionNode expressionNode = node.expression();
        Optional<? extends TypeSymbol> typeDescriptor = this.getTypeDesc(context, expressionNode);

        if (typeDescriptor.isEmpty()) {
            return Optional.empty();
        }

        List<FieldSymbol> fieldSymbols = new ArrayList<>();
        TypeSymbol rawType = CommonUtil.getRawType(typeDescriptor.get());
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            fieldSymbols.addAll(((ObjectTypeSymbol) rawType).fieldDescriptors());
        } else if (rawType.typeKind() == TypeDescKind.RECORD) {
            fieldSymbols.addAll(((RecordTypeSymbol) rawType).fieldDescriptors());
        }

        return fieldSymbols.stream()
                .filter(fieldDescriptor -> fieldDescriptor.name().equals(fieldName))
                .map(FieldSymbol::typeDescriptor)
                .findAny();
    }

    private Optional<? extends TypeSymbol> getTypeDescForNameRef(CompletionContext context,
                                                                 NameReferenceNode referenceNode) {
        if (referenceNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) referenceNode).name().text();
        List<Symbol> visibleSymbols = context.getVisibleSymbols(context.getCursorPosition());
        Optional<Symbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(name))
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbolRef.get());
    }

    private Optional<? extends TypeSymbol> getTypeDescForFunctionCall(CompletionContext context,
                                                                      FunctionCallExpressionNode expr) {
        String fName = ((SimpleNameReferenceNode) expr.functionName()).name().text();
        List<Symbol> visibleSymbols = context.getVisibleSymbols(context.getCursorPosition());
        Optional<FunctionSymbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(fName) && symbol.kind() == SymbolKind.FUNCTION)
                .map(symbol -> (FunctionSymbol) symbol)
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return symbolRef.get().typeDescriptor().returnTypeDescriptor();
    }

    private Optional<? extends TypeSymbol> getTypeDescForMethodCall(CompletionContext context,
                                                                    MethodCallExpressionNode node) {
        String methodName = ((SimpleNameReferenceNode) node.methodName()).name().text();

        Optional<? extends TypeSymbol> fieldTypeDesc = this.getTypeDesc(context, node.expression());

        if (fieldTypeDesc.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol rawType = CommonUtil.getRawType(fieldTypeDesc.get());
        List<FunctionSymbol> visibleMethods = rawType.langLibMethods();
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            visibleMethods.addAll(((ObjectTypeSymbol) rawType).methods());
        }
        Optional<FunctionSymbol> filteredMethod = visibleMethods.stream()
                .filter(methodSymbol -> methodSymbol.name().equals(methodName))
                .findFirst();

        if (filteredMethod.isEmpty()) {
            return Optional.empty();
        }

        return filteredMethod.get().typeDescriptor().returnTypeDescriptor();
    }

    private Optional<? extends TypeSymbol> getTypeDescForIndexedExpr(CompletionContext context,
                                                                     IndexedExpressionNode node) {
        Optional<? extends TypeSymbol> typeDesc = getTypeDesc(context, node.containerExpression());

        if (typeDesc.isEmpty() || typeDesc.get().typeKind() != TypeDescKind.ARRAY) {
            return Optional.empty();
        }

        return Optional.of(((ArrayTypeSymbol) typeDesc.get()).memberTypeDescriptor());
    }

    private List<LSCompletionItem> getCompletionsForTypeDesc(CompletionContext context, TypeSymbol typeDescriptor) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        TypeSymbol rawType = CommonUtil.getRawType(typeDescriptor);
        switch (rawType.typeKind()) {
            case RECORD:
                ((RecordTypeSymbol) rawType).fieldDescriptors().forEach(fieldDescriptor -> {
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(fieldDescriptor.name());
                    completionItem.setInsertText(fieldDescriptor.name());
                    completionItem.setDetail(fieldDescriptor.typeDescriptor().signature());
                    completionItems.add(new FieldCompletionItem(context, fieldDescriptor, completionItem));
                });
                break;
            case OBJECT:
                ObjectTypeSymbol objTypeDesc = (ObjectTypeSymbol) rawType;
                objTypeDesc.fieldDescriptors().forEach(fieldDescriptor -> {
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(fieldDescriptor.name());
                    completionItem.setInsertText(fieldDescriptor.name());
                    completionItem.setDetail(fieldDescriptor.typeDescriptor().signature());
                    completionItems.add(new FieldCompletionItem(context, fieldDescriptor, completionItem));
                });
                completionItems.addAll(this.getCompletionItemList(objTypeDesc.methods(), context));
                break;
            default:
                break;
        }
        completionItems.addAll(this.getCompletionItemList(typeDescriptor.langLibMethods(), context));

        return completionItems;
    }
}
