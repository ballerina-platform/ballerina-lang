/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.PositionedOperationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Symbol resolver for the field access expressions.
 * This visitor will go through the nodes and capture the respective types of the particular node.
 *
 * @since 2.0.0
 */
public class FieldAccessCompletionResolver extends NodeTransformer<Optional<TypeSymbol>> {
    private final PositionedOperationContext context;
    private final boolean optionalFieldAccess;

    public FieldAccessCompletionResolver(PositionedOperationContext context, boolean optionalFieldAccess) {
        this.context = context;
        this.optionalFieldAccess = optionalFieldAccess;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        Symbol symbol = this.getSymbolByName(context.visibleSymbols(context.getCursorPosition()), node.name().text());

        return SymbolUtil.getTypeDescriptor(symbol);
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        // First capture the expression and the respective symbols
        Optional<TypeSymbol> typeSymbol = node.expression().apply(this);
//        this.getVisibleEntries(typeSymbol.orElseThrow());

        NameReferenceNode fieldName = node.fieldName();
        if (fieldName.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) fieldName).name().text();
        Symbol filteredSymbol = this.getSymbolByName(this.getVisibleEntries(typeSymbol.orElseThrow()), name);

        return SymbolUtil.getTypeDescriptor(filteredSymbol);
    }

    @Override
    public Optional<TypeSymbol> transform(MethodCallExpressionNode node) {
        Optional<TypeSymbol> exprTypeSymbol = node.expression().apply(this);
        NameReferenceNode nameRef = node.methodName();
        if (nameRef.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.METHOD
                || symbol.kind() == SymbolKind.FUNCTION;
        String methodName = ((SimpleNameReferenceNode) nameRef).name().text();
        List<Symbol> visibleEntries = this.getVisibleEntries(exprTypeSymbol.orElseThrow());

        FunctionSymbol symbol = (FunctionSymbol) this.getSymbolByName(visibleEntries, methodName, predicate);
        FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) SymbolUtil.getTypeDescriptor(symbol).orElseThrow();

        return functionTypeSymbol.returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        NameReferenceNode nameRef = node.functionName();

        if (nameRef.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }

        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.FUNCTION;
        String functionName = ((SimpleNameReferenceNode) nameRef).name().text();
        List<Symbol> visibleEntries = context.visibleSymbols(context.getCursorPosition());

        FunctionSymbol symbol = (FunctionSymbol) this.getSymbolByName(visibleEntries, functionName, predicate);
        return symbol.typeDescriptor().returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType = node.containerExpression().apply(this);
        TypeSymbol rawType = CommonUtil.getRawType(containerType.orElseThrow());
        if (rawType.typeKind() == TypeDescKind.ARRAY) {
            return Optional.of(((ArrayTypeSymbol) rawType).memberTypeDescriptor());
        }
        if (rawType.typeKind() == TypeDescKind.MAP) {
            return ((MapTypeSymbol) rawType).typeParameter();
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(AnnotAccessExpressionNode node) {
        return this.context.currentSemanticModel().orElseThrow().type(node);
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return Optional.empty();
    }

    /**
     * Get the visible symbol entries on the type of the node.
     *
     * @param node to be evaluated
     * @return {@link List}
     */
    public List<Symbol> getVisibleEntries(Node node) {
        Optional<TypeSymbol> typeSymbol = node.apply(this);
        return typeSymbol.map(this::getVisibleEntries).orElse(Collections.emptyList());
    }

    private Symbol getSymbolByName(List<Symbol> visibleSymbols, String name) {
        return visibleSymbols.stream()
                .filter((symbol -> symbol.getName().orElse("").equals(name)))
                .findFirst().orElseThrow();
    }

    private Symbol getSymbolByName(List<Symbol> visibleSymbols, String name, @Nonnull Predicate<Symbol> predicate) {
        Predicate<Symbol> namePredicate = symbol -> symbol.getName().orElse("").equals(name);
        return visibleSymbols.stream()
                .filter(namePredicate.and(predicate))
                .findFirst().orElseThrow();
    }

    private List<Symbol> getVisibleEntries(TypeSymbol typeSymbol) {
        List<Symbol> visibleEntries = new ArrayList<>();
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        switch (rawType.typeKind()) {
            case RECORD:
                // If the invoked for field access expression, then avoid suggesting the optional fields
                List<RecordFieldSymbol> filteredEntries =
                        ((RecordTypeSymbol) rawType).fieldDescriptors().values().stream()
                                .filter(recordFieldSymbol -> this.optionalFieldAccess
                                        || !recordFieldSymbol.isOptional())
                                .collect(Collectors.toList());
                visibleEntries.addAll(filteredEntries);
                break;
            case OBJECT: // add class field access test case as well
                ObjectTypeSymbol objTypeDesc = (ObjectTypeSymbol) rawType;
                visibleEntries.addAll(objTypeDesc.fieldDescriptors().values());
                boolean isClient = SymbolUtil.isClient(objTypeDesc);
                // If the object type desc is a client, then we avoid all the remote methods
                List<MethodSymbol> methodSymbols = objTypeDesc.methods().values().stream()
                        .filter(methodSymbol -> (!isClient || !methodSymbol.qualifiers().contains(Qualifier.REMOTE))
                                && !methodSymbol.qualifiers().contains(Qualifier.RESOURCE))
                        .collect(Collectors.toList());
                visibleEntries.addAll(methodSymbols);
                break;
            default:
                break;
        }
        visibleEntries.addAll(typeSymbol.langLibMethods());

        return visibleEntries;
    }
}
