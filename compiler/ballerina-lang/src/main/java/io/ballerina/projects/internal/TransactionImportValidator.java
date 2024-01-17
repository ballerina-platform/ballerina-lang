/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal;

import io.ballerina.compiler.syntax.tree.CommitActionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RetryStatementNode;
import io.ballerina.compiler.syntax.tree.RollbackStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TransactionStatementNode;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class will check whether an import statement to internal transaction repo is needed or not.
 * @since 2.0.0
 */
public class TransactionImportValidator extends NodeVisitor {

    private boolean importTransactionPackage;

    public boolean shouldImportTransactionPackage(ModulePartNode modulePartNode) {
        modulePartNode.accept(this);
        return importTransactionPackage;
    }

    @Override
    public void visit(TransactionStatementNode transactionStatementNode) {
        importTransactionPackage = true;
    }

    @Override
    public void visit(CommitActionNode commitActionNode) {
        importTransactionPackage = true;
    }

    @Override
    public void visit(RollbackStatementNode rollbackStatementNode) {
        importTransactionPackage = true;
    }

    @Override
    public void visit(RetryStatementNode retryStatementNode) {
        StatementNode statementNode = retryStatementNode.retryBody();
        if (statementNode.kind() == SyntaxKind.TRANSACTION_STATEMENT) {
            importTransactionPackage = true;
        }
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        List<String> qualifiers = functionDefinitionNode.qualifierList().stream().map(Token::text)
                .collect(Collectors.toList());
        if (qualifiers.contains(SyntaxKind.TRANSACTIONAL_KEYWORD.stringValue()) &&
                qualifiers.contains(SyntaxKind.RESOURCE_KEYWORD.stringValue())) {
            importTransactionPackage = true;
        }
        super.visit(functionDefinitionNode);
    }

    @Override
    public void visit(TransactionalExpressionNode transactionalExpressionNode) {
        importTransactionPackage = true;
    }

    protected void visitSyntaxNode(Node node) {
        if (importTransactionPackage) {
            return;
        }
        super.visitSyntaxNode(node);
    }
}
