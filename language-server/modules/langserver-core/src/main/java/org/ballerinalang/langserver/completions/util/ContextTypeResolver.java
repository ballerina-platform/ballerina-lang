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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
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
    public Optional<TypeSymbol> transform(ListenerDeclarationNode node) {
        Optional<TypeDescriptorNode> typeDesc = node.typeDescriptor();
        if (typeDesc.isEmpty()) {
            return Optional.empty();
        }

        return typeDesc.get().apply(this);
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
        Optional<TypeSymbol> parentType = node.parent().apply(new ContextTypeResolver(context));
        if (parentType.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol parentRawType = CommonUtil.getRawType(parentType.get());
        if (parentRawType.typeKind() == TypeDescKind.MAP) {
            TypeSymbol rawContextType = this.getRawContextType(parentType.get());
            return Optional.of(rawContextType);
        }

        if (parentRawType.typeKind() != TypeDescKind.RECORD
                || (node.fieldName().kind() != SyntaxKind.STRING_LITERAL
                && node.fieldName().kind() != SyntaxKind.IDENTIFIER_TOKEN)) {
            return Optional.empty();
        }

        if (node.fieldName().kind() == SyntaxKind.STRING_LITERAL) {
            return Optional.of(parentRawType);
        }

        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) parentRawType;
        String fieldName = ((IdentifierToken) node.fieldName()).text();
        // Extract the type of the particular field
        Optional<TypeSymbol> typeOfField = recordTypeSymbol.fieldDescriptors().entrySet().stream()
                .filter(entry -> entry.getKey().equals(fieldName))
                .findFirst()
                .map(entry -> entry.getValue().typeDescriptor());

        return typeOfField.isEmpty() ? Optional.empty()
                : Optional.of(typeOfField.get());
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
        if (moduleContent.size() != 1) {
            // At the moment we do not handle the ambiguity. Hence consider only single item
            return Optional.empty();
        }

        Symbol symbol = moduleContent.get(0);
        if (symbol.kind() == SymbolKind.CLASS) {
            ClassSymbol classSymbol = (ClassSymbol) symbol;
            return Optional.of(classSymbol);
        }
        TypeSymbol typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();
        return Optional.of(typeDescriptor);
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode node) {
        Optional<Symbol> variableSymbol = this.getSymbolByName(node.fieldName().text());
        Optional<TypeSymbol> typeSymbol = variableSymbol.flatMap(SymbolUtil::getTypeDescriptor);
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.getRawContextType(typeSymbol.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType =
                context.currentSemanticModel().flatMap(semanticModel -> semanticModel.type(node));
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
    public Optional<TypeSymbol> transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        /*
         * For error constructor node we return the detailed type descriptor of the error type desc.
         */
        Optional<TypeDescriptorNode> typeRef = errorConstructorExpressionNode.typeReference();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (typeRef.isEmpty() || semanticModel.isEmpty()) {
            return Optional.empty();
        }
        Optional<Symbol> symbol = context.currentSemanticModel().get().symbol(typeRef.get());
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.TYPE) {
            return Optional.empty();
        }
        TypeSymbol typeSymbol = ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
        if (typeSymbol.typeKind() != TypeDescKind.ERROR) {
            return Optional.empty();
        }
        return Optional.ofNullable(CommonUtil.getRawType(((ErrorTypeSymbol) typeSymbol).detailTypeDescriptor()));
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode node) {
        /*
        For the function definition, we consider the return type. In order to support the record-type-descriptor
        and to get the particular symbol, we extract the type symbol from the function symbol. 
         */
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = node.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = context.currentSemanticModel().get().symbol(node);

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
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        Optional<Symbol> funcSymbol = Optional.empty();
        NameReferenceNode nameRef = node.functionName();
        if (nameRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nameRef;
            Predicate<Symbol> predicate = symbol -> symbol.getName().isPresent()
                    && symbol.kind() == SymbolKind.FUNCTION;
            Predicate<Symbol> qNamePredicate =
                    predicate.and(symbol -> symbol.getName().orElse("").equals(qNameRef.identifier().text()));
            funcSymbol = this.getTypeFromQNameReference(qNameRef, qNamePredicate);
        } else if (nameRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            funcSymbol = getSymbolByName(((SimpleNameReferenceNode) nameRef).name().text());
        }

        if (funcSymbol.isEmpty()) {
            return Optional.empty();
        }

        if (!CommonUtil.isInFunctionCallParameterContext(context, node)
                || !(funcSymbol.get() instanceof FunctionSymbol)) {
            return SymbolUtil.getTypeDescriptor(funcSymbol.get());
        }

        Optional<ParameterSymbol> paramSymbol =
                CommonUtil.resolveFunctionParameterSymbol(
                        ((FunctionSymbol) funcSymbol.get()).typeDescriptor(), context, node);

        if (paramSymbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(paramSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(MethodCallExpressionNode node) {
        if (node.methodName().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            // Should always be simple name reference.
            return Optional.empty();
        }
        SimpleNameReferenceNode methodName = (SimpleNameReferenceNode) node.methodName();
        FieldAccessCompletionResolver resolver = new FieldAccessCompletionResolver(this.context);
        List<Symbol> visibleEntries = resolver.getVisibleEntries(node.expression());
        Optional<Symbol> methodSymbol = visibleEntries.stream()
                .filter(symbol -> symbol.getName().orElse("").equals(methodName.name().text()))
                .findFirst();

        if (methodSymbol.isEmpty() || methodSymbol.get().kind() != SymbolKind.METHOD) {
            return Optional.empty();
        }
        if (!CommonUtil.isInMethodCallParameterContext(context, node)) {
            return SymbolUtil.getTypeDescriptor(methodSymbol.get());
        }

        Optional<ParameterSymbol> paramSymbol =
                CommonUtil.resolveFunctionParameterSymbol(
                        ((MethodSymbol) methodSymbol.get()).typeDescriptor(), context, node);

        if (paramSymbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(paramSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(RemoteMethodCallActionNode node) {
        Optional<Symbol> methodSymbol = this.context.currentSemanticModel().get().symbol(node);

        if (methodSymbol.isEmpty() || methodSymbol.get().kind() != SymbolKind.METHOD) {
            return Optional.empty();
        }
        if (!CommonUtil.isInMethodCallParameterContext(context, node)) {
            return SymbolUtil.getTypeDescriptor(methodSymbol.get());
        }

        Optional<ParameterSymbol> paramSymbol =
                CommonUtil.resolveFunctionParameterSymbol(
                        ((MethodSymbol) methodSymbol.get()).typeDescriptor(), context, node);

        if (paramSymbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(paramSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode positionalArgumentNode) {
        // TODO: Add other cases like error constructors here
        switch (positionalArgumentNode.parent().kind()) {
            case FUNCTION_CALL:
                return getPositionalArgumentTypeForFunction(positionalArgumentNode,
                        ((FunctionCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent());
            case METHOD_CALL:
                return getPositionalArgumentTypeForFunction(positionalArgumentNode,
                        ((MethodCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent());
            case PARENTHESIZED_ARG_LIST:
                ParenthesizedArgList parenthesizedArgList = (ParenthesizedArgList) positionalArgumentNode.parent();
                switch (parenthesizedArgList.parent().kind()) {
                    case IMPLICIT_NEW_EXPRESSION:
                        ImplicitNewExpressionNode implicitNewExpressionNode =
                                (ImplicitNewExpressionNode) parenthesizedArgList.parent();
                        Optional<ParenthesizedArgList> argList = implicitNewExpressionNode.parenthesizedArgList();
                        if (argList.isEmpty()) {
                            return Optional.empty();
                        }

                        return getPositionalArgumentTypeForNewExpr(positionalArgumentNode,
                                argList.get().arguments(),
                                implicitNewExpressionNode);
                    case EXPLICIT_NEW_EXPRESSION:
                        ExplicitNewExpressionNode explicitNewExpressionNode =
                                (ExplicitNewExpressionNode) parenthesizedArgList.parent();
                        return getPositionalArgumentTypeForNewExpr(positionalArgumentNode,
                                explicitNewExpressionNode.parenthesizedArgList().arguments(),
                                explicitNewExpressionNode);
                }
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(NamedArgumentNode namedArgumentNode) {
        switch (namedArgumentNode.parent().kind()) {
            case FUNCTION_CALL:
            case METHOD_CALL:
                NonTerminalNode parentNode = namedArgumentNode.parent();
                Optional<List<ParameterSymbol>> parameterSymbols = context.currentSemanticModel()
                        .flatMap(semanticModel -> semanticModel.symbol(parentNode))
                        .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION ||
                                symbol.kind() == SymbolKind.METHOD ||
                                symbol.kind() == SymbolKind.RESOURCE_METHOD)
                        .flatMap(symbol -> ((FunctionSymbol) symbol).typeDescriptor().params());

                if (parameterSymbols.isEmpty()) {
                    return Optional.empty();
                }

                for (ParameterSymbol parameterSymbol : parameterSymbols.get()) {
                    if (parameterSymbol.getName().stream()
                            .anyMatch(name -> name.equals(namedArgumentNode.argumentName().name().text()))) {
                        TypeSymbol typeDescriptor = parameterSymbol.typeDescriptor();
                        return Optional.of(typeDescriptor);
                    }
                }
                break;
            case ERROR_CONSTRUCTOR: {
                // TODO: Couldn't find a way to get this done yet due to a limitation. Need to implement
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ListConstructorExpressionNode node) {
        Optional<TypeSymbol> typeSymbol = this.visit(node.parent());
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol.get());

        if (rawType.typeKind() != TypeDescKind.ARRAY) {
            return Optional.empty();
        }

        TypeSymbol memberType = ((ArrayTypeSymbol) rawType).memberTypeDescriptor();
        return Optional.of(memberType);
    }

    @Override
    public Optional<TypeSymbol> transform(DefaultableParameterNode node) {

        Optional<Symbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(node));

        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.PARAMETER) {
            return Optional.empty();
        }

        ParameterSymbol parameterSymbol = (ParameterSymbol) symbol.get();
        TypeSymbol typeDescriptor = parameterSymbol.typeDescriptor();
        return Optional.of(typeDescriptor);
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitAnonymousFunctionExpressionNode node) {
        /*
        For the function definition, we consider the return type.
         */
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = node.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> typeSymbol = context.currentSemanticModel().get().symbol(returnTypeDesc.get().type());

        if (typeSymbol.isEmpty() || typeSymbol.get().kind() != SymbolKind.TYPE) {
            return Optional.empty();
        }
        TypeSymbol symbol = (TypeSymbol) typeSymbol.get();
        return Optional.of(symbol);
    }

    @Override
    public Optional<TypeSymbol> transform(RecordFieldWithDefaultValueNode node) {
        Optional<Symbol> variableSymbol = this.getSymbolByName(node.fieldName().text());
        Optional<TypeSymbol> typeSymbol = variableSymbol.flatMap(SymbolUtil::getTypeDescriptor);
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.getRawContextType(typeSymbol.get()));
    }

//    @Override
//    public Optional<TypeSymbol> transform(InterpolationNode interpolationNode) {
//        return super.transform(interpolationNode);
//    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<Symbol> getTypeFromQNameReference(QualifiedNameReferenceNode node, Predicate<Symbol> predicate) {
        List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, node, predicate);
        if (moduleContent.size() != 1) {
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
        TypeSymbol rawType = typeSymbol;
        switch (typeSymbol.typeKind()) {
            case MAP:
                rawType = ((MapTypeSymbol) rawType).typeParam();
                break;
            case TABLE:
                rawType = ((TableTypeSymbol) rawType).rowTypeParameter();
                break;
            default:
                break;
        }

        return rawType;
    }

    /**
     * Given a positional argument node, it's parent (function or method call expression node) and function's/method's
     * argument nodes; this method returns the type symbol of the argument corresponding to the positional argument
     * provided.
     *
     * @param positionalArgNode        Positional argument node
     * @param argumentNodes            Argument nodes of the function/method call expression
     * @param functionOrMethodCallExpr Function/method call expression
     * @return {@link Optional<TypeSymbol>} Type symbol.
     */
    private Optional<TypeSymbol> getPositionalArgumentTypeForFunction(PositionalArgumentNode positionalArgNode,
                                                                      NodeList<FunctionArgumentNode> argumentNodes,
                                                                      NonTerminalNode functionOrMethodCallExpr) {
        int argIndex = -1;
        for (int i = 0; i < argumentNodes.size(); i++) {
            if (argumentNodes.get(i).equals(positionalArgNode)) {
                argIndex = i;
                break;
            }
        }

        Optional<List<ParameterSymbol>> parameterSymbols = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(functionOrMethodCallExpr))
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION ||
                        symbol.kind() == SymbolKind.METHOD ||
                        symbol.kind() == SymbolKind.RESOURCE_METHOD)
                .flatMap(symbol -> ((FunctionSymbol) symbol).typeDescriptor().params());

        if (argIndex == -1 || parameterSymbols.isEmpty() || parameterSymbols.get().size() <= argIndex) {
            return Optional.empty();
        }

        ParameterSymbol parameterSymbol = parameterSymbols.get().get(argIndex);
        TypeSymbol typeDescriptor = parameterSymbol.typeDescriptor();
        
        return Optional.of(typeDescriptor);
    }

    /**
     * Given a new expression node and a positional argument node, this method finds the type of the argument at the
     * positional argument.
     *
     * @param positionalArgumentNode Positional arg node
     * @param argumentNodes          Argument nodes
     * @param newExpressionNode      Implicit/explicit new expression node
     * @return Optiona type symbol of the parameter
     */
    private Optional<TypeSymbol> getPositionalArgumentTypeForNewExpr(PositionalArgumentNode positionalArgumentNode,
                                                                     NodeList<FunctionArgumentNode> argumentNodes,
                                                                     NewExpressionNode newExpressionNode) {
        int argIndex = -1;
        for (int i = 0; i < argumentNodes.size(); i++) {
            if (argumentNodes.get(i).equals(positionalArgumentNode)) {
                argIndex = i;
                break;
            }
        }

        Optional<List<ParameterSymbol>> parameterSymbols = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.type(newExpressionNode))
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol)))
                .filter(typeSymbol -> typeSymbol instanceof ClassSymbol)
                .flatMap(typeSymbol -> (((ClassSymbol) typeSymbol).initMethod()))
                .flatMap(methodSymbol -> methodSymbol.typeDescriptor().params());

        if (argIndex == -1 || parameterSymbols.isEmpty() || parameterSymbols.get().size() <= argIndex) {
            return Optional.empty();
        }
        TypeSymbol typeDescriptor = parameterSymbols.get().get(argIndex).typeDescriptor();
        return Optional.of(typeDescriptor);
    }
}
