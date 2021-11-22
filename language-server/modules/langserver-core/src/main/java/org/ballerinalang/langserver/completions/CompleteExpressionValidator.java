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
package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLQualifiedNameNode;

/**
 * Validates whether the expressions are completed.
 * 
 * @since 2.0.0
 */
public class CompleteExpressionValidator extends NodeTransformer<Boolean> {

    @Override
    public Boolean transform(TemplateExpressionNode node) {
        return !node.endBacktick().isMissing();
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        return !node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(TableConstructorExpressionNode node) {
        return !node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(MappingConstructorExpressionNode node) {
        return !node.closeBrace().isMissing();
    }

    @Override
    public Boolean transform(ObjectConstructorExpressionNode node) {
        return !node.closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ExplicitNewExpressionNode node) {
        return !node.parenthesizedArgList().closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(ImplicitNewExpressionNode node) {
        return (node.parenthesizedArgList().isEmpty() && !node.newKeyword().isMissing())
                || !node.parenthesizedArgList().get().closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(IdentifierToken node) {
        return !node.isMissing();
    }

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        return !node.name().isMissing();
    }

    @Override
    public Boolean transform(QualifiedNameReferenceNode node) {
        return node.identifier().apply(this);
    }

    @Override
    public Boolean transform(XMLQualifiedNameNode node) {
        return node.name().apply(this);
    }

    @Override
    public Boolean transform(FieldAccessExpressionNode node) {
        return node.fieldName().apply(this);
    }

    @Override
    public Boolean transform(OptionalFieldAccessExpressionNode node) {
        return node.fieldName().apply(this);
    }

    @Override
    public Boolean transform(AnnotAccessExpressionNode node) {
        return node.annotTagReference().apply(this);
    }

    @Override
    public Boolean transform(IndexedExpressionNode node) {
        return !node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(FunctionCallExpressionNode node) {
        return !node.closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(MethodCallExpressionNode node) {
        return !node.closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(ErrorConstructorExpressionNode node) {
        return !node.closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(ExplicitAnonymousFunctionExpressionNode node) {
        return node.functionBody().apply(this);
    }

    @Override
    public Boolean transform(FunctionBodyBlockNode node) {
        return !node.closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ExpressionFunctionBodyNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(LetExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TypeCastExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TypeofExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(UnaryExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        return node.rhsExpr().apply(this);
    }

    @Override
    public Boolean transform(TypeTestExpressionNode node) {
        return node.typeDescriptor().apply(this);
    }

    @Override
    public Boolean transform(ConditionalExpressionNode node) {
        return node.endExpression().apply(this);
    }

    @Override
    public Boolean transform(CheckExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TrapExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(QueryExpressionNode node) {
        return (node.onConflictClause().isEmpty()
                && node.onConflictClause().get().expression().apply(this))
                || node.selectClause().expression().apply(this);
    }

    @Override
    public Boolean transform(XMLFilterExpressionNode node) {
        return !node.xmlPatternChain().gtToken().isMissing();
    }
    
    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return false;
    }
}
