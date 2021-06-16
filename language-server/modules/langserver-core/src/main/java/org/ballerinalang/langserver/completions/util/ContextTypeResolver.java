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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.PositionedOperationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * This visitor is used to resolve a type of a given context.
 * For example, consider the following example,
 * <pre>
 *     function sayHello() returns int {
 *         ...
 *         Person p = {[cursor]}
 *         ...
 *     }
 * </pre>
 * For the given cursor position the resolved type is Person. At the usage of this resolver, we provide the mapping
 * expression and the resolver traverse the parents to capture the possible type.
 * <p>
 * Consider the following
 *
 * <pre>
 *     function getPerson() PersonRecord {
 *         ...
 *         return {[cursor]}
 *     }
 * </pre>
 * For the above example the visible type is resolved by the return type of the function definition, which is
 * PersonRecord
 *
 * @since 2.0.0
 */
public class ContextTypeResolver extends NodeTransformer<Optional<TypeSymbol>> {
    private final PositionedOperationContext context;
    private final List<Node> visitedNodes = new ArrayList<>();

    public ContextTypeResolver(PositionedOperationContext context) {
        this.context = context;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        /* Here we extract the symbol from the name since we need to capture the type definition for the following
        eg: TypeName xyz = ...
        Otherwise this will capture the TypeReferenceType of TypeName which needs addition processing for handling 
         */
        Optional<Symbol> symbol = this.getSymbolByName(node.name().text());
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(SpecificFieldNode node) {
        // Support only the identifier nodes
        if (node.fieldName().kind() != SyntaxKind.IDENTIFIER_TOKEN) {
            return Optional.empty();
        }
        Optional<TypeSymbol> parentType = this.visit(node.parent());
        if (parentType.isEmpty() || CommonUtil.getRawType(parentType.get()).typeKind() != TypeDescKind.RECORD) {
            return Optional.empty();
        }
        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) CommonUtil.getRawType(parentType.get());
        String fieldName = ((IdentifierToken) node.fieldName()).text();
        // Extract the type of the particular field
        return recordTypeSymbol.fieldDescriptors().entrySet().stream()
                .filter(entry -> entry.getKey().equals(fieldName))
                .findFirst()
                .map(entry -> entry.getValue().typeDescriptor());
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode node) {
        // Resolves the type of the variable reference
        return this.visit(node.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(QualifiedNameReferenceNode node) {
        // Only handles the Type Definitions.
        Predicate<Symbol> predicate = symbol -> symbol.getName().isPresent()
                && (symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS)
                && symbol.getName().get().equals(node.identifier().text());
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, node, predicate);
        if (moduleContent.size() > 1) {
            // At the moment we do not handle the ambiguity. Hence consider only single item
            return Optional.empty();
        }

        Symbol symbol = moduleContent.get(0);
        if (symbol.kind() == SymbolKind.CLASS) {
            return Optional.of((ClassSymbol) symbol);
        }
        return Optional.of(((TypeDefinitionSymbol) symbol).typeDescriptor());
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType = this.visit(node.containerExpression());
        if (containerType.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.getRawContextType(containerType.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(CaptureBindingPatternNode node) {
        Optional<Symbol> variableSymbol = this.getSymbolByName(node.variableName().text());
        Optional<TypeSymbol> typeSymbol = variableSymbol.flatMap(SymbolUtil::getTypeDescriptor);
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.getRawContextType(typeSymbol.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(AnnotationNode node) {
        Node annotationRef = node.annotReference();
        Optional<Symbol> annotationSymbol;
        Predicate<Symbol> predicate = symbol -> symbol.getName().isPresent() && symbol.kind() == SymbolKind.ANNOTATION;

        if (annotationRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) annotationRef;
            Predicate<Symbol> qNamePredicate =
                    predicate.and(symbol -> symbol.getName().get().equals(qNameRef.identifier().text()));
            annotationSymbol = this.getTypeFromQNameReference(qNameRef, qNamePredicate);
        } else if (annotationRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            annotationSymbol = this.getSymbolByName(((SimpleNameReferenceNode) annotationRef).name().text(), predicate);
        } else {
            return Optional.empty();
        }

        if (annotationSymbol.isEmpty()) {
            return Optional.empty();
        }
        return ((AnnotationSymbol) annotationSymbol.get()).typeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode node) {
        /*
        For the function definition, we consider the return type. In order to support the record-type-descriptor
        and to get the particular symbol, we extract the type symbol from the function symbol. 
         */
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = node.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.FUNCTION;
        Optional<Symbol> functionSymbol = this.getSymbolByName(node.functionName().text(), predicate);

        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }

        return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        FieldAccessCompletionResolver resolver = new FieldAccessCompletionResolver(context);
        List<Symbol> visibleEntries = resolver.getVisibleEntries(node.expression());
        NameReferenceNode nameRef = node.fieldName();
        if (nameRef.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String fieldName = ((SimpleNameReferenceNode) nameRef).name().text();
        Optional<Symbol> filteredSymbol = visibleEntries.stream()
                .filter(symbol -> symbol.getName().isPresent() && symbol.getName().get().equals(fieldName))
                .findFirst();
        if (filteredSymbol.isEmpty()) {
            return Optional.empty();
        }
        
        return SymbolUtil.getTypeDescriptor(filteredSymbol.get());
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<Symbol> getTypeFromQNameReference(QualifiedNameReferenceNode node, Predicate<Symbol> predicate) {
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, node, predicate);
        if (moduleContent.size() > 1) {
            // At the moment we do not handle the ambiguity. Hence consider only single item
            return Optional.empty();
        }

        return Optional.ofNullable(moduleContent.get(0));
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }
        visitedNodes.add(node);
        return node.apply(this);
    }

    private Optional<Symbol> getSymbolByName(String name) {
        return this.context.visibleSymbols(context.getCursorPosition()).stream()
                .filter((symbol -> symbol.getName().orElse("").equals(name)))
                .findFirst();
    }

    private Optional<Symbol> getSymbolByName(String name, @Nonnull Predicate<Symbol> predicate) {
        Predicate<Symbol> namePredicate = symbol -> symbol.getName().orElse("").equals(name);
        return this.context.visibleSymbols(context.getCursorPosition()).stream()
                .filter(namePredicate.and(predicate))
                .findFirst();
    }

    private TypeSymbol getRawContextType(TypeSymbol typeSymbol) {
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        switch (rawType.typeKind()) {
            case MAP:
                return ((MapTypeSymbol) rawType).typeParam();
            case ARRAY:
                return ((ArrayTypeSymbol) rawType).memberTypeDescriptor();
            case TABLE:
                return ((TableTypeSymbol) rawType).rowTypeParameter();
            default:
                return rawType;
        }
    }
}
