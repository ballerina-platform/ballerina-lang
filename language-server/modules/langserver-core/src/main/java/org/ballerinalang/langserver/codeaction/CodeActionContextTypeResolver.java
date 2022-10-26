/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This visitor is used to resolve the type of given code action context.
 * 
 * Todo: Use the ContextTypeResolver instead.
 * @since 2201.1.1
 */
public class CodeActionContextTypeResolver extends NodeTransformer<Optional<TypeSymbol>> {

    private final CodeActionContext context;
    private final List<Node> visitedNodes = new ArrayList<>();

    public CodeActionContextTypeResolver(CodeActionContext context) {
        this.context = context;
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }
        visitedNodes.add(node);
        return node.apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        return getTypeDescriptorOfVariable(node);
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode assignmentStatementNode) {
        return this.visit(assignmentStatementNode.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(moduleVariableDeclarationNode);
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(letVariableDeclarationNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode objectFieldNode) {
        return objectFieldNode.typeName().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode) {
        Optional<Symbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(builtinSimpleNameReferenceNode));
        return SymbolUtil.getTypeDescriptor(symbol.orElse(null));
    }

    @Override
    public Optional<TypeSymbol> transform(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        return context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(mappingConstructorExpressionNode))
                .filter(typeSymbol -> typeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> mappingConstructorExpressionNode.parent().apply(this));
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode indexedExpressionNode) {
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().flatMap(
                semanticModel -> semanticModel.typeOf(indexedExpressionNode));
        if (typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.getRawContextType(typeSymbol.get()));
    }

    @Override
    public Optional<TypeSymbol> transform(SpecificFieldNode specificFieldNode) {
        Optional<TypeSymbol> parentType = specificFieldNode.parent().apply(this);
        if (parentType.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol parentRawType = CommonUtil.getRawType(parentType.get());
        if (parentRawType.typeKind() == TypeDescKind.MAP) {
            TypeSymbol rawContextType = this.getRawContextType(parentType.get());
            return Optional.of(rawContextType);
        }

        if (parentRawType.typeKind() != TypeDescKind.RECORD
                || (specificFieldNode.fieldName().kind() != SyntaxKind.STRING_LITERAL
                && specificFieldNode.fieldName().kind() != SyntaxKind.IDENTIFIER_TOKEN)) {
            return Optional.empty();
        }

        if (specificFieldNode.fieldName().kind() == SyntaxKind.STRING_LITERAL) {
            return Optional.of(parentRawType);
        }

        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) parentRawType;
        String fieldName = ((Token) specificFieldNode.fieldName()).text();
        // Extract the type of the particular field
        Optional<TypeSymbol> typeOfField = recordTypeSymbol.fieldDescriptors().entrySet().stream()
                .filter(entry -> entry.getKey().equals(fieldName))
                .findFirst()
                .map(entry -> entry.getValue().typeDescriptor());

        return typeOfField.isEmpty() ? Optional.empty()
                : Optional.of(typeOfField.get());
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode functionDefinitionNode) {
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = functionDefinitionNode.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = context.currentSemanticModel().get().symbol(functionDefinitionNode);

        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }

        return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(ListConstructorExpressionNode listConstructorExpressionNode) {
        Optional<TypeSymbol> typeSymbol = this.visit(listConstructorExpressionNode.parent());
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
    public Optional<TypeSymbol> transform(TableConstructorExpressionNode tableConstructorExpressionNode) {
        Optional<TypeSymbol> optionalTypeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(tableConstructorExpressionNode))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR)
                .or(() -> tableConstructorExpressionNode.parent().apply(this))
                .filter(tSymbol -> tSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR);

        if (optionalTypeSymbol.isEmpty()) {
            return Optional.empty();
        }

        TypeSymbol typeSymbol = CommonUtil.getRawType(optionalTypeSymbol.get());
        if (typeSymbol.typeKind() != TypeDescKind.TABLE) {
            return Optional.of(typeSymbol);
        }

        if (tableConstructorExpressionNode.keySpecifier().isPresent() &&
                tableConstructorExpressionNode.keySpecifier().get().textRange().startOffset()
                        < context.cursorPositionInTree() && context.cursorPositionInTree()
                < tableConstructorExpressionNode.keySpecifier().get().textRange().endOffset()) {
            typeSymbol = ((TableTypeSymbol) typeSymbol).rowTypeParameter();
        } else if (tableConstructorExpressionNode.openBracket().textRange().endOffset()
                < context.cursorPositionInTree() && context.cursorPositionInTree()
                < tableConstructorExpressionNode.closeBracket().textRange().startOffset()) {
            typeSymbol = ((TableTypeSymbol) typeSymbol).rowTypeParameter();
        }

        return Optional.of(typeSymbol);
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
    public Optional<TypeSymbol> transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
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
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode simpleNameReferenceNode) {
        Optional<Symbol> symbol = this.getSymbolByName(simpleNameReferenceNode.name().text());
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(symbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode positionalArgumentNode) {
        switch (positionalArgumentNode.parent().kind()) {
            case FUNCTION_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((FunctionCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.cursorPositionInTree());
            case METHOD_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((MethodCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.cursorPositionInTree());
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
                                implicitNewExpressionNode, context, context.cursorPositionInTree());
                    case EXPLICIT_NEW_EXPRESSION:
                        ExplicitNewExpressionNode explicitNewExpressionNode =
                                (ExplicitNewExpressionNode) parenthesizedArgList.parent();
                        return TypeResolverUtil.getPositionalArgumentTypeForNewExpr(
                                explicitNewExpressionNode.parenthesizedArgList().arguments(),
                                explicitNewExpressionNode, context, context.cursorPositionInTree());
                }
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getTypeDescriptorOfVariable(Node node) {
        Optional<Symbol> symbol = context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        return Optional.of(((VariableSymbol) symbol.get()).typeDescriptor());
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

    private Optional<Symbol> getSymbolByName(String name) {
        return this.context.visibleSymbols(context.cursorPosition()).stream()
                .filter((symbol -> symbol.getName().orElse("").equals(name)))
                .findFirst();
    }
}
