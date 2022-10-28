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
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.StringTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.PositionedOperationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * This visitor is used to resolve a type of given context.
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
        List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, node, predicate);
        if (moduleContent.size() != 1) {
            // At the moment we do not handle the ambiguity. Hence, consider only single item
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
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode node) {
        Optional<Symbol> symbol =
                this.context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node.typeName()));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.TYPE) {
            return Optional.empty();
        }
        return Optional.of(this.getRawContextType((TypeSymbol) symbol.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType =
                context.currentSemanticModel().flatMap(semanticModel -> semanticModel.typeOf(node));
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
         * For error constructor node we return the detail type descriptor of the error type desc.
         */
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return Optional.empty();
        }
        Optional<TypeSymbol> typeRefSymbol = semanticModel.get().typeOf(errorConstructorExpressionNode);
        if (typeRefSymbol.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol typeSymbol = CommonUtil.getRawType(typeRefSymbol.get());
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

        if (!TypeResolverUtil.isInFunctionCallParameterContext(context, node)
                || !(funcSymbol.get() instanceof FunctionSymbol)) {
            return SymbolUtil.getTypeDescriptor(funcSymbol.get());
        }

        return TypeResolverUtil.resolveParameterTypeSymbol(((FunctionSymbol) funcSymbol.get()).typeDescriptor(),
                context.getCursorPositionInTree(), node.arguments());
    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitNewExpressionNode implicitNewExpressionNode) {

        Optional<TypeSymbol> classSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(implicitNewExpressionNode))
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol))).stream().findFirst();
        if (classSymbol.isEmpty()) {
            return Optional.empty();
        }
        if (!TypeResolverUtil.isInNewExpressionParameterContext(context, implicitNewExpressionNode)) {
            return SymbolUtil.getTypeDescriptor(classSymbol.get());
        }
        if (classSymbol.get().typeKind() == TypeDescKind.UNION) {
            classSymbol = ((UnionTypeSymbol) classSymbol.get()).memberTypeDescriptors().stream()
                    .filter(typeSymbol ->
                            CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.OBJECT)
                    .map(CommonUtil::getRawType).findFirst();
            if (classSymbol.isEmpty()) {
                return Optional.empty();
            }
        }
        if (!(classSymbol.get() instanceof ClassSymbol)) {
            return Optional.of(classSymbol.get());
        }
        Optional<ParenthesizedArgList> args = implicitNewExpressionNode.parenthesizedArgList();
        if (args.isEmpty()) {
            return Optional.empty();
        }

        Optional<MethodSymbol> methodSymbol = ((ClassSymbol) classSymbol.get()).initMethod();
        if (methodSymbol.isEmpty() || implicitNewExpressionNode.parenthesizedArgList().isEmpty()) {
            return Optional.empty();
        }
        return TypeResolverUtil.resolveParameterTypeSymbol(methodSymbol.get().typeDescriptor(),
                context.getCursorPositionInTree(), implicitNewExpressionNode.parenthesizedArgList().get().arguments());
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitNewExpressionNode explicitNewExpressionNode) {
        Optional<TypeSymbol> classSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(explicitNewExpressionNode))
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol))).stream().findFirst();
        if (classSymbol.isEmpty()) {
            return Optional.empty();
        }
        if (!TypeResolverUtil.isInNewExpressionParameterContext(context, explicitNewExpressionNode)) {
            return SymbolUtil.getTypeDescriptor(classSymbol.get());
        }
        if (classSymbol.get().typeKind() == TypeDescKind.UNION) {
            classSymbol = ((UnionTypeSymbol) classSymbol.get()).memberTypeDescriptors().stream()
                    .filter(typeSymbol ->
                            CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.OBJECT)
                    .map(CommonUtil::getRawType).findFirst();
            if (classSymbol.isEmpty()) {
                return Optional.empty();
            }
        }
        if (!(classSymbol.get() instanceof ClassSymbol)) {
            return Optional.of(classSymbol.get());
        }
        Optional<MethodSymbol> methodSymbol = ((ClassSymbol) classSymbol.get()).initMethod();
        if (methodSymbol.isEmpty()) {
            return Optional.empty();
        }
        return TypeResolverUtil.resolveParameterTypeSymbol(methodSymbol.get().typeDescriptor(),
                context.getCursorPositionInTree(), explicitNewExpressionNode.parenthesizedArgList().arguments());
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

        if (methodSymbol.isEmpty() || (methodSymbol.get().kind() != SymbolKind.METHOD
                && methodSymbol.get().kind() != SymbolKind.FUNCTION)) {
            return Optional.empty();
        }
        if (!TypeResolverUtil.isInMethodCallParameterContext(context, node)) {
            return SymbolUtil.getTypeDescriptor(methodSymbol.get());
        }

        return TypeResolverUtil.getPositionalArgumentTypeForFunction(node.arguments(), node, context,
                context.getCursorPositionInTree());
    }

    @Override
    public Optional<TypeSymbol> transform(RemoteMethodCallActionNode node) {
        Optional<Symbol> methodSymbol = this.context.currentSemanticModel().get().symbol(node);

        if (methodSymbol.isEmpty() || methodSymbol.get().kind() != SymbolKind.METHOD) {
            return Optional.empty();
        }
        if (!TypeResolverUtil.isInMethodCallParameterContext(context, node)) {
            // Here, we want the type of the context, not the type of the method itself
            return node.parent().apply(this);
        }

        return TypeResolverUtil.resolveParameterTypeSymbol(
                ((MethodSymbol) methodSymbol.get()).typeDescriptor(), context.getCursorPositionInTree(),
                node.arguments());
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode positionalArgumentNode) {
        // TODO: Add other cases like error constructors here
        switch (positionalArgumentNode.parent().kind()) {
            case FUNCTION_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((FunctionCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.getCursorPositionInTree());
            case METHOD_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((MethodCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.getCursorPositionInTree());
            case REMOTE_METHOD_CALL_ACTION:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((RemoteMethodCallActionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.getCursorPositionInTree());
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
                        return TypeResolverUtil.getPositionalArgumentTypeForNewExpr(argList.get().arguments(),
                                implicitNewExpressionNode, context, context.getCursorPositionInTree());
                    case EXPLICIT_NEW_EXPRESSION:
                        ExplicitNewExpressionNode explicitNewExpressionNode =
                                (ExplicitNewExpressionNode) parenthesizedArgList.parent();
                        return TypeResolverUtil.getPositionalArgumentTypeForNewExpr(
                                explicitNewExpressionNode.parenthesizedArgList().arguments(),
                                explicitNewExpressionNode, context, context.getCursorPositionInTree());
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
                Optional<TypeSymbol> errorDetail = this.visit(namedArgumentNode.parent());
                if (errorDetail.isEmpty() || errorDetail.get().typeKind() != TypeDescKind.RECORD) {
                    return Optional.empty();
                }
                Optional<RecordFieldSymbol> fieldSymbol =
                        ((RecordTypeSymbol) errorDetail.get()).fieldDescriptors().values().stream()
                                .filter(recordFieldSymbol -> recordFieldSymbol.getName().isPresent()
                                        && namedArgumentNode.argumentName().name().text().trim()
                                        .equals(recordFieldSymbol.getName().get())).findFirst();
                if (fieldSymbol.isPresent()) {
                    return Optional.of(fieldSymbol.get().typeDescriptor());
                }
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
    public Optional<TypeSymbol> transform(ImplicitAnonymousFunctionExpressionNode node) {
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node))
                .or(() -> node.parent().apply(this));

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() != TypeDescKind.FUNCTION) {
            return Optional.empty();
        }

        FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) typeSymbol.get();
        if (!node.rightDoubleArrow().isMissing() &&
                context.getCursorPositionInTree() >= node.rightDoubleArrow().textRange().endOffset()) {
            // Cursor is at the expression node
            return functionTypeSymbol.returnTypeDescriptor();
        }

        return typeSymbol;
    }

    @Override
    public Optional<TypeSymbol> transform(RecordFieldWithDefaultValueNode node) {

        Optional<Symbol> symbol =
                this.context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node.typeName()));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.TYPE) {
            return Optional.empty();
        }

        return Optional.of(this.getRawContextType((TypeSymbol) symbol.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(mappingConstructorExpressionNode))
                .filter(typeSymbol -> typeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> mappingConstructorExpressionNode.parent().apply(this));
    }

    @Override
    public Optional<TypeSymbol> transform(MappingMatchPatternNode mappingMatchPatternNode) {
        return mappingMatchPatternNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(MatchClauseNode matchClauseNode) {
        return matchClauseNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(MatchStatementNode matchStatementNode) {
        return context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(matchStatementNode.condition()));
    }

    @Override
    public Optional<TypeSymbol> transform(TableConstructorExpressionNode node) {
        Optional<TypeSymbol> optionalTypeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> node.parent().apply(this))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR);

        if (optionalTypeSymbol.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol typeSymbol = CommonUtil.getRawType(optionalTypeSymbol.get());
        if (typeSymbol.typeKind() != TypeDescKind.TABLE) {
            // We have got the row type already
            return Optional.of(typeSymbol);
        }

        if (node.keySpecifier().isPresent() &&
                node.keySpecifier().get().textRange().startOffset() < context.getCursorPositionInTree() &&
                context.getCursorPositionInTree() < node.keySpecifier().get().textRange().endOffset()) {
            // Check if cursor is within the key specifier node
            typeSymbol = ((TableTypeSymbol) typeSymbol).rowTypeParameter();
        } else if (node.openBracket().textRange().endOffset() < context.getCursorPositionInTree() &&
                context.getCursorPositionInTree() < node.closeBracket().textRange().startOffset()) {
            // Check if the cursor is within the brackets. If so, we return the row type parameter.
            // NOTE: We don't check if the typeSymbol's type desc kind to be TABLE because, it cannot be otherwise.
            typeSymbol = ((TableTypeSymbol) typeSymbol).rowTypeParameter();
        }

        return Optional.of(typeSymbol);
    }

    @Override
    public Optional<TypeSymbol> transform(SelectClauseNode node) {
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node.expression()))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> node.parent().apply(this))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR);
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }

        // select clause can be inside a query expression of 3 types:
        //  table from...
        //  stream from...
        //  from...
        // In such cases, we take the member type
        switch (typeSymbol.get().typeKind()) {
            case TABLE:
                TableTypeSymbol tableType = (TableTypeSymbol) typeSymbol.get();
                return Optional.of(tableType.rowTypeParameter());
            case STREAM:
                StreamTypeSymbol streamType = (StreamTypeSymbol) typeSymbol.get();
                return Optional.of(streamType.typeParameter());
            case ARRAY:
                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) typeSymbol.get();
                return Optional.of(arrayType.memberTypeDescriptor());
            default:
                return typeSymbol;
        }
    }

    //    @Override
//    public Optional<TypeSymbol> transform(InterpolationNode interpolationNode) {
//        return super.transform(interpolationNode);
//    }

    @Override
    public Optional<TypeSymbol> transform(FromClauseNode fromClauseNode) {
        if (context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }

        // from typed-binding-pattern in <cursor>
        if (context.getCursorPositionInTree() > fromClauseNode.inKeyword().textRange().endOffset()) {
            Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(fromClauseNode.expression());

            if (typeSymbol.isPresent()) {
                return typeSymbol;
            }
            Optional<Symbol> optionalSymbol = context.currentSemanticModel().get()
                    .symbol(fromClauseNode.typedBindingPattern().bindingPattern());
            if (optionalSymbol.isEmpty()) {
                return Optional.empty();
            }
            typeSymbol = SymbolUtil.getTypeDescriptor(optionalSymbol.get());

            if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
                return Optional.empty();
            }
            
            return Optional.of(buildUnionOfIterables(typeSymbol.get(), context.currentSemanticModel().get()));
        }

        // from <cursor> in iterable
        Optional<Symbol> optionalSymbol = context.currentSemanticModel().get()
                .symbol(fromClauseNode.typedBindingPattern().bindingPattern());
        if (optionalSymbol.isEmpty()) {
            Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get()
                    .typeOf(fromClauseNode.expression());
            if (typeSymbol.isEmpty()) {
                return typeSymbol;
            }
            switch (typeSymbol.get().typeKind()) {
                case ARRAY:
                    return Optional.of(((ArrayTypeSymbol) typeSymbol.get()).memberTypeDescriptor());
                case STRING:
                    return Optional.of(context.currentSemanticModel().get().types().STRING);
                case TABLE:
                    return Optional.of(((TableTypeSymbol) typeSymbol.get()).rowTypeParameter());
                case STREAM:
                    return Optional.of(((StreamTypeSymbol) typeSymbol.get()).typeParameter());
                case XML:
                    return Optional.of(context.currentSemanticModel().get().types().XML);
                case MAP:
                    return Optional.of(((MapTypeSymbol) typeSymbol.get()).typeParam());
            }
        }
        
        return Optional.empty();
    }

    private UnionTypeSymbol buildUnionOfIterables(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Types types = semanticModel.types();
        TypeBuilder builder = types.builder();
        List<TypeSymbol> unionTypeMembers = new ArrayList<>(
                List.of(builder.ARRAY_TYPE.withType(typeSymbol).build(),
                        builder.MAP_TYPE.withTypeParam(typeSymbol).build(),
                        builder.STREAM_TYPE.withValueType(typeSymbol).build()));
        if (CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.RECORD) {
            try {
                unionTypeMembers
                        .add(builder.TABLE_TYPE.withRowType(CommonUtil.getRawType(typeSymbol)).build());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (typeSymbol instanceof StringTypeSymbol) {
            unionTypeMembers.add(types.STRING);
        }
        if (typeSymbol.typeKind() == TypeDescKind.XML) {
            unionTypeMembers.add(types.XML);
        }
        return builder.UNION_TYPE
                .withMemberTypes(unionTypeMembers.toArray(TypeSymbol[]::new)).build();
    }
    
    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<Symbol> getTypeFromQNameReference(QualifiedNameReferenceNode node, Predicate<Symbol> predicate) {
        List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, node, predicate);
        if (moduleContent.size() != 1) {
            // At the moment we do not handle the ambiguity. Hence, consider only single item
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
}
