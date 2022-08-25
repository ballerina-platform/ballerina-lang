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
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;

import io.ballerina.compiler.api.impl.util.NodeUtil;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.impl.PositionUtil.isWithinParenthesis;
import static org.ballerinalang.model.tree.NodeKind.ERROR_CONSTRUCTOR_EXPRESSION;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_KEY_VALUE;
import static org.ballerinalang.model.tree.NodeKind.USER_DEFINED_TYPE;
import static org.ballerinalang.model.tree.NodeKind.VARIABLE;
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
    private final SymbolFactory symbolFactory;

    private final LinePosition linePosition;

    private final NodeFinder nodeFinder;
    private final SymbolFinder symbolFinder;

    private final SymbolTable symbolTable;

    public  ExpectedTypeFinder(SemanticModel semanticModel, BLangCompilationUnit bLangCompilationUnit,
                               TypesFactory typesFactory, LinePosition linePosition, SymbolFactory symbolFactory,
                               SymbolTable symbolTable) {
        this.semanticModel = semanticModel;
        this.bLangCompilationUnit = bLangCompilationUnit;
        this.typesFactory = typesFactory;
        this.linePosition = linePosition;
        this.nodeFinder = new NodeFinder(true);
        this.symbolFinder = new SymbolFinder();
        this.symbolFactory = symbolFactory;
        this.symbolTable = symbolTable;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        // sample case 1
        // int x = <cursor>; cursor position identified as , $missingNode
        //
        // sample case 2
        // if (x < <cursor>) ; cursor position node is null
        //

        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        Optional<BType> expectedType = NodeUtil.getExpectedType(bLangNode);
        if (expectedType.isPresent()) {
            return Optional.of(typesFactory.getTypeDescriptor(expectedType.get()));
        }

        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(SpecificFieldNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return getRawType(bLangNode);
    }

    @Override
    public Optional<TypeSymbol> transform(ObjectFieldNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        Optional<BType> expectedType = NodeUtil.getExpectedType(bLangNode);
        if (expectedType.isPresent()) {
            return Optional.of(typesFactory.getTypeDescriptor(expectedType.get()));
        }

        return Optional.empty();
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
        if (bLangNode instanceof BLangFieldBasedAccess) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangFieldBasedAccess) bLangNode).expectedType));
        }

        return Optional.empty();
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
        BLangNode rhsNode = nodeFinder.lookup(this.bLangCompilationUnit, node.rhsExpr().lineRange());
        BLangNode lhsNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lhsExpr().lineRange());
        //TODO Try to reduce code lines - refactor
        if (PositionUtil.posWithinRange(linePosition, node.rhsExpr().lineRange())) {
            if (rhsNode != null) {
                return this.visit(node.rhsExpr());
            }

            // if rhs node is null and lhs node is not null
            if (lhsNode != null) {
                return this.visit(node.lhsExpr());
            }
        } else {
            if (lhsNode != null) {
                return this.visit(node.lhsExpr());
            }

            // if lhs node is null and lhs node is not null
            if (rhsNode != null) {
                return this.visit(node.rhsExpr());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());

        if (bLangNode instanceof BLangInvocation) {
            // check position is in parameter context
            if (isWithinParenthesis(linePosition, node.openParenToken(), node.closeParenToken())) {
                int size = ((BLangInvocation) bLangNode).argExprs.size();
                if (size == 0) {
                    BSymbol symbol = ((BLangInvocation) bLangNode).symbol;
                    BInvokableType type = (BInvokableType) ((BInvokableSymbol) symbol).params.get(0).getType();
                    return Optional.of(typesFactory.getTypeDescriptor(type));
                } else {
                    int argumentIndex = 0;
                    for (BLangNode node1 : ((BLangInvocation) bLangNode).argExprs) {
                        // Need line check
                        if (node1.getPosition().lineRange().endLine().offset() < linePosition.offset()) {
                            argumentIndex += 1;
                        }
                    }

                    return Optional.of(typesFactory.getTypeDescriptor(((BLangInvocation) bLangNode)
                            .argExprs.get(argumentIndex).expectedType));

                    // add lang lib check
                }
            }

            return Optional.of(typesFactory.getTypeDescriptor(((BLangInvocation) bLangNode).expectedType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(MethodCallExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());

        if (bLangNode instanceof BLangInvocation) {
            Optional<BLangExpression> argument = ((BLangInvocation) bLangNode).argExprs.stream()
                    .filter(argumentNode -> PositionUtil.posWithinRange(linePosition,
                            argumentNode.getPosition().lineRange()))
                    .findFirst();

            if (argument.isPresent()) {
                return Optional.
                        of(typesFactory.getTypeDescriptor(argument.get().expectedType));
            }
        }

        // check if its in the body and return function return type
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
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode instanceof BLangSimpleVarRef) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangSimpleVarRef) bLangNode).expectedType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode instanceof BLangRecordLiteral) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangRecordLiteral) bLangNode).expectedType));
        }

        return this.visit(node.expression());
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode node) {
        Optional<Symbol> lookupSymbol = lookupSymbol(this.bLangCompilationUnit, linePosition);
        if (lookupSymbol.isPresent() &&
                lookupSymbol.get() instanceof FunctionSymbol) {
            return (((BallerinaFunctionSymbol) lookupSymbol.get()).typeDescriptor().returnTypeDescriptor());
        }

        return Optional.empty();
    }

    public Optional<TypeSymbol> transform(MatchClauseNode matchClauseNode) {
        return matchClauseNode.parent().apply(this);
    }

    public Optional<TypeSymbol> transform(MatchStatementNode matchClauseNode) {
        return matchClauseNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(MappingMatchPatternNode mappingMatchPatternNode) {
        return mappingMatchPatternNode.parent().apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitNewExpressionNode implicitNewExpressionNode) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, implicitNewExpressionNode.lineRange());
        // add parameter type context
        if (bLangNode instanceof BLangTypeInit) {
            return Optional.
                    of(typesFactory.getTypeDescriptor(((BLangTypeInit) bLangNode).expectedType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ImplicitNewExpressionNode implicitNewExpressionNode) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, implicitNewExpressionNode.lineRange());
        // add parameter type context
        // union case is covered by the following case
        if (bLangNode instanceof BLangTypeInit) {
            return Optional.
                    of(typesFactory.getTypeDescriptor(((BLangTypeInit) bLangNode).expectedType));
        }

        return Optional.empty();
    }
    @Override
    public Optional<TypeSymbol> transform(IfElseStatementNode node) {
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
        return Optional.of(typesFactory.getTypeDescriptor(bLangNode.getBType()));
    }

    @Override
    public Optional<TypeSymbol> transform(ExpressionFunctionBodyNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit,
                node.expression().lineRange());
        return Optional.of(typesFactory.getTypeDescriptor(bLangNode.getBType()));
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode instanceof BLangIndexBasedAccess) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangIndexBasedAccess) bLangNode).expectedType));
        }

        return Optional.empty();
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
        if (bLangNode instanceof BLangInvocation.BLangActionInvocation) {
            BLangInvocation.BLangActionInvocation bLangActionInvocation = (BLangInvocation.BLangActionInvocation)
                    nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
            Optional<BLangExpression> argument = bLangActionInvocation.argExprs.stream().filter
                            (argumentNode -> PositionUtil.posWithinRange(linePosition, argumentNode.getPosition()
                                    .lineRange())).findFirst();
            return Optional.of(typesFactory.getTypeDescriptor(argument.get().expectedType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ListConstructorExpressionNode node) {
        //TODO add checks
        return Optional.of(typesFactory.getTypeDescriptor(
                ((BArrayType)((BLangListConstructorExpr) nodeFinder
                        .lookup(this.bLangCompilationUnit, node.lineRange())).expectedType).eType));
    }

    @Override
    public Optional<TypeSymbol> transform(CaptureBindingPatternNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode.getBType().getKind() == TypeKind.MAP) {
            return Optional.of(typesFactory.getTypeDescriptor(((BMapType) bLangNode.getBType()).constraint));
        }

        return Optional.of(typesFactory.getTypeDescriptor(bLangNode.getBType()));
    }

    // address mapping and tables
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
    public Optional<TypeSymbol> transform(TableConstructorExpressionNode node) {
        // get type symbol
        // check key specifier and row parameter

        return Optional.empty();
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

    // lookupSymbol implementation and need to check the possibility to refactor this
    private Optional<Symbol> lookupSymbol(BLangCompilationUnit compilationUnit, LinePosition position) {
        SymbolFinder symbolFinder = new SymbolFinder();
        BSymbol symbolAtCursor = symbolFinder.lookup(compilationUnit, position);

        if (symbolAtCursor == null || symbolAtCursor == symbolTable.notFoundSymbol) {
            return Optional.empty();
        }

        if (symbolAtCursor.kind == SymbolKind.TYPE_DEF
                && isCursorNotAtDefinition(compilationUnit, symbolAtCursor, position)) {
            return Optional.ofNullable(
                    typesFactory.getTypeDescriptor(((BTypeDefinitionSymbol) symbolAtCursor).referenceType));
        }

        if (isTypeSymbol(symbolAtCursor) &&
                (isInlineSingletonType(symbolAtCursor) || isInlineErrorType(symbolAtCursor)
                        || isCursorNotAtDefinition(compilationUnit, symbolAtCursor, position))) {
            return Optional.ofNullable(
                    typesFactory.getTypeDescriptor(symbolAtCursor.type, symbolAtCursor));
        }

        return Optional.ofNullable(symbolFactory.getBCompiledSymbol(symbolAtCursor,
                symbolAtCursor.getOriginalName().getValue()));
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

    // TODO add returnStatementNode if necessary
    private Optional<TypeSymbol> getRawType(BLangNode node) {
        if (node.getKind() == RECORD_LITERAL_KEY_VALUE) {
            return Optional.of(typesFactory.getTypeDescriptor(node.getBType()));
        } else if (node.getKind() == VARIABLE) {
            return Optional.of(typesFactory.getTypeDescriptor(((BLangSimpleVariable) node).expr.expectedType));
        } else if (node.getKind() == USER_DEFINED_TYPE) {
            return Optional.of(typesFactory.getTypeDescriptor(node.getBType()));
        }

        return Optional.empty();
    }

    public static Boolean isInNewExpressionParameterContext(LinePosition ctx,
                                                            ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> argList = node.parenthesizedArgList();
        if (argList.isEmpty()) {
            return false;
        }
        return isWithinParenthesis(ctx, argList.get().openParenToken(), argList.get().closeParenToken());
    }

}