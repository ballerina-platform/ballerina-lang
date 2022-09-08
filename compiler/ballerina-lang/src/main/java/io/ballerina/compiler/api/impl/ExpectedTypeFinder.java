/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaArrayTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;

import io.ballerina.compiler.api.impl.util.NodeUtil;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
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
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.impl.PositionUtil.isWithinParenthesis;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static org.ballerinalang.model.tree.NodeKind.ERROR_CONSTRUCTOR_EXPRESSION;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.PACKAGE;

/**
 *
 * @since 2201.2.1
 */
public class ExpectedTypeFinder extends NodeTransformer<Optional<TypeSymbol>> {

    private final SemanticModel semanticModel;
    private final BLangCompilationUnit bLangCompilationUnit;
    private final List<Node> visitedNodes = new ArrayList<>();
    private final TypesFactory typesFactory;
    private final LinePosition linePosition;

    private final NodeFinder nodeFinder;
    private final Document document;

    public  ExpectedTypeFinder(SemanticModel semanticModel, BLangCompilationUnit bLangCompilationUnit,
                               CompilerContext context, LinePosition linePosition, Document srcDocument) {
        this.nodeFinder = new NodeFinder(true);
        this.semanticModel = semanticModel;
        this.bLangCompilationUnit = bLangCompilationUnit;
        this.typesFactory = TypesFactory.getInstance(context);
        this.linePosition = linePosition;
        this.document = srcDocument;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(AnnotationNode node) {
        // TODO check why bLang approach is not working
        Node annotationRef = node.annotReference();
        Optional<Symbol> annotationSymbol;
        Predicate<Symbol> predicate = symbol -> symbol.getName().isPresent() && symbol.kind() == io.ballerina.compiler.api.symbols.SymbolKind.ANNOTATION;

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
    public Optional<TypeSymbol> transform(SpecificFieldNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        // TODO can use getElementType(bLangNode.getBType)
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(BasicLiteralNode node) {
        //TODO positional details ignored since only one possible case
        return Optional.of(typesFactory.getTypeDescriptor(
                nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()).getBType()));
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode node) {
        return this.visit(node.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangSimpleVarRef)) {
            return Optional.empty();
        }

        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
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
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        // node.initializer() approach issue
        // node.initializer().get().lineRange()  provides ending position as start of next line
//        if (node.initializer().isPresent()
//                && PositionUtil.posWithinRange(linePosition, node.initializer().get().lineRange())) {
//            BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.initializer().get().lineRange());
//            return Optional.
//                    of(typesFactory.getTypeDescriptor(((BLangExpression) bLangNode).expectedType));
//        }
        return this.visit(node.typedBindingPattern().bindingPattern()); // Pass to captureBindingPattern
    }

    @Override
    public Optional<TypeSymbol> transform(BinaryExpressionNode node) {
        if (node.operator().text().equals("*")) {
            return Optional.empty();
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (!(bLangNode instanceof BLangBinaryExpr)) {
            return Optional.empty();
        }

        BLangBinaryExpr bLangBinaryExpr = (BLangBinaryExpr) bLangNode;
        if (bLangBinaryExpr.rhsExpr.expectedType.getKind() == TypeKind.OTHER &&
                bLangBinaryExpr.lhsExpr.expectedType.getKind() == TypeKind.OTHER) {
            return Optional.empty();
        } else if (!(bLangBinaryExpr.rhsExpr.expectedType.getKind() == TypeKind.OTHER)) {
            return getRawType(bLangBinaryExpr.rhsExpr);

        } else if (!(bLangBinaryExpr.lhsExpr.expectedType.getKind() == TypeKind.OTHER)) {
            return getRawType(bLangBinaryExpr.lhsExpr);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangInvocation) {
            // check position is in parameter context
            if (isWithinParenthesis(linePosition, node.openParenToken(), node.closeParenToken())) {
                int size = ((BLangInvocation) bLangNode).argExprs.size();
                if (size == 0) {
                    BSymbol symbol = ((BLangInvocation) bLangNode).symbol;
                    if (symbol == null) {
                        return Optional.empty();
                    }

                    BSymbol paramSymbol = ((BInvokableSymbol) symbol).params.get(0);
                    return getSymbolType(paramSymbol);
                } else {
                    int argumentIndex = 0;
                    for (BLangNode node1 : ((BLangInvocation) bLangNode).argExprs) {
                        // TODO Need line check
                        if (node1.getPosition().lineRange().endLine().offset() < linePosition.offset()) {
                            argumentIndex += 1;
                        }
                    }

                    if ((((BLangInvocation) bLangNode)
                            .argExprs.get(argumentIndex).expectedType == null)) {
                        return Optional.empty();
                    }

                    return getRawType(((BLangInvocation) bLangNode).argExprs.get(argumentIndex));
                }
            }

            return getRawType(bLangNode);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(MethodCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangInvocation) {
            // check position is in parameter context
            if (isWithinParenthesis(linePosition, node.openParenToken(), node.closeParenToken())) {
                int size = ((BLangInvocation) bLangNode).argExprs.size();
                boolean langLibInvocation = ((BLangInvocation) bLangNode).langLibInvocation;
                if (size == 0) {
                    BSymbol symbol = ((BLangInvocation) bLangNode).symbol;
                    // TODO check this with functionCallExpression
                    // TODO also check whether to add a support function
                    // Testcase : method_call_expression_ctx_source1.bal
                    if (symbol == null) {
                        return Optional.empty();
                    }

                    BSymbol paramSymbol = ((BInvokableSymbol) symbol).params.get(0);
                    return getSymbolType(paramSymbol);
                } else {
                    int argumentIndex = 0;
                    for (BLangNode node1 : ((BLangInvocation) bLangNode).argExprs) {
                        // TODO add line check
                        if (node1.getPosition().lineRange().endLine().offset() < linePosition.offset()) {
                            argumentIndex += 1;
                        }
                    }

                    if (langLibInvocation) {
                        if (((BLangInvocation) bLangNode).expr.getBType().getKind() == TypeKind.ARRAY) {
                            return Optional.ofNullable(typesFactory.getTypeDescriptor
                                    (((BArrayType) ((BLangInvocation) bLangNode).expr.expectedType).eType));
                        }

                        return getRawType(((BLangInvocation) bLangNode).argExprs.get(0));
                    }

                    if ((((BLangInvocation) bLangNode)
                            .argExprs.get(argumentIndex).expectedType == null)) {
                        return Optional.empty();
                    }

                    return getRawType(((BLangInvocation) bLangNode).argExprs.get(argumentIndex));
                }
            }

            return getRawType(bLangNode);
        }

        return Optional.empty();
    }

    public Optional<TypeSymbol> transform(QualifiedNameReferenceNode node) {
        // sample case
        // function getArea() returns float {
        //    float radius = 1.2;
        //    return float:P<cursor> * float:pow(radius, 2);
        //}
        // bLangNode at the cursor position doesn't provide the correct expected type
        // but cloneRef provides the correct expected type.

        // Added to prevent from the case module1:$missingNode returns a expectedType ("string")
        if (node.identifier().isMissing()) {
            return Optional.empty();
        }

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }
        // TODO check for package alias and try to give the returnType
        // sample case : mapping_expr_ctx_config20.json

        if (bLangNode instanceof BLangSimpleVarRef) {
            return getRawType(bLangNode);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitAnonymousFunctionExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangFunction) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangFunction) nodeFinder.lookup
                    (this.bLangCompilationUnit, node.lineRange())).returnTypeNode.getBType()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitAnonymousFunctionExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (!(bLangNode instanceof BLangArrowFunction)) {
            return Optional.empty();
        }

        BType returnType = ((BLangArrowFunction) nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()))
                .funcType.getReturnType();
        //TODO check pos
        if (!node.rightDoubleArrow().isMissing()) {
            // Cursor is at the expression node
            return Optional.of(typesFactory.getTypeDescriptor(returnType));
        }

        // debug this and check for the required return type
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangRecordLiteral) {
            return getRawType(bLangNode);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode node) {
        // Following returns null
        // nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange())
        // symbol can not identify by position
        // Optional<Symbol> lookupSymbol = lookupSymbol(this.bLangCompilationUnit, linePosition);
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = node.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = this.semanticModel.symbol(node);

        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }

        return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
    }

    public Optional<TypeSymbol> transform(MatchClauseNode matchClauseNode) {
        return matchClauseNode.parent().apply(this);
    }

    public Optional<TypeSymbol> transform(MatchStatementNode matchStatementNode) {
        return this.semanticModel.typeOf(matchStatementNode.condition());
    }

    @Override
    public Optional<TypeSymbol> transform(MappingMatchPatternNode mappingMatchPatternNode) {
        return mappingMatchPatternNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitNewExpressionNode implicitNewExpressionNode) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, implicitNewExpressionNode.lineRange());
        // TODO add parameter type context
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, implicitNewExpressionNode.lineRange());
        // TODO add parameter type context
        // union case is covered by the following case
        return getRawType(bLangNode);
    }
    @Override
    public Optional<TypeSymbol> transform(IfElseStatementNode node) {
        //TODO fix this
        // sample case 1
        // function test() {
        //    if  {
        //        x = x + 1;
        //    }
        // }
        // ((BLangIf) nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange())).getCondition() ->
        // returns BLangRecordLiteralNode {x: x +1}
        // but expectedType is boolean

        // sample case 2
        // function test() {
        //    if <cursor>
        //}
        // can get type by calling expected type

        if (PositionUtil.posWithinRange(linePosition, node.condition().lineRange())) {
            return this.visit(node.condition());
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(WhileStatementNode node) {
        if (PositionUtil.posWithinRange(linePosition, node.condition().lineRange())) {
            return this.visit(node.condition());
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(DefaultableParameterNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ExpressionFunctionBodyNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.expression().lineRange());
        return getRawType(bLangNode);
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(RemoteMethodCallActionNode node) {
        // Approach 1 - following returns null symbol
        // Optional<Symbol> methodSymbol = lookupSymbol(this.bLangCompilationUnit, linePosition);

        // Approach 2
        // ((BLangInvocation.BLangActionInvocation) nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()))
        // .argExprs.get(0).expectedType
        // above statement returns null for the expected type
        // ((BLangInvocation.BLangActionInvocation) nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()))
        // .argExprs.get(0).getBType() can be used

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode instanceof BLangInvocation.BLangActionInvocation) {
            BLangInvocation.BLangActionInvocation bLangActionInvocation = (BLangInvocation.BLangActionInvocation)
                    nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
            if (isWithinParenthesis(linePosition, node.openParenToken(), node.closeParenToken())) {
                Optional<BLangExpression> argument = bLangActionInvocation.argExprs.stream().filter
                        (argumentNode -> PositionUtil.posWithinRange(linePosition, argumentNode.getPosition()
                                .lineRange())).findFirst();
                if (argument.isPresent()) {
                    return getRawType(argument.get());
                }

            }
            // TODO check whether we need to consider expression separately
            return getRawType(bLangNode);
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ListConstructorExpressionNode node) {
        //TODO add checks
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode == null) {
            return Optional.empty();
        }

        Optional<TypeSymbol> extractedType = getRawType(bLangNode);
        if (!extractedType.isPresent()) {
            return Optional.empty();
        }

        if (extractedType.get().typeKind() != TypeDescKind.ARRAY) {
            return Optional.empty();
        }

        BArrayType bArrayType = (BArrayType) ((BallerinaArrayTypeSymbol) extractedType.get()).getBType();
        if (bArrayType.eType != null && bArrayType.eType.getKind() != TypeKind.OTHER ) {
            return Optional.of(typesFactory.getTypeDescriptor(bArrayType.eType));
        }


        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(CaptureBindingPatternNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        //TODO add null check for other cases if needed
        if (bLangNode == null) {
            return Optional.empty();
        }

        if (bLangNode.getBType().getKind() == TypeKind.MAP) {
            return  getElementType(bLangNode.getBType());
        }

        BType bType = bLangNode.getBType();
        if (bLangNode.getBType().getKind() == TypeKind.OTHER) {
            return Optional.empty();
        }

        return Optional.of(typesFactory.getTypeDescriptor(bType));
    }

    @Override
    public Optional<TypeSymbol> transform(MappingConstructorExpressionNode node) {
        return node.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        // check parameter context by arg
        // else
        // recheck this
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, errorConstructorExpressionNode.lineRange());
        if (bLangNode.getKind() == ERROR_CONSTRUCTOR_EXPRESSION) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangErrorConstructorExpr) bLangNode).expectedType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(NamedArgumentNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(TableConstructorExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (!(bLangNode instanceof BLangTableConstructorExpr)) {
            return Optional.empty();
        }

        if (node.keySpecifier().isPresent() &&
                isWithinParenthesis(linePosition, node.keySpecifier().get().openParenToken(),
                        node.keySpecifier().get().closeParenToken())) {
            BType expectedType = ((BLangTableConstructorExpr) bLangNode).expectedType;
            if (expectedType instanceof BTableType) {
                return Optional.of(typesFactory.getTypeDescriptor(Objects.
                        requireNonNullElse(((BTableType) expectedType).keyTypeConstraint, expectedType)));

            }

        } else if (isWithinParenthesis(linePosition, node.openBracket(), node.closeBracket())) {
            BType rowType = ((BTableType) ((BTypeReferenceType) ((BLangTableConstructorExpr) bLangNode).expectedType)
                    .referredType).constraint;
            return Optional.of(typesFactory.getTypeDescriptor(rowType));
        }

        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(RecordFieldWithDefaultValueNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }

        visitedNodes.add(node);
        return node.apply(this);
    }

    private boolean isCursorNotAtDefinition(BLangCompilationUnit compilationUnit, BSymbol symbolAtCursor,
                                            LinePosition cursorPos) {
        return !(compilationUnit.getPackageID().equals(symbolAtCursor.pkgID)
                && compilationUnit.getName().equals(symbolAtCursor.pos.lineRange().filePath())
                && PositionUtil.withinBlock(cursorPos, symbolAtCursor.pos));
    }

    private boolean isTypeSymbol(BSymbol tSymbol) {
        if (tSymbol.kind == SymbolKind.TYPE_DEF) {
            return true;
        }
        return tSymbol instanceof BTypeSymbol && !Symbols.isTagOn(tSymbol, PACKAGE)
                && !Symbols.isTagOn(tSymbol, ANNOTATION);
    }

    private boolean isInlineSingletonType(BSymbol symbol) {
        // !(symbol.kind == SymbolKind.TYPE_DEF) is checked to exclude type defs
        return !(symbol.kind == SymbolKind.TYPE_DEF) && symbol.type.tag == TypeTags.FINITE &&
                ((BFiniteType) symbol.type).getValueSpace().size() == 1;
    }

    private boolean isInlineErrorType(BSymbol symbol) {
        return symbol.type.tag == TypeTags.ERROR && Symbols.isFlagOn(symbol.type.flags, Flags.ANONYMOUS);
    }

    private Optional<TypeSymbol> getRawType(BLangNode node) {
        if (node == null) {
            return Optional.empty();
        }

        BType bType = null;
        switch (node.getKind()) {
            case RECORD_LITERAL_KEY_VALUE:
            case USER_DEFINED_TYPE:
                bType = node.getBType();
                break;
            case  VARIABLE:
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
                bType = ((BLangSimpleVarRef) node).expectedType;
                break;
            case NAMED_ARGS_EXPR:
                bType = ((BLangNamedArgsExpression) node).expectedType;
                break;
            case LIST_CONSTRUCTOR_EXPR:
                bType = ((BLangListConstructorExpr) node).expectedType;
                break;
                //TODO add other literals as well
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
        }

        if (bType != null && bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getElementType(BType type) {
        BType bType = null;
        if (type.getKind() == TypeKind.MAP) {
            bType = ((BMapType)type).constraint;
        }

        if (bType != null && bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getSymbolType(BSymbol symbol) {
        BType bType = null;
        if (symbol.getKind() == SymbolKind.VARIABLE) {
            bType = symbol.getType();
        } else if (symbol.getKind() == SymbolKind.FUNCTION) {
            bType = symbol.getType();
        }
        // TODO check for more possible cases

        if (bType != null && bType.getKind() != TypeKind.OTHER) {
            return Optional.of(typesFactory.getTypeDescriptor(bType));
        }

        return Optional.empty();
    }

    //TODO add check non null
    private Optional<Symbol> getSymbolByName(String name, Predicate<Symbol> predicate) {
        Predicate<Symbol> namePredicate = symbol -> symbol.getName().orElse("").equals(name);
        return this.semanticModel.visibleSymbols(document,linePosition).stream()
                .filter(namePredicate.and(predicate))
                .findFirst();
    }

    public static Boolean isInNewExpressionParameterContext(LinePosition ctx,
                                                            ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> argList = node.parenthesizedArgList();
        if (argList.isEmpty()) {
            return false;
        }
        return isWithinParenthesis(ctx, argList.get().openParenToken(), argList.get().closeParenToken());
    }

    // Copied from LS

    public Optional<ModuleSymbol> searchModuleForAlias(String alias) {
        List<Symbol> visibleSymbols = this.semanticModel.visibleSymbols(document, linePosition);
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && Objects.equals(symbol.getName().orElse(null), alias)) {
                return Optional.of((ModuleSymbol) symbol);
            }
        }

        return Optional.empty();
    }

    public static String getAlias(QualifiedNameReferenceNode qNameRef) {
        String alias = qNameRef.modulePrefix().text();
        return getAlias(alias);
    }

    private static String getAlias(String alias) {
        return alias.startsWith("'") ? alias.substring(1) : alias;
    }

    public List<Symbol> getModuleContent(QualifiedNameReferenceNode qNameRef,
                                                Predicate<Symbol> predicate) {
        Optional<ModuleSymbol> module = searchModuleForAlias(getAlias(qNameRef));
        return module.map(moduleSymbol -> moduleSymbol.allSymbols().stream()
                        .filter(predicate)
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    private Optional<Symbol> getTypeFromQNameReference(QualifiedNameReferenceNode node, Predicate<Symbol> predicate) {
        List<Symbol> moduleContent = getModuleContent(node, predicate);
        if (moduleContent.size() != 1) {
            // At the moment we do not handle the ambiguity. Hence, consider only single item
            return Optional.empty();
        }

        return Optional.ofNullable(moduleContent.get(0));
    }

}