/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.symbols.BallerinaArrayTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.StringTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
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
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.impl.PositionUtil.isPosWithinOpenCloseLineRanges;
import static io.ballerina.compiler.api.impl.PositionUtil.isPosWithinRange;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static org.ballerinalang.model.tree.NodeKind.ERROR_CONSTRUCTOR_EXPRESSION;

/**
 * This transformer is used to resolve an expected type for a given context.
 *
 * @since 2201.4.0
 */
public class ExpectedTypeFinder extends NodeTransformer<Optional<TypeSymbol>> {

    private final SemanticModel semanticModel;
    private final BLangCompilationUnit bLangCompilationUnit;
    private final List<Node> visitedNodes = new ArrayList<>();
    private final TypesFactory typesFactory;
    private final LinePosition linePosition;

    private final NodeFinder nodeFinder;
    private final Document document;
    private final LangLibrary langLibrary;
    private final LangLibFunctionBinder langLibFunctionBinder;
    private FunctionArgumentNode functionArgNodeAtCursor;

    public ExpectedTypeFinder(SemanticModel semanticModel, BLangCompilationUnit bLangCompilationUnit,
                              CompilerContext context, LinePosition linePosition, Document srcDocument) {
        this.nodeFinder = new NodeFinder(true);
        this.semanticModel = semanticModel;
        this.bLangCompilationUnit = bLangCompilationUnit;
        this.typesFactory = TypesFactory.getInstance(context);
        this.linePosition = linePosition;
        this.document = srcDocument;
        this.langLibrary = LangLibrary.getInstance(context);
        this.langLibFunctionBinder = new LangLibFunctionBinder(context);
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        // TODO remove this after fix #38522
        if (bLangNode == null) {
            Optional<Symbol> symbol = findSymbolByName(node.name().text());
            if (symbol.isEmpty()) {
                return Optional.empty();
            }

            return SymbolUtils.getTypeDescriptor(symbol.get());
        }

        if (node.parent().kind() == SyntaxKind.POSITIONAL_ARG || node.parent().kind() == SyntaxKind.NAMED_ARG) {
            Optional<TypeSymbol> expectedType = node.parent().apply(this);
            if (expectedType.isPresent()) {
                return expectedType;
            }
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(AnnotationNode node) {
        Node annotationRef = node.annotReference();
        Optional<Symbol> annotationSymbol;
        Predicate<Symbol> predicate = symbol -> symbol.getName().isPresent() &&
                symbol.kind() == io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;

        if (annotationRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) annotationRef;
            Predicate<Symbol> qNamePredicate =
                    predicate.and(symbol -> symbol.getName().get().equals(qNameRef.identifier().text()));
            annotationSymbol = getQNameReferenceSymbol(qNameRef, qNamePredicate);
        } else if (annotationRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            annotationSymbol = findSymbolByName(((SimpleNameReferenceNode) annotationRef).name().text(), predicate);
        } else {
            return Optional.empty();
        }

        if (annotationSymbol.isEmpty()) {
            return Optional.empty();
        }

        return ((AnnotationSymbol) annotationSymbol.get()).typeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(SpecificFieldNode node) {
        Optional<TypeSymbol> elementType;
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());

        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangRecordLiteral.BLangRecordVarNameField bLangNodeRecLiteral) {
            if (bLangNodeRecLiteral.symbol != null && node.parent().kind() == SyntaxKind.MAPPING_CONSTRUCTOR) {
                BLangExpression bLangExpression = bLangNodeRecLiteral.impConversionExpr;
                if (bLangExpression != null && bLangExpression.getBType().getKind() == TypeKind.ANY) {
                    BType bType = bLangExpression.getBType();
                    return getTypeFromBType(bType);
                }

                elementType = getTypeFromBType(bLangNodeRecLiteral.symbol.getType());
                if (elementType.isPresent()) {
                    return elementType;
                }
            }
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(BasicLiteralNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getTypeFromBType(bLangNode.getBType());
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode node) {
        return this.visit(node.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(ListenerDeclarationNode node) {
        Optional<TypeDescriptorNode> typeDesc = node.typeDescriptor();
        return typeDesc.isEmpty() ? Optional.empty() : typeDesc.get().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        if (node.typedBindingPattern().typeDescriptor().kind() == SyntaxKind.VAR_TYPE_DESC) {
            Types types = semanticModel.types();
            TypeBuilder builder = types.builder();
            UnionTypeSymbol unionTypeSymbol = builder.UNION_TYPE.withMemberTypes(types.ANY, types.ERROR).build();
            return Optional.of(unionTypeSymbol);
        }

        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(BinaryExpressionNode node) {
        if (!node.operator().isMissing() && node.operator().kind() == SyntaxKind.ASTERISK_TOKEN
                || node.operator().kind() == SyntaxKind.SLASH_TOKEN) {
            return Optional.empty();
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangBinaryExpr bLangBinaryExpr)) {
            return Optional.empty();
        }

        BLangExpression rhs = bLangBinaryExpr.rhsExpr;
        BLangExpression lhs = bLangBinaryExpr.lhsExpr;
        BType rhsExpType = rhs.expectedType;
        if (rhsExpType == null || lhs.expectedType == null) {
            return Optional.empty();
        }
        if (rhsExpType.getKind() != TypeKind.OTHER) {
            return getExpectedType(rhs);
        }
        return getExpectedType(lhs);
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangInvocation bLangInvocation)) {
            return Optional.empty();
        }

        return (bLangInvocation.symbol instanceof BInvokableSymbol && !bLangInvocation.getRequiredArgs().isEmpty()) ?
                transformFunctionOrMethod(bLangNode,
                        ((BLangNode) bLangInvocation.getRequiredArgs().get(0)).getBType(),
                        (BInvokableSymbol) bLangInvocation.symbol, node.arguments(),
                        node.openParenToken(), node.closeParenToken(), true) :
                getExpectedTypeFromFunction(bLangNode, node.openParenToken(), node.closeParenToken());
    }

    @Override
    public Optional<TypeSymbol> transform(MethodCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangInvocation)) {
            return Optional.empty();
        }

        BLangNode bLangFunctionCall = nodeFinder.lookup(this.bLangCompilationUnit,
                node.expression().lineRange());
        BInvokableSymbol bLangOriginalLangLib =
                this.langLibrary.getLangLibMethod(bLangFunctionCall.getBType(),
                        SymbolUtils.unescapeUnicode(node.methodName().toString()));

        return transformFunctionOrMethod(bLangNode, bLangFunctionCall.getBType(), bLangOriginalLangLib,
                node.arguments(), node.openParenToken(), node.closeParenToken(), false);
    }

    @Override
    public Optional<TypeSymbol> transform(QualifiedNameReferenceNode node) {
        // Added to prevent from the case module1:$missingNode returns a expectedType ("string").
        if (node.identifier().isMissing()) {
            return Optional.empty();
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangSimpleVarRef simpleVarRef && ((BLangSimpleVarRef) bLangNode).symbol != null) {
            return getTypeFromBType(simpleVarRef.symbol.getType());
        }

        if (bLangNode instanceof BLangUserDefinedType userDefinedType && userDefinedType.symbol != null) {
            return getTypeFromBType(userDefinedType.symbol.getType());
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitAnonymousFunctionExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangFunction)) {
            return Optional.empty();
        }

        return Optional.of(typesFactory.getTypeDescriptor(((BLangFunction) nodeFinder.lookup
                (this.bLangCompilationUnit, node.lineRange())).returnTypeNode.getBType()));

    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitAnonymousFunctionExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangArrowFunction arrowFunction)) {
            return Optional.empty();
        }

        if (!node.rightDoubleArrow().isMissing() &&
                node.rightDoubleArrow().lineRange().endLine().offset() <= linePosition.offset()) {
            // Cursor is at the expression node.
            BType funcType = arrowFunction.funcType;
            if (funcType == null) {
                return Optional.empty();
            }
            return Optional.of(typesFactory.getTypeDescriptor(funcType.getReturnType()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode node) {
        if (node.parent().kind() == SyntaxKind.METHOD_CALL || node.parent().kind() == SyntaxKind.FUNCTION_CALL) {
            this.functionArgNodeAtCursor = node;
            Optional<TypeSymbol> expectedType = node.parent().apply(this);
            if (expectedType.isPresent()) {
                return expectedType;
            }
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangRecordLiteral)) {
            return Optional.empty();
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode node) {
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = node.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = this.semanticModel.symbol(node);

        if (functionSymbol.isEmpty() || !(functionSymbol.get() instanceof FunctionSymbol)) {
            return Optional.empty();
        }

        return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(MatchClauseNode node) {
        return node.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(MatchStatementNode node) {
        return this.semanticModel.typeOf(node.condition());
    }

    @Override
    public Optional<TypeSymbol> transform(MappingMatchPatternNode mappingMatchPatternNode) {
        return mappingMatchPatternNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitNewExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        ParenthesizedArgList parenthesizedArgList = node.parenthesizedArgList();
        // Check the cursor position in within the parenthesis.
        Token openParen = parenthesizedArgList.openParenToken();
        Token closeParen = parenthesizedArgList.closeParenToken();
        if (isWithinParenthesis(openParen, closeParen)
                && bLangNode instanceof BLangTypeInit
                && ((BLangTypeInit) bLangNode).initInvocation instanceof BLangInvocation
                && (((BLangInvocation) ((BLangTypeInit) bLangNode).initInvocation).symbol
                instanceof BInvokableSymbol)) {
            List<BVarSymbol> params = ((BInvokableSymbol) (((BLangInvocation) ((BLangTypeInit) bLangNode).
                    initInvocation).symbol)).params;
            if (params.isEmpty()) {
                throw new IllegalStateException();
            }

            int argIndex = 0;
            List<BLangExpression> argsExpr = ((BLangTypeInit) bLangNode).argsExpr;
            if (argsExpr.isEmpty()) {
                return getTypeFromBType(params.get(0).getType());
            }

            for (BLangExpression bLangExpression : argsExpr) {
                if (isPosWithinRange(linePosition, bLangExpression.getPosition().lineRange())
                        && argIndex < params.size()) {
                    return getTypeFromBType(params.get(argIndex).getType());
                }

                argIndex += 1;
            }
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitNewExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        Optional<ParenthesizedArgList> parenthesizedArgList = node.parenthesizedArgList();
        if (parenthesizedArgList.isEmpty()) {
            return getExpectedType(bLangNode);
        }

        Token openParen = parenthesizedArgList.get().openParenToken();
        Token closeParen = parenthesizedArgList.get().closeParenToken();
        // Check if the line position is within the parameter context.
        if (isWithinParenthesis(openParen, closeParen)) {
            if (!(bLangNode instanceof BLangTypeInit bLangTypeInit)) {
                return getExpectedType(bLangNode);
            }

            if (!(bLangTypeInit.initInvocation instanceof BLangInvocation initInvocation)) {
                return getExpectedType(bLangNode);
            }

            if (initInvocation.symbol == null) {
                throw new IllegalStateException();
            }

            List<BVarSymbol> params = ((BInvokableSymbol) initInvocation.symbol).params;
            if (params.isEmpty()) {
                throw new IllegalStateException();
            }

            if (initInvocation.argExprs.isEmpty()) {
                return getParamType(initInvocation, 0, Collections.emptyList());
            }

            int argIndex = 0;
            for (BLangExpression bLangExpression : ((BLangTypeInit) bLangNode).argsExpr) {
                if (isPosWithinRange(linePosition, bLangExpression.getPosition().lineRange())
                        && argIndex < params.size()) {
                    return getTypeFromBType(params.get(argIndex).getType());
                }

                argIndex += 1;
                if (params.size() < argIndex) {
                    throw new IllegalStateException();
                }
            }
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(IfElseStatementNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (isPosWithinRange(linePosition, node.condition().lineRange()) &&
                bLangNode instanceof BLangIf bLangIf) {
            return getExpectedType(bLangIf.expr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(WhileStatementNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (isPosWithinRange(linePosition, node.condition().lineRange())
                && bLangNode instanceof BLangWhile bLangWhile) {
            return getExpectedType(bLangWhile.expr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(DefaultableParameterNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ExpressionFunctionBodyNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.expression().lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(RemoteMethodCallActionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangInvocation.BLangActionInvocation) {
            BLangInvocation.BLangActionInvocation bLangActionInvocation = (BLangInvocation.BLangActionInvocation)
                    nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
            Token openParen = node.openParenToken();
            Token closeParen = node.closeParenToken();
            if (isWithinParenthesis(openParen, closeParen)) {
                if (bLangActionInvocation.argExprs.isEmpty()) {
                    return getTypeFromBType(((BInvokableSymbol) bLangActionInvocation.symbol).
                            getParameters().get(0).getType());
                }

                Optional<BLangExpression> argument = bLangActionInvocation.argExprs.stream().filter
                        (argumentNode -> isPosWithinRange(linePosition, argumentNode.getPosition()
                                .lineRange())).findFirst();
                if (argument.isPresent()) {
                    return getExpectedType(argument.get());
                }
            }

            return getExpectedType(bLangNode);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ListConstructorExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        Optional<TypeSymbol> extractedType = getExpectedType(bLangNode);
        if (extractedType.isEmpty()) {
            return Optional.empty();
        }

        if (extractedType.get().typeKind() != TypeDescKind.ARRAY) {
            throw new IllegalStateException();
        }

        BArrayType bArrayType = (BArrayType) ((BallerinaArrayTypeSymbol) extractedType.get()).getBType();
        Token openBracket = node.openBracket();
        Token closeBracket = node.closeBracket();
        if (isWithinParenthesis(openBracket, closeBracket) &&
                (bArrayType.eType != null && bArrayType.eType.getKind() != TypeKind.OTHER)) {
            return Optional.of(typesFactory.getTypeDescriptor(bArrayType.eType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(CaptureBindingPatternNode node) {
        if (findSymbolByName(node.variableName().text()).isEmpty()) {
            return Optional.empty();
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        BType bType = bLangNode.getBType();
        if (bLangNode.getBType().getKind() == TypeKind.MAP) {
            return getTypeFromBType(bLangNode.getBType());
        }

        if (bType.getKind() == TypeKind.OTHER) {
            return Optional.empty();
        }

        return Optional.of(typesFactory.getTypeDescriptor(bType));
    }

    @Override
    public Optional<TypeSymbol> transform(MappingConstructorExpressionNode node) {
        return node.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(WaitActionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangWaitExpr waitExpr)) {
            return Optional.empty();
        }

        for (BLangExpression bLangExpression : waitExpr.exprList) {
            if (isPosWithinRange(linePosition, bLangExpression.getPosition().lineRange())) {
                return getExpectedType(bLangExpression);
            }
        }

        return getExpectedType(waitExpr);
    }

    @Override
    public Optional<TypeSymbol> transform(ErrorConstructorExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode.getKind() == ERROR_CONSTRUCTOR_EXPRESSION) {
            Token openParen = node.openParenToken();
            Token closeParen = node.closeParenToken();
            if (isWithinParenthesis(openParen, closeParen)) {
                return Optional.of(typesFactory.getTypeDescriptor(((BErrorType)
                        ((BLangErrorConstructorExpr) bLangNode).expectedType).detailType));
            }

            return Optional.of(typesFactory.getTypeDescriptor(((BLangErrorConstructorExpr) bLangNode).expectedType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(NamedArgumentNode node) {
        if (node.parent().kind() == SyntaxKind.METHOD_CALL || node.parent().kind() == SyntaxKind.FUNCTION_CALL) {
            this.functionArgNodeAtCursor = node;
            Optional<TypeSymbol> expectedType = node.parent().apply(this);
            if (expectedType.isPresent()) {
                return expectedType;
            }
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(TableConstructorExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangTableConstructorExpr bLangTableConstructorExpr)) {
            return Optional.empty();
        }

        if (node.keySpecifier().isPresent() &&
                isWithinParenthesis(node.keySpecifier().get().openParenToken(),
                        node.keySpecifier().get().closeParenToken())) {
            BType expectedType = bLangTableConstructorExpr.expectedType;
            if (expectedType instanceof BTableType tableType) {
                return Optional.of(typesFactory.getTypeDescriptor(Objects.
                        requireNonNullElse(tableType.constraint, expectedType)));
            }

            if (expectedType instanceof BTypeReferenceType referenceType) {
                BType referredType = referenceType.referredType;
                return Optional.of(typesFactory.getTypeDescriptor(Objects.
                        requireNonNullElse(((BTableType) referredType).constraint, expectedType)));
            }

        }

        Token openBracket = node.openBracket();
        Token closeBracket = node.closeBracket();
        if (isWithinParenthesis(openBracket, closeBracket)) {
            if (!((bLangTableConstructorExpr.expectedType) instanceof BTypeReferenceType)) {
                BType constraint = ((BTableType) bLangTableConstructorExpr.expectedType).constraint;
                return getTypeFromBType(constraint);
            }

            BType rowType = ((BTableType) ((BTypeReferenceType) bLangTableConstructorExpr.expectedType)
                    .referredType).constraint;
            return Optional.of(typesFactory.getTypeDescriptor(rowType));
        }

        return getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(RecordFieldWithDefaultValueNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return bLangNode == null ? Optional.empty() : getExpectedType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(SelectClauseNode node) {
        BLangNode bLangNode = nodeFinder.lookup(bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangSelectClause selectClause)) {
            return Optional.empty();
        }

        BType expectedType = ((BLangExpression) selectClause.getExpression()).expectedType;
        return getTypeFromBType(expectedType);

    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }

        visitedNodes.add(node);
        return node.apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(FromClauseNode node) {
        if (isPosWithinRange(linePosition, node.fromKeyword().lineRange()) ||
                isPosWithinRange(linePosition, node.inKeyword().lineRange())) {
            return Optional.empty();
        }

        // Check if the line position within the `in` keyword.
        if (!(node.inKeyword().lineRange().endLine().line() <= linePosition.line() &&
                node.inKeyword().lineRange().endLine().offset() <= linePosition.offset())) {
            BLangNode expressionNode = nodeFinder.lookup(bLangCompilationUnit, node.expression().lineRange());
            if (expressionNode != null) {
                BType bType = expressionNode.getBType();
                switch (bType.getKind()) {
                    case ARRAY:
                        return Optional.of(typesFactory.getTypeDescriptor((((BArrayType)
                                expressionNode.getBType()).eType)));
                    case STRING:
                        return Optional.of(semanticModel.types().STRING);
                    case TABLE:
                        return Optional.of(typesFactory.getTypeDescriptor((((BTableType)
                                expressionNode.getBType()).constraint)));
                    case STREAM:
                        return Optional.of(typesFactory.getTypeDescriptor(((BStreamType)
                                expressionNode.getBType()).constraint));
                    case XML:
                        return Optional.of(typesFactory.getTypeDescriptor(((BXMLType)
                                expressionNode.getBType()).constraint));
                    case MAP:
                        return Optional.of(typesFactory.getTypeDescriptor(((BMapType)
                                expressionNode.getBType()).constraint));
                    default:
                        break;
                }
            }
        }

        Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(node.expression());
        if (typeSymbol.isPresent()) {
            return typeSymbol;
        }

        Optional<Symbol> optionalSymbol = semanticModel.symbol(node.typedBindingPattern().bindingPattern());
        if (optionalSymbol.isEmpty()) {
            return Optional.empty();
        }

        typeSymbol = SymbolUtils.getTypeDescriptor(optionalSymbol.get());
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Optional.empty();
        }

        return Optional.of(buildUnionOfIterables(typeSymbol.get(), semanticModel));
    }

    @Override
    public Optional<TypeSymbol> transform(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        BLangNode bLangNode = nodeFinder.lookup(bLangCompilationUnit, clientResourceAccessActionNode.lineRange());
        Optional<ParenthesizedArgList> arguments = clientResourceAccessActionNode.arguments();
        if (arguments.isPresent() && isWithinParenthesis(arguments.get().openParenToken(),
                arguments.get().closeParenToken())) {
            if (!(bLangNode instanceof BLangInvocation bLangInvocation)) {
                return getExpectedType(bLangNode);
            }
            if (bLangInvocation.symbol == null) {
                return Optional.empty();
            }
            BInvokableSymbol symbol = ((BInvokableSymbol) bLangInvocation.symbol);
            //`params` contains path params and path rest params as well. We need to skip those
            List<BVarSymbol> params = symbol.params.stream().filter(param ->
                    param.getKind() != SymbolKind.PATH_PARAMETER
                            && param.getKind() != SymbolKind.PATH_REST_PARAMETER).toList();
            BVarSymbol restPram = ((BInvokableSymbol) bLangInvocation.symbol).restParam;
            TypeSymbol restParamMemberType = null;


            if (restPram != null) {
                Optional<TypeSymbol> restParamType = getTypeFromBType(restPram.type);
                if (restParamType.isPresent() && restParamType.get().typeKind() == TypeDescKind.ARRAY) {
                    restParamMemberType = ((ArrayTypeSymbol) restParamType.get()).memberTypeDescriptor();
                }
            }
            if (params.isEmpty()) {
                if (restParamMemberType == null) {
                    throw new IllegalStateException();
                }
                return Optional.of(restParamMemberType);
            }

            if (bLangInvocation.argExprs.isEmpty()) {
                return getParamType(bLangInvocation, 0, Collections.emptyList());
            }

            int argIndex = 0;
            for (BLangExpression bLangExpression : bLangInvocation.argExprs) {
                if (isPosWithinRange(linePosition, bLangExpression.getPosition().lineRange())
                        && argIndex < params.size()) {
                    return getTypeFromBType(params.get(argIndex).getType());
                }

                argIndex += 1;
                if (params.size() < argIndex) {
                    if (restParamMemberType != null) {
                        return Optional.of(restParamMemberType);
                    }
                    throw new IllegalStateException();
                }
            }
        }
        return Optional.empty();
    }

    // Following method return expectedType extracted from BLangNode.
    private Optional<TypeSymbol> getExpectedType(BLangNode node) {
        if (node == null) {
            return Optional.empty();
        }

        BType bType = null;
        switch (node.getKind()) {
            case RECORD_LITERAL_KEY_VALUE:
                BLangRecordLiteral.BLangRecordKeyValueField field = (BLangRecordLiteral.BLangRecordKeyValueField) node;
                bType = field.getValue().expectedType;
                break;
            case USER_DEFINED_TYPE:
                bType = node.getBType();
                break;
            case VARIABLE:
                if (((BLangSimpleVariable) node).expr != null) {
                    bType = ((BLangSimpleVariable) node).expr.expectedType;
                }

                break;
            case TABLE_CONSTRUCTOR_EXPR:
                bType = ((BLangTableConstructorExpr) node).expectedType;
                break;
            case RECORD_LITERAL_EXPR:
                bType = ((BLangRecordLiteral) node).expectedType;
                break;
            case SIMPLE_VARIABLE_REF:
                if (node instanceof BLangRecordLiteral recordLiteral) {
                    BLangExpression bLangExpression = recordLiteral.impConversionExpr;
                    if (bLangExpression != null && bLangExpression.getBType().getKind() == TypeKind.ANY) {
                        bType = bLangExpression.getBType();
                        break;
                    }
                }

                bType = ((BLangSimpleVarRef) node).expectedType;
                break;
            case NAMED_ARGS_EXPR:
                bType = ((BLangNamedArgsExpression) node).expectedType;
                break;
            case LIST_CONSTRUCTOR_EXPR:
                bType = ((BLangListConstructorExpr) node).expectedType;
                break;
            case LITERAL:
                bType = ((BLangLiteral) node).expectedType;
                break;
            case NUMERIC_LITERAL:
                bType = ((BLangNumericLiteral) node).expectedType;
                break;
            case FIELD_BASED_ACCESS_EXPR:
                bType = ((BLangFieldBasedAccess) node).expectedType;
                break;
            case TYPE_INIT_EXPR:
                bType = ((BLangTypeInit) node).expectedType;
                break;
            case INDEX_BASED_ACCESS_EXPR:
                bType = ((BLangIndexBasedAccess) node).expectedType;
                break;
            case INVOCATION:
                bType = ((BLangInvocation) node).expectedType;
                if (bType.getKind() == TypeKind.OTHER && ((BLangInvocation) node).symbol != null) {
                    bType = ((BInvokableSymbol) ((BLangInvocation) node).symbol).retType;
                }
                break;
            default:
        }

        if (bType != null && bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getTypeFromBType(BType type) {
        BType bType = type;
        if (type.getKind() == TypeKind.MAP) {
            bType = ((BMapType) type).constraint;
        }

        if (type instanceof BTypeReferenceType
                && ((BTypeReferenceType) type).referredType.getKind() == TypeKind.RECORD) {
            return Optional.of(typesFactory.getTypeDescriptor(((BTypeReferenceType) type).referredType));

        }

        if (bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getSymbolType(BSymbol symbol) {
        BType bType = null;

        if (symbol.getKind() == SymbolKind.VARIABLE || symbol.getKind() == SymbolKind.FUNCTION) {
            bType = symbol.getType();
        }

        if (bType != null && bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    private Optional<Symbol> findSymbol(Predicate<Symbol> predicate) {
        return this.semanticModel.visibleSymbols(document, linePosition).stream()
                .filter(predicate)
                .findFirst();
    }

    private Optional<Symbol> findSymbolByName(String name) {
        Predicate<Symbol> namePredicate = symbol -> symbol.getName().orElse("").equals(name);
        return findSymbol(namePredicate);
    }

    private Optional<Symbol> findSymbolByName(String name, Predicate<Symbol> predicate) {
        Predicate<Symbol> namePredicate = symbol -> symbol.getName().orElse("").equals(name);
        return findSymbol(namePredicate.and(predicate));
    }

    private Optional<ModuleSymbol> searchModuleForAlias(String alias) {
        List<Symbol> visibleSymbols = this.semanticModel.visibleSymbols(document, linePosition);
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && Objects.equals(symbol.getName().orElse(null), alias)) {
                return Optional.of((ModuleSymbol) symbol);
            }
        }

        return Optional.empty();
    }

    private List<Symbol> getModuleSymbols(QualifiedNameReferenceNode qNameRef,
                                          Predicate<Symbol> predicate) {
        String modulePrefix = qNameRef.modulePrefix().text();
        String alias = modulePrefix.startsWith("'") ? modulePrefix.substring(1) : modulePrefix;
        Optional<ModuleSymbol> module = searchModuleForAlias(alias);
        return module.map(moduleSymbol -> moduleSymbol.allSymbols().stream()
                        .filter(predicate)
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    private Optional<Symbol> getQNameReferenceSymbol(QualifiedNameReferenceNode node, Predicate<Symbol> predicate) {
        List<Symbol> moduleSymbols = getModuleSymbols(node, predicate);
        if (moduleSymbols.size() != 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(moduleSymbols.get(0));
    }

    /**
     * Get the TypeSymbol related to the parameter at the given index.
     *
     * @param bLangNode     bLangInvocationNode related to the function/method
     * @param argumentIndex index of the argument
     * @param namedArgs     list of named arguments
     * @return the type symbol if available, if not, returns empty
     */
    private Optional<TypeSymbol> getParamType(BLangInvocation bLangNode, int argumentIndex, List<String> namedArgs) {
        BSymbol symbol = bLangNode.symbol;
        if (symbol == null) {
            return Optional.empty();
        }

        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symbol;
        int paramsSize = invokableSymbol.params.size();
        // The function can have required/defaultable parameters or it can have only a rest parameter.
        if (!invokableSymbol.params.isEmpty() && argumentIndex < paramsSize) {
            if (namedArgs.isEmpty()) {
                return getSymbolType(invokableSymbol.params.get(argumentIndex));
            }
            // If the function has named arguments, we need to find the next possible named argument.
            //Skip the first n arguments where n is the number of positional arguments specified by the user.
            int numOfPositionalArgs = argumentIndex - namedArgs.size();
            return invokableSymbol.params.subList(numOfPositionalArgs, paramsSize)
                    .stream().filter(param -> !namedArgs.contains(param.name.value))
                    .findFirst().flatMap(this::getSymbolType);
        }
        if (invokableSymbol.restParam != null) {
            return getSymbolType(invokableSymbol.restParam)
                    .filter(type -> type.typeKind() == TypeDescKind.ARRAY)
                    .map(typeSymbol -> ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor());
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
        if (getRawType(typeSymbol).typeKind() == TypeDescKind.RECORD) {
            try {
                unionTypeMembers
                        .add(builder.TABLE_TYPE.withRowType(getRawType(typeSymbol)).build());
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

    private static TypeSymbol getRawType(TypeSymbol typeDescriptor) {
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

    /**
     * Get the expected type for a function argument.
     *
     * @param bLangNode     bLangNode related to the function/method
     * @param openParen     Open parenthesis token of the function
     * @param closeParen    Close parenthesis token of the function
     * @return the type symbol if available, if not, returns empty
     */
    private Optional<TypeSymbol> getExpectedTypeFromFunction(BLangNode bLangNode, Token openParen, Token closeParen) {
        if (!isWithinParenthesis(openParen, closeParen)) {
            return getExpectedType(bLangNode);
        }

        BLangInvocation bLangInvocation = (BLangInvocation) bLangNode;
        int size = bLangInvocation.argExprs.size();
        boolean langLibInvocation = bLangInvocation.langLibInvocation;

        if (size == 0 && !langLibInvocation) {
            return getParamType(bLangInvocation, 0, Collections.emptyList());
        }

        // func(arg1, arg<cursor>, 10)
        int argumentIndex = 0;
        List<String> namedArgs = new ArrayList<>();
        for (BLangNode nodeInst : bLangInvocation.argExprs) {
            // only offset is considered
            if (nodeInst.getPosition().lineRange().endLine().offset() < linePosition.offset()) {
                if (nodeInst.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                    namedArgs.add(((BLangNamedArgsExpression) nodeInst).name.value);
                }
                argumentIndex += 1;
            }
        }

        if (langLibInvocation) {
            if (bLangInvocation.expr.getBType().getKind() == TypeKind.ARRAY) {
                return Optional.ofNullable(typesFactory.getTypeDescriptor
                        (((BArrayType) bLangInvocation.expr.expectedType).eType));
            }

            // First argExpr is the expr of the BLangInvocationNode. Therefore, it should not be considered
            // and the type of the second arg should be returned
            return getParamType(bLangInvocation, argumentIndex, namedArgs);
        }

        // As mentioned in issue #41573, since there is an inconsistency in the generated line range, the following 
        // condition is added in case of an out-of-index error.
        if (argumentIndex >= bLangInvocation.argExprs.size()) {
            return getExpectedType(
                    nodeFinder.lookup(this.bLangCompilationUnit, this.functionArgNodeAtCursor.lineRange()));
        }

        BLangExpression bLangExpression = bLangInvocation.argExprs.get(argumentIndex);
        if (bLangExpression.toString().startsWith("$missingNode$")) {
            return getParamType(bLangInvocation, argumentIndex, namedArgs);
        }

        return getExpectedType(bLangExpression);
    }

    private boolean isParenthesisMissing(Token openParen, Token closeParen) {
        return openParen.isMissing() || closeParen.isMissing();
    }

    private boolean isWithinParenthesis(Token openParen, Token closeParen) {
        return !isParenthesisMissing(openParen, closeParen) &&
                isPosWithinOpenCloseLineRanges(linePosition, openParen.lineRange(), closeParen.lineRange());
    }

    private Optional<TypeSymbol> transformFunctionOrMethod(BLangNode bLangNode, BType parameterType,
                                                           BInvokableSymbol originalInvokable,
                                                           SeparatedNodeList<FunctionArgumentNode> arguments,
                                                           Token openParenToken, Token closeParenToken,
                                                           boolean hasFirstArg) {
        if (originalInvokable == null || originalInvokable.params.isEmpty()) {
            return getExpectedTypeFromFunction(bLangNode, openParenToken, closeParenToken);
        }

        // The first parameter is always the value of the type.
        BVarSymbol firstParam = originalInvokable.params.get(0);
        BType typeParam = new TypeParamFinder().find(firstParam.getType());

        if (typeParam == null) {
            return getExpectedTypeFromFunction(bLangNode, openParenToken, closeParenToken);
        }

        BInvokableSymbol langLibMethod =
                this.langLibFunctionBinder.cloneAndBind(originalInvokable, parameterType,
                        SymbolUtils.getTypeParamBoundType(parameterType));

        // Since the first parameter is always the value, the function should have at least two arguments for it to
        // possibly contain a function. For instance, In the langlib method, `array:map(arr, mappingFunction)`, the
        // first argument is always arr which is the value.
        if (langLibMethod.getParameters().size() < 2) {
            return getExpectedTypeFromFunction(bLangNode, openParenToken, closeParenToken);
        }

//        TODO: Need to implement code to detect the position when the `PositionalArgumentNode` is null.
//        (e.g. `arr.sort(array:ASCENDING, <cursor >`). Since sort is the only lang lib function that accepts a
//        @typeParam function in the second parameter and it is yet to be supported, this should be implemented
//        along with #41498.
        int position = hasFirstArg ? 0 : 1;
        if (this.functionArgNodeAtCursor != null) {
            for (FunctionArgumentNode argNode : arguments) {
                if (argNode.equals(this.functionArgNodeAtCursor)) {
                    break;
                }
                position++;
            }
        }

        return Optional.of(typesFactory.getTypeDescriptor(
                langLibMethod.getType().getParameterTypes().get(position)));
    }
}
