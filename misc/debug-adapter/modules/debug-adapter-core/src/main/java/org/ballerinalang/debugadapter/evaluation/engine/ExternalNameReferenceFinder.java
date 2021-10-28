/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternFullNode;
import io.ballerina.compiler.syntax.tree.FieldBindingPatternVarnameNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LimitClauseNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.RestBindingPatternNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Syntax tree visitor implementation to capture all the "captured" variable references within a given Ballerina
 * expression.
 * <p>
 * All the variable references that are defined within the expressions (i.e internal variables)
 * will be ignored. (e.g. declared variables in let expressions, binding patterns in query expressions).
 *
 * @since 2.0.0
 */
public class ExternalNameReferenceFinder extends NodeVisitor {

    private final ExpressionNode expressionNode;
    private final Set<String> internalVariables = new HashSet<>();
    private final Set<String> capturedVariables = new HashSet<>();

    public ExternalNameReferenceFinder(ExpressionNode node) {
        this.expressionNode = node;
    }

    /**
     * @return Captures and returns all the variable references within the given user expression node.
     */
    public Set<String> getCapturedVariables() {
        expressionNode.accept(this);
        return capturedVariables;
    }

    // ############################### Query Expression Clause Nodes ############################### //

    @Override
    public void visit(FromClauseNode fromClauseNode) {
        fromClauseNode.typedBindingPattern().accept(this);
        fromClauseNode.expression().accept(this);
    }

    @Override
    public void visit(WhereClauseNode whereClauseNode) {
        whereClauseNode.expression().accept(this);
    }

    @Override
    public void visit(JoinClauseNode joinClauseNode) {
        joinClauseNode.typedBindingPattern().accept(this);
        joinClauseNode.expression().accept(this);
    }

    @Override
    public void visit(LetClauseNode letClauseNode) {
        letClauseNode.letVarDeclarations().forEach(declaration -> {
            declaration.typedBindingPattern().accept(this);
            declaration.expression().accept(this);
        });
    }

    @Override
    public void visit(LimitClauseNode limitClauseNode) {
        limitClauseNode.expression().accept(this);
    }

    @Override
    public void visit(SelectClauseNode selectClauseNode) {
        selectClauseNode.expression().accept(this);
    }

    @Override
    public void visit(OnConflictClauseNode onConflictClauseNode) {
        onConflictClauseNode.expression().accept(this);
    }

    // ################################## Expression Nodes ################################## //

    @Override
    public void visit(BracedExpressionNode bracedExpressionNode) {
        bracedExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(BinaryExpressionNode binaryExpressionNode) {
        binaryExpressionNode.lhsExpr().accept(this);
        binaryExpressionNode.rhsExpr().accept(this);
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        functionCallExpressionNode.arguments().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        methodCallExpressionNode.expression().accept(this);
        methodCallExpressionNode.arguments().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(ErrorConstructorExpressionNode errorConstructorExpressionNode) {
        errorConstructorExpressionNode.arguments().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {
        fieldAccessExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {
        optionalFieldAccessExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(ConditionalExpressionNode conditionalExpressionNode) {
        conditionalExpressionNode.lhsExpression().accept(this);
        conditionalExpressionNode.middleExpression().accept(this);
        conditionalExpressionNode.endExpression().accept(this);
    }

    @Override
    public void visit(TypeofExpressionNode typeofExpressionNode) {
        typeofExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(IndexedExpressionNode indexedExpressionNode) {
        indexedExpressionNode.containerExpression().accept(this);
        indexedExpressionNode.keyExpression().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(TypeTestExpressionNode typeTestExpressionNode) {
        typeTestExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(TypeCastExpressionNode typeCastExpressionNode) {
        typeCastExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(AnnotAccessExpressionNode annotAccessExpressionNode) {
        annotAccessExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(TemplateExpressionNode templateExpressionNode) {
        visitSyntaxNode(templateExpressionNode);
    }

    @Override
    public void visit(InterpolationNode interpolationNode) {
        interpolationNode.expression().accept(this);
    }

    @Override
    public void visit(XMLStepExpressionNode xmlStepExpressionNode) {
        xmlStepExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(XMLFilterExpressionNode xmlFilterExpressionNode) {
        xmlFilterExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(TrapExpressionNode trapExpressionNode) {
        trapExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(UnaryExpressionNode unaryExpressionNode) {
        unaryExpressionNode.expression().accept(this);
    }

    @Override
    public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
        explicitNewExpressionNode.parenthesizedArgList().arguments().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
        implicitNewExpressionNode.parenthesizedArgList().ifPresent(parenthesizedArgList ->
                parenthesizedArgList.arguments().forEach(node -> node.accept(this)));
    }

    @Override
    public void visit(QueryExpressionNode queryExpressionNode) {
        visitSyntaxNode(queryExpressionNode);
    }

    @Override
    public void visit(LetExpressionNode letExpressionNode) {
        letExpressionNode.letVarDeclarations().forEach(declarationNode -> declarationNode.accept(this));
        letExpressionNode.expression().accept(this);
    }

    // ############################### Function Argument Nodes ############################### //

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {
        positionalArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {
        namedArgumentNode.expression().accept(this);
    }

    @Override
    public void visit(RestArgumentNode restArgumentNode) {
        restArgumentNode.expression().accept(this);
    }

    // ############################## Binding Pattern Nodes ############################## //

    @Override
    public void visit(TypedBindingPatternNode typedBindingPatternNode) {
        typedBindingPatternNode.bindingPattern().accept(this);
    }

    @Override
    public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
        internalVariables.add(captureBindingPatternNode.variableName().text().trim());
    }

    @Override
    public void visit(ListBindingPatternNode listBindingPatternNode) {
        listBindingPatternNode.bindingPatterns().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(MappingBindingPatternNode mappingBindingPatternNode) {
        mappingBindingPatternNode.fieldBindingPatterns().forEach(node -> node.accept(this));
    }

    @Override
    public void visit(FieldBindingPatternFullNode fieldBindingPatternFullNode) {
        fieldBindingPatternFullNode.bindingPattern().accept(this);
    }

    @Override
    public void visit(FieldBindingPatternVarnameNode fieldBindingPatternVarnameNode) {
        internalVariables.add(fieldBindingPatternVarnameNode.variableName().name().text().trim());
    }

    @Override
    public void visit(RestBindingPatternNode restBindingPatternNode) {
        internalVariables.add(restBindingPatternNode.variableName().name().text().trim());
    }

    @Override
    public void visit(ErrorBindingPatternNode errorBindingPatternNode) {
        errorBindingPatternNode.argListBindingPatterns().forEach(node -> node.accept(this));
    }

    // ################################## Other Nodes ################################## //

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        String variableRef = simpleNameReferenceNode.name().text().trim();
        if (!internalVariables.contains(variableRef)) {
            capturedVariables.add(variableRef);
        }
    }
}
