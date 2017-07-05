/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Source generation for the IF Else Statement
 */
class IfElseStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the If Else statement
     * @return {boolean} true|false - check whether the If Else statement can be visited
     */
    canVisitIfElseStatement() {
        return true;
    }

    /**
     * Begin visit fot the IF Else Statement
     * @param {IfElseStatement} statement - If Else statement ASTNode
     */
    beginVisitIfElseStatement(statement) {
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
     * Visit the If Statement
     * @param {IfStatement} statement - If Statement ASTNode
     */
    visitIfStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit the Else If Statement
     * @param {ElseIfStatement} statement - Else If Statement ASTNode
     */
    visitElseIfStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit the Else Statement
     * @param {ElseStatement} statement - Else Statement ASTNode
     */
    visitElseStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * End visit fot the IF Else Statement
     * @param {IfElseStatement} statement - If Else statement ASTNode
     */
    endVisitIfElseStatement(statement) {
        this.appendSource((statement.whiteSpace.useDefault)
          ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default IfElseStatementVisitor;
