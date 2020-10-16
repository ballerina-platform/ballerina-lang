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
import io.ballerina.compiler.api.symbols.Qualifier;
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
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.FieldCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<? extends BallerinaTypeDescriptor> getTypeDescForFieldAccess(LSContext context,
                                                                                 FieldAccessExpressionNode node) {
        String fieldName = ((SimpleNameReferenceNode) node.fieldName()).name().text();
        ExpressionNode expressionNode = node.expression();
        Optional<? extends BallerinaTypeDescriptor> typeDescriptor = this.getTypeDesc(context, expressionNode);

        if (typeDescriptor.isEmpty()) {
            return Optional.empty();
        }

        List<FieldDescriptor> fieldDescriptors = new ArrayList<>();

        if (typeDescriptor.get().kind() == TypeDescKind.OBJECT) {
            fieldDescriptors.addAll(((ObjectTypeDescriptor) typeDescriptor.get()).fieldDescriptors());
        } else if (typeDescriptor.get().kind() == TypeDescKind.RECORD) {
            fieldDescriptors.addAll(((RecordTypeDescriptor) typeDescriptor.get()).fieldDescriptors());
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

        List<MethodSymbol> visibleMethods = fieldTypeDesc.get().builtinMethods();
        if (fieldTypeDesc.get().kind() == TypeDescKind.OBJECT) {
            visibleMethods.addAll(((ObjectTypeDescriptor) fieldTypeDesc.get()).methods());
        }
        Optional<MethodSymbol> filteredMethod = visibleMethods.stream()
                .filter(methodSymbol -> methodSymbol.name().equals(methodName))
                .findFirst();

        if (filteredMethod.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(filteredMethod.get().typeDescriptor());
    }

    private List<LSCompletionItem> getCompletionsForTypeDesc(LSContext context,
                                                             BallerinaTypeDescriptor typeDescriptor) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        switch (typeDescriptor.kind()) {
            case RECORD:
                ((RecordTypeDescriptor) typeDescriptor).fieldDescriptors().forEach(fieldDescriptor -> {
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(fieldDescriptor.name());
                    completionItem.setInsertText(fieldDescriptor.name());
                    completionItem.setDetail(fieldDescriptor.typeDescriptor().signature());
                    completionItems.add(new FieldCompletionItem(context, fieldDescriptor, completionItem));
                });
                break;
            case OBJECT:
                ObjectTypeDescriptor objTypeDesc = (ObjectTypeDescriptor) typeDescriptor;
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

    public List<LSCompletionItem> getObjectMethods(LSContext context,
                                                   ObjectTypeDescriptor objectTypeDesc,
                                                   String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectTypeDesc.moduleID().moduleName();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract the method entries
        List<Symbol> methods = objectTypeDesc.methods().stream()
                .filter(symbol -> {
                    boolean isPrivate = symbol.qualifiers().contains(Qualifier.PRIVATE);
                    boolean isPublic = symbol.qualifiers().contains(Qualifier.PUBLIC);

                    return !((symbol.name().contains(".init") && !"self".equals(symbolName))
                            || (isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                }).collect(Collectors.toList());

        return this.getCompletionItemList(methods, context);
    }

    public List<LSCompletionItem> getObjectFields(LSContext context,
                                                  ObjectTypeDescriptor objectTypeDesc,
                                                  String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectTypeDesc.moduleID().moduleName();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract Field Entries
        List<FieldDescriptor> fields = objectTypeDesc.fieldDescriptors().stream()
                .filter(fieldDescriptor -> {
                    boolean isPrivate = fieldDescriptor.qualifier().isPresent()
                            && fieldDescriptor.qualifier().get() == Qualifier.PRIVATE;
                    boolean isPublic = fieldDescriptor.qualifier().isPresent()
                            && fieldDescriptor.qualifier().get() == Qualifier.PUBLIC;
                    return !((isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toList());

        List<LSCompletionItem> completionItems = new ArrayList<>();

        for (FieldDescriptor field : fields) {
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(field.name());
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(field.name());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());

            completionItems.add(new FieldCompletionItem(context, field, fieldItem));
        }

        return completionItems;
    }
}
