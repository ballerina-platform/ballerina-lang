/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.utils;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;

import java.util.stream.StreamSupport;

/**
 * Validates whether the user inputs are completed.
 *
 * @since 2.0.0
 */
public class IncompleteInputFinder extends NodeTransformer<Boolean> {

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        // Check node diagnostics since parseAsExpression("foreach Employee e in t") recovered as
        // SimpleNameReferenceNode
        return node.name().isMissing() || node.hasDiagnostics();
    }

    @Override
    public Boolean transform(BuiltinSimpleNameReferenceNode node) {
        // Check node diagnostics since parseAsExpression("foreach float x in v") recovered as
        // BuiltinSimpleNameReferenceNode
        return node.name().isMissing() || node.hasDiagnostics();
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        return node.rhsExpr().apply(this);
    }

    @Override
    public Boolean transform(FunctionDefinitionNode node) {
        return  node.functionKeyword().isMissing() || node.functionSignature().apply(this)
                || node.functionBody().apply(this);
    }

    @Override
    public Boolean transform(FunctionSignatureNode node) {
        return node.openParenToken().isMissing() || node.closeParenToken().isMissing();
    }

    @Override
    public Boolean transform(FunctionBodyBlockNode node) {
        return node.openBraceToken().isMissing() || node.closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ExplicitAnonymousFunctionExpressionNode node) {
        return node.functionBody().apply(this);
    }

    @Override
    public Boolean transform(BasicLiteralNode basicLiteralNode) {
        return false;
    }

    @Override
    public Boolean transform(ExpressionFunctionBodyNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        return node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(IfElseStatementNode node) {
        return node.ifKeyword().isMissing() || node.ifBody().openBraceToken().isMissing()
                || node.ifBody().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(WhileStatementNode node) {
        return node.whileKeyword().isMissing() || node.whileBody().openBraceToken().isMissing()
                || node.whileBody().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ForEachStatementNode node) {
        return node.forEachKeyword().isMissing() || node.inKeyword().isMissing()
                || node.blockStatement().openBraceToken().isMissing()
                || node.blockStatement().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(VariableDeclarationNode node) {
        if (node.initializer().isPresent()) {
            return node.equalsToken().get().isMissing() || node.initializer().get().apply(this)
                    || node.typedBindingPattern().typeDescriptor().apply(this);
        } else if (node.equalsToken().isPresent()) {
            return node.equalsToken().get().isMissing() || node.typedBindingPattern().typeDescriptor().apply(this)
                    || node.typedBindingPattern().bindingPattern().apply(this);
        } else {
            return node.typedBindingPattern().typeDescriptor().apply(this)
                    || node.typedBindingPattern().bindingPattern().apply(this);
        }
    }

    @Override
    public Boolean transform(MappingBindingPatternNode node) {
        return node.closeBrace().isMissing();
    }

    @Override
    public Boolean transform(ModuleVariableDeclarationNode node) {
        // Diagnostic check to avoid case : annotation not attached to construct
        if (StreamSupport.stream(node.diagnostics().spliterator(), false).
                anyMatch(diagnostic -> diagnostic.diagnosticInfo().code().equals("BCE0524"))) {
            return false;
        }

        if (node.initializer().isPresent()) {
            return node.equalsToken().get().isMissing() || node.initializer().get().apply(this)
                    || node.typedBindingPattern().typeDescriptor().apply(this);
        } else if (node.equalsToken().isPresent()) {
            return node.equalsToken().get().isMissing() || node.typedBindingPattern().typeDescriptor().apply(this);
        } else {
            if (!node.equalsToken().isPresent()) {
                return true;
            }

            return node.typedBindingPattern().typeDescriptor().apply(this);
        }
    }

    @Override
    public Boolean transform(ParenthesisedTypeDescriptorNode node) {
        return node.closeParenToken().isMissing() || node.closeParenToken().hasDiagnostics()
                || node.openParenToken().hasDiagnostics();
    }

    @Override
    public Boolean transform(BlockStatementNode node) {
        // Check openBraceToken() to handle case when missing closed bracket recovered when parsing as block statement
        if (node.openBraceToken().isMissing() || node.closeBraceToken().isMissing()) {
            return true;
        }

        if (node.statements().size() > 0) {
            return node.statements().get(0).apply(this);
        }

        return true;
    }

    @Override
    public Boolean transform(ExpressionStatementNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TypeDefinitionNode node) {
        return node.typeDescriptor().apply(this);
    }

    @Override
    public Boolean transform(RecordTypeDescriptorNode node) {
        return node.bodyStartDelimiter().isMissing() || node.bodyEndDelimiter().isMissing();
    }

    @Override
    public Boolean transform(MappingConstructorExpressionNode node) {
        return node.closeBrace().isMissing();
    }

    @Override
    public Boolean transform(QueryExpressionNode node) {
        return node.queryPipeline().fromClause().fromKeyword().isMissing()
                || node.selectClause().expression().apply(this);
    }

    @Override
    public Boolean transform(SelectClauseNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TableConstructorExpressionNode node) {
        return node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(BracedExpressionNode node) {
        // check node.openParen().hasDiagnostics() to handle cases "if(x==y)" falsely not identifying missing openParen
        return node.openParen().hasDiagnostics() || node.openParen().isMissing() || node.closeParen().isMissing();
    }

    @Override
    public Boolean transform(IndexedExpressionNode node) {
        return node.openBracket().isMissing() || node.closeBracket().isMissing()
                || node.containerExpression().apply(this) || node.keyExpression().get(0).apply(this);
    }

    @Override
    public Boolean transform(LetExpressionNode node) {
        return node.letKeyword().isMissing();
    }

    @Override
    public Boolean transform(ServiceDeclarationNode node) {
        return node.serviceKeyword().isMissing();
    }

    @Override
    public Boolean transform(ConditionalExpressionNode node) {
        return node.colonToken().isMissing() || node.questionMarkToken().isMissing();
    }

    @Override
    public Boolean transform(TypeTestExpressionNode node) {
        return node.isKeyword().isMissing() || node.expression().apply(this);
    }

    @Override
    public Boolean transform(ClassDefinitionNode node) {
        return node.classKeyword().isMissing() || node.openBrace().isMissing() || node.closeBrace().isMissing();
    }

    @Override
    public Boolean transform(TypeCastExpressionNode node) {
        return node.gtToken().isMissing() || node.ltToken().isMissing();
    }

    @Override
    public Boolean transform(TransactionStatementNode node) {
        return node.transactionKeyword().isMissing() || node.blockStatement().apply(this);
    }

    @Override
    public Boolean transform(QualifiedNameReferenceNode node) {
        return node.colon().isMissing();
    }

    @Override
    public Boolean transform(NamedWorkerDeclarationNode node) {
        return node.workerKeyword().isMissing() || node.workerBody().apply(this);
    }

    @Override
    public Boolean transform(NamedWorkerDeclarator node) {
        return node.namedWorkerDeclarations().get(0).workerBody().apply(this);
    }

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return false;
    }
}
