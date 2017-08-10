/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';

/**
 * Source Generation for the Transaction Aborted Statement
 */
class TransactionAbortedStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Transaction Aborted Statement
     * @return {boolean} true|false - whether the transaction aborted statement can visit or not
     */
    canVisitTransactionAbortedStatement() {
        return true;
    }

    /**
     * Begin visit fot the Transaction Aborted Statement
     * @param {TransactionAbortedStatement} statement - Transaction Aborted statement ASTNode
     */
    beginVisitTransactionAbortedStatement(statement) {
        this.parentNode = statement;
        if (statement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        statement.setLineNumber(lineNumber, { doSilently: true });
    }

    /**
     * Visit Transaction Statement
     * @param {TransactionStatement} statement - Transaction Statement ASTNode
     */
    visitTransactionStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Aborted Statement
     * @param {AbortedStatement} statement - Aborted Statement ASTNode
     */
    visitAbortedStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Committed Statement
     * @param {CommittedStatement} statement - Committed Statement ASTNode
     */
    visitCommittedStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * End Visit Transaction Aborted Statement
     * @param {TransactionAbortedStatement} statement - Transaction Aborted statement ASTNode
     */
    endVisitTransactionAbortedStatement(statement) {
        this.appendSource((statement.whiteSpace.useDefault)
            ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }
    /**
     * Visit connector declarations
     * @param {ConnectorDeclaration} connectorDeclaration - connector declaration AST node
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }
}

export default TransactionAbortedStatementVisitor;
