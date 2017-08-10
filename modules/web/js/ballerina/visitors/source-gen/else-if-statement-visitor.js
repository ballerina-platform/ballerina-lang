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

import _ from 'lodash';
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';

/**
 * Source Generation for the Else Statement
 */
class ElseIfStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Else If statement
     * @return {boolean} true|false - whether the Else If statement can visit or not
     */
    canVisitElseIfStatement() {
        return true;
    }

    /**
     * Begin visit for the Else statement
     * @param {ElseIfStatement} elseIfStatement - Else If statement ASTNode
     */
    beginVisitElseIfStatement(elseIfStatement) {
        this.node = elseIfStatement;

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        elseIfStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'else' + elseIfStatement.getWSRegion(1) + 'if' + elseIfStatement.getWSRegion(2)
            + '(' + elseIfStatement.getWSRegion(3) + elseIfStatement.getConditionString()
            + ')' + elseIfStatement.getWSRegion(4) + '{' + elseIfStatement.getWSRegion(5)
            + ((elseIfStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * End visit for the Else If Statement
     * @param {ElseIfStatement} elseIfStatement - Else If Statement ASTNode
     */
    endVisitElseIfStatement(elseIfStatement) {
        this.outdent();
        const elseIfStmts = elseIfStatement.getParent().getElseIfStatements();
        const elseStmt = elseIfStatement.getParent().getElseStatement();
        // if using default ws, add a new line to end unless there are anymore elseif stmts available
        // or an else statement is available
        let tailingWS = (elseIfStatement.whiteSpace.useDefault
            && (_.isNil(elseStmt) && _.isEqual(_.last(elseIfStmts), elseIfStatement)))
            ? '\n' : elseIfStatement.getWSRegion(6);

        // if there is a newly added else-if or an else stmt after this
        // reset tailing whitespace
        let nextBlock;
        if (!_.isEqual(_.last(elseIfStmts), elseIfStatement)) {
            nextBlock = _.nth(elseIfStmts, _.indexOf(elseIfStmts, elseIfStatement) + 1);
        } else if (!_.isNil(elseStmt)) {
            nextBlock = elseStmt;
        }
        tailingWS = (!_.isNil(nextBlock) && nextBlock.whiteSpace.useDefault)
                        ? ' ' : tailingWS;

        const constructedSourceSegment = '}' + tailingWS;

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit Statements
     * @param {Statement} statement - statement ASTNode
     */
    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
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

export default ElseIfStatementVisitor;
