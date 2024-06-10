/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter.completion.resolver;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.completion.context.CompletionContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Symbol resolver for the field access expressions.
 * This visitor will go through the nodes and capture the respective types of the particular node.
 *
 * @since 2.0.0
 */
public class FieldAccessCompletionResolver extends NodeTransformer<Optional<TypeSymbol>> {

    private final CompletionContext context;

    public FieldAccessCompletionResolver(CompletionContext context) {
        this.context = context;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        SuspendedContext suspendedContext = context.getSuspendedContext();
        Optional<Symbol> symbol = this.getSymbolByName(
                context.visibleSymbols(suspendedContext.getLineNumber() - 1, 0), node.name().text());

        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        return getTypeDescriptor(symbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        // First capture the expression and the respective symbols
        Optional<TypeSymbol> typeSymbol = node.expression().apply(this);

        NameReferenceNode fieldName = node.fieldName();
        if (fieldName.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE || typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) fieldName).name().text();
        List<Symbol> visibleEntries = this.getVisibleEntries(typeSymbol.get(), node.expression());
        Optional<Symbol> filteredSymbol = this.getSymbolByName(visibleEntries, name);

        if (filteredSymbol.isEmpty()) {
            return Optional.empty();
        }

        return getTypeDescriptor(filteredSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(OptionalFieldAccessExpressionNode node) {
        // First capture the expression and the respective symbols
        // In future we should use the following approach and get rid of this resolver.
        Optional<TypeSymbol> resolvedType = this.context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node));
        if (resolvedType.isPresent() && resolvedType.get().typeKind() != TypeDescKind.COMPILATION_ERROR) {
            return getTypeDescriptor(resolvedType.get());
        }

        Optional<TypeSymbol> typeSymbol = node.expression().apply(this);
        NameReferenceNode fieldName = node.fieldName();
        if (fieldName.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE || typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) fieldName).name().text();
        List<Symbol> visibleEntries = this.getVisibleEntries(typeSymbol.get(), node.expression());
        Optional<Symbol> filteredSymbol = this.getSymbolByName(visibleEntries, name);

        if (filteredSymbol.isEmpty()) {
            return Optional.empty();
        }

        return getTypeDescriptor(filteredSymbol.get());
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
        List<Symbol> visibleEntries = this.getVisibleEntries(exprTypeSymbol.orElseThrow(), node.expression());

        FunctionSymbol symbol = (FunctionSymbol) this.getSymbolByName(visibleEntries, methodName, predicate);
        FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) getTypeDescriptor(symbol).orElseThrow();

        return functionTypeSymbol.returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        // TODO: Needs to be implemented
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType = node.containerExpression().apply(this);
        TypeSymbol rawType = getRawType(containerType.orElseThrow());
        if (rawType.typeKind() == TypeDescKind.ARRAY) {
            return Optional.of(((ArrayTypeSymbol) rawType).memberTypeDescriptor());
        }
        if (rawType.typeKind() == TypeDescKind.MAP) {
            return Optional.of(((MapTypeSymbol) rawType).typeParam());
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(AnnotAccessExpressionNode node) {
        return this.context.currentSemanticModel().flatMap(semanticModel -> semanticModel.typeOf(node));
    }

    @Override
    public Optional<TypeSymbol> transform(BasicLiteralNode node) {
        return this.context.currentSemanticModel().flatMap(semanticModel -> semanticModel.typeOf(node));
    }

    @Override
    public Optional<TypeSymbol> transform(BracedExpressionNode node) {
        return node.expression().apply(this);
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
        Optional<TypeSymbol> typeSymbol = getTypeSymbol(node);
        return this.getVisibleEntries(typeSymbol.get(), node);
    }

    /**
     * Returns the TypeSymbol given the node.
     *
     * @param node Node of which the TypeSymbol should be resolved
     * @return {@link TypeSymbol}
     */
    public Optional<TypeSymbol> getTypeSymbol(Node node) {
        return node.apply(this);
    }

    private Optional<Symbol> getSymbolByName(List<Symbol> visibleSymbols, String name) {
        return visibleSymbols.stream()
                .filter((symbol -> symbol.nameEquals(name)))
                .findFirst();
    }

    private Symbol getSymbolByName(List<Symbol> visibleSymbols, String name, Predicate<Symbol> predicate) {
        Predicate<Symbol> namePredicate = symbol -> symbol.nameEquals(name);
        return visibleSymbols.stream()
                .filter(namePredicate.and(predicate))
                .findFirst().orElseThrow();
    }

    private List<Symbol> getVisibleEntries(TypeSymbol typeSymbol, Node node) {
        List<Symbol> visibleEntries = new ArrayList<>();
        TypeSymbol rawType = getRawType(typeSymbol);
        switch (rawType.typeKind()) {
            case RECORD:
                // If the invoked for field access expression, then avoid suggesting the optional fields
                List<RecordFieldSymbol> filteredEntries =
                        new ArrayList<>(((RecordTypeSymbol) rawType).fieldDescriptors().values());
                visibleEntries.addAll(filteredEntries);
                break;
            case OBJECT:
                // add class field access test case as well
                Package currentPkg = context.getSuspendedContext().getProject().currentPackage();
                Document currentModule = context.getSuspendedContext().getDocument();

                ObjectTypeSymbol objTypeDesc = (ObjectTypeSymbol) rawType;
                visibleEntries.addAll(objTypeDesc.fieldDescriptors().values().stream()
                        .filter(objectFieldSymbol -> withValidAccessModifiers(node, objectFieldSymbol, currentPkg,
                                currentModule.module().moduleId())).toList());
                boolean isClient = isClient(objTypeDesc);
                boolean isService = getTypeDescForObjectSymbol(objTypeDesc)
                        .qualifiers().contains(Qualifier.SERVICE);
                // If the object type desc is a client, then we avoid all the remote methods
                List<MethodSymbol> methodSymbols = objTypeDesc.methods().values().stream()
                        .filter(methodSymbol -> ((!isClient && !isService)
                                || !methodSymbol.qualifiers().contains(Qualifier.REMOTE))
                                && !methodSymbol.qualifiers().contains(Qualifier.RESOURCE)
                                && withValidAccessModifiers(node, methodSymbol, currentPkg,
                                currentModule.module().moduleId()))
                        .toList();
                visibleEntries.addAll(methodSymbols);
                break;
            default:
                break;
        }
        visibleEntries.addAll(typeSymbol.langLibMethods());

        return visibleEntries;
    }

    private boolean withValidAccessModifiers(Node exprNode, Symbol symbol, Package currentPackage,
                                             ModuleId currentModule) {
        Optional<ModuleSymbol> symbolModule = symbol.getModule();

        boolean isPrivate = false;
        boolean isPublic = false;
        boolean isResource = false;

        if (symbol instanceof Qualifiable qSymbol) {
            isPrivate = qSymbol.qualifiers().contains(Qualifier.PRIVATE);
            isPublic = qSymbol.qualifiers().contains(Qualifier.PUBLIC);
            isResource = qSymbol.qualifiers().contains(Qualifier.RESOURCE);
        }

        if (exprNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                && ((SimpleNameReferenceNode) exprNode).name().text().equals(Names.SELF.getValue())
                && !isResource) {
            return true;
        }

        ModuleID objModuleId = symbolModule.get().id();
        return isPublic || (!isPrivate && objModuleId.moduleName().equals(currentModule.moduleName())
                && objModuleId.orgName().equals(currentPackage.packageOrg().value()));
    }

    /**
     * Check whether the given symbol is a symbol with the type Object.
     *
     * @param symbol to evaluate
     * @return {@link Boolean} whether the symbol holds the type object
     */
    private boolean isObject(Symbol symbol) {
        TypeSymbol typeDescriptor;
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();
                break;
            case VARIABLE:
                typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                break;
            case PARAMETER:
                typeDescriptor = ((ParameterSymbol) symbol).typeDescriptor();
                break;
            case CLASS:
                typeDescriptor = (ClassSymbol) symbol;
                break;
            case TYPE:
                typeDescriptor = (TypeSymbol) symbol;
                break;
            default:
                return false;
        }
        return getRawType(typeDescriptor).typeKind() == TypeDescKind.OBJECT;
    }

    /**
     * Get the type descriptor of the given symbol.
     * If the symbol is not a variable symbol this method will return empty optional value
     *
     * @param symbol to evaluate
     * @return {@link Optional} type descriptor
     */
    private Optional<TypeSymbol> getTypeDescriptor(Symbol symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                return Optional.ofNullable(((TypeDefinitionSymbol) symbol).typeDescriptor());
            case VARIABLE:
                return Optional.ofNullable(((VariableSymbol) symbol).typeDescriptor());
            case PARAMETER:
                return Optional.ofNullable(((ParameterSymbol) symbol).typeDescriptor());
            case ANNOTATION:
                return ((AnnotationSymbol) symbol).typeDescriptor();
            case FUNCTION:
            case METHOD:
                return Optional.ofNullable(((FunctionSymbol) symbol).typeDescriptor());
            case CONSTANT:
            case ENUM_MEMBER:
                return Optional.ofNullable(((ConstantSymbol) symbol).typeDescriptor());
            case CLASS:
                return Optional.of((ClassSymbol) symbol);
            case RECORD_FIELD:
                return Optional.ofNullable(((RecordFieldSymbol) symbol).typeDescriptor());
            case OBJECT_FIELD:
                return Optional.of(((ObjectFieldSymbol) symbol).typeDescriptor());
            case CLASS_FIELD:
                return Optional.of(((ClassFieldSymbol) symbol).typeDescriptor());
            case TYPE:
                return Optional.of((TypeSymbol) symbol);
            default:
                return Optional.empty();
        }
    }

    /**
     * Get the type descriptor for the object symbol.
     *
     * @param symbol to evaluate
     * @return {@link ObjectTypeSymbol} for the object symbol
     */
    private ObjectTypeSymbol getTypeDescForObjectSymbol(Symbol symbol) {
        Optional<? extends TypeSymbol> typeDescriptor = getTypeDescriptor(symbol);
        if (typeDescriptor.isEmpty() || !isObject(symbol)) {
            throw new UnsupportedOperationException("Cannot find a valid type descriptor");
        }
        return (ObjectTypeSymbol) getRawType(typeDescriptor.get());
    }

    /**
     * Check Whether the provided symbol is a client symbol.
     *
     * @param symbol to be evaluated
     * @return {@link Boolean} status of the evaluation
     */
    private boolean isClient(Symbol symbol) {
        if (!isObject(symbol)) {
            return false;
        }
        ObjectTypeSymbol typeDesc = getTypeDescForObjectSymbol(symbol);
        return typeDesc.qualifiers().contains(Qualifier.CLIENT);
    }

    /**
     * Get the raw type of the type descriptor. If the type descriptor is a type reference then return the associated
     * type descriptor.
     *
     * @param typeDescriptor type descriptor to evaluate
     * @return {@link TypeSymbol} extracted type descriptor
     */
    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.INTERSECTION) {
            return getRawType(((IntersectionTypeSymbol) typeDescriptor).effectiveTypeDescriptor());
        }
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            if (typeRef.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
                return getRawType(((IntersectionTypeSymbol) typeRef.typeDescriptor()).effectiveTypeDescriptor());
            }
            return typeRef.typeDescriptor();
        }
        return typeDescriptor;
    }
}
