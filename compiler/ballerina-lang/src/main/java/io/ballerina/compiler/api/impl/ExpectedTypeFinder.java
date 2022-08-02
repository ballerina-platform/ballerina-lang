package io.ballerina.compiler.api.impl;


import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.impl.util.CommonUtil;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.impl.util.TypeResolverUtil;
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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;



/**
 *
 * @since 2.0.0
 */
public class ExpectedTypeFinder extends NodeTransformer<Optional<TypeSymbol>> {

    private final SemanticModel semanticModel;
    private final BLangCompilationUnit bLangCompilationUnit;
    //TODO remove and add semantic model
    private final List<Node> visitedNodes = new ArrayList<>();
    private final TypesFactory typesFactory;

    private final LinePosition linePosition;

    private final NodeFinder nodeFinder;

    public  ExpectedTypeFinder(SemanticModel semanticModel, BLangCompilationUnit bLangCompilationUnit,
                              TypesFactory typesFactory, LinePosition linePosition) {
        this.semanticModel = semanticModel;
        this.bLangCompilationUnit = bLangCompilationUnit;
        this.typesFactory = typesFactory;
        this.linePosition = linePosition;
        this.nodeFinder = new NodeFinder(true);
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        // sample case 1
        // int x = <cursor>; cursor position identified as , $missingNode
        //
        // sample case 2
        // if (x < <cursor>) ; cursor position node is null
        //
        BLangNode lookUpNode = nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange());
        if (lookUpNode == null) {
            return Optional.empty();
        }
        // function main() {
        //   while<cursor>
        // cant use BType

        // check whether is it possible to generalize this case
        return Optional.of(typesFactory.getTypeDescriptor(((BLangSimpleVarRef) lookUpNode).expectedType));
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
        Optional<ExpressionNode> initializer = node.initializer();
        if (initializer.isPresent() && PositionUtil.posWithinRange(linePosition, initializer.get().lineRange())) {
            return this.visit(initializer.get());
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        if (node.initializer().isPresent()
                && PositionUtil.posWithinRange(linePosition, node.initializer().get().lineRange())) {
            BLangNode bLangNode = nodeFinder.lookup(this.bLangCompilationUnit, node.initializer().get().lineRange());
            return Optional.
                    of(typesFactory.getTypeDescriptor(((BLangExpression) bLangNode).expectedType));
        }

        return Optional.empty();
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
        return Optional.of(typesFactory.
                getTypeDescriptor(nodeFinder.lookup(this.bLangCompilationUnit, node.lineRange()).getBType()));
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

        //TODO recheck this
        return this.visit(node);
    }

    @Override
    public Optional<TypeSymbol> transform(WhileStatementNode node) {
        //TODO check sample case
        //function main() {
        //    while <cursor> {}
        //}
        if (PositionUtil.posWithinRange(linePosition, node.condition().lineRange())) {
            return this.visit(node.condition());
        }

        //TODO recheck this
        return this.visit(node);
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        // check for more sample cases
        return Optional.empty();
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }

        visitedNodes.add(node);
        return node.apply(this);
    }
}