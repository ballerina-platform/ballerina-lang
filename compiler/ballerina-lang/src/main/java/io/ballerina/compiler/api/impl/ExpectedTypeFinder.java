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
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Optional<Symbol> symbol = lookupSymbol(this.bLangCompilationUnit, linePosition);
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtils.getTypeDescriptor(symbol.get());

    }

    @Override
    public Optional<TypeSymbol> transform(BasicLiteralNode node) {
        //TODO positional details ignored since only one possible case
        return Optional.of(typesFactory.getTypeDescriptor(
                nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()).getBType()));
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode node) {
        // Resolve the following sample cases
        return this.visit(node.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode node) {
        return this.visit(node.typedBindingPattern().bindingPattern());
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        // node.initializer() approach issue
        // node.initializer().get().lineRange() provide (96.0) instead if (95.11)
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
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        return Optional.of(typesFactory.getTypeDescriptor
                (nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()).getBType()));
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode node) {
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

    @Override
    public Optional<TypeSymbol> transform(ImplicitNewExpressionNode node) {
        // need to check arguments
        // check within the parameterContext
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
        //    if<cursor>
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
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (bLangNode instanceof BLangIndexBasedAccess) {
            return Optional.
                    of(typesFactory.getTypeDescriptor(((BLangIndexBasedAccess) bLangNode).expectedType));
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(RemoteMethodCallActionNode node) {
        // Approach 1 - following returns null symobl
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
    public Optional<TypeSymbol> transform(CaptureBindingPatternNode node) {
        return Optional.of(typesFactory.getTypeDescriptor
                (nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()).getBType()));
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

}