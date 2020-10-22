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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
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
    protected List<LSCompletionItem> getEntries(LSContext ctx, ExpressionNode expr) {
        Optional<? extends BallerinaTypeDescriptor> typeDesc = this.getTypeDesc(ctx, expr);
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

    private Optional<? extends BallerinaTypeDescriptor> getTypeDesc(LSContext ctx, ExpressionNode expr) {
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

            default:
                return Optional.empty();
        }
    }

    private Optional<? extends BallerinaTypeDescriptor> getTypeDescForFieldAccess(LSContext context,
                                                                                  FieldAccessExpressionNode node) {
        String fieldName = ((SimpleNameReferenceNode) node.fieldName()).name().text();
        ExpressionNode expressionNode = node.expression();
        Optional<? extends BallerinaTypeDescriptor> typeDescriptor = this.getTypeDesc(context, expressionNode);

        if (typeDescriptor.isEmpty()) {
            return Optional.empty();
        }

        List<FieldDescriptor> fieldDescriptors = new ArrayList<>();
        BallerinaTypeDescriptor rawType = CommonUtil.getRawType(typeDescriptor.get());
        if (rawType.kind() == TypeDescKind.OBJECT) {
            fieldDescriptors.addAll(((ObjectTypeDescriptor) rawType).fieldDescriptors());
        } else if (rawType.kind() == TypeDescKind.RECORD) {
            fieldDescriptors.addAll(((RecordTypeDescriptor) rawType).fieldDescriptors());
        }

        return fieldDescriptors.stream()
                .filter(fieldDescriptor -> fieldDescriptor.name().equals(fieldName))
                .map(FieldDescriptor::typeDescriptor)
                .findAny();
    }

    private Optional<? extends BallerinaTypeDescriptor> getTypeDescForNameRef(LSContext context,
                                                                              NameReferenceNode referenceNode) {
        if (referenceNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) referenceNode).name().text();
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<Symbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(name))
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbolRef.get());
    }

    private Optional<? extends BallerinaTypeDescriptor> getTypeDescForFunctionCall(LSContext context,
                                                                                   FunctionCallExpressionNode expr) {
        String fName = ((SimpleNameReferenceNode) expr.functionName()).name().text();
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<FunctionSymbol> symbolRef = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(fName) && symbol.kind() == SymbolKind.FUNCTION)
                .map(symbol -> (FunctionSymbol) symbol)
                .findFirst();
        if (symbolRef.isEmpty()) {
            return Optional.empty();
        }

        return symbolRef.get().typeDescriptor().returnTypeDescriptor();
    }

    private Optional<? extends BallerinaTypeDescriptor> getTypeDescForMethodCall(LSContext context,
                                                                                 MethodCallExpressionNode node) {
        String methodName = ((SimpleNameReferenceNode) node.methodName()).name().text();

        Optional<? extends BallerinaTypeDescriptor> fieldTypeDesc = this.getTypeDesc(context, node.expression());

        if (fieldTypeDesc.isEmpty()) {
            return Optional.empty();
        }
        BallerinaTypeDescriptor rawType = CommonUtil.getRawType(fieldTypeDesc.get());
        List<MethodSymbol> visibleMethods = rawType.builtinMethods();
        if (rawType.kind() == TypeDescKind.OBJECT) {
            visibleMethods.addAll(((ObjectTypeDescriptor) rawType).methods());
        }
        Optional<MethodSymbol> filteredMethod = visibleMethods.stream()
                .filter(methodSymbol -> methodSymbol.name().equals(methodName))
                .findFirst();

        if (filteredMethod.isEmpty()) {
            return Optional.empty();
        }

        return filteredMethod.get().typeDescriptor().returnTypeDescriptor();
    }

    private List<LSCompletionItem> getCompletionsForTypeDesc(LSContext context,
                                                             BallerinaTypeDescriptor typeDescriptor) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        BallerinaTypeDescriptor rawType = CommonUtil.getRawType(typeDescriptor);
        switch (rawType.kind()) {
            case RECORD:
                ((RecordTypeDescriptor) rawType).fieldDescriptors().forEach(fieldDescriptor -> {
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(fieldDescriptor.name());
                    completionItem.setInsertText(fieldDescriptor.name());
                    completionItem.setDetail(fieldDescriptor.typeDescriptor().signature());
                    completionItems.add(new FieldCompletionItem(context, fieldDescriptor, completionItem));
                });
                break;
            case OBJECT:
                ObjectTypeDescriptor objTypeDesc = (ObjectTypeDescriptor) rawType;
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
        completionItems.addAll(this.getCompletionItemList(typeDescriptor.builtinMethods(), context));

        return completionItems;
    }
}
